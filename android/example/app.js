

// ENTER URLS to download must be different urls
var URL1 = 'enter url to assest to download here';
var URL2 = 'enter url to assest to download here';
var URL3 = 'enter url to assest to download here';
var URL4 = 'enter url to assest to download here';

// open a single window
var window = Ti.UI.createWindow({
	backgroundColor : 'white'
});
window.open();

var NappDownloadManager = require('dk.napp.downloadmanager');

// settings
NappDownloadManager.permittedNetworkTypes = NappDownloadManager.NETWORK_TYPE_ANY;
NappDownloadManager.maximumSimultaneousDownloads = 4;


NappDownloadManager.deleteItem(URL1);
NappDownloadManager.deleteItem(URL2);
NappDownloadManager.deleteItem(URL3);
NappDownloadManager.deleteItem(URL4);

var scrollView = Ti.UI.createScrollView({
	scrollType : 'vertical',
	layout : 'vertical',
});

var progressOptions = {
	top : 0,
	left : 10,
	min : 0,
	max : 100,
	value : 0,
	width : 280,
};
var labelOptions = {
	width : Ti.UI.SIZE,
	height : Ti.UI.SIZE
};

var progress1 = Ti.UI.createProgressBar(progressOptions);
scrollView.add(progress1);
progress1.show();
var label1 = Ti.UI.createLabel(labelOptions);
scrollView.add(label1);

var progress2 = Ti.UI.createProgressBar(progressOptions);
scrollView.add(progress2);
progress2.show();
var label2 = Ti.UI.createLabel(labelOptions);
scrollView.add(label2);

var progress3 = Ti.UI.createProgressBar(progressOptions);
scrollView.add(progress3);
progress3.show();
var label3 = Ti.UI.createLabel(labelOptions);
scrollView.add(label3);

var progress4 = Ti.UI.createProgressBar(progressOptions);
scrollView.add(progress4);
progress4.show();
var label4 = Ti.UI.createLabel(labelOptions);
scrollView.add(label4);

var pauseButton = Ti.UI.createButton({
	title : 'Pause All',
	left : 10,
	width : 280,
});
pauseButton.addEventListener('click', function() {
	NappDownloadManager.pauseAll();
});

scrollView.add(pauseButton);

var resumeButton = Ti.UI.createButton({
	left : 10,
	width : 280,
	title : 'Resume All',
});
resumeButton.addEventListener('click', function() {
	NappDownloadManager.resumeAll();
});
scrollView.add(resumeButton);

var startButton = Ti.UI.createButton({
	left : 10,
	width : 280,
	title : 'Start Download',
});
startButton.addEventListener('click', function() {
	var file1 = Ti.Filesystem.getFile(Ti.Filesystem.externalStorageDirectory, 'File1.bin');
	if (file1.exists == false) {
		file1.deleteFile();
	}
	var file2 = Ti.Filesystem.getFile(Ti.Filesystem.externalStorageDirectory, 'File2.bin');
	if (file2.exists) {
		file2.deleteFile();
	}
	var file3 = Ti.Filesystem.getFile(Ti.Filesystem.externalStorageDirectory, 'File3.bin');
	if (file3.exists) {
		file3.deleteFile();
	}
	var file4 = Ti.Filesystem.getFile(Ti.Filesystem.externalStorageDirectory, 'File4.bin');
	if (file4.exists) {
		file4.deleteFile();
	}

	NappDownloadManager.addDownload({
		name : 'name 1',
		url : URL1,
		filePath : file1.nativePath,
		priority : NappDownloadManager.DOWNLOAD_PRIORITY_NORMAL
	});
	NappDownloadManager.addDownload({
		name : 'name 2',
		url : URL2,
		filePath : file2.nativePath,
		priority : NappDownloadManager.DOWNLOAD_PRIORITY_LOW
	});
	NappDownloadManager.addDownload({
		name : 'name 3',
		url : URL3,
		filePath : file3.nativePath,
		priority : NappDownloadManager.DOWNLOAD_PRIORITY_HIGH
	});
	NappDownloadManager.addDownload({
		name : 'name 4',
		url : URL4,
		filePath : file4.nativePath,
		priority : NappDownloadManager.DOWNLOAD_PRIORITY_LOW
	});
});
scrollView.add(startButton);

var printQueueButton = Ti.UI.createButton({
	left : 10,
	width : 280,
	title : 'Log the queue',
});
printQueueButton.addEventListener('click', function() {
	// print it to the log
	handleEvent(NappDownloadManager.getAllDownloadInfo());
});
scrollView.add(printQueueButton);

window.add(scrollView);

NappDownloadManager.addEventListener('progress', function(e) {
	var progress = e.downloadedBytes * 100.0 / e.totalBytes;
	var text = e.downloadedBytes + '/' + e.totalBytes + ' ' + Math.round(progress) + '% ' + e.bps + ' bps';

	if (e.url == URL1) {
		label1.text = text;
		progress1.value = progress;
	} else if (e.url == URL2) {
		label2.text = text;
		progress2.value = progress;
	} else if (e.url == URL3) {
		label3.text = text;
		progress3.value = progress;
	} else if (e.url == URL4) {
		label4.text = text;
		progress4.value = progress;
	}

}); 


// Events
NappDownloadManager.addEventListener('paused', handleEvent);
NappDownloadManager.addEventListener('failed', handleEvent);
NappDownloadManager.addEventListener('completed', handleEvent);
NappDownloadManager.addEventListener('cancelled', handleEvent); 
NappDownloadManager.addEventListener('started', handleEvent);

function handleEvent(e){
	Ti.API.info("Event: " + e.type);
	Ti.API.info( typeof e === 'object' ? JSON.stringify(e, null, '\t') : e);
}
