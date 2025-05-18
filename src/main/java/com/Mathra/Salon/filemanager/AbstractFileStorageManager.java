package com.Mathra.Salon.filemanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for file-based storage implementations
 * @param <T> The type of object to store, must implement FileStorable
 */
public abstract class AbstractFileStorageManager<T extends FileStorable> {

    private final Logger logger = LoggerFactory.getLogger(AbstractFileStorageManager.class);
    private final String dataDirectory;
    private final String fileName;
    private final AtomicLong idSequence = new AtomicLong(1);

    /**
     * Constructor
     * @param dataDirectory The directory to store files in
     * @param fileName The name of the file to store data in
     */
    protected AbstractFileStorageManager(String dataDirectory, String fileName) {
        this.dataDirectory = dataDirectory;
        this.fileName = fileName;
        initializeStorage();
    }

    /**
     * Initialize the storage - create directory and file if needed
     */
    private void initializeStorage() {
        try {
            Path dirPath = Paths.get(dataDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                logger.info("Created directory: {}", dirPath);
            }

            Path filePath = Paths.get(dataDirectory, fileName);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                logger.info("Created file: {}", filePath);
            } else {
                // If file exists, initialize ID sequence to max id + 1
                long maxId = findAll().stream()
                        .map(this::getIdFromItem)
                        .filter(id -> id != null)
                        .mapToLong(Long::longValue)
                        .max()
                        .orElse(0);
                idSequence.set(maxId + 1);
                logger.info("Initialized ID sequence to: {}", maxId + 1);
            }
        } catch (IOException e) {
            logger.error("Failed to initialize storage: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize storage: " + e.getMessage(), e);
        }
    }

    /**
     * Get the next available ID
     * @return A new unique ID
     */
    protected Long getNextId() {
        return idSequence.getAndIncrement();
    }

    /**
     * Get the ID from an item
     * @param item The item
     * @return The ID
     */
    protected abstract Long getIdFromItem(T item);

    /**
     * Create an item from a file string
     * @param fileString The string from the file
     * @return The created item
     */
    protected abstract T createFromFileString(String fileString);

    /**
     * Find all items
     * @return List of all items
     */
    public List<T> findAll() {
        List<T> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(dataDirectory + File.separator + fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    T item = createFromFileString(line);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
            logger.debug("Read {} items from file", items.size());
        } catch (IOException e) {
            logger.error("Error reading from file: {}", e.getMessage(), e);
            throw new RuntimeException("Error reading from file: " + e.getMessage(), e);
        }

        return items;
    }

    /**
     * Find an item by ID
     * @param id The ID
     * @return Optional containing the item, or empty if not found
     */
    public Optional<T> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return findAll().stream()
                .filter(item -> id.equals(getIdFromItem(item)))
                .findFirst();
    }

    /**
     * Save an item
     * @param item The item to save
     * @return true if successful, false otherwise
     */
    public boolean save(T item) {
        if (item == null) {
            logger.error("Cannot save null item");
            return false;
        }

        List<T> items = findAll();
        Long itemId = getIdFromItem(item);

        // Remove existing item with same ID if present
        if (itemId != null) {
            items.removeIf(existingItem -> itemId.equals(getIdFromItem(existingItem)));
        }

        // Add the new or updated item
        items.add(item);
        logger.debug("Saving item with ID: {}", itemId);

        // Write all items back to the file
        return writeToFile(items);
    }

    /**
     * Delete an item by ID
     * @param id The ID of the item to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteById(Long id) {
        if (id == null) {
            logger.error("Cannot delete item with null ID");
            return false;
        }

        List<T> items = findAll();
        boolean removed = items.removeIf(item -> id.equals(getIdFromItem(item)));

        if (removed) {
            logger.debug("Deleted item with ID: {}", id);
            return writeToFile(items);
        }

        logger.debug("Item with ID {} not found for deletion", id);
        return false;
    }

    /**
     * Write a list of items to the file
     * @param items The items to write
     * @return true if successful, false otherwise
     */
    private boolean writeToFile(List<T> items) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataDirectory + File.separator + fileName))) {
            for (T item : items) {
                writer.write(((FileStorable) item).toFileString());
                writer.newLine();
            }
            logger.debug("Saved {} items to file: {}", items.size(), fileName);
            return true;
        } catch (IOException e) {
            logger.error("Error writing to file: {}", e.getMessage(), e);
            throw new RuntimeException("Error writing to file: " + e.getMessage(), e);
        }
    }
}