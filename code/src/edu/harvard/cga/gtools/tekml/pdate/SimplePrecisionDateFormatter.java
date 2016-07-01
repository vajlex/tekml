package edu.harvard.cga.gtools.tekml.pdate;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import static edu.harvard.cga.gtools.tekml.pdate.PrecisionDate.Resolution;


/**
 *  Use of the Adapter Pattern for the standard library SimpleDateFormat
 *  used to take advantage of its features and make it interoperable with PrecisionDates
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class SimplePrecisionDateFormatter implements PrecisionDateFormatter {
	
	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");

	private SimpleDateFormat sdfmt;

	public SimplePrecisionDateFormatter(String format) {
		try {
			sdfmt = new SimpleDateFormat(format);
		} catch(IllegalArgumentException e) {
			logger.error("Invalid date format: " + format);
			throw e;
		} catch(NullPointerException e) {
			logger.error("Null date format.");
			throw e;
		}		
	}
	
	/* 
	 * @see edu.harvard.cga.gtools.tekml.PrecisionDateFormatter#format(edu.harvard.cga.gtools.tekml.PrecisionDate)
	 */
	public String format(PrecisionDate pdate) {
		return sdfmt.format(pdate.getDate());
	}

	/**
	 *  By default (since using Java date formatting), the resolution is to the day
	 *  
	 * @see edu.harvard.cga.gtools.tekml.pdate.PrecisionDateFormatter#parse(java.lang.String)
	 */
	public PrecisionDate parse(String dateString) {
		PrecisionDate pdate = null;
		try {
			new PrecisionDate(sdfmt.parse(dateString, new ParsePosition(0)), Resolution.DAY);
		} catch (NullPointerException e) {
			logger.error("Date string to parse is null");
		}
		
		return pdate;
	}

	/* 
	 * @see edu.harvard.cga.gtools.tekml.PrecisionDateFormatter#parse(java.lang.String, edu.harvard.cga.gtools.tekml.PrecisionDate.Resolution)
	 */
	public PrecisionDate parse(String dateString, Resolution res) {
		PrecisionDate pdate = null;
		try {
			new PrecisionDate(sdfmt.parse(dateString, new ParsePosition(0)), res);
		} catch (NullPointerException e) {
			logger.error("Date string to parse is null");
		}
		
		return pdate;
	}

}
