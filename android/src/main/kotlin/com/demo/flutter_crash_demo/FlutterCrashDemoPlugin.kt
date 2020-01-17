package com.demo.flutter_crash_demo

import android.content.Context
import android.content.Intent
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterCrashDemoPlugin(registrar: Registrar): MethodCallHandler {
  private val context: Context = registrar.context()

  companion object {
    @JvmStatic
    private val TAG = "FlutterCrashDemoPlugin"
    @JvmStatic
    private val CHANNEL_NAME = "com.demo.crash.plugins/entrance"
    @JvmStatic
    private val METHOD_PREFIX = "FlutterBackground"
    @JvmStatic
    val SHARED_PREFERENCES_KEY = "background_cache"
    @JvmStatic
    val CALLBACK_HANDLE_KEY = "callback_handle"
    @JvmStatic
    val CALLBACK_DISPATCHER_HANDLE_KEY = "callback_dispatch_handler"
    @JvmStatic
    val EXECUTION_ACTION = "com.demo.crash.BackgroundAction"

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
      val plugin = FlutterCrashDemoPlugin(registrar)
      channel.setMethodCallHandler(plugin)
    }

    @JvmStatic
    private fun initializeService(context: Context, args: List<*>?) {
      Log.d(TAG, "initializeService")
      val callbackHandle = args!![0] as Long
      context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
              .edit()
              .putLong(CALLBACK_DISPATCHER_HANDLE_KEY, callbackHandle)
              .apply()
    }

    @JvmStatic
    private fun runInBackground(context: Context, args: List<*>?) {
      Log.d(TAG, "runInBackground")
      val callbackHandle = args!![0] as Long
      broadcastToRun(context, callbackHandle)
    }

    @JvmStatic
    fun getBroadcastIntent(context: Context, callbackHandle: Long): Intent {
      return Intent(EXECUTION_ACTION)
              .setClass(context, BackgroundReceiver::class.java)
              .putExtra(CALLBACK_HANDLE_KEY, callbackHandle)
    }

    @JvmStatic
    fun broadcastToRun(context: Context, callbackHandle: Long) {
      val intent = getBroadcastIntent(context, callbackHandle)
      try {
        context.sendBroadcast(intent)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    val args = call.arguments<List<*>>()
    when(call.method) {
      "$METHOD_PREFIX.initializeService" -> {
        initializeService(context, args)
        result.success(true)
      }
      "$METHOD_PREFIX.runInBackground" -> {
        runInBackground(context, args)
        result.success(true)
      }
      else -> result.notImplemented()
    }
  }
}
