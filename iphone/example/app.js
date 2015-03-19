

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

var scrollView = Ti.UI.createScrollView({
	top: "40dp",
	scrollType : 'vertical',
	layout : 'vertical',
});

var progressOptions = {
	min : 0,
	max : 100,
	value : 0,
	top : "6dp",
	width : "300dp",
};
var labelOptions = {
	width : Ti.UI.SIZE,
	height : Ti.UI.SIZE,
	font:{
		fontSize: "11dp"
	},
	height: Ti.UI.SIZE
};

scrollView.add(Ti.UI.createLabel({
	top: 0,
	text: "Downloads",
	font:{
		fontSize: "14dp"
	},
	height: Ti.UI.SIZE
}));

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


scrollView.add(Ti.UI.createLabel({
	top: "10dp",
	text: "Overall progress",
	font:{
		fontSize: "14dp"
	},
	height: Ti.UI.SIZE
}));

var progress5 = Ti.UI.createProgressBar(progressOptions);
scrollView.add(progress5);
progress5.show();
var label5 = Ti.UI.createLabel(labelOptions);
scrollView.add(label5);


///////////////
// BUTTONS
///////////////

var startButton = Ti.UI.createButton({
	lwidth : Ti.UI.SIZE,
	title : 'Start Download',
});

startButton.addEventListener('click', function() {
	if(!Ti.Network.online){
		Ti.API.error("WE ARE OFFLINE");
		return;
	}
	
	var file1 = Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'File1.bin');
	if (file1.exists()) {
		file1.deleteFile();
	}
	var file2 = Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'File2.bin');
	if (file2.exists()) {
		file2.deleteFile();
	}
	var file3 = Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'File3.bin');
	if (file3.exists()) {
		file3.deleteFile();
	}
	var file4 = Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'File4.bin');
	if (file4.exists()) {
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


var pauseButton = Ti.UI.createButton({
	title : 'Pause All Downloads',
	width : Ti.UI.SIZE
});
pauseButton.addEventListener('click', function() {
	NappDownloadManager.pauseAll();
});

scrollView.add(pauseButton);

var resumeButton = Ti.UI.createButton({
	width : Ti.UI.SIZE,
	title : 'Resume All Downloads'
});
resumeButton.addEventListener('click', function() {
	NappDownloadManager.resumeAll();
});
scrollView.add(resumeButton);

var deleteButton = Ti.UI.createButton({
	width : Ti.UI.SIZE,
	title : 'Delete All Downloads',
});
deleteButton.addEventListener('click', function() {
	NappDownloadManager.deleteItem(URL1);
	NappDownloadManager.deleteItem(URL2);
	NappDownloadManager.deleteItem(URL3);
	NappDownloadManager.deleteItem(URL4);
});
scrollView.add(deleteButton);

window.add(scrollView);

var printQueueButton = Ti.UI.createButton({
	width : Ti.UI.SIZE,
	title : 'Log the queue',
});
printQueueButton.addEventListener('click', function() {
	// print it to the log
	handleEvent(NappDownloadManager.getAllDownloadInfo());
});
scrollView.add(printQueueButton);


var deleteQueueButton = Ti.UI.createButton({
	width : Ti.UI.SIZE,
	title : 'Delete the entire queue',
});
deleteQueueButton.addEventListener('click', function() {
	NappDownloadManager.deleteQueue();
});
scrollView.add(deleteQueueButton);

window.add(scrollView);

NappDownloadManager.addEventListener('progress', function(e) {
	handleEvent(e);
	Ti.API.info( "progress: " + new Date().getTime());
	
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

NappDownloadManager.addEventListener('overallprogress', function(e){
	label5.text = e.procentage + "% " + Math.round(e.bps/1000) + " kbps";
	progress5.value = e.procentage;
});

function handleEvent(e){
	Ti.API.info("Event: " + e.type);
	Ti.API.info( typeof e === 'object' ? JSON.stringify(e, null, '\t') : e);
}

