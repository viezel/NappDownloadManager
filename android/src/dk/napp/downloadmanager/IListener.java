package dk.napp.downloadmanager;

public interface IListener<E> {
	public void handleEvent(E event);
}
