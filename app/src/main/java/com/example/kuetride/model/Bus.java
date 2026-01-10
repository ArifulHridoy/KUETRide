package com.example.kuetride.model;

public class Bus {
    private String id;
    private String number;
    private int capacity;

    public Bus(String id, String number, int capacity) {
        this.id = id;
        this.number = number;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public int getCapacity() {
        return capacity;
    }
}
