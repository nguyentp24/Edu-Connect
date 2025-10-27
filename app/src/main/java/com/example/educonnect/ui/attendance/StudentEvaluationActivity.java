package com.example.educonnect.ui.attendance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.educonnect.databinding.ActivityStudentEvaluationBinding;

public class StudentEvaluationActivity extends AppCompatActivity {

    private ActivityStudentEvaluationBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ActivityStudentEvaluationBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        String name = getIntent().getStringExtra("student_name");
        if (name != null && !name.isEmpty()) {
            // gắn vào tiêu đề nếu bạn muốn
            vb.tvTitle.setText(getString(com.example.educonnect.R.string.study_attitude_title) + " - " + name);
        }

        // Nút Lưu: lấy 2 lựa chọn + ghi chú
        vb.btnSave.setOnClickListener(v -> {
            int attId = vb.groupAttitude.getCheckedChipId();
            int prepId = vb.groupPreparation.getCheckedChipId();
            String attitude = attId == vb.chipVeryGood.getId() ? "Rất tốt" :
                    attId == vb.chipGood.getId() ? "Tốt" :
                            attId == vb.chipAverage.getId() ? "Trung bình" : "Kém";
            String prep = prepId == vb.chipPrepGood.getId() ? "Hoàn thành bài tốt" :
                    prepId == vb.chipPrepHas.getId() ? "Có chuẩn bị bài" :
                            prepId == vb.chipPrepImprove.getId() ? "Cần cải thiện thêm" :
                                    "Chưa chuẩn bị bài";
            String note = vb.edtNote.getText().toString().trim();

            // TODO: gửi về server / trả về AttendanceActivity
            Toast.makeText(this, "Lưu: " + attitude + " | " + prep, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
