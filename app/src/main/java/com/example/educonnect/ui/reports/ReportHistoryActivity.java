package com.example.educonnect.ui.reports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educonnect.R;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.model.Term;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.educonnect.model.Report;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReportHistoryAdapter adapter;
    private List<Report> reportList = new ArrayList<>();
    private String authToken;
    private String classId;

    private boolean justCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_history);

        justCreated = getIntent().getBooleanExtra("JUST_CREATED", false);

        SharedPreferences prefs = getSharedPreferences("EduConnectApp", Context.MODE_PRIVATE);
        authToken = prefs.getString("token", null);
        classId = prefs.getString("classId", null);

        if (authToken == null || classId == null) {
            Toast.makeText(this, "Lỗi: Phiên đăng nhập hết hạn.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view_reports);

        adapter = new ReportHistoryAdapter(reportList, report -> {
            Intent intent = new Intent(ReportHistoryActivity.this, ReportDetailActivity.class);
            intent.putExtra("REPORT_BASIC_INFO", report);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchReportHistoryWithDelay();
    }

    private void fetchReportHistoryWithDelay() {
        int delay = justCreated ? 3000 : 0;

        if (justCreated) {
            justCreated = false;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            callGetReportHistoryApi();
        }, delay);
    }

    private void callGetReportHistoryApi() {
        Call<List<Report>> call = ApiClient.service().getReportHistory(
                classId,
                authToken,
                System.currentTimeMillis()
        );

        Log.d("ReportHistory", "Đang gọi API: " + call.request().url());

        call.enqueue(new Callback<List<Report>>() {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ReportHistory", "API trả về " + response.body().size() + " báo cáo.");
                    List<Report> basicReports = response.body();
                    if (basicReports.isEmpty()) {
                        Toast.makeText(ReportHistoryActivity.this, "Không có lịch sử báo cáo", Toast.LENGTH_SHORT).show();
                        reportList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    fetchTermDetailsForList(basicReports);
                } else {
                    Log.e("ReportHistory", "Lỗi gọi API: " + response.code() + " - " + response.message());
                    Toast.makeText(ReportHistoryActivity.this, "Không thể tải lịch sử: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Report>> call, Throwable t) {
                Log.e("ReportHistory", "Lỗi mạng", t);
                Toast.makeText(ReportHistoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTermDetailsForList(List<Report> basicReports) {
        Report[] orderedReports = new Report[basicReports.size()];
        AtomicInteger counter = new AtomicInteger(basicReports.size());

        reportList.clear();

        if (basicReports.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (int i = 0; i < basicReports.size(); i++) {
            final int index = i;
            Report report = basicReports.get(i);

            ApiClient.service().getTermDetails(report.getTermId(), authToken).enqueue(new Callback<Term>() {
                @Override
                public void onResponse(Call<Term> call, Response<Term> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        report.setCreatedAt(response.body().getCreatedAt());
                        orderedReports[index] = report;
                    } else {
                        orderedReports[index] = report;
                    }

                    if (counter.decrementAndGet() == 0) {
                        reportList.clear();
                        for (Report r : orderedReports) {
                            if (r != null) {
                                reportList.add(r);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<Term> call, Throwable t) {
                    orderedReports[index] = report;
                    if (counter.decrementAndGet() == 0) {
                        reportList.clear();
                        for (Report r : orderedReports) {
                            if (r != null) {
                                reportList.add(r);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}