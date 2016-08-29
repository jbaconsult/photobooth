package de.johannesbayer.photobooth.printing;

import java.awt.Image;

public class PrintJob {

	private Image image;

	public PrintJob(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}

}
