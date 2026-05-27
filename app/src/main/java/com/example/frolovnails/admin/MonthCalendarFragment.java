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
    private Calendar currentCalendar = Calendar.getInstance();
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private TextView tvMonthTitle;
    private Button btnPrevMonth, btnNextMonth;

    private Map<String, List<Appointment>> appointmentsByDay = new HashMap<>();
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
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateTitle();
            loadData();
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

        loadData();
    }

    private void updateTitle() {
        tvMonthTitle.setText(monthFormat.format(currentCalendar.getTime()));
    }

    private void loadData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String startDate = dateFormat.format(currentCalendar.getTime());
        viewModel.loadTimeline(35);
        viewModel.getTimelineResult().observe(getViewLifecycleOwner(), this::handleTimelineResult);
    }

    private void handleTimelineResult(Resource<TimelineResponse> resource) {
        if (resource instanceof Resource.Success) {
            TimelineResponse response = ((Resource.Success<TimelineResponse>) resource).getData();
            if (response != null && response.getAppointmentsByDay() != null) {
                appointmentsByDay = response.getAppointmentsByDay();
                buildCalendarDays();
            }
        }
    }

    private void buildCalendarDays() {
        dayItems.clear();

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < firstDayOfWeek; i++) {
            dayItems.add(new DayItem(null, null));
        }

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            List<Appointment> dayAppointments = appointmentsByDay.get(dateKey);
            if (dayAppointments == null) dayAppointments = new ArrayList<>();
            dayItems.add(new DayItem(String.valueOf(d), dayAppointments));
        }

        while (dayItems.size() < 42) {
            dayItems.add(new DayItem(null, null));
        }

        rvCalendar.setAdapter(new CalendarAdapter(dayItems, this::onDayClick));
    }

    private void onDayClick(String dayNum, List<Appointment> appointments) {
        if (dayNum == null) return;

        Calendar selected = (Calendar) currentCalendar.clone();
        selected.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayNum));

        // Получаем родительский CalendarFragment
        Fragment parent = getParentFragment();
        if (parent instanceof CalendarFragment) {
            ((CalendarFragment) parent).switchToTimeline(selected.getTimeInMillis());
        }
    }

    static class DayItem {
        String dayNum;
        List<Appointment> appointments;
        DayItem(String dayNum, List<Appointment> appointments) {
            this.dayNum = dayNum;
            this.appointments = appointments != null ? appointments : new ArrayList<>();
        }
    }

    static class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {
        List<DayItem> items;
        OnDayClickListener listener;

        interface OnDayClickListener {
            void onDayClick(String dayNum, List<Appointment> appointments);
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
            } else {
                holder.tvDay.setText(item.dayNum);
                holder.llAppointments.removeAllViews();

                int count = 0;
                for (Appointment apt : item.appointments) {
                    if (count >= 3) {
                        TextView moreView = new TextView(holder.itemView.getContext());
                        moreView.setText("+ " + (item.appointments.size() - 3) + " ещё");
                        moreView.setTextSize(10);
                        moreView.setTextColor(0xFF888888);
                        holder.llAppointments.addView(moreView);
                        break;
                    }
                    TextView aptView = new TextView(holder.itemView.getContext());
                    String time = apt.getStartTime().split(" ")[1].substring(0, 5);
                    aptView.setText(time + " " + apt.getClient().getFirstName());
                    aptView.setTextSize(11);
                    aptView.setTextColor(getColorForStatus(apt.getStatus()));
                    holder.llAppointments.addView(aptView);
                    count++;
                }

                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDayClick(item.dayNum, item.appointments);
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
}