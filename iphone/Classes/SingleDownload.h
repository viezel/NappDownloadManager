/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import <Foundation/Foundation.h>
#import "DownloadRequest.h"
#import "DownloadInformation.h"


@protocol SingleDownloadDelegate <NSObject>
- (void) downloadCompleted: (DownloadInformation *) information;
- (void) downloadProgress: (DownloadInformation *) information;
- (BOOL) canContinue: (DownloadInformation *) information;
- (void) downloadFailed: (DownloadInformation *) information;
- (void) downloadRestart: (DownloadInformation *) information;
- (void) downloadStarted: (DownloadInformation *) information;
@end

@interface SingleDownload : NSObject
{
}


@property (retain, nonatomic) id <SingleDownloadDelegate> delegate;
@property (retain) DownloadRequest* downloadRequest;
@property (retain) NSNumber* permittedNetworks;

-(void)start;
-(void)startRequest;


@end
