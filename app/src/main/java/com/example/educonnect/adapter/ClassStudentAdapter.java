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
        private final String name;
        private final String dob;
        public Student(String n, String d){ name = n; dob = d; }
        
        public String getName() { return name; }
        public String getDob() { return dob; }
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
        h.vb.tvName.setText(s.getName());
        h.vb.tvDob.setText(s.getDob());

        h.itemView.setOnClickListener(v -> {
            Context context = h.itemView.getContext();

            // Lấy studentId thật (nếu có dữ liệu API)
            String studentId = "";
            if (fullStudentData != null && pos < fullStudentData.size()) {
                studentId = fullStudentData.get(pos).getStudentId();
            }

            if (studentId == null || studentId.isEmpty()) {
                Toast.makeText(context, "Không tìm thấy ID học sinh này", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mở màn hình ParentProfileActivity và truyền studentId qua Intent
            Intent intent = new Intent(context, ParentProfileActivity.class);
            intent.putExtra(ParentProfileActivity.EXTRA_STUDENT_ID, studentId);
            context.startActivity(intent);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemStudentSimpleBinding vb;
        VH(ItemStudentSimpleBinding b){ super(b.getRoot()); vb = b; }
    }
}
