package com.demo.flutter_crash_demo

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.core.app.JobIntentService
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

class MethodRunService : MethodChannel.MethodCallHandler, JobIntentService() {
    private val queue = ArrayDeque<Intent>()
    private lateinit var mBackgroundChannel: MethodChannel
    private lateinit var mContext: Context
    private val mHandler: Handler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(mainLooper)
    }

    companion object {
        @JvmStatic
        private val TAG = "MethodRunService"
        @JvmStatic
        private val CHANNEL_NAME = "com.demo.crash.plugins/method_run"
        @JvmStatic
        private val METHOD_PREFIX = "MethodRun"
        @JvmStatic
        private val JOB_ID = 12345678
        @JvmStatic
        private var sBackgroundFlutterView: FlutterNativeView? = null
        @JvmStatic
        private val sServiceStarted = AtomicBoolean(false)

        @JvmStatic
        private lateinit var sPluginRegistrantCallback: PluginRegistry.PluginRegistrantCallback

        @JvmStatic
        fun enqueueWork(context: Context, work: Intent) {
            try {
                enqueueWork(context, MethodRunService::class.java, JOB_ID, work)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun setPluginRegistrant(callback: PluginRegistry.PluginRegistrantCallback) {
            sPluginRegistrantCallback = callback
        }
    }

    private fun checkBackgroundView(context: Context) {
        Log.i(TAG, "checkBackgroundView")
        synchronized(sServiceStarted) {
            mContext = context
            if (sBackgroundFlutterView == null) {
                val callbackHandle = context.getSharedPreferences(
                        FlutterCrashDemoPlugin.SHARED_PREFERENCES_KEY,
                        Context.MODE_PRIVATE)
                        .getLong(FlutterCrashDemoPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0)
                Log.i(TAG, "callbackHandle = $callbackHandle")
                val callbackInfo: FlutterCallbackInformation? = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                if (callbackInfo == null) {
                    Log.e(TAG, "Fatal: failed to find callback")
                    return
                }
                Log.i(TAG, "creating flutter background view...")
                val nativeView = FlutterNativeView(context, true)

                val registry = nativeView.pluginRegistry
                sPluginRegistrantCallback.registerWith(registry)

                val args = FlutterRunArguments()
                args.bundlePath = FlutterMain.findAppBundlePath()
                args.entrypoint = callbackInfo.callbackName
                args.libraryPath = callbackInfo.callbackLibraryPath

                nativeView.runFromBundle(args)

                sBackgroundFlutterView = nativeView

                Log.i(TAG, "background isolate initialized")
            }
        }
        mBackgroundChannel = MethodChannel(sBackgroundFlutterView, CHANNEL_NAME)
        mBackgroundChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call.method) {
            "$METHOD_PREFIX.initialized" -> {
                synchronized(sServiceStarted) {
                    while (!queue.isEmpty()) {
                        executeDartCallbackInBackgroundIsolate(queue.remove())
                    }
                    sServiceStarted.set(true)
                }
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    override fun onCreate() {
        super.onCreate()
        checkBackgroundView(this)
    }

    override fun onHandleWork(intent: Intent) {
        synchronized(sServiceStarted) {
            if (!sServiceStarted.get()) {
                // Queue up events while background isolate is starting
                queue.add(intent)
                return
            }
        }

        // There were no pre-existing callback requests. Execute the callback
        // specified by the incoming intent.
        val latch = CountDownLatch(1)
        mHandler.post { executeDartCallbackInBackgroundIsolate(intent, latch) }

        try {
            latch.await()
        } catch (ex: InterruptedException) {
            Log.i(TAG, "Exception waiting to execute Dart callback", ex)
        }
    }

    /**
     * Executes the desired Dart callback in a background Dart isolate.
     *
     *
     * The given `intent` should contain a `long` extra called "callbackHandle", which
     * corresponds to a callback registered with the Dart VM.
     */
    private fun executeDartCallbackInBackgroundIsolate(
            intent: Intent, latch: CountDownLatch? = null) {
        // Grab the handle for the callback associated event. Pay close
        // attention to the type of the callback handle as storing this value in a
        // variable of the wrong size will cause the callback lookup to fail.
        val callbackHandle = intent.getLongExtra(FlutterCrashDemoPlugin.CALLBACK_HANDLE_KEY, 0)

        // If another thread is waiting, then wake that thread when the callback returns a result.
        var result: MethodChannel.Result? = null
        if (latch != null) {
            result = object : MethodChannel.Result {
                override fun success(result: Any?) {
                    latch.countDown()
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    latch.countDown()
                }

                override fun notImplemented() {
                    latch.countDown()
                }
            }
        }

        // Handle the callback data in Dart
        mBackgroundChannel.invokeMethod(
                "", listOf(callbackHandle), result)
    }
}