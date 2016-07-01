package edu.harvard.cga.gtools.tekml.pdate;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import static edu.harvard.cga.gtools.tekml.pdate.PrecisionDate.Resolution;


/**
 *  Formatter for dates adhering to the ISO8601 Standard
 *  as codified by XMLSchema
 *  
 *  This implementation is incomplete - only Dates, not times, are handled
 *  Unlike other java implementations (e.g., see joda.time) the following are salient
 *    date resolution for year, month, and day
 *    the leading hyphen for dates BCE
 *    
 *  With Sun JVM, max values for years BCE  between 292270000 and 292260000
 *   which is not the same as the max for CE dates returned by the max value call
 *   there appears to be an error since min value returns "1"
 *  
 * 
 *  Follows java.text.DateFormatter patterns:  returns null on parse error
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class ISO8601PrecisionDateFormatter implements PrecisionDateFormatter {

	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");


	private static final int GROUP_HYPHEN = 1;
	private static final int GROUP_YEAR = 2;
	private static final int GROUP_MONTH = 4;
	private static final int GROUP_DAY = 6;
		
	/*   
	 *  Strict parsing with minimum 4 digits for year
	 */	
	private static String regex = "^([\\-])?(\\d{4,})([\\-](\\d{2})([\\-](\\d{2}))?)?$";
	/*   
	 *  Allows input dates of 1 or more digits, without leading zeros
	 */	
	private static String regexLenient = "^([\\-])?(\\d{1,})([\\-](\\d{2})([\\-](\\d{2}))?)?$";
	private static Pattern isopattern;
	
	static {
	}
	
	private Calendar cal = new GregorianCalendar();
	private NumberFormat nfYear;
	private NumberFormat nfMonthDay;
	
	/**
	 *  default constructor with lenient parsing
	 */
	public ISO8601PrecisionDateFormatter() {
		this(true);
	}

	/**
	 * 
	 * @param lenientParse - Parsing allowing leading zeroes for historical years pre-1000
	 */
	public ISO8601PrecisionDateFormatter(boolean lenientParse) {
		cal.setLenient(false);  //don't want Java date handling to be lenient!!
		nfYear = NumberFormat.getInstance();
		nfYear.setMinimumIntegerDigits(4);  //required by ISO8601 spec
		nfYear.setGroupingUsed(false);
		nfMonthDay = NumberFormat.getInstance();
		nfMonthDay.setMinimumIntegerDigits(2);
		nfMonthDay.setGroupingUsed(false);
		if (lenientParse) {
			isopattern = Pattern.compile(regexLenient);	
		} else {
			isopattern = Pattern.compile(regex);	
		}
	}


// implementation of interface methods	
	
	/** 
	 * @return formatted PrecisionDate or null if an error
	 * format to the resolution of the pdate
	 */
	public String format(PrecisionDate pdate) {
		
		synchronized(cal) {
			StringBuffer sb = new StringBuffer();  // threadsafe
			
			cal.setTimeInMillis(pdate.getDate().getTime());
			if (cal.get(GregorianCalendar.ERA) == GregorianCalendar.BC) {
				sb.append("-");
			}
			
			sb.append(nfYear.format(cal.get(Calendar.YEAR)));
			
			Resolution res = pdate.getResolution();
						
			if ((res == Resolution.MONTH) || (res == Resolution.DAY)) {
				sb.append("-").append(nfMonthDay.format(cal.get(Calendar.MONTH) + 1));
			
				if (res == Resolution.DAY) {
					sb.append("-").append(nfMonthDay.format(cal.get(Calendar.DAY_OF_MONTH)));
				}
			}
			
			return sb.toString();
		}
	}

	/** 
	 *  Parses date formatted as ISO8601
	 * 
	 * @param isodate - string formatted as ISO8601 date
	 * @return formatted PrecisionDate or null if an error
	 */
	public PrecisionDate parse(String isodate) {
		
		if (isodate == null) return null;
		 
		try {
			Matcher matcher = isopattern.matcher(isodate);
			
			if (!matcher.matches()) { 
				logger.error("Invalid isodate format: " + isodate);
				return null; 			    
			}
			Resolution res = Resolution.YEAR; //default
			int yr = 0;
			int mo = 1;
			int day = 1;
			
			boolean ce = (matcher.group(GROUP_HYPHEN) == null);  //common era or AD
			
			String syear = matcher.group(GROUP_YEAR);
			yr = Integer.parseInt(syear);
			
			String smonth = matcher.group(GROUP_MONTH);
			if (smonth != null) {
				mo = Integer.parseInt(smonth);
				String sday = matcher.group(GROUP_DAY);
				if (sday == null) {
					res = Resolution.MONTH;

				} else {
					day = Integer.parseInt(sday);
					res = Resolution.DAY;					
				}
			}
			
			Date date = createDate(yr, mo, day, ce);
			return new PrecisionDate(date, res);
			
		} catch(IllegalStateException e) {
			logger.error("iso8601 date parse error: " + e);
		} catch(IndexOutOfBoundsException e) {
			logger.error("iso8601 date parse error: " + e);
		} catch(IllegalArgumentException e) {
			logger.error("iso8601 date parse error with bad argument: " + e);
		} catch (Exception e) {
			logger.error("iso8601 date parse error: " + e);
//			e.printStackTrace();
		}
		return null;
	}

	/** 
	 * 
	 * @param isodate - String formatted as ISO8601 date
	 * @param res  Resolution of date
	 * @return A PrecisionDate parsed from the string. In case of error, returns null.
	 */
	public PrecisionDate parse(String isodate, Resolution res) {
		if (isodate == null) return null;
		 
		try {
			Matcher matcher = isopattern.matcher(isodate);
			
			if (!matcher.matches()) { 
				logger.error("Invalid isodate format: " + isodate);
				return null; 			    
			}
			
			int yr = 0;
			int mo = 1;
			int day = 1;
			
			boolean ce = (matcher.group(GROUP_HYPHEN) == null);  //common era or AD
			
			String syear = matcher.group(GROUP_YEAR);
			yr = Integer.parseInt(syear);
			
			if ((res == Resolution.MONTH) || (res == Resolution.DAY)) { 
				String smonth = matcher.group(GROUP_MONTH);
				if (smonth == null) {
					logger.error("ISO8601 parse error: no month specified: " + isodate);
					return null;
				} else {
					mo = Integer.parseInt(smonth);
					
					if (res == Resolution.DAY) {
						String sday = matcher.group(GROUP_DAY);
						if (sday == null) {
							logger.error("ISO8601 parse error: no day specified: " + isodate);
							return null;
						} else {
							day = Integer.parseInt(sday);
						}
					}
				}
			}
			
			Date date = createDate(yr, mo, day, ce);
			return new PrecisionDate(date, res);
			
		} catch(IllegalStateException e) {
			logger.error("iso8601 date parse error: " + e);
		} catch(IndexOutOfBoundsException e) {
			logger.error("iso8601 date parse error: " + e);
		} catch(IllegalArgumentException e) {
			logger.error("iso8601 date parse error with bad argument: " + e);
		} catch (Exception e) {
			logger.error("iso8601 date parse error: " + e);
		}
		return null;
	}
	
//private methods
	/*
	 *   resolution doesn't matter here - need to give a good default value for higher resolutions
	 */
	private Date createDate(int yr, int mo, int day, boolean ce) {
		synchronized(cal) { 
			cal.set(Calendar.YEAR, yr); 
			cal.set(Calendar.MONTH, mo - 1);
			cal.set(Calendar.DAY_OF_MONTH, day);
			cal.set(Calendar.ERA, ce ? GregorianCalendar.AD : GregorianCalendar.BC);
			return cal.getTime();
		}
	}
}
