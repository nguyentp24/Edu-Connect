package com.example.educonnect.model.request;

/**
 * Model request cho API đăng nhập
 */
public class LoginRequest {
    public String email;
    public String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

