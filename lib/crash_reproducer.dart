import 'dart:ui';

import 'package:flutter/services.dart';

import 'callback_dispatcher.dart';
import 'flutter_crash_demo.dart';

class CrashReproducer implements FlutterCrashDemo {
  static CrashReproducer _instance;

  static CrashReproducer get instance => _instance ??= CrashReproducer._();

  CrashReproducer._();

  final MethodChannel _channel =
      MethodChannel("com.demo.crash.plugins/entrance");

  @override
  Future<void> initialize() async {
    final CallbackHandle callback =
        PluginUtilities.getCallbackHandle(callbackDispatcher);
    await _channel.invokeMethod('FlutterBackground.initializeService',
        <dynamic>[callback.toRawHandle()]);
  }

  @override
  Future<void> runInBackground(Function function) async {
    final CallbackHandle callback = PluginUtilities.getCallbackHandle(function);
    await _channel.invokeMethod(
        'FlutterBackground.runInBackground', <dynamic>[callback.toRawHandle()]);
  }
}
