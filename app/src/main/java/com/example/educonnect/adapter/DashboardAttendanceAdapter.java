package com.example.educonnect.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educonnect.databinding.ItemStudentDashboardBinding;
import com.example.educonnect.R;
import java.util.List;

public class DashboardAttendanceAdapter extends RecyclerView.Adapter<DashboardAttendanceAdapter.VH> {

    public static class AttendanceDisplayItem {
        public String studentName;
        public String className;
        public String subjectName;
        public String timeRange; // "7:50 SA - 8:35 SA"
        public String notes;
        public String participation; // "Vắng mặt", "Đi trễ", "Có mặt"
        public boolean isAbsent; // true nếu là "Vắng mặt"

        public AttendanceDisplayItem(String studentName, String className, String subjectName,
                                   String timeRange, String notes, String participation) {
            this.studentName = studentName;
            this.className = className;
            this.subjectName = subjectName;
            this.timeRange = timeRange;
            this.notes = notes != null && !notes.isEmpty() ? notes : "Không có ghi chú";
            this.participation = participation;
            this.isAbsent = "Vắng mặt".equals(participation);
        }
    }

    private final List<AttendanceDisplayItem> data;

    public DashboardAttendanceAdapter(List<AttendanceDisplayItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemStudentDashboardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AttendanceDisplayItem item = data.get(position);
        
        holder.vb.tvStudentName.setText(item.studentName);
        holder.vb.tvStudentClass.setText(item.className);
        holder.vb.tvSubject.setText(item.subjectName);
        holder.vb.tvTime.setText(item.timeRange);
        holder.vb.tvNotes.setText(item.notes);
        holder.vb.tvStatus.setText(item.participation);

        // Đặt màu và icon cho status
        int statusColor;
        int statusIconRes;
        if ("Vắng mặt".equals(item.participation)) {
            statusColor = holder.vb.getRoot().getContext().getColor(R.color.status_absent);
            statusIconRes = R.drawable.ic_status_absent;
        } else if ("Đi trễ".equals(item.participation)) {
            statusColor = holder.vb.getRoot().getContext().getColor(R.color.status_late);
            statusIconRes = R.drawable.ic_status_late;
        } else {
            statusColor = holder.vb.getRoot().getContext().getColor(R.color.status_present);
            statusIconRes = R.drawable.ic_status_present;
        }
        holder.vb.tvStatus.setTextColor(statusColor);
        holder.vb.ivStatusIcon.setImageResource(statusIconRes);

        // Hiển thị thanh đỏ bên trái nếu là "Vắng mặt"
        if (item.isAbsent) {
            holder.vb.getRoot().setBackgroundResource(R.drawable.bg_card_absent);
        } else {
            holder.vb.getRoot().setBackgroundResource(R.drawable.bg_card);
        }
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemStudentDashboardBinding vb;

        VH(ItemStudentDashboardBinding binding) {
            super(binding.getRoot());
            this.vb = binding;
        }
    }
}
