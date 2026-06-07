package com.example.frolovnails.client;

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

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.ServicesAdapter;
import com.example.frolovnails.client.ScheduleViewModel;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.request.CreateAppointmentRequest;
import com.example.frolovnails.network.models.response.AvailableDay;
import com.example.frolovnails.network.models.response.AvailableSlotsResponse;
import com.example.frolovnails.network.models.response.Service;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookingFragment extends Fragment {

    // Шаг 1: Выбор услуги
    private MaterialCardView cardServiceSelection;
    private TextView tvSelectedService;
    private RecyclerView rvServices;
    private ServicesAdapter servicesAdapter;
    private Long selectedServiceId;
    private String selectedServiceName;
    private int selectedServiceDuration;

    // Шаг 2: Выбор даты
    private MaterialCardView cardDateSelection;
    private TextView tvSelectedDate;
    private RecyclerView rvAvailableDays;
    private AvailableDaysAdapter availableDaysAdapter;
    private String selectedDate;

    // Шаг 3: Выбор времени
    private MaterialCardView cardTimeSelection, cardNotes;
    private RecyclerView rvAvailableSlots;
    private AvailableSlotsAdapter slotsAdapter;
    private String selectedSlot;

    // Шаг 4: Заметки и подтверждение
    private TextInputEditText etNotes;
    private Button btnConfirmBooking;

    private ProgressBar progressBar;
    private TextView tvStep1Status, tvStep2Status, tvStep3Status;

    private BookingViewModel viewModel;
    private ServicesViewModel servicesViewModel;
    private ScheduleViewModel scheduleViewModel;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        loadServices();
        setupClickListeners();

        // Проверяем, передан ли ID услуги из HomeFragment
        if (getArguments() != null) {
            selectedServiceId = getArguments().getLong("selected_service_id", 0);
            selectedServiceName = getArguments().getString("selected_service_name");
            selectedServiceDuration = getArguments().getInt("selected_service_duration", 0);
            if (selectedServiceId != 0) {
                updateSelectedServiceDisplay();
                loadAvailableDays();
            }
        }
    }

    private void initViews(View view) {
        // Шаг 1
        cardServiceSelection = view.findViewById(R.id.cardServiceSelection);
        tvSelectedService = view.findViewById(R.id.tvSelectedService);
        rvServices = view.findViewById(R.id.rvServices);
        tvStep1Status = view.findViewById(R.id.tvStep1Status);

        // Шаг 2
        cardDateSelection = view.findViewById(R.id.cardDateSelection);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        rvAvailableDays = view.findViewById(R.id.rvAvailableDays);
        tvStep2Status = view.findViewById(R.id.tvStep2Status);

        // Шаг 3
        cardTimeSelection = view.findViewById(R.id.cardTimeSelection);
        rvAvailableSlots = view.findViewById(R.id.rvAvailableSlots);
        tvStep3Status = view.findViewById(R.id.tvStep3Status);

        // Шаг 4
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking);
        cardNotes = view.findViewById(R.id.cardNotes);
        etNotes = view.findViewById(R.id.etNotes);

        progressBar = view.findViewById(R.id.progressBar);

        // Настройка RecyclerView для услуг
        rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        servicesAdapter = new ServicesAdapter();
        rvServices.setAdapter(servicesAdapter);

        // Настройка RecyclerView для доступных дней
        rvAvailableDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        availableDaysAdapter = new AvailableDaysAdapter();
        rvAvailableDays.setAdapter(availableDaysAdapter);

        // Настройка RecyclerView для слотов
        rvAvailableSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        slotsAdapter = new AvailableSlotsAdapter();
        rvAvailableSlots.setAdapter(slotsAdapter);
    }

    private void initViewModels() {
        try {
            TokenManager tokenManager = new TokenManager(requireContext());

            servicesViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @NonNull
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) new ServicesViewModel(tokenManager);
                }
            }).get(ServicesViewModel.class);

            scheduleViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @NonNull
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) new ScheduleViewModel(tokenManager);
                }
            }).get(ScheduleViewModel.class);

            viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @NonNull
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) new BookingViewModel(tokenManager);
                }
            }).get(BookingViewModel.class);

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        // Наблюдатели
        servicesViewModel.getServicesResult().observe(getViewLifecycleOwner(), this::handleServicesResult);
        scheduleViewModel.getAvailableDaysResult().observe(getViewLifecycleOwner(), this::handleAvailableDaysResult);
        scheduleViewModel.getAvailableSlotsResult().observe(getViewLifecycleOwner(), this::handleAvailableSlotsResult);
        viewModel.getCreateAppointmentResult().observe(getViewLifecycleOwner(), this::handleCreateResult);
    }

    private void loadServices() {
        servicesViewModel.loadServices();
    }

    private void handleServicesResult(Resource<List<Service>> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Success) {
            List<Service> services = ((Resource.Success<List<Service>>) resource).getData();
            if (services != null && !services.isEmpty()) {
                servicesAdapter.setServices(services);
                servicesAdapter.setOnItemClickListener(service -> {
                    selectedServiceId = service.getId();
                    selectedServiceName = service.getName();
                    selectedServiceDuration = service.getDurationMinutes();
                    updateSelectedServiceDisplay();
                    loadAvailableDays();
                });
            }
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), "Ошибка загрузки услуг", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSelectedServiceDisplay() {
        tvSelectedService.setText(selectedServiceName + " (" + selectedServiceDuration + " мин)");
        tvStep1Status.setText("✅ Выбрано");
        cardServiceSelection.setStrokeColor(getResources().getColor(R.color.success, null));

        // Открываем выбор даты
        cardDateSelection.setVisibility(View.VISIBLE);
    }

    private void loadAvailableDays() {
        if (selectedServiceId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        scheduleViewModel.loadAvailableDays(30); // Убрали serviceId
    }

    private void handleAvailableDaysResult(Resource<List<AvailableDay>> resource) {
        progressBar.setVisibility(View.GONE);

        if (resource instanceof Resource.Success) {
            List<AvailableDay> days = ((Resource.Success<List<AvailableDay>>) resource).getData();
            if (days != null && !days.isEmpty()) {
                availableDaysAdapter.setDays(days);
                availableDaysAdapter.setOnDaySelectedListener(day -> {
                    selectedDate = day.getAvailableDate();
                    tvSelectedDate.setText(selectedDate);
                    tvStep2Status.setText("✅ Выбрано");
                    availableDaysAdapter.setDays(days);
                    cardDateSelection.setStrokeColor(getResources().getColor(R.color.success, null));

                    // Загружаем слоты для выбранной даты
                    loadAvailableSlots();
                });
            } else {
                Toast.makeText(getContext(), "Нет доступных дней для записи", Toast.LENGTH_SHORT).show();
            }
        } else if (resource instanceof Resource.Error) {
            Toast.makeText(getContext(), "Ошибка загрузки доступных дней", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAvailableSlots() {
        if (selectedServiceId == null || selectedDate == null) return;

        progressBar.setVisibility(View.VISIBLE);
        scheduleViewModel.loadAvailableSlots(selectedDate, selectedServiceId);
    }

    private void handleAvailableSlotsResult(Resource<AvailableSlotsResponse> resource) {
        progressBar.setVisibility(View.GONE);

        if (resource instanceof Resource.Success) {
            AvailableSlotsResponse response = ((Resource.Success<AvailableSlotsResponse>) resource).getData();

            // Логируем для отладки
            android.util.Log.d("BookingFragment", "Слоты получены: " +
                    (response != null && response.getAvailableSlots() != null ?
                            response.getAvailableSlots().size() : 0));

            if (response != null && response.getAvailableSlots() != null && !response.getAvailableSlots().isEmpty()) {
                // Показываем слоты
                slotsAdapter.setSlots(response.getAvailableSlots());
                slotsAdapter.setOnSlotSelectedListener(slot -> {
                    selectedSlot = slot;
                    // Извлекаем только время из полной даты
                    String timeOnly = extractTimeFromSlot(slot);
                    tvStep3Status.setText("✅ Выбрано: " + timeOnly);
                    cardTimeSelection.setStrokeColor(getResources().getColor(R.color.success, null));
                    btnConfirmBooking.setVisibility(View.VISIBLE);
                });
                cardTimeSelection.setVisibility(View.VISIBLE);
                rvAvailableSlots.setVisibility(View.VISIBLE);
                // ПОКАЗЫВАЕМ КАРТОЧКУ С ЗАМЕТКАМИ
                cardNotes.setVisibility(View.VISIBLE);
                btnConfirmBooking.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "Нет доступного времени на выбранную дату", Toast.LENGTH_SHORT).show();
                cardTimeSelection.setVisibility(View.GONE);
            }
        } else if (resource instanceof Resource.Error) {
            String error = ((Resource.Error<AvailableSlotsResponse>) resource).getMessage();
            android.util.Log.e("BookingFragment", "Ошибка загрузки слотов: " + error);
            Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
            cardTimeSelection.setVisibility(View.GONE);
        }
    }

    private String extractTimeFromSlot(String slot) {
        // slot приходит в формате "2026-06-09 10:00:00"
        if (slot != null && slot.contains(" ")) {
            String[] parts = slot.split(" ");
            if (parts.length >= 2) {
                String time = parts[1];
                // Убираем секунды
                if (time.length() > 5) {
                    time = time.substring(0, 5);
                }
                return time;
            }
        }
        return slot;
    }

    // Форматирование времени для отображения
    private String formatSlotTime(String slot) {
        // slot приходит в формате "2026-06-09 10:00:00"
        // нужно показать только время "10:00"
        if (slot != null && slot.contains(" ")) {
            String[] parts = slot.split(" ");
            if (parts.length >= 2) {
                String time = parts[1];
                // Убираем секунды если есть
                if (time.length() > 5) {
                    time = time.substring(0, 5);
                }
                return time;
            }
        }
        return slot;
    }

    private void setupClickListeners() {
        // Выбор услуги (открыть список)
        cardServiceSelection.setOnClickListener(v -> {
            if (rvServices.getVisibility() == View.VISIBLE) {
                rvServices.setVisibility(View.GONE);
            } else {
                rvServices.setVisibility(View.VISIBLE);
            }
        });

        // Выбор даты (открыть календарь)
        cardDateSelection.setOnClickListener(v -> {
            showDatePicker();
        });

        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            selectedDate = dateFormat.format(calendar.getTime());
            tvSelectedDate.setText(selectedDate);
            tvStep2Status.setText("✅ Выбрано");
            loadAvailableSlots();
        });

        datePicker.show(getChildFragmentManager(), "date_picker");
    }

    private void confirmBooking() {
        if (selectedServiceId == null) {
            Toast.makeText(getContext(), "Выберите услугу", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Выберите дату", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSlot == null) {
            Toast.makeText(getContext(), "Выберите время", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем заметки из поля
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        // Извлекаем время из слота
        String timeOnly = extractTimeFromSlot(selectedSlot);
        String startTime = selectedDate + " " + timeOnly;

        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setServiceId(selectedServiceId);
        request.setStartTime(startTime);
        request.setClientNotes(notes);  // ← заметки клиента

        android.util.Log.d("BookingFragment", "Отправка запроса: serviceId=" + selectedServiceId +
                ", startTime=" + startTime + ", notes=" + notes);

        progressBar.setVisibility(View.VISIBLE);
        btnConfirmBooking.setEnabled(false);

        viewModel.createAppointment(request);
    }

    private void handleCreateResult(Resource<Void> resource) {
        progressBar.setVisibility(View.GONE);
        btnConfirmBooking.setEnabled(true);

        if (resource instanceof Resource.Success) {
            // Показываем успешное сообщение и сбрасываем форму
            Toast.makeText(getContext(), "✅ Запись успешно создана!", Toast.LENGTH_LONG).show();
            resetForm();
        } else if (resource instanceof Resource.Error) {
            String error = ((Resource.Error<Void>) resource).getMessage();
            Toast.makeText(getContext(), "❌ Ошибка: " + error, Toast.LENGTH_LONG).show();
        }
    }

    private void resetForm() {
        selectedServiceId = null;
        selectedServiceName = null;
        selectedDate = null;
        selectedSlot = null;

        tvSelectedService.setText("Не выбрано");
        tvSelectedDate.setText("Не выбрана");
        tvStep1Status.setText("⬜ Не выбран");
        tvStep2Status.setText("⬜ Не выбрана");
        tvStep3Status.setText("⬜ Не выбрано");
        etNotes.setText("");

        cardServiceSelection.setStrokeColor(getResources().getColor(R.color.text_hint, null));
        cardDateSelection.setStrokeColor(getResources().getColor(R.color.text_hint, null));
        cardTimeSelection.setStrokeColor(getResources().getColor(R.color.text_hint, null));
        btnConfirmBooking.setVisibility(View.GONE);
        cardTimeSelection.setVisibility(View.GONE);
        rvServices.setVisibility(View.GONE);

        // Скрываем карточку заметок
        cardNotes.setVisibility(View.GONE);
        etNotes.setText("");
    }

    // Внутренний адаптер для доступных дней
    private static class AvailableDaysAdapter extends RecyclerView.Adapter<AvailableDaysAdapter.DayViewHolder> {

        private List<AvailableDay> days = new ArrayList<>();
        private OnDaySelectedListener listener;

        interface OnDaySelectedListener {
            void onDaySelected(AvailableDay day);
        }

        void setDays(List<AvailableDay> days) {
            this.days = days;
            notifyDataSetChanged();
        }

        void setOnDaySelectedListener(OnDaySelectedListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_day_small, parent, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            AvailableDay day = days.get(position);
            holder.bind(day, listener);
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        static class DayViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvWorkHours;

            DayViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvWorkHours = itemView.findViewById(R.id.tvWorkHours);
            }

            void bind(AvailableDay day, OnDaySelectedListener listener) {
                tvDate.setText(day.getAvailableDate());
                tvWorkHours.setText(day.getWorkStart() + " — " + day.getWorkEnd());
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDaySelected(day);
                    }
                });
            }
        }
    }

    // Внутренний адаптер для доступных слотов
    private static class AvailableSlotsAdapter extends RecyclerView.Adapter<AvailableSlotsAdapter.SlotViewHolder> {

        private List<String> slots = new ArrayList<>();
        private OnSlotSelectedListener listener;
        private int selectedPosition = -1;

        interface OnSlotSelectedListener {
            void onSlotSelected(String slot);
        }

        void setSlots(List<String> slots) {
            this.slots = slots;
            this.selectedPosition = -1;
            notifyDataSetChanged();
        }

        void setOnSlotSelectedListener(OnSlotSelectedListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_slot, parent, false);
            return new SlotViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
            String slot = slots.get(position);
            boolean isSelected = position == selectedPosition;
            holder.bind(slot, isSelected, v -> {
                selectedPosition = holder.getAdapterPosition();
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onSlotSelected(slot);
                }
            });
        }

        @Override
        public int getItemCount() {
            return slots.size();
        }

        static class SlotViewHolder extends RecyclerView.ViewHolder {
            TextView tvSlot;

            SlotViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSlot = itemView.findViewById(R.id.tvSlot);
            }

            void bind(String slot, boolean isSelected, View.OnClickListener clickListener) {
                tvSlot.setText(slot);
                if (isSelected) {
                    tvSlot.setBackgroundResource(R.drawable.bg_slot_selected);
                } else {
                    tvSlot.setBackgroundResource(R.drawable.bg_slot_unselected);
                }
                itemView.setOnClickListener(clickListener);
            }
        }
    }
}