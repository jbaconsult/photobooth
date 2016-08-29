package de.johannesbayer.photobooth.watcher;

import java.nio.file.Path;

public interface FileCreatedListener {
	void fileCreated(Path fileName);
}
