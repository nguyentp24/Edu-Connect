package com.example.educonnect.ui.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.educonnect.R;
import com.example.educonnect.model.Report;
public class ReportHistoryAdapter extends RecyclerView.Adapter<ReportHistoryAdapter.ReportViewHolder> {

    private List<Report> reportList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Report report);
    }

    public ReportHistoryAdapter(List<Report> reportList, OnItemClickListener listener) {
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.bind(report, listener);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, className, teacherName, createdDate;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_report_title);
            className = itemView.findViewById(R.id.tv_class_name);
            teacherName = itemView.findViewById(R.id.tv_teacher_name);
            createdDate = itemView.findViewById(R.id.tv_created_date);
        }

        public void bind(final Report report, final OnItemClickListener listener) {
            title.setText(report.getTitle());
            className.setText("Lớp: " + report.getClassName());
            teacherName.setText("Giáo viên chủ nhiệm: " + report.getTeacherName());

            if (report.getCreatedAt() != null) {
                createdDate.setText("Ngày tạo: " + formatDisplayDate(report.getCreatedAt()));
                createdDate.setVisibility(View.VISIBLE);
            } else {
                createdDate.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(report));
        }

        private String formatDisplayDate(String apiDate) {
            if (apiDate == null) return "N/A";
            try {
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("d 'thg' M, yyyy", new Locale("vi", "VN"));
                Date date = apiFormat.parse(apiDate);
                return displayFormat.format(date);
            } catch (ParseException e) {
                return apiDate;
            }
        }
    }
}