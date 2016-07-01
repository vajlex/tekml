package edu.harvard.cga.gtools.tekml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

public class Utils {
	
	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");	

	public static File[] getSourceFiles(String srcArg, String basePath) 
	throws IOException, FileNotFoundException {
		
		//get absolute path
		
		srcArg = FilenameUtils.concat(basePath, srcArg);		
		logger.debug("Path for src files: " + srcArg);
		
		//verify directory path exists
		
		String dirPath = FilenameUtils.getFullPathNoEndSeparator(srcArg);
		File dir = new File(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IOException("Source file directory doesn't exist: " + dirPath);
		}
		
		String filename = FilenameUtils.getName(srcArg);
		
		//assume filename is a filter
		FileFilter fileFilter = new WildcardFileFilter(filename);
		
		File[] far = dir.listFiles(fileFilter);
		if (far.length == 0) {
			logger.error("No files found in directory: " + dirPath + " matching " + filename);
			throw new FileNotFoundException();
		}
		
		for (File f : far) {
			logger.debug("   " + f.getAbsolutePath());
		}
		
		return  far;
		
	}
	
	public static File getDestinationDir(String destArg, String basePath) 
	throws IOException, FileNotFoundException {
		
		destArg = FilenameUtils.concat(basePath, destArg);		
		logger.debug("Absolute path for dest files: " + destArg);
		
		//verify directory path exists
		
		File dir = new File(destArg);
		if (!dir.exists() || !dir.isDirectory()) {
			throw new FileNotFoundException("Source file directory doesn't exist: " + destArg);
		}
			
		return dir;
	}
	
	

}
