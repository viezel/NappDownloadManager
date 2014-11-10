/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import <Foundation/Foundation.h>
#import "NetworkTypes.h"
#import "DownloadRequest.h"


@interface DownloadQueue : NSObject
{
}

@property (retain, readonly) NSMutableArray* downloadRequests;

-(NSUInteger)getDownloadRequestCount;
-(DownloadRequest*)remove:(NSString*)url;
-(void)add:(DownloadRequest*)downloadRequest
defaultStorageLocation:(NSString*)storageLocation
defaultPermittedNetworks:(NetworkTypes)permittedNetworks;

-(DownloadRequest*)getDownloadRequest:(NSString*)url;
-(void)setRequest:(NSString*)url
         toStatus:(DownloadStatus)status;
-(NSArray*)getQueuedDownloadRequestUrls;
-(DownloadRequest*)getNextDownloadCandidate:(NetworkTypes)network;
-(NSArray*)permittedNetworkTypesChanged:(NetworkTypes)permitted;

-(void)persistToStorage;
-(void)loadFromStorage;

@end
