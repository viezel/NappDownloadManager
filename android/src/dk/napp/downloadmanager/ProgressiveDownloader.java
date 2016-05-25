package dk.napp.downloadmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class ProgressiveDownloader {	
	// Future:
	//		Error handling throughout to prevent any crash or thread crash
	//		Storage quota, used, etc.
	//		Adjust for quota on the fly
	//		Permitted networks
	//		Adjust for network changes (i.e. permitted networks) on the fly
	//		Case insensitivity on URL comparisons/keys?
	//		Throttling
	//		Indicate MediaBitsPerSecond and IsReadyForPlayback (based on BufferSeconds property?)

	// Constants
	private static final String LCAT = "ProgressiveDownloader";
	private static final long OFFSET_ZERO = 0;
	private static final String HTTP_GET = "GET";
	private static final int WAIT_MS_UPON_RESUME = 0;
	private static final int WAIT_MS_UPON_START = 500;
	private static final int DOWNLOAD_BUFFER_SIZE = 2048;
	private static final int TRACK_BPS_ON_ITERATION_NUMBER = 10;
	private static final int MAX_CONCURRENT_HTTP_CONNECTIONS = 500;
	private static final int DEFAULT_MAXIMUM_SIMULTANEOUS_DOWNLOADS = 2;
	private static final int FIRE_PROGRESS_EVENT_ON_ITERATION_NUMBER = 4;
	private static final int WAIT_MS_BETWEEN_QUEUED_REQUEST_CHECKS = 500;
	private static final long DEFAULT_STORAGE_BYTES_QUOTA = Long.MAX_VALUE;
	private static final String HTTP_RESPONSE_HEADER_ACCEPT_RANGES_NONE = "none";
	private static final String DOWNLOAD_THREAD_NAME_PREFACE = "DownloadThread: ";
	private static final String HTTP_RESPONSE_HEADER_ACCEPT_RANGES = "Accept-Ranges";
	private static final EnumSet<NetworkTypes> DEFAULT_PERMITTED_NETWORK_TYPES = EnumSet.of(NetworkTypes.Wireless80211);
	private static final int DOWNLOAD_THREAD_PRIORITY_LOW = Thread.MIN_PRIORITY;
	private static final int DOWNLOAD_THREAD_PRIORITY_NORMAL = Thread.MIN_PRIORITY + 1;
	private static final int DOWNLOAD_THREAD_PRIORITY_HIGH = Thread.MIN_PRIORITY + 2;
	
	
	private static final int CONTROLLER_THREAD_PRIORITY = Thread.MIN_PRIORITY;


	// Fields
	private int _id;
	private int _waitMsUponStart;
	private long _storageBytesUsed;
	private long _storageBytesQuota;
	private Thread _controllerThread;
	private DownloadQueue _downloadQueue;
	private String _defaultStorageLocation;
	private boolean _timeToStopControllerThread;
	private int _maximumSimultaneousDownloads;
	private ProgressiveDownloaderStatus _status;
	private EnumSet<NetworkTypes> _permittedNetworkTypes;
	private Object _downloadBatchInformationLock;
	private Hashtable<String, EnumSet<NetworkTypes>> _downloadRequestUrlsThatMayProceed;
	private CompletedDownloadCatalog _completedDownloadCatalog;
	private Hashtable<UUID, DownloadBatchInformation> _downloadBatchInformationById;
	private ContextWrapper _context;
	private ConnectivityManager _connectivityManager;

	/// <summary>
	/// Constructor
	/// </summary>
	public ProgressiveDownloader(ContextWrapper context)
	{
		// Initialize
		_context = context;
		_connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		_id = 0;
		_storageBytesUsed = 0;
		_downloadQueue = new DownloadQueue(context);
		_permittedNetworkTypes = DEFAULT_PERMITTED_NETWORK_TYPES;
		_status = ProgressiveDownloaderStatus.None;
		_downloadBatchInformationLock = new Object();
		_storageBytesQuota = DEFAULT_STORAGE_BYTES_QUOTA;
		_downloadRequestUrlsThatMayProceed = new Hashtable<String, EnumSet<NetworkTypes>>();
		_completedDownloadCatalog = new CompletedDownloadCatalog(context);
		_maximumSimultaneousDownloads = DEFAULT_MAXIMUM_SIMULTANEOUS_DOWNLOADS;
		_downloadBatchInformationById = new Hashtable<UUID, DownloadBatchInformation>();
		_defaultStorageLocation = "";

		// Increase permissible number of concurrent HTTP connections
		//ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
		//ConnManagerParams.setMaxTotalConnections(params, maxTotalConnections);
		//ServicePointManager.DefaultConnectionLimit = MAX_CONCURRENT_HTTP_CONNECTIONS;

		// Start controller thread
		startControllerThread(WAIT_MS_UPON_START);
	}


	/// <summary>
	/// Constructor
	/// </summary>
	/// <param name="id"></param>
	public ProgressiveDownloader(ContextWrapper context, int id)
	{
		this(context);
		_id = id;
	}

	/// <summary>
	/// Occurs when an individual download was paused.
	/// </summary>
	public EventListener<DownloadEvent> DownloadPaused = new EventListener<DownloadEvent>();
	
	/// <summary>
	/// Occurs when an individual download failed.
	/// </summary>
	public EventListener<DownloadEvent> DownloadFailed = new EventListener<DownloadEvent>();


	/// <summary>
	/// Provides download progress information for files or a batches of files that are currently being download.
	/// </summary>
	public EventListener<DownloadEvent> DownloadProgress = new EventListener<DownloadEvent>();


	/// <summary>
	/// Occurs when an individual download completed.
	/// </summary>
	public EventListener<DownloadEvent> DownloadCompleted = new EventListener<DownloadEvent>();


	/// <summary>
	/// Occurs when an individual download was cancelled.
	/// </summary>
	public EventListener<DownloadEvent> DownloadCancelled = new EventListener<DownloadEvent>();

	/// <summary>
	/// Occurs when an individual download was cancelled.
	/// </summary>
	public EventListener<DownloadEvent> DownloadStarted = new EventListener<DownloadEvent>();


	/// <summary>
	/// Occurs when a download batch was paused.
	/// </summary>
	public EventListener<DownloadEvent> DownloadBatchPaused = new EventListener<DownloadEvent>();


	/// <summary>
	/// Occurs when a download batch failed.
	/// </summary>
	public EventListener<DownloadEvent> DownloadBatchFailed = new EventListener<DownloadEvent>();


	/// <summary>
	/// Occurs when a download batch was cancelled.
	/// </summary>
	public EventListener<DownloadEvent> DownloadBatchCancelled = new EventListener<DownloadEvent>();


	/// <summary>
	/// Occurs when a download batch completed.
	/// </summary>
	public EventListener<DownloadEvent> DownloadBatchCompleted = new EventListener<DownloadEvent>();


	/// <summary>
	/// Gets the status of this ProgressiveDownloader instance.
	/// </summary>
	public ProgressiveDownloaderStatus getStatus()
	{
		return _status;
	}


	/// <summary>
	/// Gets or sets the default location where downloaded files will be stored on the device.
	/// </summary>
	public String getDefaultStorageLocation()
	{
		return _defaultStorageLocation;
	}
	public void setDefaultStorageLocation(String value)
	{
		_defaultStorageLocation = value;		
	}


	/// <summary>
	/// Gets the identifier that was used to create the instance of this Object, if any.
	/// </summary>
	public int getId()
	{
		return _id;
	}


	/// <summary>
	/// Gets or sets the maximum number of downloads that are permitted to occur simultaneously.
	/// </summary>
	public int getMaximumSimultaneousDownloads()
	{
		return _maximumSimultaneousDownloads;
	}
	public void setMaximumSimultaneousDownloads(int value)
	{
		if (_maximumSimultaneousDownloads < 1 || _maximumSimultaneousDownloads > MAX_CONCURRENT_HTTP_CONNECTIONS)
		{
			throw new java.lang.IllegalArgumentException("Value must be between 1 and " + MAX_CONCURRENT_HTTP_CONNECTIONS);
		}
		
		_maximumSimultaneousDownloads = value;
	}


	/// <summary>
	/// Gets or sets the networks over which downloads are permitted.
	/// </summary>
	public EnumSet<NetworkTypes> getPermittedNetworkTypes()
	{
		return _permittedNetworkTypes;
	}
	public void setPermittedNetworkTypes(EnumSet<NetworkTypes> value)
	{
		_permittedNetworkTypes = value;		
		
		// Need to change all requests that do not have an overridden network to the set value
		ArrayList<String> changedList = _downloadQueue.permittedNetworkTypesChanged(value);
		for (String url : changedList)
		{
			if (_downloadRequestUrlsThatMayProceed.containsKey(url))
			{
				_downloadRequestUrlsThatMayProceed.put(url, value);
			}
		}
	}


	/// <summary>
	/// Gets the number of bytes of free space remaining in available download storage.
	/// </summary>
	public long getStorageBytesAvailable()
	{
		return (_storageBytesQuota - _storageBytesUsed);
	}


	/// <summary>
	/// Gets or sets the maximum number of bytes available for download storage.
	/// </summary>
	public long getStorageBytesQuota()
	{
		return _storageBytesQuota;
	}
	public void setStorageBytesQuota(long value)
	{
		_storageBytesQuota = value;		
	}


	/// <summary>
	/// Gets the number of bytes used of available download storage.
	/// </summary>
	public long getStorageBytesUsed()
	{
		return _storageBytesUsed;
	}


	/// <summary>
	/// Cancels any in-progress or future download of the DownloadRequests by the specified URLs.
	/// </summary>
	/// <param name="urls"></param>
	public void cancel(Iterable<String> urls)
	{
		// Cancel for each URL
		for (String url : urls)
		{
			cancel(url);
		}
	}


	/// <summary>
	/// Cancels any in-progress or future download of the DownloadBatchRequest by the specified identifier.
	/// </summary>
	/// <param name="downloadBatchRequestId"></param>
	public void cancel(UUID downloadBatchRequestId)
	{
		// Locals
		DownloadBatchRequest batchRequest;
		DownloadBatchInformation downloadBatchInformation;

		// Get batch request
		batchRequest = _downloadQueue.getDownloadBatchRequest(downloadBatchRequestId);
		if (batchRequest == null)
		{
			return;
		}

		// Cancel each item in batch
		for (DownloadRequest request : batchRequest.getDownloadRequests())
		{
			cancel(request.getUrl());
		}

		// Remove batch from queue
		_downloadQueue.remove(downloadBatchRequestId);

		// Synchronize
		synchronized (_downloadBatchInformationLock)
		{
			// Remove batch's download information (if any)
			downloadBatchInformation = _downloadBatchInformationById.get(downloadBatchRequestId);
			if (downloadBatchInformation != null)
			{
				_downloadBatchInformationById.remove(downloadBatchRequestId);
			}
		}

		// Fire batch cancelled event
		this.DownloadBatchCancelled.fireEvent(new DownloadEvent(this, null, downloadBatchInformation));
	}


	/// <summary>
	/// Cancels any in-progress or future download of the DownloadRequest by the specified URL.
	/// </summary>
	/// <param name="url"></param>
	public void cancel(String url)
	{
		// Locals
		DownloadRequest request;

		// Cancel any in-progress download
		_downloadRequestUrlsThatMayProceed.remove(url);

		// Remove download from queue
		DownloadInformation di = getDownloadInformation(url);
		request = _downloadQueue.remove(url);
		if (request == null)
		{
			return;
		}

		// Was the request partially completed?
		if (request.getAvailableLength() > 0)
		{
			// Delete any portion of the item from storage
			String filePath = request.getFilePath();
			if (filePath.startsWith("file://"))
			{
				filePath = filePath.substring(7);
			}
			
			File file = new File(filePath);
			if (file.exists() == true)
			{
				file.delete();
			}

			// Delete item from the completed catalog (if it exists)
			_completedDownloadCatalog.deleteCompletedDownload(url);
		}

		// Fire event
		this.DownloadCancelled.fireEvent(new DownloadEvent(this, di, null));
	}


	/// <summary>
	/// Cancels and deletes any in-progress, future, or complete downloads by the specified URLs.
	/// </summary>
	/// <param name="urls"></param>
	public void delete(Iterable<String> urls)
	{
		// Delete for each URL
		for (String url : urls)
		{
			delete(url);
		}
	}


	/// <summary>
	/// Cancels and deletes any in-progress, future, or complete download batch by the specified identifier.
	/// </summary>
	/// <param name="downloadBatchRequestId"></param>
	public void delete(UUID downloadBatchRequestId)
	{
		// Cancel batch
		cancel(downloadBatchRequestId);

		// TODO: Delete any completed
	}


	/// <summary>
	/// Cancels and deletes any in-progress, future, or complete download by the specified URL.
	/// </summary>
	/// <param name="url"></param>
	public void delete(String url)
	{
		// Cancel item
		cancel(url);

		// Delete any matching completed download
		_completedDownloadCatalog.deleteCompletedDownload(url);
	}


	/// <summary>
	/// Queues the specified DownloadRequests for download.
	/// </summary>
	/// <param name="requests"></param>
	public void download(Iterable<DownloadRequest> requests)
	{
		for (DownloadRequest dr : requests)
		{
			download(dr);
		}
	}


	/// <summary>
	/// Queues the specified DownloadBatchRequest for download.
	/// </summary>
	/// <param name="batchRequest"></param>
	public void download(DownloadBatchRequest batchRequest)
	{
		// Reset batch request
		batchRequest.setLength(0);
		batchRequest.setAvailableLength(0);
		batchRequest.setDownloadStatus(DownloadStatus.None);

		// Validate batch request
		if (batchRequest.getDownloadBatchRequestId() == null)
		{
			throw new IllegalArgumentException("batchRequest.DownloadPriority");
		}
		if (batchRequest.getDownloadRequests() == null ||
			batchRequest.getDownloadRequests().size() == 0)
		{
			throw new IllegalArgumentException("batchRequest.DownloadRequests");
		}
		if (batchRequest.getDownloadPriority() == DownloadPriority.None)
		{
			throw new IllegalArgumentException("batchRequest.DownloadPriority");
		}
		if (batchRequest.getLocale() == null || batchRequest.getLocale().length() == 0)
		{
			throw new IllegalArgumentException("batchRequest.Locale");
		}
		if (batchRequest.getName() == null || batchRequest.getName().length() == 0)
		{
			throw new IllegalArgumentException("batchRequest.Name");
		}
		if (batchRequest.getOverridePermittedNetworkTypes() != null &&
			batchRequest.getOverridePermittedNetworkTypes().contains(NetworkTypes.None))
		{
			throw new IllegalArgumentException("batchRequest.OverridePermittedNetworkTypes");
		}

		// Reset and validate each download request
		for (DownloadRequest request : batchRequest.getDownloadRequests())
		{
			resetAndValidateRequest(request);
		}

		// Add batch request to queue
		_downloadQueue.add(batchRequest, _defaultStorageLocation, _permittedNetworkTypes);
	}


	/// <summary>
	/// Queues the specified DownloadRequest for download.
	/// </summary>
	/// <param name="downloadRequest"></param>
	public void download(DownloadRequest request)
	{
		Log.d(LCAT, "Add request to the download queue");
		resetAndValidateRequest(request);

		// Add request to queue
		_downloadQueue.add(request, _defaultStorageLocation, _permittedNetworkTypes);
	}
	
	private void resetAndValidateRequest(DownloadRequest request)
	{
		// Reset download request
		request.setLength(0);
		request.setAvailableLength(0);
		request.setDownloadStatus(DownloadStatus.None);
		
		// Validate download request
		if (request.getDownloadPriority() == DownloadPriority.None)
		{
			throw new IllegalArgumentException("request.DownloadPriority");
		}
		if (request.getLocale() == null || request.getLocale().length() == 0)
		{
			throw new IllegalArgumentException("request.Locale");
		}
		if (request.getName() == null || request.getName().length() == 0)
		{
			throw new IllegalArgumentException("request.Name");
		}
		if (request.getOverridePermittedNetworkTypes() != null &&
			request.getOverridePermittedNetworkTypes().contains(NetworkTypes.None))
		{
			throw new IllegalArgumentException("request.OverridePermittedNetworkTypes");
		}
		if (request.getUrl() == null || request.getUrl().length() == 0)
		{
			throw new IllegalArgumentException("request.Url");
		}
		if (request.getFileName() == null || request.getFileName().length() == 0)
		{
			throw new IllegalArgumentException("request.FileName");
		}
		if (_downloadQueue.getDownloadRequest(request.getUrl()) != null)
		{
			throw new IllegalArgumentException("A download request already exists with the specified request's URL.");
		}		
	}


	/// <summary>
	/// Gets download progress information for all queued or active downloads.
	/// </summary>
	/// <returns></returns>
	public Iterable<DownloadInformation> getDownloadInformation()
	{
		return getDownloadInformation(_downloadQueue.getQueuedDownloadRequestUrls());
	}


	/// <summary>
	/// Gets download progress information for the DownloadRequests by the specified URLs.
	/// </summary>
	/// <param name="urls"></param>
	/// <returns></returns>
	public Iterable<DownloadInformation> getDownloadInformation(Iterable<String> urls)
	{
		// Locals
		ArrayList<DownloadInformation> downloadInformations;

		// Get download information for each URL
		downloadInformations = new ArrayList<DownloadInformation>();
		for (String url : urls)
		{
			downloadInformations.add(getDownloadInformation(url));
		}

		// Return results
		return downloadInformations;
	}


	/// <summary>
	/// Gets download progress information for the DownloadBatchRequest by the specified identifer.
	/// </summary>
	/// <param name="downloadBatchRequestId"></param>
	/// <returns></returns>
	public DownloadBatchInformation getDownloadInformation(UUID downloadBatchRequestId)
	{
		// Locals
		DownloadBatchRequest batchRequest;

		// Get batch downloadInformation
		batchRequest = _downloadQueue.getDownloadBatchRequest(downloadBatchRequestId);
		if (batchRequest == null)
		{
			return null;
		}

		// Get download information for each item in batch
		return null;
	}


	/// <summary>
	/// Gets download progress information for the DownloadRequest by the specified URL.
	/// </summary>
	/// <param name="url"></param>
	/// <returns></returns>
	public DownloadInformation getDownloadInformation(String url)
	{
		Log.d(LCAT, "getDownloadInformation " + url);
		// Get download information
		DownloadInformation downloadInformation = _completedDownloadCatalog.getDownloadInformation(url);
		if (downloadInformation == null)
		{
			Log.d(LCAT, "NOT in completed catalog check queue");
			// If no download information is available for a completed download,
			// create it from any matching queued download request
			DownloadRequest request = _downloadQueue.getDownloadRequest(url);
			if (request != null)
			{
				Log.d(LCAT, "Found in queue");
				downloadInformation = new DownloadInformation();
				downloadInformation.setUrl(request.getUrl());
				downloadInformation.setName(request.getName());
				downloadInformation.setLocale(request.getLocale());
				downloadInformation.setFilePath(request.getFilePath());
				downloadInformation.setLength(request.getLength());
				downloadInformation.setMediaBitsPerSecond(request.getMediaBitsPerSecond());
				downloadInformation.setAvailableLength(request.getAvailableLength());
				downloadInformation.setCreationUtc(request.getCreationUtc());
				downloadInformation.setLastWriteUtc(request.getLastWriteUtc());
				downloadInformation.setLastDownloadBitsPerSecond(request.getLastDownloadBitsPerSecond());
				downloadInformation.setDownloadPriority(request.getDownloadPriority());
				downloadInformation.setIsReadyForPlayback(request.getIsReadyForPlayback());
				downloadInformation.setPermittedNetworkTypes(request.getFinalPermittedNetworkTypes());
				downloadInformation.setStorageLocation(request.getFinalStorageLocation());
			}
		}

		// Return result
		return downloadInformation;
	}

	public void stop() {
		stopAllDownloading();
		stopControllerThread();
		_downloadQueue.stopPersist();
	}

	/// <summary>
	/// Pauses all download activity of this ProgressiveDownloader instance.
	/// </summary>
	public void pause()
	{
		pause(_downloadQueue.getQueuedDownloadRequestUrls());
	}


	/// <summary>
	/// Pauses any in-progress or future download of the DownloadRequests by the specified URLs.
	/// </summary>
	/// <param name="urls"></param>
	public void pause(Iterable<String> urls)
	{
		// Pause for each URL
		for (String url : urls)
		{
			pause(url);
		}
	}


	/// <summary>
	/// Pauses any in-progress or future download of the DownloadBatchRequest by the specified identifier.
	/// </summary>
	/// <param name="downloadBatchRequestId"></param>
	public void pause(UUID downloadBatchRequestId)
	{
		// Locals
		DownloadBatchRequest batchRequest;
		DownloadBatchInformation downloadBatchInformation;

		// Get batch downloadInformation
		batchRequest = _downloadQueue.getDownloadBatchRequest(downloadBatchRequestId);
		if (batchRequest == null)
		{
			return;
		}

		// Pause each item in batch
		for (DownloadRequest request : batchRequest.getDownloadRequests())
		{
			pause(request.getUrl());
		}

		// Fire batch paused event
		if (this.DownloadBatchPaused != null)
		{
			// Synchronize
			synchronized (_downloadBatchInformationLock)
			{
				downloadBatchInformation = _downloadBatchInformationById.get(downloadBatchRequestId);
			}
			this.DownloadBatchPaused.fireEvent(new DownloadEvent(this, null, downloadBatchInformation));
		}
	}


	/// <summary>
	/// Pauses any in-progress or future download of the DownloadRequest by the specified URL.
	/// </summary>
	/// <param name="url"></param>
	public void pause(String url)
	{
		// Locals
		DownloadRequest request;

		// Cancel any in-progress download
		_downloadRequestUrlsThatMayProceed.remove(url);

		// Set status of download request in queue to paused
		request = _downloadQueue.getDownloadRequest(url);
		if (request == null)
		{
			return;
		}
		request.setDownloadStatus(DownloadStatus.Paused);

		// Fire event
		this.DownloadPaused.fireEvent(new DownloadEvent(this, getDownloadInformation(url), null));
	}


	/// <summary>
	/// Restarts download of the in-progress or completed item with the specified URL.
	/// </summary>
	/// <param name="url"></param>
	public void restart(String url)
	{
		restart(url, OFFSET_ZERO);
	}


	/// <summary>
	/// Restarts download of the in-progress or completed item with the specified URL, beginning at the specified byte offset.
	/// </summary>
	/// <param name="url"></param>
	/// <param name="byteOffset"></param>
	public void restart(String url, long byteOffset)
	{
		// Locals
		DownloadRequest downloadRequest;
		DownloadInformation downloadInformation;

		// Get download information
		downloadRequest = _downloadQueue.getDownloadRequest(url);
		if (downloadRequest == null)
		{
			downloadInformation = _completedDownloadCatalog.getDownloadInformation(url);
			if (downloadInformation == null)
			{
				return;
			}
			
			downloadRequest = new DownloadRequest();
			downloadRequest.setUrl(downloadInformation.getUrl());
			downloadRequest.setName(downloadInformation.getName());
			downloadRequest.setLocale(downloadInformation.getLocale());
			downloadRequest.setOverrideStorageLocation(downloadInformation.getStorageLocation());
			downloadRequest.setDownloadPriority(downloadInformation.getDownloadPriority());
			downloadRequest.setOverridePermittedNetworkTypes(downloadInformation.getPermittedNetworkTypes());

			if (byteOffset == OFFSET_ZERO)
			{
				delete(downloadInformation.getUrl());
			}
			else
			{
				downloadRequest.setAvailableLength(byteOffset);
			}
		}
		else
		{
			downloadRequest = new DownloadRequest();
			downloadRequest.setUrl(downloadRequest.getUrl());
			downloadRequest.setName(downloadRequest.getName());
			downloadRequest.setLocale(downloadRequest.getLocale());
			downloadRequest.setOverrideStorageLocation(downloadRequest.getOverrideStorageLocation());
			downloadRequest.setDownloadPriority(downloadRequest.getDownloadPriority());
			downloadRequest.setOverridePermittedNetworkTypes(downloadRequest.getOverridePermittedNetworkTypes());
		}

		// Restart download information
		_downloadQueue.add(downloadRequest, _defaultStorageLocation, _permittedNetworkTypes);
	}

	public void start() {
		startControllerThread(WAIT_MS_UPON_RESUME);
		_downloadQueue.startPersist();		
	}

	/// <summary>
	/// Resumes any download activity of this ProgressiveDownloader instance.
	/// </summary>
	public void resume()
	{
		resume(_downloadQueue.getQueuedDownloadRequestUrls());
	}


	/// <summary>
	/// Resumes any in-progress or future download of the DownloadRequests by the specified URLs.
	/// </summary>
	/// <param name="urls"></param>
	public void resume(Iterable<String> urls)
	{
		// Resume for each URL
		for (String url : urls)
		{
			resume(url);
		}
	}


	/// <summary>
	/// Resumes any in-progress or future download of the DownloadBatchRequest by the specified identifier.
	/// </summary>
	/// <param name="downloadBatchRequestId"></param>
	public void resume(UUID downloadBatchRequestId)
	{
		// Locals
		DownloadBatchRequest batchRequest;

		// Get batch downloadInformation
		batchRequest = _downloadQueue.getDownloadBatchRequest(downloadBatchRequestId);
		if (batchRequest == null)
		{
			return;
		}

		// Resume each item in batch
		for (DownloadRequest request : batchRequest.getDownloadRequests())
		{
			resume(request.getUrl());
		}
	}


	/// <summary>
	/// Resumes any in-progress or future download of the DownloadRequest by the specified URL.
	/// </summary>
	/// <param name="url"></param>
	public void resume(String url)
	{
		// Locals
		DownloadRequest request;

		// Reset status of download request in queue so that downloading may resume
		request = _downloadQueue.getDownloadRequest(url);
		if (request == null)
		{
			return;
		}
		request.setDownloadStatus(DownloadStatus.None);
	}
	
	
	
	
	
	/// <summary>
	/// Stops this download thread.
	/// </summary>
	public void stopControllerThread()
	{
		if (_controllerThread != null)
		{
			_status = ProgressiveDownloaderStatus.Paused;
			_timeToStopControllerThread = true;
			_controllerThread.interrupt();
			_controllerThread = null;
		}
	}


	/// <summary>
	/// Starts the download controller thread.
	/// </summary>
	/// <param name="waitMsUponStart">Milliseconds to wait before downloading starts.</param>
	private void startControllerThread(int waitMsUponStart)
	{
		// Start thread if not already started
		if (_controllerThread == null ||
			_controllerThread.getState() == Thread.State.TERMINATED)
		{
			Log.d(LCAT, "Starting the controller thread");
			_timeToStopControllerThread = false;
			_waitMsUponStart = waitMsUponStart;
            _controllerThread = new ControllerThread();// Thread(new ThreadStart(controllerThreadProc))
            _controllerThread.setPriority(CONTROLLER_THREAD_PRIORITY);
            _controllerThread.setName("Progressive Downloader Controller");
			_controllerThread.start();
		}
	}


	/// <summary>
	/// Stops all downloading.
	/// </summary>
	private void stopAllDownloading()
	{
		_downloadRequestUrlsThatMayProceed.clear();
	}
	
	class ControllerThread extends Thread
	{
		
		public void run()
		{
			// Locals
			Thread downloadThread;
			DownloadRequest request;

			// Initialize
			_status = ProgressiveDownloaderStatus.Started;

			// Perform initial wait
			try {
				Thread.sleep(_waitMsUponStart);

				Log.d(LCAT, "Download Queue Count: " + _downloadQueue.getDownloadRequestCount());
				// Loop
				while (_timeToStopControllerThread == false)
				{
					// Has the maximum simultaneous downloads been reached?
					if (_downloadQueue.getDownloadRequestCount() != 0 &&
						_downloadRequestUrlsThatMayProceed.size() < _maximumSimultaneousDownloads)
					{
						ArrayList<String> removeUrls = new ArrayList<String>();
						EnumSet<NetworkTypes> network = getNetworkTypes();
						for (Entry<String, EnumSet<NetworkTypes>> entry : _downloadRequestUrlsThatMayProceed.entrySet())
						{
							if (entry.getValue().containsAll(network) == false)
							{
								removeUrls.add(entry.getKey());
							}
						}
						
						for (String url : removeUrls)
						{
							_downloadRequestUrlsThatMayProceed.remove(url);
							request = _downloadQueue.getDownloadRequest(url);
							if (request != null)
							{
								request.setDownloadStatus(DownloadStatus.None);
							}
						}
						
						
						// Log.d(LCAT, "Need to download item?");
						// Anything to download?
						request = _downloadQueue.getNextDownloadCandidate(network);
						if (request != null &&
							_downloadRequestUrlsThatMayProceed.containsKey(request.getUrl()) == false)
						{
							// Log.d(LCAT, "Found item to download start download thread?");
							// Initialize download thread
							downloadThread = new DownloadThread(request);
							
							if (request.getDownloadPriority() == DownloadPriority.Low)
							{
								downloadThread.setPriority(DOWNLOAD_THREAD_PRIORITY_LOW);								
							} else if (request.getDownloadPriority() == DownloadPriority.High) {
								downloadThread.setPriority(DOWNLOAD_THREAD_PRIORITY_HIGH);
							} else {
								downloadThread.setPriority(DOWNLOAD_THREAD_PRIORITY_NORMAL);
							}
							
							downloadThread.setName(DOWNLOAD_THREAD_NAME_PREFACE + request.getName());
	
							_downloadRequestUrlsThatMayProceed.put(request.getUrl(), request.getFinalPermittedNetworkTypes());
	
							// Update status of download request
							if (request.getDownloadStatus() == DownloadStatus.None) {
								request.setDownloadStatus(DownloadStatus.InProgress);
							}
	
							// Begin download thread
							Log.d(LCAT, "Controller thread starting download thread");
							downloadThread.start();
						}
					}
	
					// Wait between loops
					Thread.sleep(WAIT_MS_BETWEEN_QUEUED_REQUEST_CHECKS);
				}			
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private EnumSet<NetworkTypes> getNetworkTypes() {
		NetworkInfo netInfo = _connectivityManager.getActiveNetworkInfo();
		
		if (netInfo != null)
		{
			int netType = netInfo.getType();
			if (netType == ConnectivityManager.TYPE_WIFI)
			{
				return EnumSet.of(NetworkTypes.Wireless80211);
			} 
			else if (netType == ConnectivityManager.TYPE_MOBILE)
			{
				return NetworkTypes.Mobile;
			}
		}
		
		return EnumSet.of(NetworkTypes.None);
	
	}
	
	/// <summary>
	/// Services a download downloadInformation until directed to stop.
	/// </summary>
	class DownloadThread extends Thread
	{
		private DownloadRequest request;
		
		public DownloadThread(DownloadRequest request)
		{
			this.request = request;
		}
		
		public void run() 
		{
			Log.d(LCAT, "Download thread started");
			// Locals
			HttpClient webRequest1;
			DownloadRequest downloadRequest;
			DownloadInformation downloadInformation;
			DownloadBatchRequest downloadBatchRequest;
			DownloadBatchInformation downloadBatchInformation;
			DownloadPriority priority;

			// Initialize
			downloadInformation = null;
			downloadBatchInformation = null;
			downloadRequest = this.request;
			downloadBatchRequest = downloadRequest.getDownloadBatchRequestId() == null ?
				null : _downloadQueue.getDownloadBatchRequest(downloadRequest.getDownloadBatchRequestId());
			priority = downloadRequest.getDownloadPriority();

			try
			{
				// Initialize download information to report progress
				downloadInformation = new DownloadInformation();
				downloadInformation.setUrl(downloadRequest.getUrl());
				downloadInformation.setName(downloadRequest.getName());
				downloadInformation.setLocale(downloadRequest.getLocale());
				downloadInformation.setFilePath(downloadRequest.getFilePath());
				downloadInformation.setLength(downloadRequest.getLength());
				downloadInformation.setMediaBitsPerSecond(downloadRequest.getMediaBitsPerSecond());
				downloadInformation.setAvailableLength(downloadRequest.getAvailableLength());
				downloadInformation.setCreationUtc(downloadRequest.getCreationUtc());
				downloadInformation.setLastWriteUtc(downloadRequest.getLastWriteUtc());
				downloadInformation.setLastDownloadBitsPerSecond(downloadRequest.getLastDownloadBitsPerSecond());
				downloadInformation.setDownloadPriority(downloadRequest.getDownloadPriority());
				downloadInformation.setIsReadyForPlayback(downloadRequest.getIsReadyForPlayback());
				downloadInformation.setPermittedNetworkTypes(downloadRequest.getFinalPermittedNetworkTypes());
				downloadInformation.setStorageLocation(downloadRequest.getFinalStorageLocation());

				if (downloadBatchRequest != null)
				{
					// Get or create batch download information
					synchronized (_downloadBatchInformationLock)
					{
						downloadBatchInformation = _downloadBatchInformationById.get(downloadBatchRequest.getDownloadBatchRequestId());
						if (downloadBatchInformation == null)
						{
							downloadBatchInformation = new DownloadBatchInformation();
							downloadBatchInformation.setDownloadBatchRequestId(downloadBatchRequest.getDownloadBatchRequestId());
							downloadBatchInformation.setName(downloadBatchRequest.getName());
							downloadBatchInformation.setLocale(downloadBatchRequest.getLocale());
							downloadBatchInformation.setOverrideStorageLocation(downloadBatchRequest.getOverrideStorageLocation());
							downloadBatchInformation.setDownloadPriority(downloadBatchRequest.getDownloadPriority());
							_downloadBatchInformationById.put(downloadBatchRequest.downloadBatchRequestId, downloadBatchInformation);
						}

						// Add this download's information to the batch's collection of download informations
						downloadBatchInformation.getDownloadInformations().add(downloadInformation);
					}
				}
				
				// Locals
				int byteCount;
				byte[] buffer;
				FileOutputStream fileStream;
				long bpsTrackingSpan;
				Date bpsTrackingStart;
				int trackBpsIterationCount;
				int bytesForBps;
				Date lastFiredProgressEvent;
				boolean downloadBatchRequestIsComplete;

				// Initialize
				fileStream = null;
				trackBpsIterationCount = 0;
				bpsTrackingSpan = 0;
				bytesForBps = 0;
				
				InputStream responseStream = null;
				
				try
				{
					// Open output file and seek to next write position
					String filePath = downloadRequest.getFilePath();
					if (filePath.startsWith("file://"))
					{
						filePath = filePath.substring(7);
					}
					
					String message = "resume";
					Log.d(LCAT, "Creating file for writing: " + filePath);
					File file = new File(filePath);
					if (file.exists() == false) 
					{
						message = "start";
						boolean createRet = file.createNewFile();
						Log.d(LCAT, "file.createNewFile returned " + createRet);
					}

					downloadInformation.setMessage(message);
					DownloadStarted.fireEvent(new DownloadEvent(this, downloadInformation, null));

					
					
					Log.d(LCAT, "File length: " + file.length() + " available length: " + downloadRequest.getAvailableLength());	
					if (file.length() != downloadRequest.getAvailableLength())
					{
						Log.d(LCAT, "File length and available length were different so using file length");	
						downloadRequest.setAvailableLength(file.length());
					}
					
					webRequest1 = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					request.setURI(new URI(downloadRequest.getUrl()));
					Map<String, Object> headers = downloadRequest.getHeaders();
					if(headers != null && !headers.isEmpty()) {
						for ( Entry<String, Object> entry : headers.entrySet()) {
							request.setHeader(entry.getKey(), entry.getValue().toString());
						}
					}
					
					if (downloadRequest.getAvailableLength() > OFFSET_ZERO)
					{
						request.setHeader("Range", "bytes=" + downloadRequest.getAvailableLength() + '-' + downloadRequest.getLength());
						// Start downloading after already-downloaded bytes
						//webRequest1.AddRange(downloadRequest.getAvailableLength());
					}
					//webRequest1.AutomaticDecompression = DecompressionMethods.GZip | DecompressionMethods.Deflate;
					HttpResponse response = webRequest1.execute(request);
					responseStream = response.getEntity().getContent();
					FileOutputStream outputStream;
					
					// Error Check: We do not want to continue if response code is above 400 (error occured)
					int statusCode = response.getStatusLine().getStatusCode();
					if(statusCode >= 400){
						Log.e(LCAT, "Download is invalid. Response code: " + statusCode);
						downloadInformation.setMessage("Response error");
						throw new RuntimeException("Download is invalid.");
					}
					
					
						// Get response
						if (response.containsHeader(HTTP_RESPONSE_HEADER_ACCEPT_RANGES) == true &&
							response.getFirstHeader(HTTP_RESPONSE_HEADER_ACCEPT_RANGES).getValue() == HTTP_RESPONSE_HEADER_ACCEPT_RANGES_NONE)
						{
							// Response is for entire file, or server does not support byte ranges, so reset available length to 0
							Log.d(LCAT, "Resetting available length");
							downloadRequest.setAvailableLength(0);
							downloadInformation.setAvailableLength(0);
							outputStream = new FileOutputStream(file, false);
						}
						else
						{
							outputStream = new FileOutputStream(file, true);
						}

						// On first download downloadInformation for file, set length of downloadRequest
						if (downloadRequest.getLength() == 0 ||
							downloadRequest.getAvailableLength() == 0)
						{
							long contentLength = Long.parseLong(response.getFirstHeader("content-length").getValue());
							Log.d(LCAT, "Setting length to " + contentLength);
							downloadRequest.setLength(contentLength);
							downloadInformation.setLength(contentLength);
						}
						
						//FileOutputStream outputStream = new FileOutputStream(file, true);
						outputStream.flush();
						
						// Receive and store bytes
						long loopCount = 0;
						buffer = new byte[DOWNLOAD_BUFFER_SIZE];
						bpsTrackingStart = new Date();
						lastFiredProgressEvent = new Date();
						while (_downloadRequestUrlsThatMayProceed.containsKey(downloadRequest.getUrl()) == true &&
							(byteCount = responseStream.read(buffer, 0, DOWNLOAD_BUFFER_SIZE)) > 0)
						{
							if (priority == DownloadPriority.Low) {
								Thread.yield();								
							} else if (priority == DownloadPriority.Normal && (loopCount % 4) == 0) {
								Thread.yield();
							}
							++loopCount;
							
							bytesForBps += byteCount;
							// Update status of downloadRequest
							if (downloadRequest.getDownloadStatus() == DownloadStatus.None) {
								downloadRequest.setDownloadStatus(DownloadStatus.InProgress);
							}
							
							if (downloadBatchRequest != null && downloadBatchRequest.getDownloadStatus() == DownloadStatus.None)
							{
								downloadBatchRequest.setDownloadStatus(DownloadStatus.InProgress);
							}

							// Store bytes
							outputStream.write(buffer, 0, byteCount);
							outputStream.flush();

							// Update statistics
							downloadRequest.setAvailableLength(downloadRequest.getAvailableLength() + byteCount);
							downloadRequest.setLastWriteUtc(new Date());
							downloadInformation.setLastWriteUtc(downloadRequest.getLastWriteUtc());
							downloadInformation.setAvailableLength(downloadRequest.getAvailableLength());

							// Track bits/second (every n iterations through loop)
							trackBpsIterationCount++;
							if (trackBpsIterationCount == TRACK_BPS_ON_ITERATION_NUMBER)
							{
								Date now = new Date();
								
								bpsTrackingSpan = now.getTime() - bpsTrackingStart.getTime();
								if (bpsTrackingSpan != 0)
								{
									downloadRequest.setLastDownloadBitsPerSecond( (int) (
										(8.0 * // bits per byte
											bytesForBps) / // bytes downloaded in timespan
										(bpsTrackingSpan/1000.0))); // Seconds in timespan
									downloadInformation.setLastDownloadBitsPerSecond(downloadRequest.getLastDownloadBitsPerSecond());
								}
							}
							
							// Fire progress event (twice every second)
							Date now = new Date();
							if(now.getTime() - lastFiredProgressEvent.getTime() > 500){
								lastFiredProgressEvent = now;
								DownloadProgress.fireEvent(new DownloadEvent(this, downloadInformation, downloadBatchInformation));
							}
							
							// Reset tracking of bits/second (every n iterations through loop)
							if (trackBpsIterationCount == TRACK_BPS_ON_ITERATION_NUMBER)
							{
								trackBpsIterationCount = 0;
								if (bpsTrackingSpan != 0)
								{
									bytesForBps = 0;
									bpsTrackingStart = new Date();
								}
							}
						}

						// Did the download complete?
						if (_downloadRequestUrlsThatMayProceed.containsKey(downloadRequest.getUrl()) == true)
						{
							// The download succeeded

							// Close file
							if (outputStream != null)
							{
								outputStream.close();
								outputStream.flush();
								outputStream = null;
							}

							// Remove download request from queue
							_downloadQueue.remove(downloadRequest.getUrl());
							downloadRequest.setDownloadStatus(DownloadStatus.Complete); // This must come after removing the request from the queue to avoid re-downloading

							// Update batch download status
							if (downloadBatchRequest != null)
							{
								boolean isInProgress = false;
								for (DownloadRequest dr : downloadBatchRequest.getDownloadRequests())
								{
									if (dr.getDownloadStatus() == DownloadStatus.InProgress)
									{
										isInProgress = true;
										break;
									}
								}
								
								downloadBatchRequest.setDownloadStatus(isInProgress ? DownloadStatus.InProgress : DownloadStatus.None);
							}

							// Close response
							if (responseStream != null)
							{
								responseStream.close();
								responseStream = null;
							}

							// Remove download tracking
							_downloadRequestUrlsThatMayProceed.remove(downloadRequest.getUrl());

							// Cleanup batch request?
							if (downloadBatchRequest == null)
							{
								downloadBatchRequestIsComplete = false;
							}
							else
							{
								downloadBatchRequestIsComplete = _downloadQueue.downloadBatchRequestIsComplete(downloadBatchRequest.getDownloadBatchRequestId());
								if (downloadBatchRequestIsComplete == true)
								{
									// Remove batch request from queue
									_downloadQueue.remove(downloadBatchRequest.getDownloadBatchRequestId());

									// Synchronize
									synchronized (_downloadBatchInformationLock)
									{
										// Remove batch's download information (if any)
										if (_downloadBatchInformationById.containsKey(downloadBatchRequest.getDownloadBatchRequestId()) == true)
										{
											_downloadBatchInformationById.remove(downloadBatchRequest.getDownloadBatchRequestId());
										}
									}
								}
							}

							// Create record for completed item
							if (downloadRequest.getAvailableLength() >= downloadRequest.getLength())
							{
								_completedDownloadCatalog.addCompletedDownload(downloadInformation);
							}
							
							// Fire an extra progress event to keep it at 100%
							DownloadProgress.fireEvent(new DownloadEvent(this, downloadInformation, downloadBatchInformation));

							// Fire downloadInformation completed event
							DownloadCompleted.fireEvent(new DownloadEvent(this, downloadInformation, null));
							
							// Batch completed?
							if (downloadBatchInformation != null &&
								downloadBatchRequestIsComplete == true)
							{
								// Create record for completed batch
								_completedDownloadCatalog.addCompletedBatchDownload(downloadBatchInformation);

								// Fire batch completed event?
								DownloadBatchCompleted.fireEvent(new DownloadEvent(this, null, downloadBatchInformation));
							}
						}
						else
						{
							// The download was cancelled or paused

							// Reset status of download request in download queue
							if (downloadRequest.getDownloadStatus() == DownloadStatus.InProgress)
							{
								downloadRequest.setDownloadStatus(DownloadStatus.None);
							}
							if (downloadBatchRequest != null)
							{
								boolean isInProgress = false;
								for (DownloadRequest dr : downloadBatchRequest.getDownloadRequests())
								{
									if (dr.getDownloadStatus() == DownloadStatus.InProgress)
									{
										isInProgress = true;
										break;
									}
								}
								
								downloadBatchRequest.setDownloadStatus(isInProgress ? DownloadStatus.InProgress : DownloadStatus.None);
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						Log.d(LCAT, "Download thread exception " + e.toString());
						// Cancel the entire batch?
						if (downloadBatchRequest != null)
						{
							cancel(downloadBatchRequest.getDownloadBatchRequestId());
						}
						
						// Fire download failed events
						DownloadFailed.fireEvent(new DownloadEvent(this, downloadInformation, null));
						
						// check for batch
						if (downloadBatchRequest != null)
						{
							DownloadBatchFailed.fireEvent(new DownloadEvent(this, null, downloadBatchInformation));
						}
					}
					finally
					{
						// Close file if necessary
						if (fileStream != null)
						{
							fileStream.flush();
							fileStream.close();
							fileStream = null;
						}

						// Close response if necessary
						if (responseStream != null)
						{
							responseStream.close();
							responseStream = null;
						}
					}
			}
			catch (Exception e)
			{
				// Cleanup
				Log.d(LCAT, "Download thread exception " + e.toString());
				downloadRequest.setDownloadStatus(DownloadStatus.None);
				if (downloadBatchRequest != null)
				{
					boolean isInProgress = false;
					for (DownloadRequest dr : downloadBatchRequest.getDownloadRequests())
					{
						if (dr.getDownloadStatus() == DownloadStatus.InProgress)
						{
							isInProgress = true;
							break;
						}
					}
					
					downloadBatchRequest.setDownloadStatus(isInProgress ? DownloadStatus.InProgress : DownloadStatus.None);
				}

				_downloadRequestUrlsThatMayProceed.remove(downloadRequest.getUrl());

				// Cancel the entire batch?
				if (downloadBatchRequest != null)
				{
					cancel(downloadBatchRequest.getDownloadBatchRequestId());
				}

				// Fire download failed events
				DownloadFailed.fireEvent(new DownloadEvent(this, downloadInformation, null));
				if (downloadBatchRequest != null)
				{
					DownloadBatchFailed.fireEvent(new DownloadEvent(this, null, downloadBatchInformation));
				}
			}
			
		}
	}
}
