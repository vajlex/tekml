package edu.harvard.cga.gtools.tekml;

import edu.harvard.cga.gtools.tekml.pdate.*;
import edu.harvard.cga.gtools.tekml.pdate.PrecisionDate.Resolution;

public class AttributeTableTimeSource extends KmlTimeSource {
		
	public AttributeTableTimeSource(String inputFormat, Resolution resolution) {
		super(inputFormat, resolution);		
	}


	/**
	 *  Implements interface
	 *  
	 *  Renders KmlTimeData element from the AttributeTable data transfer object
	 *  
	 *  Implements business rule: If only one of begin or end exists, then 
	 * 
	 * 
	 * 
	 *  @param Attribute table from KML with expected time elements
	 *  
	 */
	public KmlTimeData getTimeData(Object obj) throws RewriterException { 
		
		AttributeTable at = null;
		
		try {
			at = (AttributeTable) obj;  
		} catch (ClassCastException e) {
			logger.error("Attribute table is not of the correct class");
			throw new RewriterException(e);
		}
		
		logger.debug("Attribute table: " + at.toString());
	
		KmlTimeData data = new KmlTimeData();
			
		String beginValue = at.getBeginTime();
		PrecisionDate begin = null;
		if (beginValue != null) {
			begin = super.getKMLDate(beginValue);
			if (begin != null) {
				data.setBeginTime(begin.toString());
				logger.debug("Parsed begin value = " + begin.toString());
			} else {
				throw new RewriterException("Error parsing begin time value: " + beginValue);
			}
		}
		
		String endValue = at.getEndTime();
		PrecisionDate end = null;
		if (endValue != null) {
			end = super.getKMLDate(endValue);
			if (end != null) {
				data.setEndTime(end.toString()); 
				logger.debug("Parsed end value = " + end.toString());				
			} else {
				throw new RewriterException("Error parsing end time value: " + endValue);
			}
		}
		
		//conditionally try for timestamp
		PrecisionDate ts = null;
		if ((begin == null) && (end == null)) {
			String tsValue = at.getTimestamp();
			if (tsValue != null) {
				ts = super.getKMLDate(tsValue);
				if (ts != null) {
					data.setTimestamp(ts.toString()); 
					logger.debug("Parsed timestamp value = " + ts.toString());				
				} else {
					throw new RewriterException("Error parsing end time value: " + tsValue);
				}
			}
		}
		
		if ((begin == null) && (end == null) && (ts == null)) {
			throw new RewriterException("Incomplete time data from attribute table");
		}
		
		logger.debug("Time data from attribute table: " + data.toString());
		return data;
	}
	
}
