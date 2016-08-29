package de.johannesbayer.photobooth.upload;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

public class WebdavUpload implements Uploader {

	private Log log = LogFactory.getLog(WebdavUpload.class);
	Sardine sardine;
	private static WebdavUpload INSTANCE;
	private String server;

	private WebdavUpload() {
		server = "http://";
		server += Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_SERVER);
		server += ":" + Integer.parseInt(Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_PORT));
		server += "/";
		String user = Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_USER);
		String password = Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_PASS);
		sardine = SardineFactory.begin();
		sardine.setCredentials(user, password);		
	}

	@Override
	public void doUpload(File file) {
		log.info("Uploading: " + file.getName());
		try {
			byte[] data = FileUtils.readFileToByteArray(file);
			sardine.put(server + file.getName(), data);
		} catch (IOException e) {
			log.error(e);
		}
	}

	@Override
	public void deleteFile(String fileName) {
		log.info("deleteFile: " + fileName);
		try {
			sardine.delete(server + fileName);
		} catch (IOException e) {
			// unnessecary
			//log.error(e);
		}

	}

	public static WebdavUpload getINSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new WebdavUpload();
		}
		return INSTANCE;
	}
}
