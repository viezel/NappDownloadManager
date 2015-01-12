/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import <CommonCrypto/CommonDigest.h>
#import "CompletedDownloadCatalog.h"

@interface CompletedDownloadCatalog()
{
    NSMutableDictionary* downloadInformationsByUrl;
    dispatch_queue_t queue;    
}

@end


@implementation CompletedDownloadCatalog

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        queue = dispatch_queue_create("dk.napp.completedqueue", 0);
        downloadInformationsByUrl = [[NSMutableDictionary alloc] init];
        
        [self loadFromStorage];
    }
    
    return self;
}

-(void)dealloc
{
    [downloadInformationsByUrl release];
    
    [super dealloc];     
}
-(NSString*)MD5:(NSString*)value;
{
    // Create pointer to the string as UTF8
    const char *ptr = [value UTF8String];
    
    // Create byte array of unsigned chars
    unsigned char md5Buffer[CC_MD5_DIGEST_LENGTH];
    
    // Create 16 byte MD5 hash value, store in buffer
    CC_MD5(ptr, strlen(ptr), md5Buffer);
    
    // Convert MD5 value in the buffer to NSString of hex values
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) 
        [output appendFormat:@"%02x",md5Buffer[i]];
    
    return output;    
}

-(void)deleteCompletedDownload:(NSString*)url
{
    dispatch_sync(queue, ^{        
        DownloadInformation* info = [downloadInformationsByUrl  objectForKey:[self MD5:url]];
        if (info != nil)
        {
            NSString* path = [info filePath];
            [downloadInformationsByUrl removeObjectForKey:[self MD5:url]];
            [[NSFileManager defaultManager] removeItemAtPath:path error:nil];
            [self persistToStorage];
        }
    });
}

-(void)addCompletedDownload:(DownloadInformation*)downloadInformation
{
    dispatch_sync(queue, ^{        
        [downloadInformationsByUrl setValue:downloadInformation forKey:[self MD5:[downloadInformation url]]];
        [self persistToStorage];
    });
}

-(DownloadInformation*)getDownloadInformation:(NSString*)url
{
    __block DownloadInformation* info;
    dispatch_sync(queue, ^{        
        info = [downloadInformationsByUrl objectForKey:[self MD5:url]];
    }); 
    
    return info;
}

-(void)persistToStorage
{
    dispatch_async(queue, ^{        
        NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, 
                                                             NSUserDomainMask, YES); 
        NSString* documentsDirectory = [paths objectAtIndex:0];
        NSString* filePath = [documentsDirectory stringByAppendingPathComponent:@"/DownloadItemCatalog.dat"];
        NSMutableData *fileData = [NSMutableData data];
        NSKeyedArchiver *coder = [[NSKeyedArchiver alloc] initForWritingWithMutableData:fileData];
        [coder encodeObject:downloadInformationsByUrl forKey:@"downloadInformation"];
        [coder finishEncoding];
        [fileData writeToFile:filePath atomically:YES];
        [coder release];        
    });    
    
}

-(void)loadFromStorage
{
    dispatch_async(queue, ^{        
        TiLog(@"CompletedDownloadCatalog loadFromStorage");
        NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, 
                                                             NSUserDomainMask, YES); 
        NSString* documentsDirectory = [paths objectAtIndex:0];
        NSString* filePath = [documentsDirectory stringByAppendingPathComponent:@"/DownloadItemCatalog.dat"];
        BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:filePath];
        if (fileExists == YES)
        {
            NSData *fileData = [[NSData alloc] initWithContentsOfFile:filePath];
            NSKeyedUnarchiver *decoder = [[NSKeyedUnarchiver alloc] initForReadingWithData:fileData];
            NSMutableDictionary *downloadInformation = [decoder decodeObjectForKey:@"downloadInformation"];
            
            downloadInformationsByUrl = downloadInformation;
            [downloadInformationsByUrl retain];
            [decoder release];
            [fileData release];
        }
    });    
    
}

@end
