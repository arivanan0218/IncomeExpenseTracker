package com.ruh.IncomeExpenseTracker.service;

import com.ruh.IncomeExpenseTracker.model.User;
import com.ruh.IncomeExpenseTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        try {
            // Check if there's an authentication object in the security context
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                System.err.println("No authentication found in SecurityContext");
                throw new RuntimeException("No authentication found. Please log in.");
            }

            // Try to get the principal
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Check if principal is of type UserDetails
            if (!(principal instanceof UserDetails)) {
                System.err.println("Authentication principal is not of type UserDetails: " + principal);
                throw new RuntimeException("Invalid authentication details. Please log in again.");
            }

            UserDetails userDetails = (UserDetails) principal;
            System.out.println("Found authenticated user: " + userDetails.getUsername());

            return userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> {
                        System.err.println("User not found in database: " + userDetails.getUsername());
                        return new RuntimeException("Error: User not found in the system.");
                    });
        } catch (NullPointerException e) {
            System.err.println("Null pointer when getting current user: " + e.getMessage());
            throw new RuntimeException("Authentication data is missing. Please log in again.", e);
        } catch (ClassCastException e) {
            System.err.println("Class cast exception when getting current user: " + e.getMessage());
            throw new RuntimeException("Authentication data is invalid. Please log in again.", e);
        } catch (IllegalStateException e) {
            System.err.println("Illegal state when getting current user: " + e.getMessage());
            throw new RuntimeException("Authentication state is invalid. Please log in again.", e);
        } catch (Exception e) {
            System.err.println("Unexpected error getting current user: " + e.getMessage());
            throw new RuntimeException("Authentication error. Please log in again.", e);
        }
    }

    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }
}
