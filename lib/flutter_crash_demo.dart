import 'dart:async';

import 'package:flutter/services.dart';

import 'crash_reproducer.dart';

abstract class FlutterCrashDemo {
  factory FlutterCrashDemo() => CrashReproducer.instance;

  Future initialize();

  Future runInBackground(Function function);
}
