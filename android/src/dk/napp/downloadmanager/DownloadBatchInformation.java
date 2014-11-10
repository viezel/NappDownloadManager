package dk.napp.downloadmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class DownloadBatchInformation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DownloadBatchInformation()
	{
		this.downloadInformations = new ArrayList<DownloadInformation>();
	}

	private Object data;
	private String name;
	private String locale;
	private UUID downloadBatchRequestId;
	private String overrideStorageLocation;
	private DownloadPriority downloadPriority;
	private ArrayList<DownloadInformation> downloadInformations;

	public Object getData()
	{
		return this.data;
	}
	public void setData(Object value)
	{
		this.data = value;
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

	public UUID getDownloadBatchRequestId()
	{
		return this.downloadBatchRequestId;
	}
	public void setDownloadBatchRequestId(UUID value)
	{
		this.downloadBatchRequestId = value;
	}

	public String getOverrideStorageLocation()
	{
		return this.overrideStorageLocation;
	}
	public void setOverrideStorageLocation(String value)
	{
		this.overrideStorageLocation = value;
	}

	public DownloadPriority getDownloadPriority()
	{
		return this.downloadPriority;
	}
	public void setDownloadPriority(DownloadPriority value)
	{
		this.downloadPriority = value;
	}
	
	public ArrayList<DownloadInformation> getDownloadInformations()
	{
		return this.downloadInformations;
	}

	public Date getCreationUtc()
	{
		Date creation = null;
		for (DownloadInformation di : this.downloadInformations)
		{
			if (creation == null)
			{
				creation = di.getCreationUtc();
			}
			else if (di.getCreationUtc().before(creation))
			{
				creation = di.getCreationUtc();
			}
		}
		return creation;
	}

	public long getLength()
	{
		long len = 0;
		for (DownloadInformation di : this.downloadInformations)
		{
			len += di.getLength();
		}
		return len;
	}

	public long getAvailableLength()
	{
		long len = 0;
		for (DownloadInformation di : this.downloadInformations)
		{
			len += di.getAvailableLength();
		}
		return len;
	}

	public Date getLastWriteUtc()
	{
		Date lastWrite = null;
		for (DownloadInformation di : this.downloadInformations)
		{
			if (lastWrite == null)
			{
				lastWrite = di.getLastWriteUtc();
			}
			else if (di.getLastWriteUtc().after(lastWrite))
			{
				lastWrite = di.getLastWriteUtc();
			}
		}
		return lastWrite;
	}

	public int getLastDownloadBitsPerSecond()
	{
		int bps = Integer.MAX_VALUE;
		for (DownloadInformation di : this.downloadInformations)
		{
			if (di.getLastDownloadBitsPerSecond() < bps)
			{
				bps = di.getLastDownloadBitsPerSecond();
			}
		}
		return bps;
	}
}
