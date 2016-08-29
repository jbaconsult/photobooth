package de.johannesbayer.photobooth.directory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.johannesbayer.photobooth.watcher.FileCreatedListener;
import de.johannesbayer.photobooth.watcher.Watcher;

public class DirectoryWatcher extends Watcher {

	private Log log = LogFactory.getLog(DirectoryWatcher.class);
	private String directory;

	public DirectoryWatcher(String directory, FileCreatedListener listener) {
		super(listener);
		this.directory = directory;
	}
	
	@Override
	public void run() {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path dir = Paths.get(directory);
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

			log.info("Watch Service registered for dir: " + dir.getFileName());

			while (true) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException ex) {
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = ev.context();
					if (kind.name().equals("ENTRY_CREATE")) {
						listener.fileCreated(fileName);
					}

					// log.info(kind.name() + ": " + fileName);

					if (kind == ENTRY_MODIFY && fileName.toString().equals("DirectoryWatchDemo.java")) {
						log.info("My source file has changed!!!");
					}
				}

				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}

		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

}
