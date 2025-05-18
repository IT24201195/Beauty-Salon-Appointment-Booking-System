package com.Mathra.Salon.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId; 
    private transient User user; 
    private int rating; 
    private String comment;
    private LocalDateTime createdAt;
    private boolean approved;

    public Review() {
        this.createdAt = LocalDateTime.now();
        this.approved = false;
    }

    public Review(User user, int rating, String comment) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
        this.approved = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1) {
            this.rating = 1;
        } else if (rating > 5) {
            this.rating = 5;
        } else {
            this.rating = rating;
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", approved=" + approved +
                '}';
    }
} 
