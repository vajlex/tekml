package edu.harvard.cga.gtools.tekml.pdate;

import java.util.Date;


/**
 *  Date class that allows for precision set to year, month, or day,
 *  following specification for dates in the KML TimeSpan Element Specification
 *  see: http://code.google.com/apis/kml/documentation/kml_tags_21.html#timespan
 *  which in turn invokes the XML Schema Datatype specification for Dates
 *  see: http://www.w3.org/TR/xmlschema-2/#isoformats  Section D
 *  
 *  Additional requirements from this spec:
 *    year 0 is invalid ; actually this may change per ISO8601 docs, so ignore
 *    years can be represented as integers greater than 9999 and less than -9999
 *    dates before year 0 are represented with a leading negative sign (hyphen), with no spaces
 *  
 *  The java date/time library lacks the ability to set the resolution to year or month,
 *  for example '1835' is not a valid date except that it can be interpreted as '1/1/1835'.
 *  In the event that the resolution is to the day, then this class wraps a java Date object.
 *  
 *  This class also works around the "local century" feature in SimpleDateFormat for 2 digit years.
 *  1/12/96 will not be parsed to mean 1/12/1996
 *  
 *  Currently, this class only supports resolutions year, month, day.  
 *  Later versions may support hour, minute, sec etc. as well as TimesZones.
 *   
 *   Like Date, this class is immutable
 *   
 *   Will every date in the class loader use the same format??  probably not.
 *   DateFormats are not synchronized - make this class thread safe??
 * 
 * @author W. Hays  - whays at nearity.com
 * 
 */
public class PrecisionDate {
	
	//only used for formatting - which is synchronized
	static private final ISO8601PrecisionDateFormatter fmtr = new ISO8601PrecisionDateFormatter();
			
	public enum Resolution {YEAR, MONTH, DAY};  // in future:  HOUR, MIN, SEC, ...
		
	private Date date;
	private Resolution res;	
	
//constructors	
	/**
	 * create instance with the current date
	 */
	public PrecisionDate(Resolution res) {
		this.date = new Date();
		this.res = res;
	}
		
	/*
	 * 
	 */
	public PrecisionDate(Date d, Resolution res) {
		this.date = d;
		this.res = res;
	}

// getters
	public Resolution getResolution() {
		return res;
	}
	
	public Date getDate() {
		return date;
	}
	
	/**
	 * @return normal form at the given resolution
	 */
	public String toString() {
		return fmtr.format(this);  
	}

}
