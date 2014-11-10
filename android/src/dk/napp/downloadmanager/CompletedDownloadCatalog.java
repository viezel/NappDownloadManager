package dk.napp.downloadmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import android.content.ContextWrapper;

public class CompletedDownloadCatalog {
	// Constants
	private static final String ITEM_PERSIST_FILENAME = "DownloadItemCatalog.dat";
	private static final String BATCH_PERSIST_FILENAME = "DownloadBatchCatalog.dat";


	// Fields
	private Object _downloadInfoSync;
	private Map<String, DownloadInformation> _downloadInformationsByUrl;
	private Map<UUID, DownloadBatchInformation> _downloadBatchInformationsById;
	private ContextWrapper context;

	/// <summary>
	/// Constructor
	/// </summary>
	public CompletedDownloadCatalog(ContextWrapper context)
	{
		this.context = context;
		_downloadInfoSync = new Object();
		loadFromStorage();
	}


	/// <summary>
	/// Deletes any record and storage of a completed download matching the specified URL.
	/// </summary>
	/// <param name="url"></param>
	public void deleteCompletedDownload(String url)
	{
		// Locals
		DownloadInformation downloadInformation;

		// Synchronize
		synchronized (_downloadInfoSync)
		{
			// Is there an item by the specified URL?
			downloadInformation = _downloadInformationsByUrl.get(url);
			if (downloadInformation != null)
			{
				// Remove the item from the catalog
				_downloadInformationsByUrl.remove(url);

				// Delete the item from storage
				File file = new File(downloadInformation.getFilePath());
				if (file.exists() == true)
				{
					file.delete();
				}

				// Persist the catalog to storage on a background thread
				new PersistThread();
			}
		}
	}


	/// <summary>
	/// Deletes any record and storage of a completed download batch matching the specified identifier.
	/// </summary>
	/// <param name="downloadBatchRequestId"></param>
	public void deleteCompletedBatchDownload(UUID downloadBatchRequestId)
	{
		// Locals
		DownloadBatchInformation batchInformation;

		// Synchronize
		synchronized (_downloadInfoSync)
		{
			// Is there an item by the specified ID?
			batchInformation = _downloadBatchInformationsById.get(downloadBatchRequestId);
			if (batchInformation != null)
			{
				// Remove the item from the catalog
				_downloadBatchInformationsById.remove(downloadBatchRequestId);

				// Remove each item in the batch
				for (DownloadInformation di : batchInformation.getDownloadInformations())
				{
					// Remove the batch item from the catalog
					_downloadInformationsByUrl.remove(di.getUrl());

					// Delete the batch item from storage
					File file = new File(di.getFilePath());
					if (file.exists() == true)
					{
						file.delete();
					}
				}

				// Persist the catalog to storage on a background thread
				new PersistThread();
			}
		}
	}


	/// <summary>
	/// Adds the specified completed download information to the catalog.
	/// </summary>
	/// <param name="downloadInformation"></param>
	public void addCompletedDownload(DownloadInformation downloadInformation)
	{
		// Synchronize
		synchronized (_downloadInfoSync)
		{
			// Add or overwrite item
			_downloadInformationsByUrl.put(downloadInformation.getUrl(), downloadInformation);

			// Persist the catalog to storage on a background thread
			new PersistThread();
		}
	}


	/// <summary>
	/// Adds the specified completed download batch information to the catalog.
	/// </summary>
	/// <param name="downloadBatchInformation"></param>
	public void addCompletedBatchDownload(DownloadBatchInformation downloadBatchInformation)
	{
		// Synchronize
		synchronized (_downloadInfoSync)
		{
			// Add or overwrite item
			_downloadBatchInformationsById.put(downloadBatchInformation.getDownloadBatchRequestId(), downloadBatchInformation);

			// Add each item in the batch
			for (DownloadInformation di : downloadBatchInformation.getDownloadInformations())
			{
				// Add or overwrite item
				_downloadInformationsByUrl.put(di.getUrl(), di);
			}

			// Persist the catalog to storage on a background thread
			new PersistThread();
			//Task.Factory.StartNew(() => PersistToStorage());
		}
	}


	/// <summary>
	/// Attempts to get download information for the item by the specified URL.
	/// </summary>
	/// <param name="url"></param>
	/// <param name="downloadInformation"></param>
	/// <returns></returns>
	public DownloadInformation getDownloadInformation(String url)
	{
		// Synchronize
		synchronized (_downloadInfoSync)
		{
			return _downloadInformationsByUrl.get(url);
		}
	}


	/// <summary>
	/// Attempts to get download batch information for the item by the specified identifier.
	/// </summary>
	/// <param name="downloadBatchRequestId"></param>
	/// <param name="downloadBatchInformation"></param>
	/// <returns></returns>
	public DownloadBatchInformation getDownloadInformation(UUID downloadBatchRequestId)
	{
		// Synchronize
		synchronized (_downloadInfoSync)
		{
			return _downloadBatchInformationsById.get(downloadBatchRequestId);
		}
	}

	private class PersistThread implements Runnable
	{
		public PersistThread()
		{
			Thread t = new Thread(this);
			t.start();
		}
		
		public void run() 
		{
			persistToStorage();
		}
	}

	/// <summary>
	/// Persists catalog contents to storage.
	/// </summary>
	private void persistToStorage()
	{
		// Synchronize
		synchronized (_downloadInfoSync)
		{
			FileOutputStream itemOutStream;
			try {
				itemOutStream = context.openFileOutput(ITEM_PERSIST_FILENAME, 0);
				ObjectOutputStream objectOut = new ObjectOutputStream(itemOutStream);
				objectOut.writeObject(this._downloadInformationsByUrl);
				objectOut.close();
				
				FileOutputStream batchOutStream = context.openFileOutput(BATCH_PERSIST_FILENAME, 0);
				ObjectOutputStream batchOut = new ObjectOutputStream(batchOutStream);
				batchOut.writeObject(this._downloadBatchInformationsById);
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


	/// <summary>
	/// Loads catalog contents from storage.
	/// </summary>
	private void loadFromStorage()
	{
		// Locals
		FileInputStream inStream;

		// Synchronize
		synchronized (_downloadInfoSync)
		{
			try
			{
				// File exists?
				File file = new File(ITEM_PERSIST_FILENAME);
				if (file.exists() == false)
				{
					_downloadInformationsByUrl = new Hashtable<String, DownloadInformation>();
				}
				else
				{
					try
					{
						// Load
						inStream = context.openFileInput(ITEM_PERSIST_FILENAME);
						ObjectInputStream ois = new ObjectInputStream(inStream);
						this._downloadInformationsByUrl = (Hashtable<String, DownloadInformation>) ois.readObject();
						inStream.close();
					}
					catch (Exception e)
					{
						// Delete corrupt file
						file.delete();
						_downloadInformationsByUrl = new Hashtable<String, DownloadInformation>();
					}
				}
			}
			catch (Exception e)
			{
				// Suppress exception at this level
				_downloadInformationsByUrl = new Hashtable<String, DownloadInformation>();
			}

			try
			{
				// File exists?
				File file = new File(BATCH_PERSIST_FILENAME);
				if (file.exists() == false)
				{
					_downloadBatchInformationsById = new Hashtable<UUID, DownloadBatchInformation>();
				}
				else
				{
					try
					{
						// Load
						inStream = context.openFileInput(BATCH_PERSIST_FILENAME);
						ObjectInputStream ois = new ObjectInputStream(inStream);
						this._downloadBatchInformationsById = (Hashtable<UUID, DownloadBatchInformation>) ois.readObject();
						inStream.close();
					}
					catch (Exception e)
					{
						// Delete corrupt file
						file.delete();
						_downloadBatchInformationsById = new Hashtable<UUID, DownloadBatchInformation>();
					}
				}
			}
			catch (Exception e)
			{
				// Suppress exception at this level
				_downloadBatchInformationsById = new Hashtable<UUID, DownloadBatchInformation>();
			}
		}
	}

}
