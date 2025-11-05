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
import java.util.Map;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {

    public interface OnItemClick { void onClick(ScheduleItem item); }

    private final List<ScheduleItem> data;
    private final OnItemClick onItemClick;
    private Map<String, String> classroomMap; // Map: classId -> className

    public ScheduleAdapter(List<ScheduleItem> data, OnItemClick onItemClick) {
        this.data = data;
        this.onItemClick = onItemClick;
        this.classroomMap = null;
    }

    public ScheduleAdapter(List<ScheduleItem> data, OnItemClick onItemClick, Map<String, String> classroomMap) {
        this.data = data;
        this.onItemClick = onItemClick;
        this.classroomMap = classroomMap;
    }
    
    public void setClassroomMap(Map<String, String> classroomMap) {
        this.classroomMap = classroomMap;
        notifyDataSetChanged(); // Cập nhật lại UI khi có classroomMap
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(ItemScheduleBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ScheduleItem it = data.get(pos);
        h.vb.txtTimeStart.setText(it.getStart());
        h.vb.txtTimeEnd.setText(it.getEnd());
        h.vb.txtSubject.setText(it.getSubject());
        h.vb.txtStatus.setText(it.getStatus());
        h.vb.txtClass.setText("Lớp: " + mapClassIdToName(it.getClassId()));
        // Cập nhật trạng thái theo thời gian
        updateTimeStatus(h, it);
        // Attendance
        h.vb.txtAttendance.setText(it.isAttended() ? "Đã điểm danh" : "Chưa điểm danh");
        int color = it.isAttended() ? h.vb.getRoot().getContext().getColor(com.example.educonnect.R.color.green)
                                 : h.vb.getRoot().getContext().getColor(com.example.educonnect.R.color.red);
        h.vb.txtAttendance.setTextColor(color);

        android.view.View dot = h.vb.getRoot().findViewById(com.example.educonnect.R.id.dotStatus);
        if (dot != null) {
            dot.setBackgroundResource(it.isAttended() ? com.example.educonnect.R.drawable.dot_green
                                                  : com.example.educonnect.R.drawable.dot_red);
        }

        h.vb.getRoot().setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(it);
        });


    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemScheduleBinding vb;
        VH(ItemScheduleBinding b){ super(b.getRoot()); vb = b; }
    }

    private void updateTimeStatus(VH h, ScheduleItem it) {
        long now = System.currentTimeMillis();
        long start = parseIsoToMillis(it.getStartIso());
        long end = parseIsoToMillis(it.getEndIso());
        if (start <= 0 || end <= 0) {
            h.vb.txtStatus.setText("");
            return;
        }

        if (now < start) {
            long minutesLeft = (start - now) / (60 * 1000);
            if (minutesLeft >= 60) {
                h.vb.txtStatus.setText("Sắp bắt đầu");
                h.vb.txtStatus.setTextColor(h.vb.getRoot().getContext().getColor(com.example.educonnect.R.color.green));
            } else {
                h.vb.txtStatus.setText(minutesLeft + " phút trước khi bắt đầu");
                h.vb.txtStatus.setTextColor(h.vb.getRoot().getContext().getColor(com.example.educonnect.R.color.amber));
            }
        } else if (now >= start && now <= end) {
            h.vb.txtStatus.setText("Đang bắt đầu");
            h.vb.txtStatus.setTextColor(h.vb.getRoot().getContext().getColor(com.example.educonnect.R.color.active));
        } else { // now > end
            h.vb.txtStatus.setText("Đã bắt đầu");
            h.vb.txtStatus.setTextColor(h.vb.getRoot().getContext().getColor(com.example.educonnect.R.color.red));
        }
    }

    private long parseIsoToMillis(String iso) {
        try {
            // Format: yyyy-MM-ddTHH:mm:ss
            String[] parts = iso.split("T");
            String[] ymd = parts[0].split("-");
            String[] hms = parts[1].split(":");
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.set(java.lang.Integer.parseInt(ymd[0]), java.lang.Integer.parseInt(ymd[1]) - 1, java.lang.Integer.parseInt(ymd[2]),
                    java.lang.Integer.parseInt(hms[0]), java.lang.Integer.parseInt(hms[1]), java.lang.Integer.parseInt(hms[2]));
            c.set(java.util.Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (Exception e) {
            return -1L;
        }
    }

    private String mapClassIdToName(String classId) {
        if (classId == null) return "";
        // Nếu đã có classroomMap, dùng nó
        if (classroomMap != null && classroomMap.containsKey(classId)) {
            return classroomMap.get(classId);
        }
        // Fallback: trả về classId
        return classId;
    }
}

