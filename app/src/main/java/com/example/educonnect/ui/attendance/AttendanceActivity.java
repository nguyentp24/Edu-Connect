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
import com.example.educonnect.model.Classroom;
import com.example.educonnect.model.request.CourseStatusRequest;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.utils.SessionManager;
import okhttp3.ResponseBody;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class AttendanceActivity extends AppCompatActivity {

    private ActivityAttendanceBinding vb;
    private final ArrayList<Student> students = new ArrayList<>();
    private StudentAdapter adapter;
    private String courseId; // Lưu courseId để dùng khi lưu
    private boolean isPresent; // Lưu trạng thái course có attendance hay không
    private Map<String, String> classroomMap = new HashMap<>(); // Cache: classId -> className
    private String currentClassId; // Lưu classId hiện tại để cập nhật UI

    private Student.Status mapParticipationToStatus(String participation) {
        if (participation == null) return Student.Status.ABSENT;
        switch (participation) {
            case "Có mặt": return Student.Status.PRESENT;
            case "Đi trễ": return Student.Status.LATE;
            case "Vắng mặt": return Student.Status.ABSENT;
            default: return Student.Status.ABSENT;
        }
    }

    private String mapStatusToParticipation(Student.Status status) {
        switch (status) {
            case PRESENT: return "Có mặt";
            case LATE: return "Đi trễ";
            case ABSENT: return "Vắng mặt";
            default: return "Vắng mặt";
        }
    }

    private String mapClassIdToName(String classId) {
        if (classId == null) return "";
        // Nếu đã có trong cache, trả về className
        if (classroomMap.containsKey(classId)) {
            return classroomMap.get(classId);
        }
        // Nếu chưa có, trả về classId (fallback)
        return classId;
    }
    
    private void fetchClassrooms() {
        SessionManager sm = new SessionManager(this);
        String teacherId = sm.getTeacherId();
        String token = sm.getToken();
        if (teacherId == null || token == null) {
            return;
        }
        
        ApiClient.ApiService api = ApiClient.service();
        api.getClassroomByTeacherId(teacherId, "Bearer " + token).enqueue(new retrofit2.Callback<List<Classroom>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Classroom>> call, retrofit2.Response<List<Classroom>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    classroomMap.clear();
                    for (Classroom classroom : response.body()) {
                        if (classroom.getClassId() != null && classroom.getClassName() != null) {
                            classroomMap.put(classroom.getClassId(), classroom.getClassName());
                        }
                    }
                    // Cập nhật lại UI với tên lớp đúng
                    updateClassDisplay();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Classroom>> call, Throwable t) {
                // Nếu lỗi, vẫn hiển thị classId như cũ
                android.util.Log.e("AttendanceActivity", "Failed to fetch classrooms", t);
            }
        });
    }
    
    private void updateClassDisplay() {
        if (currentClassId != null && vb != null) {
            String classDisplay = mapClassIdToName(currentClassId);
            vb.tvClass.setText(getString(R.string.class_fmt, classDisplay));
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityAttendanceBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        String subject = getIntent().getStringExtra("subject");
        String time    = getIntent().getStringExtra("time");
        this.currentClassId = getIntent().getStringExtra("class");
        String className = getIntent().getStringExtra("className"); // Tên lớp từ Timetable
        
        vb.tvSubject.setText(getString(R.string.subject_fmt, subject != null ? subject : "—"));
        vb.tvTime.setText(getString(R.string.time_fmt, time != null ? time : "—"));
        // Ưu tiên dùng className từ Timetable, nếu không có thì dùng classId
        String classDisplay = className != null ? className : (currentClassId != null ? currentClassId : "—");
        vb.tvClass.setText(getString(R.string.class_fmt, classDisplay));

        vb.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        this.courseId = getIntent().getStringExtra("courseId");
        this.isPresent = getIntent().getBooleanExtra("isPresent", false);
        boolean shouldFetch = getIntent().getBooleanExtra("shouldFetchStudents", false);
        
        // Nếu đã có className từ Timetable thì không cần fetch, chỉ fetch nếu chưa có
        if (className == null) {
            // Fetch danh sách classrooms để map classId -> className
            fetchClassrooms();
        }

        if (isPresent && courseId != null) {
            // Nếu đã điểm danh → gọi API attendance để lấy dữ liệu
            fetchAttendanceData(courseId, currentClassId);
        } else if (shouldFetch && currentClassId != null) {
            // Nếu chưa điểm danh → gọi API classroom để lấy danh sách học sinh
            fetchStudents(currentClassId);
        }
        vb.rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students, () -> {});
        vb.rvStudents.setAdapter(adapter);

        vb.btnSave.setOnClickListener(v -> {
            if (courseId == null || courseId.isEmpty()) {
                Toast.makeText(this, "Không có courseId", Toast.LENGTH_SHORT).show();
                return;
            }
            saveAttendanceToServer();
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
                    if (s.getName().equals(studentName)) {
                        s.setNote(note != null ? note : "");
                        s.setHomework(homework != null ? homework : "");
                        s.setFocus(focus != null ? focus : "");
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
                        // lưu studentId và tên, mặc định trạng thái vắng mặt
                        students.add(new Student(cs.getFullName(), cs.getStudentId(), Student.Status.ABSENT));
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
                        attendanceMap.put(ai.getStudentId(), ai);
                    }

                    // Gọi API classroom để lấy danh sách học sinh và merge với attendance
                    if (classId != null) {
                        api.getClassroomStudents(classId, "Bearer " + token).enqueue(new retrofit2.Callback<java.util.List<ClassroomStudent>>() {
                            @Override public void onResponse(retrofit2.Call<java.util.List<ClassroomStudent>> call2, retrofit2.Response<java.util.List<ClassroomStudent>> response2) {
                                if (response2.isSuccessful() && response2.body() != null) {
                                    for (ClassroomStudent cs : response2.body()) {
                                        AttendanceItem ai = attendanceMap.get(cs.getStudentId());
                                        if (ai != null) {
                                            // Map participation sang Status
                                            Student.Status status = mapParticipationToStatus(ai.getParticipation());
                                            students.add(new Student(cs.getFullName(), cs.getStudentId(), status, ai.getNote(), ai.getHomework(), ai.getFocus()));
                                        } else {
                                            // Nếu không có trong attendance → mặc định vắng mặt
                                            students.add(new Student(cs.getFullName(), cs.getStudentId(), Student.Status.ABSENT));
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

    private void saveAttendanceToServer() {
        if (students.isEmpty()) {
            Toast.makeText(this, "Không có học sinh để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sm = new SessionManager(this);
        String token = sm.getToken();
        if (token == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo mảng AttendanceItem từ danh sách students
        java.util.List<AttendanceItem> attendanceList = new ArrayList<>();
        for (Student s : students) {
            if (s.getStudentId() == null || s.getStudentId().isEmpty()) {
                continue; // Bỏ qua nếu không có studentId
            }
            AttendanceItem item = new AttendanceItem();
            item.setAtID(""); // Để chuỗi rỗng khi POST
            item.setStudentId(s.getStudentId());
            item.setCourseId(courseId);
            item.setParticipation(mapStatusToParticipation(s.getStatus()));
            item.setNote(s.getNote() != null ? s.getNote() : "");
            item.setHomework(s.getHomework() != null ? s.getHomework() : "");
            item.setFocus(s.getFocus() != null ? s.getFocus() : "");
            attendanceList.add(item);
        }

        if (attendanceList.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu hợp lệ để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bắt đầu với loading
        setLoading(true);

        // Nếu course đã có attendance (status = present), DELETE trước
        if (isPresent && courseId != null && !courseId.isEmpty()) {
            deleteAttendanceThenPost(attendanceList, token);
        } else {
            // Nếu chưa có attendance, POST trực tiếp
            postAttendance(attendanceList, token);
        }
    }

    private void deleteAttendanceThenPost(java.util.List<AttendanceItem> attendanceList, String token) {
        ApiClient.ApiService api = ApiClient.service();
        api.deleteAttendanceByCourse(courseId, "Bearer " + token).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                int code = response.code();
                if (code >= 200 && code < 300) {
                    // DELETE thành công → POST attendance
                    android.util.Log.d("AttendanceActivity", "Delete attendance successful, now posting...");
                    postAttendance(attendanceList, token);
                } else {
                    // DELETE thất bại
                    setLoading(false);
                    android.util.Log.e("AttendanceActivity", "Delete attendance failed with code: " + code);
                    Toast.makeText(AttendanceActivity.this, "Xóa điểm danh cũ thất bại: " + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                android.util.Log.e("AttendanceActivity", "Delete attendance onFailure", t);
                Toast.makeText(AttendanceActivity.this, "Lỗi mạng khi xóa điểm danh: " + (t.getMessage() != null ? t.getMessage() : "Không kết nối được server"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postAttendance(java.util.List<AttendanceItem> attendanceList, String token) {
        ApiClient.ApiService api = ApiClient.service();
        api.saveAttendance(attendanceList, "Bearer " + token).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                // Không quan tâm response body, chỉ kiểm tra status code
                int code = response.code();
                if (code >= 200 && code < 300) {
                    // POST thành công → cập nhật status course và quay về
                    updateCourseStatusToPresent(token);
                } else {
                    // Lỗi từ server
                    setLoading(false);
                    Toast.makeText(AttendanceActivity.this, "Lưu thất bại: " + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                // Lỗi mạng hoặc kết nối
                setLoading(false);
                Toast.makeText(AttendanceActivity.this, "Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : "Không kết nối được server"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCourseStatusToPresent(String token) {
        if (courseId == null || courseId.isEmpty()) {
            setLoading(false);
            android.util.Log.w("AttendanceActivity", "updateCourseStatusToPresent: courseId is null or empty");
            Toast.makeText(this, "Đã lưu điểm danh thành công", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CourseStatusRequest request = new CourseStatusRequest();
        request.setCourseId(courseId);
        request.setStatus("present");

        android.util.Log.d("AttendanceActivity", "Updating course status: courseId=" + courseId + ", status=present");

        ApiClient.ApiService api = ApiClient.service();
        api.updateCourseStatus(request, "Bearer " + token).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                setLoading(false);
                // Kiểm tra status code
                int code = response.code();
                android.util.Log.d("AttendanceActivity", "updateCourseStatus response code: " + code);
                if (code >= 200 && code < 300) {
                    android.util.Log.d("AttendanceActivity", "Course status updated successfully");
                    Toast.makeText(AttendanceActivity.this, "Đã lưu điểm danh thành công", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.e("AttendanceActivity", "Course status update failed with code: " + code);
                    try {
                        if (response.errorBody() != null) {
                            String error = response.errorBody().string();
                            android.util.Log.e("AttendanceActivity", "Error body: " + error);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AttendanceActivity", "Error reading error body", e);
                    }
                    Toast.makeText(AttendanceActivity.this, "Đã lưu điểm danh (cập nhật status thất bại: " + code + ")", Toast.LENGTH_SHORT).show();
                }
                finish(); // Quay về timetable
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                android.util.Log.e("AttendanceActivity", "updateCourseStatus onFailure", t);
                // Vẫn finish vì đã lưu attendance thành công
                Toast.makeText(AttendanceActivity.this, "Đã lưu điểm danh (cập nhật status thất bại: " + (t.getMessage() != null ? t.getMessage() : "Lỗi mạng") + ")", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setLoading(boolean loading) {
        vb.btnSave.setEnabled(!loading);
        vb.btnCancel.setEnabled(!loading);
        vb.loadingOverlay.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}
