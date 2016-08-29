package de.johannesbayer.photobooth.watcher;

public abstract class Watcher implements Runnable {

	protected FileCreatedListener listener;
	
	public Watcher(FileCreatedListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null");
		}
		this.listener = listener;
	}	
	
}
