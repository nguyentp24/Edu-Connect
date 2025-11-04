package com.example.educonnect.ui.login;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.example.educonnect.databinding.ActivityLoginBinding;
import com.example.educonnect.ui.main.MainActivity;
import com.example.educonnect.api.ApiClient;
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
                            loginResponse.token,
                            loginResponse.userId,
                            loginResponse.fullName,
                            loginResponse.email,
                            loginResponse.role
                    );
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
