package com.Mathra.Salon.controller;

import com.mathra.salon.model.Review;
import com.mathra.salon.model.User;
import com.mathra.salon.service.ReviewFileService;
import com.mathra.salon.service.UserFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling user reviews
 */
@Controller
public class ReviewController {

    private final ReviewFileService reviewFileService;
    private final UserFileService userFileService;

    @Autowired
    public ReviewController(ReviewFileService reviewFileService, UserFileService userFileService) {
        this.reviewFileService = reviewFileService;
        this.userFileService = userFileService;
    }

    /**
     * Show the reviews page
     */
    @GetMapping("/reviews")
    public String showReviews(Model model) {
        model.addAttribute("reviews", reviewFileService.findApprovedReviews());
        model.addAttribute("averageRating", reviewFileService.getAverageRating());
        return "reviews";
    }

    /**
     * Show review form
     */
    @GetMapping("/review/new")
    public String showReviewForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        User user = userFileService.findUserByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("review", new Review());
        model.addAttribute("userReviews", reviewFileService.findReviewsByUser(user));
        return "review-form";
    }

    /**
     * Process review form
     */
    @PostMapping("/review/submit")
    public String submitReview(@ModelAttribute Review review, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        User user = userFileService.findUserByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }

        try {
            review.setUser(user);
            reviewFileService.createReview(review);
            redirectAttributes.addFlashAttribute("success", "Your review has been submitted and is pending approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/review/new";
    }

    /**
     * Admin: manage reviews
     */
    @GetMapping("/admin/reviews")
    public String manageReviews(Model model) {
        model.addAttribute("reviews", reviewFileService.findAllReviews());
        return "admin/reviews";
    }

    /**
     * Admin: approve review
     */
    @PostMapping("/admin/review/{id}/approve")
    public String approveReview(@PathVariable("id") Long id, @RequestParam("approved") boolean approved, RedirectAttributes redirectAttributes) {
        try {
            reviewFileService.approveReview(id, approved);
            redirectAttributes.addFlashAttribute("success", "Review " + (approved ? "approved" : "unapproved") + " successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    /**
     * Admin: delete review
     */
    @PostMapping("/admin/review/{id}/delete")
    public String deleteReview(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewFileService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
} 