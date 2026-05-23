package com.example.frolovnails.ui;

import android.app.Dialog;
import android.os.Bundle;
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

import java.io.IOException;
import java.security.GeneralSecurityException;

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

    private void saveBlock() {
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        String reason = etReason.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(getContext(), "Заполните время начала и окончания", Toast.LENGTH_SHORT).show();
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
        if (resource instanceof Resource.Success) {
            Toast.makeText(getContext(), "Блокировка добавлена", Toast.LENGTH_SHORT).show();
            dismiss();
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), ((Resource.Error<ScheduleBlock>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}