/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "BaseDownloadRequest.h"

@implementation BaseDownloadRequest

@synthesize name = _name;
@synthesize locale = _locale;
@synthesize creationUtc = _creationUtc;
@synthesize overrideStorageLocation = _overrideStorageLocation;
@synthesize headers;
@synthesize priority = _priority;
@synthesize overridePermittedNetworkTypes = _overridePermittedNetworkTypes;
@synthesize length = _length;
@synthesize availableLength = _availableLength;
@synthesize finalStorageLocation = _finalStorageLocation;
@synthesize status = _status;
@synthesize finalPermittedNetworkTypes = _finalPermittedNetworkTypes;


- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        self.priority = 0.2;
        self.status = DownloadStatusNone;
    }
    
    return self;
}

- (void)dealloc
{
    [self.name release];
    [self.locale release];
    [self.creationUtc release];
    [self.overrideStorageLocation release];
    [self.finalStorageLocation release];
    [self.headers release];
    
    [super dealloc];
}

@end
