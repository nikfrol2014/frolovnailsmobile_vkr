package com.example.frolovnails.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.ScheduleBlock;
import com.example.frolovnails.network.models.response.ScheduleBlocksResponse;
import com.example.frolovnails.network.models.response.TimelineResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthCalendarFragment extends Fragment {

    public interface OnDaySelectedListener {
        void onDaySelected(long dateMillis);
    }

    private RecyclerView rvCalendar;
    private CalendarViewModel viewModel;
    private ScheduleViewModel scheduleViewModel;
    private Calendar currentCalendar = Calendar.getInstance();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private TextView tvMonthTitle;
    private Button btnPrevMonth, btnNextMonth;

    private Map<String, List<Appointment>> appointmentsByDay = new HashMap<>();
    private Map<String, List<ScheduleBlock>> blocksByDay = new HashMap<>();
    private List<DayItem> dayItems = new ArrayList<>();
    private OnDaySelectedListener daySelectedListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnDaySelectedListener) {
            daySelectedListener = (OnDaySelectedListener) getParentFragment();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_month_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCalendar = view.findViewById(R.id.rvCalendar);
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);

        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateTitle();
            loadData();
            loadBlocksForMonth();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateTitle();
            loadData();
            loadBlocksForMonth();
        });

        updateTitle();

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

        scheduleViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ScheduleViewModel.class)) {
                    try {
                        TokenManager tm = new TokenManager(requireContext());
                        return (T) new ScheduleViewModel(tm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }).get(ScheduleViewModel.class);

        loadData();
        loadBlocksForMonth();
    }

    private void updateTitle() {
        tvMonthTitle.setText(monthFormat.format(currentCalendar.getTime()));
    }

    private void loadData() {
        // Получаем начало текущего месяца
        Calendar startCal = (Calendar) currentCalendar.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(startCal.getTime());

        // Загружаем 45 дней (весь месяц + запас для захвата всех дней)
        viewModel.loadTimeline(startDate, 45);
        viewModel.getTimelineResult().observe(getViewLifecycleOwner(), this::handleTimelineResult);
    }

    private void loadBlocksForMonth() {
        // Получаем начало текущего месяца
        Calendar startCal = (Calendar) currentCalendar.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dateFormat.format(startCal.getTime());

        // Получаем конец текущего месяца
        Calendar endCal = (Calendar) currentCalendar.clone();
        endCal.add(Calendar.MONTH, 1);
        endCal.add(Calendar.DAY_OF_MONTH, -1);
        String endDate = dateFormat.format(endCal.getTime());

        scheduleViewModel.getScheduleBlocks(startDate, endDate);
        scheduleViewModel.getScheduleBlocksResponseResult().observe(getViewLifecycleOwner(), this::handleBlocksResult);
    }

    private void handleTimelineResult(Resource<TimelineResponse> resource) {
        android.util.Log.d("MONTH_DEBUG", "handleTimelineResult called, resource: " + resource);
        if (resource instanceof Resource.Success) {
            TimelineResponse response = ((Resource.Success<TimelineResponse>) resource).getData();
            android.util.Log.d("MONTH_DEBUG", "appointmentsByDay size: " +
                    (response != null && response.getAppointmentsByDay() != null ?
                            response.getAppointmentsByDay().size() : 0));
            if (response != null && response.getAppointmentsByDay() != null) {
                // Конвертируем ключи из "2026-05-31" в "31.05.2026"
                Map<String, List<Appointment>> converted = new HashMap<>();
                for (Map.Entry<String, List<Appointment>> entry : response.getAppointmentsByDay().entrySet()) {
                    String[] parts = entry.getKey().split("-");
                    String newKey = parts[2] + "." + parts[1] + "." + parts[0];
                    converted.put(newKey, entry.getValue());
                }
                appointmentsByDay = converted;
                buildCalendarDays();
            }
        }
    }

    private void handleBlocksResult(Resource<ScheduleBlocksResponse> resource) {
        android.util.Log.d("MONTH_DEBUG", "handleBlocksResult called");
        if (resource instanceof Resource.Success) {
            ScheduleBlocksResponse data = ((Resource.Success<ScheduleBlocksResponse>) resource).getData();
            android.util.Log.d("MONTH_DEBUG", "blocks size: " +
                    (data != null && data.getBlocks() != null ? data.getBlocks().size() : 0));
            if (data != null && data.getBlocks() != null) {
                blocksByDay.clear();
                for (ScheduleBlock block : data.getBlocks()) {
                    String dateKey = block.getStartTime().split(" ")[0];
                    if (!blocksByDay.containsKey(dateKey)) {
                        blocksByDay.put(dateKey, new ArrayList<>());
                    }
                    blocksByDay.get(dateKey).add(block);
                }
                buildCalendarDays();
            }
        }
    }

    private void buildCalendarDays() {
        dayItems.clear();

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;

        // Пустые ячейки для дней предыдущего месяца
        for (int i = 0; i < firstDayOfWeek; i++) {
            dayItems.add(new DayItem(null, null, null));
        }

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            String dateKey = dateFormat.format(cal.getTime());
            List<Appointment> dayAppointments = appointmentsByDay.get(dateKey);
            if (dayAppointments == null) dayAppointments = new ArrayList<>();
            List<ScheduleBlock> dayBlocks = blocksByDay.get(dateKey);
            if (dayBlocks == null) dayBlocks = new ArrayList<>();
            dayItems.add(new DayItem(String.valueOf(d), dayAppointments, dayBlocks));
        }

        // Заполняем до 42 ячеек (6 недель)
        while (dayItems.size() < 42) {
            dayItems.add(new DayItem(null, null, null));
        }

        rvCalendar.setAdapter(new CalendarAdapter(dayItems, this::onDayClick));
    }

    private void onDayClick(String dayNum, List<Appointment> appointments, List<ScheduleBlock> blocks) {
        if (dayNum == null) return;

        Calendar selected = (Calendar) currentCalendar.clone();
        selected.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayNum));

        if (daySelectedListener != null) {
            daySelectedListener.onDaySelected(selected.getTimeInMillis());
        }
    }

    static class DayItem {
        String dayNum;
        List<Appointment> appointments;
        List<ScheduleBlock> blocks;
        DayItem(String dayNum, List<Appointment> appointments, List<ScheduleBlock> blocks) {
            this.dayNum = dayNum;
            this.appointments = appointments != null ? appointments : new ArrayList<>();
            this.blocks = blocks != null ? blocks : new ArrayList<>();
        }
    }

    static class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
        List<DayItem> items;
        OnDayClickListener listener;

        interface OnDayClickListener {
            void onDayClick(String dayNum, List<Appointment> appointments, List<ScheduleBlock> blocks);
        }

        CalendarAdapter(List<DayItem> items, OnDayClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_calendar_day, parent, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            DayItem item = items.get(position);
            if (item.dayNum == null) {
                holder.tvDay.setText("");
                holder.llAppointments.setVisibility(View.GONE);
                holder.itemView.setClickable(false);
            } else {
                holder.tvDay.setText(item.dayNum);
                holder.llAppointments.removeAllViews();
                holder.llAppointments.setVisibility(View.VISIBLE);

                int count = 0;
                int total = item.blocks.size() + item.appointments.size();

                // Блокировки
                for (ScheduleBlock block : item.blocks) {
                    if (count >= 3) {
                        TextView moreView = new TextView(holder.itemView.getContext());
                        moreView.setText("+ " + (total - 3) + " ещё");
                        moreView.setTextSize(10);
                        moreView.setTextColor(0xFF888888);
                        holder.llAppointments.addView(moreView);
                        break;
                    }
                    TextView blockView = new TextView(holder.itemView.getContext());
                    String startTime = block.getStartTime().split(" ")[1];
                    String endTime = block.getEndTime().split(" ")[1];
                    blockView.setText("🚫 " + startTime + "-" + endTime + " " + (block.getReason() != null ? block.getReason() : "Заблокировано"));
                    blockView.setTextSize(11);
                    blockView.setTextColor(0xFFF44336);
                    holder.llAppointments.addView(blockView);
                    count++;
                }

                // Записи
                for (Appointment apt : item.appointments) {
                    if (count >= 3) {
                        TextView moreView = new TextView(holder.itemView.getContext());
                        moreView.setText("+ " + (total - 3) + " ещё");
                        moreView.setTextSize(10);
                        moreView.setTextColor(0xFF888888);
                        holder.llAppointments.addView(moreView);
                        break;
                    }
                    TextView aptView = new TextView(holder.itemView.getContext());
                    String time = apt.getStartTime().split(" ")[1];
                    aptView.setText(time + " " + apt.getClient().getFirstName());
                    aptView.setTextSize(11);
                    aptView.setTextColor(getColorForStatus(apt.getStatus()));
                    holder.llAppointments.addView(aptView);
                    count++;
                }

                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDayClick(item.dayNum, item.appointments, item.blocks);
                    }
                });
            }
        }

        private int getColorForStatus(Appointment.AppointmentStatus status) {
            switch (status) {
                case CONFIRMED: return 0xFF4CAF50;
                case PENDING: return 0xFFFF9800;
                case CREATED: return 0xFFFF9800;
                case CANCELLED: return 0xFFF44336;
                case COMPLETED: return 0xFF2196F3;
                default: return 0xFF9E9E9E;
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class DayViewHolder extends RecyclerView.ViewHolder {
            TextView tvDay;
            LinearLayout llAppointments;
            DayViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDay = itemView.findViewById(R.id.tvDay);
                llAppointments = itemView.findViewById(R.id.llAppointments);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        loadBlocksForMonth();
    }
}