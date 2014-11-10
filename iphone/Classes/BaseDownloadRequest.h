/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#include "DownloadStatus.h"
#include "NetworkTypes.h"

@interface BaseDownloadRequest : NSObject
{
}

@property (retain) NSString* name;
@property (retain) NSString* locale;
@property (retain) NSDate* creationUtc;
@property (retain) NSString* overrideStorageLocation;
@property double priority;
@property NetworkTypes overridePermittedNetworkTypes;
@property NSUInteger length;
@property NSUInteger availableLength;
@property (retain) NSString* finalStorageLocation;
@property DownloadStatus status;
@property NetworkTypes finalPermittedNetworkTypes;



@end
