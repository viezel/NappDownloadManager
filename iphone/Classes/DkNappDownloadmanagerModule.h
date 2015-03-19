/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "TiModule.h"
#import "Downloader.h"

@interface DkNappDownloadmanagerModule : TiModule <DownloaderDelegate>
{
}


@property(nonatomic,readonly) NSNumber *NETWORK_TYPE_WIFI;
@property(nonatomic,readonly) NSNumber *NETWORK_TYPE_MOBILE;
@property(nonatomic,readonly) NSNumber *NETWORK_TYPE_ANY;

@property(nonatomic,readonly) NSNumber *DOWNLOAD_PRIORITY_LOW;
@property(nonatomic,readonly) NSNumber *DOWNLOAD_PRIORITY_NORMAL;
@property(nonatomic,readonly) NSNumber *DOWNLOAD_PRIORITY_HIGH;

-(void)setMaximumSimultaneousDownloads:(id)value;
-(id)maximumSimultaneousDownloads;

-(void)setPermittedNetworkTypes:(id)value;
-(id)permittedNetworkTypes;

-(void)addDownload:(id)args;

-(void)stopDownloader:(id)args;
-(void)restartDownloader:(id)args;

-(void)pauseAll:(id)args;
-(void)pauseItem:(id)args;
-(void)resumeAll:(id)args;
-(void)resumeItem:(id)args;
-(void)cancelItem:(id)args;
-(void)deleteItem:(id)args;

-(void)deleteQueue:(id)args;

-(id)getDownloadInfo:(id)args;
-(id)getAllDownloadInfo:(id)args;

@end
