package com.mathra.salon.controller;

import com.mathra.salon.service.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Main controller for handling basic navigation
 */
@Controller
public class MainController {

    private final UserFileService userFileService;

    @Autowired
    public MainController(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    /**
     * Welcome page (public)
     */
    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }
    
    /**
     * Alias for welcome page
     */
    @GetMapping("/welcome")
    public String welcomeAlias() {
        return "welcome";
    }
    
    /**
     * Home page (requires login)
     * Redirects based on user role
     */
    @GetMapping("/home")
    public String home(Authentication authentication) {
        // Check if user has STAFF role
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFF")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/staff/dashboard";
        }
        
        // For customers and other roles
        return "home";
    }
    
    /**
     * Login page
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * Registration page
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }
} 