package com.mathra.salon.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller to handle various HTTP errors
 */
@Controller
public class CustomErrorController implements ErrorController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);
    
    @RequestMapping("/error")
    public String handleError(HttpServletResponse response, Model model) {
        int statusCode = response.getStatus();
        HttpStatus status = HttpStatus.valueOf(statusCode);
        
        logger.error("Error occurred with status code: {}", statusCode);
        
        model.addAttribute("status", statusCode);
        model.addAttribute("error", status.getReasonPhrase());
        
        switch (statusCode) {
            case 404:
                model.addAttribute("message", "The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.");
                break;
            case 403:
                model.addAttribute("message", "You don't have permission to access this resource.");
                break;
            case 500:
                model.addAttribute("message", "Our server encountered an unexpected error. Please try again later.");
                break;
            default:
                model.addAttribute("message", "An unexpected error occurred. Please try again later.");
                break;
        }
        
        return "error";
    }
} 