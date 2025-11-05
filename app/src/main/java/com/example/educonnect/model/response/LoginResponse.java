package com.example.educonnect.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Model response từ API đăng nhập
 */
public class LoginResponse {
    @SerializedName("token")
    public String token;
    
    @SerializedName("userId")
    public String userId;
    
    @SerializedName("fullName")
    public String fullName;
    
    @SerializedName("email")
    public String email;
    
    @SerializedName("role")
    public String role;
}

