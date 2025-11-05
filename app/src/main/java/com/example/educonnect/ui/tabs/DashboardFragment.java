package com.example.educonnect.ui.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.educonnect.api.ApiClient;
import com.example.educonnect.model.AttendanceItem;
import com.example.educonnect.model.Class;
import com.example.educonnect.model.ClassroomStudent; // Ensure you're using the right model
import com.example.educonnect.model.Course;
import com.example.educonnect.databinding.FragmentDashboardBinding;
import com.example.educonnect.model.Student; // Assuming you are still using the student class for other parts
import com.example.educonnect.utils.SessionManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.Calendar;
=======
>>>>>>> a2101d922eb195f0445ecfe97caeaf290804ed7c
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding vb;
    private List<Course> todayCourses = new ArrayList<>(); // Lưu courses hôm nay
    private String currentClassId; // Lưu classId hiện tại

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

<<<<<<< HEAD
        // Lấy các lớp và tiết học từ SessionManager
        SessionManager sm = new SessionManager(requireContext());
        String teacherId = sm.getTeacherId();
        String token = sm.getToken();
        
        if (teacherId != null && token != null) {
            loadClasses(teacherId, token);
        } else {
            Toast.makeText(getContext(), "Chưa đăng nhập hoặc thiếu thông tin", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadClasses(String teacherId, String token) {
        ApiClient.ApiService apiService = ApiClient.service();

        // Gọi API để lấy lớp học
        Call<List<Class>> call = apiService.getClasses(teacherId, "Bearer " + token);
        call.enqueue(new Callback<List<Class>>() {
            @Override
            public void onResponse(Call<List<Class>> call, Response<List<Class>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Class> classes = response.body();
                    if (!classes.isEmpty()) {
                        currentClassId = classes.get(0).getClassId(); // Lấy classId đầu tiên
                        loadCourses(currentClassId);
                        loadStudents(currentClassId); // Lấy học sinh theo classId
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi lấy lớp học: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Class>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses(String classId) {
        ApiClient.ApiService apiService = ApiClient.service();
        SessionManager sm = new SessionManager(requireContext());
        String token = sm.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<List<Course>> call = apiService.getCourses(classId, "Bearer " + token);
        call.enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = response.body();
                    // Lọc courses hôm nay
                    todayCourses = filterTodayCourses(courses);
                    // Cập nhật số tiết học hôm nay
                    int todayPeriodsCount = todayCourses.size();
                    vb.tvTodayPeriods.setText(String.valueOf(todayPeriodsCount));
                    
                    // Sau khi có courses hôm nay, gọi API attendance
                    if (!todayCourses.isEmpty() && currentClassId != null) {
                        loadAttendance(currentClassId, token);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi lấy khóa học: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudents(String classId) {
        ApiClient.ApiService apiService = ApiClient.service();
        SessionManager sm = new SessionManager(requireContext());
        String token = sm.getToken();
        if (token == null){
            return;
        }
        // Assuming this is the correct call to get students from a classroom
        Call<List<ClassroomStudent>> call = apiService.getClassroomStudents(classId, "Bearer " + token);
        call.enqueue(new Callback<List<ClassroomStudent>>() {
            @Override
            public void onResponse(Call<List<ClassroomStudent>> call, Response<List<ClassroomStudent>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ClassroomStudent> students = response.body();
                    // Lưu danh sách học sinh để dùng cho attendance
                    processAttendanceWithStudents(students);
                } else {
                    Toast.makeText(getContext(), "Lỗi lấy học sinh: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ClassroomStudent>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gọi API attendance theo classId và lọc theo courses hôm nay
     */
    private void loadAttendance(String classId, String token) {
        ApiClient.ApiService apiService = ApiClient.service();
        Call<List<AttendanceItem>> call = apiService.getAttendanceByClass(classId, "Bearer " + token);
        call.enqueue(new Callback<List<AttendanceItem>>() {
            @Override
            public void onResponse(Call<List<AttendanceItem>> call, Response<List<AttendanceItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AttendanceItem> allAttendance = response.body();
                    // Lọc attendance theo courses hôm nay
                    List<AttendanceItem> todayAttendance = filterTodayAttendance(allAttendance);
                    // Sắp xếp theo thứ tự: Vắng mặt, Đi trễ, Có mặt
                    sortAttendanceByParticipation(todayAttendance);
                    // Đếm số học sinh có mặt và tổng số
                    updateStudentRatio(todayAttendance);
                } else {
                    Toast.makeText(getContext(), "Lỗi lấy điểm danh: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AttendanceItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối attendance: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Lọc attendance theo courseId của các courses hôm nay
     */
    private List<AttendanceItem> filterTodayAttendance(List<AttendanceItem> allAttendance) {
        if (todayCourses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Lấy set các courseId hôm nay
        Set<String> todayCourseIds = todayCourses.stream()
                .map(course -> course.courseId)
                .collect(Collectors.toSet());
        
        // Lọc attendance có courseId trong danh sách courses hôm nay
        return allAttendance.stream()
                .filter(att -> att.courseId != null && todayCourseIds.contains(att.courseId))
                .collect(Collectors.toList());
    }

    /**
     * Sắp xếp attendance theo thứ tự: Vắng mặt, Đi trễ, Có mặt
     */
    private void sortAttendanceByParticipation(List<AttendanceItem> attendance) {
        Collections.sort(attendance, new Comparator<AttendanceItem>() {
            @Override
            public int compare(AttendanceItem a1, AttendanceItem a2) {
                int order1 = getParticipationOrder(a1.participation);
                int order2 = getParticipationOrder(a2.participation);
                return Integer.compare(order1, order2);
            }
            
            private int getParticipationOrder(String participation) {
                if (participation == null) return 999;
                switch (participation) {
                    case "Vắng mặt": return 0;
                    case "Đi trễ": return 1;
                    case "Có mặt": return 2;
                    default: return 999;
                }
            }
        });
    }

    /**
     * Lưu danh sách học sinh để merge với attendance
     */
    private Map<String, ClassroomStudent> studentsMap;

    /**
     * Xử lý attendance với danh sách học sinh
     */
    private void processAttendanceWithStudents(List<ClassroomStudent> students) {
        // Tạo map để tra cứu học sinh theo studentId
        studentsMap = students.stream()
                .collect(Collectors.toMap(
                    s -> s.studentId,
                    s -> s,
                    (existing, replacement) -> existing
                ));
    }

    /**
     * Cập nhật số lượng học sinh có mặt trên tổng số học sinh
     */
    private void updateStudentRatio(List<AttendanceItem> todayAttendance) {
        if (todayAttendance.isEmpty()) {
            vb.tvStudentRatio.setText("0/0");
            return;
        }

        // Lấy unique studentId từ attendance
        Set<String> uniqueStudentIds = todayAttendance.stream()
                .map(att -> att.studentId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        // Đếm số học sinh có mặt (học sinh có ít nhất 1 "Có mặt" trong các courses hôm nay)
        Set<String> presentStudentIds = todayAttendance.stream()
                .filter(att -> "Có mặt".equals(att.participation))
                .map(att -> att.studentId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        // Tổng số học sinh (unique)
        int totalStudents = uniqueStudentIds.size();
        // Số học sinh có mặt (unique)
        int presentCount = presentStudentIds.size();

        // Hiển thị: số có mặt / tổng số
        vb.tvStudentRatio.setText(presentCount + "/" + totalStudents);
    }

    /**
     * Lọc các Course có ngày là hôm nay
     */
    private List<Course> filterTodayCourses(List<Course> courses) {
        List<Course> todayCourses = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        for (Course course : courses) {
            if (course.startTime != null && !course.startTime.isEmpty()) {
                try {
                    // Parse ISO string: yyyy-MM-ddTHH:mm:ss
                    String[] parts = course.startTime.split("T");
                    if (parts.length >= 1) {
                        String[] dateParts = parts[0].split("-");
                        if (dateParts.length >= 3) {
                            int courseYear = Integer.parseInt(dateParts[0]);
                            int courseMonth = Integer.parseInt(dateParts[1]) - 1; // Calendar.MONTH is 0-based
                            int courseDay = Integer.parseInt(dateParts[2]);

                            // So sánh với ngày hôm nay
                            if (courseYear == todayYear && courseMonth == todayMonth && courseDay == todayDay) {
                                todayCourses.add(course);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Bỏ qua nếu không parse được
                }
            }
        }
        return todayCourses;
=======
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
>>>>>>> a2101d922eb195f0445ecfe97caeaf290804ed7c
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
