package com.example.educonnect.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educonnect.databinding.ItemScheduleBinding;
import com.example.educonnect.model.ScheduleItem;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {

    public interface OnItemClick { void onClick(ScheduleItem item); }

    private final List<ScheduleItem> data;
    private final OnItemClick onItemClick;

    public ScheduleAdapter(List<ScheduleItem> data, OnItemClick onItemClick) {
        this.data = data;
        this.onItemClick = onItemClick;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(ItemScheduleBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ScheduleItem it = data.get(pos);
        h.vb.txtTimeStart.setText(it.start);
        h.vb.txtTimeEnd.setText(it.end);
        h.vb.txtSubject.setText(it.subject);
        h.vb.txtStatus.setText(it.status);
        h.vb.txtClass.setText("Lớp: " + it.classroom);
        h.vb.txtAttendance.setText(it.attended ? "Đã điểm danh" : "Chưa điểm danh");

        h.vb.getRoot().setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(it);
        });


    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemScheduleBinding vb;
        VH(ItemScheduleBinding b){ super(b.getRoot()); vb = b; }
    }
}

