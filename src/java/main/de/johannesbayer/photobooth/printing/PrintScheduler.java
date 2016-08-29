package de.johannesbayer.photobooth.printing;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PrintScheduler implements Runnable {

	private static List<PrintJob> jobs = Collections.synchronizedList(new LinkedList<PrintJob>());
	private Log log = LogFactory.getLog(PrintScheduler.class);

	public PrintScheduler() {
		log.info("Starting print scheduler");
	}

	@Override
	public void run() {
		while (true) {
			if (jobs.size() > 0) {
				log.info("current job queue size: " + jobs.size());
				PrintJob job = jobs.remove(0);
				PrinterService ps = new PrinterService(job.getImage());
				Thread t = new Thread(ps);
				t.start();
			}
			// we sleep a bit
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}
	}

	public static void addJob(PrintJob job) {
		jobs.add(job);
	}
}
