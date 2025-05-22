package com.Mathra.Salon.datastructures;

import com.Mathra.Salon.model.Booking;

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

    public boolean enqueue(Booking booking) {
        if (isFull()) {
            return false;
        }
        rear = (rear + 1) % capacity;
        queue[rear] = booking;
        size++;
        return true;
    }

    public Booking dequeue() {
        if (isEmpty()) {
            return null;
        }
        Booking booking = queue[front];
        front = (front + 1) % capacity;
        size--;
        return booking;
    }

    public Booking peek() {
        if (isEmpty()) {
            return null;
        }
        return queue[front];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = 0;
        rear = -1;
        size = 0;
    }

    public Booking[] toArray() {
        Booking[] result = new Booking[size];
        for (int i = 0; i < size; i++) {
            result[i] = queue[(front + i) % capacity];
        }
        return result;
    }
} 
