import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter_crash_demo/flutter_crash_demo.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    if (kDebugMode) {
      await Future.delayed(Duration(milliseconds: 800));
    }
    await FlutterCrashDemo().initialize();
    await FlutterCrashDemo().runInBackground(_onBackgroundCall);

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {

    });
  }

  static void _onBackgroundCall() {
    print("call from background isolate");
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter Crash Demo'),
        ),
        body: Center(
          child: Text('Test'),
        ),
      ),
    );
  }
}
