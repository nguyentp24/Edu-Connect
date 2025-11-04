package com.example.educonnect.model;

import com.example.educonnect.model.response.LoginResponse;

/**
 * Model đại diện cho thông tin người dùng trong app
 */
public class User {
    public String userId;
    public String fullName;
    public String email;
    public String role;
    
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
            response.userId,
            response.fullName,
            response.email,
            response.role
        );
    }
}
