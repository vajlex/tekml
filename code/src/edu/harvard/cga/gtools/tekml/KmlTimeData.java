package edu.harvard.cga.gtools.tekml;

/**
 * A simple data transport object containing KML time data
 * The data elements are Strings expected to conform to ISO8601
 * 
 * Both timespan (a pair of values) and timestamp (a single value) are supported
 * by the same class.  
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class KmlTimeData {
	private String beginTime;
	private String endTime;
	
	public KmlTimeData() {}
	
	
	
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
	
	public void setTimespan(String beginTime, String endTime) {
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
	
	public boolean isSpan() {
		if ((beginTime != null) && (beginTime.length() > 0)
				&& (endTime != null) && (endTime.length() > 0)) {
			return true;
		} else return false;
	} 
	
	public void setTimestamp(String timestamp) {
		this.beginTime = timestamp;
	}	
	
	public String getTimestamp() {
		if ((beginTime != null) && (beginTime.length() > 0)) {
			return beginTime;
		} else if ((endTime != null) && (endTime.length() > 0)) {
			return endTime;
		} else return null;   
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (isSpan()) {
			sb.append("start = ").append(beginTime);
			sb.append("; end = ").append(endTime);
		} else {
			String ts = getTimestamp();
			if (ts == null) {
				return "";
			} else {
				sb.append("timestamp = ").append(ts);
			}
		}
		return sb.toString();
	}

}
