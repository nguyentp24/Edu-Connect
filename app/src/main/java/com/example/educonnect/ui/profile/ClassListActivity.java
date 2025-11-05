package com.example.educonnect.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.educonnect.adapter.ClassStudentAdapter;
import com.example.educonnect.databinding.ActivityClassListBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.example.educonnect.R;

import com.google.android.material.textfield.TextInputLayout;
import android.view.ViewGroup;


public class ClassListActivity extends AppCompatActivity {

    private ActivityClassListBinding vb;
    private final List<ClassStudentAdapter.Student> students = new ArrayList<>();
    private ClassStudentAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityClassListBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Header từ Intent
        String classroom   = getIntent().getStringExtra("class");
        String teacher = getIntent().getStringExtra("teacher");
        String year    = getIntent().getStringExtra("year");

        vb.tvClass.setText(classroom != null ? "Lớp: " + classroom : "Lớp: 10A1");
        vb.tvTeacher.setText(teacher != null ? "GV chủ nhiệm:  " + teacher : "GV chủ nhiệm:  Nguyễn Văn A");
        vb.tvYear.setText(year != null ? "Năm học:  " + year : "Năm học:  2025-2026");

        vb.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // RecyclerView
        vb.rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassStudentAdapter(students);
        vb.rvStudents.setAdapter(adapter);

        // Mock data ban đầu
        students.addAll(mockStudents());
        adapter.notifyDataSetChanged();

        // FAB: thêm học sinh
        vb.fabAdd.setOnClickListener(v -> showAddStudentDialog());
    }

    /** Hiển thị dialog thêm học sinh mới */
    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null, false);
        TextInputLayout tilDob = dialogView.findViewById(R.id.tilDob);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtDob  = dialogView.findViewById(R.id.edtDob);

        // mở DatePicker khi bấm vào ô hoặc icon
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
            String dob  = edtDob.getText().toString().trim();

            if (name.isEmpty()) { tilDob.getEditText().clearFocus(); edtName.setError("Nhập họ tên"); return; }
            if (dob.isEmpty())  { edtDob.setError("Chọn ngày sinh"); return; }

            students.add(0, new ClassStudentAdapter.Student(name, "Ngày sinh: " + dob));
            adapter.notifyItemInserted(0);
            vb.rvStudents.scrollToPosition(0);
            d.dismiss();
            Toast.makeText(this, "Đã thêm: " + name, Toast.LENGTH_SHORT).show();
        });

        d.show();

        // Bắt buộc dialog full chiều ngang (đẹp hơn)
        if (d.getWindow() != null) {
            d.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    /** DatePicker -> ghi ra dạng vi-VN: 30 tháng 3, 2011 */
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

    private List<ClassStudentAdapter.Student> mockStudents(){
        List<ClassStudentAdapter.Student> list = new ArrayList<>();
        list.add(new ClassStudentAdapter.Student("Phạm Minh D", "Ngày sinh: 30 tháng 3, 2011"));
        list.add(new ClassStudentAdapter.Student("Đỗ Nhật E",  "Ngày sinh: 9 tháng 7, 2012"));
        list.add(new ClassStudentAdapter.Student("Ngô Thị F",  "Ngày sinh: 12 tháng 10, 2010"));
        list.add(new ClassStudentAdapter.Student("Bùi Văn G",  "Ngày sinh: 21 tháng 11, 2011"));
        list.add(new ClassStudentAdapter.Student("Hoàng Mai H","Ngày sinh: 5 tháng 1, 2010"));
        list.add(new ClassStudentAdapter.Student("Tạ Công I",  "Ngày sinh: 9 tháng 6, 2011"));
        list.add(new ClassStudentAdapter.Student("Lý Minh J",  "Ngày sinh: 18 tháng 3, 2012"));
        return list;
    }
}
