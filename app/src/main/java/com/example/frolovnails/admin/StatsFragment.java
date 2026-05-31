package com.example.frolovnails.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.frolovnails.adapters.TopClientsAdapter;
import com.example.frolovnails.adapters.TopServicesAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.stats.DashboardStatsResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private Calendar customStartCalendar;
    private Calendar customEndCalendar;
    private String customStartDate;
    private String customEndDate;
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private SwipeRefreshLayout swipeRefresh;

    // Графики
    private LineChart chartRevenue;
    private BarChart chartAppointments;
    private BarChart chartPeakHours;
    private TabLayout tabChartType;

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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

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
        setupCharts();
        setupSwipeRefresh();  // ДОБАВИТЬ
        setupDateRangePicker();  // ДОБАВИТЬ для выбора диапазона

        viewModel.loadStatsForWeek();
    }

    private void initViews(View view) {
        // Графики
        chartRevenue = view.findViewById(R.id.chartRevenue);
        chartAppointments = view.findViewById(R.id.chartAppointments);
        chartPeakHours = view.findViewById(R.id.chartPeakHours);
        tabChartType = view.findViewById(R.id.tabChartType);

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

        swipeRefresh = view.findViewById(R.id.swipeRefreshStats);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                // Обновляем данные в зависимости от текущего выбранного периода
                if (btnWeek.isChecked()) {
                    viewModel.loadStatsForWeek();
                } else if (btnMonth.isChecked()) {
                    viewModel.loadStatsForMonth();
                } else if (btnCustom.isChecked() && customStartDate != null && customEndDate != null) {
                    viewModel.loadStatsForPeriod(customStartDate, customEndDate);
                } else {
                    viewModel.loadStatsForWeek();
                }
            });
            swipeRefresh.setColorSchemeColors(
                    getResources().getColor(android.R.color.holo_blue_dark),
                    getResources().getColor(android.R.color.holo_green_dark),
                    getResources().getColor(android.R.color.holo_orange_dark)
            );
        }
    }

    private void setupCharts() {
        // Настройка графика выручки (линейный)
        chartRevenue.getDescription().setEnabled(false);
        chartRevenue.setTouchEnabled(true);
        chartRevenue.setDragEnabled(true);
        chartRevenue.setScaleEnabled(true);
        chartRevenue.setPinchZoom(true);
        chartRevenue.setDrawGridBackground(false);

        XAxis xAxisRevenue = chartRevenue.getXAxis();
        xAxisRevenue.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisRevenue.setGranularity(1f);
        xAxisRevenue.setLabelRotationAngle(-45);

        YAxis leftAxisRevenue = chartRevenue.getAxisLeft();
        leftAxisRevenue.setDrawGridLines(true);
        leftAxisRevenue.setAxisMinimum(0f);

        chartRevenue.getAxisRight().setEnabled(false);

        // Настройка графика записей (столбчатый)
        chartAppointments.getDescription().setEnabled(false);
        chartAppointments.setTouchEnabled(true);
        chartAppointments.setDragEnabled(true);
        chartAppointments.setScaleEnabled(true);
        chartAppointments.setDrawGridBackground(false);

        XAxis xAxisAppointments = chartAppointments.getXAxis();
        xAxisAppointments.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisAppointments.setGranularity(1f);
        xAxisAppointments.setLabelRotationAngle(-45);

        YAxis leftAxisAppointments = chartAppointments.getAxisLeft();
        leftAxisAppointments.setDrawGridLines(true);
        leftAxisAppointments.setAxisMinimum(0f);

        chartAppointments.getAxisRight().setEnabled(false);

        // Настройка графика часов пик (столбчатый)
        chartPeakHours.getDescription().setEnabled(false);
        chartPeakHours.setTouchEnabled(true);
        chartPeakHours.setDrawGridBackground(false);

        XAxis xAxisPeak = chartPeakHours.getXAxis();
        xAxisPeak.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisPeak.setGranularity(1f);

        YAxis leftAxisPeak = chartPeakHours.getAxisLeft();
        leftAxisPeak.setDrawGridLines(true);
        leftAxisPeak.setAxisMinimum(0f);

        chartPeakHours.getAxisRight().setEnabled(false);

        // Переключение типа графика (выручка/записи)
        tabChartType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    chartRevenue.setVisibility(View.VISIBLE);
                    chartAppointments.setVisibility(View.GONE);
                } else {
                    chartRevenue.setVisibility(View.GONE);
                    chartAppointments.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
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
        // Инициализируем календари
        if (customStartCalendar == null) {
            customStartCalendar = Calendar.getInstance();
            customStartCalendar.add(Calendar.DAY_OF_MONTH, -7); // неделя назад по умолчанию
        }
        if (customEndCalendar == null) {
            customEndCalendar = Calendar.getInstance();
        }

        // Создаем диалог выбора диапазона
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Выберите период");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_date_range, null);

        TextView tvStartDate = dialogView.findViewById(R.id.tvStartDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);
        Button btnApply = dialogView.findViewById(R.id.btnApply);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Отображаем текущие даты
        updateDateRangeDisplay(tvStartDate, tvEndDate);

        // Выбор начальной даты
        tvStartDate.setOnClickListener(v -> showSingleDatePicker(true, tvStartDate, tvEndDate));

        // Выбор конечной даты
        tvEndDate.setOnClickListener(v -> showSingleDatePicker(false, tvStartDate, tvEndDate));

        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        btnApply.setOnClickListener(v -> {
            if (customStartCalendar != null && customEndCalendar != null) {
                customStartDate = apiDateFormat.format(customStartCalendar.getTime());
                customEndDate = apiDateFormat.format(customEndCalendar.getTime());

                // Обновляем заголовок периода
                tvPeriodRange.setText(customStartDate + " — " + customEndDate);

                // Загружаем статистику
                viewModel.loadStatsForPeriod(customStartDate, customEndDate);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Выберите начальную и конечную дату", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSingleDatePicker(boolean isStartDate, TextView tvStartDate, TextView tvEndDate) {
        Calendar calendar = isStartDate ? customStartCalendar : customEndCalendar;
        if (calendar == null) {
            calendar = Calendar.getInstance();
            if (isStartDate) {
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                customStartCalendar = calendar;
            } else {
                customEndCalendar = calendar;
            }
        }

        com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                        .setTitleText(isStartDate ? "Выберите начальную дату" : "Выберите конечную дату")
                        .setSelection(calendar.getTimeInMillis())
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTimeInMillis(selection);

            if (isStartDate) {
                customStartCalendar = selectedCalendar;
                // Если начальная дата больше конечной, корректируем конечную
                if (customEndCalendar != null && customStartCalendar.after(customEndCalendar)) {
                    customEndCalendar = (Calendar) customStartCalendar.clone();
                    customEndCalendar.add(Calendar.DAY_OF_MONTH, 7);
                }
            } else {
                customEndCalendar = selectedCalendar;
                // Если конечная дата меньше начальной, корректируем начальную
                if (customStartCalendar != null && customEndCalendar.before(customStartCalendar)) {
                    customStartCalendar = (Calendar) customEndCalendar.clone();
                    customStartCalendar.add(Calendar.DAY_OF_MONTH, -7);
                }
            }

            updateDateRangeDisplay(tvStartDate, tvEndDate);
        });

        datePicker.show(getChildFragmentManager(), "date_picker");
    }

    private void updateDateRangeDisplay(TextView tvStartDate, TextView tvEndDate) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        if (customStartCalendar != null) {
            tvStartDate.setText(displayFormat.format(customStartCalendar.getTime()));
        } else {
            tvStartDate.setText("Выбрать");
        }
        if (customEndCalendar != null) {
            tvEndDate.setText(displayFormat.format(customEndCalendar.getTime()));
        } else {
            tvEndDate.setText("Выбрать");
        }
    }

    private void handleStatsResult(Resource<DashboardStatsResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            // НЕ скрываем карточки при загрузке через SwipeRefresh
            if (swipeRefresh == null || !swipeRefresh.isRefreshing()) {
                cardTotalAppointments.setVisibility(View.GONE);
                cardRevenue.setVisibility(View.GONE);
                cardAverageCheck.setVisibility(View.GONE);
                cardOccupancy.setVisibility(View.GONE);
                chartRevenue.setVisibility(View.GONE);
                chartAppointments.setVisibility(View.GONE);
                chartPeakHours.setVisibility(View.GONE);
            }
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            // Останавливаем SwipeRefresh если он крутится
            if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
            cardTotalAppointments.setVisibility(View.VISIBLE);
            cardRevenue.setVisibility(View.VISIBLE);
            cardAverageCheck.setVisibility(View.VISIBLE);
            cardOccupancy.setVisibility(View.VISIBLE);
            chartRevenue.setVisibility(View.VISIBLE);
            chartPeakHours.setVisibility(View.VISIBLE);

            DashboardStatsResponse data = ((Resource.Success<DashboardStatsResponse>) resource).getData();
            if (data != null) {
                displayStats(data);
                updateCharts(data);
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            // Останавливаем SwipeRefresh если он крутится
            if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
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

    private void updateCharts(DashboardStatsResponse data) {
        // График выручки и записей по дням
        if (data.getDailyStats() != null && !data.getDailyStats().isEmpty()) {
            updateRevenueChart(data.getDailyStats());
            updateAppointmentsChart(data.getDailyStats());
        }

        // График часов пик
        if (data.getPeakHours() != null && !data.getPeakHours().isEmpty()) {
            updatePeakHoursChart(data.getPeakHours());
        }
    }

    private void updateRevenueChart(List<DashboardStatsResponse.DailyStats> dailyStats) {
        List<Entry> revenueEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < dailyStats.size(); i++) {
            DashboardStatsResponse.DailyStats day = dailyStats.get(i);
            float revenue = day.getRevenue() != null ? day.getRevenue().floatValue() : 0f;
            revenueEntries.add(new Entry(i, revenue));
            xLabels.add(day.getDayOfWeek() + "\n" + day.getDate());
        }

        LineDataSet dataSet = new LineDataSet(revenueEntries, "Выручка, ₽");
        dataSet.setColor(Color.rgb(76, 175, 80));
        dataSet.setCircleColor(Color.rgb(76, 175, 80));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ((int) value) + " ₽";
            }
        });
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(76, 175, 80));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        chartRevenue.setData(lineData);

        XAxis xAxis = chartRevenue.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        chartRevenue.invalidate();
    }

    private void updateAppointmentsChart(List<DashboardStatsResponse.DailyStats> dailyStats) {
        List<BarEntry> appointmentsEntries = new ArrayList<>();
        List<BarEntry> completedEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < dailyStats.size(); i++) {
            DashboardStatsResponse.DailyStats day = dailyStats.get(i);
            appointmentsEntries.add(new BarEntry(i, day.getAppointmentsCount()));
            completedEntries.add(new BarEntry(i, day.getCompletedCount()));
            xLabels.add(day.getDayOfWeek() + "\n" + day.getDate());
        }

        BarDataSet appointmentsSet = new BarDataSet(appointmentsEntries, "Всего записей");
        appointmentsSet.setColor(Color.rgb(33, 150, 243));
        appointmentsSet.setValueTextSize(10f);

        BarDataSet completedSet = new BarDataSet(completedEntries, "Выполнено");
        completedSet.setColor(Color.rgb(76, 175, 80));
        completedSet.setValueTextSize(10f);

        BarData barData = new BarData(appointmentsSet, completedSet);
        barData.setBarWidth(0.35f);

        chartAppointments.setData(barData);

        XAxis xAxis = chartAppointments.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        chartAppointments.invalidate();
    }

    private void updatePeakHoursChart(List<DashboardStatsResponse.HourlyStats> peakHours) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        for (DashboardStatsResponse.HourlyStats hour : peakHours) {
            entries.add(new BarEntry(hour.getHour(), (float) hour.getOccupancyRate()));
            xLabels.add(hour.getHour() + ":00");
        }

        BarDataSet dataSet = new BarDataSet(entries, "Загруженность, %");
        dataSet.setColor(Color.rgb(255, 152, 0));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ((int) value) + "%";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        chartPeakHours.setData(barData);

        XAxis xAxis = chartPeakHours.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);

        chartPeakHours.invalidate();
    }

    private void setupDateRangePicker() {
        btnCustom.setOnClickListener(v -> showDateRangePicker());
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
        if (data.getDailyStats() != null && !data.getDailyStats().isEmpty()) {
            String firstDate = data.getDailyStats().get(0).getDate();
            String lastDate = data.getDailyStats().get(data.getDailyStats().size() - 1).getDate();
            tvPeriodRange.setText(firstDate + " — " + lastDate);
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0 ₽";
        return price + " ₽";
    }
}