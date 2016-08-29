package de.johannesbayer.photobooth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import de.johannesbayer.photobooth.upload.Configuration;
import de.johannesbayer.photobooth.upload.Uploader;

public class FTPUpload implements Uploader {

	private Log log = LogFactory.getLog(FTPUpload.class);

	private static FTPClient ftpClient;
	private static FTPUpload INSTANCE;

	private FTPUpload() {
		ftpClient = new FTPClient();
	}

	public static FTPUpload getINSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new FTPUpload();
		}
		if (!ftpClient.isConnected()) {
			INSTANCE.connect();
		}

		return INSTANCE;
	}

	public void doUpload(File file) {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			log.info("Start uploading first file");
			boolean done = ftpClient.storeFile(file.getName(), inputStream);
			inputStream.close();
			if (done) {
				log.info("The first file is uploaded successfully.");
			}
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}

	}

	public void deleteFile(String filename) {
		try {
			ftpClient.deleteFile(filename);
		} catch (IOException e) {
			log.error(e);
		}
	}

	private void connect() {
		try {
			String server = Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_SERVER);
			int port = Integer.parseInt(Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_PORT));
			String user = Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_USER);
			String password = Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_PASS);
			log.info("Connecting to FTP, server " + server + " port " + port + " user " + user);
			ftpClient.setConnectTimeout(10);
			ftpClient.connect(server, port);
			ftpClient.login(user, password);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		} catch (IOException ex) {
			log.info("Error: " + ex.getMessage());
			log.error(ex);
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				log.error(ex);
			}
		}

	}
}
