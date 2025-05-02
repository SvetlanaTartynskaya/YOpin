package com.example.yopin;

public class User {
    private int id;
    private String email;
    private String fullName;
    private String birthDate;

    public User(int id, String email, String fullName, String birthDate) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.birthDate = birthDate;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
}