package com.example.frolovnails.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.AppointmentAdapter;
import com.example.frolovnails.adapters.DateAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.TimelineResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private RecyclerView rvDates, rvAppointments;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvStats;
    private CalendarViewModel viewModel;
    private DateAdapter dateAdapter;
    private AppointmentAdapter appointmentAdapter;
    private List<String> datesList = new ArrayList<>();
    private Map<String, List<Appointment>> appointmentsByDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDates = view.findViewById(R.id.rvDates);
        rvAppointments = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvStats = view.findViewById(R.id.tvStats);

        // Настройка горизонтального списка дат
        rvDates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dateAdapter = new DateAdapter();
        rvDates.setAdapter(dateAdapter);

        // Настройка списка записей
        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentAdapter = new AppointmentAdapter();
        rvAppointments.setAdapter(appointmentAdapter);

        // Инициализация ViewModel
        TokenManager tokenManager = null;
        try {
            tokenManager = new TokenManager(requireContext());
            Log.d("CalendarFrag", "TokenManager created, token exists: " + (tokenManager.getAccessToken() != null));
        } catch (Exception e) {
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

        // Обработчик выбора даты
        dateAdapter.setOnDateClickListener((date, position) -> {
            dateAdapter.setSelectedPosition(position);
            showAppointmentsForDate(date);
        });

        // Загрузка данных
        viewModel.loadTimeline(7);
        viewModel.getTimelineResult().observe(getViewLifecycleOwner(), this::handleTimelineResult);
    }

    private void handleTimelineResult(Resource<TimelineResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            rvDates.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            return;
        }

        if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);

            TimelineResponse response = ((Resource.Success<TimelineResponse>) resource).getData();

            if (response == null) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Нет данных");
                return;
            }

            // Получаем исходную карту
            Map<String, List<Appointment>> originalMap = response.getAppointmentsByDay();

            if (originalMap == null || originalMap.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Нет записей");
                rvDates.setVisibility(View.GONE);
                rvAppointments.setVisibility(View.GONE);
                return;
            }

            // Преобразуем ключи из "2026-05-21" в "21.05.2026"
            appointmentsByDay = new HashMap<>();
            for (Map.Entry<String, List<Appointment>> entry : originalMap.entrySet()) {
                String oldKey = entry.getKey();
                // Преобразуем "2026-05-21" → "21.05.2026"
                String newKey = convertDateFormat(oldKey);
                appointmentsByDay.put(newKey, entry.getValue());
            }

            // Заполняем список дат
            datesList = new ArrayList<>(appointmentsByDay.keySet());
            datesList.sort(String::compareTo);
            dateAdapter.setDates(datesList);

            rvDates.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);

            // Статистика
            TimelineResponse.TimelineStats stats = response.getStats();
            if (stats != null) {
                String statsText = "✅ " + stats.getConfirmedCount() +
                        " | ⏳ " + stats.getPendingCount() +
                        " | ❌ " + stats.getCancelledCount() +
                        " | ✔️ " + stats.getCompletedCount();
                tvStats.setText(statsText);
            }

            // Показываем записи за первый день
            if (!datesList.isEmpty()) {
                dateAdapter.setSelectedPosition(0);
                showAppointmentsForDate(datesList.get(0));
            }

        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            rvDates.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.GONE);
            tvEmpty.setText("Ошибка: " + ((Resource.Error<TimelineResponse>) resource).getMessage());
        }
    }

    // Метод для преобразования формата даты
    private String convertDateFormat(String date) {
        if (date == null) return "";
        // Если уже в формате dd.MM.yyyy
        if (date.contains(".")) return date;
        // Если в формате yyyy-MM-dd
        if (date.contains("-")) {
            String[] parts = date.split("-");
            if (parts.length == 3) {
                return parts[2] + "." + parts[1] + "." + parts[0];
            }
        }
        return date;
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