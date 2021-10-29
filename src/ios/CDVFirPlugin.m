#import "CDVFirPlugin.h"

#import "VersionChecker.h"

@implementation CDVFirPlugin


- (id)settingForKey:(NSString*)key
{
    return [self.commandDelegate.settings objectForKey:[key lowercaseString]];
}

- (void)versionCheck:(CDVInvokedUrlCommand*)command {
    
    if ([self settingForKey:@"AppleUrl"]) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString: [self settingForKey:@"AppleUrl"]] options:@{} completionHandler:nil];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    __weak CDVFirPlugin* weakSelf = self;
    
    [VersionChecker check:^(NSString *upgradeUrl) {
        if (upgradeUrl == nil){
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:NO];
            [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        }

        UIAlertController *alert = [UIAlertController
                                    alertControllerWithTitle:NSLocalizedString(@"NewVersion", nil)
                                    message:NSLocalizedString(@"New is coming, upgrade?", nil)
                                    preferredStyle:UIAlertControllerStyleAlert];

        UIAlertAction *confirm = [UIAlertAction
                                  actionWithTitle:NSLocalizedString(@"GoToUpdate", nil)
                                  style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString: upgradeUrl] options:@{} completionHandler:nil];


        }];
        UIAlertAction *cancel = [UIAlertAction
                                  actionWithTitle:NSLocalizedString(@"IgnoreUpdate", nil)
                                  style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        }];

        [alert addAction:confirm];
        [alert addAction:cancel];
        [weakSelf.viewController presentViewController:alert animated:YES completion:nil];
            
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
        [weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

@end
