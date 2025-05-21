package com.Mathra.Salon.datastructures;

import com.Mathra.Salon.model.Booking;

/**
 * Custom Queue implementation for managing salon appointments.
 * This is a circular array-based queue implementation.
 */
public class AppointmentQueue {
    private static final int DEFAULT_CAPACITY = 100;
    private Booking[] queue;
    private int front;
    private int rear;
    private int size;
    private int capacity;

    public AppointmentQueue() {
        this(DEFAULT_CAPACITY);
    }

    public AppointmentQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new Booking[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    /**
     * Adds a booking to the end of the queue
     * @param booking The booking to add
     * @return true if the booking was added successfully
     */
    public boolean enqueue(Booking booking) {
        if (isFull()) {
            return false;
        }
        rear = (rear + 1) % capacity;
        queue[rear] = booking;
        size++;
        return true;
    }

    /**
     * Removes and returns the booking at the front of the queue
     * @return The booking at the front of the queue, or null if the queue is empty
     */
    public Booking dequeue() {
        if (isEmpty()) {
            return null;
        }
        Booking booking = queue[front];
        front = (front + 1) % capacity;
        size--;
        return booking;
    }

    /**
     * Returns the booking at the front of the queue without removing it
     * @return The booking at the front of the queue, or null if the queue is empty
     */
    public Booking peek() {
        if (isEmpty()) {
            return null;
        }
        return queue[front];
    }

    /**
     * Checks if the queue is empty
     * @return true if the queue is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks if the queue is full
     * @return true if the queue is full
     */
    public boolean isFull() {
        return size == capacity;
    }

    /**
     * Returns the current number of bookings in the queue
     * @return The size of the queue
     */
    public int size() {
        return size;
    }

    /**
     * Clears all bookings from the queue
     */
    public void clear() {
        front = 0;
        rear = -1;
        size = 0;
    }

    /**
     * Returns an array containing all bookings in the queue
     * @return An array of bookings
     */
    public Booking[] toArray() {
        Booking[] result = new Booking[size];
        for (int i = 0; i < size; i++) {
            result[i] = queue[(front + i) % capacity];
        }
        return result;
    }
} 