package com.example.educonnect.ui.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educonnect.adapter.DayChipAdapter;
import com.example.educonnect.adapter.ScheduleAdapter;
import com.example.educonnect.databinding.FragmentTimetableBinding;
import com.example.educonnect.model.DayChip;
import com.example.educonnect.model.ScheduleItem;
import com.example.educonnect.model.Course;
import com.example.educonnect.model.Classroom;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.utils.SessionManager;
import com.example.educonnect.ui.attendance.AttendanceActivity;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TimeTableFragment extends Fragment {

    private FragmentTimetableBinding vb;
    private final List<DayChip> days = new ArrayList<>();
    private DayChipAdapter chipAdapter;
    private ScheduleAdapter scheduleAdapter; // Lưu adapter để cập nhật khi có classroomMap
    private Calendar selectedDate;     // ngày đang chọn
    private final Locale vi = new Locale("vi", "VN");
    private final List<Course> allCourses = new ArrayList<>();
    private Map<String, String> classroomMap = new HashMap<>(); // Cache: classId -> className

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vb = FragmentTimetableBinding.inflate(inflater, container, false);

        // Ngày mặc định: hôm nay (demo bạn có thể set cứng một ngày)
        selectedDate = Calendar.getInstance();
        // selectedDate.set(2025, Calendar.JULY, 24);

        setupHeaderAndChips();
        // Tải dữ liệu thật khi vào màn
        fetchClassrooms(); // Fetch classrooms trước để có tên lớp
        fetchCoursesForTeacher();

        return vb.getRoot();
    }

    /** Header + Chips + DatePicker */
    private void setupHeaderAndChips() {
        // Header text
        updateBigDateText();

        // Click vào header mở DatePicker
        vb.txtBigDate.setOnClickListener(v -> openDatePicker());

        // Chips RecyclerView: 7 cột, thêm spacing đều giữa các chip
        vb.rvDays.setHasFixedSize(true);
        vb.rvDays.setLayoutManager(new GridLayoutManager(getContext(), 7, GridLayoutManager.VERTICAL, false));
        buildWeekChipsAround(selectedDate);
        chipAdapter = new DayChipAdapter(days, pos -> {
            // chọn ngày theo chip
            for (int i = 0; i < days.size(); i++) days.get(i).setSelected(i == pos);
            selectedDate = (Calendar) days.get(pos).getDate().clone();
            chipAdapter.notifyDataSetChanged();
            vb.rvDays.smoothScrollToPosition(pos);

            updateBigDateText();
            updateSchedulesForSelectedDate();
        });
        vb.rvDays.setAdapter(chipAdapter);
    }

    /** Mở Material Date Picker */
    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(selectedDate.getTimeInMillis())
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selection);
            selectedDate = c;

            // cập nhật header + chips + dữ liệu
            updateBigDateText();
            buildWeekChipsAround(selectedDate);
            chipAdapter.notifyDataSetChanged();

            // scroll tới chip đang chọn
            int selIdx = findSelectedIndex();
            if (selIdx >= 0) vb.rvDays.scrollToPosition(selIdx);

            updateSchedulesForSelectedDate();
        });

        picker.show(getParentFragmentManager(), "date_picker");
    }

    /** Tạo 7 chip xung quanh ngày đang chọn: T2..CN của tuần */
    private void buildWeekChipsAround(Calendar base) {
        days.clear();

        // Tìm thứ 2 của tuần chứa 'base'
        Calendar monday = (Calendar) base.clone();
        int dayOfWeek = monday.get(Calendar.DAY_OF_WEEK); // CN=1..T7=7 (theo US)
        int deltaToMonday = ((dayOfWeek + 5) % 7); // số ngày lùi về T2
        monday.add(Calendar.DAY_OF_MONTH, -deltaToMonday);

        String[] weekdayShort = {"Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "CN"};

        for (int i = 0; i < 7; i++) {
            Calendar d = (Calendar) monday.clone();
            d.add(Calendar.DAY_OF_MONTH, i);

            boolean isSelected = sameDay(d, base);
            String dayNumber = new SimpleDateFormat("d", vi).format(d.getTime());
            String label = weekdayShort[i];

            days.add(new DayChip(dayNumber, label, isSelected, d));
        }
    }

    /** Tìm index của day selected */
    private int findSelectedIndex() {
        for (int i = 0; i < days.size(); i++)
            if (days.get(i).isSelected())
                return i;
        return -1;
    }

    /** Cập nhật text ngày to */
    private void updateBigDateText() {
        String big = new SimpleDateFormat("d 'tháng' M, yyyy", vi).format(selectedDate.getTime());
        vb.txtBigDate.setText(big);
    }

    /** So sánh cùng ngày (không xét giờ) */
    private boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    /** Lọc và hiển thị lịch theo ngày đã chọn từ danh sách courses */
    private void updateSchedulesForSelectedDate() {
        ArrayList<ScheduleItem> list = new ArrayList<>();
        if (!allCourses.isEmpty()) {
            for (Course c : allCourses) {
                if (isSameDayIso(c.getStartTime(), selectedDate)) {
                    String start = formatTime(c.getStartTime());
                    String end   = formatTime(c.getEndTime());
                    boolean attended = c.getStatus() != null && c.getStatus().equalsIgnoreCase("present");
                    ScheduleItem item = new ScheduleItem(start, end, c.getStartTime(), c.getEndTime(), c.getSubjectName(), c.getClassId(), "", attended, c.getCourseId());
                    // Set className từ classroomMap nếu có
                    if (c.getClassId() != null && classroomMap.containsKey(c.getClassId())) {
                        item.setClassName(classroomMap.get(c.getClassId()));
                    }
                    list.add(item);
                }
            }
        }

        // sắp xếp theo thời gian bắt đầu tăng dần
        java.util.Collections.sort(list, (a, b) -> Integer.compare(
                timeMinutesIso(a.getStartIso()),
                timeMinutesIso(b.getStartIso())
        ));

        vb.rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleAdapter = new ScheduleAdapter(list, item -> {
            Intent i = new Intent(requireContext(), AttendanceActivity.class);
            i.putExtra("subject", item.getSubject());
            i.putExtra("time", item.getStart() + (item.getEnd() == null || item.getEnd().isEmpty() ? "" : " - " + item.getEnd()));
            i.putExtra("class", item.getClassId());
            // Truyền tên lớp từ ScheduleItem (đã được set từ API)
            String className = item.getClassName() != null ? item.getClassName() : item.getClassId();
            i.putExtra("className", className);
            i.putExtra("courseId", item.getCourseId());
            // Nếu chưa điểm danh (API status = unpresent) thì yêu cầu AttendanceActivity tự fetch học sinh
            i.putExtra("shouldFetchStudents", !item.isAttended());
            i.putExtra("isPresent", item.isAttended());
            startActivity(i);
        }, classroomMap);
        vb.rvSchedules.setAdapter(scheduleAdapter);
    }

    /** Gọi API lấy courses theo teacherId */
    private void fetchCoursesForTeacher() {
        SessionManager sm = new SessionManager(requireContext());
        String teacherId = sm.getTeacherId();
        String token = sm.getToken();
        if (teacherId == null || token == null) {
            updateSchedulesForSelectedDate();
            return;
        }
        ApiClient.ApiService api = ApiClient.service();
        api.getCoursesByTeacher(teacherId, "Bearer " + token).enqueue(new retrofit2.Callback<List<Course>>() {
            @Override public void onResponse(retrofit2.Call<List<Course>> call, retrofit2.Response<List<Course>> response) {
                allCourses.clear();
                if (response.isSuccessful() && response.body() != null) {
                    allCourses.addAll(response.body());
                }
                // Sau khi có courses, fetch classrooms cho từng classId
                fetchClassroomsForCourses();
                updateSchedulesForSelectedDate();
            }

            @Override public void onFailure(retrofit2.Call<List<Course>> call, Throwable t) {
                updateSchedulesForSelectedDate();
            }
        });
    }

    private boolean isSameDayIso(String iso, Calendar day) {
        try {
            // iso: yyyy-MM-ddTHH:mm:ss
            String[] parts = iso.split("T");
            String[] ymd = parts[0].split("-");
            int y = Integer.parseInt(ymd[0]);
            int m = Integer.parseInt(ymd[1]) - 1;
            int d = Integer.parseInt(ymd[2]);
            return day.get(Calendar.YEAR) == y && day.get(Calendar.MONTH) == m && day.get(Calendar.DAY_OF_MONTH) == d;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatTime(String iso) {
        if (iso == null || !iso.contains("T")) return "";
        int t = iso.indexOf('T');
        int end = Math.min(iso.length(), t + 6); // HH:mm
        String hm = iso.substring(t + 1, end);
        String[] sp = hm.split(":");
        int h = Integer.parseInt(sp[0]);
        int min = Integer.parseInt(sp[1]);
        String suffix = h < 12 ? "SA" : "CH";
        int h12 = h % 12; if (h12 == 0) h12 = 12;
        return h12 + ":" + (min < 10 ? "0" + min : min) + " " + suffix;
    }

    // Lấy tổng phút từ đầu ngày cho chuỗi ISO, phục vụ sắp xếp
    private int timeMinutesIso(String iso) {
        if (iso == null || !iso.contains("T")) return 0;
        int t = iso.indexOf('T');
        int end = Math.min(iso.length(), t + 6);
        String hm = iso.substring(t + 1, end); // HH:mm
        String[] sp = hm.split(":");
        int h = Integer.parseInt(sp[0]);
        int m = Integer.parseInt(sp[1]);
        return h * 60 + m;
    }

    // bỏ vì đã dùng startIso thực

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
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
        SessionManager sm = new SessionManager(requireContext());
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
                    // Cập nhật adapter với classroomMap mới
                    if (scheduleAdapter != null) {
                        scheduleAdapter.setClassroomMap(classroomMap);
                    }
                    // Cập nhật lại danh sách schedule với tên lớp đúng
                    updateSchedulesForSelectedDate();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Classroom>> call, Throwable t) {
                // Nếu lỗi, vẫn hiển thị classId như cũ
                android.util.Log.e("TimeTableFragment", "Failed to fetch classrooms", t);
            }
        });
    }

    /** Gọi API getClassroom cho từng classId unique trong danh sách courses */
    private void fetchClassroomsForCourses() {
        SessionManager sm = new SessionManager(requireContext());
        String token = sm.getToken();
        if (token == null) {
            return;
        }

        // Lấy tất cả classId unique từ danh sách courses
        java.util.Set<String> classIds = new java.util.HashSet<>();
        for (Course course : allCourses) {
            if (course.getClassId() != null && !course.getClassId().isEmpty()) {
                classIds.add(course.getClassId());
            }
        }

        if (classIds.isEmpty()) {
            return;
        }

        // Gọi API getClassroom cho từng classId
        ApiClient.ApiService api = ApiClient.service();
        final int[] completedCount = {0};
        final int totalCount = classIds.size();

        for (String classId : classIds) {
            api.getClassroom(classId, "Bearer " + token).enqueue(new retrofit2.Callback<Classroom>() {
                @Override
                public void onResponse(retrofit2.Call<Classroom> call, retrofit2.Response<Classroom> response) {
                    synchronized (classroomMap) {
                        completedCount[0]++;
                        if (response.isSuccessful() && response.body() != null) {
                            Classroom classroom = response.body();
                            if (classroom.getClassId() != null && classroom.getClassName() != null) {
                                classroomMap.put(classroom.getClassId(), classroom.getClassName());
                            }
                        }
                        // Khi tất cả đã hoàn thành, cập nhật UI
                        if (completedCount[0] == totalCount) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    // Cập nhật adapter với classroomMap mới
                                    if (scheduleAdapter != null) {
                                        scheduleAdapter.setClassroomMap(classroomMap);
                                    }
                                    // Cập nhật lại danh sách schedule với tên lớp đúng
                                    updateSchedulesForSelectedDate();
                                });
                            }
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Classroom> call, Throwable t) {
                    synchronized (classroomMap) {
                        completedCount[0]++;
                        android.util.Log.e("TimeTableFragment", "Failed to fetch classroom: " + call.request().url(), t);
                        // Khi tất cả đã hoàn thành (kể cả lỗi), vẫn cập nhật UI
                        if (completedCount[0] == totalCount) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (scheduleAdapter != null) {
                                        scheduleAdapter.setClassroomMap(classroomMap);
                                    }
                                    updateSchedulesForSelectedDate();
                                });
                            }
                        }
                    }
                }
            });
        }
    }
}
