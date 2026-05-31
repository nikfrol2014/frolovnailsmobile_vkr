package com.example.frolovnails.admin;

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

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.TopClientsAdapter;
import com.example.frolovnails.adapters.TopServicesAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.stats.DashboardStatsResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatsFragment extends Fragment {

    // Карточки с метриками
    private MaterialCardView cardTotalAppointments, cardRevenue, cardAverageCheck, cardOccupancy;
    private TextView tvTotalAppointments, tvRevenue, tvAverageCheck, tvOccupancy;
    private TextView tvAppointmentsChange, tvRevenueChange, tvAverageCheckChange, tvOccupancyChange;

    // Дополнительная статистика
    private TextView tvCompletedCount, tvCancelledCount, tvCreatedCount, tvConfirmedCount, tvNewClients;

    // Списки
    private RecyclerView rvTopServices, rvTopClients;
    private TopServicesAdapter topServicesAdapter;
    private TopClientsAdapter topClientsAdapter;

    // Кнопки периода
    private MaterialButton btnWeek, btnMonth, btnCustom;

    private ProgressBar progressBar;
    private StatsViewModel viewModel;
    private TextView tvPeriodRange;

    private DecimalFormat decimalFormat = new DecimalFormat("#.#");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        setupClickListeners();

        // Загружаем статистику за текущую неделю по умолчанию
        viewModel.loadStatsForWeek();
    }

    private void initViews(View view) {
        // Карточки
        cardTotalAppointments = view.findViewById(R.id.cardTotalAppointments);
        cardRevenue = view.findViewById(R.id.cardRevenue);
        cardAverageCheck = view.findViewById(R.id.cardAverageCheck);
        cardOccupancy = view.findViewById(R.id.cardOccupancy);

        tvTotalAppointments = view.findViewById(R.id.tvTotalAppointments);
        tvRevenue = view.findViewById(R.id.tvRevenue);
        tvAverageCheck = view.findViewById(R.id.tvAverageCheck);
        tvOccupancy = view.findViewById(R.id.tvOccupancy);

        tvAppointmentsChange = view.findViewById(R.id.tvAppointmentsChange);
        tvRevenueChange = view.findViewById(R.id.tvRevenueChange);
        tvAverageCheckChange = view.findViewById(R.id.tvAverageCheckChange);
        tvOccupancyChange = view.findViewById(R.id.tvOccupancyChange);

        // Дополнительная статистика
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvCancelledCount = view.findViewById(R.id.tvCancelledCount);
        tvCreatedCount = view.findViewById(R.id.tvCreatedCount);
        tvConfirmedCount = view.findViewById(R.id.tvConfirmedCount);
        tvNewClients = view.findViewById(R.id.tvNewClients);

        // Списки
        rvTopServices = view.findViewById(R.id.rvTopServices);
        rvTopClients = view.findViewById(R.id.rvTopClients);

        rvTopServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTopClients.setLayoutManager(new LinearLayoutManager(getContext()));

        topServicesAdapter = new TopServicesAdapter();
        topClientsAdapter = new TopClientsAdapter();

        rvTopServices.setAdapter(topServicesAdapter);
        rvTopClients.setAdapter(topClientsAdapter);

        // Кнопки
        btnWeek = view.findViewById(R.id.btnWeek);
        btnMonth = view.findViewById(R.id.btnMonth);
        btnCustom = view.findViewById(R.id.btnCustom);

        progressBar = view.findViewById(R.id.progressBar);
        tvPeriodRange = view.findViewById(R.id.tvPeriodRange);
    }

    private void initViewModels() {
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
                return (T) new StatsViewModel(finalTokenManager);
            }
        }).get(StatsViewModel.class);

        viewModel.getDashboardStats().observe(getViewLifecycleOwner(), this::handleStatsResult);
    }

    private void setupClickListeners() {
        btnWeek.setOnClickListener(v -> {
            setActiveButton(btnWeek);
            viewModel.loadStatsForWeek();
        });

        btnMonth.setOnClickListener(v -> {
            setActiveButton(btnMonth);
            viewModel.loadStatsForMonth();
        });

        btnCustom.setOnClickListener(v -> {
            setActiveButton(btnCustom);
            showDateRangePicker();
        });
    }

    private void setActiveButton(MaterialButton activeButton) {
        btnWeek.setChecked(false);
        btnMonth.setChecked(false);
        btnCustom.setChecked(false);
        activeButton.setChecked(true);
    }

    private void showDateRangePicker() {
        // TODO: реализовать выбор диапазона дат
        // Можно использовать DateRangePickerDialog из Material Design
        Toast.makeText(getContext(), "Выбор диапазона дат будет добавлен позже", Toast.LENGTH_SHORT).show();
    }

    private void handleStatsResult(Resource<DashboardStatsResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            cardTotalAppointments.setVisibility(View.GONE);
            cardRevenue.setVisibility(View.GONE);
            cardAverageCheck.setVisibility(View.GONE);
            cardOccupancy.setVisibility(View.GONE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            cardTotalAppointments.setVisibility(View.VISIBLE);
            cardRevenue.setVisibility(View.VISIBLE);
            cardAverageCheck.setVisibility(View.VISIBLE);
            cardOccupancy.setVisibility(View.VISIBLE);

            DashboardStatsResponse data = ((Resource.Success<DashboardStatsResponse>) resource).getData();
            if (data != null) {
                displayStats(data);
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            String error = ((Resource.Error<DashboardStatsResponse>) resource).getMessage();
            Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayStats(DashboardStatsResponse data) {
        DashboardStatsResponse.PeriodStats current = data.getCurrentPeriod();
        DashboardStatsResponse.ComparisonStats comparison = data.getComparison();

        // Основные метрики
        tvTotalAppointments.setText(String.valueOf(current.getTotalAppointments()));
        tvRevenue.setText(formatPrice(current.getTotalRevenue()));
        tvAverageCheck.setText(formatPrice(current.getAverageCheck()));
        tvOccupancy.setText(decimalFormat.format(current.getOccupancyRate()) + "%");

        // Изменения
        setChangeText(tvAppointmentsChange, comparison.getAppointmentsChange());
        setChangeText(tvRevenueChange, comparison.getRevenueChange());
        setChangeText(tvAverageCheckChange, comparison.getAverageCheckChange());
        setChangeText(tvOccupancyChange, comparison.getOccupancyChange());

        // Дополнительная статистика
        tvCompletedCount.setText(String.valueOf(current.getCompletedAppointments()));
        tvCancelledCount.setText(String.valueOf(current.getCancelledAppointments()));
        tvCreatedCount.setText(String.valueOf(current.getCreatedAppointments()));
        tvConfirmedCount.setText(String.valueOf(current.getConfirmedAppointments()));
        tvNewClients.setText(String.valueOf(current.getNewClientsCount()));

        // Топ услуг
        if (data.getTopServices() != null && !data.getTopServices().isEmpty()) {
            topServicesAdapter.setServices(data.getTopServices());
        }

        // Топ клиентов
        if (data.getTopClients() != null && !data.getTopClients().isEmpty()) {
            topClientsAdapter.setClients(data.getTopClients());
        }

        // Обновляем заголовок периода
        updatePeriodTitle(data);
    }

    private void setChangeText(TextView textView, double change) {
        if (change > 0) {
            textView.setText("↑ +" + decimalFormat.format(change) + "%");
            textView.setTextColor(0xFF4CAF50);
        } else if (change < 0) {
            textView.setText("↓ " + decimalFormat.format(change) + "%");
            textView.setTextColor(0xFFF44336);
        } else {
            textView.setText("→ 0%");
            textView.setTextColor(0xFF9E9E9E);
        }
    }

    private void updatePeriodTitle(DashboardStatsResponse data) {
        // Пытаемся определить период из dailyStats
        if (data.getDailyStats() != null && !data.getDailyStats().isEmpty()) {
            String firstDate = data.getDailyStats().get(0).getDate();
            String lastDate = data.getDailyStats().get(data.getDailyStats().size() - 1).getDate();
            tvPeriodRange.setText(firstDate + " — " + lastDate);
        }
    }

    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "0 ₽";
        return price + " ₽";
    }
}