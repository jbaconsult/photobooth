package de.johannesbayer.photobooth.upload;

import de.johannesbayer.photobooth.FTPUpload;

public class UploaderFactory {
	public static Uploader getUploader() {
		if ("webdav".equals(Configuration.INSTANCE().getProps().getProperty(Configuration.CONNECTION_METHOD))) {
			return WebdavUpload.getINSTANCE();
		} else if ("ftp".equals(Configuration.INSTANCE().getProps().getProperty(Configuration.CONNECTION_METHOD))) {
			return FTPUpload.getINSTANCE();
		} else {
			return new NullUpload();
		}

	}
}
