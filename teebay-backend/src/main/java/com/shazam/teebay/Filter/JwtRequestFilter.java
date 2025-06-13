package com.shazam.teebay.filter;

import com.shazam.teebay.Cache.CachedBodyHttpServletRequest;
import com.shazam.teebay.Utils.JwtTokenUtil;
import com.shazam.teebay.service.JwtUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final JwtUserDetailsService jwtUserDetailsService;
	private final JwtTokenUtil jwtTokenUtil;

	private JSONObject formJson(String message, String path) {
		JSONObject error = new JSONObject();
		error.put("timestamp", Instant.now().toString());
		error.put("status", HttpStatus.UNAUTHORIZED.value());
		error.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
		error.put("message", message);
		error.put("path", path);
		return error;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");
		String jwtToken = null;
		String username = null;
		log.info("Inside jwt filer ");

		if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().equals("/graphql")) {

			CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
			String body = new String(cachedRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

			if (body.contains("login")) {
				filterChain.doFilter(cachedRequest, response);
				return;
			}

			log.info("After login mutation in jwt filer ");

			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				jwtToken = authHeader.substring(7);
				try {
					username = jwtTokenUtil.getUsernameFromToken(jwtToken);
				} catch (RuntimeException e) {
					log.error("Invalid JWT token: {}", e.getMessage());
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.setStatus(HttpStatus.UNAUTHORIZED.value());
					response.getWriter().write(formJson(e.getMessage(), request.getServletPath()).toString());
					return;
				}
			} else {
				log.warn("JWT token is missing or does not start with 'Bearer '");
			}

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
				try {
					if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
						UsernamePasswordAuthenticationToken authToken =
								new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(cachedRequest));

						SecurityContextHolder.getContext().setAuthentication(authToken);
					}
				} catch (RuntimeException e) {
					log.error("JWT validation failed: {}", e.getMessage());
					response.setStatus(HttpStatus.UNAUTHORIZED.value());
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write(formJson(e.getMessage(), request.getServletPath()).toString());
					return;
				}
			}

			filterChain.doFilter(cachedRequest, response);
		}
		else{
			filterChain.doFilter(request, response);
		}
	}
}
