package de.johannesbayer.photobooth.upload;

import java.io.File;

public interface Uploader {
	void doUpload(File file);
	void deleteFile(String fileName);
}
