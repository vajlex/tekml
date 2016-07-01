package edu.harvard.cga.gtools.tekml;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FilenameUtils;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import edu.harvard.cga.gtools.tekml.pdate.PrecisionDate.Resolution;

/**
 * 
 *  To be subclassed by a commandline app and possibly a gui app
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public abstract class BaseApp {
	
	public static String APP_NAME = "teKML";

	protected static final String USER_DIR = System.getProperty("user.dir") + File.separator;
	protected static final String TEKML_PROPS ="/teKML.properties";  ///config/ 
	protected static final String LOG_PROPS = "/log4j.properties";
	
	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");
	
	protected static PropertiesConfiguration config;
	protected static PropertiesConfiguration appProperties;
	protected static Properties logProperties;
	
	public static final String CFG_KML_GENERATOR = "kml.generator";
	public static final String CFG_KML_GENERATOR_OGR2OGR = "ogr2ogr"; //value
	public static final String CFG_KML_GENERATOR_AVEXP = "avexp";     //value
	public static final String CFG_KML_GENERATOR_GOOGLE_EARTH = "googleearth";     //value
	public static final String CFG_KML_GENERATOR_OTHER = "other";  //value
	public static final String CFG_OGR2OGR_EXEC = "ogr2ogr.exec"; // default value; followed by <output> <input> 
	
	public static final String CFG_DATA_TRANSFORM = "geodata.transform";  //boolean
	
	public static final String CFG_BASE_WORK_PATH = "base.path";
	public static final String CFG_OUTPUT_FILE_TAG = "kml.filename.tag";
	public static final String CFG_OUTPUT_FILE_TAG_DEFAULT = "-TE";  //value
	
	public static final String CFG_TIME_SOURCE = "kml.time.source";
	public static final String CFG_TIME_SOURCE_FIXED = "fixed";  //value
	public static final String CFG_TIME_SOURCE_TABLE = "attribute.table";  //value
	public static final String CFG_TIME_SOURCE_FORMAT = "kml.time.source.format";
	public static final String CFG_TIME_SOURCE_FORMAT_ISO8601 = "iso8601";             //value
	
	public static final String CFG_TIME_RESOLUTION = "kml.time.source.resolution";  //values from enum
	
	public static final String CFG_TIME_FIXED_BEGIN = "kml.time.fixed.begin";
	public static final String CFG_TIME_FIXED_END = "kml.time.fixed.end";
	public static final String CFG_TIME_FIXED_TIMESTAMP = "kml.time.fixed.timestamp";
	
	public static final String CFG_ATTRIB_LABEL_NAME = "attrib.table.label.name";  
	public static final String CFG_ATTRIB_LABEL_BEGIN = "attrib.table.label.begin";  //list
	public static final String CFG_ATTRIB_LABEL_END = "attrib.table.label.end";  //list
	public static final String CFG_ATTRIB_LABEL_TIMESTAMP = "attrib.table.label.timestamp";  //list
	
	public static final String CFG_EMPTY_FOLDER = "kml.empty.folder.name";

	public static final String CFG_KML_COMMENT = "kml.comment";

	//from teKML props	
	public static final String AVEXP_TABLE_ROW_SEPARATOR = "avexp.table.row.separator";
	public static final String AVEXP_TABLE_ROW_PATTERN = "avexp.table.row.pattern";
	
	public static final String GOOGLE_EARTH_TABLE_ROW_SEPARATOR = "ge.table.row.separator";
	public static final String GOOGLE_EARTH_TABLE_ROW_PATTERN = "ge.table.row.pattern";
	
	public static final String OGR2OGR_TABLE_ROW_SEPARATOR = "ogr2ogr.table.row.separator";
	public static final String OGR2OGR_TABLE_ROW_PATTERN = "ogr2ogr.table.row.pattern";
	
	public static final String GENERATOR_TABLE_ROW_SEPARATOR = "generator.table.row.separator";
	public static final String GENERATOR_TABLE_ROW_PATTERN = "generator.table.row.pattern";
	
	public static final String KML_NAMESPACE = "kml.namespace";
	
	protected OgrTransformer transformer;
	protected KmlRewriter rewriter;
	protected boolean transform = false;
	
	/**
	 *  Base class for a teKML application
	 *  Provides methods for getting configuration settings from files and setting up logging
	 */
	public BaseApp() {
		
		transformer = new OgrTransformer();
		rewriter = new StaxKmlRewriter();
		logger.debug("rewriter is of type: " + rewriter.getClass());
	} 
	
	/**
	 *  Load log properties from file on the classpath, specifically in the config directory
	 */
	protected void initializeLogging()  {
	    logProperties = new Properties();

	    try {
	        logProperties.load(BaseApp.class.getResourceAsStream(LOG_PROPS));
	        PropertyConfigurator.configure(logProperties);
	    	logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");

	        logger.debug("Logging initialized.");
	    } catch(IOException e) {
	        throw new RuntimeException("Unable to load logging property " + LOG_PROPS);
	    } catch(NullPointerException e) {
	        throw new RuntimeException("Unable to find source: " + LOG_PROPS);
	    }
	}
	
	/**
	 *  Load teKML properties as resource
	 */
	protected void loadAppProperties() 
	throws ConfigurationException, FileNotFoundException {
		URL url = BaseApp.class.getResource(TEKML_PROPS);
		if (url != null) {
			appProperties = new PropertiesConfiguration(url);
			appProperties.setThrowExceptionOnMissing(true);
		} else {
			throw new FileNotFoundException(TEKML_PROPS + " not found.");					
		}				
		
		logger.debug("teKML application properties loaded.");
				
	}

	
	/**
	 * 
	 * @param configFilePath - can be either absolute or relative.
	 *                         If relative, can be a class resource on the classpath
	 *                         or relative to the current user directory 
	 * @throws ConfigurationException
	 * @throws FileNotFoundException
	 */
	protected void loadConfigProperties(String configFile) 
	throws ConfigurationException, FileNotFoundException {
		
		String source = null;
		File f = new File(configFile);
		if (!f.exists()) { 
			f = new File(USER_DIR + configFile);
			if (!f.exists()) {
				URL url = BaseApp.class.getResource("/" + configFile);
				if (url != null) {
					source = "from a class resource";
					f = new File(url.getFile());
				} else {
					throw new FileNotFoundException(configFile + " not found.");					
				}				
			} else {
				source = "from user directory " + USER_DIR;
			}
		} else {
			source = "from absolute location.";
		}
		
		config = new PropertiesConfiguration(f);
//		config.setThrowExceptionOnMissing(true);
		logger.debug("Configuration properties " + configFile + " loaded " + source);
	}
	
	protected void configureTransformer() throws TransformException {
		try {
			transformer.setExecCommandBase(config.getString(CFG_OGR2OGR_EXEC));
		} catch(NoSuchElementException e) {
			logger.error("Configuration properties error: " + e);
			throw new RuntimeException(e);
		}
		logger.debug("Rewriter configuration completed.");		
	}
	
	/**
	 *   Configure rewriter
	 * @throws RewriterException
	 */
	protected void configureRewriter() throws RewriterException {
		
		try {
			String generator = config.getString(CFG_KML_GENERATOR);
			
			transform = generator.equals(CFG_KML_GENERATOR_OGR2OGR) 
			            && config.getBoolean(CFG_DATA_TRANSFORM, false);
										
			rewriter.setProcessedMarker(BaseApp.APP_NAME);  
			
			rewriter.setUserCommentTag(config.getString(CFG_KML_COMMENT, "teKML"));
			
			rewriter.setKmlNamespace(appProperties.getString(KML_NAMESPACE));
			
			rewriter.setFolderToEmpty(config.getString(CFG_EMPTY_FOLDER));
			
			String timeFormat = config.getString(CFG_TIME_SOURCE_FORMAT, CFG_TIME_SOURCE_FORMAT_ISO8601);
			
			String resval = config.getString(CFG_TIME_RESOLUTION);
			Resolution resolution = null;
			if (resval == null) {
				resolution = Resolution.YEAR;  //better default??
			} else if (resval.equals("year")) {
				resolution = Resolution.YEAR;
			} else if (resval.equals("month")) {
				resolution = Resolution.MONTH;
			} else if (resval.equals("day")) {
				resolution = Resolution.DAY;
			} else {
				throw new RewriterException("Invalid configuration for time resolution: " + resval);
			}
			
			//attribute table  factory
			AttributeTableFactory atFactory = new AttributeTableFactory();
			String nameLabel = config.getString(CFG_ATTRIB_LABEL_NAME);  //default??
			logger.debug("AttributeTable name label: ");
			atFactory.setNameLabel(nameLabel);			
			rewriter.setAttributeTableFactory(atFactory);			

			if (generator.equals(CFG_KML_GENERATOR_AVEXP)) {
				atFactory.setRowSeparator(appProperties.getString(AVEXP_TABLE_ROW_SEPARATOR));
				atFactory.setRowPattern(appProperties.getString(AVEXP_TABLE_ROW_PATTERN));
			} else if (generator.equals(CFG_KML_GENERATOR_OGR2OGR)) {
				atFactory.setRowSeparator(appProperties.getString(OGR2OGR_TABLE_ROW_SEPARATOR));
				atFactory.setRowPattern(appProperties.getString(OGR2OGR_TABLE_ROW_PATTERN));			
			} else if (generator.equals(CFG_KML_GENERATOR_GOOGLE_EARTH)) {
				atFactory.setRowSeparator(appProperties.getString(GOOGLE_EARTH_TABLE_ROW_SEPARATOR));
				atFactory.setRowPattern(appProperties.getString(GOOGLE_EARTH_TABLE_ROW_PATTERN));
			} else if (generator.equals(CFG_KML_GENERATOR_OTHER)) {
				atFactory.setRowSeparator(appProperties.getString(GENERATOR_TABLE_ROW_SEPARATOR));
				atFactory.setRowPattern(appProperties.getString(GENERATOR_TABLE_ROW_PATTERN));
			}
			
			String timeSourceType = config.getString(CFG_TIME_SOURCE, CFG_TIME_SOURCE_TABLE);
			if (timeSourceType.equals(CFG_TIME_SOURCE_FIXED)) {
				
				FixedKmlTimeSource ts = null;
				try {
					String beginTime = config.getString(CFG_TIME_FIXED_BEGIN);
					if ((beginTime != null) && (beginTime.length() > 0)) {
						String endTime = config.getString(CFG_TIME_FIXED_END);
						if ((endTime != null) && (endTime.length() > 0)) {
							ts = new FixedKmlTimeSource("iso8601", resolution, beginTime, endTime);
						}
					} else {
						String timestamp = config.getString(CFG_TIME_FIXED_TIMESTAMP);
						if ((timestamp != null) && (timestamp.length() > 0)) {
							ts = new FixedKmlTimeSource(timeFormat, resolution, timestamp);
						}
					}
				} catch(NullPointerException e) {
					throw new RewriterException("Date parse failed when creating FixedKmlTimeSource");
				}
				rewriter.setKmlTimeSource(ts);
				
			} else if (timeSourceType.equals(CFG_TIME_SOURCE_TABLE)) {
				
				rewriter.setKmlTimeSource(new AttributeTableTimeSource(timeFormat, resolution));
				
				String[] beginAr = config.getStringArray(CFG_ATTRIB_LABEL_BEGIN);
				logger.debug("AttributeTable begin labels: ");
				for (String s : beginAr) {
					logger.debug("     " + s);
				}			
				atFactory.setBeginList(new LabelList(beginAr));
				
				String[] endAr = config.getStringArray(CFG_ATTRIB_LABEL_END);
				logger.debug("AttributeTable end labels: ");
				for (String s : endAr) {
					logger.debug("     " + s);
				}			
				atFactory.setEndList(new LabelList(endAr));
				
				String[] timestampAr = config.getStringArray(CFG_ATTRIB_LABEL_TIMESTAMP);
				logger.debug("AttributeTable timestamp labels: ");
				for (String s : timestampAr) {
					logger.debug("     " + s);
				}			
				atFactory.setTimestampList(new LabelList(timestampAr));
								
			} else {
				throw new RewriterException("Time data source not specified in configuration file");
			}
			
			
		} catch(NoSuchElementException e) {
			logger.error("Configuration properties error: " + e);
			throw new RuntimeException(e);
		}
		logger.debug("Rewriter configuration completed.");
		
	}
	
	

	/**
	 * 	Process the KML using the ogr transformer and the rewriter
	 *  To be called by a subclass of BaseApp
	 * 
	 * @param far - Array of KML files to be rewritten
	 * @param destDir - directory location of rewritten file
	 * @throws RewriterException
	 * @throws TransformException
	 */
	protected void run(File[] far, File destDir) throws RewriterException, TransformException {

		logger.info("Count of files to be processed: " + far.length);

		for (File f : far) {
			
			File destFile = new File(destDir, getOutputName(f.getName()));
			logger.info("     Processing file: " + f.getName());
			
			if (transform) { 
				String tempPath = FilenameUtils.concat(destDir.getAbsolutePath(), "tekml-temp.kml");
				File temp = transformer.transform(f.getAbsolutePath(), tempPath);  
				logger.info("Rewriting kml file: " + f.getName()  + " as " + destFile.getName());
				try {
					rewriter.rewrite(temp, destFile); 
				}catch(RewriterException e) {
					throw e;
				} finally {
					if (temp.delete()) {
						logger.debug("ogr transformed filed deleted.");
					} else {
						logger.debug("Unable to delete ogr transformed file " + destFile);						
					} 
				}
			} else {
				logger.info("Rewriting kml file: " + f.getName() + " as " + destFile.getName());
				rewriter.rewrite(f, destFile);
			}
		}
		
		logger.debug("Finished processing files - total: " + far.length);
	}
	
	private String getOutputName(String srcName) {
		String base = FilenameUtils.getBaseName(srcName);	
		String tag = config.getString(BaseApp.CFG_OUTPUT_FILE_TAG);
		if ((tag == null) || (tag.length() == 0)) {
			tag = BaseApp.CFG_OUTPUT_FILE_TAG_DEFAULT;
		}
		return base + tag + ".kml";		
	}

} //end class
