package dk.napp.downloadmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;

import android.content.ContextWrapper;

public class DownloadQueue {
	// Contstants
	private static final String LCAT = "DownloadQueue";
	private static final int PERSIST_TIMER_WAIT_MS_INTERVAL = 5000;
	private static final int PERSIST_TIMER_WAIT_MS_ON_START = 5000;
	private static final String REQUEST_PERSIST_FILENAME = "DownloadQueue.dat";
	private static final String BATCH_REQUEST_PERSIST_FILENAME = "DownloadBatches.dat";
	
	private Object syncRoot;
	private Timer persistTimer;
	
	private ArrayList<DownloadRequest> downloadRequests;
	private ArrayList<DownloadBatchRequest> batchRequests;
	private ContextWrapper context;
	
	public DownloadQueue(ContextWrapper context) {
		this.context = context;
		this.syncRoot = new Object();
		loadFromStorage();
		startPersist();
	}
	
	public int getDownloadRequestCount() {
		synchronized(this.syncRoot) {
			return this.downloadRequests.size();
		}
	}
	
	public DownloadRequest remove(String url) {
		DownloadRequest request = null; 
		
		synchronized(this.syncRoot) {
			
			for (DownloadRequest r : this.downloadRequests) {
				if (r.getUrl().equals(url)) {
					request = r;
					break;
				}
			}
			
			if (request != null) {
				downloadRequests.remove(request);
			}
		}
		
		return request;
	}
	
	public void stopPersist() {
		if (persistTimer != null) {
			persistTimer.cancel();
			persistTimer = null;
		}		
	}
	
	public void startPersist() {
		stopPersist();
		
		this.persistTimer = new Timer();
		this.persistTimer.schedule(new PersistTask(), PERSIST_TIMER_WAIT_MS_ON_START, PERSIST_TIMER_WAIT_MS_INTERVAL);		
	}
	
	public DownloadBatchRequest remove(UUID downloadBatchRequestId) {
		DownloadBatchRequest batchRequest = null;
		
		synchronized(this.syncRoot) {
			for (DownloadBatchRequest b : this.batchRequests) {
				if (b.getDownloadBatchRequestId().equals(downloadBatchRequestId)) {
					batchRequest = b;
					break;
				}
			}
			
			if (batchRequest != null) {
				this.batchRequests.remove(batchRequest);
				
				for (DownloadRequest dr : batchRequest.getDownloadRequests()) {
					for (DownloadRequest r : this.downloadRequests) {
						if (r.getUrl().equals(dr.getUrl())) {
							this.downloadRequests.remove(r);
							break;
						}
					}
					
				}
			}
			
		}
		
		return batchRequest;
	}

	public void add(DownloadRequest downloadRequest, String defaultStorageLocation, EnumSet<NetworkTypes> defaultPermittedNetworkTypes)
	{
		// Set final storage location of request
		if (downloadRequest.getOverrideStorageLocation() == null || downloadRequest.getOverrideStorageLocation().length() == 0)
		{
			downloadRequest.setFinalStorageLocation(defaultStorageLocation);
		}
		else
		{
			downloadRequest.setFinalStorageLocation(downloadRequest.getOverrideStorageLocation());
		}

		// Set final permitted network types of request
		if (downloadRequest.getOverridePermittedNetworkTypes() == null)
		{
			downloadRequest.setFinalPermittedNetworkTypes(defaultPermittedNetworkTypes);
		}
		else
		{
			downloadRequest.setFinalPermittedNetworkTypes(downloadRequest.getOverridePermittedNetworkTypes());
		}

		// Set UTC-based created date/time of request
		downloadRequest.setCreationUtc(new Date());
			
		// Synchronize
		synchronized (this.syncRoot)
		{
			// Add request
			this.downloadRequests.add(downloadRequest);
		}
	}


	public void add(DownloadBatchRequest batchRequest, String defaultStorageLocation, EnumSet<NetworkTypes> defaultPermittedNetworkTypes)
	{
		// Set UTC-based created date/time of request
		batchRequest.setCreationUtc(new Date());

		// Set final storage location of request
		if (batchRequest.getOverrideStorageLocation() == null || batchRequest.getOverrideStorageLocation().length() == 0)
		{
			batchRequest.setFinalStorageLocation(defaultStorageLocation);
		}
		else
		{
			batchRequest.setFinalStorageLocation(batchRequest.getOverrideStorageLocation());
		}

		// Synchronize
		synchronized (this.syncRoot)
		{
			// Add batch request
			this.batchRequests.add(batchRequest);

			// Add batch's download requests
			for (DownloadRequest downloadRequest : batchRequest.getDownloadRequests())
			{
				// Set batch ID on download request
				downloadRequest.setDownloadBatchRequestId(batchRequest.getDownloadBatchRequestId());

				// Add request
				add(downloadRequest, batchRequest.getFinalStorageLocation(), defaultPermittedNetworkTypes);
			}
		}
	}


	public boolean downloadBatchRequestIsComplete(UUID downloadBatchRequestId)
	{
		// Synchronize
		synchronized (this.syncRoot)
		{
			for (DownloadRequest r : this.downloadRequests)
			{
				if (r.getDownloadBatchRequestId().equals(downloadBatchRequestId))
				{
					return false;
				}
			}
			return true;
		}
	}


	public Iterable<String> getQueuedDownloadRequestUrls()
	{
		// Synchronize
		synchronized (this.syncRoot)
		{
			ArrayList<String> urls = new ArrayList<String>();
			for (DownloadRequest r : this.downloadRequests)
			{
				urls.add(r.getUrl());
			}
			
			return urls;
		}
	}


	public DownloadRequest getDownloadRequest(String url)
	{
		// Locals
		DownloadRequest request = null;

		// Synchronize
		synchronized (this.syncRoot)
		{
			// Get request
			for (DownloadRequest r : this.downloadRequests)
			{
				if (r.getUrl().equals(url))
				{
					request = r;
					break;
				}
			}
		}

		// Return request
		return request;
	}


	public DownloadBatchRequest getDownloadBatchRequest(UUID downloadBatchRequestId)
	{
		// Locals
		DownloadBatchRequest request = null;

		// Synchronize
		synchronized (this.syncRoot)
		{
			// Get request
			for (DownloadBatchRequest r : this.batchRequests)
			{
				if (r.getDownloadBatchRequestId().equals(downloadBatchRequestId))
				{
					request = r;
					break;
				}
			}
		}

		// Return request
		return request;
	}


	public DownloadRequest getNextDownloadCandidate(EnumSet<NetworkTypes> network)
	{
		// Synchronize
		synchronized (this.syncRoot)
		{
			DownloadRequest drc;
			DownloadRequest bdrc;
			DownloadBatchRequest brc;

			// Get next download request candidate
			drc = getNextDownloadCandidateFromRequests(network);

			// Get next batch request candidate
			brc = getNextBatchDownloadRequestCandidate(network);

			//Log.d(LCAT, "DownloadQueue.getNextDownloadCandidate drc=" + drc + " brc=" + brc);
			// Select next candidate from download request or batch request
			if (brc == null)
			{
				// No batch candidate, so return download request candidate (if any)
				return drc;
			}
			else if (drc == null)
			{
				bdrc = getNextDownloadCandidateFromBatch(brc);
				return bdrc;
			}
			else
			{
				// Is there a difference in priority?
				if (brc.getDownloadPriority() == drc.getDownloadPriority())
				{
					// Priorities are the same, so return item that was first added to queue
					if (brc.getCreationUtc().before(drc.getCreationUtc()))
					{
						bdrc = getNextDownloadCandidateFromBatch(brc);
						if (bdrc == null)
						{
							return drc;
						}
						else
						{
							return bdrc;
						}
					}
					else
					{
						return drc;
					}
				}
				else if (brc.getDownloadPriority().lower(drc.getDownloadPriority()))
				{
					// Return download request candidate (if any)
					return drc;
				}
				else
				{
					// Return next batch request candidate (if any)
					bdrc = getNextDownloadCandidateFromBatch(brc);
					if (bdrc == null)
					{
						return drc;
					}
					else
					{
						return bdrc;
					}
				}
			}
		}
	}
	
	
	public ArrayList<String> permittedNetworkTypesChanged(EnumSet<NetworkTypes> permitted)
	{
		ArrayList<String> urlsUpdated = new ArrayList<String>();
		for (DownloadRequest dr : this.downloadRequests)
		{
			// Set final permitted network types of request
			if (dr.getOverridePermittedNetworkTypes() == null)
			{
				dr.setFinalPermittedNetworkTypes(permitted);
				urlsUpdated.add(dr.getUrl());
			}
		}
		
		for (DownloadBatchRequest dbr : this.batchRequests)
		{
			// Set final permitted network types of request
			if (dbr.getOverridePermittedNetworkTypes() == null)
			{
				dbr.setFinalPermittedNetworkTypes(permitted);
			}
			for (DownloadRequest dr : dbr.getDownloadRequests())
			{
				// Set final permitted network types of request
				if (dr.getOverridePermittedNetworkTypes() == null)
				{
					dr.setFinalPermittedNetworkTypes(permitted);
					urlsUpdated.add(dr.getUrl());
				}
			}
		}
		
		return urlsUpdated;
	}
	
	private DownloadRequest getNextDownloadCandidateFromRequests(EnumSet<NetworkTypes> network)
	{
		// Locals
		DownloadRequest dc = null;

		// Get next download request candidate of the specified batch
		for (DownloadRequest dr : this.downloadRequests)
		{
			// Log.d(LCAT, "getNextDownloadCandidateFromRequests batchID: " + dr.getDownloadBatchRequestId() + " status: " + dr.getDownloadStatus());
			if (dr.getDownloadBatchRequestId() == null && dr.getDownloadStatus() == DownloadStatus.None && dr.getFinalPermittedNetworkTypes().containsAll(network))
			{
				if (dc == null)
				{
					dc = dr;
				}
				else if (dr.getDownloadPriority().higher(dc.getDownloadPriority()))
				{
					dc = dr;
				}
				else if (dr.getDownloadPriority() == dc.getDownloadPriority() && dr.getCreationUtc().before(dc.getCreationUtc()))
				{
					dc = dr;
				}
			}
		}
		
		// Log.d(LCAT, "getNextDownloadCandidateFromRequests result: " + dc);

		// Return result
		return dc;
	}
	
	private DownloadBatchRequest getNextBatchDownloadRequestCandidate(EnumSet<NetworkTypes> network)
	{
		// Locals
		DownloadBatchRequest brc = null;

		// Get next download request candidate of the specified batch
		for (DownloadBatchRequest br : this.batchRequests)
		{
			boolean canUse = false;
			if (br.getDownloadStatus() == DownloadStatus.None)
			{
				canUse = true;
			}
			else 
			{
				for (DownloadRequest dr : br.getDownloadRequests())
				{
					if (dr.getDownloadStatus() == DownloadStatus.None)
					{
						canUse = true;
						break;
					}
				}
			}
			
			if (canUse)
			{
				if (brc == null)
				{
					brc = br;
				}
				else if (br.getDownloadPriority().higher(brc.getDownloadPriority()))
				{
					brc = br;
				}
				else if (br.getDownloadPriority() == brc.getDownloadPriority() && br.getCreationUtc().before(brc.getCreationUtc()))
				{
					brc = br;
				}				
			}
		}

		// Return result
		return brc;
		
	}

	private DownloadRequest getNextDownloadCandidateFromBatch(DownloadBatchRequest brc)
	{
		// Locals
		DownloadRequest bdrc = null;

		// Get next download request candidate of the specified batch
		for (DownloadRequest dr : brc.getDownloadRequests())
		{
			if (dr.getDownloadStatus() == DownloadStatus.None)
			{
				if (bdrc == null)
				{
					bdrc = dr;
				}
				else if (dr.getDownloadPriority().higher(bdrc.getDownloadPriority()))
				{
					bdrc = dr;
				}
				else if (dr.getDownloadPriority() == bdrc.getDownloadPriority() && dr.getCreationUtc().before(bdrc.getCreationUtc()))
				{
					bdrc = dr;
				}
			}
		}

		// Return result
		return bdrc;
	}


	private void persistToStorage()
	{
		// Synchronize
		synchronized (this.syncRoot)
		{
			// Persist
			FileOutputStream itemOutStream;
			try {
				// Log.d(LCAT, "persistToStorage number of requests = " + this.downloadRequests.size());
				itemOutStream = context.openFileOutput(REQUEST_PERSIST_FILENAME, 0);
				ObjectOutputStream objectOut = new ObjectOutputStream(itemOutStream);
				objectOut.writeObject(this.downloadRequests);
				objectOut.close();
				
				FileOutputStream batchOutStream = context.openFileOutput(BATCH_REQUEST_PERSIST_FILENAME, 0);
				ObjectOutputStream batchOut = new ObjectOutputStream(batchOutStream);
				batchOut.writeObject(this.batchRequests);
				batchOut.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}


	private void loadFromStorage()
	{
		Log.d(LCAT, "DownloadQueue.loadFromStorage");
		// Locals
		FileInputStream inStream;

		// Synchronize
		synchronized (this.syncRoot)
		{
			try
			{
				File file = new File(context.getFilesDir() + "/" + REQUEST_PERSIST_FILENAME);
				
				// File exists?
				if (file.exists() == false)
				{
					Log.d(LCAT, "Download Queue file does not exist so just create a new array");
					this.downloadRequests = new ArrayList<DownloadRequest>();
				}
				else
				{
					try
					{
						// Load
						Log.d(LCAT, "Download Queue file does exist so try loading it");
						inStream = context.openFileInput(REQUEST_PERSIST_FILENAME);
						ObjectInputStream ois = new ObjectInputStream(inStream);
						this.downloadRequests = (ArrayList<DownloadRequest>) ois.readObject();
						
						// reset the status of the requests
						for (DownloadRequest dr : this.downloadRequests) {
							dr.setDownloadStatus(DownloadStatus.None);
						}
						Log.d(LCAT, "Loaded number of requests = " + this.downloadRequests.size());
						ois.close();
						inStream.close();
					}
					catch (Exception e)
					{
						// Delete corrupt file
						Log.d(LCAT, "Download Queue exception loading file " + e.toString());
						file.delete();
						this.downloadRequests = new ArrayList<DownloadRequest>();
					}
				}
			}
			catch (Exception e)
			{
				Log.d(LCAT, "Download Queue exception " + e.toString());
				// Suppress exception at this level
				this.downloadRequests = new ArrayList<DownloadRequest>();
			}

			try
			{
				// File exists?
				File file = new File(BATCH_REQUEST_PERSIST_FILENAME);
				if (file.exists() == false)
				{
					this.batchRequests = new ArrayList<DownloadBatchRequest>();
				}
				else
				{
					try
					{
						// Load
						inStream = context.openFileInput(BATCH_REQUEST_PERSIST_FILENAME);
						ObjectInputStream ois = new ObjectInputStream(inStream);
						this.batchRequests = (ArrayList<DownloadBatchRequest>) ois.readObject();
						
						// reset the status on the batch and requests
						for (DownloadBatchRequest dbr : this.batchRequests) {
							dbr.setDownloadStatus(DownloadStatus.None);
							for (DownloadRequest dr : dbr.getDownloadRequests()) {
								dr.setDownloadStatus(DownloadStatus.None);
							}
						}
						
						inStream.close();
					}
					catch (Exception e)
					{
						// Delete corrupt file
						file.delete();
						this.batchRequests = new ArrayList<DownloadBatchRequest>();
					}
				}
			}
			catch (Exception e)
			{
				// Suppress exception at this level
				this.batchRequests = new ArrayList<DownloadBatchRequest>();
			}
		}
	}
	
	
	
	class PersistTask extends TimerTask {
		public void run() {
			stopPersist();
			persistToStorage();
			startPersist();
		}
	}
}
