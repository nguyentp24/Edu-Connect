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
import com.example.educonnect.model.LoginRequest;
import com.example.educonnect.model.Teacher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding vb;
    private ApiClient.ApiService apiService;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        apiService = ApiClient.service();
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

        apiService.login(new LoginRequest(email, pass)).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();

                    JsonElement tokenElement = body.get("token");
                    if (tokenElement == null || tokenElement.isJsonNull()) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi: Login không trả về token", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String authToken = "Bearer " + tokenElement.getAsString();

                    JsonElement userIdElement = body.get("userId");
                    if (userIdElement == null || userIdElement.isJsonNull()) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi: Login không trả về userId", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String userId = userIdElement.getAsString();

                    fetchTeacherInfo(authToken, userId);

                } else {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi mạng (Login): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTeacherInfo(String authToken, String userId) {
        apiService.getTeacherInfo(userId, authToken).enqueue(new Callback<Teacher>() {
            @Override
            public void onResponse(Call<Teacher> call, Response<Teacher> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Teacher teacher = response.body();
                    String realTeacherId = teacher.getTeacherId();
                    String teacherName = teacher.getFirstName() + " " + teacher.getLastName();

                    if (realTeacherId == null || realTeacherId.isEmpty()) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi: Không thể lấy teacherId từ User", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    fetchClassId(authToken, realTeacherId, userId, teacherName);
                } else {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Lỗi: Không thể lấy thông tin Teacher (ID: " + userId + ")", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Teacher> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi mạng (GetTeacher): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchClassId(String authToken, String teacherId, String userId, String teacherName) {

        apiService.getClassroomByTeacherId(teacherId, authToken).enqueue(new Callback<List<Classroom>>() {
            @Override
            public void onResponse(Call<List<Classroom>> call, Response<List<Classroom>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Classroom classroom = response.body().get(0);
                    String classId = classroom.getClassId();
                    String className = classroom.getClassName();

                    if(classId == null || classId.isEmpty() || className == null || className.isEmpty()){
                        Toast.makeText(LoginActivity.this, "Lỗi: Lớp học trả về thiếu classId hoặc className.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences("EduConnectApp", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("token", authToken);
                    editor.putString("classId", classId);
                    editor.putString("userId", userId);
                    editor.putString("teacherId", teacherId);
                    editor.putString("teacherName", teacherName); // <-- Đã thêm
                    editor.putString("className", className);     // <-- Đã thêm
                    editor.apply();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, "Lỗi: Không tìm thấy lớp nào cho giáo viên này (TID: " + teacherId + ")", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<Classroom>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi mạng (GetClass): " + t.getMessage(), Toast.LENGTH_SHORT).show();
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