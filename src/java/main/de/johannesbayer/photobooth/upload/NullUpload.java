package de.johannesbayer.photobooth.upload;

import java.io.File;

public class NullUpload implements Uploader {

	@Override
	public void doUpload(File file) {
		// dont do anything
	}

	@Override
	public void deleteFile(String fileName) {
		// dont do anything

	}

}
