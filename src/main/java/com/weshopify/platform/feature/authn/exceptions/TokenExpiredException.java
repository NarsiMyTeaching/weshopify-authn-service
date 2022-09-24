package com.weshopify.platform.feature.authn.exceptions;

public class TokenExpiredException extends Exception {

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

	public TokenExpiredException(String message) {
		this.message = message;
	}
	
	

}
