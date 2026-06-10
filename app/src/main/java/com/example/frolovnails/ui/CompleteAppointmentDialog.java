package com.example.frolovnails.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frolovnails.R;
import com.example.frolovnails.admin.CalendarViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.request.UpdateAppointmentStatusRequest;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.utils.ToastUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CompleteAppointmentDialog extends DialogFragment {

    private static final String ARG_APPOINTMENT = "appointment";

    private TextView tvClientName, tvService, tvTime, tvPrice;
    private EditText etActualPrice, etActualServices, etMasterComment;
    private Button btnComplete, btnCancel;
    private ProgressBar progressBar;
    private CalendarViewModel viewModel;
    private Appointment appointment;

    public static CompleteAppointmentDialog newInstance(Appointment appointment) {
        CompleteAppointmentDialog fragment = new CompleteAppointmentDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APPOINTMENT, appointment);
        fragment.setArguments(args);
        return fragment;
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
        dialog.setTitle("Завершение записи");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_complete_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvClientName = view.findViewById(R.id.tvClientName);
        tvService = view.findViewById(R.id.tvService);
        tvTime = view.findViewById(R.id.tvTime);
        tvPrice = view.findViewById(R.id.tvPrice);
        etActualPrice = view.findViewById(R.id.etActualPrice);
        etActualServices = view.findViewById(R.id.etActualServices);
        etMasterComment = view.findViewById(R.id.etMasterComment);
        btnComplete = view.findViewById(R.id.btnComplete);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        if (appointment != null) {
            tvClientName.setText(appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName());
            tvService.setText(appointment.getService().getName() + " (" + appointment.getService().getCategory() + ")");
            tvTime.setText(appointment.getStartTime() + " — " + appointment.getEndTime());
            tvPrice.setText("Цена по прайсу: " + appointment.getService().getPrice() + " ₽");

            // Предзаполняем фактическую цену
            etActualPrice.setText(appointment.getService().getPrice().toString());
        }

        TokenManager tokenManager = null;
        try {
            tokenManager = new TokenManager(requireContext());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        final TokenManager finalTokenManager = tokenManager;

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new CalendarViewModel(finalTokenManager);
            }
        }).get(CalendarViewModel.class);

        btnComplete.setOnClickListener(v -> completeAppointment());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void completeAppointment() {
        String actualPriceStr = etActualPrice.getText().toString().trim();
        String actualServices = etActualServices.getText().toString().trim();
        String masterComment = etMasterComment.getText().toString().trim();

        BigDecimal actualPrice;
        try {
            actualPrice = new BigDecimal(actualPriceStr);
        } catch (NumberFormatException e) {
            ToastUtils.show(getContext(), "Введите корректную цену", Toast.LENGTH_SHORT);
            return;
        }

        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
        request.setStatus("COMPLETED");
        request.setMasterNotes(appointment.getMasterNotes());
        request.setActualPrice(actualPrice);
        request.setActualServices(actualServices.isEmpty() ? null : actualServices);
        request.setMasterCompletionComment(masterComment.isEmpty() ? null : masterComment);

        viewModel.updateAppointmentStatus(appointment.getId(), request);
        viewModel.getUpdateStatusResult().observe(getViewLifecycleOwner(), this::handleResult);
    }

    private void handleResult(Resource<Appointment> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnComplete.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "Запись завершена", Toast.LENGTH_SHORT);
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnComplete.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<Appointment>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }
}