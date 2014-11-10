package dk.napp.downloadmanager;

import java.io.Serializable;
import java.util.Date;
import java.util.EnumSet;

public abstract class BaseDownloadRequest implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6931428897911014914L;
	private String name;
	private String locale;
	private Date creationUtc;
	private String overrideStorageLocation;
	private DownloadPriority priority = DownloadPriority.Normal;
	private EnumSet<NetworkTypes> overridePermittedNetworkTypes;
	
	private long length;
	private long availableLength;
	private String finalStorageLocation;
	private DownloadStatus status;
	private EnumSet<NetworkTypes> finalPermittedNetworkTypes;
	
	public String getName()
	{
		return this.name;
	}
	public void setName(String e)
	{
		this.name = e;
	}
	
	public String getLocale()
	{
		return this.locale;
	}
	public void setLocale(String e)
	{
		this.locale = e;
	}
	
	public Date getCreationUtc()
	{
		return this.creationUtc;
	}
	public void setCreationUtc(Date e)
	{
		this.creationUtc = e;
	}

	public String getOverrideStorageLocation()
	{
		return this.overrideStorageLocation;
	}
	public void setOverrideStorageLocation(String e)
	{
		this.overrideStorageLocation = e;
	}

	public DownloadPriority getDownloadPriority()
	{
		return this.priority;
	}
	public void setDownloadPriority(DownloadPriority e)
	{
		this.priority = e;
	}
	
	public EnumSet<NetworkTypes> getOverridePermittedNetworkTypes()
	{
		return this.overridePermittedNetworkTypes;
	}
	public void setOverridePermittedNetworkTypes(EnumSet<NetworkTypes> e)
	{
		this.overridePermittedNetworkTypes = e;
	}
	
	public long getLength()
	{
		return this.length;
	}
	public void setLength(long e)
	{
		this.length = e;
	}
	
	public long getAvailableLength()
	{
		return this.availableLength;
	}
	public void setAvailableLength(long e)
	{
		this.availableLength = e;
	}

	public String getFinalStorageLocation()
	{
		return this.finalStorageLocation;
	}
	public void setFinalStorageLocation(String e)
	{
		this.finalStorageLocation = e;
	}
	
	public DownloadStatus getDownloadStatus()
	{
		return this.status;
	}
	public void setDownloadStatus(DownloadStatus e)
	{
		this.status = e;
	}

	public EnumSet<NetworkTypes> getFinalPermittedNetworkTypes()
	{
		return this.finalPermittedNetworkTypes;
	}
	public void setFinalPermittedNetworkTypes(EnumSet<NetworkTypes> e)
	{
		this.finalPermittedNetworkTypes = e;
	}
	
}
