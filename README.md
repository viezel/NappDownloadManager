# Napp Download Manager

## Notice

**THIS IS WORK IN PROGRESS. HELP OUT IMPROVE IT**

**The Mission: Create a common way for Ti apps to download multiple files in a background thread. The more work the module can do - the better. We do not want to spam the JS thread with too much info. e.g. if you use the event "progress" you keep spamming the JS thread - hence not really getting anything out of threading.**


## Description

This is a download module for Appcelerator Titanium that allows you to add urls and download the files in the background.

## Accessing the module

To access this module from JavaScript, you would do the following:

	var NappDownloadManager = require("dk.napp.downloadmanager");

The downloader variable is a reference to the Module object.	

## Reference

### Network Types Constants

	NappDownloadManager.NETWORK_TYPE_WIFI
	NappDownloadManager.NETWORK_TYPE_MOBILE
	NappDownloadManager.NETWORK_TYPE_ANY

### Priority Constants

	NappDownloadManager.DOWNLOAD_PRIORITY_LOW
	NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL
	NappDownloadManager.DOWNLOAD_PRIORITY_HIGH

### Events

	NappDownloadManager.addEventListener('progress', handleEvent);
	NappDownloadManager.addEventListener('paused', handleEvent);
	NappDownloadManager.addEventListener('failed', handleEvent);
	NappDownloadManager.addEventListener('completed', handleEvent);
	NappDownloadManager.addEventListener('cancelled', handleEvent);	
	NappDownloadManager.addEventListener('started', handleEvent);

	handleEvent for started will have a reason property with either 'start' or 'resume'
	
All events send an object of the download information.  Example of event handler
	
	function handleEvent(e) {
		e.name;
		e.url;
		e.downloadedBytes;
		e.totalBytes;
		e.filePath;
		e.createdDate;
		e.priority;
	}

### maximumSimultaneousDownloads

	NappDownloadManager.maximumSimultaneousDownloads = 4;
	var maxDownloads = NappDownloadManager.getMaximumSimultaneousDownloads();
	NappDownloadManager.setMaximumSimultaneousDownloads(4);
	
### permittedNetworkTypes

	NappDownloadManager.permittedNetworkTypes = NappDownloadManager.NETWORK_TYPE_ANY;
	var maxDownloads = NappDownloadManager.getPermittedNetworkTypes();
	NappDownloadManager.setPermittedNetworkTypes(NappDownloadManager.NETWORK_TYPE_WIFI);

### addDownload

	NappDownloadManager.addDownload({
		name:'Some name',
		url:'http://host/file',
		filePath: 'native file path',
		priority: NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL
	});
	
> **Important**
> 
> The downloader starts immediately downloading the files, after you add them! Be sure to set any event listeners before adding your first file or it might not get called!



	
	
### stopDownloader

	NappDownloadManager.stopDownloader();

### restartDownloader

	NappDownloadManager.restartDownloader();
	
### pauseAll

	NappDownloadManager.pauseAll();

### pauseItem

	NappDownloadManager.pauseItem('http://host/file');

### resumeAll

	NappDownloadManager.resumeAll();

### resumeItem

	NappDownloadManager.resumeItem('http://host/file');

### cancelItem

	NappDownloadManager.cancelItem('http://host/file');

### deleteItem

	NappDownloadManager.deleteItem('http://host/file');

### getDownloadInfo

	NappDownloadManager.getDownloadInfo('http://host/file');
	
### getAllDownloadInfo

	NappDownloadManager.getAllDownloadInfo();


## Usage

	var NappDownloadManager = require('dk.napp.downloadmanager');
	NappDownloadManager.permittedNetworkTypes = NappDownloadManager.NETWORK_TYPE_ANY;
	NappDownloadManager.maximumSimultaneousDownloads = 4;
	NappDownloadManager.addEventListener('progress', function(e) {
	    var progress = e.downloadedBytes * 100.0 / e.totalBytes;
	    var text = e.downloadedBytes + '/' + e.totalBytes + ' ' + Math.round(progress)+ '% ' +  e.bps + ' bps';	
	});

	NappDownloadManager.addDownload({name: 'name 1', url:URL1, filePath:file1.nativePath, priority: NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL});
    NappDownloadManager.addDownload({name: 'name 2', url:URL2, filePath:file2.nativePath, priority: NappDownloadManager.DOWNLOAD_PRIORITY_LOW});
    NappDownloadManager.addDownload({name: 'name 3', url:URL3, filePath:file3.nativePath, priority: NappDownloadManager.DOWNLOAD_PRIORITY_HIGH});
    NappDownloadManager.addDownload({name: 'name 4', url:URL4, filePath:file4.nativePath, priority: NappDownloadManager.DOWNLOAD_PRIORITY_LOW});


## Changelog


* v1.1.2 (iOS)
  * Bugfix. Better handling of invalid for forbidden download links. 

* v1.1.1 (iOS)
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
    
    Copyright (c) 2010-2014 Mads Møller

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
