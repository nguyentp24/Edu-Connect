package com.example.educonnect.ui.attendance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.educonnect.R;
import com.example.educonnect.adapter.StudentAdapter;
import com.example.educonnect.databinding.ActivityAttendanceBinding;
import com.example.educonnect.model.Student;
import com.example.educonnect.model.ClassroomStudent;
import com.example.educonnect.model.AttendanceItem;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.utils.SessionManager;
import java.util.HashMap;
import java.util.Map;

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
        String klass   = getIntent().getStringExtra("klass");
        vb.tvSubject.setText(getString(R.string.subject_fmt, subject != null ? subject : "—"));
        vb.tvTime.setText(getString(R.string.time_fmt, time != null ? time : "—"));
        String classDisplay = klass != null ? mapClassIdToName(klass) : "—";
        vb.tvClass.setText(getString(R.string.class_fmt, classDisplay));

        vb.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        String courseId = getIntent().getStringExtra("courseId");
        boolean isPresent = getIntent().getBooleanExtra("isPresent", false);
        boolean shouldFetch = getIntent().getBooleanExtra("shouldFetchStudents", false);

        if (isPresent && courseId != null) {
            // Nếu đã điểm danh → gọi API attendance để lấy dữ liệu
            fetchAttendanceData(courseId, klass);
        } else if (shouldFetch && klass != null) {
            // Nếu chưa điểm danh → gọi API classroom để lấy danh sách học sinh
            fetchStudents(klass);
        } else {
            mockStudents();
        }
        vb.rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students, () -> {});
        vb.rvStudents.setAdapter(adapter);

        vb.btnSave.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.saved_n, students.size()), Toast.LENGTH_SHORT).show();
            finish();
        });
        vb.btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String studentName = data.getStringExtra("student_name");
            String note = data.getStringExtra("note");
            String homework = data.getStringExtra("homework");
            String focus = data.getStringExtra("focus");
            
            // Tìm và cập nhật Student trong danh sách
            if (studentName != null) {
                for (Student s : students) {
                    if (s.name.equals(studentName)) {
                        s.note = note != null ? note : "";
                        s.homework = homework != null ? homework : "";
                        s.focus = focus != null ? focus : "";
                        break;
                    }
                }
                // Refresh adapter
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String mapClassIdToName(String classId) {
        if (classId == null) return "";
        switch (classId) {
            case "class01": return "10A1";
            case "class02": return "11A2";
            case "class03": return "12A2";
            case "class04": return "12A1";
            case "class05": return "12A6";
            default: return classId;
        }
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

    private void fetchStudents(String classId) {
        students.clear();
        SessionManager sm = new SessionManager(this);
        String token = sm.getToken();
        if (token == null) return;
        ApiClient.ApiService api = ApiClient.service();
        api.getClassroomStudents(classId, "Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<ClassroomStudent>>() {
            @Override public void onResponse(retrofit2.Call<java.util.List<ClassroomStudent>> call, retrofit2.Response<java.util.List<ClassroomStudent>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ClassroomStudent cs : response.body()) {
                        // chỉ lấy tên, mặc định trạng thái vắng mặt
                        students.add(new Student(cs.fullName, Student.Status.ABSENT));
                    }
                }
                if (adapter != null) adapter.notifyDataSetChanged();
            }

            @Override public void onFailure(retrofit2.Call<java.util.List<ClassroomStudent>> call, Throwable t) {
                // fallback giữ list rỗng, hoặc bạn có thể mock
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchAttendanceData(String courseId, String classId) {
        students.clear();
        SessionManager sm = new SessionManager(this);
        String token = sm.getToken();
        if (token == null) return;

        // Gọi API attendance để lấy dữ liệu điểm danh
        ApiClient.ApiService api = ApiClient.service();
        api.getAttendanceByCourse(courseId, "Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<AttendanceItem>>() {
            @Override public void onResponse(retrofit2.Call<java.util.List<AttendanceItem>> call, retrofit2.Response<java.util.List<AttendanceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Tạo map để tra cứu theo studentId
                    Map<String, AttendanceItem> attendanceMap = new HashMap<>();
                    for (AttendanceItem ai : response.body()) {
                        attendanceMap.put(ai.studentId, ai);
                    }

                    // Gọi API classroom để lấy danh sách học sinh và merge với attendance
                    if (classId != null) {
                        api.getClassroomStudents(classId, "Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<ClassroomStudent>>() {
                            @Override public void onResponse(retrofit2.Call<java.util.List<ClassroomStudent>> call2, retrofit2.Response<java.util.List<ClassroomStudent>> response2) {
                                if (response2.isSuccessful() && response2.body() != null) {
                                    for (ClassroomStudent cs : response2.body()) {
                                        AttendanceItem ai = attendanceMap.get(cs.studentId);
                                        if (ai != null) {
                                            // Map participation sang Status
                                            Student.Status status = mapParticipationToStatus(ai.participation);
                                            students.add(new Student(cs.fullName, status, ai.note, ai.homework, ai.focus));
                                        } else {
                                            // Nếu không có trong attendance → mặc định vắng mặt
                                            students.add(new Student(cs.fullName, Student.Status.ABSENT));
                                        }
                                    }
                                }
                                if (adapter != null) adapter.notifyDataSetChanged();
                            }

                            @Override public void onFailure(retrofit2.Call<java.util.List<ClassroomStudent>> call2, Throwable t2) {
                                if (adapter != null) adapter.notifyDataSetChanged();
                            }
                        });
                    }
                } else {
                    if (adapter != null) adapter.notifyDataSetChanged();
                }
            }

            @Override public void onFailure(retrofit2.Call<java.util.List<AttendanceItem>> call, Throwable t) {
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
    }

    private Student.Status mapParticipationToStatus(String participation) {
        if (participation == null) return Student.Status.ABSENT;
        switch (participation) {
            case "Có mặt": return Student.Status.PRESENT;
            case "Đi trễ": return Student.Status.LATE;
            case "Vắng mặt": return Student.Status.ABSENT;
            default: return Student.Status.ABSENT;
        }
    }
}
