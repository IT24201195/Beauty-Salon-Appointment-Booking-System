package com.Mathra.Salon.datastructures;

import com.Mathra.Salon.model.Booking;

import java.time.LocalTime;

/**
 * Utility class for sorting salon appointments using Quick Sort algorithm.
 * This implementation sorts appointments by their time slots.
 */
public class AppointmentSorter {
    
    /**
     * Sorts an array of bookings by their time slots using Quick Sort
     * @param bookings The array of bookings to sort
     */
    public static void quickSort(Booking[] bookings) {
        if (bookings == null || bookings.length <= 1) {
            return;
        }
        quickSort(bookings, 0, bookings.length - 1);
    }

    /**
     * Recursive Quick Sort implementation
     * @param bookings The array of bookings to sort
     * @param low The starting index
     * @param high The ending index
     */
    private static void quickSort(Booking[] bookings, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(bookings, low, high);
            quickSort(bookings, low, pivotIndex - 1);
            quickSort(bookings, pivotIndex + 1, high);
        }
    }

    /**
     * Partitions the array around a pivot element
     * @param bookings The array of bookings to partition
     * @param low The starting index
     * @param high The ending index
     * @return The index of the pivot element
     */
    private static int partition(Booking[] bookings, int low, int high) {
        LocalTime pivot = bookings[high].getBookingTime();
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (bookings[j].getBookingTime().compareTo(pivot) <= 0) {
                i++;
                swap(bookings, i, j);
            }
        }

        swap(bookings, i + 1, high);
        return i + 1;
    }

    /**
     * Swaps two elements in the array
     * @param bookings The array of bookings
     * @param i The index of the first element
     * @param j The index of the second element
     */
    private static void swap(Booking[] bookings, int i, int j) {
        Booking temp = bookings[i];
        bookings[i] = bookings[j];
        bookings[j] = temp;
    }

    /**
     * Sorts a queue of appointments by their time slots
     * @param queue The queue of appointments to sort
     * @return A new sorted array of bookings
     */
    public static Booking[] sortQueue(AppointmentQueue queue) {
        Booking[] bookings = queue.toArray();
        quickSort(bookings);
        return bookings;
    }
} 