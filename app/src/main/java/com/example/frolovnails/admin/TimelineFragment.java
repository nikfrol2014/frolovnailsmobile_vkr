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
import com.example.frolovnails.network.models.request.UpdateAppointmentStatusRequest;
import com.example.frolovnails.network.models.response.Appointment;
import com.example.frolovnails.network.models.response.ScheduleBlock;
import com.example.frolovnails.network.models.response.TimelineResponse;
import com.example.frolovnails.ui.AppointmentActionDialog;
import com.example.frolovnails.ui.ClientDetailsDialog;
import com.example.frolovnails.ui.CompleteAppointmentDialog;
import com.example.frolovnails.ui.MasterNotesDialog;
import com.example.frolovnails.ui.RescheduleDialog;
import com.example.frolovnails.utils.ToastUtils;

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
    private boolean isInitialized = false;

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

        // ========== ОБРАБОТЧИК КЛИКА ПО ЗАПИСИ (открыть меню действий) ==========
        timelineView.setOnEventClickListener(this::showAppointmentActionDialog);

        // ========== ОБРАБОТЧИК КНОПКИ ЗАМЕТОК ==========
        timelineView.setOnNotesClickListener(appointment -> {
            MasterNotesDialog dialog = MasterNotesDialog.newInstance(appointment);
            dialog.show(getChildFragmentManager(), "master_notes");
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

        setupNavigation();
        isInitialized = true;

        if (getArguments() != null) {
            long dateMillis = getArguments().getLong("selected_date_millis", -1);
            if (dateMillis != -1) {
                currentCalendar.setTimeInMillis(dateMillis);
                updateDateDisplay();
                loadDataForDate();
            } else {
                loadData();
            }
        } else {
            loadData();
        }
    }

    public void setDate(long dateMillis) {
        if (isInitialized) {
            currentCalendar.setTimeInMillis(dateMillis);
            updateDateDisplay();
            loadDataForDate();
        }
    }

    private void setupNavigation() {
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
    }

    private void updateDateDisplay() {
        tvCurrentDate.setText(dateFormat.format(currentCalendar.getTime()));
    }

    private void loadData() {
        String startDate = dateFormat.format(currentCalendar.getTime());
        viewModel.loadTimeline(startDate, 1);
        loadBlocksForDate();

        viewModel.getTimelineResult().observe(getViewLifecycleOwner(), this::handleTimelineResult);
    }

    private void loadDataForDate() {
        String startDate = dateFormat.format(currentCalendar.getTime());
        viewModel.loadTimeline(startDate, 1);
        loadBlocksForDate();

        viewModel.getTimelineResult().observe(getViewLifecycleOwner(), this::handleTimelineResult);
    }

    private void loadBlocksForDate() {
        String startDate = dateFormat.format(currentCalendar.getTime());
        viewModel.loadBlocksForDate(startDate);
        viewModel.getBlocksResult().observe(getViewLifecycleOwner(), this::handleBlocksResult);
    }

    private void handleBlocksResult(Resource<List<ScheduleBlock>> resource) {
        if (resource instanceof Resource.Success) {
            List<ScheduleBlock> blocks = ((Resource.Success<List<ScheduleBlock>>) resource).getData();
            timelineView.setBlocks(blocks != null ? blocks : new ArrayList<>());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataForDate();
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
                    ToastUtils.show(getContext(), "Нет записей на этот день", Toast.LENGTH_SHORT);
                }
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            timelineView.setVisibility(View.VISIBLE);
            ToastUtils.show(getContext(), ((Resource.Error<TimelineResponse>) resource).getMessage(), Toast.LENGTH_SHORT);
        }
    }

    // ========== ДИАЛОГ ДЕЙСТВИЙ С ЗАПИСЬЮ ==========

    private void showAppointmentActionDialog(Appointment appointment) {
        if (appointment == null) {
            ToastUtils.show(getContext(), "Ошибка: запись не найдена", Toast.LENGTH_SHORT);
            return;
        }

        AppointmentActionDialog dialog = AppointmentActionDialog.newInstance(appointment);
        dialog.setOnActionListener(new AppointmentActionDialog.OnActionListener() {
            @Override
            public void onComplete(Appointment appointment) {
                showCompleteAppointmentDialog(appointment);
            }

            @Override
            public void onReschedule(Appointment appointment) {
                showRescheduleDialog(appointment);
            }

            @Override
            public void onCancel(Appointment appointment) {
                showCancelConfirmationDialog(appointment);
            }

            @Override
            public void onDelete(Appointment appointment) {
                showDeleteConfirmationDialog(appointment);
            }

            @Override
            public void onChangeStatus(Appointment appointment) {
                showStatusChangeDialog(appointment);
            }

            @Override
            public void onMasterNotes(Appointment appointment) {
                MasterNotesDialog notesDialog = MasterNotesDialog.newInstance(appointment);
                notesDialog.show(getChildFragmentManager(), "master_notes");
            }

            @Override
            public void onClientDetails(Appointment appointment) {
                ClientDetailsDialog detailsDialog = ClientDetailsDialog.newInstance(appointment.getClient().getId());
                detailsDialog.show(getChildFragmentManager(), "client_details");
            }
        });
        dialog.show(getChildFragmentManager(), "appointment_actions");
    }

    private void showCompleteAppointmentDialog(Appointment appointment) {
        CompleteAppointmentDialog dialog = CompleteAppointmentDialog.newInstance(appointment);
        dialog.setOnCompleteListener(() -> {
            loadDataForDate();  // Обновляем таймлайн после завершения
        });
        dialog.show(getChildFragmentManager(), "complete_appointment");
    }

    // ========== ПЕРЕНОС ЗАПИСИ ==========

    private void showRescheduleDialog(Appointment appointment) {
        String status = appointment.getStatus().toString();
        if ("CANCELLED".equals(status)) {
            ToastUtils.show(getContext(), "❌ Отменённую запись нельзя перенести", Toast.LENGTH_SHORT);
            return;
        }
        if ("COMPLETED".equals(status)) {
            ToastUtils.show(getContext(), "❌ Завершённую запись нельзя перенести", Toast.LENGTH_SHORT);
            return;
        }

        RescheduleDialog dialog = RescheduleDialog.newInstance(appointment);
        dialog.setOnRescheduleListener(() -> {
            loadDataForDate();
            ToastUtils.show(getContext(), "✅ Запись перенесена", Toast.LENGTH_SHORT);
        });
        dialog.show(getChildFragmentManager(), "reschedule");
    }

    // ========== ОТМЕНА ЗАПИСИ (статус CANCELLED) ==========

    private void showCancelConfirmationDialog(Appointment appointment) {
        String status = appointment.getStatus().toString();
        if ("CANCELLED".equals(status)) {
            ToastUtils.show(getContext(), "❌ Запись уже отменена", Toast.LENGTH_SHORT);
            return;
        }
        if ("COMPLETED".equals(status)) {
            ToastUtils.show(getContext(), "❌ Завершённую запись нельзя отменить", Toast.LENGTH_SHORT);
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Отмена записи")
                .setMessage("Вы уверены, что хотите отменить запись?\n\n" +
                        "👤 Клиент: " + appointment.getClient().getFirstName() + " " +
                        (appointment.getClient().getLastName() != null ? appointment.getClient().getLastName() : "") + "\n" +
                        "💅 Услуга: " + appointment.getService().getName() + "\n" +
                        "⏰ Время: " + appointment.getStartTime() + " — " + appointment.getEndTime() + "\n\n" +
                        "Запись будет переведена в статус ОТМЕНЕНО")
                .setPositiveButton("Отменить", (dialog, which) -> {
                    UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
                    request.setStatus("CANCELLED");
                    viewModel.updateAppointmentStatus(appointment.getId(), request);
                    observeCancelResult();
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void observeCancelResult() {
        viewModel.getUpdateStatusResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Success) {
                ToastUtils.show(getContext(), "✅ Запись отменена", Toast.LENGTH_SHORT);
                loadDataForDate();
                viewModel.getUpdateStatusResult().removeObservers(getViewLifecycleOwner());
            } else if (resource instanceof Resource.Error) {
                ToastUtils.show(getContext(), "❌ " + ((Resource.Error<Appointment>) resource).getMessage(), Toast.LENGTH_SHORT);
                viewModel.getUpdateStatusResult().removeObservers(getViewLifecycleOwner());
            }
        });
    }

    // ========== УДАЛЕНИЕ ЗАПИСИ (полное) ==========

    private void showDeleteConfirmationDialog(Appointment appointment) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Удаление записи")
                .setMessage("⚠️ ВНИМАНИЕ! Это действие необратимо.\n\n" +
                        "Вы уверены, что хотите ПОЛНОСТЬЮ УДАЛИТЬ запись?\n\n" +
                        "👤 Клиент: " + appointment.getClient().getFirstName() + " " +
                        (appointment.getClient().getLastName() != null ? appointment.getClient().getLastName() : "") + "\n" +
                        "💅 Услуга: " + appointment.getService().getName() + "\n" +
                        "⏰ Время: " + appointment.getStartTime() + " — " + appointment.getEndTime())
                .setPositiveButton("Удалить", (dialog, which) -> {
                    viewModel.deleteAppointment(appointment.getId());
                    observeDeleteResult();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void observeDeleteResult() {
        viewModel.getDeleteAppointmentResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Success) {
                ToastUtils.show(getContext(), "✅ Запись удалена", Toast.LENGTH_SHORT);
                loadDataForDate();
                viewModel.getDeleteAppointmentResult().removeObservers(getViewLifecycleOwner());
            } else if (resource instanceof Resource.Error) {
                ToastUtils.show(getContext(), "❌ " + ((Resource.Error<Void>) resource).getMessage(), Toast.LENGTH_SHORT);
                viewModel.getDeleteAppointmentResult().removeObservers(getViewLifecycleOwner());
            }
        });
    }

    // ========== ИЗМЕНЕНИЕ СТАТУСА ==========

    private void showStatusChangeDialog(Appointment appointment) {
        String[] statuses = {"Подтверждено", "Выполнено", "Отменено", "Ожидание"};
        String[] statusValues = {"CONFIRMED", "COMPLETED", "CANCELLED", "PENDING"};

        int checkedItem = 0;
        String currentStatus = appointment.getStatus().toString();
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equals(currentStatus)) {
                checkedItem = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Изменить статус")
                .setSingleChoiceItems(statuses, checkedItem, (dialog, which) -> {
                    String newStatus = statusValues[which];

                    UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
                    request.setStatus(newStatus);

                    viewModel.updateAppointmentStatus(appointment.getId(), request);
                    viewModel.getUpdateStatusResult().observe(getViewLifecycleOwner(), resource -> {
                        if (resource instanceof Resource.Success) {
                            ToastUtils.show(getContext(), "✅ Статус обновлен", Toast.LENGTH_SHORT);
                            loadDataForDate();
                            dialog.dismiss();
                        } else if (resource instanceof Resource.Error) {
                            ToastUtils.show(getContext(), "❌ " + ((Resource.Error<Appointment>) resource).getMessage(), Toast.LENGTH_SHORT);
                        }
                    });
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}