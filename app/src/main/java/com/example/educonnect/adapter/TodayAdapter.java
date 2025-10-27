package com.example.educonnect.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educonnect.databinding.ItemTodayBinding;
import java.util.ArrayList;

public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.VH> {
    private final ArrayList<TodayItem> data;
    public TodayAdapter(ArrayList<TodayItem> d) { data = d; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemTodayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        var it = data.get(pos);
        h.vb.txtCourse.setText(it.course);
        h.vb.txtRoom.setText(it.room);
        h.vb.txtTime.setText(it.time);
    }
    @Override public int getItemCount() { return data==null?0:data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemTodayBinding vb;
        VH(ItemTodayBinding b){ super(b.getRoot()); vb=b; }
    }
}
