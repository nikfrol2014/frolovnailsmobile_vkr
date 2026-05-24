package com.example.frolovnails.admin;

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

import com.example.frolovnails.R;
import com.example.frolovnails.calendar.views.SimpleTimelineView;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.TimelineResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimelineFragment extends Fragment {

    private SimpleTimelineView timelineView;
    private ProgressBar progressBar;
    private TextView tvCurrentDate;
    private Button btnPrevDay, btnNextDay, btnToday;
    private CalendarViewModel viewModel;

    private Calendar currentCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.timeline_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timelineView = view.findViewById(R.id.timelineView);
        progressBar = view.findViewById(R.id.progressBar);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        btnPrevDay = view.findViewById(R.id.btnPrevDay);
        btnNextDay = view.findViewById(R.id.btnNextDay);
        btnToday = view.findViewById(R.id.btnToday);

        updateDateDisplay();

        btnPrevDay.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            loadData();
        });

        btnNextDay.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            loadData();
        });

        btnToday.setOnClickListener(v -> {
            currentCalendar = Calendar.getInstance();
            updateDateDisplay();
            loadData();
        });

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

    private void updateDateDisplay() {
        tvCurrentDate.setText(dateFormat.format(currentCalendar.getTime()));
    }

    private void loadData() {
        // Используем метод без startDate
        viewModel.loadTimeline(7);
        viewModel.getTimelineResult().observe(getViewLifecycleOwner(), this::handleTimelineResult);
    }

    private void handleTimelineResult(Resource<TimelineResponse> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            timelineView.setVisibility(View.GONE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            timelineView.setVisibility(View.VISIBLE);

            TimelineResponse response = ((Resource.Success<TimelineResponse>) resource).getData();
            if (response != null) {
                String currentDateStr = dateFormat.format(currentCalendar.getTime());
                String[] parts = currentDateStr.split("\\.");
                String serverDateKey = parts[2] + "-" + parts[1] + "-" + parts[0];

                List<Appointment> dayAppointments = response.getAppointmentsByDay().get(serverDateKey);
                if (dayAppointments == null) dayAppointments = new ArrayList<>();

                timelineView.setAppointments(dayAppointments);

                if (dayAppointments.isEmpty()) {
                    Toast.makeText(getContext(), "Нет записей на этот день", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            timelineView.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), ((Resource.Error<TimelineResponse>) resource).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}