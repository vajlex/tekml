package edu.harvard.cga.gtools.tekml;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import java.util.regex.PatternSyntaxException;


import org.apache.log4j.Logger;

import edu.harvard.cga.gtools.tekml.pdate.PrecisionDate;

public abstract class KmlRewriter {
	
	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");
	
	protected KmlTimeSource timeSource;	
	protected AttributeTableFactory attributeTableFactory;
	
	protected String kmlNamespace = "";
	protected String processedMarker = null;  //identifying string to avoid reprocessing same input
	protected String userCommentTag = null;
	protected String folderToEmpty = null;
	
	
//public methods
	public abstract void rewrite(File kmlSource, File kmlDest) throws RewriterException; 		
	public abstract void rewrite(InputStream kmlSource, OutputStream kmlDest) throws RewriterException; 
		
	
//setters
	public void setKmlTimeSource(KmlTimeSource ts) {
		this.timeSource = ts;
	}
	
	public KmlTimeSource getKmlTimeSource() {
		return timeSource;
	}
	public void setAttributeTableFactory(AttributeTableFactory factory) {
		this.attributeTableFactory = factory;
	}
	
	public AttributeTableFactory getAttributeTableFactory() {
		return attributeTableFactory;
	}
	public String getKmlNamespace() {
		return kmlNamespace;
	}

	public void setKmlNamespace(String kmlNamespace) {
		this.kmlNamespace = kmlNamespace;
	}

	public String getProcessedMarker() {
		return processedMarker;
	}

	public void setProcessedMarker(String processedMarker) {
		this.processedMarker = processedMarker;
	}

	public String getUserCommentTag() {
		return userCommentTag;
	}

	public void setUserCommentTag(String userCommentTag) {
		this.userCommentTag = userCommentTag;
	}
	public String getFolderToEmpty() {
		return folderToEmpty;
	}
	public void setFolderToEmpty(String folderToEmpty) {
//		logger.debug("Folder to be emptied: " + folderToEmpty);
		this.folderToEmpty = folderToEmpty;
	}
	
	/**
	 *   
	 * @param base - KML namespaceURI
	 * @param toCmp - KML namespaceURI to compare
	 * @return whether the namespaceURI to compare is at a higher version
	 */
	protected boolean isMoreCurrentKMLSpec(String base, String toCmp) {
		
		if ((base == null) || "".equals(base)) return true;
		if ((toCmp == null) || "".equals(toCmp)) return false;
		
		Pattern p = Pattern.compile("^.+(\\d+\\.\\d+)$");  //http://earth.google.com/kml/
		
		try {
			Matcher mBase = p.matcher(base); 
			if (!mBase.matches()) {
				logger.debug("Unable to read KML version from namespace: " + base);
				return true;
			}
			Matcher mCmp = p.matcher(toCmp);
			if (!mCmp.matches()) {
				logger.debug("Unable to read KML version from config namespace: " + toCmp);
				return false;
			}
			
			logger.debug("source KML version = " + mBase.group(1));
			logger.debug("config KML version = " + mCmp.group(1));
			
			float flBase = Float.parseFloat(mBase.group(1));
			float flCmp = Float.parseFloat(mCmp.group(1));
			
			return (flBase < flCmp );
			
		} catch(NumberFormatException e) {
			logger.warn("Unable to parse namespace version");
		}
		
		return true;
	}

	/**
	 *  Provides a way of marking processed files to prevent them from
	 *  being process a second time
	 *  
	 *  Example:  <code>Processed by teKML v. 1.0 on 02-02-2008T13:04:05 - user specified text goes here</code>
	 * @return comment string
	 */
	protected String getComment() {
		PrecisionDate pdate = new PrecisionDate(new Date(), PrecisionDate.Resolution.DAY);
		
		StringBuilder sb = new StringBuilder("Created by: ");
		sb.append(processedMarker).append(" on ").append(pdate.toString());
		
		if (userCommentTag != null) {
			sb.append(" -- ").append(userCommentTag);
		}
		
		return sb.toString();
	}

}
