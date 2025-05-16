package com.mathra.salon.filemanager;

import com.mathra.salon.model.User;
import org.springframework.stereotype.Component;

/**
 * File storage manager for User objects
 * Handles reading and writing User objects to/from the users.txt file
 */
@Component
public class UserFileManager extends AbstractFileStorageManager<User> {
    
    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_NAME = "users.txt";
    
    /**
     * Constructor
     * Initializes with the data directory and file name
     */
    public UserFileManager() {
        super(DATA_DIRECTORY, FILE_NAME);
    }
    
    /**
     * Get the ID from a User object
     * @param user The User object
     * @return The ID of the user
     */
    @Override
    protected Long getIdFromItem(User user) {
        return user.getId();
    }
    
    /**
     * Create a new User from a file string
     * @param fileString The string from the file
     * @return A new User with data from the string
     */
    @Override
    protected User createFromFileString(String fileString) {
        User user = new User();
        user.fromFileString(fileString);
        return user;
    }
    
    /**
     * Save a new user, assigning an ID if needed
     * @param user The user to save
     * @return true if successful, false otherwise
     */
    @Override
    public boolean save(User user) {
        if (user.getId() == null) {
            user.setId(getNextId());
        }
        return super.save(user);
    }
    
    /**
     * Find a user by username
     * @param username The username to search for
     * @return The matching User or null if not found
     */
    public User findByUsername(String username) {
        if (username == null) {
            return null;
        }
        
        return findAll().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Find a user by email
     * @param email The email to search for
     * @return The matching User or null if not found
     */
    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        
        return findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if a username exists
     * @param username The username to check
     * @return true if exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }
    
    /**
     * Check if an email exists
     * @param email The email to check
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }
} 