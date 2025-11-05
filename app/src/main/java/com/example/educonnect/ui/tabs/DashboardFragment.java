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
import com.example.educonnect.adapter.DashboardAttendanceAdapter;
import com.example.educonnect.model.AttendanceItem;
import com.example.educonnect.model.Class;
import com.example.educonnect.model.ClassroomStudent;
import com.example.educonnect.model.Course;
import com.example.educonnect.databinding.FragmentDashboardBinding;
import com.example.educonnect.model.Student;
import com.example.educonnect.adapter.ScheduleAdapter;
import com.example.educonnect.model.ScheduleItem;
import com.example.educonnect.utils.SessionManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding vb;
    private List<Course> todayCourses = new ArrayList<>(); // Lưu courses hôm nay
    private String currentClassId; // Lưu classId hiện tại
    private String currentClassName; // Lưu tên lớp hiện tại

    // Danh sách học sinh để merge với attendance
    private Map<String, ClassroomStudent> studentsMap;
    // Map courseId -> Course để lấy thông tin môn học và thời gian
    private Map<String, Course> courseMap;
    // Adapter cho RecyclerView
    private DashboardAttendanceAdapter attendanceAdapter;

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

        vb.tvNotice.setText(getString(com.example.educonnect.R.string.notificationDashboard));

        // Khởi tạo RecyclerView
        attendanceAdapter = new DashboardAttendanceAdapter(new ArrayList<>());
        vb.rvAttendanceList.setLayoutManager(new LinearLayoutManager(getContext()));
        vb.rvAttendanceList.setAdapter(attendanceAdapter);
        vb.rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));

        // Click handlers cho 3 cột
        vb.colStudents.setOnClickListener(v1 -> showStudentsSection());
        vb.colPeriods.setOnClickListener(v12 -> showSchedulesSection());
        vb.colReports.setOnClickListener(v13 -> navigateToReportTab());

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
                        currentClassName = classes.get(0).getClassName(); // Lấy tên lớp
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
                    // Tạo map courseId -> Course
                    courseMap = new HashMap<>();
                    for (Course course : todayCourses) {
                        if (course.courseId != null) {
                            courseMap.put(course.courseId, course);
                        }
                    }
                    // Cập nhật số tiết học hôm nay
                    int todayPeriodsCount = todayCourses.size();
                    vb.tvTodayPeriods.setText(String.valueOf(todayPeriodsCount));
                    
                    // Sau khi có courses hôm nay, gọi API attendance
                    if (!todayCourses.isEmpty() && currentClassId != null) {
                        loadAttendance(currentClassId, token);
                        // Đồng thời chuẩn bị danh sách lịch hôm nay để hiển thị khi người dùng bấm
                        buildTodaySchedules();
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
                    // Hiển thị danh sách học sinh
                    displayAttendanceList(todayAttendance);
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
     * Xử lý attendance với danh sách học sinh
     */
    private void processAttendanceWithStudents(List<ClassroomStudent> students) {
        // Tạo map để tra cứu học sinh theo studentId
        studentsMap = new HashMap<>();
        for (ClassroomStudent student : students) {
            if (student.studentId != null) {
                studentsMap.put(student.studentId, student);
            }
        }
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

        // Đếm số học sinh có mặt hoặc đi trễ (ít nhất 1 lần "Có mặt" hoặc "Đi trễ" trong các courses hôm nay)
        Set<String> presentStudentIds = todayAttendance.stream()
                .filter(att -> "Có mặt".equals(att.participation) || "Đi trễ".equals(att.participation))
                .map(att -> att.studentId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        // Tổng số học sinh (unique)
        int totalStudents = uniqueStudentIds.size();
        // Số học sinh có mặt hoặc đi trễ (unique)
        int presentCount = presentStudentIds.size();

        // Hiển thị: số (có mặt + đi trễ) / tổng số
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
    }

    /**
     * Hiển thị danh sách attendance trong RecyclerView
     */
    private void displayAttendanceList(List<AttendanceItem> attendanceList) {
        if (studentsMap == null || courseMap == null) {
            return;
        }

        List<DashboardAttendanceAdapter.AttendanceDisplayItem> displayItems = new ArrayList<>();

        for (AttendanceItem att : attendanceList) {
            // Lấy thông tin học sinh
            ClassroomStudent student = studentsMap.get(att.studentId);
            if (student == null) continue;

            // Lấy thông tin course
            Course course = courseMap.get(att.courseId);
            if (course == null) continue;

            // Format thời gian
            String timeRange = formatTimeRange(course.startTime, course.endTime);

            // Tạo display item
            DashboardAttendanceAdapter.AttendanceDisplayItem item =
                    new DashboardAttendanceAdapter.AttendanceDisplayItem(
                            student.fullName != null ? student.fullName : "",
                            currentClassName != null ? currentClassName : "",
                            course.subjectName != null ? course.subjectName : "",
                            timeRange,
                            att.note != null ? att.note : "",
                            att.participation != null ? att.participation : ""
                    );
            displayItems.add(item);
        }

        // Cập nhật adapter
        attendanceAdapter = new DashboardAttendanceAdapter(displayItems);
        vb.rvAttendanceList.setAdapter(attendanceAdapter);

        // Cập nhật thông báo
        if (displayItems.isEmpty()) {
            vb.tvNotice.setText(getString(com.example.educonnect.R.string.notificationDashboard));
        } else {
            vb.tvNotice.setText("Thông báo hôm nay!");
        }

        // Hiển thị khu vực học sinh, ẩn lịch
        vb.rvAttendanceList.setVisibility(View.VISIBLE);
        vb.rvSchedules.setVisibility(View.GONE);
    }

    /**
     * Format thời gian từ ISO string sang "HH:mm SA/CH - HH:mm SA/CH"
     */
    private String formatTimeRange(String startTimeIso, String endTimeIso) {
        String start = formatTime(startTimeIso);
        String end = formatTime(endTimeIso);
        return start + " - " + end;
    }

    /**
     * Format thời gian từ ISO string sang "HH:mm SA/CH"
     */
    private String formatTime(String iso) {
        if (iso == null || !iso.contains("T")) return "";
        try {
            int t = iso.indexOf('T');
            int end = Math.min(iso.length(), t + 6); // HH:mm
            String hm = iso.substring(t + 1, end);
            String[] sp = hm.split(":");
            int h = Integer.parseInt(sp[0]);
            int min = Integer.parseInt(sp[1]);
            String suffix = h < 12 ? "SA" : "CH";
            int h12 = h % 12;
            if (h12 == 0) h12 = 12;
            return h12 + ":" + (min < 10 ? "0" + min : min) + " " + suffix;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Dựng danh sách các lịch hôm nay từ todayCourses để tái sử dụng
     */
    private void buildTodaySchedules() {
        ArrayList<ScheduleItem> list = new ArrayList<>();
        if (!todayCourses.isEmpty()) {
            for (Course c : todayCourses) {
                String start = formatTime(c.startTime);
                String end   = formatTime(c.endTime);
                boolean attended = c.status != null && c.status.equalsIgnoreCase("present");
                list.add(new ScheduleItem(start, end, c.startTime, c.endTime, c.subjectName, c.classId, "", attended, c.courseId));
            }
        }
        vb.rvSchedules.setAdapter(new ScheduleAdapter(list, item -> {
            // không điều hướng ở Dashboard khi bấm item, để đơn giản giữ nguyên
        }));
    }

    private void showStudentsSection() {
        vb.rvAttendanceList.setVisibility(View.VISIBLE);
        vb.rvSchedules.setVisibility(View.GONE);
    }

    private void showSchedulesSection() {
        vb.rvAttendanceList.setVisibility(View.GONE);
        vb.rvSchedules.setVisibility(View.VISIBLE);
    }

    private void navigateToReportTab() {
        android.view.View bottom = requireActivity().findViewById(com.example.educonnect.R.id.bottomNav);
        if (bottom instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
            ((com.google.android.material.bottomnavigation.BottomNavigationView) bottom)
                    .setSelectedItemId(com.example.educonnect.R.id.nav_report);
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
}
