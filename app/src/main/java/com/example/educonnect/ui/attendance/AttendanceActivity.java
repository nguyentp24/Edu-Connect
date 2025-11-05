package com.example.educonnect.ui.attendance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.educonnect.R;
import com.example.educonnect.adapter.StudentAdapter;
import com.example.educonnect.databinding.ActivityAttendanceBinding;
import com.example.educonnect.model.Student;

import java.util.ArrayList;

public class AttendanceActivity extends AppCompatActivity {

    private ActivityAttendanceBinding vb;
    private final ArrayList<Student> students = new ArrayList<>();
    private StudentAdapter adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityAttendanceBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        String subject = getIntent().getStringExtra("subject");
        String time    = getIntent().getStringExtra("time");
        String classroom   = getIntent().getStringExtra("class");
        vb.tvSubject.setText(getString(R.string.subject_fmt, subject != null ? subject : "—"));
        vb.tvTime.setText(getString(R.string.time_fmt, time != null ? time : "—"));
        vb.tvClass.setText(getString(R.string.class_fmt, classroom != null ? classroom : "—"));

        vb.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        mockStudents();
        vb.rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students, () -> {});
        vb.rvStudents.setAdapter(adapter);

        vb.btnSave.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.saved_n, students.size()), Toast.LENGTH_SHORT).show();
            finish();
        });
        vb.btnCancel.setOnClickListener(v -> finish());
    }

    private void mockStudents() {
        students.clear();
        students.add(new Student("Nguyễn Văn A", Student.Status.PRESENT));
        students.add(new Student("Trần Thị B",   Student.Status.LATE));
        students.add(new Student("Lê Hoàng C",   Student.Status.ABSENT));
        students.add(new Student("Phạm Minh D",  Student.Status.ABSENT));
        students.add(new Student("Đỗ Nhật E",    Student.Status.ABSENT));
        students.add(new Student("Ngô Thị F",    Student.Status.ABSENT));
    }
}
