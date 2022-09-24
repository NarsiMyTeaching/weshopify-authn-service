package com.weshopify.platform.features.authn.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.weshopify.platform.features.authn.repository.CustomerDataRepo;
import com.weshopify.platform.featuresauthn.models.Customer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WeshopifyUserDetailsService implements UserDetailsService {

	@Autowired
	private CustomerDataRepo customerRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("the logged in user name is:\t"+username);
		Customer customer = customerRepo.searchByEmail(username).get(0);
		
		String loggedInUser = customer.getEmail();
		log.info("user fetched from the db is:\t"+loggedInUser);
		
		String password = customer.getPassword();
		log.info("password fetched from the db is:\t"+password);
		
		List<GrantedAuthority> authoritiesList = new ArrayList<>();
		String roleName = customer.getRole().getRole();
		log.info("role name of the customer is:\t"+roleName);
		authoritiesList.add(new SimpleGrantedAuthority(roleName));
		
		return new User(loggedInUser, password, authoritiesList);
	}

}
