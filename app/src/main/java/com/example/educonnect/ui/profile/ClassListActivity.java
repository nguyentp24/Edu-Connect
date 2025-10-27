package com.example.educonnect.ui.profile;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.educonnect.adapter.ClassStudentAdapter;
import com.example.educonnect.databinding.ActivityClassListBinding;

import java.util.ArrayList;
import java.util.List;

public class ClassListActivity extends AppCompatActivity {

    private ActivityClassListBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityClassListBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Lấy dữ liệu từ Intent (nếu có)
        String klass   = getIntent().getStringExtra("klass");
        String teacher = getIntent().getStringExtra("teacher");
        String year    = getIntent().getStringExtra("year");

        vb.tvClass.setText(klass != null ? "Lớp: " + klass : "Lớp: 10A1");
        vb.tvTeacher.setText(teacher != null ? "GV chủ nhiệm:  " + teacher : "GV chủ nhiệm:  Nguyễn Văn A");
        vb.tvYear.setText(year != null ? "Năm học:  " + year : "Năm học:  2025-2026");

        vb.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Mock danh sách học sinh (thay bằng API của bạn)
        List<ClassStudentAdapter.Student> students = mockStudents();

        vb.rvStudents.setLayoutManager(new LinearLayoutManager(this));
        vb.rvStudents.setAdapter(new ClassStudentAdapter(students));
    }

    private List<ClassStudentAdapter.Student> mockStudents(){
        ArrayList<ClassStudentAdapter.Student> list = new ArrayList<>();
        list.add(new ClassStudentAdapter.Student("Phạm Minh D", "Ngày sinh: 30 tháng 3, 2011"));
        list.add(new ClassStudentAdapter.Student("Đỗ Nhật E", "Ngày sinh: 9 tháng 7, 2012"));
        list.add(new ClassStudentAdapter.Student("Ngô Thị F", "Ngày sinh: 12 tháng 10, 2010"));
        list.add(new ClassStudentAdapter.Student("Bùi Văn G", "Ngày sinh: 21 tháng 11, 2011"));
        list.add(new ClassStudentAdapter.Student("Hoàng Mai H", "Ngày sinh: 5 tháng 1, 2010"));
        list.add(new ClassStudentAdapter.Student("Tạ Công I", "Ngày sinh: 9 tháng 6, 2011"));
        list.add(new ClassStudentAdapter.Student("Lý Minh J", "Ngày sinh: 18 tháng 3, 2012"));
        return list;
    }
}
