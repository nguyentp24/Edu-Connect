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
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding vb;

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
    }

    private String capitalizeFirst(String s){
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        vb = null;
    }
}
