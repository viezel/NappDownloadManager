/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#include "BaseDownloadRequest.h"

@interface DownloadRequest : BaseDownloadRequest <NSCoding>
{
}

@property (retain) NSString* filePath;
@property (retain) NSString* url;
@property (retain) NSString* fileName;
@property NSInteger mediaBitsPerSecond;

@property (retain) NSDate* lastWriteUtc;
@property NSInteger lastDownloadBitsPerSecond;
@property BOOL isReadyForPlayback;

-(NSDictionary*)convertToDict;
-(void)initWithDict:(NSDictionary*)data;


@end
