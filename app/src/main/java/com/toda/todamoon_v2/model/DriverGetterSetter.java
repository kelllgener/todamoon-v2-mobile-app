package com.toda.todamoon_v2.model;

public class DriverGetterSetter {
    public String uid;
    public String profileImage;
    public String name;
    public String barangay;
    public String tricycleNumber;
    public String email;
    public String phoneNumber;
    public String password;
    public String confirmPassword;
    public String plateNumber;
    public double balance;
    public String role;
    public boolean inQueue;

    // Constructor
    public DriverGetterSetter(String uid,
                              String profileImage,
                              String name,
                              String barangay,
                              String tricycleNumber,
                              String email,
                              String phoneNumber,
                              String password,
                              String confirmPassword,
                              String plateNumber,
                              double balance,
                              String role,
                              boolean inQueue) {
        this.uid = uid;
        this.profileImage = profileImage;
        this.name = name;
        this.barangay = barangay;
        this.tricycleNumber = tricycleNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.plateNumber = plateNumber;
        this.balance = balance;
        this.role = role;
        this.inQueue = inQueue;
    }

    // Default constructor for Firebase
    public DriverGetterSetter() {}

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() { return name; }
    public void setName(String Name) { this.name = Name; }
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }

    public String getTricycleNumber() { return tricycleNumber; }
    public void setTricycleNumber(String tricycleNumber) { this.tricycleNumber = tricycleNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public double getBalance() {
        return balance;
    }
    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isInQueue() { return inQueue; }
    public void setInQueue(boolean inQueue) { this.inQueue = inQueue; }
}
