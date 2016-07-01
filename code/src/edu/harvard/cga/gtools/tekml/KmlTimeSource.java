package edu.harvard.cga.gtools.tekml;

import org.apache.log4j.Logger;

import edu.harvard.cga.gtools.tekml.pdate.*;
import edu.harvard.cga.gtools.tekml.pdate.PrecisionDate.Resolution;

/**
 *  Implementations will, as delegates of a KmlRewriter, provide the time/date data object to
 *  the rewriter.  This is made as general as possible given the non-standard ways this might
 *  be implemented in various KML providers, such as with attribute tables embedded as CDATA
 *  sections in the <description> element.  It also enables other external methods.
 *  
 * @author W.Hays  (whays at nearity.com)
 *
 */
public abstract class KmlTimeSource {
	
	public static final String ISO8601FORMAT = "iso8601";
	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");
	
	protected Resolution res;	
	protected PrecisionDateFormatter parser = null; 

	
	
	public KmlTimeSource()  {}
	
	/**
	 * 
	 * @param inputFormat - either "iso8601" or conforming to the Java SimpleDateFormat scheme
	 * @param resolution - in the Enum { YEAR, MONTH, DAY }
	 */
	public KmlTimeSource(String inputFormat, Resolution resolution) {
		this.res = resolution;
		
		if (inputFormat.equals(ISO8601FORMAT)) {
			parser = new ISO8601PrecisionDateFormatter(true);  // choose lenient parse 
		} else {
			parser = new SimplePrecisionDateFormatter(inputFormat);  //will throw unchecked
		}		
	}
	
	/**
	 * 
	 * @param obj - source object can vary for various implementations
	 * @return KmlTimeData transfer object
	 */
	public abstract KmlTimeData getTimeData(Object obj) throws RewriterException;
	
	/**
	 * 
	 * @param source - String conforming to declared format 
	 * @return PrecisionDate reformatted from input; null on parse error
	 */
	protected PrecisionDate getKMLDate(String source) {
			return parser.parse(source, res);			
	}

}
