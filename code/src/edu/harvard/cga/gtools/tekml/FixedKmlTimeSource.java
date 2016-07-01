package edu.harvard.cga.gtools.tekml;

import edu.harvard.cga.gtools.tekml.pdate.PrecisionDate.Resolution;

/** 
 *   Implementation to provide way of using a fixed date values for a KmlRewriter
 *   
 *   Calls  default base class constructor because parsers aren't needed
 *   
 *   @author W. Hays - whays at nearity.com
 *   @since 1.0
 * 
 */
public class FixedKmlTimeSource extends KmlTimeSource {

	private KmlTimeData data;

	/**
	 *  This constructor assumes data is correctly formatted in ISO8601, no validation provided
	 * @param data
	 */
	public FixedKmlTimeSource(KmlTimeData data) {
		this.data = data; 
	}

	/**
	 *  For begin and end dates
	 * 
	 * @param inputFormat
	 * @param resolution
	 * @param beginSource
	 * @param endSource
	 * @throws NullPointerException - when date parse fails
	 */
	public FixedKmlTimeSource(String inputFormat, Resolution resolution, String beginSource, String endSource) 
	throws NullPointerException {  //date parse can fail
		super(inputFormat, resolution);
		data = new KmlTimeData();		
		data.setBeginTime(super.getKMLDate(beginSource).toString());
		data.setEndTime(super.getKMLDate(endSource).toString());
	}
	
	/**
	 *   For single timestamp date
	 * @param inputFormat
	 * @param resolution
	 * @param timestampSource
	 * @throws NullPointerException
	 */
	public FixedKmlTimeSource(String inputFormat, Resolution resolution, String timestampSource) 
	throws NullPointerException {  //date parse can fail
		super(inputFormat, resolution);
		data = new KmlTimeData();		
		data.setTimestamp(super.getKMLDate(timestampSource).toString());
	}
	
	/**
	 *   @param For this subclass of KmlTimeSource, this paramater is unnecesssary, so null is adequate
	 */
	public KmlTimeData getTimeData(Object obj) {
		return data;
	}

	
}
