package com.example.educonnect.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educonnect.databinding.ItemStudentSimpleBinding;
import com.example.educonnect.ui.profile.ParentProfileActivity;

import java.util.List;

public class ClassStudentAdapter extends RecyclerView.Adapter<ClassStudentAdapter.VH> {

    public static class Student {
        public final String name;
        public final String dob;
        public Student(String n, String d){ name = n; dob = d; }
    }

    private final List<Student> data;

    private final List<com.example.educonnect.model.Student> fullStudentData;

    public ClassStudentAdapter(List<Student> d,List<com.example.educonnect.model.Student> fullData){
        data = d;
        this.fullStudentData = fullData;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemStudentSimpleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Student s = data.get(pos);
        h.vb.tvName.setText(s.name);
        h.vb.tvDob.setText(s.dob);

        h.itemView.setOnClickListener(v -> {
            // Dòng này giờ sẽ hoạt động vì `fullStudentData` đã được khai báo đúng
            String studentId = fullStudentData.get(pos).studentId;

            // SỬA LỖI 2: Dùng đúng lớp Context
            Context context = h.itemView.getContext();

            if (studentId != null && !studentId.isEmpty()) {
                Intent intent = new Intent(context, ParentProfileActivity.class);
                intent.putExtra(ParentProfileActivity.EXTRA_STUDENT_ID, studentId);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Không có ID cho học sinh này", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemStudentSimpleBinding vb;
        VH(ItemStudentSimpleBinding b){ super(b.getRoot()); vb = b; }
    }
}
