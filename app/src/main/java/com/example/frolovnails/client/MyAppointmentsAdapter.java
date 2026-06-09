package com.example.frolovnails.client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private OnConfirmClickListener confirmClickListener;
    private OnCancelClickListener cancelClickListener;
    private OnItemClickListener itemClickListener;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnConfirmClickListener {
        void onConfirmClick(Appointment appointment);
    }

    public interface OnCancelClickListener {
        void onCancelClick(Appointment appointment);
    }

    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.confirmClickListener = listener;
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
        holder.bind(appointment, confirmClickListener, cancelClickListener, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvDateTime, tvClientNotes, tvStatus, tvPrice;
        LinearLayout llActions;
        Button btnConfirm, btnCancel, btnCancelOnly;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvClientNotes = itemView.findViewById(R.id.tvClientNotes);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            llActions = itemView.findViewById(R.id.llActions);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnCancelOnly = itemView.findViewById(R.id.btnCancelOnly);
        }

        void bind(Appointment appointment,
                  OnConfirmClickListener confirmListener,
                  OnCancelClickListener cancelListener,
                  OnItemClickListener itemListener) {

            tvServiceName.setText(appointment.getService().getName());
            tvDateTime.setText(appointment.getStartTime() + " — " + appointment.getEndTime());

            // Цена (приоритет actualPrice)
            java.math.BigDecimal actualPrice = appointment.getActualPrice();
            java.math.BigDecimal displayPrice = actualPrice != null ? actualPrice : appointment.getService().getPrice();
            tvPrice.setText(displayPrice + " ₽");

            if (actualPrice != null) {
                tvPrice.setTextColor(0xFFFF9800);
            } else {
                tvPrice.setTextColor(0xFF2196F3);
            }

            // Статус и цвет
            String status = appointment.getStatus().toString();
            tvStatus.setText(getStatusText(status));
            setStatusColor(status, tvStatus);

            // Заметки клиента
            if (appointment.getClientNotes() != null && !appointment.getClientNotes().isEmpty()) {
                tvClientNotes.setVisibility(View.VISIBLE);
                tvClientNotes.setText("✏️ " + appointment.getClientNotes());
            } else {
                tvClientNotes.setVisibility(View.GONE);
            }

            // Проверяем, предстоящая ли запись
            boolean isUpcoming = isAppointmentUpcoming(appointment);

            // Для записей в статусе CREATED или PENDING - показываем кнопки подтверждения/отмены
            if (isUpcoming && (status.equals("CREATED") || status.equals("PENDING"))) {
                llActions.setVisibility(View.VISIBLE);
                btnCancelOnly.setVisibility(View.GONE);

                btnConfirm.setOnClickListener(v -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick(appointment);
                    }
                });

                btnCancel.setOnClickListener(v -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick(appointment);
                    }
                });
            }
            // Для подтвержденных предстоящих записей - только кнопка отмены
            else if (isUpcoming && status.equals("CONFIRMED")) {
                llActions.setVisibility(View.GONE);
                btnCancelOnly.setVisibility(View.VISIBLE);
                btnCancelOnly.setOnClickListener(v -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick(appointment);
                    }
                });
            }
            // Для остальных - никаких кнопок
            else {
                llActions.setVisibility(View.GONE);
                btnCancelOnly.setVisibility(View.GONE);
            }

            // Клик по карточке для просмотра деталей
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
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
                aptTime.set(Calendar.SECOND, 0);
                aptTime.set(Calendar.MILLISECOND, 0);
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

        private void setStatusColor(String status, TextView textView) {
            switch (status) {
                case "CONFIRMED":
                    textView.setTextColor(0xFF4CAF50);
                    break;
                case "PENDING":
                case "CREATED":
                    textView.setTextColor(0xFFFF9800);
                    break;
                case "CANCELLED":
                    textView.setTextColor(0xFFF44336);
                    break;
                case "COMPLETED":
                    textView.setTextColor(0xFF2196F3);
                    break;
                default:
                    textView.setTextColor(0xFF9E9E9E);
            }
        }
    }
}