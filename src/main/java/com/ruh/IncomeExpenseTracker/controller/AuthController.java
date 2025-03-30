package com.ruh.IncomeExpenseTracker.controller;

import com.ruh.IncomeExpenseTracker.payload.request.LoginRequest;
import com.ruh.IncomeExpenseTracker.payload.request.SignupRequest;
import com.ruh.IncomeExpenseTracker.payload.response.JwtResponse;
import com.ruh.IncomeExpenseTracker.payload.response.MessageResponse;
import com.ruh.IncomeExpenseTracker.security.JwtTokenProvider;
import com.ruh.IncomeExpenseTracker.service.UserService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruh.IncomeExpenseTracker.security.UserDetailsImpl;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signin")
    @Operation(summary = "Sign in a user", description = "Authenticates a user and returns JWT token")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Attempting to authenticate user: " + loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            System.out.println("Authentication successful for user: " + userDetails.getUsername());
            System.out.println("Generated JWT token length: " + jwt.length());

            JwtResponse response = new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail());

            System.out.println("Returning JWT response: " + response);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Logger.getLogger(AuthController.class.getName()).log(Level.WARNING, "Bad credentials: {0}", e.getMessage());
            return ResponseEntity.status(401).body(new MessageResponse("Invalid username or password"));
        } catch (LockedException | DisabledException | CredentialsExpiredException e) {
            // Handle locked, disabled, and expired credential accounts similarly
            Logger.getLogger(AuthController.class.getName()).log(Level.WARNING, "Account access restricted: {0}", e.getMessage());
            return ResponseEntity.status(401).body(new MessageResponse("Account access is restricted"));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            Logger.getLogger(AuthController.class.getName()).log(Level.WARNING, "JWT expired: {0}", e.getMessage());
            return ResponseEntity.status(401).body(new MessageResponse("Authentication token has expired"));
        } catch (io.jsonwebtoken.UnsupportedJwtException | io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.SignatureException e) {
            Logger.getLogger(AuthController.class.getName()).log(Level.WARNING, "JWT validation error: {0}", e.getMessage());
            return ResponseEntity.status(401).body(new MessageResponse("Invalid authentication token"));
        } catch (JwtException e) {
            Logger.getLogger(AuthController.class.getName()).log(Level.WARNING, "Other JWT error: {0}", e.getMessage());
            return ResponseEntity.status(401).body(new MessageResponse("Authentication token error"));
        } catch (IllegalArgumentException | NullPointerException | SecurityException e) {
            Logger.getLogger(AuthController.class.getName()).log(Level.SEVERE, "Authentication failed with validation error", e);
            return ResponseEntity.status(400).body(new MessageResponse("Authentication failed due to invalid data"));
        } catch (RuntimeException e) {
            Logger.getLogger(AuthController.class.getName()).log(Level.SEVERE, "Authentication failed with runtime error", e);
            return ResponseEntity.status(500).body(new MessageResponse("Authentication failed due to server error"));
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a user", description = "Creates a new user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            userService.registerUser(
                    signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    signupRequest.getPassword());

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
