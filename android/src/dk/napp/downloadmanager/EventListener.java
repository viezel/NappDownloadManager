package dk.napp.downloadmanager;

import java.util.ArrayList;


public class EventListener<E> {
	private ArrayList<IListener<E>> listeners = new ArrayList<IListener<E>>();
	
	public synchronized void addListener(IListener<E> l) 
	{
		this.listeners.add(l);
	}
	public synchronized void removeListener(IListener<E> l)
	{
		this.listeners.remove(l);
	}
	public synchronized void fireEvent(E args)
	{
		for (IListener<E> l : this.listeners)
		{
			l.handleEvent(args);
		}
	}
}
