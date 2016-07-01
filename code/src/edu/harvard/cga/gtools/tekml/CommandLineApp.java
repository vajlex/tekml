package edu.harvard.cga.gtools.tekml;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

//import edu.harvard.cga.gtools.tekml.pdate.PrecisionDate.Resolution;

/**
 * Commandline application to
 *    <ul> 
 *    <li>conversion of geographic datafiles to KML using ogr2ogr</li>
 *    <li>write timespan or timestamp values to the new KML time fields</li>
 *    
 *    </ul>
 *    
 *    The Configuration of application runs is based on a configuration file  
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class CommandLineApp extends BaseApp {
	
	/**
	 * 
	 */
	public CommandLineApp() {
		logger.debug("teKML CommandLine App initialized");
		logger.info("teKML application initiated");
		logger.info("Harvard University - Center for Geospatial Analysis");
		
	}
	
	/**
	 *  Entry point for application
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLineApp app = new CommandLineApp();
		
		CommandLine cl = null;
		File[] sourceFiles = null;
		File destDir = null;
		Options opt = null;
		
        try {
        	app.initializeLogging();
        	app.loadAppProperties();
        	
        	
            opt = new Options();

            opt.addOption("h", false, "Print help for this application");
            opt.addOption("c", true, "Configuration file");
            opt.addOption("src", true, "Geographic datafiles to process");
            opt.addOption("dest", true, "Directory location of the converted files");

            BasicParser parser = new BasicParser();
            cl = parser.parse(opt, args);

            if ( cl.hasOption('h') ) {
                app.showHelp(opt);
                System.exit(2);
            } 
            
            logger.debug("Processing command line.");
            
            
        	String configPath = cl.getOptionValue("c");
        	logger.debug("Configuration file: " + configPath);
        	app.loadConfigProperties(configPath);
        	
            String basePath = config.getString(CFG_BASE_WORK_PATH);
            logger.debug("Base path from config: " + basePath);
        	
        	String srcArg = cl.getOptionValue("src");
        	logger.debug("Source file arg: " + srcArg);       	
        	sourceFiles = Utils.getSourceFiles(srcArg, basePath);
        	        	
        	String destPath = cl.getOptionValue("dest", "");
        	logger.debug("Destination directory arg: " + destPath);
        	destDir = Utils.getDestinationDir(destPath, basePath);        	       	
        	
        } catch (ParseException e) {
        	System.out.println("Commandline parse exception.");
        	System.out.println("Application terminated.");
        	app.showHelp(opt);
        	System.exit(-1);
        } catch(Exception e) {
        	System.out.println("Configuration exception.");
        	System.out.println("Application terminated.");
        	app.showHelp(opt);
//        	e.printStackTrace();
        	System.exit(-1);
        }
        
        try {   
        	app.configureTransformer();
        	app.configureRewriter();
        	app.run(sourceFiles, destDir);       	       	
        } catch(RewriterException e) {       	
        	logger.error("Processing incomplete. " + e);
        	e.printStackTrace();
        } catch(TransformException e) {
        	logger.error("Processing incomplete due to ogr transform error. " + e);        	
		} catch(RuntimeException e) {
			logger.error("Processing incomplete. " + e);
			//e.printStackTrace();
        }
        
        logger.info("teKML application completed ");
	}
	
	private void showHelp(Options opt) {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("tekml ", opt);   //ref to run script		
	}
	
}
