import 'dart:async';
import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

final MethodChannel _bgChannel =
    MethodChannel("com.demo.crash.plugins/method_run");

// This is the entrypoint for the background isolate. Since we can only enter
// an isolate once, we setup a MethodChannel to listen for method invokations
// from the native portion of the plugin. This allows for the plugin to perform
// any necessary processing in Dart (e.g., populating a custom object) before
// invoking the provided callback.
void callbackDispatcher() {
  // Setup Flutter state needed for MethodChannels.
  WidgetsFlutterBinding.ensureInitialized();

  _bgChannel.setMethodCallHandler(MethodCallHandler.instance._handleCallback);

  // Once we've finished initializing, let the native portion of the plugin
  // know that it can start scheduling alarms.
  try {
    _bgChannel.invokeMethod('MethodRun.initialized');
  } on PlatformException catch (e) {
    print("service initialized error: $e");
  }
}

class MethodCallHandler {
  static MethodCallHandler _instance;

  static MethodCallHandler get instance =>
      _instance ??= MethodCallHandler._internal();

  factory MethodCallHandler._() => instance;

  MethodCallHandler._internal();

  // This is where the magic happens and we handle background events from the
  // native portion of the plugin.
  Future _handleCallback(MethodCall call) async {
    final List<dynamic> args = call.arguments;
    if (args.length != 1) {
      print("callback args error");
      return;
    }
    // PluginUtilities.getCallbackFromHandle performs a lookup based on the
    // callback handle and returns a tear-off of the original callback.
    final Function closure = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));
    if (closure == null) {
      print("callback is null");
      return;
    }

    closure();
  }
}
