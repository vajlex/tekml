package edu.harvard.cga.gtools.tekml;

/**
 * 
 * @author W.Hays  (whays at nearity.com)
 *
 */
public class RewriterException extends Exception {
	public static final long serialVersionUID = 1L;
	
	public RewriterException(Exception e) {
		super(e);
	}
	
	public RewriterException(String s) {
		super(s);
	}

	
}
