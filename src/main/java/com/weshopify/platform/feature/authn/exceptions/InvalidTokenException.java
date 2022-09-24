package com.weshopify.platform.feature.authn.exceptions;

public class InvalidTokenException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public InvalidTokenException(String message) {
		this.message = message;
	}
	
	

}
