package com.example.educonnect.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.example.educonnect.databinding.ActivityLoginBinding;
import com.example.educonnect.ui.main.MainActivity;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.model.Classroom;
import com.example.educonnect.model.Teacher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import com.example.educonnect.model.request.LoginRequest;
import com.example.educonnect.model.response.LoginResponse;
import com.example.educonnect.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding vb;
    private SessionManager sessionManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        sessionManager = new SessionManager(this);
        vb.btnSignIn.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = vb.edtEmail.getText().toString().trim();
        String pass  = vb.edtPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);

        ApiClient.ApiService api = ApiClient.service();

        api.login(new LoginRequest(email, pass)).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    // Lưu thông tin đăng nhập vào SharedPreferences
                    sessionManager.saveLoginSession(
                            loginResponse.getToken(),
                            loginResponse.getUserId(),
                            loginResponse.getFullName(),
                            loginResponse.getEmail(),
                            loginResponse.getRole()
                    );
                    // Sau khi login thành công -> gọi API lấy thông tin Teacher
                    fetchTeacherAndGo(loginResponse.getUserId(), loginResponse.getToken());
                } else {
                    Toast.makeText(LoginActivity.this, "Lỗi: Không tìm thấy lớp nào cho giáo viên này (TID: " + teacherId + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi mạng (GetClass): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTeacherAndGo(String userId, String token) {
        ApiClient.ApiService api = ApiClient.service();
        api.getTeacher(userId, "Bearer " + token).enqueue(new retrofit2.Callback<com.example.educonnect.model.Teacher>() {
            @Override public void onResponse(retrofit2.Call<com.example.educonnect.model.Teacher> call, retrofit2.Response<com.example.educonnect.model.Teacher> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.educonnect.model.Teacher t = response.body();
                    sessionManager.saveTeacher(t.getTeacherId(), t.getPhoneNumber(), t.getUserImage());
                    // Sau khi lấy teacher -> lấy luôn danh sách courses theo teacherId và lưu
                    fetchCoursesByTeacherAndGo(t.getTeacherId(), token);
                    return;
                }
                // fallback
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override public void onFailure(retrofit2.Call<com.example.educonnect.model.Teacher> call, Throwable t) {
                // Nếu lỗi, vẫn cho vào app với thông tin login đã có
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void fetchCoursesByTeacherAndGo(String teacherId, String token) {
        ApiClient.ApiService api = ApiClient.service();
        api.getCoursesByTeacher(teacherId, "Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<com.example.educonnect.model.Course>>() {
            @Override public void onResponse(retrofit2.Call<java.util.List<com.example.educonnect.model.Course>> call, retrofit2.Response<java.util.List<com.example.educonnect.model.Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    String json = gson.toJson(response.body());
                    sessionManager.saveCoursesJson(json);
                }
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override public void onFailure(retrofit2.Call<java.util.List<com.example.educonnect.model.Course>> call, Throwable t) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void setLoading(boolean loading) {
        vb.btnSignIn.setEnabled(!loading);
        vb.btnGoogle.setEnabled(!loading);
        vb.edtEmail.setEnabled(!loading);
        vb.edtPassword.setEnabled(!loading);
        vb.loadingOverlay.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}