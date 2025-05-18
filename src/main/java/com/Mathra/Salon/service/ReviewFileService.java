package com.Mathra.Salon.service;

import com.Mathra.Salon.model.Review;
import com.Mathra.Salon.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


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

     
        review.setId(nextId++);
        review.setCreatedAt(LocalDateTime.now());
        review.setApproved(false);

        reviews.add(review);
        try {
            saveReviews();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save review: " + e.getMessage(), e);
        }

        return review;
    }

    
    public void updateReview(Long id, Review updatedReview, User user) {
        Review existingReview = findReviewById(id);
        if (existingReview == null) {
            throw new IllegalArgumentException("Review not found");
        }
        if (!existingReview.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own reviews");
        }
        if (existingReview.isApproved()) {
            throw new IllegalArgumentException("Approved reviews cannot be updated");
        }
        if (updatedReview.getRating() < 1 || updatedReview.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (updatedReview.getComment() == null || updatedReview.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

       
        existingReview.setRating(updatedReview.getRating());
        existingReview.setComment(updatedReview.getComment());
        existingReview.setCreatedAt(LocalDateTime.now()); 
        existingReview.setApproved(false); 

        try {
            saveReviews();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save reviews: " + e.getMessage(), e);
        }
    }

    
    public List<Review> findAllReviews() {
        return new ArrayList<>(reviews);
    }

 
    public List<Review> findApprovedReviews() {
        return reviews.stream()
                .filter(Review::isApproved)
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

   
    public List<Review> findReviewsByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        return reviews.stream()
                .filter(review -> review.getUserId() != null && review.getUserId().equals(user.getId()))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

   
    public Review findReviewById(Long id) {
        return reviews.stream()
                .filter(review -> review.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

   
    public void approveReview(Long id, boolean approved) {
        Review review = findReviewById(id);
        if (review != null) {
            review.setApproved(approved);
            try {
                saveReviews();
            } catch (IOException e) {
                throw new RuntimeException("Failed to save reviews: " + e.getMessage(), e);
            }
        }
    }

  
    public void deleteReview(Long id) {
        reviews.removeIf(review -> review.getId().equals(id));
        try {
            saveReviews();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save reviews: " + e.getMessage(), e);
        }
    }

   
    public double getAverageRating() {
        if (reviews.isEmpty() || findApprovedReviews().isEmpty()) {
            return 0.0;
        }

        return findApprovedReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }


    private void saveReviews() throws IOException {
        Path filePath = Paths.get(REVIEWS_FILE);
        Files.createDirectories(filePath.getParent());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(reviews);
            oos.writeObject(nextId);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadReviews() {
        Path filePath = Paths.get(REVIEWS_FILE);
        try {
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                reviews = new ArrayList<>();
                return;
            }
        } catch (IOException e) {
            reviews = new ArrayList<>();
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            reviews = (List<Review>) ois.readObject();
            nextId = (Long) ois.readObject();

          
            for (Review review : reviews) {
                if (review.getUserId() != null) {
                    try {
                        User user = userFileService.findUserById(review.getUserId());
                        review.setUser(user);
                    } catch (Exception e) {
                      
                        review.setUser(null);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
       
            reviews = new ArrayList<>();
            System.err.println("Warning: Failed to load reviews, starting with empty list: " + e.getMessage());
        }
    }
}
