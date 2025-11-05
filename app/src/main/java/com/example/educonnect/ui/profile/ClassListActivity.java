package com.example.educonnect.ui.profile;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.educonnect.R;
import com.example.educonnect.adapter.ClassStudentAdapter;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.databinding.ActivityClassListBinding;
import com.example.educonnect.model.ClassroomStudent;
import com.example.educonnect.model.Student;
import com.example.educonnect.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassListActivity extends AppCompatActivity {

    private ActivityClassListBinding vb;
    private final List<ClassStudentAdapter.Student> displayStudents = new ArrayList<>();
    private final List<Student> fullStudents = new ArrayList<>();
    private ClassStudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityClassListBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Header từ Intent
        String klass = getIntent().getStringExtra("klass");
        String teacher = getIntent().getStringExtra("teacher");
        String year = getIntent().getStringExtra("year");

        vb.tvClass.setText(klass != null ? "Lớp: " + klass : "Lớp: 10A1");
        vb.tvTeacher.setText(teacher != null ? "GV chủ nhiệm: " + teacher : "GV chủ nhiệm: Nguyễn Văn A");
        vb.tvYear.setText(year != null ? "Năm học: " + year : "Năm học: 2025-2026");

        vb.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // RecyclerView
        vb.rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassStudentAdapter(displayStudents, fullStudents);
        vb.rvStudents.setAdapter(adapter);

        // Lấy classId thật từ Intent (hoặc tạm hardcode)
        String classId = getIntent().getStringExtra("classId");
        if (classId == null) classId = "class01";

        // Lấy token đăng nhập
        SessionManager sm = new SessionManager(this);
        String token = sm.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API lấy danh sách học sinh
        fetchStudentsFromApi(classId, token);

        // FAB thêm học sinh (vẫn giữ cho UI đẹp)
        vb.fabAdd.setOnClickListener(v -> showAddStudentDialog());
    }

    /** 🔹 Gọi API thật để lấy danh sách học sinh */
    private void fetchStudentsFromApi(String classId, String token) {
        // Hiển thị loading nếu bạn có ProgressBar trong layout
        // (nếu chưa có thì bỏ 2 dòng vb.progressBar này đi)
        // vb.progressBar.setVisibility(View.VISIBLE);

        ApiClient.ApiService api = ApiClient.service();
        api.getClassroomStudents(classId, "Bearer " + token)
                .enqueue(new Callback<List<ClassroomStudent>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ClassroomStudent>> call,
                                           @NonNull Response<List<ClassroomStudent>> response) {
                        // vb.progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<ClassroomStudent> list = response.body();
                            displayStudents.clear();
                            fullStudents.clear();

                            for (ClassroomStudent s : list) {
                                String dob = (s.getDateOfBirth() != null && !s.getDateOfBirth().isEmpty())
                                        ? s.getDateOfBirth()
                                        : "Chưa cập nhật";

                                displayStudents.add(new ClassStudentAdapter.Student(
                                        s.getFullName(),
                                        "Ngày sinh: " + dob
                                ));

                                fullStudents.add(new Student(
                                        s.getFullName(),
                                        s.getStudentId(),
                                        Student.Status.PRESENT
                                ));
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ClassListActivity.this,
                                    "Không thể tải danh sách học sinh (mã " + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ClassroomStudent>> call,
                                          @NonNull Throwable t) {
                        // vb.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ClassListActivity.this,
                                "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** 🔹 Lấy token từ SharedPreferences */
    private String getTokenFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", MODE_PRIVATE);
        return prefs.getString("AUTH_TOKEN", null);
    }

    /** 🔹 Dialog thêm học sinh mới (UI) */
    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null, false);
        TextInputLayout tilDob = dialogView.findViewById(R.id.tilDob);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtDob = dialogView.findViewById(R.id.edtDob);

        View.OnClickListener openCal = v -> openDatePicker(edtDob);
        edtDob.setOnClickListener(openCal);
        tilDob.setEndIconDrawable(com.google.android.material.R.drawable.material_ic_calendar_black_24dp);
        tilDob.setEndIconOnClickListener(openCal);

        final androidx.appcompat.app.AlertDialog d = new MaterialAlertDialogBuilder(this,
                com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> d.dismiss());
        dialogView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String dob = edtDob.getText().toString().trim();

            if (name.isEmpty()) {
                tilDob.getEditText().clearFocus();
                edtName.setError("Nhập họ tên");
                return;
            }
            if (dob.isEmpty()) {
                edtDob.setError("Chọn ngày sinh");
                return;
            }

            displayStudents.add(0, new ClassStudentAdapter.Student(name, "Ngày sinh: " + dob));
            adapter.notifyItemInserted(0);
            vb.rvStudents.scrollToPosition(0);
            d.dismiss();
            Toast.makeText(this, "Đã thêm: " + name, Toast.LENGTH_SHORT).show();
        });

        d.show();
        if (d.getWindow() != null) {
            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /** 🔹 DatePicker chọn ngày sinh */
    private void openDatePicker(EditText target) {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth);
                    String pretty = new SimpleDateFormat(
                            "d 'tháng' M, yyyy", new Locale("vi"))
                            .format(cal.getTime());
                    target.setText(pretty);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }
}