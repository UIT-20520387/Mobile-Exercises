package com.example.listviewex;

public class Contact {
    private int id; // Dùng để sắp xếp tăng/giảm
    private String name;
    private String phoneNumber;

    public Contact(int id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
}
