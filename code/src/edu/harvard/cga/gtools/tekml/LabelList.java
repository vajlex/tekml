package edu.harvard.cga.gtools.tekml;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * Lists for begin and end labels, to match against in rewriting KML
 * 
 * treats list items as regex
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class LabelList {

	private final Set<String> list;
	
	public LabelList() {
		list = new HashSet<String>();
	}

	public LabelList(String[] ar) {
		list = new HashSet<String>();
		if (ar != null) {
			for (String s : ar) {
				list.add(s);
			}
		}
	}
	

	public void populate(BufferedReader reader) throws IOException {
		String s = null;
		while ((s = reader.readLine()) != null) {
			list.add(s.trim());
		}
	}
	
	/**
	 * Add new label to list
	 * @param s
	 */
	public void add(String s) {
		list.add(s);
	}
	
	public void remove(String s) {
		list.remove(s);
	}
	
	public void removeAll() {
		list.clear();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public int size() {
		return list.size();
	}
	
	/**
	 * Test to see if label is on list
	 * @param s - name to match
	 * @return - true if match found, otherwise false
	 */
	public boolean hasMatch(String s) {
		
		for (String label : list) {
			if (s.matches(label)) {  // treats each list item as regex
				return true;
			}
		}
		return false;
	}
}
