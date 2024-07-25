package com.toda.todamoon_v2.model;

public class PassengerGetterSetter {
    private String userId;
    private String name;
    private String email;
    private String profileUri;
    private String role;

    public PassengerGetterSetter() {

    }

    public PassengerGetterSetter(String userId, String name, String email, String profileUri, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileUri = profileUri;
        this.role = role;
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

    public String getProfileUri() {
        return profileUri;
    }
    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
