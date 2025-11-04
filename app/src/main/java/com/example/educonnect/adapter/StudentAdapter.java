package com.example.educonnect.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educonnect.R;
import com.example.educonnect.databinding.ItemStudentStatusBinding;
import com.example.educonnect.model.Student;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.VH> {

    public interface OnChanged { void onChanged(); }

    private final List<Student> data;
    private final OnChanged onChanged;

    public StudentAdapter(List<Student> d, OnChanged cb) {
        data = d; onChanged = cb;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(ItemStudentStatusBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        final Context ctx = h.itemView.getContext();   // dùng 1 lần ở đây
        final Student st = data.get(pos);
        h.vb.tvName.setText(st.name);
        applyStatus(ctx, h, st.status);

        // Bắt tên (string) để dùng trong lambda, tránh bắt cả đối tượng st
        final String studentName = st.name;
        final String note = (st.note != null && !st.note.isEmpty() && !st.note.equals("no")) ? st.note : "";
        final String homework = (st.homework != null && !st.homework.isEmpty() && !st.homework.equals("no")) ? st.homework : "";
        final String focus = (st.focus != null && !st.focus.isEmpty() && !st.focus.equals("no")) ? st.focus : "";
        final int position = pos; // Lưu position để cập nhật sau

        // Mở trang "Thái độ học tập" khi bấm vào tên
        h.vb.tvName.setOnClickListener(v -> {
            // Kiểm tra xem context có phải Activity không để dùng startActivityForResult
            if (ctx instanceof android.app.Activity) {
                android.app.Activity activity = (android.app.Activity) ctx;
                Intent i = new Intent(ctx, com.example.educonnect.ui.attendance.StudentEvaluationActivity.class);
                i.putExtra("student_name", studentName);
                i.putExtra("note", note);
                i.putExtra("homework", homework);
                i.putExtra("focus", focus);
                activity.startActivityForResult(i, position);
            } else {
                // Fallback nếu không phải Activity
                Intent i = new Intent(ctx, com.example.educonnect.ui.attendance.StudentEvaluationActivity.class);
                i.putExtra("student_name", studentName);
                i.putExtra("note", note);
                i.putExtra("homework", homework);
                i.putExtra("focus", focus);
                ctx.startActivity(i);
            }
        });

        // Đổi trạng thái điểm danh khi bấm chip
        h.vb.chipStatus.setOnClickListener(v -> {
            switch (st.status) {
                case PRESENT: st.status = Student.Status.LATE; break;
                case LATE:    st.status = Student.Status.ABSENT; break;
                default:      st.status = Student.Status.PRESENT; break;
            }
            applyStatus(ctx, h, st.status);
            if (onChanged != null) onChanged.onChanged();
        });
    }


    private void applyStatus(Context ctx, VH h, Student.Status s) {
        if (s == Student.Status.PRESENT) {
            h.vb.chipStatus.setText("Có mặt");
            h.vb.chipStatus.setChipBackgroundColorResource(R.color.green);
            h.vb.chipStatus.setChipIconResource(R.drawable.ic_check);
            h.vb.chipStatus.setTextColor(ctx.getColor(R.color.primary));
            h.vb.chipStatus.setChipIconTintResource(R.color.primary);
        } else if (s == Student.Status.LATE) {
            h.vb.chipStatus.setText("Đi muộn");
            h.vb.chipStatus.setChipBackgroundColorResource(R.color.amber);
            h.vb.chipStatus.setChipIconResource(R.drawable.ic_time);
            h.vb.chipStatus.setTextColor(ctx.getColor(R.color.primary));
            h.vb.chipStatus.setChipIconTintResource(R.color.primary);
        } else {
            h.vb.chipStatus.setText("Vắng");
            h.vb.chipStatus.setChipBackgroundColorResource(R.color.red);
            h.vb.chipStatus.setChipIconResource(R.drawable.ic_close);
            h.vb.chipStatus.setTextColor(ctx.getColor(R.color.primary));
            h.vb.chipStatus.setChipIconTintResource(R.color.primary);
        }
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemStudentStatusBinding vb;
        VH(ItemStudentStatusBinding b) { super(b.getRoot()); vb = b; }
    }
}
