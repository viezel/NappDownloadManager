/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#ifndef downloader_DownloadStatus_h
#define downloader_DownloadStatus_h
enum
{
    DownloadStatusNone = 0,
    DownloadStatusInProgress = 1,
    DownloadStatusPaused = 2,
    DownloadStatusComplete = 3
};


typedef NSInteger DownloadStatus;

#endif
