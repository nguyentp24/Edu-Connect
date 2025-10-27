package com.example.educonnect.ui.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.educonnect.databinding.FragmentProfileBinding;
import com.example.educonnect.ui.profile.ClassListActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding vb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        vb = FragmentProfileBinding.inflate(inflater, container, false);
        return vb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // gán text demo
        vb.txtName.setText("Văn A Nguyễn");
        vb.txtEmail.setText("teacher1@gmail.com");
        vb.txtClassTitle.setText("10A1 - Lớp 10A1");
        vb.txtSchoolYear.setText("Năm học: 2025 - 2026");

        // bấm vào chính card lớp -> mở danh sách học sinh
        vb.classItem.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), com.example.educonnect.ui.profile.ClassListActivity.class);
            i.putExtra("klass", "10A1");
            i.putExtra("teacher", "Nguyễn Văn A");
            i.putExtra("year", "2025-2026");
            startActivity(i);
        });

        vb.btnLogout.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Đăng xuất", Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        vb = null; // tránh leak
    }
}
