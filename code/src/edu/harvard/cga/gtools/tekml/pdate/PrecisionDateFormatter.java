package edu.harvard.cga.gtools.tekml.pdate;


/**
 * 	This is not a subclass of java.text.DateFormat
 *  though attempts will be made to reuse elements there
 *  
 * 
 * 
 * @author W.Hays  (whays at nearity.com)
 * 
 * @see PrecisionDate
 *
 */
public interface PrecisionDateFormatter {
	
	public PrecisionDate parse(String dateString);
	
	public PrecisionDate parse(String dateString, PrecisionDate.Resolution res);
	
	public String format(PrecisionDate pdate);

}
