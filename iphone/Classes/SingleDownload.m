/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "SingleDownload.h"

@interface SingleDownload()
{
    long bytesForBPS;
    int trackBpsIterationCount;
    int TRACK_BPS_ON_ITERATION_NUMBER;
    NSDate* bpsTrackingStart;
    NSDate* lastFiredProgressEvent;
    DownloadInformation* downloadInformation;
    NSURLConnection* urlConnection;
    NSThread* downloadThread;
}

@end


@implementation SingleDownload

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        bytesForBPS = 0;
        trackBpsIterationCount = 0;
        TRACK_BPS_ON_ITERATION_NUMBER = 30;
    }
    
    return self;
}

-(void)dealloc
{
    [self.downloadRequest release];
    [self.permittedNetworks release];
    [self.delegate release];
    [bpsTrackingStart release];
    [lastFiredProgressEvent release];
    [urlConnection release];
    [downloadInformation release];
    [downloadThread release];
    
    [super dealloc];     
}

@synthesize delegate = _delegate;
@synthesize downloadRequest = _downloadRequest;
@synthesize permittedNetworks = _permittedNetworks;


-(void)start
{                    
    downloadThread = [[NSThread alloc] initWithTarget:self selector:@selector(downloadThread) object:nil];
    [downloadThread setThreadPriority:[self.downloadRequest priority]];
    NSString* threadName = @"DownloadThread: ";
    [downloadThread setName:[threadName stringByAppendingString:[self.downloadRequest name]]];
    
    [self.downloadRequest setStatus:DownloadStatusInProgress];
    [downloadThread start];
    lastFiredProgressEvent = [NSDate new];
}



-(void)downloadThread
{           
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    
    downloadInformation = [[DownloadInformation alloc] init];
    [downloadInformation setUrl:[self.downloadRequest url]];
    [downloadInformation setName:[self.downloadRequest name]];
    [downloadInformation setLocale:[self.downloadRequest locale]];
    [downloadInformation setFilePath:[self.downloadRequest filePath]];
    [downloadInformation setLength:[self.downloadRequest length]];
    [downloadInformation setMediaBitsPerSecond:[self.downloadRequest mediaBitsPerSecond]];
    [downloadInformation setAvailableLength:[self.downloadRequest availableLength]];
    [downloadInformation setCreationUtc:[self.downloadRequest creationUtc]];
    [downloadInformation setLastWriteUtc:[self.downloadRequest lastWriteUtc]];
    [downloadInformation setLastDownloadBitsPerSecond:[self.downloadRequest lastDownloadBitsPerSecond]];
    [downloadInformation setDownloadPriority:[self.downloadRequest priority]];
    [downloadInformation setIsReadyForPlayback:[self.downloadRequest isReadyForPlayback]];
    [downloadInformation setPermittedNetworkTypes:[self.downloadRequest finalPermittedNetworkTypes]];
    [downloadInformation setStorageLocation:[self.downloadRequest finalStorageLocation]];
    [downloadInformation setHeaders:[self.downloadRequest headers]];
    
    
    downloadInformation.message = @"resume";
    NSFileManager* fileMan = [[[NSFileManager alloc] init] autorelease];    
    //NSLog(@"Checking if file exists. %@", [downloadRequest filePath]);            
    
    // Checking if file exists
    if ([fileMan fileExistsAtPath:[self.downloadRequest filePath]] == false) {
        downloadInformation.message = @"start";
        [fileMan createDirectoryAtPath:[[self.downloadRequest filePath] stringByDeletingLastPathComponent] withIntermediateDirectories:YES attributes:nil error:nil];        
        
        // create new file
        NSError* error;
        BOOL result = [fileMan createFileAtPath:[self.downloadRequest filePath] contents:nil attributes:nil];
        if (result == YES) {
            TiLog(@"File successfully created.");
            
            // set no backup flag for iCloud
            NSURL *downloadURL = [NSURL fileURLWithPath:[self.downloadRequest filePath]];
            [downloadURL setResourceValue:[NSNumber numberWithBool:YES] forKey:NSURLIsExcludedFromBackupKey error:NULL];
        } else {
            NSLog(@"Failed to create file. %@", [error debugDescription]);                                    
        }
    }
    
    UInt32 size = [[fileMan attributesOfItemAtPath:[self.downloadRequest filePath] error:nil] fileSize];
    NSLog(@"File Size %u of %lu", (unsigned int)size, (unsigned long)[self.downloadRequest length]);            
    if (size != [self.downloadRequest availableLength]) {
        [self.downloadRequest setAvailableLength:size];
    }
    
    [self startRequest];
    [self.delegate downloadStarted:downloadInformation];
    
    CFRunLoopRun();
    [urlConnection release];
    urlConnection = nil;
    [downloadInformation release];
    downloadInformation = nil;
    [pool drain];
    
}

-(void)startRequest
{
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:[self.downloadRequest url]]];
    [request setHTTPMethod:@"GET"];
    
    NSDictionary* headers = self.downloadRequest.headers;
    if(headers !=  nil && !headers.count == 0 ) {
        for (NSString* key in headers) {
            [request setValue:headers[key] forHTTPHeaderField:key];
        }
    }
    
    if ([self.downloadRequest availableLength] > 0)
    {
        NSString* range = @"bytes=";
        range = [range stringByAppendingString:[[NSNumber numberWithInt:[self.downloadRequest availableLength]] stringValue]];
        range = [range stringByAppendingString:@"-"];
        //NSString* range = [NSString stringWithFormat:@"bytes=%i-", [downloadRequest availableLength]];
        
        TiLog(@"Setting Range Request %@", range);
        [request setValue:range forHTTPHeaderField:@"Range"];
    }
    
    TiLog(@"Download Thread sending request.");
    urlConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self startImmediately:YES];    
}

#pragma mark NSURLConnection delegate methods
- (NSURLRequest *)connection:(NSURLConnection *)connection
 			 willSendRequest:(NSURLRequest *)request
 			redirectResponse:(NSURLResponse *)redirectResponse {
 	NSLog(@"Connection received data");
    return request;
}

-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    if (self.downloadRequest.status == DownloadStatusNone)
    {
        [self.downloadRequest setStatus:DownloadStatusInProgress];
    }
    
    //NSLog(@"Receive Data: %d", [data length]);
    NSUInteger byteCount = [data length];
    bytesForBPS += byteCount;
    NSFileHandle* fileHandle = [NSFileHandle fileHandleForWritingAtPath:[self.downloadRequest filePath]];
    [fileHandle seekToEndOfFile];
    [fileHandle writeData:data];
    [fileHandle closeFile];
    
    [self.downloadRequest setAvailableLength:[self.downloadRequest availableLength] + byteCount];
    [self.downloadRequest setLastWriteUtc:[NSDate new]];
    [downloadInformation setLastWriteUtc:[self.downloadRequest lastWriteUtc]];
    [downloadInformation setAvailableLength:[self.downloadRequest availableLength]];
    
    // Track bits/second (every n iterations through loop)
    trackBpsIterationCount++;
    if (trackBpsIterationCount == TRACK_BPS_ON_ITERATION_NUMBER)
    {    
        double bpsTrackingSpan = [bpsTrackingStart timeIntervalSinceNow] * -1;
        if (bpsTrackingSpan > 0.0f)
        {
            double bps = (8.0f * bytesForBPS) / bpsTrackingSpan;                
            [self.downloadRequest setLastDownloadBitsPerSecond:bps];
            [downloadInformation setLastDownloadBitsPerSecond:[self.downloadRequest lastDownloadBitsPerSecond]];
        }
    }        
    

    // Fire progress event (twice every second)
    // We do this to limit the need of stressing the UI thread with too many calls
    if(fabs([lastFiredProgressEvent timeIntervalSinceNow]) > 0.5f )
    {
        lastFiredProgressEvent = [NSDate new];
        [self.delegate downloadProgress:downloadInformation];
    }
    
    
    // Reset tracking of bits/second (every n iterations through loop)
    if (trackBpsIterationCount == TRACK_BPS_ON_ITERATION_NUMBER)
    {            
        trackBpsIterationCount = 0;
        bytesForBPS = 0;
        bpsTrackingStart = [NSDate new];
    }        
    
    
    if ([self.delegate canContinue:downloadInformation] == false)
    {
        NSLog(@"Download cancelled %@ - %ld", [self.downloadRequest url], (long)self.downloadRequest.status);
        if (self.downloadRequest.status == DownloadStatusInProgress)
        {
            [self.downloadRequest setStatus:DownloadStatusNone];
        }
        [urlConnection cancel];
        CFRunLoopStop(CFRunLoopGetCurrent());
    }
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection{
    
    if ([self.downloadRequest availableLength] == [self.downloadRequest length]) {
        // send an extra progress event to keep it at 100%
        [self.delegate downloadProgress:downloadInformation];
        
        [self.downloadRequest setStatus:DownloadStatusComplete];
        [self.delegate downloadCompleted:downloadInformation];
        TiLog(@"Download finished loading %lu / %lu", (unsigned long)[self.downloadRequest availableLength], (unsigned long)[self.downloadRequest length]);
        CFRunLoopStop(CFRunLoopGetCurrent());
    }
    else 
    {
        [urlConnection cancel];
        NSLog(@"Download invalid %lu / %lu", (unsigned long)[self.downloadRequest availableLength], (unsigned long)[self.downloadRequest length]);
        NSLog(@"Download invalid filePath %@", [self.downloadRequest filePath]);
        NSLog(@"Download invalid url %@", [self.downloadRequest url]);
        [[NSFileManager defaultManager] removeItemAtPath:[self.downloadRequest filePath] error:nil];
        
        // validate the url
        if([self validateUrl:[self.downloadRequest url]]){
            // try downloading again
            [self.downloadRequest setAvailableLength:0];
            [self startRequest];
        } else {
            downloadInformation.message = @"invalid download url";
            [self.delegate downloadInvalid:downloadInformation];
            
            CFRunLoopStop(CFRunLoopGetCurrent());
        }
    }
}

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    bpsTrackingStart = [NSDate new];
    TiLog(@"Download received response. Writing to %@", [self.downloadRequest filePath]);
    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse*)response;
    NSInteger code = [httpResponse statusCode];
    
    // Error Check: We do not want to continue if response code is above 400 (error occured)
    if(code >= 400){
        //  Stop the download from proceeding
        NSLog(@"Error: Download is invalid. Response code: %ld", (long)code);
        [urlConnection cancel];
        [self.downloadRequest setAvailableLength:0];
        downloadInformation.message = @"Response error";
        [self.delegate downloadFailed:downloadInformation];
        CFRunLoopStop(CFRunLoopGetCurrent());
        return;
    }
    
    if ([httpResponse respondsToSelector:@selector(allHeaderFields)])
    {
        NSDictionary *dictionary = [httpResponse allHeaderFields];
        NSString *acceptRanges = [dictionary valueForKey:@"Accept-Ranges"];
        if (acceptRanges == nil || [acceptRanges  isEqual: @"none"])
        {
            TiLog(@"Server doesn't allow accept ranges so download the whole file. %@", [self.downloadRequest filePath]);
            [self.downloadRequest setAvailableLength:0];
            [downloadInformation setAvailableLength:0];
            
            NSFileHandle* fileHandle = [NSFileHandle fileHandleForWritingAtPath:[self.downloadRequest filePath]];
            [fileHandle truncateFileAtOffset:0];
            [fileHandle closeFile];
        }
        else
        {
            TiLog(@"Server allows accept ranges so append to the file.");
        }
        
        NSString *contentLength = [dictionary valueForKey:@"content-length"];        
        TiLog(@"Content length %@", contentLength);
        
        if ([self.downloadRequest length] == 0)
        {
            [self.downloadRequest setLength:contentLength.integerValue];
            [downloadInformation setLength:contentLength.integerValue];
            [self.delegate downloadProgress:downloadInformation];
        }
    }
}


-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{    
    NSLog(@"Fetch failed: %@", [error localizedDescription]);
    CFRunLoopStop(CFRunLoopGetCurrent());
    
    // send failed event
    downloadInformation.message = [error localizedDescription];
    [self.delegate downloadFailed:downloadInformation];
}

- (BOOL) validateUrl: (NSString *) candidate {
    NSString *urlRegEx = @"(http|https)://((\\w)*|([0-9]*)|([-|_])*)+([\\.|/]((\\w)*|([0-9]*)|([-|_])*))+";
    NSPredicate *urlTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", urlRegEx];
    return [urlTest evaluateWithObject:candidate];
}

- (BOOL)addSkipBackupAttributeToItemAtPath:(NSString *) filePathString
{
    NSURL* URL= [NSURL fileURLWithPath: filePathString];
    assert([[NSFileManager defaultManager] fileExistsAtPath: [URL path]]);
    
    NSError *error = nil;
    BOOL success = [URL setResourceValue: [NSNumber numberWithBool: YES]
                                  forKey: NSURLIsExcludedFromBackupKey error: &error];
    if(!success){
        NSLog(@"Error excluding %@ from backup %@", [URL lastPathComponent], error);
    }
    return success;
}

@end
