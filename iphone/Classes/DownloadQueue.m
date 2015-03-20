/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "DownloadQueue.h"

@interface DownloadQueue()
{
    dispatch_queue_t queue;    
}

@property (retain, readwrite) NSMutableArray* downloadRequests;

@end


@implementation DownloadQueue

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        queue = dispatch_queue_create("dk.napp.downloadqueue", 0);
        self.downloadRequests = [[NSMutableArray alloc] initWithCapacity:4];
        
        [self loadFromStorage];
    }
    
    return self;
}

-(void)dealloc
{
    [self.downloadRequests release];    
    [super dealloc];     
}

@synthesize downloadRequests = _downloadRequests;

-(NSUInteger)getDownloadRequestCount
{
    __block NSUInteger count;
    dispatch_sync(queue, ^{        
        count = [[self downloadRequests] count];
    });
    
    return count;
}

-(DownloadRequest*)remove:(NSString*)url
{
    NSString* blockUrl = url;
    __block DownloadRequest* request = nil;
    dispatch_sync(queue, ^{        
        for (DownloadRequest* item in [self downloadRequests]) 
        {
            if ([[item url] compare:blockUrl options:NSCaseInsensitiveSearch] == NSOrderedSame)
            {
                request = item;
                break;
            }
        }
        
        if (request != nil)
        {
            [[self downloadRequests] removeObject:request];
        }        
    });    
    [self persistToStorage];
    return request;
}

-(void)add:(DownloadRequest*)downloadRequest
defaultStorageLocation:(NSString*)storageLocation
defaultPermittedNetworks:(NetworkTypes)permittedNetworks
{
    if ([downloadRequest overrideStorageLocation] == nil || [[downloadRequest overrideStorageLocation] length] == 0)
    {
        [downloadRequest setFinalStorageLocation:storageLocation];
    }
    else 
    {
        [downloadRequest setFinalStorageLocation:[downloadRequest overrideStorageLocation]];
    }
    
    if ([downloadRequest overridePermittedNetworkTypes] == NetworkTypeNone)
    {
        [downloadRequest setFinalPermittedNetworkTypes:permittedNetworks];
    }
    else
    {
        [downloadRequest setFinalPermittedNetworkTypes:[downloadRequest overridePermittedNetworkTypes]];
    }
    
    [downloadRequest setCreationUtc:[NSDate new]];
    
    [downloadRequest retain];
    dispatch_sync(queue, ^{
        TiLog(@"Adding to request Queue: %@", [downloadRequest url]);
        [[self downloadRequests] addObject:downloadRequest];
        TiLog(@"downloadRequests count: %lu", (unsigned long)[[self downloadRequests] count]);
    });
    [self persistToStorage];
    [downloadRequest release];
}

-(void)setRequest:(id)url
toStatus:(DownloadStatus)status
{
    NSString* blockUrl = url;
    dispatch_sync(queue, ^{
        for (DownloadRequest* item in [self downloadRequests])
        {
            if ([item.url caseInsensitiveCompare:blockUrl] == NSOrderedSame)
            {
                item.status = status;
                break;
            }
        }
        
    });
}

-(DownloadRequest*)getDownloadRequest:(NSString*)url
{
    NSString* blockUrl = url;
    __block DownloadRequest* request = nil;
    dispatch_sync(queue, ^{        
//        NSLog(@"getDownloadRequest: %@", blockUrl);
        for (DownloadRequest* item in [self downloadRequests])
        {
            if ([item.url caseInsensitiveCompare:blockUrl] == NSOrderedSame)
            {
//                NSLog(@"getDownloadRequest found: %@", blockUrl);
                request = item;
                break;
            }
        }
        
    });
    
    return request;
}

-(NSArray*)getQueuedDownloadRequestUrls
{
    __block NSMutableArray* urls = [[[NSMutableArray alloc] init] autorelease];
    dispatch_sync(queue, ^{        
        for (DownloadRequest* item in [self downloadRequests]) 
        {
            [urls addObject:[item url]];
        }
    });
    
    return urls;              
}

-(DownloadRequest*)getNextDownloadCandidate:(NetworkTypes)network
{
    __block DownloadRequest* dc = nil;
    dispatch_sync(queue, ^{        
        
        for (DownloadRequest* request in [self downloadRequests]) 
        {
//            NSLog(@"DR = %@ - %d", [request url], [request status]);
            
            if ([request status] == DownloadStatusNone && ([request finalPermittedNetworkTypes] & network) == network)
            {
                if (dc == nil)
                {
                    dc = request;
                }
                else if ([request priority] > [dc priority])
                {
                    dc = request;
                }
                else if ([request priority] == [dc priority] && [[request creationUtc] compare:[dc creationUtc]] == NSOrderedAscending)
                {
                    dc = request;
                }
            }
        }
        
    });
    
    return dc;
}


-(NSArray*)permittedNetworkTypesChanged:(NetworkTypes)permitted
{
    NSMutableArray* urlsUpdated = [[[NSMutableArray alloc] init] autorelease];
    
    for (DownloadRequest* request in [self downloadRequests]) 
    {
        if ([request overridePermittedNetworkTypes] == NetworkTypeNone)
        {
            [request setFinalPermittedNetworkTypes:permitted];
            [urlsUpdated addObject:[request url]];
        }
    }
    
    return urlsUpdated;
}

-(void)persistToStorage
{  
    dispatch_sync(queue, ^{
        //TiLog(@"Persisting queue to file");
        NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                             NSUserDomainMask, YES); 
        NSString* documentsDirectory = [paths objectAtIndex:0];
        NSString* filePath = [documentsDirectory stringByAppendingPathComponent:@"/DownloadQueue.dat"];
        
        NSMutableData *fileData = [NSMutableData data];
        NSKeyedArchiver *coder = [[NSKeyedArchiver alloc] initForWritingWithMutableData:fileData];
        [coder encodeObject:self.downloadRequests forKey:@"downloadInformation"];
        [coder finishEncoding];
        [fileData writeToFile:filePath atomically:YES];
//        NSLog(@"Finished writing queue to file");
        [coder release];
    });
}

-(void)loadFromStorage
{
    TiLog(@"DownloadQueue loadFromStorage");
    dispatch_sync(queue, ^{        
        NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, 
                                                             NSUserDomainMask, YES); 
        NSString* documentsDirectory = [paths objectAtIndex:0];
        NSString* filePath = [documentsDirectory stringByAppendingPathComponent:@"/DownloadQueue.dat"];
        BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:filePath];
        if (fileExists == YES)
        {
            NSData *fileData = [[NSData alloc] initWithContentsOfFile:filePath];
            
            NSKeyedUnarchiver *decoder = [[NSKeyedUnarchiver alloc] initForReadingWithData:fileData];
            NSMutableArray *downloadInformation = [decoder decodeObjectForKey:@"downloadInformation"];
            for (DownloadRequest* request in downloadInformation) {
                [request setStatus:DownloadStatusNone];
            }
            
            [self setDownloadRequests:downloadInformation];
            [decoder release];
            [fileData release];               
        }
    });
}

@end
