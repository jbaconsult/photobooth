package de.johannesbayer.photobooth;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.johannesbayer.photobooth.upload.Configuration;
import de.johannesbayer.photobooth.upload.UploaderFactory;
import de.johannesbayer.photobooth.watcher.FileCreatedListener;

public class ImageStore implements FileCreatedListener {

	private Log log = LogFactory.getLog(ImageStore.class);

	private String outputDir = null;
	private String inputDir = null;

	private BufferedImage imageUpLeft = null;
	private BufferedImage imageUpRight = null;
	private BufferedImage imageDownLeft = null;
	private BufferedImage imageDownRight = null;
	int width = Integer.valueOf(Configuration.INSTANCE().getProps().getProperty(Configuration.TARGETIMAGE_WIDTH));
	int height = Integer.valueOf(Configuration.INSTANCE().getProps().getProperty(Configuration.TARGETIMAGE_HEIGHT));

	// in inches
	File workingImage;

	public ImageStore(String inputDir, String outputDir) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		workingImage = new File(Configuration.INSTANCE().getProps().getProperty(Configuration.WORKING_IMAGE));
		if (!workingImage.exists()) {
			log.warn("WARNING: 'working image' not found");
		}

		File ipd = new File(inputDir);
		// clearing in directory:
		File[] files = ipd.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				f.delete();
			}
		}
	}

	@Override
	public void fileCreated(Path fileName) {
		log.info("File created: " + fileName.toString());
		try {
			setImage(new File(inputDir, fileName.toString()));
		} catch (IOException e) {
			log.error(e);
		}
	}

	private synchronized void setImage(File file) throws IOException {
		if (!file.exists()) {
			log.error("File " + file + "does not exists");
			return;
		}
		if (imageUpLeft == null) {
			createWorkingImage();
			imageUpLeft = resize(file, width / 2, height / 2);
		} else if (imageUpRight == null) {
			imageUpRight = resize(file, width / 2, height / 2);
		} else if (imageDownLeft == null) {
			imageDownLeft = resize(file, width / 2, height / 2);
		} else {
			imageDownRight = resize(file, width / 2, height / 2);
			Worker w = new Worker(outputDir, imageUpLeft, imageUpRight, imageDownLeft, imageDownRight);
			Thread t = new Thread(w);
			t.start();
			reset();
		}
	}

	private void createWorkingImage() {
		log.info("createWorkingImage");
		// delete the old workingImage:
		UploaderFactory.getUploader().deleteFile(workingImage.getName());
		UploaderFactory.getUploader().doUpload(workingImage);
	}

	public BufferedImage resize(File file, int newW, int newH) throws IOException {
		log.info("ResizeFile");
		BufferedImage img = ImageIO.read(file);
		if (img != null) {
			int w = img.getWidth();
			int h = img.getHeight();
			BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
			Graphics2D g = dimg.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
			g.dispose();

			// delete the original File
			file.delete();
			return dimg;
		}
		return null;
	}

	private void reset() {
		imageUpLeft = null;
		imageUpRight = null;
		imageDownLeft = null;
		imageDownRight = null;
	}
}
