//
//  VersionChecker.m
//  aeolian
//
//  Created by 顾九 on 2020/10/14.
//


#import <UIKit/UIKit.h>
#import "VersionChecker.h"


@implementation VersionChecker


+ (void)check:(CheckBlock) block{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"FirConfig" ofType:@"plist"];
        NSMutableDictionary *data = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
        NSString *appId = [data objectForKey:@"FIR_IOS_APP_ID"];
        NSString *apiToken = [data objectForKey:@"FIR_API_TOKEN"];

        NSString *idUrlString = [NSString stringWithFormat:@"http://api.bq04.com/apps/latest/%@?api_token=%@",appId, apiToken];
        NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:idUrlString]];

        NSURLSessionTask *dataTask = [[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (!error && data) {
                    NSError *jsonError = nil;
                    id object = [NSJSONSerialization JSONObjectWithData:data options:0 error:&jsonError];
                    if (!jsonError && [object isKindOfClass:[NSDictionary class]]) {
                        NSString *code = object[@"code"];
                        NSString *errors = object[@"errors"];

                        if (code && errors) {
                            block(nil);
                        } else {
                            NSString *version = object[@"version"];
                            NSString *build = object[@"build"];
                            NSString *newUrl = object[@"update_url"];
                            NSString *currentBuild = [[NSBundle mainBundle] objectForInfoDictionaryKey:(NSString *)kCFBundleVersionKey];

                            build = [build stringByReplacingOccurrencesOfString:@"." withString:@""];
                            currentBuild = [currentBuild stringByReplacingOccurrencesOfString:@"." withString:@""];

                            if ([build integerValue] > [currentBuild integerValue]) {
                                NSLog(@"FIR － 检测到新版本 v%@(%@), url:%@ ",version,build, newUrl);
                                block(newUrl);
                            } else {
                                block(nil);
                            }
                        }
                    }
                } else {
                    block(nil);
                }
            });
        }];
        [dataTask resume];
    });
}
@end