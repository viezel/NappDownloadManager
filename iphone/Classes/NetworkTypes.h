/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#ifndef downloader_NetworkTypes_h
#define downloader_NetworkTypes_h

enum
{
	NetworkTypeNone = 0x00,
	NetworkTypeEthernet = 0x01,
	NetworkTypeMobileBroadbank2G = 0x02,
	NetworkTypeMobileBroadbank3G = 0x04,
	NetworkTypeMobileBroadbank4G = 0x08,
	NetworkTypeWireless80211 = 0x10,
    NetworkTypeMobile = 0x1e,
    NetworkTypeNetworkTypeAny = 0xff,
    
};


typedef NSInteger NetworkTypes;

#endif
