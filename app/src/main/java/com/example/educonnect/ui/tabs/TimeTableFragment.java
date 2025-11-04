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
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.utils.SessionManager;
import com.example.educonnect.ui.attendance.AttendanceActivity;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimeTableFragment extends Fragment {

    private FragmentTimetableBinding vb;
    private final List<DayChip> days = new ArrayList<>();
    private DayChipAdapter chipAdapter;
    private Calendar selectedDate;     // ngày đang chọn
    private final Locale vi = new Locale("vi", "VN");
    private final List<Course> allCourses = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vb = FragmentTimetableBinding.inflate(inflater, container, false);

        // Ngày mặc định: hôm nay (demo bạn có thể set cứng một ngày)
        selectedDate = Calendar.getInstance();
        // selectedDate.set(2025, Calendar.JULY, 24);

        setupHeaderAndChips();
        // Tải dữ liệu thật khi vào màn
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
//        final int spacingPx = dp(0);
//        vb.rvDays.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//                outRect.left = spacingPx;
//                outRect.right = spacingPx;
//            }
//        });
        buildWeekChipsAround(selectedDate);

        chipAdapter = new DayChipAdapter(days, pos -> {
            // chọn ngày theo chip
            for (int i = 0; i < days.size(); i++) days.get(i).selected = (i == pos);
            selectedDate = (Calendar) days.get(pos).date.clone();
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

    /** Tìm index của chip selected */
    private int findSelectedIndex() {
        for (int i = 0; i < days.size(); i++) if (days.get(i).selected) return i;
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
                if (isSameDayIso(c.startTime, selectedDate)) {
                    String start = formatTime(c.startTime);
                    String end   = formatTime(c.endTime);
                    boolean attended = c.status != null && c.status.equalsIgnoreCase("present");
                    list.add(new ScheduleItem(start, end, c.startTime, c.endTime, c.subjectName, c.classId, "", attended));
                }
            }
        }

        // sắp xếp theo thời gian bắt đầu tăng dần
        java.util.Collections.sort(list, (a, b) -> Integer.compare(
                timeMinutesIso(a.startIso),
                timeMinutesIso(b.startIso)
        ));

        vb.rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));
        vb.rvSchedules.setAdapter(new ScheduleAdapter(list, item -> {
            Intent i = new Intent(requireContext(), AttendanceActivity.class);
            i.putExtra("subject", item.subject);
            i.putExtra("time", item.start + (item.end == null || item.end.isEmpty() ? "" : " - " + item.end));
            i.putExtra("klass", item.klass);
            startActivity(i);
        }));
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
}
