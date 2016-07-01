package edu.harvard.cga.gtools.tekml;

/**
 * 
 * 	A simple java bean that encapsulates data from actual attribute table from KML file
 *  that contains only the name value pairs that correspond to allowable items specified in the config.
 *  Note these are just unvalidated string values. 
 *  
 *  In the current implementation, the items selected are time values and name
 *  
 *  The parsing and other complexities and business rules of the time values are handled by the 
 *  TimeSource and KmlTimeData classes after the raw data strings are passed to them.
 *  
 *  Objects of this class are created by a factory method in another class
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class AttributeTable {
	
	private String name;
	private String beginTime;
	private String endTime;
	private String timestamp;
	
	public AttributeTable() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name = ").append(name);
		sb.append("; begin time = ").append(beginTime);
		sb.append("; end time = ").append(endTime);
		sb.append("; timestamp = ").append(timestamp);
		return sb.toString();
	}
}
