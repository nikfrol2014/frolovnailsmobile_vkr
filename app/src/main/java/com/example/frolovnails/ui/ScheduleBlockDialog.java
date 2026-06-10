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
import com.example.frolovnails.network.models.request.CreateScheduleBlockRequest;
import com.example.frolovnails.network.models.response.ScheduleBlock;
import com.example.frolovnails.utils.ToastUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleBlockDialog extends DialogFragment {

    private EditText etStartTime, etEndTime, etReason, etNotes;
    private Button btnSave, btnCancel;
    private View progressBar;
    private ScheduleViewModel viewModel;

    public static ScheduleBlockDialog newInstance() {
        return new ScheduleBlockDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Добавление блокировки");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_schedule_block, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        etReason = view.findViewById(R.id.etReason);
        etNotes = view.findViewById(R.id.etNotes);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        etStartTime.setInputType(InputType.TYPE_NULL);
        etEndTime.setInputType(InputType.TYPE_NULL);

        etStartTime.setOnClickListener(v -> showDateTimePicker(true));
        etEndTime.setOnClickListener(v -> showDateTimePicker(false));

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

        btnSave.setOnClickListener(v -> saveBlock());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void showDateTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (dateView, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                String formattedDateTime = String.format(Locale.getDefault(),
                                        "%02d.%02d.%04d %02d:%02d", dayOfMonth, month + 1, year, hourOfDay, minute);
                                if (isStartTime) {
                                    etStartTime.setText(formattedDateTime);
                                } else {
                                    etEndTime.setText(formattedDateTime);
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    timePicker.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void saveBlock() {
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String reason = etReason.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (startTime.isEmpty() || endTime.isEmpty()) {
            ToastUtils.show(getContext(), "Заполните время начала и окончания", Toast.LENGTH_SHORT);
            return;
        }

        CreateScheduleBlockRequest request = new CreateScheduleBlockRequest();
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setReason(reason);
        request.setNotes(notes);

        viewModel.addScheduleBlock(request);
        viewModel.getAddScheduleBlockResult().observe(getViewLifecycleOwner(), this::handleResult);
    }

    private void handleResult(Resource<ScheduleBlock> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "Блокировка добавлена", Toast.LENGTH_SHORT);
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<ScheduleBlock>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }
}