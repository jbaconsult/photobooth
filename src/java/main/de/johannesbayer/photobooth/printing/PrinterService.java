package de.johannesbayer.photobooth.printing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.johannesbayer.photobooth.upload.Configuration;

public class PrinterService implements Printable, Runnable {

	private Log log = LogFactory.getLog(PrinterService.class);

	private Image image;

	public PrinterService(Image image) {
		this.image = image;
	}

	@Override
	public void run() {
		if (Boolean.parseBoolean(Configuration.INSTANCE().getProps().getProperty(Configuration.DO_PRINT))) {
			PrintService printService = PrintUtility
					.findPrintService(Configuration.INSTANCE().getProps().getProperty(Configuration.PRINTERNAME));
			if (printService == null) {
				log.info("WARNING: printService is null, will try the default printer");
			} else {
				log.info("Printer found: " + printService.getName());
			}
			PrinterJob printerjob = PrinterJob.getPrinterJob();
			PageFormat pageFormat = printerjob.defaultPage();
//			Paper paper = pageFormat.getPaper();
//			// double width =
//			// fromCMToPPI(Double.valueOf(Configuration.INSTANCE().getProps().getProperty(Configuration.PAPER_WIDTH)));
//			// double height =
//			// fromCMToPPI(Double.valueOf(Configuration.INSTANCE().getProps().getProperty(Configuration.PAPER_HEIGHT)));
//			double paperWidth = 6 * 72d;
//			double paperHeight = 4 * 72d;
//
//			paper.setImageableArea(0, 0, paperWidth, paperHeight);ace
			pageFormat.setOrientation(PageFormat.LANDSCAPE);
//			pageFormat.setPaper(paper);
			// pageFormat.setPaper(paper);
			// PageFormat validatePage = printerjob.validatePage(pageFormat);
			// log.info("Valid- " + dump(validatePage));

			printerjob.setPrintable(this, pageFormat); // Server was my
														// class's name, you
														// use yours.
			try {
				printerjob.setPrintService(printService); // Try setting the
															// printer
															// you want
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Error: No printer named '"
						+ Configuration.INSTANCE().getProps().getProperty(Configuration.PRINTERNAME)
						+ "', using default printer.");
				pageFormat = printerjob.defaultPage(); // Set the default
														// printer
														// instead.
			} catch (PrinterException exception) {
				System.err.println("Printing error: " + exception);
			}

			try {
				printerjob.print(); // Actual print command
			} catch (PrinterException exception) {
				System.err.println("Printing error: " + exception);
			}
		}
	}

	protected static String dump(PageFormat pf) {
		Paper paper = pf.getPaper();
		return dump(paper);
	}

	protected static String dump(Paper paper) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(paper.getWidth()).append("x").append(paper.getHeight()).append("/").append(paper.getImageableX())
				.append("x").append(paper.getImageableY()).append(" - ").append(paper.getImageableWidth()).append("x")
				.append(paper.getImageableHeight());
		return sb.toString();
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		log.info(pageIndex);
		int result = NO_SUCH_PAGE;
		if (pageIndex < 1) {
			Graphics2D g2d = (Graphics2D) graphics;
			log.info("[Print] " + dump(pageFormat));
			double width = pageFormat.getImageableWidth();
			double height = pageFormat.getImageableHeight();

			// log.info("Print Size = " + fromPPItoCM(width) + "x" +
			// fromPPItoCM(height));
			g2d.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());

//			Image scaled = null;
//			if (width > height) {
//				scaled = image.getScaledInstance((int) Math.round(width), -1, Image.SCALE_SMOOTH);
//			} else {
//				scaled = image.getScaledInstance(-1, (int) Math.round(height), Image.SCALE_SMOOTH);
//			}

			g2d.drawImage(image, 0, 0, (int) width, (int) height, null);
			result = PAGE_EXISTS;
		}
		return result;
	}
}
