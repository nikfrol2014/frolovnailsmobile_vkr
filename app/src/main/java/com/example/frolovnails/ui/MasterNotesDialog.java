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
import com.example.frolovnails.admin.CalendarViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.utils.ToastUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MasterNotesDialog extends DialogFragment {

    private static final String ARG_APPOINTMENT_ID = "appointment_id";
    private static final String ARG_APPOINTMENT_STATUS = "appointment_status";
    private static final String ARG_EXISTING_NOTES = "existing_notes";

    private EditText etNotes;
    private Button btnSave, btnCancel;
    private View progressBar;
    private CalendarViewModel viewModel;
    private Long appointmentId;
    private String currentStatus;
    private String existingNotes;

    public static MasterNotesDialog newInstance(Appointment appointment) {
        MasterNotesDialog fragment = new MasterNotesDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_APPOINTMENT_ID, appointment.getId());
        args.putString(ARG_APPOINTMENT_STATUS, appointment.getStatus().toString());
        args.putString(ARG_EXISTING_NOTES, appointment.getMasterNotes());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            appointmentId = getArguments().getLong(ARG_APPOINTMENT_ID);
            currentStatus = getArguments().getString(ARG_APPOINTMENT_STATUS);
            existingNotes = getArguments().getString(ARG_EXISTING_NOTES);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Заметки мастера");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_master_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNotes = view.findViewById(R.id.etNotes);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        progressBar = view.findViewById(R.id.progressBar);

        if (existingNotes != null && !existingNotes.isEmpty()) {
            etNotes.setText(existingNotes);
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

        btnSave.setOnClickListener(v -> saveNotes());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveNotes() {
        String notes = etNotes.getText().toString().trim();

        // Проверяем, изменились ли заметки
        String existing = existingNotes != null ? existingNotes : "";
        if (notes.equals(existing)) {
            ToastUtils.show(getContext(), "Заметки не изменены", Toast.LENGTH_SHORT);
            dismiss();
            return;
        }

        // Подписываемся на результат ДО вызова API
        viewModel.getUpdateNotesResult().observe(getViewLifecycleOwner(), this::handleResult);

        // Вызываем API
        viewModel.updateMasterNotes(appointmentId, notes);
    }

    private void handleResult(Resource<Appointment> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "Заметки сохранены", Toast.LENGTH_SHORT);
            dismiss();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            ToastUtils.show(getContext(), ((Resource.Error<Appointment>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }
}