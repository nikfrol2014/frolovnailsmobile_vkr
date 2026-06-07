package com.example.frolovnails.client;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyAppointmentsAdapter extends RecyclerView.Adapter<MyAppointmentsAdapter.ViewHolder> {

    private List<Appointment> appointments = new ArrayList<>();
    private OnCancelClickListener cancelClickListener;
    private OnItemClickListener itemClickListener;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnCancelClickListener {
        void onCancelClick(Appointment appointment);
    }

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.cancelClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, cancelClickListener, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvDateTime, tvStatus, tvPrice;
        Button btnCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        @SuppressLint("SetTextI18n")
        void bind(Appointment appointment,
                  OnCancelClickListener cancelListener,
                  OnItemClickListener itemListener) {

            tvServiceName.setText(appointment.getService().getName());
            tvDateTime.setText(appointment.getStartTime() + " — " + appointment.getEndTime());

            // Цена - приоритет actualPrice
            java.math.BigDecimal actualPrice = appointment.getActualPrice();
            java.math.BigDecimal displayPrice = actualPrice != null ? actualPrice : appointment.getService().getPrice();
            tvPrice.setText(displayPrice + " ₽");

            if (actualPrice != null) {
                tvPrice.setTextColor(0xFFFF9800); // Оранжевый
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tvPrice.setTooltipText("Фактическая цена (было " + appointment.getService().getPrice() + " ₽)");
                }
            } else {
                tvPrice.setTextColor(0xFF2196F3); // Синий
            }

            // Статус и цвет
            String status = appointment.getStatus().toString();
            tvStatus.setText(getStatusText(status));

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

            // Кнопка отмены только для активных предстоящих записей
            boolean isUpcoming = isAppointmentUpcoming(appointment);
            boolean isActive = status.equals("CONFIRMED") || status.equals("PENDING") || status.equals("CREATED");

            if (isUpcoming && isActive) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick(appointment);
                    }
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }

            // Клик по всей карточке для просмотра деталей
            itemView.setOnClickListener(v -> {
                if (itemListener != null) {
                    itemListener.onItemClick(appointment);
                }
            });
        }

        private boolean isAppointmentUpcoming(Appointment appointment) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                Calendar aptTime = Calendar.getInstance();
                aptTime.setTime(sdf.parse(appointment.getStartTime()));
                Calendar now = Calendar.getInstance();
                return aptTime.after(now);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }

        private String getStatusText(String status) {
            switch (status) {
                case "CONFIRMED": return "✅ Подтверждено";
                case "PENDING": return "⏳ Ожидание";
                case "CREATED": return "🆕 Создано";
                case "CANCELLED": return "❌ Отменено";
                case "COMPLETED": return "✔️ Выполнено";
                default: return status;
            }
        }
    }
}