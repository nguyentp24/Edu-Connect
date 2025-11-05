package com.example.educonnect.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educonnect.R;
import com.example.educonnect.model.DayChip;
import java.util.List;

public class DayChipAdapter extends RecyclerView.Adapter<DayChipAdapter.VH> {
    public interface OnDayClick { void onClick(int position); }

    private final List<DayChip> data;
    private final OnDayClick onClick;

    public DayChipAdapter(List<DayChip> data, OnDayClick onClick) {
        this.data = data; this.onClick = onClick;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_day_chip, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        DayChip d = data.get(pos);
        h.dayTop.setText(d.getDayNumber());
        h.weekday.setText(d.getDayLabel());

        // set selected để đổi background
        h.container.setSelected(d.isSelected());

        // màu chữ: selected = trắng, normal = xám
        int selectedText = 0xFFFFFFFF;
        int normalText   = 0xFF6B7280; // xám đậm nhẹ

        int color = d.isSelected() ? selectedText : normalText;
        h.dayTop.setTextColor(color);
        h.weekday.setTextColor(color);

        h.itemView.setOnClickListener(v -> onClick.onClick(pos));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView dayTop, weekday;
        View container;
        VH(@NonNull View v) {
            super(v);
            dayTop    = v.findViewById(R.id.txtDayTop);
            weekday   = v.findViewById(R.id.txtWeekday);
            container = v.findViewById(R.id.chipContainer);
        }
    }

}
