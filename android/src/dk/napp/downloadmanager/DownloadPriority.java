package dk.napp.downloadmanager;

public enum DownloadPriority {
	None(0),
	Low(1),
	Normal(2),
	High(3);
	
	private int priority;
	private DownloadPriority(int priority)
	{
		this.priority = priority;
	}
	
	public boolean lower(DownloadPriority dp)
	{
		return this.priority < dp.priority;
	}
	
	public boolean higher(DownloadPriority dp)
	{
		return this.priority > dp.priority;
	}
	
	public int getValue()
	{
		return this.priority;
	}
}
