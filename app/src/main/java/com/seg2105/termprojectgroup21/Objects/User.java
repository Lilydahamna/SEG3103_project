package com.seg2105.termprojectgroup21.Objects;

public class User {
    String id;
    String username;
    String role;

    public User(String id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
