package com.example.frolovnails.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;

import java.util.ArrayList;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<String> dates = new ArrayList<>();
    private int selectedPosition = 0;
    private OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(String date, int position);
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.listener = listener;
    }

    public void setDates(List<String> dates) {
        Log.d("DateAdapter", "setDates called with " + (dates == null ? "null" : dates.size()) + " items");
        this.dates = dates;
        notifyDataSetChanged();
        Log.d("DateAdapter", "notifyDataSetChanged called");
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dates.get(position);
        holder.bind(date, position == selectedPosition, listener);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        void bind(String date, boolean isSelected, OnDateClickListener listener) {
            tvDate.setText(date);
            tvDate.setSelected(isSelected);
            if (isSelected) {
                tvDate.setBackgroundResource(android.R.drawable.editbox_background);
            } else {
                tvDate.setBackgroundResource(0);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDateClick(date, getAdapterPosition());
                }
            });
        }
    }
}