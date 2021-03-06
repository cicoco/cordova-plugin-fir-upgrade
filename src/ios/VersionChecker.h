//
//  VersionChecker.h
//  aeolian
//
//  Created by 顾九 on 2020/10/14.
//

#import <Foundation/Foundation.h>

typedef void(^CheckBlock)(NSString *upgradeUrl);

@interface VersionChecker : NSObject
/**
 *  根据当前项目 build 版本号检查新版本，有则自动弹出 UIAlertView 提醒
 */
+ (void)check:(CheckBlock) block;

@end
