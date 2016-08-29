package de.johannesbayer.photobooth.directory.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import de.johannesbayer.photobooth.watcher.FileCreatedListener;
import de.johannesbayer.photobooth.watcher.Watcher;

public class TranscendWatcher extends Watcher {

	private Log log = LogFactory.getLog(TranscendWatcher.class);
	private HttpClient httpClient;
	private String httpServer;
	private String dcimFolder;
	private File inputDir;
	private File downloadedFilesList;
	private Set<String> downloadedFiles;
	BufferedWriter bufferWritter;

	public TranscendWatcher(String httpServer, String dcimFolder, String inputDir, FileCreatedListener listener)
			throws IOException {
		super(listener);
		this.httpServer = httpServer;
		this.dcimFolder = dcimFolder;
		this.inputDir = new File(inputDir);
		if ((this.inputDir == null) || (!this.inputDir.isDirectory()) || (!this.inputDir.canWrite())) {
			RuntimeException e = new RuntimeException("Can not write in directory " + inputDir);
			log.error(e);
			throw e;
		}
		
		downloadedFiles = new HashSet<>();
		
		// check for downloadedFileList
		downloadedFilesList = new File(this.inputDir.getParent(), "downloadedFiles.txt");
		if (downloadedFilesList.exists()) {
			readDownloadedFiles();
		} else {
			downloadedFilesList.createNewFile();			
		}
		FileWriter fileWritter = new FileWriter(downloadedFilesList, true);
		bufferWritter = new BufferedWriter(fileWritter);
		httpClient = HttpClients.createDefault();
	}

	private void readDownloadedFiles() {
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new FileReader(downloadedFilesList));
			String line = "";
			while ((line = rd.readLine()) != null) {
				downloadedFiles.add(line);
			}
			closeStream(rd);
		} catch (FileNotFoundException e) {
			log.error(e);
			closeStream(rd);
		} catch (IOException e) {
			log.error(e);
			closeStream(rd);
		}
	}

	@Override
	public void run() {
		log.info("Starting loop");
		// checks once a second for new images.
		while (true) {
			String path = "/www/sd/DCIM/" + dcimFolder;
			String url = "http://" + httpServer + "/cgi-bin/tslist?PATH=" + path;
			log.info(url);
			HttpUriRequest request = new HttpGet(url);
			InputStream content = null;
			BufferedReader rd = null;
			try {
				HttpResponse response = httpClient.execute(request);
				content = response.getEntity().getContent();
				rd = new BufferedReader(new InputStreamReader(content));
				String line = "";
				int i = 0;
				while ((line = rd.readLine()) != null) {
					if (i == 2) {
						processFiles(line, path);
						i = 0;
					}
					i++;
				}
				closeStream(content);
				closeStream(rd);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(e);
				closeStream(content);
				closeStream(rd);
			} catch (ClientProtocolException e) {
				log.error(e);
				closeStream(content);
				closeStream(rd);
			} catch (IOException e) {
				log.error(e);
				closeStream(content);
				closeStream(rd);
			}
		}
	}

	private void processFiles(String line, String path) {		
		String[] segments = line.split("&");
		for (int i = 0; i < segments.length - 1; i = i + 2) {
			String fileSegment = segments[i];
			if (fileSegment != null) {
				String fileName = fileSegment.substring(fileSegment.lastIndexOf('=') + 1);
				downloadFile(fileName, path);
			}
		}
	}

	private void downloadFile(String fileName, String path) {
		if (downloadedFiles != null && !downloadedFiles.contains(fileName)) {
			log.info("Downloading: " + fileName);
			InputStream inputStream = null;
			FileOutputStream fos = null;
			try {
				String url = "http://" + httpServer + "/cgi-bin/wifi_download?fn=" + fileName + "&fd=" + path;
				//log.debug(url);
				HttpGet httpget = new HttpGet(url);
				HttpResponse response = httpClient.execute(httpget);
				HttpEntity entity = response.getEntity();
				File downloadedFile = new File(inputDir, fileName);
				if (entity != null) {
					inputStream = entity.getContent();
					fos = new FileOutputStream(downloadedFile);
					int inByte;
					while ((inByte = inputStream.read()) != -1) {
						fos.write(inByte);
					}
					closeStream(inputStream);
					closeStream(fos);
				}

				// write successfull download into file list
				downloadedFiles.add(fileName);	
				bufferWritter.write(fileName + "\n");
				bufferWritter.flush();
				
				// now add the file to the processing queue
				listener.fileCreated(Paths.get(downloadedFile.getName()));				
			} catch (IOException e) {
				log.error(e);
				closeStream(inputStream);
				closeStream(fos);
			}
		}
	}

	private void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
