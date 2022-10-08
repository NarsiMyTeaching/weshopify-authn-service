package com.weshopify.platform.features.authn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class WeshopifyPlatformSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private WeshopifyUserDetailsService userDetailsService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.authorizeRequests()
		.antMatchers("/validate-token")
		.permitAll()
		.antMatchers("/authn")
		.authenticated()
		.and().csrf().disable()
		.httpBasic();
	}
	
public static void main(String[] args) {
	PasswordEncoder pwd = new BCryptPasswordEncoder();
	System.out.println(pwd.encode("testUser@123"));
}
	
}
