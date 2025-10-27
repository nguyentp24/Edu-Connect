package com.example.educonnect.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.educonnect.R;
import com.example.educonnect.databinding.ActivityMainBinding;
import com.example.educonnect.ui.tabs.DashboardFragment;
import com.example.educonnect.ui.tabs.TimeTableFragment;
import com.example.educonnect.ui.tabs.ReportFragment;
import com.example.educonnect.ui.tabs.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding vb;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        vb = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Add bottom inset to BottomNavigationView
        ViewCompat.setOnApplyWindowInsetsListener(vb.bottomNav, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), sys.bottom);
            return insets;
        });

        // (khuyến nghị) thêm bottom padding cho nội dung list để không bị che
        ViewCompat.setOnApplyWindowInsetsListener(vb.fragmentContainer, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), 0);
            return insets;
        });

        switchFragment(new DashboardFragment());

        vb.bottomNav.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.nav_dashboard)      f = new DashboardFragment();
            else if (id == R.id.nav_timetable) f = new TimeTableFragment();
            else if (id == R.id.nav_report)    f = new ReportFragment();
            else                                f = new ProfileFragment();
            switchFragment(f);
            return true;
        });
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
