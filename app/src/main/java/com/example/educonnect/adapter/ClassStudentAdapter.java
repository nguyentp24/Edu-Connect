package com.example.educonnect.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educonnect.databinding.ItemStudentSimpleBinding;

import java.util.List;

public class ClassStudentAdapter extends RecyclerView.Adapter<ClassStudentAdapter.VH> {

    public static class Student {
        public final String name;
        public final String dob;
        public Student(String n, String d){ name = n; dob = d; }
    }

    private final List<Student> data;

    public ClassStudentAdapter(List<Student> d){
        data = d;
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
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemStudentSimpleBinding vb;
        VH(ItemStudentSimpleBinding b){ super(b.getRoot()); vb = b; }
    }
}
