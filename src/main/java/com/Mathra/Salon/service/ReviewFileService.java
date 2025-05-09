package com.Mathra.Salon.service;

import com.mathra.salon.model.Review;
import com.mathra.salon.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling review operations
 */
@Service
public class ReviewFileService {
    
    private static final String REVIEWS_FILE = "data/reviews.ser";
    private final UserFileService userFileService;
    
    private List<Review> reviews;
    private long nextId = 1;
    
    @Autowired
    public ReviewFileService(UserFileService userFileService) {
        this.userFileService = userFileService;
        this.reviews = new ArrayList<>();
        loadReviews();
    }
    
    /**
     * Create a new review
     */
    public Review createReview(Review review) {
        if (review.getUser() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        
        // Set ID and creation time
        review.setId(nextId++);
        review.setCreatedAt(LocalDateTime.now());
        review.setApproved(false);
        
        reviews.add(review);
        saveReviews();
        
        return review;
    }
    
    /**
     * Find all reviews
     */
    public List<Review> findAllReviews() {
        return new ArrayList<>(reviews);
    }
    
    /**
     * Find approved reviews
     */
    public List<Review> findApprovedReviews() {
        return reviews.stream()
                .filter(Review::isApproved)
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Find reviews by user
     */
    public List<Review> findReviewsByUser(User user) {
        return reviews.stream()
                .filter(review -> review.getUser().getId().equals(user.getId()))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Find review by ID
     */
    public Review findReviewById(Long id) {
        return reviews.stream()
                .filter(review -> review.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Update review approval status
     */
    public void approveReview(Long id, boolean approved) {
        Review review = findReviewById(id);
        if (review != null) {
            review.setApproved(approved);
            saveReviews();
        }
    }
    
    /**
     * Delete a review
     */
    public void deleteReview(Long id) {
        reviews.removeIf(review -> review.getId().equals(id));
        saveReviews();
    }
    
    /**
     * Calculate average rating
     */
    public double getAverageRating() {
        if (reviews.isEmpty() || findApprovedReviews().isEmpty()) {
            return 0.0;
        }
        
        return findApprovedReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Save reviews to file
     */
    private void saveReviews() {
        File file = new File(REVIEWS_FILE);
        file.getParentFile().mkdirs();
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(reviews);
            oos.writeObject(nextId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load reviews from file
     */
    @SuppressWarnings("unchecked")
    private void loadReviews() {
        File file = new File(REVIEWS_FILE);
        if (!file.exists()) {
            reviews = new ArrayList<>();
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            reviews = (List<Review>) ois.readObject();
            nextId = (Long) ois.readObject();
            
            // Reconnect users (they might have become detached)
            for (Review review : reviews) {
                if (review.getUser() != null) {
                    try {
                        User user = userFileService.findUserById(review.getUser().getId());
                        review.setUser(user);
                    } catch (Exception e) {
                        // If user cannot be found, set to null
                        review.setUser(null);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            reviews = new ArrayList<>();
        }
    }
} 