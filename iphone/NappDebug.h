/**
 * Module developed by Napp ApS
 * www.napp.dk
 * Mads Møller
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import <Foundation/Foundation.h>

#if defined(DEBUG) || (TARGET_IPHONE_SIMULATOR)
#define TiLog(...) { NSLog(__VA_ARGS__); }
#else
#define TiLog(...) {}
#endif
