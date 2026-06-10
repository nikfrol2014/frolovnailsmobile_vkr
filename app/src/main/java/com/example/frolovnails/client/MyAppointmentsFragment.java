package com.example.frolovnails.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.frolovnails.R;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.request.UpdateAppointmentStatusRequest;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.utils.ToastUtils;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyAppointmentsFragment extends Fragment {

    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private MyAppointmentsAdapter adapter;
    private MyAppointmentsViewModel viewModel;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private int currentTab = 0; // 0 - предстоящие, 1 - прошедшие, 2 - все

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupTabLayout();
        setupSwipeRefresh();
        setupAdapter();
        loadAppointments();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvAppointments = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyAppointmentsAdapter();
        rvAppointments.setAdapter(adapter);
    }

    private void initViewModel() {
        try {
            TokenManager tokenManager = new TokenManager(requireContext());
            viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @NonNull
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) new MyAppointmentsViewModel(tokenManager);
                }
            }).get(MyAppointmentsViewModel.class);

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        viewModel.getAppointmentsResult().observe(getViewLifecycleOwner(), this::handleAppointmentsResult);
        viewModel.getUpdateStatusResult().observe(getViewLifecycleOwner(), this::handleUpdateStatusResult);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Предстоящие"));
        tabLayout.addTab(tabLayout.newTab().setText("Прошедшие"));
        tabLayout.addTab(tabLayout.newTab().setText("Все"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                filterAppointments();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadAppointments();
        });
        swipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.primary, null),
                getResources().getColor(R.color.secondary, null)
        );
    }

    private void setupAdapter() {
        // Подтверждение записи
        adapter.setOnConfirmClickListener(appointment -> {
            showConfirmDialog(appointment);
        });

        // Отмена записи
        adapter.setOnCancelClickListener(appointment -> {
            showCancelDialog(appointment);
        });

        // Детали записи
        adapter.setOnItemClickListener(appointment -> {
            showAppointmentDetailsDialog(appointment);
        });
    }

    private void loadAppointments() {
        viewModel.loadMyAppointments();
    }

    private void handleAppointmentsResult(Resource<List<Appointment>> resource) {
        swipeRefresh.setRefreshing(false);

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            List<Appointment> appointments = ((Resource.Success<List<Appointment>>) resource).getData();
            if (appointments != null) {
                viewModel.setAppointments(appointments);
                filterAppointments();
            } else {
                showEmptyState();
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            showEmptyState();
            String error = ((Resource.Error<List<Appointment>>) resource).getMessage();
            ToastUtils.show(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT);
        }
    }

    private void filterAppointments() {
        List<Appointment> allAppointments = viewModel.getAppointmentsList();
        if (allAppointments == null || allAppointments.isEmpty()) {
            showEmptyState();
            return;
        }

        List<Appointment> filtered = new ArrayList<>();
        Calendar now = Calendar.getInstance();

        for (Appointment apt : allAppointments) {
            boolean isUpcoming = isAppointmentUpcoming(apt, now);
            Appointment.AppointmentStatus status = apt.getStatus();

            switch (currentTab) {
                case 0: // Предстоящие
                    if (isUpcoming && status != Appointment.AppointmentStatus.CANCELLED) {
                        filtered.add(apt);
                    }
                    break;
                case 1: // Прошедшие
                    if (!isUpcoming || status == Appointment.AppointmentStatus.CANCELLED) {
                        filtered.add(apt);
                    }
                    break;
                case 2: // Все
                    filtered.add(apt);
                    break;
            }
        }

        if (filtered.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            adapter.setAppointments(filtered);
        }
    }

    private boolean isAppointmentUpcoming(Appointment apt, Calendar now) {
        try {
            Calendar aptTime = Calendar.getInstance();
            aptTime.setTime(dateFormat.parse(apt.getStartTime()));
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

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        rvAppointments.setVisibility(View.GONE);

        switch (currentTab) {
            case 0:
                tvEmpty.setText("Нет предстоящих записей");
                break;
            case 1:
                tvEmpty.setText("Нет прошедших записей");
                break;
            case 2:
                tvEmpty.setText("У вас пока нет записей");
                break;
        }
    }

    private void hideEmptyState() {
        tvEmpty.setVisibility(View.GONE);
        rvAppointments.setVisibility(View.VISIBLE);
    }

    // ========== ДИАЛОГИ ДЕЙСТВИЙ ==========

    private void showConfirmDialog(Appointment appointment) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Подтверждение записи")
                .setMessage("Вы подтверждаете запись?\n\n" +
                        "💅 Услуга: " + appointment.getService().getName() + "\n" +
                        "⏰ Время: " + appointment.getStartTime() + "\n" +
                        "💰 Цена: " + appointment.getService().getPrice() + " ₽")
                .setPositiveButton("Подтвердить", (dialog, which) -> {
                    UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
                    request.setStatus("CONFIRMED");
                    viewModel.updateAppointmentStatus(appointment.getId(), request);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showCancelDialog(Appointment appointment) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Отмена записи")
                .setMessage("Вы уверены, что хотите отменить запись?\n\n" +
                        "💅 Услуга: " + appointment.getService().getName() + "\n" +
                        "⏰ Время: " + appointment.getStartTime())
                .setPositiveButton("Отменить", (dialog, which) -> {
                    UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
                    request.setStatus("CANCELLED");
                    viewModel.updateAppointmentStatus(appointment.getId(), request);
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void handleUpdateStatusResult(Resource<Appointment> resource) {
        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.show(getContext(), "✅ Статус записи обновлен", Toast.LENGTH_SHORT);
            loadAppointments();
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            String error = ((Resource.Error<Appointment>) resource).getMessage();
            ToastUtils.show(getContext(), "❌ Ошибка: " + error, Toast.LENGTH_SHORT);
        }
    }

    private void showAppointmentDetailsDialog(Appointment appointment) {
        java.math.BigDecimal actualPrice = appointment.getActualPrice();
        java.math.BigDecimal displayPrice = actualPrice != null ? actualPrice : appointment.getService().getPrice();
        String priceText = displayPrice + " ₽";
        if (actualPrice != null) {
            priceText += " (было " + appointment.getService().getPrice() + " ₽)";
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Детали записи")
                .setMessage(
                        "💅 Услуга: " + appointment.getService().getName() + "\n" +
                                "📅 Дата и время: " + appointment.getStartTime() + "\n" +
                                "💰 Цена: " + priceText + "\n" +
                                "📊 Статус: " + getStatusText(appointment.getStatus()) + "\n" +
                                (appointment.getMasterNotes() != null ? "📝 Заметки мастера: " + appointment.getMasterNotes() : "") + "\n" +
                                (appointment.getClientNotes() != null ? "✏️ Ваши пожелания: " + appointment.getClientNotes() : "")
                )
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private String getStatusText(Appointment.AppointmentStatus status) {
        switch (status) {
            case CONFIRMED: return "✅ Подтверждено";
            case PENDING: return "⏳ Ожидает подтверждения";
            case CREATED: return "🆕 Создано";
            case CANCELLED: return "❌ Отменено";
            case COMPLETED: return "✔️ Выполнено";
            default: return status.toString();
        }
    }
}