package com.example.educonnect.ui.reports;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.educonnect.R;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.databinding.ActivityReportDetailBinding;
import com.example.educonnect.model.ReportBotRequest;
import com.example.educonnect.model.Term;
import com.example.educonnect.model.Report;
import com.example.educonnect.utils.SessionManager; // <-- THÊM IMPORT NÀY

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailActivity extends AppCompatActivity {

    private ActivityReportDetailBinding vb;
    private Report basicReport;
    private String authToken;
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("d 'tháng' M, yyyy", new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityReportDetailBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // === THAY THẾ SharedPreferences BẰNG SessionManager ===
        SessionManager sessionManager = new SessionManager(this);
        authToken = sessionManager.getToken();

        if (authToken == null) {
            Toast.makeText(this, "Lỗi: Phiên đăng nhập hết hạn.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        basicReport = (Report) getIntent().getSerializableExtra("REPORT_BASIC_INFO");
        if (basicReport == null) {
            Toast.makeText(this, "Lỗi tải dữ liệu báo cáo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        vb.toolbarDetail.setNavigationOnClickListener(v -> finish());
        vb.toolbarDetail.setTitle(basicReport.getTitle());

        populateBasicData();
        fetchTermDetails();
        fetchReportStats();

        vb.btnGoBack.setOnClickListener(v -> finish());
        vb.btnSendReport.setOnClickListener(v -> {
            Toast.makeText(this, "Đang gửi báo cáo...", Toast.LENGTH_SHORT).show();
        });
    }

    private void populateBasicData() {
        vb.tvDetailTitle.setText(basicReport.getTitle());
        vb.tvDetailClass.setText(basicReport.getClassName());
        vb.tvDetailTeacher.setText(basicReport.getTeacherName());

        vb.tvDetailCreatedDate.setVisibility(View.GONE);
        vb.tvDetailType.setVisibility(View.GONE);
        vb.tvDetailStartDate.setVisibility(View.GONE);
        vb.tvDetailEndDate.setVisibility(View.GONE);
        vb.tvStatStudents.setVisibility(View.GONE);
        vb.tvStatLessons.setVisibility(View.GONE);
        vb.tvStatPoints.setVisibility(View.GONE);
        vb.tvStatAbsences.setVisibility(View.GONE);
    }

    // === FIX LỖI 401: Thêm tiền tố "Bearer " ===
    private void fetchTermDetails() {
        String termId = basicReport.getTermId();
        if (termId == null) return;

        // Thêm tiền tố "Bearer "
        Call<Term> call = ApiClient.service().getTermDetails(termId, "Bearer " + authToken);
        call.enqueue(new Callback<Term>() {
            @Override
            public void onResponse(Call<Term> call, Response<Term> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Term term = response.body();
                    vb.tvDetailType.setText(term.getMode());
                    vb.tvDetailStartDate.setText(formatDisplayDate(term.getStartTime()));
                    vb.tvDetailEndDate.setText(formatDisplayDate(term.getEndTime()));
                    vb.tvDetailCreatedDate.setText(formatDisplayDate(term.getCreatedAt()));

                    vb.tvDetailType.setVisibility(View.VISIBLE);
                    vb.tvDetailStartDate.setVisibility(View.VISIBLE);
                    vb.tvDetailEndDate.setVisibility(View.VISIBLE);
                    vb.tvDetailCreatedDate.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<Term> call, Throwable t) {
                Log.e("ReportDetailActivity", "Lỗi tải Term: " + t.getMessage());
            }
        });
    }

    // === FIX LỖI 401: Thêm tiền tố "Bearer " ===
    private void fetchReportStats() {
        String termId = basicReport.getTermId();
        String classId = basicReport.getClassId();
        if (termId == null || classId == null) return;

        ReportBotRequest botRequest = new ReportBotRequest(termId, classId);
        // Thêm tiền tố "Bearer "
        Call<String> call = ApiClient.service().generateReportDetails(botRequest, "Bearer " + authToken);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String plainTextResponse = response.body();

                    vb.tvStatStudents.setText(parseStat(plainTextResponse, "Tổng số học sinh:"));
                    vb.tvStatLessons.setText(parseStat(plainTextResponse, "Số tiết học đã diễn ra:"));
                    vb.tvStatPoints.setText(parseStat(plainTextResponse, "Số lượng điểm đã nhập:"));
                    vb.tvStatAbsences.setText(parseStat(plainTextResponse, "Tổng lượt điểm danh:"));

                    vb.tvStatStudents.setVisibility(View.VISIBLE);
                    vb.tvStatLessons.setVisibility(View.VISIBLE);
                    vb.tvStatPoints.setVisibility(View.VISIBLE);
                    vb.tvStatAbsences.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ReportDetailActivity.this, "Không thể tải chi tiết thống kê", Toast.LENGTH_SHORT).show();
                    Log.e("ReportDetailActivity", "Lỗi thống kê: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ReportDetailActivity.this, "Lỗi mạng (thống kê): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ... (formatDisplayDate và parseStat giữ nguyên) ...

    private String formatDisplayDate(String apiDate) {
        if (apiDate == null) return "N/A";
        try {
            Date date = apiDateFormat.parse(apiDate);
            return displayDateFormat.format(date);
        } catch (ParseException e) {
            return apiDate;
        }
    }

    private String parseStat(String fullText, String prefix) {
        if (fullText.contains(prefix)) {
            try {
                int startIndex = fullText.indexOf(prefix);
                int endIndex = fullText.indexOf("\n", startIndex);
                if (endIndex == -1) endIndex = fullText.length();

                String line = fullText.substring(startIndex, endIndex);

                String value = line.replace(prefix, "");
                value = value.replaceAll("[\\p{So}]", "").trim();

                return value.isEmpty() ? "0" : value;
            } catch (Exception e) {
                return "N/A";
            }
        }
        return "N/A";
    }
}