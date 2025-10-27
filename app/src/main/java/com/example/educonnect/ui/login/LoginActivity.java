package com.example.educonnect.ui.login;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.educonnect.databinding.ActivityLoginBinding;
import com.example.educonnect.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding vb;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        vb.btnSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
