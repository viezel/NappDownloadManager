package dk.napp.downloadmanager;

import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

public class DownloadInformation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object data;
	private String url;
	private String name;
	private String locale;
	private String filePath;
	private String storageLocation;
	private Map<String, Object> headers;
	private EnumSet<NetworkTypes> permittedNetworkTypes;
	private long length;
	private int mediaBitsPerSecond;
	private long availableLength;
	private Date creationUtc;
	private Date lastWriteUtc;
	private int lastDownloadBitsPerSecond;
	private DownloadPriority downloadPriority;
	private boolean isReadyForPlayback;
	private String message;
	
	public Object getData()
	{
		return this.data;
	}
	public void setData(Object value)
	{
		this.data = value;
	}
	
	public String getUrl()
	{
		return this.url;
	}
	public void setUrl(String value)
	{
		this.url = value;
	}
	
	public String getName()
	{
		return this.name;
	}
	public void setName(String value)
	{
		this.name = value;
	}
	
	public String getLocale()
	{
		return this.locale;
	}
	public void setLocale(String value)
	{
		this.locale = value;
	}
	
	public String getFilePath()
	{
		return this.filePath;
	}
	public void setFilePath(String value)
	{
		this.filePath = value;
	}
	
	public String getStorageLocation()
	{
		return this.storageLocation;
	}
	public void setStorageLocation(String value)
	{
		this.storageLocation = value;
	}
	
	public EnumSet<NetworkTypes> getPermittedNetworkTypes()
	{
		return this.permittedNetworkTypes;
	}
	public void setPermittedNetworkTypes(EnumSet<NetworkTypes> value)
	{
		this.permittedNetworkTypes = value;
	}
	
	public long getLength()
	{
		return this.length;
	}
	public void setLength(long value)
	{
		this.length = value;
	}
	
	public int getMediaBitsPerSecond()
	{
		return this.mediaBitsPerSecond;
	}
	public void setMediaBitsPerSecond(int value)
	{
		this.mediaBitsPerSecond = value;
	}
	
	public long getAvailableLength()
	{
		return this.availableLength;
	}
	public void setAvailableLength(long value)
	{
		this.availableLength = value;
	}
	
	public Date getCreationUtc()
	{
		return this.creationUtc;
	}
	public void setCreationUtc(Date value)
	{
		this.creationUtc = value;
	}
	
	public Date getLastWriteUtc()
	{
		return this.lastWriteUtc;
	}
	public void setLastWriteUtc(Date value)
	{
		this.lastWriteUtc = value;
	}
	
	public int getLastDownloadBitsPerSecond()
	{
		return this.lastDownloadBitsPerSecond;
	}
	public void setLastDownloadBitsPerSecond(int value)
	{
		this.lastDownloadBitsPerSecond = value;
	}
	
	public DownloadPriority getDownloadPriority()
	{
		return this.downloadPriority;
	}
	public void setDownloadPriority(DownloadPriority value)
	{
		this.downloadPriority = value;
	}
	
	public boolean getIsReadyForPlayback()
	{
		return this.isReadyForPlayback;
	}
	public void setIsReadyForPlayback(boolean value)
	{
		this.isReadyForPlayback = value;
	}
	
	public String getMessage() {
		return this.message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Map<String, Object> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}
}
