package com.example.frolovnails.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.AvailableDay;

import java.util.ArrayList;
import java.util.List;

public class AvailableDaysAdapter extends RecyclerView.Adapter<AvailableDaysAdapter.ViewHolder> {

    private List<AvailableDay> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(AvailableDay item);
        void onDeleteClick(AvailableDay item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AvailableDay> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AvailableDay item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWorkHours, tvNotes;
        Button btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWorkHours = itemView.findViewById(R.id.tvWorkHours);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(AvailableDay item, OnItemClickListener listener) {
            tvDate.setText(item.getAvailableDate());
            tvWorkHours.setText(item.getWorkStart() + " — " + item.getWorkEnd());
            tvNotes.setText(item.getNotes() != null ? item.getNotes() : "");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(item);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        }
    }
}