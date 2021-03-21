package com.m2r.easyparser;

public class ParserException extends Exception {

	private static final long serialVersionUID = 1L;

	private int errorOffset;
	
	public ParserException(String msg, Throwable throwable) {
		super(msg, throwable);
	}
	
	public ParserException(String msg, int errorOffset) {
		super(msg);
		this.errorOffset = errorOffset;
	}
	
	public int getErrorOffset() {
		return errorOffset;
	}

}
