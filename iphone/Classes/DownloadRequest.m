/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "DownloadRequest.h"

@implementation DownloadRequest

@synthesize filePath = _filePath;
@synthesize url = _url;
@synthesize fileName = _fileName;
@synthesize mediaBitsPerSecond = _mediaBitsPerSecond;

@synthesize lastWriteUtc = _lastWriteUtc;
@synthesize lastDownloadBitsPerSecond = _lastDownloadBitsPerSecond;
@synthesize isReadyForPlayback = _isReadyForPlayback;


- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
    }
    
    return self;
}

- (void)dealloc
{
    [self.filePath release];
    [self.url release];
    [self.fileName release];
    [self.lastWriteUtc release];
    
    [super dealloc];
}

- (NSDictionary*) convertToDict
{
    NSMutableDictionary* dict = [[[NSMutableDictionary alloc] init] autorelease];
    [dict setValue:self.url forKey:@"url"];
    [dict setValue:self.name forKey:@"name"];
    [dict setValue:self.locale forKey:@"locale"];
    [dict setValue:self.filePath forKey:@"filePath"];
    [dict setValue:self.fileName forKey:@"fileName"];
    [dict setValue:self.overrideStorageLocation forKey:@"storageLocation"];
    [dict setValue:[NSNumber numberWithInt:self.overridePermittedNetworkTypes] forKey:@"permittedNetworkTypes"];
    [dict setValue:[NSNumber numberWithInt:self.length] forKey:@"length"];
    [dict setValue:[NSNumber numberWithInt:self.mediaBitsPerSecond] forKey:@"mediaBitsPerSeconds"];
    [dict setValue:[NSNumber numberWithInt:self.availableLength] forKey:@"availableLength"];
    [dict setValue:self.creationUtc forKey:@"creationUtc"];
    [dict setValue:self.lastWriteUtc forKey:@"lastWriteUtc"];
    [dict setValue:[NSNumber numberWithInt:self.lastDownloadBitsPerSecond] forKey:@"lastDownloadBitsPerSecond"];
    [dict setValue:[NSNumber numberWithDouble:self.priority] forKey:@"downloadPriority"];
    [dict setValue:[NSNumber numberWithInt:self.isReadyForPlayback] forKey:@"isReadyForPlayback"];
    [dict setValue:self.finalStorageLocation forKey:@"finalstorageLocation"];
    [dict setValue:[NSNumber numberWithInt:self.finalPermittedNetworkTypes] forKey:@"finalpermittedNetworkTypes"];
    [dict setValue:[NSNumber numberWithInt:self.status] forKey:@"status"];

    return dict;
}

- (void) initWithDict:(NSDictionary *)data
{
    self.url = [data valueForKey:@"url"];
    self.name = [data valueForKey:@"name"];
    self.locale = [data valueForKey:@"locale"];
    self.filePath = [data valueForKey:@"filePath"];
    self.fileName = [data valueForKey:@"fileName"];
    self.overrideStorageLocation = [data valueForKey:@"storageLocation"];
    self.overridePermittedNetworkTypes = [[data valueForKey:@"permittedNetworkTypes"] intValue];
    self.length = [[data valueForKey:@"length"] intValue];
    self.mediaBitsPerSecond = [[data valueForKey:@"mediaBitsPerSeconds"] intValue];
    self.availableLength = [[data valueForKey:@"availableLength"] intValue];
    self.creationUtc = [data valueForKey:@"creationUtc"];
    self.lastWriteUtc = [data valueForKey:@"lastWriteUtc"];
    self.lastDownloadBitsPerSecond = [[data valueForKey:@"lastDownloadBitsPerSecond"] intValue];
    self.priority = [[data valueForKey:@"downloadPriority"] doubleValue];
    self.isReadyForPlayback = [[data valueForKey:@"isReadyForPlayback"] intValue];
    self.finalStorageLocation = [data valueForKey:@"finalstorageLocation"];
    self.finalPermittedNetworkTypes = [[data valueForKey:@"finalpermittedNetworkTypes"] intValue];
    self.status = [[data valueForKey:@"status"] intValue];
}

- (id) initWithCoder:(NSCoder *)aDecoder
{
    if (self = [super init]) {
        self.url = [aDecoder decodeObjectForKey:@"url"];   
        self.name = [aDecoder decodeObjectForKey:@"name"];   
        self.locale = [aDecoder decodeObjectForKey:@"locale"];   
        self.filePath = [aDecoder decodeObjectForKey:@"filePath"];   
        self.fileName = [aDecoder decodeObjectForKey:@"fileName"];   
        self.overrideStorageLocation = [aDecoder decodeObjectForKey:@"storageLocation"];   
        self.overridePermittedNetworkTypes = [aDecoder decodeIntegerForKey:@"permittedNetworkTypes"];   
        self.length = [aDecoder decodeIntegerForKey:@"length"];   
        self.mediaBitsPerSecond = [aDecoder decodeIntegerForKey:@"mediaBitsPerSeconds"];   
        self.availableLength = [aDecoder decodeIntegerForKey:@"availableLength"];   
        self.creationUtc = [aDecoder decodeObjectForKey:@"creationUtc"];   
        self.lastWriteUtc = [aDecoder decodeObjectForKey:@"lastWriteUtc"];   
        self.lastDownloadBitsPerSecond = [aDecoder decodeIntegerForKey:@"lastDownloadBitsPerSecond"];   
        self.priority = [aDecoder decodeDoubleForKey:@"downloadPriority"];   
        self.isReadyForPlayback = [aDecoder decodeBoolForKey:@"isReadyForPlayback"];        
        self.finalStorageLocation = [aDecoder decodeObjectForKey:@"finalstorageLocation"];   
        self.finalPermittedNetworkTypes = [aDecoder decodeIntegerForKey:@"finalpermittedNetworkTypes"];   
        self.status = [aDecoder decodeIntegerForKey:@"status"];
    }
    return self;    
}

- (void) encodeWithCoder:(NSCoder *)aCoder
{        
    [aCoder encodeObject: self.url forKey:@"url"];   
    [aCoder encodeObject: self.name forKey:@"name"];   
    [aCoder encodeObject: self.locale forKey:@"locale"];   
    [aCoder encodeObject: self.filePath forKey:@"filePath"];
    [aCoder encodeObject: self.fileName forKey:@"fileName"];   
    [aCoder encodeObject: self.overrideStorageLocation forKey:@"storageLocation"];   
    [aCoder encodeInt: self.overridePermittedNetworkTypes forKey:@"permittedNetworkTypes"];   
    [aCoder encodeInt: self.length forKey:@"length"];   
    [aCoder encodeInt: self.mediaBitsPerSecond forKey:@"mediaBitsPerSeconds"];   
    [aCoder encodeInt: self.availableLength forKey:@"availableLength"];   
    [aCoder encodeObject: self.creationUtc forKey:@"creationUtc"];   
    [aCoder encodeObject: self.lastWriteUtc forKey:@"lastWriteUtc"];   
    [aCoder encodeInt: self.lastDownloadBitsPerSecond forKey:@"lastDownloadBitsPerSecond"];   
    [aCoder encodeDouble: self.priority forKey:@"downloadPriority"];   
    [aCoder encodeBool:self.isReadyForPlayback forKey:@"isReadyForPlayback"];
    [aCoder encodeObject: self.finalStorageLocation forKey:@"finalstorageLocation"];   
    [aCoder encodeInt: self.finalPermittedNetworkTypes forKey:@"finalpermittedNetworkTypes"];   
    [aCoder encodeInt: self.status forKey:@"status"];   
}

@end
