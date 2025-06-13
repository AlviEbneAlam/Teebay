package com.shazam.teebay.service;

import com.shazam.teebay.entity.UserInfo;
import com.shazam.teebay.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class JwtUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;

	@Autowired
	public JwtUserDetailsService(UserRepository userRepository){
		this.userRepository=userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		log.info("Username in loadUserByUsername class is: {}",username);
		UserInfo userInfo=null;

		try{
			 userInfo = userRepository.findByEmail(username);
		}
		catch(Exception ex){
			log.info("Exception occurred while fetching user data from db");
			log.info("Exception for username: {}, : {}", username,ex.getLocalizedMessage());
		}

		if (userInfo!=null) {
			return new User(userInfo.getEmail(), userInfo.getPassword(),
					new ArrayList<>());
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}

}