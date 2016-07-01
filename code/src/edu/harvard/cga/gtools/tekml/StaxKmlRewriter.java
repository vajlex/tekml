package edu.harvard.cga.gtools.tekml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.namespace.QName;
import javax.xml.stream.events.*;
//import javax.xml.stream.events.XMLEvent;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import static javax.xml.stream.XMLStreamConstants.*;


/**
 * 	Rewrite the KML with the following changes:
 * 
 * 	1.  change namespace to the current time-enabled version, at least 2.1
 *  2.  take time elements in the attribute tables of each placemark and convert them
 *      into the time elements for KML 2.1
 *  3.  if the placemark name is missing take the name from the attribute table 
 *  4.  if so specified, empty a folder of its placemarks
 * 
 * 
 * 	This implementation uses the StAX iterator API 
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class StaxKmlRewriter extends KmlRewriter {
	
	private final String endline = System.getProperty("line.separator");
	
	/**
	 *  array of element names that precede <description> excluding first element <name>
	 *  used to find correct location to write new description
	 */
	private final String[] FolderPostDesc = {"Placemark", "LookAt", "styleUrl", "Style", 
			"StyleMap", "Region", "metadata"  };	
	private Set<String> setFolderPostDesc = new HashSet<String>();

	private final String[] PostTime = {"styleUrl", "Style", "StyleMap", "Region", "metadata", 
			"Point", "LineString", "LinearRing", "Polygon", "MultiGeometry", "Model"  };	
	private Set<String> setPostTime = new HashSet<String>();

	private XMLInputFactory inputFactory;
	private XMLOutputFactory outputFactory;
	private XMLEventFactory eventFactory;

	public StaxKmlRewriter() {
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        eventFactory = XMLEventFactory.newInstance();
        
        initSet(setFolderPostDesc, FolderPostDesc);
        initSet(setPostTime, PostTime);
	}

	/**
	 *  Rewrites incoming KML file as new file
	 *  Merely a wrapper to call the method rewrite(InputStream, OutputStream)
	 *  
	 *  @param kmlSource - The source file to convert.
	 *  @param kmlDest   - The destination file result.
	 *  @exception       - throws RewriterException on parse error

	 */
	@Override
	public void rewrite(File kmlSource, File kmlDest) throws RewriterException {
		FileInputStream fis = null;
		FileOutputStream fos = null;

        try {
            fis = new FileInputStream(kmlSource);                         
            fos = new FileOutputStream(kmlDest);
            
            rewrite(fis, fos);
        } catch (FileNotFoundException e) {
        	logger.error("KML file not found exception: " + e);
            throw new RewriterException(e);
        } catch (RewriterException e) {
			if (kmlDest.delete()) {
				logger.debug("Deleted incomplete output file " + kmlDest.getName());
			} else {
				logger.warn("Unable to delete incomplete output file " + kmlDest.getName());
			} 
			e.printStackTrace();
			throw e;
        } finally {
        	try {
	            fis.close();	            
	            fos.close();
        	} catch (IOException e) {
        		logger.warn("Error closing IOStream: " + e);
        	}
        }
	}

	/**
	 *  Rewrites incoming KML stream as new stream
	 *  
	 *  @param kmlSource - The source stream to convert.
	 *  @param kmlDest   - The destination stream result.
	 *  @exception       - throws RewriterException on parse error, capturng XMLStreamException
	 */
	@Override
	public void rewrite(InputStream kmlSource, OutputStream kmlDest)
			throws RewriterException {
		
		XMLEventReader reader = null;
		XMLEventWriter writer = null;
		
		boolean docname = false;  //flag for existence of document name
		int folderCount = 0;
        String tag = "";   // set by each start element for use by chars event
		
        try {
            reader = inputFactory.createXMLEventReader(kmlSource);            
            writer = outputFactory.createXMLEventWriter(kmlDest);
            
            while (reader.hasNext()) {
            	XMLEvent event = (XMLEvent) reader.next(); 
            	
                int etype = event.getEventType();
                switch (etype) {
                case START_ELEMENT : {
                	StartElement se = event.asStartElement();
                	tag = se.getName().getLocalPart();
                	
                	if (tag.equals("kml")) {
                		String ns = se.getNamespaceURI("");
                		logger.info("namespace from incoming KML: " + ns);
                		
                		//if ns is older than the one specified, then replace
                		String nsuri = super.getKmlNamespace()	;	                		
                		if (isMoreCurrentKMLSpec(ns, super.getKmlNamespace())) {
	                		logger.info("replacing namespace with: " + nsuri);                			
	                		Set<Attribute> attrs = new HashSet<Attribute>();  //empty	                		
	                		Set<Namespace> nss = new HashSet<Namespace>();
	                		nss.add(eventFactory.createNamespace(nsuri));
	                		event = eventFactory.createStartElement("", "", "kml", attrs.iterator(), nss.iterator());
                		}
        				writer.add(event);
        			} else if (tag.equals("Document")) {
        				writer.add(event);
        			} else if (tag.equals("name")) {
            				writer.add(event);
        					processDocumentName(reader, writer);  //just adds chars and end
            				docname = true;
        			} else {  //everything else
        				if (!docname) {  //if docname missing write before the rest
    	                	QName qn = new QName("name");
    	                	writer.add(eventFactory.createStartElement(qn, null, null));   
    	                	writer.add(eventFactory.createCharacters("Processed by " + processedMarker));
    	                	writer.add(eventFactory.createEndElement(qn, null));      					
        					docname = true;   
        				}
        				if (tag.equals("Style")) {
	        				writer.add(event);
	        				processStyle(reader, writer);
	        			} else if (tag.equals("Folder")) {	        				
	        				logger.debug("Folder " + ++folderCount);
	        				writer.add(event);
	        				processFolder(reader, writer);
	        			} else {
	        				writer.add(event);
	        			}
        			}
                } break;   
                
        		case END_ELEMENT :
        			//logger.debug("end: " + reader.getLocalName());
        			writer.add(event);
        			break;
        			
        		case COMMENT :
        			if (processedMarker != null) {
        				if (event.asCharacters().getData().matches(processedMarker)) {
        					throw new RewriterException("Input file is identified as already processed by " + processedMarker);
        				}
        			}
            	                              
            	default : writer.add(event);            	
                }           	
            }
            
        } catch (XMLStreamException e) {
        	logger.error("KML parsing exception: " + e);
            throw new RewriterException(e);
        } catch (RewriterException e) {
        	logger.error("KML rewriter exception: " + e);
        	throw e;
        } finally {
        	try {
	            reader.close();	            
	            writer.flush();
	            writer.close();
        	} catch(XMLStreamException e) {
        		logger.warn("Error closing Stax reader/writer");
        	}
        }
	}	
	
	private void processDocumentName(XMLEventReader reader, XMLEventWriter writer) 
	throws XMLStreamException, RewriterException {
        while (reader.hasNext()) {
        	XMLEvent event = (XMLEvent) reader.next();         	
            int etype = event.getEventType();
       	
            switch (etype) {
            case START_ELEMENT : //name already added; no other start elements 
            	break;
    		case CHARACTERS : 
    			String chars = event.asCharacters().getData();
    			
				if (chars.trim().length() > 0) { // dont process whitespace in text       			
        			event = eventFactory.createCharacters(chars + " - " + processedMarker);
				}
				writer.add(event);
    			break;
            
            case END_ELEMENT :
				writer.add(event);
            	return;
            }           
        }           
	}	
	
	/**
	 * 	If folder name matches config item for removing placemarks from folder, 
	 *     then change name, desc, and remove placemarks
	 * 
	 * 
	 * @param reader
	 * @param writer
	 * @throws XMLStreamException
	 * @throws RewriterException
	 */
	private void processFolder(XMLEventReader reader, XMLEventWriter writer) 
	throws XMLStreamException, RewriterException {
		
		int placemarkCount = 0;
		boolean emptyFolder = false;  //flag to mark folder for which to remove elements
		int skippedElementCount = 0;
		boolean descFlag = false;    //used to mark existence of description 
        String tag = "";   // set by each start element for use by chars event       			
		logger.debug("Processing Folder");

        while (reader.hasNext()) {
        	XMLEvent event = (XMLEvent) reader.next(); 
        	
            int etype = event.getEventType();
       	
            switch (etype) {
            case START_ELEMENT : 
            	StartElement se = event.asStartElement();
            	tag = se.getName().getLocalPart();
            	logger.debug("start element: " + tag);
    			
    			if(tag.equals("name")) {
    				writer.add(event);
    			} else if(tag.equals("description")) {
					descFlag = true;
    				if (!emptyFolder) {  
    					writer.add(event);
    				}    				
    			} else if (setFolderPostDesc.contains(tag)) {
    				if (emptyFolder && !descFlag) {
    					addSimpleElement(writer, "description", "Duplicate set of Placemarks removed - " + processedMarker);
    					descFlag = true;
    				}
        			if (tag.equals("Placemark")) {
        				if (emptyFolder) {
        					skipElement(reader, "Placemark");
        					skippedElementCount++;
        				} else {
        					writer.add(event);
            				placemarkCount++;
            				processPlacemark(reader, writer);
        				}
        			} else if (tag.equals("Style")) {
	    				writer.add(event);
	    				processStyle(reader, writer);
	    			} else {
	    				writer.add(event);
	    			}
    			} else {
    				writer.add(event);
    			} 				
				break;
            	
            case CHARACTERS : 
    			String chars = event.asCharacters().getData();
    			
    			String trimChars = chars.trim();
				if (trimChars.length() > 0) { // dont process whitespace in text       			
        			if (tag.equals("name")) { 
        				logger.info("Folder: " + trimChars);
        				
        				if ((folderToEmpty != null) && !("".equals(folderToEmpty)) 
        						&& trimChars.startsWith(folderToEmpty)) {  
        					logger.info("Located folder to remove placemarks: " + chars);
        					emptyFolder = true;
        					event = eventFactory.createCharacters(chars + " - Removed " + processedMarker);
        				} 
        				writer.add(event);

        			} else if (tag.equals("description")) {
        				if (emptyFolder) { 
        				//	skip  -- new desc written , see above
        				} else {
        					writer.add(event);
        				}
        			} else {
            			writer.add(event);
        			}
				} else {
					writer.add(event);  //whitespace  
				}
    			break;
            
            case END_ELEMENT :
    			String local = event.asEndElement().getName().getLocalPart();
            	logger.debug("end element: " + local);
    			if (local.equals("Folder")) {
    				writer.add(event);
    		        logger.info("Number of placemarks read: " + placemarkCount);  
    		        if (skippedElementCount > 0) {
    		        	logger.info("Number of elements skipped: " + skippedElementCount);
    		        } else {
    		        	logger.debug("Number of elements skipped: " + skippedElementCount);
    		        }
    				return;
    			} else if (local.equals("description")) {
    				if (!emptyFolder) {
    					writer.add(event);    					
    				}  // else skip -- entire element written as unit before name
    			} else {
    				writer.add(event);
    			}				
            	break;
            	
        	default : writer.add(event);            	
            }

        }           
	}
	
	/**
	 *  Read elements up to description and defer in a queue. 
	 *  so as to extract new name and time elements from description
	 *  
	 * @param reader
	 * @param writer
	 * @throws XMLStreamException
	 * @throws RewriterException
	 */
	private void processPlacemark(XMLEventReader reader, XMLEventWriter writer) 
	throws XMLStreamException, RewriterException {
		
        String tag = "Placemark";   // set by each start element for use by chars event
    	QName qnMultiGeometry = new QName("MultiGeometry");
		
		logger.debug("Processing Placemark");
		
    	Queue<XMLEvent> queue = new LinkedList<XMLEvent>();

    	String attrTableString = deferEvents(reader, queue);
		
		AttributeTable attributeTable = null;
		KmlTimeData timeData = null;

    	logger.debug("chars: >>" + attrTableString + "<<");
		if (attrTableString.length() > 0) {   
		    attributeTable = attributeTableFactory.createAttributeTable(attrTableString);
    		timeData = timeSource.getTimeData(attributeTable);
    		if (timeData == null) { 
    			logger.error("null time data"); 
    			throw new XMLStreamException("null time data");
    		} 
		}
		
		//process queue, now that description has been retrieved and munged
		boolean nameFlag = false;
		
		while (queue.peek() != null) {
			XMLEvent event = queue.remove();
            int etype = event.getEventType();
           	
            switch (etype) {
            case START_ELEMENT : 
            	tag = event.asStartElement().getName().getLocalPart();
            	logger.debug("element: " + tag);
			     			       			        			
			    if (tag.equals("name")) {  //may or may not exist; will be first element
			    	writer.add(event);
			    	nameFlag = true;
			    } else {
			    	if (!nameFlag) {  //write if missing
			    		String name = attributeTable.getName();
			    		if (!"".equals(name)) {
			    			addSimpleElement(writer, "name", name);
			    		}
			    		nameFlag = true;
			    	}
			    	writer.add(event);
    			}
            	break;   
            	
            case CHARACTERS :
		    	if (tag.equals("description")) {
	        		String chars = event.asCharacters().getData().trim();
	       		    if (chars.length() > 0) {   
			    		writer.add(eventFactory.createCData(chars));	       		    	
	       		    } else {
	       		    	writer.add(event);
	       		    }
		    	} else {
		    		writer.add(event);
		    	}
		    	break;
            default : 
            	writer.add(event);
	    		break;
            } //end switch			
		} //end queue processing

		boolean timeFlag = false;
		
        while (reader.hasNext()) {  
        	XMLEvent event = (XMLEvent) reader.next(); 
        	
            int etype = event.getEventType();
       	
            switch (etype) {
            case START_ELEMENT : 
            	tag = event.asStartElement().getName().getLocalPart();
            	logger.debug("element: " + tag);
            	
            	if (!timeFlag && setPostTime.contains(tag)) {
            		writeTimeElements(writer, timeData);
            		timeFlag = true;
            	}
			     			       			        			
   			    if (tag.equals("MultiPoint") || tag.equals("MultiPolygon") 
     					|| tag.equals("MultiLine") || tag.equals("GeometryCollection")) {
     				logger.debug("Found non-KML start element '" + tag + "' - converting to 'MultiGeometry'");
     				writer.add(eventFactory.createStartElement(qnMultiGeometry, null, null));
     			} else if (tag.equals("pointMember") || (tag.equals("polygonMember") || tag.equals("lineMember"))) {
    				//ignore - comes with Multi...
    			} else {
        			writer.add(event);
    			}
            	break;           
            
    		case CHARACTERS :   //can read multiple times for whitespace
    			String chars = event.asCharacters().getData();
				     			
				if (chars.trim().length() > 0) { // dont process whitespace in text
					if (tag.equals("altitudeMode")) {
						if (chars.equals("clampedToGround")) {
							writer.add(eventFactory.createCharacters("clampToGround"));
							logger.debug("Replacing invalid 'clampedToGround' with 'clampToGround' per spec");
						}
					} else {
						writer.add(event);  
					}
				} else {
					writer.add(event);  //whitespace;  
				}
    			break;       			
        	
			case END_ELEMENT :
				String local = event.asEndElement().getName().getLocalPart();
				if (local.equals("Placemark")) {
	    			writer.add(event);
					return;
				} else if (local.equals("pointMember") || local.equals("polygonMember") || local.equals("lineMember")) {
					//ignore - comes with MultiPoint
     			} else if (local.equals("MultiPoint") || local.equals("MultiPolygon") 
     					|| local.equals("MultiLine") || local.equals("GeometryCollection")) {
     				logger.debug("Found non-KML end element '" + local + "' - converting 'MultiGeometry'");
     				writer.add(eventFactory.createEndElement(qnMultiGeometry, null));
				} else {
	    			writer.add(event);
				}
	    		break;
            } //end switch
       } //end while
	}
	
	private void writeTimeElements(XMLEventWriter writer, KmlTimeData data) 
	throws XMLStreamException {
		QName qn = null;
		writer.add(eventFactory.createCharacters(endline + "            "));
		logger.debug("Writing time data ");
		if (data.isSpan()) {
			qn = new QName("TimeSpan");
			writer.add(eventFactory.createStartElement(qn, null, null));
			addSimpleElement(writer, "begin", data.getBeginTime());
			addSimpleElement(writer, "end", data.getEndTime());			  
			writer.add(eventFactory.createEndElement(qn, null));
		} else {
			qn = new QName("TimeStamp");
			writer.add(eventFactory.createStartElement(qn, null, null));
			addSimpleElement(writer, "when", data.getTimestamp());
			writer.add(eventFactory.createEndElement(qn, null));
		}
	}
	
	/**
	 * 
	 * @param reader
	 * @param queue - to load with events up through description
	 * @return attribute table string
	 * @throws XMLStreamException
	 * @throws RewriterException
	 */
	private String deferEvents(XMLEventReader reader, Queue<XMLEvent> queue) 
	throws XMLStreamException, RewriterException {
	    logger.debug("Capturing deferred event before description.");
	    
    	String tag = "Placemark";
    	String desc = null;
		
        while (reader.hasNext()) {
        	XMLEvent event = (XMLEvent) reader.next(); 
			queue.offer(event); //collect everything
 
            int etype = event.getEventType();
           	
            switch (etype) {
            case START_ELEMENT : 
            	tag = event.asStartElement().getName().getLocalPart();
            	break;
			case CHARACTERS :   //can read multiple times for whitespace
				if (tag.equals("description")) {	
	    			String trimChars = event.asCharacters().getData().trim();
					if (trimChars.length() > 0) { // dont process whitespace in text
						desc = trimChars;						
					}
				}				
	            break;
			case END_ELEMENT :   //can read multiple times for whitespace
				if (event.asEndElement().getName().getLocalPart().equals("description")) {
					return desc;
				}
            }
        }
        //if we get here, then description not found
        //throw new RewriterException("Placemark description not found");
        return null;
	}
		
	/**
	 * Style can be placed in many elements:  Document, Folder, Placemark
	 * Many of the deprecated elements and changes from v. 2.0 are here
	 * 
	 * @param reader
	 * @param writer
	 * @throws XMLStreamException
	 * @throws RewriterException
	 */
	private void processStyle(XMLEventReader reader, XMLEventWriter writer) 
	throws XMLStreamException, RewriterException {
		
		String tag = "Style";
		
        while (reader.hasNext()) {
        	XMLEvent event = (XMLEvent) reader.next();         	
            int etype = event.getEventType();
       	
            switch (etype) {
            case START_ELEMENT :  
            	StartElement se = event.asStartElement();
            	tag = se.getName().getLocalPart();
            	if (tag.equals("BalloonStyle")) {
            		processBalloonStyle(reader, writer);
            		logger.info("Changing deprecated BalloonStyle.color element to .bgColor");
    			} else if (tag.equals("Icon")) {
    				writer.add(event);
    				processIcon(reader, writer);
    			} else if (tag.equals("geomScale")) {
            		writer.add(eventFactory.createStartElement(new QName("scale"), null, null));
            		logger.info("Changing deprecated Style.geomScale element to .scale");
            	} else {
            		writer.add(event);
            	}
            	break;
    		case END_ELEMENT :
    			String local = event.asEndElement().getName().getLocalPart();   			
    			if (local.equals("Style")) {
            		writer.add(event);
    				return;
    			} else if (local.equals("geomScale")) {
            		writer.add(eventFactory.createEndElement(new QName("scale"), null));
            	} else {
            		writer.add(event);
    			}
        		break;	        		
    		default : 
				writer.add(event);
    			break;            
            }
        }
	}
	
	/**
	 *   Fix change in 2.1 to use bgColor element in place of 'color'
	 *   The character content may not be evalated similarly by GoogleEarth??
	 * @param reader
	 * @param writer
	 * @throws XMLStreamException
	 * @throws RewriterException
	 */
	private void processBalloonStyle(XMLEventReader reader, XMLEventWriter writer) 
	throws XMLStreamException, RewriterException {
		
		String tag = "BalloonStyle";
		
        while (reader.hasNext()) {
        	XMLEvent event = (XMLEvent) reader.next();         	
            int etype = event.getEventType();
       	
            switch (etype) {
            case START_ELEMENT :  
            	StartElement se = event.asStartElement();
            	tag = se.getName().getLocalPart();
            	if (tag.equals("color")) {
            		writer.add(eventFactory.createStartElement(new QName("bgColor"), null, null));
            		logger.info("Changing deprecated BalloonStyle.color element to .bgColor");
            	} else {
            		writer.add(event);
            	}
            	break;
    		case END_ELEMENT :
    			String local = event.asEndElement().getName().getLocalPart();   			
    			if (local.equals("BalloonStyle")) {
            		writer.add(event);
    				return;
    			} else if(local.equals("color")) {
            		writer.add(eventFactory.createEndElement(new QName("bgColor"), null));
    			} else {
            		writer.add(event);
    			}
        		break;	        		
    		default : 
				writer.add(event);
    			break;            
            }
        }
	}

	/**
	 *   elements x, y, w, h are deprecated in KML 2.1
	 * @param reader
	 * @param writer
	 * @throws XMLStreamException
	 * @throws RewriterException
	 */
	private void processIcon(XMLEventReader reader, XMLEventWriter writer) 
	throws XMLStreamException, RewriterException {
		
		String tag = "Icon";
		
        while (reader.hasNext()) {
        	XMLEvent event = (XMLEvent) reader.next();         	
            int etype = event.getEventType();
       	
            switch (etype) {
            case START_ELEMENT :  
            	StartElement se = event.asStartElement();
            	tag = se.getName().getLocalPart();
            	if (tag.equals("x") || tag.equals("y") || tag.equals("w") || tag.equals("h")) {
            		skipElement(reader, tag);
            		logger.info("Removing deprecated x, y, w, or h element in Icon element");
            	} else {
            		writer.add(event);
            	}
            	break;
    		case END_ELEMENT :
        		writer.add(event);
    			if (event.asEndElement().getName().getLocalPart().equals("Icon")) {
    				return;
    			} 
        		break;	        		
    		default : 
				writer.add(event);
    			break;            
            }           
        }           
	}	
	
	
	/**
	 *   Read this element and contents but don't write it out
	 *   The start element has already been read, but this reads through to end element
	 *   Will not work with recursive elements
	 * @param reader
	 * @param tag
	 * @throws XMLStreamException
	 */
	private void skipElement(XMLEventReader reader, String tag) throws XMLStreamException {
		
        while (reader.hasNext()) {
        	XMLEvent event = (XMLEvent) reader.next(); 
        	
            int etype = event.getEventType();
        	
        	switch(etype) {
    		case END_ELEMENT :
    			if (event.asEndElement().getName().getLocalPart().equals(tag)) {
    				return;
    			} 
        		break;	        		
        	}
        }
	}
	
	private void addSimpleElement(XMLEventWriter writer, String tag, String text) 
	throws XMLStreamException {
		  QName qn = new QName(tag);
		  writer.add(eventFactory.createStartElement(qn, null, null));
		  writer.add(eventFactory.createCharacters(text));
		  writer.add(eventFactory.createEndElement(qn, null));		
	}
	
	private void initSet(Set<String> set, String[] ar) {
		for (String s : ar) {
			set.add(s);
		}		
	}

}
