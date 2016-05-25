/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "DownloadInformation.h"

@implementation DownloadInformation

@synthesize data = _data;
@synthesize url = _url;
@synthesize name = _name;
@synthesize locale= _locale;
@synthesize filePath = _filePath;
@synthesize storageLocation = _storageLocation;
@synthesize headers = _headers;
@synthesize permittedNetworkTypes = _permittedNetworkTypes;
@synthesize length = _length;
@synthesize mediaBitsPerSecond = _mediaBitsPerSecond;
@synthesize availableLength = _availableLength;
@synthesize creationUtc = _creationUtc;
@synthesize lastWriteUtc = _lastWriteUtc;
@synthesize lastDownloadBitsPerSecond = _lastDownloadBitsPerSecond;
@synthesize downloadPriority = _downloadPriority;
@synthesize isReadyForPlayback = _isReadyForPlayback;
@synthesize message = _message;

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        self.filePath = nil;
    }
    
    return self;
}

- (void)dealloc
{
    [self.data release];
    [self.url release];
    [self.name release];
    [self.locale release];
    [self.filePath release];
    [self.storageLocation release];
    [self.headers release];
    [self.creationUtc release];
    [self.lastWriteUtc release];
    [self.message release];
    
    [super dealloc];    
}

- (id) initWithCoder:(NSCoder *)aDecoder
{
    if (self = [super init]) {        
        self.data = [aDecoder decodeObjectForKey:@"data"];        
        self.url = [aDecoder decodeObjectForKey:@"url"];   
        self.name = [aDecoder decodeObjectForKey:@"name"];   
        self.locale = [aDecoder decodeObjectForKey:@"locale"];   
        self.filePath = [aDecoder decodeObjectForKey:@"filePath"];   
        self.storageLocation = [aDecoder decodeObjectForKey:@"storageLocation"];
        self.headers = [aDecoder decodeObjectForKey:@"headers"];
        self.permittedNetworkTypes = [aDecoder decodeIntegerForKey:@"permittedNetworkTypes"];   
        self.length = [aDecoder decodeIntegerForKey:@"length"];   
        self.mediaBitsPerSecond = [aDecoder decodeIntegerForKey:@"mediaBitsPerSeconds"];   
        self.availableLength = [aDecoder decodeIntegerForKey:@"availableLength"];   
        self.creationUtc = [aDecoder decodeObjectForKey:@"creationUtc"];   
        self.lastWriteUtc = [aDecoder decodeObjectForKey:@"lastWriteUtc"];   
        self.lastDownloadBitsPerSecond = [aDecoder decodeIntegerForKey:@"lastDownloadBitsPerSecond"];   
        self.downloadPriority = [aDecoder decodeDoubleForKey:@"downloadPriority"];   
        self.isReadyForPlayback = [aDecoder decodeBoolForKey:@"isReadyForPlayback"];
        self.message = [aDecoder decodeObjectForKey:@"message"];
        
    }
    return self;    
}

- (void) encodeWithCoder:(NSCoder *)aCoder
{
    [aCoder encodeObject: self.data forKey:@"data"];        
    [aCoder encodeObject: self.url forKey:@"url"];   
    [aCoder encodeObject: self.name forKey:@"name"];   
    [aCoder encodeObject: self.locale forKey:@"locale"];   
    [aCoder encodeObject: self.filePath forKey:@"filePath"];   
    [aCoder encodeObject: self.storageLocation forKey:@"storageLocation"];
    [aCoder encodeObject: self.headers forKey:@"headers"];
    [aCoder encodeInt: self.permittedNetworkTypes forKey:@"permittedNetworkTypes"];   
    [aCoder encodeInt: self.length forKey:@"length"];   
    [aCoder encodeInt: self.mediaBitsPerSecond forKey:@"mediaBitsPerSeconds"];   
    [aCoder encodeInt: self.availableLength forKey:@"availableLength"];   
    [aCoder encodeObject: self.creationUtc forKey:@"creationUtc"];   
    [aCoder encodeObject: self.lastWriteUtc forKey:@"lastWriteUtc"];   
    [aCoder encodeInt: self.lastDownloadBitsPerSecond forKey:@"lastDownloadBitsPerSecond"];   
    [aCoder encodeDouble: self.downloadPriority forKey:@"downloadPriority"];   
    [aCoder encodeBool:self.isReadyForPlayback forKey:@"isReadyForPlayback"];
    [aCoder encodeObject: self.message forKey:@"message"];
}

@end
