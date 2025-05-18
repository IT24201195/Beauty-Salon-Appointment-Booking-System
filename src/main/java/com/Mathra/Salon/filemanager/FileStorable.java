package com.Mathra.Salon.filemanager;

/**
 * Interface for objects that can be stored in files
 */
public interface FileStorable {
    /**
     * Convert the object to a string representation for file storage
     * @return A string representation of the object
     */
    String toFileString();

    /**
     * Initialize the object from a string representation from a file
     * @param fileString The string representation from the file
     */
    void fromFileString(String fileString);
}