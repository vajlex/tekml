package edu.harvard.cga.gtools.tekml;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class OgrTransformer {
	
	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");	

	private String execCommandBase = "ogr2ogr -f KML ";  //default

	public OgrTransformer() {};
	
	public OgrTransformer(String execCommand) {
		this.execCommandBase = execCommand;
		
	}
	
	public File transform(String sourceFullPath, String destFullPath) throws TransformException {
		
//		String ogr2ogrExec = config.getString(CFG_OGR2OGR_EXEC, );

//		if (execCommandBase == null) {
//			throw new NullPointerException("ogr2ogr execute command not found in teKML.properties file");
//		}
		
		String cmd = execCommandBase + " " + destFullPath + " " + sourceFullPath;
		
		File fout = null;
		Process p = null;
		
		try {
			logger.info("Executing ogr2ogr with command >>" + cmd + "<<");
			p = Runtime.getRuntime().exec(cmd);
			
			p.waitFor();  //need to wait so can read file
			
			fout = new File(destFullPath);
			if (!fout.exists()) {
				throw new IOException("Transformed file not created: " + destFullPath );
			}
			logger.debug("Transformed file created: " + destFullPath);
			
		} catch(IOException e) {
			logger.error("IO error with ogr2ogr file transform");
			throw new TransformException(e);
		} catch(InterruptedException e) {
			logger.error("Problem with runtime execution of transform: " + e);
			throw new TransformException(e);
		} catch(SecurityException e) {
			logger.error("File system permissions problem with runtime execution of transform: " + e);
			throw new TransformException(e);
		} catch(NullPointerException e) {
			logger.error("Problem with runtime execution of transform: " + e);
			throw new TransformException(e);
		} catch(IllegalArgumentException e) {
			logger.error("Problem with runtime execution of transform: " + e);
			throw new TransformException(e);
		} finally {
			p.destroy();			
		}
		
		return fout;		
	}

	public String getExecCommandBase() {
		return execCommandBase;
	}

	public void setExecCommandBase(String execCommandBase) {
		this.execCommandBase = execCommandBase;
	}

	
	

}
