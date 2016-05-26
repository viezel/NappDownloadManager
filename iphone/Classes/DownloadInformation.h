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

@interface DownloadInformation : NSObject <NSCoding>
{
}

@property (retain) NSObject* data;
@property (retain) NSString* url;
@property (retain) NSString* name;
@property (retain) NSString* locale;
@property (retain) NSString* filePath;
@property (retain) NSString* storageLocation;
@property (retain) NSDictionary* headers;
@property NetworkTypes permittedNetworkTypes;
@property NSUInteger length;
@property NSInteger mediaBitsPerSecond;
@property NSUInteger availableLength;
@property (retain) NSDate* creationUtc;
@property (retain) NSDate* lastWriteUtc;
@property NSInteger lastDownloadBitsPerSecond;
@property double downloadPriority;
@property BOOL isReadyForPlayback;
@property (retain) NSString* message;


@end
