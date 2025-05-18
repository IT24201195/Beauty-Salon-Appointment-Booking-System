package com.Mathra.Salon.controller;

import com.Mathra.Salon.model.User;
import com.Mathra.Salon.service.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling user registration
 */
@Controller
public class RegistrationController {

    private final UserFileService userFileService;

    @Autowired
    public RegistrationController(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    /**
     * Process registration form
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Set role to CUSTOMER (in case it wasn't set in the form)
            user.setRole(User.Role.CUSTOMER);

            // Register the user
            userFileService.registerNewUser(user);

            return "redirect:/login?registrationSuccess";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}