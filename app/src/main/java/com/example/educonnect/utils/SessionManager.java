package com.example.educonnect.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "EduConnectSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Lưu thông tin đăng nhập
    public void saveLoginSession(String token, String userId, String fullName, String email, String role) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Kiểm tra đã đăng nhập chưa
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Lấy token
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    // Lấy userId
    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    // Lấy fullName
    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, null);
    }

    // Lấy email
    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    // Lấy role
    public String getRole() {
        return pref.getString(KEY_ROLE, null);
    }

    // Đăng xuất - xóa tất cả thông tin
    public void logout() {
        editor.clear();
        editor.apply();
    }
}

