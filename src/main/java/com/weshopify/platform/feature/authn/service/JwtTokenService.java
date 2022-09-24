package com.weshopify.platform.feature.authn.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.weshopify.platform.feature.authn.exceptions.InvalidTokenException;
import com.weshopify.platform.feature.authn.exceptions.TokenExpiredException;
import com.weshopify.platform.feature.authn.util.ServiceUtil;
import com.weshopify.platform.features.authn.config.WeshopifyUserDetailsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtTokenService {

	@Autowired
	private WeshopifyUserDetailsService userDetailsService;
	
	public String createToken(String loggedInUserName, List<String> roles) {
		
		Claims identities = Jwts.claims();
		
		identities.setSubject(loggedInUserName);
		identities.setIssuer(ServiceUtil.JWT_TOKEN_ISSUER);
		Date currentServerTime = new Date();
		identities.setIssuedAt(currentServerTime);
		identities.setExpiration(new Date(currentServerTime.getTime()+ServiceUtil.JWT_TOKEN_EXPIRY));
		
		identities.put("USER_ROLES", roles);
		
		
		
		return Jwts
		.builder()
		.setClaims(identities)
		.signWith(SignatureAlgorithm.HS256, ServiceUtil.JWT_TOKEN_API_KEY)
		.compact();
		
	}
	
	
	public UserDetails validateToken(String tokenWithBearer) throws TokenExpiredException, InvalidTokenException {
		boolean isTokenValid = false;

		String tokenVal = resolveToken(tokenWithBearer);
		if (tokenVal != null) {
			try {
				Claims userIdentities = Jwts.parser().setSigningKey(ServiceUtil.JWT_TOKEN_API_KEY).parseClaimsJws(tokenVal)
						.getBody();
				String loggedInUser = userIdentities.getSubject();
				UserDetails userDetails = userDetailsService.loadUserByUsername(loggedInUser);
				if (userDetails != null) { 
					Date tokenExpDate = userIdentities.getExpiration(); 
					isTokenValid = new Date().before(tokenExpDate);
					if (isTokenValid) {
						return userDetails;
					}else {
						throw new TokenExpiredException("token expired!! Please get the new token");
					}
				}else {
					throw new InvalidTokenException("Token is not valid!! It may be tampered. please try with the new one");
				}

			}catch (Exception e) {
				if(e instanceof ExpiredJwtException) {
					throw new TokenExpiredException(e.getLocalizedMessage());
				}
				throw new InvalidTokenException(e.getMessage());
			}
			
		}else {
			throw new InvalidTokenException("Token is not valid!! It may be tampered. please try with the new one");
		}
	}
	 
	
	private String resolveToken(String tokenWithBearer) {
		String tokenVal = null;
		if(tokenWithBearer.contains(ServiceUtil.JWT_TOKEN_TYPE)) {
			tokenVal = tokenWithBearer.replace(ServiceUtil.JWT_TOKEN_PREFIX, "");
			
		}
		return tokenVal;
	}
}
