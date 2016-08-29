package de.johannesbayer.photobooth;

import java.io.IOException;
import java.util.Properties;

import de.johannesbayer.photobooth.directory.DirectoryWatcher;
import de.johannesbayer.photobooth.directory.http.TranscendWatcher;
import de.johannesbayer.photobooth.printing.PrintScheduler;
import de.johannesbayer.photobooth.upload.Configuration;
import de.johannesbayer.photobooth.watcher.Watcher;

public class Combiner {

	public static void main(String[] args) throws IOException {

		// start printing server
		PrintScheduler scheduler = new PrintScheduler();
		Thread t = new Thread(scheduler);
		t.start();
		Properties props = Configuration.INSTANCE().getProps();
		Watcher watch = null;
		if ("Directory".equals(props.getProperty("workerMode", "Directory"))) {
			watch = new DirectoryWatcher(props.getProperty(Configuration.INPUTDIR), new ImageStore(
					props.getProperty(Configuration.INPUTDIR), props.getProperty(Configuration.OUTPUTDIR)));

		} else {
			watch = new TranscendWatcher(props.getProperty("httpServer"), props.getProperty("dcimFolder"),
					props.getProperty(Configuration.INPUTDIR), new ImageStore(props.getProperty(Configuration.INPUTDIR),
							props.getProperty(Configuration.OUTPUTDIR)));
		}

		Thread t2 = new Thread(watch);
		t2.start();
	}

}
