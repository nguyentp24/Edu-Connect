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
        String note = getIntent().getStringExtra("note");
        String homework = getIntent().getStringExtra("homework");
        String focus = getIntent().getStringExtra("focus");

        if (name != null && !name.isEmpty()) {
            // gắn vào tiêu đề nếu bạn muốn
            vb.tvTitle.setText(getString(com.example.educonnect.R.string.study_attitude_title) + " - " + name);
        }

        // Load và hiển thị dữ liệu từ API
        loadNote(note);
        loadHomework(homework);
        loadFocus(focus);

        // Nút Lưu: lấy 2 lựa chọn + ghi chú
        vb.btnSave.setOnClickListener(v -> {
            int attId = vb.groupAttitude.getCheckedChipId();
            int prepId = vb.groupPreparation.getCheckedChipId();
            
            // Lấy focus (thái độ học tập) - nếu không chọn thì để rỗng
            String focusResult = "";
            if (attId == vb.chipVeryGood.getId()) {
                focusResult = "Rất tốt";
            } else if (attId == vb.chipGood.getId()) {
                focusResult = "Tốt";
            } else if (attId == vb.chipAverage.getId()) {
                focusResult = "Trung bình";
            } else if (attId == vb.chipPoor.getId()) {
                focusResult = "Kém";
            }
            
            // Lấy homework (bài học) - nếu không chọn thì để rỗng
            String homeworkResult = "";
            if (prepId == vb.chipPrepGood.getId()) {
                homeworkResult = "Hoàn thành bài tốt";
            } else if (prepId == vb.chipPrepHas.getId()) {
                homeworkResult = "Có chuẩn bị bài";
            } else if (prepId == vb.chipPrepImprove.getId()) {
                homeworkResult = "Cần cải thiện thêm";
            } else if (prepId == vb.chipPrepNone.getId()) {
                homeworkResult = "Chưa chuẩn bị bài";
            }
            
            // Lấy note (ghi chú)
            String noteResult = vb.edtNote.getText().toString().trim();
            if (noteResult.isEmpty()) {
                noteResult = "";
            }

            // Trả về kết quả cho AttendanceActivity
            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra("student_name", name);
            resultIntent.putExtra("note", noteResult);
            resultIntent.putExtra("homework", homeworkResult);
            resultIntent.putExtra("focus", focusResult);
            setResult(android.app.Activity.RESULT_OK, resultIntent);
            
            Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadNote(String note) {
        // Nếu không có hoặc là "no" thì để trống
        if (note != null && !note.isEmpty() && !note.equals("no")) {
            vb.edtNote.setText(note);
        } else {
            vb.edtNote.setText("");
        }
    }

    private void loadHomework(String homework) {
        if (homework == null || homework.isEmpty() || homework.equals("no")) {
            return;
        }
        // Map homework từ API sang chip tương ứng
        // "Hoàn thành bài tốt" -> chipPrepGood
        // "Có chuẩn bị bài" -> chipPrepHas
        // "Cần cải thiện thêm" -> chipPrepImprove
        // "Chưa chuẩn bị bài" -> chipPrepNone
        if (homework.contains("Hoàn thành") || homework.contains("tốt")) {
            vb.chipPrepGood.setChecked(true);
        } else if (homework.contains("Có chuẩn bị") || homework.contains("Có")) {
            vb.chipPrepHas.setChecked(true);
        } else if (homework.contains("Cần cải thiện") || homework.contains("Cải thiện")) {
            vb.chipPrepImprove.setChecked(true);
        } else if (homework.contains("Chưa") || homework.contains("Không")) {
            vb.chipPrepNone.setChecked(true);
        }
    }

    private void loadFocus(String focus) {
        if (focus == null || focus.isEmpty() || focus.equals("no")) {
            return;
        }
        // Map focus từ API sang chip tương ứng
        // "Rất tốt" -> chipVeryGood
        // "Tốt" -> chipGood
        // "Trung bình" -> chipAverage
        // "Kém" -> chipPoor
        if (focus.contains("Rất tốt") || focus.contains("Rất")) {
            vb.chipVeryGood.setChecked(true);
        } else if (focus.contains("Tốt")) {
            vb.chipGood.setChecked(true);
        } else if (focus.contains("Trung bình") || focus.contains("Trung")) {
            vb.chipAverage.setChecked(true);
        } else if (focus.contains("Kém")) {
            vb.chipPoor.setChecked(true);
        }
    }
}
