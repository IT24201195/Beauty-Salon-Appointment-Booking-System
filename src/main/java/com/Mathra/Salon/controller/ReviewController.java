package com.Mathra.Salon.controller;

import com.Mathra.Salon.model.User;
import com.Mathra.Salon.service.ReviewFileService;
import com.Mathra.Salon.service.UserFileService;
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
        try {
            model.addAttribute("reviews", reviewFileService.findApprovedReviews());
            model.addAttribute("averageRating", reviewFileService.getAverageRating());
            return "reviews";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load reviews: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Show review form
     */
    @GetMapping("/review/new")
    public String showReviewForm(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            User user = userFileService.findUserByUsername(authentication.getName()); // Fixed typo
            if (user == null) {
                return "redirect:/login";
            }

            model.addAttribute("review", new Review());
            model.addAttribute("userReviews", reviewFileService.findReviewsByUser(user));
            return "review-form";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load review form: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Process review form
     */
    @PostMapping("/review/submit")
    public String submitReview(@ModelAttribute Review review, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            User user = userFileService.findUserByUsername(authentication.getName());
            if (user == null) {
                return "redirect:/login";
            }

            review.setUser(user);
            reviewFileService.createReview(review);
            redirectAttributes.addFlashAttribute("success", "Your review has been submitted and is pending approval.");
            return "redirect:/review/new";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/review/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit review: " + e.getMessage());
            return "redirect:/review/new";
        }
    }

    /**
     * Show update review form
     */
    @GetMapping("/review/edit/{id}")
    public String showUpdateReviewForm(@PathVariable("id") Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            User user = userFileService.findUserByUsername(authentication.getName());
            if (user == null) {
                return "redirect:/login";
            }

            Review review = reviewFileService.findReviewById(id);
            if (review == null || !review.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own reviews.");
                return "redirect:/review/new";
            }

            if (review.isApproved()) {
                redirectAttributes.addFlashAttribute("error", "Approved reviews cannot be edited.");
                return "redirect:/review/new";
            }

            model.addAttribute("review", review);
            model.addAttribute("userReviews", reviewFileService.findReviewsByUser(user));
            return "review-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to load review form: " + e.getMessage());
            return "redirect:/review/new";
        }
    }

    /**
     * Process update review form
     */
    @PostMapping("/review/update/{id}")
    public String updateReview(@PathVariable("id") Long id, @ModelAttribute Review updatedReview, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            User user = userFileService.findUserByUsername(authentication.getName());
            if (user == null) {
                return "redirect:/login";
            }

            Review review = reviewFileService.findReviewById(id);
            if (review == null || !review.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only update your own reviews.");
                return "redirect:/review/new";
            }

            reviewFileService.updateReview(id, updatedReview, user);
            redirectAttributes.addFlashAttribute("success", "Your review has been updated and is pending approval.");
            return "redirect:/review/new";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/review/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update review: " + e.getMessage());
            return "redirect:/review/new";
        }
    }

    /**
     * Delete a review
     */
    @PostMapping("/review/delete/{id}")
    public String deleteUserReview(@PathVariable("id") Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        try {
            User user = userFileService.findUserByUsername(authentication.getName());
            if (user == null) {
                return "redirect:/login";
            }

            Review review = reviewFileService.findReviewById(id);
            if (review == null || !review.getUserId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only delete your own reviews.");
                return "redirect:/review/new";
            }

            reviewFileService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Your review has been deleted successfully.");
            return "redirect:/review/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete review: " + e.getMessage());
            return "redirect:/review/new";
        }
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