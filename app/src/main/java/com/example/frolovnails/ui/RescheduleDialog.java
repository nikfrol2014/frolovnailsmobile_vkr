package com.example.frolovnails.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frolovnails.R;
import com.example.frolovnails.admin.CalendarViewModel;
import com.example.frolovnails.client.ServicesViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.Service;
import com.example.frolovnails.utils.ToastUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RescheduleDialog extends DialogFragment {

    private static final String ARG_APPOINTMENT = "appointment";

    private Appointment appointment;
    private CalendarViewModel viewModel;
    private OnRescheduleListener listener;

    private EditText etDateTime;
    private Spinner spinnerServices;
    private Button btnSave, btnCancel;
    private View progressBar;

    private List<Service> servicesList = new ArrayList<>();
    private Long selectedServiceId;

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnRescheduleListener {
        void onRescheduleSuccess();
    }

    public static RescheduleDialog newInstance(Appointment appointment) {
        RescheduleDialog fragment = new RescheduleDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APPOINTMENT, appointment);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnRescheduleListener(OnRescheduleListener listener) {
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
        dialog.setTitle("Перенос записи");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_reschedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDateTime = view.findViewById(R.id.etDateTime);
        spinnerServices = view.findViewById(R.id.spinnerServices);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        etDateTime.setInputType(InputType.TYPE_NULL);
        etDateTime.setOnClickListener(v -> showDateTimePicker());

        if (appointment != null) {
            etDateTime.setText(appointment.getStartTime());
            selectedServiceId = appointment.getService().getId();
        }

        loadServices();

        btnSave.setOnClickListener(v -> moveAppointment());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void loadServices() {
        TokenManager tokenManager = null;
        try {
            tokenManager = new TokenManager(requireContext());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        final TokenManager finalTokenManager = tokenManager;

        ServicesViewModel servicesViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ServicesViewModel(finalTokenManager);
            }
        }).get(ServicesViewModel.class);

        servicesViewModel.loadServices();
        servicesViewModel.getServicesResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Success) {
                servicesList = ((Resource.Success<List<Service>>) resource).getData();
                if (servicesList != null) {
                    setupSpinner();
                }
            }
        });
    }

    private void setupSpinner() {
        List<String> serviceNames = new ArrayList<>();
        int selectedPosition = 0;

        for (int i = 0; i < servicesList.size(); i++) {
            Service service = servicesList.get(i);
            String displayName = service.getName() + " (" + service.getDurationMinutes() + " мин, " + service.getPrice() + " ₽)";
            serviceNames.add(displayName);

            if (service.getId().equals(selectedServiceId)) {
                selectedPosition = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, serviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServices.setAdapter(adapter);
        spinnerServices.setSelection(selectedPosition);

        spinnerServices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < servicesList.size()) {
                    selectedServiceId = servicesList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedServiceId = appointment.getService().getId();
            }
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        try {
            if (!etDateTime.getText().toString().isEmpty()) {
                calendar.setTime(dateTimeFormat.parse(etDateTime.getText().toString()));
            } else {
                calendar.setTime(dateTimeFormat.parse(appointment.getStartTime()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (dateView, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                String formattedDateTime = String.format(Locale.getDefault(),
                                        "%02d.%02d.%04d %02d:%02d",
                                        dayOfMonth, month + 1, year, hourOfDay, minute);
                                etDateTime.setText(formattedDateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void moveAppointment() {
        String newDateTime = etDateTime.getText().toString().trim();

        if (newDateTime.isEmpty()) {
            ToastUtils.show(getContext(), "Выберите новую дату и время", Toast.LENGTH_SHORT);
            return;
        }

        boolean timeChanged = !newDateTime.equals(appointment.getStartTime());
        boolean serviceChanged = !selectedServiceId.equals(appointment.getService().getId());

        if (!timeChanged && !serviceChanged) {
            ToastUtils.show(getContext(), "Ничего не изменено", Toast.LENGTH_SHORT);
            dismiss();
            return;
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

        viewModel.moveAppointment(appointment.getId(), newDateTime,
                serviceChanged ? selectedServiceId : null);

        viewModel.getMoveAppointmentResult().observe(getViewLifecycleOwner(), this::handleResult);
    }

    private void handleResult(Resource<Appointment> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "✅ Запись перенесена", Toast.LENGTH_SHORT);
            if (listener != null) {
                listener.onRescheduleSuccess();
            }
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<Appointment>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }
}