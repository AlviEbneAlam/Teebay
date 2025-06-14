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
import java.util.Optional;

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
		log.info("Username in loadUserByUsername method: {}", username);

		try {
			Optional<UserInfo> userInfo = userRepository.findByEmail(username);

			if (userInfo.isEmpty()) {
				log.warn("User not found in database for username: {}", username);
				throw new UsernameNotFoundException("User not found with username: " + username);
			}

			UserInfo user = userInfo.get();
			return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
		} catch (Exception ex) {
			log.error("Exception occurred while fetching user data from database for username: {}", username, ex);
			throw new UsernameNotFoundException("Unable to load user due to internal error.");
		}
	}

}