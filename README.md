# Napp Download Manager

[![gitTio](http://gitt.io/badge.png)](http://gitt.io/component/dk.napp.downloadmanager)
[![License](http://img.shields.io/badge/license-MIT-orange.svg)](http://mit-license.org)

## Description

This is a download module for Appcelerator Titanium that allows you to add urls and download the files in the background.

![NappDownloadManager](https://raw.githubusercontent.com/viezel/NappDownloadManager/master/documentation/napp-download-manager.gif)

## Quick Start 

### Get it 
Download the latest distribution ZIP-file and consult the [Titanium Documentation](http://docs.appcelerator.com/titanium/latest/#!/guide/Using_a_Module) on how install it, or simply use the [gitTio CLI](http://gitt.io/cli):

`$ gittio install dk.napp.downloadmanager`

## Accessing the module

To access this module from JavaScript, you would do the following:

```javascript
	var NappDownloadManager = require("dk.napp.downloadmanager");
```

The downloader variable is a reference to the Module object.	

## Reference

### Network Types Constants

```javascript
	NappDownloadManager.NETWORK_TYPE_WIFI
	NappDownloadManager.NETWORK_TYPE_MOBILE
	NappDownloadManager.NETWORK_TYPE_ANY
```

### Priority Constants

```javascript
	NappDownloadManager.DOWNLOAD_PRIORITY_LOW
	NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL
	NappDownloadManager.DOWNLOAD_PRIORITY_HIGH
```

### Events

```javascript
	NappDownloadManager.addEventListener('progress', handleEvent);
	NappDownloadManager.addEventListener('overallprogress', handleEvent);
	NappDownloadManager.addEventListener('paused', handleEvent);
	NappDownloadManager.addEventListener('failed', handleEvent);
	NappDownloadManager.addEventListener('completed', handleEvent);
	NappDownloadManager.addEventListener('cancelled', handleEvent);	
	NappDownloadManager.addEventListener('started', handleEvent);

```
	
All events send an object of the download information. Example of event handler

```javascript	
	function handleEvent(e) {
		e.name;
		e.url;
		e.downloadedBytes;
		e.totalBytes;
		e.filePath;
		e.createdDate;
		e.priority;
	}
```

### maximumSimultaneousDownloads

```javascript
	NappDownloadManager.maximumSimultaneousDownloads = 4;
	var maxDownloads = NappDownloadManager.getMaximumSimultaneousDownloads();
	NappDownloadManager.setMaximumSimultaneousDownloads(4);
```
	
### permittedNetworkTypes
```javascript
	NappDownloadManager.permittedNetworkTypes = NappDownloadManager.NETWORK_TYPE_ANY;
	var maxDownloads = NappDownloadManager.getPermittedNetworkTypes();
	NappDownloadManager.setPermittedNetworkTypes(NappDownloadManager.NETWORK_TYPE_WIFI);
```

### addDownload

```javascript
NappDownloadManager.addDownload({
	name:'Some name',
	url:'http://host/file',
	filePath: 'native file path',
	priority: NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL
});
```
	
> **Important**
> 
> The downloader starts immediately downloading the files, after you add them! Be sure to set any event listeners before adding your first file or it might not get called!



	
	
### stopDownloader

```javascript
	NappDownloadManager.stopDownloader();
```

### restartDownloader

```javascript
	NappDownloadManager.restartDownloader();
```
	
### pauseAll

```javascript
	NappDownloadManager.pauseAll();
```

### pauseItem

```javascript
	NappDownloadManager.pauseItem('http://host/file');
```

### resumeAll

```javascript
	NappDownloadManager.resumeAll();
```

### resumeItem

```javascript
	NappDownloadManager.resumeItem('http://host/file');
```

### cancelItem

```javascript
	NappDownloadManager.cancelItem('http://host/file');
```

### deleteItem

```javascript
	NappDownloadManager.deleteItem('http://host/file');
```

### getDownloadInfo

```javascript
	NappDownloadManager.getDownloadInfo('http://host/file');
```
	
### getAllDownloadInfo

```javascript
	NappDownloadManager.getAllDownloadInfo();
```


## Usage

NappDownloadManager is a singleton - so only require it once globally. A good place to do so would be in alloy.js or app.js for vanilla Titanium projects. 

```javascript
// if Alloy
Alloy.Globals.NappDownloadManager = require('dk.napp.downloadmanager');
...
```

```javascript
// if vanilla Titanium
var NappDownloadManager = require('dk.napp.downloadmanager');

NappDownloadManager.permittedNetworkTypes = NappDownloadManager.NETWORK_TYPE_ANY;
NappDownloadManager.maximumSimultaneousDownloads = 4;

// add events before starting the downloads
NappDownloadManager.addEventListener('progress', function(e) {
	// show it somewhere
	var progress=e.downloadedBytes*100.0/e.totalBytes;
	var text=e.downloadedBytes+'/'+e.totalBytes+' '+Math.round(progress)+'% '+e.bps+' bps';
});

NappDownloadManager.addEventListener('completed', function(e) {
	// do stuff
});

// define a path where the downloaded file should be stored once complete
var file = Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'myfile.ext');

// add a download
NappDownloadManager.addDownload({
	name: 'myfile ..',
	url: "https://example.com/myfile.ext",
	filePath: file.nativePath,
	priority: NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL
});

// Optionally add headers to download request
NappDownloadManager.addDownload({
	name: 'myfile ..',
	url: "https://example.com/myfile.ext",
	filePath: file.nativePath,
	priority: NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL,
	headers: {"Authorization": "Bearer " + Ti.App.Properties.getString("authToken"), 
        "CustomHeader": "CustomHeaderValue"}
});
```

## Changelog
* v1.1.7
  * Contains fixes if server is using Transfer-Encoding: chunked or if server doesn't send content-length header with response.
  * Added abiility pass headers to download request (for Authorization etc).

* v1.1.5 (iOS only)  
  * Making sure we dont violate the Apple rule: 2.23 - Apps must follow the iOS Data Storage Guidelines or they will be rejected. We set the `NSURLIsExcludedFromBackupKey` flag. 

* v1.1.4
  * Bugfix for invalid urls

* v1.1.3 
  * Added `overallprogress` event. 
  * Added failed event on connection timeouts and error.

* v1.1.2 
  * Bugfix. Better handling of invalid for forbidden download links. 

* v1.1.1
  * Improvement. Better handling of Progress Event. Now it fires twice every second, instead of all the time. This limits the UI thread, if you update UI to show download progress.
  
* v1.1.0 (iOS)
  * Added 64bit support
  
* v1.0.0
  * init


## Author

**Mads Møller**  
web: http://www.napp.dk  
email: mm@napp.dk  
twitter: @nappdev  

Original work by Kevin Willford

## License

    The MIT License (MIT)
    
    Copyright (c) 2010-2015 Mads Møller

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
