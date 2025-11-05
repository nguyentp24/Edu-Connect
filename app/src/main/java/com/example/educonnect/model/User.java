package com.example.educonnect.model;

import com.example.educonnect.model.response.LoginResponse;

/**
 * Model đại diện cho thông tin người dùng trong app
 */
public class User {
    private String userId;
    private String fullName;
    private String email;
    private String role;
    
    public User(String userId, String fullName, String email, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
    
    /**
     * Tạo User từ LoginResponse
     */
    public static User fromLoginResponse(LoginResponse response) {
        return new User(
            response.getUserId(),
            response.getFullName(),
            response.getEmail(),
            response.getRole()
        );
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
