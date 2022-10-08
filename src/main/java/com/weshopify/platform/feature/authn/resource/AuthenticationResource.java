package com.weshopify.platform.feature.authn.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.weshopify.platform.feature.authn.bean.WeshopifyUser;
import com.weshopify.platform.feature.authn.exceptions.InvalidTokenException;
import com.weshopify.platform.feature.authn.exceptions.TokenExpiredException;
import com.weshopify.platform.feature.authn.service.JwtTokenService;
import com.weshopify.platform.features.authn.config.OutboundCommunicator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AuthenticationResource {

	@Autowired
	private JwtTokenService jwtTokenService;
	
	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;
	
	@Autowired
	private OutboundCommunicator wso2ApiInvoker;

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
		System.out.println("jwt token is:\t"+jwtToken);
		Map<String, String> jsonTokenMap = new HashMap<>();
		jsonTokenMap.put("accessToken", jwtToken);
		
		HashOperations<Object, Object, Object> hashMap = redisTemplate.opsForHash();
		hashMap.put(new String("weshopifyUser"),jwtToken,loggedInUser);
		hashMap.put(new String("access_token")+loggedInUser, "Bearer", jwtToken);
		hashMap.put(new String("refresh_token")+loggedInUser, "Bearer", jwtToken);
		try {
			jwtTokenService.validateToken(jwtToken);
		} catch (TokenExpiredException | InvalidTokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	
	@PostMapping(value = "/iam-authn")
	public ResponseEntity<Object> iamAuthentication(@RequestBody WeshopifyUser user){
		String jsonResp = wso2ApiInvoker.authenticateId(user.getUsername(), user.getPassword());
		System.out.println("json resp is:\t"+jsonResp);
		JSONObject jsonRespBody = new JSONObject(jsonResp);
		String jwtToken = (String) jsonRespBody.get("access_token");
		System.out.println("access toke is:\t"+jwtToken);
		
		int expiry = jsonRespBody.getInt("expires_in");
		System.out.println("*****Token expiry is:\t"+expiry);
		Date tokenExpDate = getExpiry(expiry);
		
		String loggedInUserJson = wso2ApiInvoker.getUserInfo(jwtToken);
		JSONObject loggedInUserBody = new JSONObject(loggedInUserJson);
		System.out.println("logged in user is:\t"+loggedInUserBody.getString("sub"));
		
		HashOperations<Object, Object, Object> hashMap = redisTemplate.opsForHash();
		hashMap.put(new String("weshopifyUser"),jwtToken,loggedInUserBody.getString("sub"));
		hashMap.put(new String("token_expiry"),jwtToken, tokenExpDate);
		
		return ResponseEntity.ok(jsonResp);
	}
	
	private Date getExpiry(int expiry) {
		Date date = new Date();
		System.out.println(date);
		long newTime = date.getTime()+Long.valueOf(String.valueOf(expiry*1000));
		Date updatedDate = new Date(newTime);
		System.out.println(updatedDate);
		return updatedDate;
		
	}

}
