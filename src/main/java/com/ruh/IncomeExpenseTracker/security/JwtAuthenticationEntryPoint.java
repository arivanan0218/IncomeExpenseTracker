package com.ruh.IncomeExpenseTracker.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        System.err.println("Unauthorized access detected for path: " + request.getRequestURI());
        System.err.println("Authentication error: " + authException.getMessage());

        // Create a proper JSON response instead of just sending an error status
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String jsonResponse = String.format("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"You are not authenticated. Please login to access this resource.\", \"path\": \"%s\"}", request.getRequestURI());
        response.getWriter().write(jsonResponse);
    }
}

