package de.johannesbayer.photobooth.upload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Configuration {

	private Log log = LogFactory.getLog(Configuration.class);
	
	public final static String CONNECTION_METHOD = "connectionMethod";
	public final static String WORKING_IMAGE = "workingImage";
	public final static String INPUTDIR = "inputDir";
	public final static String OUTPUTDIR = "outputDir";
	public final static String DELETE_INTERMEDIATE = "deleteIntermediate";
	public final static String TARGETIMAGE_WIDTH = "targetImageWidth";
	public final static String TARGETIMAGE_HEIGHT = "targetImageHeight";
	public final static String DO_PRINT = "doPrint";
	public final static String PRINTERNAME = "printername";
	public final static String PAPER_WIDTH = "paperWidth";
	public final static String PAPER_HEIGHT = "paperHeight";
	public final static String FTP_SERVER ="ftp_server";
	public final static String FTP_PORT ="ftp_port";
	public final static String FTP_USER = "ftp_user";
	public final static String FTP_PASS = "ftp_passwd";	
	
	private Properties props;
	private static Configuration INSTANCE;
	//private String location = "combiner.properties";

	private Configuration() {

		setProps(new Properties());
		InputStreamReader in = null;

		try {
			try {
				in = new InputStreamReader(new FileInputStream(System.getProperty("configFile")), "UTF-8");
				props.load(in);
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch (IOException ex) {
						log.error(ex);
					}
				}
			}

		} catch (IOException ex) {
			log.error(ex);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}

	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public static Configuration INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new Configuration();
		}
		return INSTANCE;
	}

}
