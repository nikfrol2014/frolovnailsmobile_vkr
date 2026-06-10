package com.example.frolovnails.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frolovnails.R;
import com.example.frolovnails.admin.ScheduleViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.AvailableDay;
import com.example.frolovnails.utils.ToastUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Locale;

public class AvailableDayDialog extends DialogFragment {

    private static final String ARG_DAY = "day";

    private EditText etDate, etWorkStart, etWorkEnd, etNotes;
    private Button btnSave, btnCancel;
    private View progressBar;
    private ScheduleViewModel viewModel;
    private AvailableDay editingDay;

    public static AvailableDayDialog newInstance() {
        return new AvailableDayDialog();
    }

    public static AvailableDayDialog newInstance(AvailableDay day) {
        AvailableDayDialog fragment = new AvailableDayDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_DAY)) {
            editingDay = (AvailableDay) getArguments().getSerializable(ARG_DAY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (editingDay != null) {
            dialog.setTitle("Редактирование рабочего дня");
        } else {
            dialog.setTitle("Добавление рабочего дня");
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_available_day, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDate = view.findViewById(R.id.etDate);
        etWorkStart = view.findViewById(R.id.etWorkStart);
        etWorkEnd = view.findViewById(R.id.etWorkEnd);
        etNotes = view.findViewById(R.id.etNotes);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        etDate.setInputType(InputType.TYPE_NULL);
        etWorkStart.setInputType(InputType.TYPE_NULL);
        etWorkEnd.setInputType(InputType.TYPE_NULL);

        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showDatePicker();
        });

        etWorkStart.setOnClickListener(v -> showTimePicker(true));
        etWorkStart.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showTimePicker(true);
        });

        etWorkEnd.setOnClickListener(v -> showTimePicker(false));
        etWorkEnd.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showTimePicker(false);
        });

        if (editingDay != null) {
            etDate.setText(editingDay.getAvailableDate());
            etWorkStart.setText(editingDay.getWorkStart());
            etWorkEnd.setText(editingDay.getWorkEnd());
            etNotes.setText(editingDay.getNotes());
        } else {
            etWorkStart.setText("10:00");
            etWorkEnd.setText("19:00");
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
                return (T) new ScheduleViewModel(finalTokenManager);
            }
        }).get(ScheduleViewModel.class);

        // Подписываемся на результаты
        viewModel.getAddAvailableDayResult().observe(getViewLifecycleOwner(), this::handleAddResult);
        viewModel.getUpdateAvailableDayResult().observe(getViewLifecycleOwner(), this::handleUpdateResult);

        btnSave.setOnClickListener(v -> saveDay());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        if (editingDay != null && editingDay.getAvailableDate() != null) {
            try {
                String[] parts = editingDay.getAvailableDate().split("\\.");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1;
                    int year = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", dayOfMonth, month + 1, year);
                    etDate.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        String currentTime = isStartTime ? etWorkStart.getText().toString() : etWorkEnd.getText().toString();

        if (currentTime != null && !currentTime.isEmpty()) {
            try {
                String[] parts = currentTime.split(":");
                if (parts.length == 2) {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (isStartTime) {
                        etWorkStart.setText(formattedTime);
                    } else {
                        etWorkEnd.setText(formattedTime);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void saveDay() {
        String date = etDate.getText().toString().trim();
        String workStart = etWorkStart.getText().toString().trim();
        String workEnd = etWorkEnd.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (date.isEmpty() || workStart.isEmpty() || workEnd.isEmpty()) {
            ToastUtils.show(getContext(), "Заполните все поля", Toast.LENGTH_SHORT);
            return;
        }

        if (editingDay != null) {
            viewModel.updateAvailableDay(
                    editingDay.getId(),
                    workStart,
                    workEnd,
                    true,
                    notes
            );
        } else {
            viewModel.addAvailableDay(date, workStart, workEnd, notes);
        }
    }

    private void handleAddResult(Resource<AvailableDay> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "День добавлен", Toast.LENGTH_SHORT);
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<AvailableDay>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }

    private void handleUpdateResult(Resource<AvailableDay> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "День обновлён", Toast.LENGTH_SHORT);
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<AvailableDay>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }
}