package com.example.educonnect.ui.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.educonnect.databinding.FragmentDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding vb;

    // Danh sách học sinh vắng mặt
    private List<Student> absentStudents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        vb = FragmentDashboardBinding.inflate(inflater, container, false);
        return vb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tiêu đề
        vb.tvTitle.setText(getString(com.example.educonnect.R.string.dashboard_title));

        // Ngày kiểu: "Thứ ba, 21 tháng 10, 2025"
        Locale vi = new Locale("vi");
        String today = new SimpleDateFormat("EEEE, dd 'tháng' MM, yyyy", vi)
                .format(new Date());
        vb.tvDate.setText(capitalizeFirst(today));

        // Demo số liệu theo ảnh
        vb.tvStudentRatio.setText("0/10");
        vb.tvTodayPeriods.setText("0");
        vb.tvReports.setText("2");

        vb.tvNotice.setText(getString(com.example.educonnect.R.string.no_absent));

        // Tạo danh sách học sinh vắng mặt và hiển thị
        loadAbsentStudents();
        displayAbsentStudents();
    }

    private void loadAbsentStudents() {
        // Thêm các học sinh vắng mặt vào danh sách (ví dụ)
        absentStudents.add(new Student("Ngô Nhật E", "10A1", "Toán", "7:50 SA - 8:35 SA", "Không có ghi chú"));
        absentStudents.add(new Student("Ngô Thị F", "10A1", "Toán", "7:50 SA - 8:35 SA", "Không có ghi chú"));
        absentStudents.add(new Student("Hoàng Mai H", "10A1", "Toán", "7:50 SA - 8:35 SA", "Không có ghi chú"));
    }

    private void displayAbsentStudents() {
        // Hiển thị thông tin học sinh vắng mặt vào các khung trong giao diện
        for (int i = 0; i < absentStudents.size(); i++) {
            Student student = absentStudents.get(i);
            // Lấy các View tương ứng với từng học sinh trong layout (item_student1, item_student2, item_student3)
            // Cập nhật dữ liệu học sinh vào các View
            if (i == 0) {
                vb.itemStudent1.tvStudentName.setText(student.getName());
                vb.itemStudent1.tvStudentClass.setText("Lớp: " + student.getClassName());
                vb.itemStudent1.tvSubject.setText("Môn: " + student.getSubject());
                vb.itemStudent1.tvTime.setText("Giờ học: " + student.getTime());
                vb.itemStudent1.tvNotes.setText(student.getNotes());
            } else if (i == 1) {
                vb.itemStudent2.tvStudentName.setText(student.getName());
                vb.itemStudent2.tvStudentClass.setText("Lớp: " + student.getClassName());
                vb.itemStudent2.tvSubject.setText("Môn: " + student.getSubject());
                vb.itemStudent2.tvTime.setText("Giờ học: " + student.getTime());
                vb.itemStudent2.tvNotes.setText(student.getNotes());
            } else if (i == 2) {
                vb.itemStudent3.tvStudentName.setText(student.getName());
                vb.itemStudent3.tvStudentClass.setText("Lớp: " + student.getClassName());
                vb.itemStudent3.tvSubject.setText("Môn: " + student.getSubject());
                vb.itemStudent3.tvTime.setText("Giờ học: " + student.getTime());
                vb.itemStudent3.tvNotes.setText(student.getNotes());
            }
        }
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        vb = null;
    }

    // Lớp sinh viên vắng mặt
    private static class Student {
        private String name;
        private String className;
        private String subject;
        private String time;
        private String notes;

        public Student(String name, String className, String subject, String time, String notes) {
            this.name = name;
            this.className = className;
            this.subject = subject;
            this.time = time;
            this.notes = notes;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }

        public String getSubject() {
            return subject;
        }

        public String getTime() {
            return time;
        }

        public String getNotes() {
            return notes;
        }
    }
}
