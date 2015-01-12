/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "Downloader.h"

@interface Downloader()
{
//    //    NSInteger identifier;
//    double waitSecondsUponStart;
//    //    NSUInteger storageBytesUsed;
//    //    NSUInteger storageBytesQuota;
//    // NSThread* controller;
//    //    DownloadQueue* downloadQueue;
//    //    NSString* defaultStorageLocation;
//    BOOL timeToStopControllerThread;
//    //    NSUInteger maximumSimultaneousDownloads;
//    //    DownloaderStatus status;
//    //    NetworkTypes permittedNetworkTypes;
//    NSMutableDictionary* downloadRequestUrlsThatMayProceed;
//    CompletedDownloadCatalog* completedDownloadCatalog;
//    //    id <DownloaderDelegate> delegate;
//    SCNetworkReachabilityRef reachabilityRef;
//    int persistQueueIterationCount;
//    int PERSIST_ON_ITERATION_COUNT;
}

@property (readwrite) DownloaderStatus status;
@property (retain) CompletedDownloadCatalog* completedDownloadCatalog;
@property double waitSecondsUponStart;
@property BOOL timeToStopControllerThread;
@property (retain) NSMutableDictionary* downloadRequestUrlsThatMayProceed;
@property SCNetworkReachabilityRef reachabilityRef;
@property int persistQueueIterationCount;
@property int PERSIST_ON_ITERATION_COUNT;

@property (readwrite) NSInteger identifier;
@property (readwrite) NSUInteger storageBytesUsed;
@property (readwrite) DownloadQueue* downloadQueue;


@end

@implementation Downloader

@synthesize delegate = _delegate;
@synthesize identifier = _identifier;
@synthesize status = _status;
@synthesize defaultStorageLocation = _defaultStorageLocation;
@synthesize maximumSimultaneousDownloads = _maximumSimultaneousDownloads;
@synthesize permittedNetworkTypes = _permittedNetworkTypes;
@synthesize storageBytesQuota = _storageBytesQuota;
@synthesize storageBytesUsed = _storageBytesUsed;
@synthesize downloadQueue = _downloadQueue;

@synthesize completedDownloadCatalog = _completedDownloadCatalog;
@synthesize waitSecondsUponStart = _waitSecondsUponStart;
@synthesize timeToStopControllerThread = _timeToStopControllerThread;
@synthesize downloadRequestUrlsThatMayProceed = _downloadRequestUrlsThatMayProceed;
@synthesize reachabilityRef = _reachabilityRef;
@synthesize persistQueueIterationCount = _persistQueueIterationCount;
@synthesize PERSIST_ON_ITERATION_COUNT = _PERSIST_ON_ITERATION_COUNT;

- (id)init
{
    self = [super init];
    
    if (self) {
        TiLog(@"Initializing Downloader 2");
        // Initialization code here.
        _identifier = 0;
        self.waitSecondsUponStart = 0.5f;
        _storageBytesUsed = 0;
        _storageBytesQuota = NSUIntegerMax;
        _downloadQueue = [[DownloadQueue alloc] init];
        _defaultStorageLocation = @"";
        self.timeToStopControllerThread = YES;
        _maximumSimultaneousDownloads = 4;
        _status = DownloaderStatusNone;
        _permittedNetworkTypes = NetworkTypeWireless80211;
        self.downloadRequestUrlsThatMayProceed = [[NSMutableDictionary alloc] init];
        self.completedDownloadCatalog = [[CompletedDownloadCatalog alloc] init];
        self.PERSIST_ON_ITERATION_COUNT = 10;
        self.persistQueueIterationCount = 0;
        
        [self startControllerThread:0.5];
    }
    
    return self;
}

-(void)dealloc
{
    TiLog(@"Cleaning up downloader");
    [self stopControllerThread];
    [self.downloadQueue release];
    [self.downloadRequestUrlsThatMayProceed release];
    [self.completedDownloadCatalog release];
    //[controller release];
    [self.delegate release];
    
    [super dealloc];     
}


-(NSUInteger)storageBytesAvailable
{
    return [self storageBytesQuota] - [self storageBytesUsed];
}

-(void)cancelItems:(NSArray*)urls
{
    for (NSString* url in urls) 
    {
        [self cancelItem:url];
    }
}

-(void)cancelItem:(NSString*)url
{
    [self.downloadRequestUrlsThatMayProceed removeObjectForKey:url];
    
    DownloadInformation* di = [self downloadInformationSingle:url];
    DownloadRequest* request = [[self downloadQueue] remove:url];
    if (request == nil)
    {
        return;
    }
    
    if ([request availableLength] > 0)
    {
        NSString* filePath = [request filePath];
        if ([[NSFileManager defaultManager] isDeletableFileAtPath:filePath] == YES)
        {
            [[NSFileManager defaultManager] removeItemAtPath:filePath error:NULL];
        }
        
        [self.completedDownloadCatalog deleteCompletedDownload:url];
    }
    
    if (di != nil)
    {
        [self.delegate cancelled:di];
    }
}

-(void)deleteItems:(NSArray*)urls
{
    for (NSString* url in urls) 
    {
        [self deleteItem:url];
    }    
}

-(void)deleteItem:(NSString*)url
{
    [self cancelItem:url];
    [self.completedDownloadCatalog deleteCompletedDownload:url];
}

-(void)downloadItems:(NSArray*)items
{
    for (DownloadRequest* dr in items) 
    {
        [self downloadItem:dr];
    }    
    
}

-(void)downloadItem:(DownloadRequest*)request
{               
    [request setLength:0];
    [request setAvailableLength:0];
    [request setStatus:DownloadStatusNone];
    //    
    //    assert([request priority] > 0);
    //    assert([request name] != nil && sizeof([request name]) > 0);
    //    assert([request url] != nil && sizeof([request url]) > 0);
    //    assert([request fileName] != nil && sizeof([request fileName]) > 0);
    
    TiLog(@"Adding download request for %@", request.url);
    [[self downloadQueue] add:request defaultStorageLocation:self.defaultStorageLocation defaultPermittedNetworks:[self permittedNetworkTypes]];
}


-(NSArray*)downloadInformationAll
{
    return [self downloadInformationFromList:[[self downloadQueue] getQueuedDownloadRequestUrls]];
}

-(NSArray*)downloadInformationFromList:(NSArray*)urls
{
    NSMutableArray* list = [[[NSMutableArray alloc] init] autorelease];
    for (NSString* url in urls) 
    {
        [list addObject:[self downloadInformationSingle:url]];
    }    
    
    return list;
}

-(DownloadInformation*)downloadInformationSingle:(NSString*)url
{
    DownloadInformation* di = [self.completedDownloadCatalog getDownloadInformation:url];
    
    if (di == nil)
    {
        DownloadRequest* request = [[self downloadQueue] getDownloadRequest:url];
        if (request != nil)
        {
            di = [[[DownloadInformation alloc] init] autorelease];
            [di setUrl:[request url]];
            [di setName:[request name]];
            [di setLocale:[request locale]];
            [di setFilePath:[request filePath]];
            [di setLength:[request length]];
            [di setMediaBitsPerSecond:[request mediaBitsPerSecond]];
            [di setAvailableLength:[request availableLength]];
            [di setCreationUtc:[request creationUtc]];
            [di setLastWriteUtc:[request lastWriteUtc]];
            [di setLastDownloadBitsPerSecond:[request lastDownloadBitsPerSecond]];
            [di setDownloadPriority:[request priority]];
            [di setIsReadyForPlayback:[request isReadyForPlayback]];
            [di setPermittedNetworkTypes:[request finalPermittedNetworkTypes]];
            [di setStorageLocation:[request finalStorageLocation]];
        }
    }
        
    return di;
}

-(void)stop
{
    [self stopAllDownloading];
    [self stopControllerThread];    
}

-(void)start
{
    if (self.timeToStopControllerThread == YES)
    {
        [self startControllerThread:0.5];
        //[downloadQueue startPersist];
    }
}

-(void)pauseAll
{
    [self pauseItems:[[self downloadQueue] getQueuedDownloadRequestUrls]];
}

-(void)pauseItems:(NSArray*)urls
{
    for (NSString* url in urls) 
    {
        [self pauseItem:url];
    }        
}

-(void)pauseItem:(NSString*)url
{
    [[self downloadQueue] setRequest:url
                            toStatus:DownloadStatusPaused];
    
    [self.downloadRequestUrlsThatMayProceed removeObjectForKey:url];
    
    [self.delegate itemPaused:[self downloadInformationSingle:url]];
}

-(void)restart:(NSString*)url
{
    [self restart:url atOffset:0];
}

-(void)restart:(NSString *)url
      atOffset:(NSUInteger)byteOffset
{
    DownloadRequest* request = [[self downloadQueue] getDownloadRequest:url];
    if (request == nil)
    {
        DownloadInformation* di = [self.completedDownloadCatalog getDownloadInformation:url];
        if (di == nil)
        {
            return;
        }
        
        request = [[[DownloadRequest alloc] init] autorelease];
        [request setUrl:[di url]];
        [request setName:[di name]];
        [request setLocale:[di locale]];
        [request setOverrideStorageLocation:[di storageLocation]];
        [request setPriority:[di downloadPriority]];
        [request setOverridePermittedNetworkTypes:[di permittedNetworkTypes]];
        
        if (byteOffset == 0)
        {
            [self deleteItem:[di url]];
        }
        else
        {
            [request setAvailableLength:byteOffset];
        }
    }
    else
    {
        DownloadRequest* r = [[[DownloadRequest alloc] init] autorelease];
        [r setUrl:[request url]];
        [r setName:[request name]];
        [r setLocale:[request locale]];
        [r setOverrideStorageLocation:[request overrideStorageLocation]];
        [r setPriority:[request priority]];
        [r setOverridePermittedNetworkTypes:[request overridePermittedNetworkTypes]];
        request = r;
    }
    
    [self.downloadQueue add:request defaultStorageLocation:self.defaultStorageLocation defaultPermittedNetworks:self.permittedNetworkTypes];
}

-(void)resumeAll
{
    [self resumeItems:[[self downloadQueue] getQueuedDownloadRequestUrls]];
}

-(void)resumeItems:(NSArray*)urls
{
    for (NSString* url in urls) 
    {
        [self resumeItem:url];
    }            
}

-(void)resumeItem:(NSString*)url
{
    [[self downloadQueue] setRequest:url
                            toStatus:DownloadStatusNone];

}

-(void)stopControllerThread
{
    if (self.timeToStopControllerThread == NO)
    {
        self.status = DownloaderStatusPaused;
        self.timeToStopControllerThread = YES;
        //[downloadQueue stopPersist];
    }
}

-(void)startControllerThread:(double)waitSeconds
{
    TiLog(@"Starting download controller thread");
    if (self.timeToStopControllerThread == YES)
    {
        self.timeToStopControllerThread = NO;
        self.waitSecondsUponStart = waitSeconds;
        // controller = [[NSThread alloc] initWithTarget:self selector:@selector(controllerThread) object:nil];
        [self performSelectorInBackground:@selector(controllerThread) withObject:nil];
    }
}

-(void)stopAllDownloading
{
    [self.downloadRequestUrlsThatMayProceed removeAllObjects];
}

-(void)controllerThread
{
    TiLog(@"Download controller thread started");
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    self.status = DownloaderStatusStarted;
    
    [NSThread sleepForTimeInterval:self.waitSecondsUponStart];
    
    while (self.timeToStopControllerThread == NO)
    {
        if ([[NSThread currentThread] isCancelled]) {
            [pool release];
            [NSThread exit];
        }
        
        ++self.persistQueueIterationCount;
        if (self.persistQueueIterationCount == self.PERSIST_ON_ITERATION_COUNT) 
        {
            self.persistQueueIterationCount = 0;
            [[self downloadQueue] persistToStorage];
        }
        
        if ([[self downloadQueue] getDownloadRequestCount] > 0 &&
            [self.downloadRequestUrlsThatMayProceed count] < self.maximumSimultaneousDownloads)
        {
            NSMutableArray* removeUrls = [[NSMutableArray alloc] init];
            NetworkTypes currentnetwork = [self networkTypes];
            
            for (NSString* key in self.downloadRequestUrlsThatMayProceed)
            {
                NSNumber* network = [self.downloadRequestUrlsThatMayProceed objectForKey:key];
                
                if (([network intValue] & currentnetwork) == 0)
                {
                    [removeUrls addObject:key];
                }
            }
            
            for (NSString* url in removeUrls)
            {
//                NSLog(@"[INFO] Set status to none for %@", url);
                [self.downloadRequestUrlsThatMayProceed removeObjectForKey:url];
                [[self downloadQueue] setRequest:url
                                        toStatus:DownloadStatusNone];
            }
            [removeUrls release];
            
            DownloadRequest* request = [[self downloadQueue] getNextDownloadCandidate:currentnetwork];
//            NSLog(@"[INFO] Download Request: %@", [request url]);
            if (request != nil && [self.downloadRequestUrlsThatMayProceed objectForKey:[request url]] == nil)
            {                
                TiLog(@"Start Download: %@", [request url]);
                [[self downloadQueue] setRequest:[request url]
                                        toStatus:DownloadStatusInProgress];
                NSNumber* permittedNetworks = [NSNumber numberWithInt:[request finalPermittedNetworkTypes]];
                [self.downloadRequestUrlsThatMayProceed setObject:permittedNetworks forKey:[request url]];                
                SingleDownload* download = [[SingleDownload alloc] init];
                [download setDownloadRequest:request];
                [download setPermittedNetworks:permittedNetworks];
                [download setDelegate:self];
                [download start];
                
            }
        }
        
        [NSThread sleepForTimeInterval:0.5f];
    }
    
    [pool release];
    TiLog(@"Download controller thread exiting");
}

-(NetworkTypes)networkTypes
{
    Boolean success;
    const char *host_name = "google.com"; //pretty reliable :)
    SCNetworkReachabilityRef reachability = SCNetworkReachabilityCreateWithName(NULL, host_name);
    SCNetworkReachabilityFlags flags;
    success = SCNetworkReachabilityGetFlags(reachability, &flags);
    
    BOOL isReachable = ((flags & kSCNetworkFlagsReachable) != 0);
    BOOL needsConnection = ((flags & kSCNetworkFlagsConnectionRequired) != 0);
    
    if(isReachable && !needsConnection) // connection is available 
    {        
        // determine what type of connection is available
        BOOL isCellularConnection = ((flags & kSCNetworkReachabilityFlagsIsWWAN) != 0);
        
        if(isCellularConnection) 
            return NetworkTypeMobile; // cellular connection available
        
        if(success)
            return NetworkTypeWireless80211; // wifi connection available
    }
    
    return NetworkTypeNone; // no connection at all
}


- (void) downloadCompleted: (DownloadInformation *) information
{
    [[self downloadQueue] setRequest:[information url]
                            toStatus:DownloadStatusComplete];
    
    [[self downloadQueue] remove:[information url]];
    [self.downloadRequestUrlsThatMayProceed removeObjectForKey:[information url]];
    
    if ([information availableLength] >= [information length])
    {
        [self.completedDownloadCatalog addCompletedDownload:information];
    }
    
    [self.delegate completed:information];
}

- (void) downloadProgress: (DownloadInformation *) information
{
    [self.delegate progress:information];
}

- (BOOL) canContinue: (DownloadInformation *) information
{
    return [self.downloadRequestUrlsThatMayProceed objectForKey:[information url]] != nil;
}

- (void) downloadFailed: (DownloadInformation *) information
{
    [[self downloadQueue] setRequest:information.url
                            toStatus:DownloadStatusNone];
    [self.delegate failed:information];
}

-(void) downloadRestart:(DownloadInformation *)information 
{
    [[self downloadQueue] setRequest:information.url
                            toStatus:DownloadStatusNone];
    DownloadRequest* request = [[self downloadQueue] getDownloadRequest:[information url]];
    [request setAvailableLength:0];
    
    [self.downloadRequestUrlsThatMayProceed removeObjectForKey:[information url]];
}

-(void) downloadStarted:(DownloadInformation *)information
{
    [self.delegate started:information];
}
@end









