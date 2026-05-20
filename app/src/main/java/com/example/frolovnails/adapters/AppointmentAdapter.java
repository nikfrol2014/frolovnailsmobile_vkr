package com.example.frolovnails.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.Appointment;


import java.util.ArrayList;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    public void setOnAppointmentClickListener(OnAppointmentClickListener listener) {
        this.listener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvClient, tvService, tvStatus;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvClient = itemView.findViewById(R.id.tvClient);
            tvService = itemView.findViewById(R.id.tvService);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(Appointment appointment, OnAppointmentClickListener listener) {
            tvTime.setText(appointment.getStartTime() + " - " + appointment.getEndTime());
            tvClient.setText(appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName());
            tvService.setText(appointment.getService().getName());

            String status = String.valueOf(appointment.getStatus());
            tvStatus.setText(status);

            // Цвет статуса
            switch (status) {
                case "CONFIRMED":
                    tvStatus.setTextColor(0xFF4CAF50);
                    break;
                case "PENDING":
                case "CREATED":
                    tvStatus.setTextColor(0xFFFF9800);
                    break;
                case "CANCELLED":
                    tvStatus.setTextColor(0xFFF44336);
                    break;
                case "COMPLETED":
                    tvStatus.setTextColor(0xFF2196F3);
                    break;
                default:
                    tvStatus.setTextColor(0xFF9E9E9E);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppointmentClick(appointment);
                }
            });
        }
    }
}