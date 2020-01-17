import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_crash_demo/flutter_crash_demo.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_crash_demo');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterCrashDemo.platformVersion, '42');
  });
}
