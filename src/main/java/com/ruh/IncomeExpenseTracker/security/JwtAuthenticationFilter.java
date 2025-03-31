package com.ruh.IncomeExpenseTracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                logger.info("Processing request with JWT: " + request.getMethod() + " " + request.getRequestURI());

                // Enhanced token validation with detailed logging
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    logger.info("JWT token is valid for user: " + username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("User authenticated successfully: " + username + " for URI: " + request.getRequestURI());
                } else {
                    // Clear any existing authentication as the token is invalid
                    SecurityContextHolder.clearContext();
                    logger.warn("JWT token validation failed for request: " + request.getRequestURI() + ". Authentication cleared.");
                }
            } else {
                // No authentication token, but we still continue the filter chain
                logger.info("No JWT token found in request: " + request.getRequestURI() + ". Access restricted to public endpoints only.");
            }
        } catch (UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
            logger.error("User not found in token validation: " + ex.getMessage() + " for URI: " + request.getRequestURI(), ex);
        } catch (IllegalArgumentException | NullPointerException ex) {
            SecurityContextHolder.clearContext();
            logger.error("Invalid argument in authentication: " + ex.getMessage() + " for URI: " + request.getRequestURI(), ex);
        } catch (io.jsonwebtoken.JwtException ex) {
            SecurityContextHolder.clearContext();
            logger.error("JWT token error: " + ex.getMessage() + " for URI: " + request.getRequestURI(), ex);
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            logger.error("Runtime error in authentication: " + ex.getMessage() + " for URI: " + request.getRequestURI(), ex);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            logger.error("Could not set user authentication: " + ex.getMessage() + " for URI: " + request.getRequestURI(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
