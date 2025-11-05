package com.example.educonnect.ui.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.educonnect.adapter.DayChipAdapter;
import com.example.educonnect.adapter.ScheduleAdapter;
import com.example.educonnect.databinding.FragmentTimetableBinding;
import com.example.educonnect.model.DayChip;
import com.example.educonnect.model.ScheduleItem;
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

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vb = FragmentTimetableBinding.inflate(inflater, container, false);

        // Ngày mặc định: hôm nay (demo bạn có thể set cứng một ngày)
        selectedDate = Calendar.getInstance();
        // selectedDate.set(2025, Calendar.JULY, 24);

        setupHeaderAndChips();
        setupSchedulesFor(selectedDate);  // dữ liệu demo

        return vb.getRoot();
    }

    /** Header + Chips + DatePicker */
    private void setupHeaderAndChips() {
        // Header text
        updateBigDateText();

        // Click vào header mở DatePicker
        vb.txtBigDate.setOnClickListener(v -> openDatePicker());

        // Chips RecyclerView
        vb.rvDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        buildWeekChipsAround(selectedDate);

        chipAdapter = new DayChipAdapter(days, pos -> {
            // chọn ngày theo chip
            for (int i = 0; i < days.size(); i++) days.get(i).selected = (i == pos);
            selectedDate = (Calendar) days.get(pos).date.clone();
            chipAdapter.notifyDataSetChanged();
            vb.rvDays.smoothScrollToPosition(pos);

            updateBigDateText();
            setupSchedulesFor(selectedDate);
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

            setupSchedulesFor(selectedDate);
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

    /** Dữ liệu lịch demo theo ngày */
    private void setupSchedulesFor(Calendar day) {
        // TODO: thay bằng call API/filter theo 'day' nếu có
        ArrayList<ScheduleItem> list = new ArrayList<>();
        list.add(new ScheduleItem("7:00 SA", "7:45 SA", "Toán", "12A1", "Đã kết thúc", false));
        list.add(new ScheduleItem("7:50 SA", "8:35 SA", "Toán", "12A6", "Đã kết thúc", false));
        list.add(new ScheduleItem("8:50 SA", "9:35 SA", "Toán", "12A2", "Đã kết thúc", false));
        list.add(new ScheduleItem("10:30 SA", "11:15 SA", "Toán", "10A1", "Đã kết thúc", false));
        list.add(new ScheduleItem("1:30 CH", "",          "Toán", "10A1", "Đã kết thúc", false));

        vb.rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));
        vb.rvSchedules.setAdapter(new ScheduleAdapter(list, item -> {
            Intent i = new Intent(requireContext(), AttendanceActivity.class);
            i.putExtra("subject", item.subject);
            i.putExtra("time", item.start + (item.end == null || item.end.isEmpty() ? "" : " - " + item.end));
            i.putExtra("class", item.classroom);
            startActivity(i);
        }));
    }
}
