package dk.napp.downloadmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class DownloadBatchRequest extends BaseDownloadRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -655345557346614063L;
	public UUID downloadBatchRequestId;
	public ArrayList<DownloadRequest> downloadRequests;
	
	public UUID getDownloadBatchRequestId() 
	{
		return this.downloadBatchRequestId;
	}
	public void setDownloadBatchRequestId(UUID e)
	{
		this.downloadBatchRequestId = e;
	}

	public ArrayList<DownloadRequest> getDownloadRequests() 
	{
		return this.downloadRequests;
	}
	public void setDownloadRequests(ArrayList<DownloadRequest> e)
	{
		this.downloadRequests = e;
	}
}
