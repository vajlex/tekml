package edu.harvard.cga.gtools.tekml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

//import edu.harvard.cga.gtools.tekml.pdate.PrecisionDate;

/**
 *  Interface for classes to implement factory method to create attributeTable
 *  
 *  
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class AttributeTableFactory {
	
	static Logger logger = Logger.getLogger("edu.harvard.cga.gtools.tekml");	
	
	private String rowSeparator;  
	private Pattern rowPattern;
	
	private LabelList beginList;
	private LabelList endList;
	private LabelList timestampList;
	
	private String nameLabel;

	
	public AttributeTableFactory() {}
	
	public AttributeTable createAttributeTable(String table) throws RewriterException {
		
		AttributeTable at = new AttributeTable();
		String[] ar = table.split(rowSeparator);
		
		String beginValue = null;
		String endValue = null;
		String timestampValue = null;
		String nameValue = null;
		
		logger.debug("Parsing attribute table with " + ar.length + " rows");
		
		try {
			for (String line : ar) {
				Matcher matcher = rowPattern.matcher(line.trim());  
				logger.debug("   line = " + line.trim());
				if (matcher.matches()) {
					String label = matcher.group(1);
	
					if ((beginList != null) && beginList.hasMatch(label)) {				
						beginValue = matcher.group(2);	
						at.setBeginTime(beginValue);
						logger.debug("Match of begin Label: " + label + " : " + beginValue);
						if (endValue != null) break;
					} else if ((endList != null) && endList.hasMatch(label)) {				
						endValue = matcher.group(2);
						at.setEndTime(endValue);
						logger.debug("Match of end Label: " + label + " : " + endValue);
					} else if ((timestampList != null) && timestampList.hasMatch(label)) {
						timestampValue = matcher.group(2);
						at.setTimestamp(timestampValue);
						logger.debug("Match of timestamp Label: " + label + " : " + timestampValue);
					} else if ((nameLabel != null) && (nameLabel.matches(label))) {
						nameValue = matcher.group(2);
						at.setName(nameValue);
						logger.debug("Match of name label: " + label + " : " + nameValue);
					}
					
					//try to shorten the process
					if ((((at.getBeginTime() != null) && (at.getEndTime() != null))
							|| (at.getTimestamp() != null)) && (at.getName() != null)) {
						break;
					}					
					
				} else {
					logger.warn("regex " + rowPattern + " not matching table entries: " + line);
					continue;
				}
			}  //end reading of attribute table
		} catch(Exception e) {  //could be unchecked match regex ex
			throw new RewriterException("Unable to parse attribute table: " + e);
		}
		return at;
	}
	
	
	public void setRowPattern(String regex) throws PatternSyntaxException {  //unchecked
		rowPattern = Pattern.compile(regex);
	}
	
	public void setRowSeparator(String sep) {
		this.rowSeparator = sep;
	}

	public String getRowSeparator() {
		return rowSeparator;
	}

	public Pattern getRowPattern() {
		return rowPattern;
	}
	
	public LabelList getBeginList() {
		return beginList;
	}

	public void setBeginList(LabelList beginList) {
		logger.debug("Begin list: " + beginList.toString());
		this.beginList = beginList;
	}

	public LabelList getEndList() {
		return endList;
	}


	public void setEndList(LabelList endList) {
		this.endList = endList;
	}

	public LabelList getTimestampList() {
		return timestampList;
	}

	public void setTimestampList(LabelList timestampList) {
		this.timestampList = timestampList;
	}

	public String getNameLabel() {
		return nameLabel;
	}

	public void setNameLabel(String nameLabel) {
		this.nameLabel = nameLabel;
	}
	
}
