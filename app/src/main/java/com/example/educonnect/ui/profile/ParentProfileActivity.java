package com.example.educonnect.ui.profile;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educonnect.R;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.model.Parent;
import com.example.educonnect.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentProfileActivity extends AppCompatActivity {

    // Key để truyền studentId qua Intent, giúp code dễ đọc và tránh lỗi chính tả
    public static final String EXTRA_STUDENT_ID = "EXTRA_STUDENT_ID";

    private TextView tvParentName, tvParentEmail, tvParentPhone;
    private ProgressBar progressBar;
    private LinearLayout infoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);


        // 1. Ánh xạ các thành phần giao diện từ file XML
        setupViews();

        // 2. Lấy dữ liệu cần thiết: studentId từ Intent và authToken từ bộ nhớ
        String studentId = getIntent().getStringExtra(EXTRA_STUDENT_ID);
        SessionManager sm = new SessionManager(this);
        String authToken = sm.getToken(); // Lấy token của giáo viên đã đăng nhập

        // 3. Kiểm tra dữ liệu đầu vào
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID học sinh", Toast.LENGTH_LONG).show();
            finish(); // Đóng activity nếu không có studentId
            return;
        }
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập", Toast.LENGTH_LONG).show();
            // Tại đây bạn có thể chuyển người dùng đến màn hình Đăng nhập
            finish(); // Đóng activity nếu không có token
            return;
        }

        // 4. Gọi API trực tiếp
        fetchParentProfile(authToken, studentId);
    }

    private void setupViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        // Thiết lập nút quay lại trên toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        tvParentName = findViewById(R.id.tv_parent_name);
        tvParentEmail = findViewById(R.id.tv_parent_email);
        tvParentPhone = findViewById(R.id.tv_parent_phone);
        progressBar = findViewById(R.id.progress_bar);
        infoContainer = findViewById(R.id.info_container);

        // Thiết lập trạng thái ban đầu: hiện loading, ẩn nội dung
        progressBar.setVisibility(View.VISIBLE);
        infoContainer.setVisibility(View.GONE);
    }

    /**
     * Hàm gọi API trực tiếp từ Activity sử dụng ApiClient của bạn.
     * @param authToken Token của giáo viên.
     * @param studentId ID của học sinh.
     */
    private void fetchParentProfile(String authToken, String studentId) {
        // Lấy service từ ApiClient của bạn
        ApiClient.ApiService apiService = ApiClient.service();

        // Tạo yêu cầu API
        Call<Parent> call = apiService.getParentProfileForTeacher("Bearer " + authToken, studentId);

        // Thực thi yêu cầu bất đồng bộ
        call.enqueue(new Callback<Parent>() {
            @Override
            public void onResponse(@NonNull Call<Parent> call, @NonNull Response<Parent> response) {
                // Luôn ẩn ProgressBar khi có kết quả (thành công hoặc thất bại)
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // Nếu API trả về thành công và có dữ liệu
                    infoContainer.setVisibility(View.VISIBLE);
                    displayProfile(response.body());
                } else {
                    // Nếu API trả về lỗi (404, 500,...)
                    Toast.makeText(ParentProfileActivity.this, "Không thể tải thông tin phụ huynh. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Parent> call, @NonNull Throwable t) {
                // Luôn ẩn ProgressBar khi có lỗi
                progressBar.setVisibility(View.GONE);

                // Nếu có lỗi mạng hoặc lỗi trong quá trình thực thi request
                Toast.makeText(ParentProfileActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProfile(Parent parent) {
        tvParentName.setText(parent.getFullName());
        tvParentEmail.setText(parent.getEmail());
        tvParentPhone.setText(parent.getPhoneNumber());
    }

    /**
     * Hàm lấy token đã được lưu sau khi đăng nhập.
     * Bạn cần thay thế bằng logic thực tế của mình (ví dụ: dùng SharedPreferences).
     */
    private String getTokenFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", MODE_PRIVATE);
        return prefs.getString("AUTH_TOKEN", null);
        // return "your_hardcoded_token_for_testing"; // Chỉ dùng để test
    }
}
