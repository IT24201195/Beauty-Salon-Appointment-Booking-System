package com.Mathra.Salon.filemanager;

/**
 * Interface for objects that can be stored in a file
 * Implementations should provide methods to convert to/from a string
 */
public interface FileStorable {
    
    /**
     * Convert the object to a string for file storage
     * @return A string representation of the object for file storage
     */
    String toFileString();
    
    /**
     * Populate the object from a file string
     *
     * @param fileString String representation from the file
     */
    void fromFileString(String fileString);
    
    /**
     * Get the ID of the object
     * @return The ID
     */
    Long getId();
    
    /**
     * Set the ID of the object
     * 
     * @param id The ID to set
     */
    void setId(Long id);
} 