package com.weshopify.platform.feature.authn.util;

import java.util.Base64;

public class ServiceUtil {

	/**
	 * in milli seconds
	 */
	//public static final long JWT_TOKEN_EXPIRY=1800000L;
	public static final long JWT_TOKEN_EXPIRY=120000L;
	public static final String JWT_TOKEN_API_KEY=new String(Base64.getEncoder().encode("weshopify-platform".getBytes()));
	public static final String JWT_TOKEN_ISSUER="weshopify-platform";
	public static final String JWT_TOKEN_HEDER_NAME="Authorization";
	public static final String JWT_TOKEN_PREFIX = "Bearer ";
	public static final String JWT_TOKEN_TYPE = "Bearer";
	
}
