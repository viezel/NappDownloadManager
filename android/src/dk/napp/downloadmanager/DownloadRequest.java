package dk.napp.downloadmanager;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class DownloadRequest extends BaseDownloadRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4231134533398097513L;

	private String filePath;
	
	private String url;
	private String fileName;
	
	private int mediaBitsPerSecond;
	private Date lastWriteUtc;
	private int lastDownloadBitsPerSecond;
	private boolean isReadyForPlayback;
	
	private UUID downloadBatchRequestId;
	
	
	public String getUrl() 
	{
		return this.url;
	}
	public void setUrl(String e)
	{
		this.url = e;
	}
	
	public String getFileName() 
	{
		return this.fileName;
	}
	public void setFileName(String e)
	{
		this.fileName = e;
	}
	
	public int getMediaBitsPerSecond() 
	{
		return this.mediaBitsPerSecond;
	}
	public void setMediaBitsPerSecond(int e)
	{
		this.mediaBitsPerSecond = e;
	}
	
	public Date getLastWriteUtc() 
	{
		return this.lastWriteUtc;
	}
	public void setLastWriteUtc(Date e)
	{
		this.lastWriteUtc = e;
	}
	
	public int getLastDownloadBitsPerSecond() 
	{
		return this.lastDownloadBitsPerSecond;
	}
	public void setLastDownloadBitsPerSecond(int e)
	{
		this.lastDownloadBitsPerSecond = e;
	}
	
	public boolean getIsReadyForPlayback() 
	{
		return this.isReadyForPlayback;
	}
	public void setIsReadyForPlayback(boolean e)
	{
		this.isReadyForPlayback = e;
	}
	
	public UUID getDownloadBatchRequestId() 
	{
		return this.downloadBatchRequestId;
	}
	public void setDownloadBatchRequestId(UUID e)
	{
		this.downloadBatchRequestId = e;
	}
	
	
	public String getFilePath() {
		// Cannot be set?
		if (this.getFinalStorageLocation() == null) {
			throw new java.lang.IllegalStateException("Cannot get FilePath until final storage location has been determined.");
		}
		
		// Already set?
		if (this.filePath == null) {
			this.filePath = this.getFinalStorageLocation() + this.fileName;
		}
		
		return this.filePath;
	}
}
