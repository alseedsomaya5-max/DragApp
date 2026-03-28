package com.example.dragapp.model;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String userId; // Required by Appwrite schema
    private String name;
    private String email;
    private String phone;
    private String password;
    private String dateOfBirth;
    private String idNumber;
    private String photoUrl;
    private String userType = "user"; // Fixed value for Appwrite schema
    private long createdAt;

    public User() {
        this.createdAt = System.currentTimeMillis();
    }

    public User(String name, String email, String phone, String password) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public User(String name, String phone, String password, String dateOfBirth, String idNumber) {
        this();
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.idNumber = idNumber;
        this.email = phone + "@medicines.app"; // default email
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    
    // Convenience methods
    public String getFullName() {
        return name;
    }

    public void setFullName(String fullName) {
        this.name = fullName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", userType='" + userType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
