import 'dart:async';

import 'crash_reproducer.dart';

abstract class FlutterCrashDemo {
  factory FlutterCrashDemo() => CrashReproducer.instance;

  Future initialize();

  Future runInBackground(Function function);
}
