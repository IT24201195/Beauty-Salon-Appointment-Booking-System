package com.Mathra.Salon.datastructures;

import com.Mathra.Salon.model.Booking;

import java.time.LocalTime;

public class AppointmentSorter {
    

    public static void quickSort(Booking[] bookings) {
        if (bookings == null || bookings.length <= 1) {
            return;
        }
        quickSort(bookings, 0, bookings.length - 1);
    }

    private static void quickSort(Booking[] bookings, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(bookings, low, high);
            quickSort(bookings, low, pivotIndex - 1);
            quickSort(bookings, pivotIndex + 1, high);
        }
    }

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

    private static void swap(Booking[] bookings, int i, int j) {
        Booking temp = bookings[i];
        bookings[i] = bookings[j];
        bookings[j] = temp;
    }

    public static Booking[] sortQueue(AppointmentQueue queue) {
        Booking[] bookings = queue.toArray();
        quickSort(bookings);
        return bookings;
    }
} 
