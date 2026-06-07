package com.example.frolovnails.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.Appointment;

public class AppointmentActionDialog extends DialogFragment {

    private static final String ARG_APPOINTMENT = "appointment";

    private Appointment appointment;
    private OnActionListener listener;

    public interface OnActionListener {
        void onReschedule(Appointment appointment);      // Перенос
        void onCancel(Appointment appointment);         // Отмена (статус CANCELLED)
        void onDelete(Appointment appointment);         // Удаление (полное)
        void onChangeStatus(Appointment appointment);   // Смена статуса
        void onMasterNotes(Appointment appointment);    // Заметки
        void onClientDetails(Appointment appointment);  // Детали клиента
    }

    public static AppointmentActionDialog newInstance(Appointment appointment) {
        AppointmentActionDialog fragment = new AppointmentActionDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APPOINTMENT, appointment);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appointment = (Appointment) getArguments().getSerializable(ARG_APPOINTMENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Действия с записью");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_appointment_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvInfo = view.findViewById(R.id.tvAppointmentInfo);
        Button btnReschedule = view.findViewById(R.id.btnReschedule);
        Button btnCancel = view.findViewById(R.id.btnCancelAppointment);
        Button btnStatus = view.findViewById(R.id.btnChangeStatus);
        Button btnNotes = view.findViewById(R.id.btnMasterNotes);
        Button btnClient = view.findViewById(R.id.btnClientDetails);
        Button btnClose = view.findViewById(R.id.btnClose);

        if (appointment != null) {
            String info = "👤 " + appointment.getClient().getFirstName() + " " +
                    (appointment.getClient().getLastName() != null ? appointment.getClient().getLastName() : "") + "\n" +
                    "💅 " + appointment.getService().getName() + "\n" +
                    "⏰ " + appointment.getStartTime() + " — " + appointment.getEndTime() + "\n" +
                    "📊 Статус: " + getStatusText(appointment.getStatus());
            tvInfo.setText(info);
        }

        TextView tvClientNotes = view.findViewById(R.id.tvClientNotes);
        View cardClientNotes = view.findViewById(R.id.cardClientNotes);

        if (appointment.getClientNotes() != null && !appointment.getClientNotes().isEmpty()) {
            cardClientNotes.setVisibility(View.VISIBLE);
            tvClientNotes.setText(appointment.getClientNotes());
        } else {
            cardClientNotes.setVisibility(View.GONE);
        }

        btnReschedule.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReschedule(appointment);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel(appointment);
            }
            dismiss();
        });

        btnStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeStatus(appointment);
            }
            dismiss();
        });

        btnNotes.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMasterNotes(appointment);
            }
            dismiss();
        });

        btnClient.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClientDetails(appointment);
            }
            dismiss();
        });

        Button btnDelete = view.findViewById(R.id.btnDeleteAppointment);
        btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(appointment);
            }
            dismiss();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    private String getStatusText(Appointment.AppointmentStatus status) {
        switch (status) {
            case CONFIRMED: return "✅ Подтверждено";
            case PENDING: return "⏳ Ожидание";
            case CREATED: return "🆕 Создано";
            case CANCELLED: return "❌ Отменено";
            case COMPLETED: return "✔️ Выполнено";
            default: return status.toString();
        }
    }
}