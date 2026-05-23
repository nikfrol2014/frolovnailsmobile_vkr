package com.example.frolovnails.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.ScheduleBlock;

import java.util.ArrayList;
import java.util.List;

public class ScheduleBlocksAdapter extends RecyclerView.Adapter<ScheduleBlocksAdapter.ViewHolder> {

    private List<ScheduleBlock> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(ScheduleBlock item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ScheduleBlock> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_block, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleBlock item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvReason, tvNotes;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(ScheduleBlock item, OnItemClickListener listener) {
            tvTime.setText(item.getStartTime() + " — " + item.getEndTime());
            tvReason.setText(item.getReason() != null ? item.getReason() : "Без причины");
            tvNotes.setText(item.getNotes() != null ? item.getNotes() : "");

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        }
    }
}