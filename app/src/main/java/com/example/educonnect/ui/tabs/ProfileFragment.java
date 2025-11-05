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
import com.example.educonnect.ui.login.LoginActivity;
import com.example.educonnect.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding vb;
    private SessionManager sessionManager;

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

        sessionManager = new SessionManager(requireContext());
        
        // Lấy thông tin từ SessionManager và hiển thị
        String fullName = sessionManager.getFullName();
        String email = sessionManager.getEmail();
        
        if (fullName != null && !fullName.isEmpty()) {
            vb.txtName.setText(fullName);
        } else {
            vb.txtName.setText("N/A");
        }
        
        if (email != null && !email.isEmpty()) {
            vb.txtEmail.setText(email);
        } else {
            vb.txtEmail.setText("N/A");
        }
        
        // Giữ thông tin lớp học và năm học (có thể lấy từ API sau)
        vb.txtClassTitle.setText("10A1 - Lớp 10A1");
        vb.txtSchoolYear.setText("Năm học: 2025 - 2026");

        // bấm vào chính card lớp -> mở danh sách học sinh
        vb.classItem.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), com.example.educonnect.ui.profile.ClassListActivity.class);
            i.putExtra("klass", "10A1");
            String teacherName = fullName != null ? fullName : "Nguyễn Văn A";
            i.putExtra("teacher", teacherName);
            i.putExtra("year", "2025-2026");
            startActivity(i);
        });

        // Xử lý đăng xuất
        vb.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        vb = null; // tránh leak
    }
}
