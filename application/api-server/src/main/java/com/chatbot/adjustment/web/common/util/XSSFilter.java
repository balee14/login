package com.chatbot.adjustment.web.common.util;

public class XSSFilter {

	public static String XSS(Object o) {
		
		if(o == null) {
			return null;
		}
		
		return XSS((String)o);
	}
	
	public static String XSS(String s) {
		String valid = null;
		
		if(s != null) {
			valid = s.trim();
			
			valid = valid.replaceAll("&", "&amp;");
			valid = valid.replaceAll("<", "&lt;");
			valid = valid.replaceAll(">", "&gt;");
			valid = valid.replaceAll("\"", "&quot;");
			valid = valid.replaceAll("\'", "&#39;");
		}
		
		return valid;
	}
	
	public static String XSSBack(String s) {
		
		String valid = null;
		
		if (s != null) {
			
			valid = s.trim();
			valid = valid.replaceAll("&amp;", "&");
			valid = valid.replaceAll("&lt;", "<");
			valid = valid.replaceAll("&gt;", ">");
			valid = valid.replaceAll("&quot;", "\"");
			valid = valid.replaceAll("&#39;", "\'");
		}
		
		return valid;
	}
}
