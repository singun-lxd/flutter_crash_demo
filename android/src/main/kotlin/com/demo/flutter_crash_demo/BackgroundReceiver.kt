package com.demo.flutter_crash_demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.flutter.view.FlutterMain

class BackgroundReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BackgroundReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "BackgroundReceiver onReceive")
        FlutterMain.ensureInitializationComplete(context, null)
        MethodRunService.enqueueWork(context, intent)
    }
}