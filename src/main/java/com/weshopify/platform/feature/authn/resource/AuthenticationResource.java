package com.weshopify.platform.feature.authn.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.weshopify.platform.feature.authn.exceptions.InvalidTokenException;
import com.weshopify.platform.feature.authn.exceptions.TokenExpiredException;
import com.weshopify.platform.feature.authn.service.JwtTokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AuthenticationResource {

	@Autowired
	private JwtTokenService jwtTokenService;

	@GetMapping(value = "/authn")
	public Map<String, String> authenticate() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInUser = authentication.getName();
		log.info("logged in user is:\t" + loggedInUser);
		List<String> rolesList = new ArrayList<>();
		Collection<? extends GrantedAuthority> authnlist = authentication.getAuthorities();
		authnlist.forEach(ga -> {
			String role = ga.getAuthority();
			rolesList.add(role);
		});
		String jwtToken = jwtTokenService.createToken(loggedInUser, rolesList);
		Map<String, String> jsonTokenMap = new HashMap<>();
		jsonTokenMap.put("accessToken", jwtToken);
		return jsonTokenMap;
	}

	@GetMapping(value = "/validate-token")
	public Map<String, Object> validateToken(@RequestHeader("Authorization") String tokenValue) {
		log.info("authorization token value is:\t" + tokenValue);
		Map<String, Object> jsonTokenMap = new HashMap<>();
		try {
			UserDetails userDetails = jwtTokenService.validateToken(tokenValue);
			log.info("Credentials Expired?:\t" + !userDetails.isCredentialsNonExpired());
			if (userDetails.isCredentialsNonExpired()) {
				jsonTokenMap.put("isValidToken", true);
			}
		} catch (TokenExpiredException e) {
			jsonTokenMap.put("error", e.getLocalizedMessage());
			jsonTokenMap.put("isTokenExpired", true);
			jsonTokenMap.put("message", "Current Token expired!! Please get the new one");
		} catch (InvalidTokenException e) {
			jsonTokenMap.put("error", e.getLocalizedMessage());
			jsonTokenMap.put("isInvalidToken", true);
		}

		return jsonTokenMap;
	}

}
