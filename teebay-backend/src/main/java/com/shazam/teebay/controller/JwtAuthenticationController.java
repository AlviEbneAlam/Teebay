package com.shazam.teebay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shazam.teebay.Utils.JwtTokenUtil;
import com.shazam.teebay.dto.JwtRequest;
import com.shazam.teebay.dto.JwtResponse;
import com.shazam.teebay.service.JwtUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("${api.base_url}")
@Slf4j
public class JwtAuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	private final JwtTokenUtil jwtTokenUtil;
	private final JwtUserDetailsService jwtUserDetailsService;

	@Autowired
	public JwtAuthenticationController(JwtTokenUtil jwtTokenUtil,
                                       JwtUserDetailsService jwtUserDetailsService){
		this.jwtTokenUtil=jwtTokenUtil;
		this.jwtUserDetailsService=jwtUserDetailsService;
	}

	@MutationMapping
	public JwtResponse login(@Argument JwtRequest jwtRequest)
			throws Exception {

		log.info(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(jwtRequest));
		log.info("Entering create authentication token");

		final UserDetails userDetails = jwtUserDetailsService
				.loadUserByUsername(jwtRequest.getEmail());

		final String token = jwtTokenUtil.generateToken(userDetails,jwtRequest);

		if(token!=null){
			JwtResponse jwtResponse=new JwtResponse("Success",token);
			log.info(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(jwtResponse));
			return jwtResponse;
		}
		else{
			JwtResponse jwtResponse=new JwtResponse("Success",token);
			log.info(new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(jwtResponse));
			return jwtResponse;
		}

	}

	private void authenticate(String username, String password) throws Exception {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);

		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
}
