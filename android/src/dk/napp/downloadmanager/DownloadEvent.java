package dk.napp.downloadmanager;

import java.util.EventObject;

public class DownloadEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4683819652413591938L;
	private DownloadInformation info;
	private DownloadBatchInformation batchInfo;
	
	public DownloadEvent(Object source, DownloadInformation downloadInfo, DownloadBatchInformation batchInfo)
	{
		super(source);
		this.info = downloadInfo;
		this.batchInfo = batchInfo;
	}
	
	public DownloadInformation getDownloadInformation()
	{
		return this.info;
	}
	
	public DownloadBatchInformation getDownloadBatchInformation() 
	{
		return this.batchInfo;
	}
}
