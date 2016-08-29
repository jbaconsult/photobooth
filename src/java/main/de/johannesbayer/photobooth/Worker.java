package de.johannesbayer.photobooth;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.johannesbayer.photobooth.printing.PrintJob;
import de.johannesbayer.photobooth.printing.PrintScheduler;
import de.johannesbayer.photobooth.upload.Configuration;
import de.johannesbayer.photobooth.upload.UploaderFactory;

public class Worker implements Runnable {

	private Log log = LogFactory.getLog(Worker.class);

	private BufferedImage imageUpLeft;
	private BufferedImage imageUpRight;
	private BufferedImage imageDownLeft;
	private BufferedImage imageDownRight;

	int width = Integer.valueOf(Configuration.INSTANCE().getProps().getProperty(Configuration.TARGETIMAGE_WIDTH));
	int height = Integer.valueOf(Configuration.INSTANCE().getProps().getProperty(Configuration.TARGETIMAGE_HEIGHT));
	private String outputDir;

	@Override
	public void run() {
		try {
			start();
		} catch (IOException e) {
			log.error(e);
		}
	}

	public Worker(String outputDir, BufferedImage imageUpLeft, BufferedImage imageUpRight, BufferedImage imageDownLeft,
			BufferedImage imageDownRight) {
		this.outputDir = outputDir;
		this.imageUpLeft = imageUpLeft;
		this.imageUpRight = imageUpRight;
		this.imageDownLeft = imageDownLeft;
		this.imageDownRight = imageDownRight;
	}

	private void start() throws IOException {
		if (imageUpLeft != null && imageUpRight != null && imageDownLeft != null && imageDownRight != null) {
			BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = combined.getGraphics();
			g.drawImage(imageUpLeft, 0, 0, null);
			g.drawImage(imageUpRight, width / 2, 0, null);
			g.drawImage(imageDownLeft, 0, height / 2, null);
			g.drawImage(imageDownRight, width / 2, height / 2, null);
			String timeStamp = new SimpleDateFormat("MM_dd_HH_mm_ss").format(new Date());
			File out = new File(outputDir, "combined_" + timeStamp + ".jpg");
			log.info("Combined image combined_" + timeStamp + ".jpg created");
			ImageIO.write(combined, "jpg", out);
			if (Configuration.INSTANCE().getProps().getProperty(Configuration.FTP_SERVER) != null) {
				log.info("Uploading file to ftp");
				UploaderFactory.getUploader().doUpload(out);
			}
			log.info("removing: " + Configuration.INSTANCE().getProps().getProperty(Configuration.WORKING_IMAGE));
			File file = new File(Configuration.INSTANCE().getProps().getProperty(Configuration.WORKING_IMAGE));
			UploaderFactory.getUploader().deleteFile(file.getName());
			log.info("Adding image to printer queue");
			PrintScheduler.addJob(new PrintJob(combined));
			if (Boolean
					.parseBoolean(Configuration.INSTANCE().getProps().getProperty(Configuration.DELETE_INTERMEDIATE))) {
				out.delete();
			}
		}
	}
}
