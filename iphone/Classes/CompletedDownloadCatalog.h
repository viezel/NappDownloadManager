/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import <Foundation/Foundation.h>
#import "DownloadInformation.h"

@interface CompletedDownloadCatalog : NSObject
{
}

-(void)deleteCompletedDownload:(NSString*)url;
-(void)addCompletedDownload:(DownloadInformation*)downloadInformation;
-(DownloadInformation*)getDownloadInformation:(NSString*)url;
-(void)persistToStorage;
-(void)loadFromStorage;
-(NSString*)MD5:(NSString*)value;

@end
