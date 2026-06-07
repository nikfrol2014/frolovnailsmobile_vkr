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
import androidx.lifecycle.ViewModelProvider;

import com.example.frolovnails.R;
import com.example.frolovnails.admin.ClientViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.ClientDetailsResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;

public class ClientDetailsDialog extends DialogFragment {

    private static final String ARG_CLIENT_ID = "client_id";

    private Long clientId;
    private ClientViewModel viewModel;
    private TextView tvName, tvPhone, tvBirthDate, tvNotes, tvRegisteredAt;
    private TextView tvTotalVisits, tvTotalSpent, tvAverageBill, tvFavoriteService, tvAttendanceRate;
    private TextView tvRecentAppointments, tvUpcomingAppointments;
    private Button btnClose;
    private View progressBar;

    public static ClientDetailsDialog newInstance(Long clientId) {
        ClientDetailsDialog fragment = new ClientDetailsDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_CLIENT_ID, clientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            clientId = getArguments().getLong(ARG_CLIENT_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Детали клиента");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_client_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Основная информация
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvNotes = view.findViewById(R.id.tvNotes);
        tvRegisteredAt = view.findViewById(R.id.tvRegisteredAt);

        // Статистика
        tvTotalVisits = view.findViewById(R.id.tvTotalVisits);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvAverageBill = view.findViewById(R.id.tvAverageBill);
        tvFavoriteService = view.findViewById(R.id.tvFavoriteService);
        tvAttendanceRate = view.findViewById(R.id.tvAttendanceRate);

        // Списки записей (заглушки)
        tvRecentAppointments = view.findViewById(R.id.tvRecentAppointments);
        tvUpcomingAppointments = view.findViewById(R.id.tvUpcomingAppointments);

        btnClose = view.findViewById(R.id.btnClose);
        progressBar = view.findViewById(R.id.progressBar);

        btnClose.setOnClickListener(v -> dismiss());

        // Инициализация ViewModel
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
                return (T) new ClientViewModel(finalTokenManager);
            }
        }).get(ClientViewModel.class);

        loadClientDetails();
    }

    private void loadClientDetails() {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.loadClientDetails(clientId);
        viewModel.getClientDetailsResult().observe(getViewLifecycleOwner(), this::handleClientDetails);
    }

    private void handleClientDetails(Resource<ClientDetailsResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ClientDetailsResponse data = ((Resource.Success<ClientDetailsResponse>) resource).getData();
            if (data != null) {
                displayClientDetails(data);
            } else {
                dismiss();
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            dismiss();
        }
    }

    private void displayClientDetails(ClientDetailsResponse data) {
        ClientDetailsResponse.ClientInfo client = data.getClient();
        ClientDetailsResponse.ClientStats stats = data.getStats();

        // Основная информация
        String fullName = (client.getFirstName() != null ? client.getFirstName() : "") + " " +
                (client.getLastName() != null ? client.getLastName() : "");
        tvName.setText(fullName.trim().isEmpty() ? "Без имени" : fullName.trim());
        tvPhone.setText(client.getPhone() != null ? client.getPhone() : "Не указан");
        tvBirthDate.setText(client.getBirthDate() != null ? client.getBirthDate() : "Не указана");
        tvNotes.setText(client.getNotes() != null ? client.getNotes() : "Нет заметок");
        tvRegisteredAt.setText(client.getRegisteredAt() != null ? client.getRegisteredAt() : "Не указана");

        // Статистика
        tvTotalVisits.setText(String.valueOf(stats.getTotalVisits() != null ? stats.getTotalVisits() : 0));
        tvTotalSpent.setText((stats.getTotalSpent() != null ? stats.getTotalSpent() : "0") + " ₽");
        tvAverageBill.setText((stats.getAverageBill() != null ? stats.getAverageBill() : "0") + " ₽");
        tvFavoriteService.setText(stats.getFavoriteService() != null ? stats.getFavoriteService() : "Нет данных");
        tvAttendanceRate.setText(String.valueOf(stats.getAttendanceRate() != null ? stats.getAttendanceRate() : 0) + "%");

        // Списки записей
        if (data.getRecentAppointments() != null && !data.getRecentAppointments().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(5, data.getRecentAppointments().size()); i++) {
                Appointment apt = data.getRecentAppointments().get(i);

                BigDecimal displayPrice = apt.getActualPrice() != null ? apt.getActualPrice() : apt.getService().getPrice();
                String priceInfo = "💰 " + displayPrice + " ₽";
                if (apt.getActualPrice() != null) {
                    priceInfo += " (было " + apt.getService().getPrice() + " ₽)";
                }

                String servicesInfo = "";
                if (apt.getActualServices() != null && !apt.getActualServices().isEmpty()) {
                    servicesInfo = "\n   📋 Фактически: " + apt.getActualServices();
                }

                String commentInfo = "";
                if (apt.getMasterCompletionComment() != null && !apt.getMasterCompletionComment().isEmpty()) {
                    commentInfo = "\n   💬 Комментарий: " + apt.getMasterCompletionComment();
                }

                // ДОБАВИТЬ ЗАМЕТКИ КЛИЕНТА
                String clientNotesInfo = "";
                if (apt.getClientNotes() != null && !apt.getClientNotes().isEmpty()) {
                    clientNotesInfo = "\n   ✏️ Пожелания: " + apt.getClientNotes();
                }

                sb.append("• ").append(apt.getStartTime()).append("\n")
                        .append("  ").append(apt.getService().getName()).append("\n")
                        .append("  ").append(priceInfo)
                        .append(servicesInfo)
                        .append(commentInfo)
                        .append(clientNotesInfo)
                        .append("\n\n");
            }
            tvRecentAppointments.setText(sb.toString());
        }

        if (data.getUpcomingAppointments() != null && !data.getUpcomingAppointments().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(3, data.getUpcomingAppointments().size()); i++) {
                Appointment apt = data.getUpcomingAppointments().get(i);
                sb.append("• ").append(apt.getStartTime()).append(" — ")
                        .append(apt.getService().getName()).append("\n");
            }
            tvUpcomingAppointments.setText(sb.toString());
        } else {
            tvUpcomingAppointments.setText("Нет предстоящих записей");
        }
    }
}