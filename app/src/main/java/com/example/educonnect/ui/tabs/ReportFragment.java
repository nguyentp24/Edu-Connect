package com.example.educonnect.ui.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.educonnect.databinding.FragmentReportBinding;

public class ReportFragment extends Fragment {
    private FragmentReportBinding vb;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vb = FragmentReportBinding.inflate(inflater, container, false);
        vb.btnSubmitReport.setOnClickListener(v -> Toast.makeText(getContext(),"Report submitted!",Toast.LENGTH_SHORT).show());
        return vb.getRoot();
    }
}
