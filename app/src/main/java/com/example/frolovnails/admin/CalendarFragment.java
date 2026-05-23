package com.example.frolovnails.admin;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.AppointmentAdapter;
import com.example.frolovnails.adapters.DateAdapter;
import com.example.frolovnails.common.RefreshableFragment;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.TimelineResponse;
import com.example.frolovnails.ui.MasterNotesDialog;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends RefreshableFragment {

    private RecyclerView rvDates, rvAppointments;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvStats;
    private CalendarViewModel viewModel;
    private DateAdapter dateAdapter;
    private AppointmentAdapter appointmentAdapter;
    private List<String> datesList = new ArrayList<>();
    private Map<String, List<Appointment>> appointmentsByDay = new HashMap<>();

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_calendar;
    }

    @Override
    protected int getSwipeRefreshId() {
        return R.id.swipeRefreshCalendar;
    }

    @Override
    protected void initViews(View view) {
        rvDates = view.findViewById(R.id.rvDates);
        rvAppointments = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvStats = view.findViewById(R.id.tvStats);

        rvDates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dateAdapter = new DateAdapter();
        rvDates.setAdapter(dateAdapter);

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentAdapter = new AppointmentAdapter();
        rvAppointments.setAdapter(appointmentAdapter);

        // Обработчик кликов по записям
        appointmentAdapter.setOnAppointmentClickListener(new AppointmentAdapter.OnAppointmentClickListener() {
            @Override
            public void onAppointmentClick(Appointment appointment) {
                // Пока пусто, можно добавить открытие деталей клиента
            }

            @Override
            public void onMasterNotesClick(Appointment appointment) {
                MasterNotesDialog dialog = MasterNotesDialog.newInstance(appointment);
                dialog.show(getChildFragmentManager(), "master_notes");
//                dialog.getDialog().setOnDismissListener(d -> loadData());
            }
        });

        dateAdapter.setOnDateClickListener((date, position) -> {
            dateAdapter.setSelectedPosition(position);
            showAppointmentsForDate(date);
        });

        TokenManager tokenManager = null;
        try {
            tokenManager = new TokenManager(requireContext());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        final TokenManager finalTokenManager = tokenManager;

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new CalendarViewModel(finalTokenManager);
            }
        }).get(CalendarViewModel.class);
    }

    @Override
    protected void loadData() {
        viewModel.loadTimeline(7);
        viewModel.getTimelineResult().observe(getViewLifecycleOwner(), this::handleTimelineResult);
    }

    @Override
    protected void onRefresh() {
        loadData();
    }

    private void handleTimelineResult(Resource<TimelineResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            rvDates.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            TimelineResponse response = ((Resource.Success<TimelineResponse>) resource).getData();

            if (response == null) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Нет данных");
                return;
            }

            appointmentsByDay = response.getAppointmentsByDay();

            if (appointmentsByDay == null || appointmentsByDay.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Нет записей");
                rvDates.setVisibility(View.GONE);
                rvAppointments.setVisibility(View.GONE);
                return;
            }

            datesList = new ArrayList<>(appointmentsByDay.keySet());
            datesList.sort(String::compareTo);
            dateAdapter.setDates(datesList);
            rvDates.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.VISIBLE);

            TimelineResponse.TimelineStats stats = response.getStats();
            if (stats != null) {
                String statsText = "✅ " + stats.getConfirmedCount() +
                        " | ⏳ " + stats.getPendingCount() +
                        " | ❌ " + stats.getCancelledCount() +
                        " | ✔️ " + stats.getCompletedCount();
                tvStats.setText(statsText);
            }

            if (!datesList.isEmpty()) {
                dateAdapter.setSelectedPosition(0);
                showAppointmentsForDate(datesList.get(0));
            }

            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            rvDates.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.GONE);
            tvEmpty.setText("Ошибка: " + ((Resource.Error<TimelineResponse>) resource).getMessage());
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        }
    }

    private void showAppointmentsForDate(String date) {
        if (appointmentsByDay == null || appointmentsByDay.isEmpty()) {
            appointmentAdapter.setAppointments(new ArrayList<>());
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Нет записей");
            rvAppointments.setVisibility(View.GONE);
            return;
        }

        List<Appointment> appointments = appointmentsByDay.get(date);
        if (appointments == null || appointments.isEmpty()) {
            appointmentAdapter.setAppointments(new ArrayList<>());
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Нет записей на этот день");
            rvAppointments.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.VISIBLE);
            appointmentAdapter.setAppointments(appointments);
        }
    }
}