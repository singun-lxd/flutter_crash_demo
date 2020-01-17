#import "FlutterCrashDemoPlugin.h"
#import <flutter_crash_demo/flutter_crash_demo-Swift.h>

@implementation FlutterCrashDemoPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterCrashDemoPlugin registerWithRegistrar:registrar];
}
@end
