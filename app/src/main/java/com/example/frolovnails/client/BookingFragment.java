package com.example.frolovnails.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frolovnails.R;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.request.CreateAppointmentRequest;
import com.example.frolovnails.network.models.response.AvailableDay;
import com.example.frolovnails.network.models.response.AvailableSlotsResponse;
import com.example.frolovnails.network.models.response.Service;
import com.example.frolovnails.utils.ToastUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingFragment extends Fragment {

    // Шаг 1: Категории
    private LinearLayout step1Container;
    private RecyclerView rvCategories;
    private CategoriesAdapter categoriesAdapter;
    private String selectedCategory;

    // Шаг 2: Услуги
    private LinearLayout step2Container;
    private RecyclerView rvServices;
    private ServicesAdapter servicesAdapter;
    private Long selectedServiceId;
    private String selectedServiceName;
    private int selectedServiceDuration;

    // Шаг 3: Дата
    private LinearLayout step3Container;
    private RecyclerView rvAvailableDays;
    private AvailableDaysAdapter availableDaysAdapter;
    private String selectedDate;

    // Шаг 4: Время
    private LinearLayout step4Container;
    private RecyclerView rvAvailableSlots;
    private AvailableSlotsAdapter slotsAdapter;
    private String selectedSlot;

    // Шаг 5: Заметки и подтверждение
    private LinearLayout step5Container;
    private TextInputEditText etNotes;
    private Button btnConfirmBooking;

    // Навигация
    private Button btnBack, btnNext;
    private TextView tvStepTitle;
    private ProgressBar progressBar;

    private int currentStep = 1;

    private BookingViewModel viewModel;
    private ServicesViewModel servicesViewModel;
    private ScheduleViewModel scheduleViewModel;

    private Map<String, List<Service>> servicesByCategory = new HashMap<>();
    private List<String> categoriesList = new ArrayList<>();
    private List<AvailableDay> availableDaysList = new ArrayList<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        loadCategoriesAndServices();
        updateStepVisibility();
        setupNavigation();
    }

    private void initViews(View view) {
        // Контейнеры шагов
        step1Container = view.findViewById(R.id.step1Container);
        step2Container = view.findViewById(R.id.step2Container);
        step3Container = view.findViewById(R.id.step3Container);
        step4Container = view.findViewById(R.id.step4Container);
        step5Container = view.findViewById(R.id.step5Container);

        // RecyclerViews
        rvCategories = view.findViewById(R.id.rvCategories);
        rvServices = view.findViewById(R.id.rvServices);
        rvAvailableDays = view.findViewById(R.id.rvAvailableDays);
        rvAvailableSlots = view.findViewById(R.id.rvAvailableSlots);

        // Кнопки навигации
        btnBack = view.findViewById(R.id.btnBack);
        btnNext = view.findViewById(R.id.btnNext);
        tvStepTitle = view.findViewById(R.id.tvStepTitle);

        // Шаг 5
        etNotes = view.findViewById(R.id.etNotes);
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking);

        progressBar = view.findViewById(R.id.progressBar);

        // Настройка RecyclerViews
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvServices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvailableDays.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvAvailableSlots.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Изначально кнопка назад скрыта
        btnBack.setVisibility(View.GONE);
    }

    private void setupNavigation() {
        btnBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateStepVisibility();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < 5) {
                    currentStep++;
                    updateStepVisibility();
                }
            }
        });

        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                if (selectedCategory == null) {
                    ToastUtils.show(getContext(), "Выберите категорию", Toast.LENGTH_SHORT);
                    return false;
                }
                return true;
            case 2:
                if (selectedServiceId == null) {
                    ToastUtils.show(getContext(), "Выберите услугу", Toast.LENGTH_SHORT);
                    return false;
                }
                return true;
            case 3:
                if (selectedDate == null) {
                    ToastUtils.show(getContext(), "Выберите дату", Toast.LENGTH_SHORT);
                    return false;
                }
                return true;
            case 4:
                if (selectedSlot == null) {
                    ToastUtils.show(getContext(), "Выберите время", Toast.LENGTH_SHORT);
                    return false;
                }
                return true;
            case 5:
                return true;
            default:
                return true;
        }
    }

    private void updateStepVisibility() {
        // Скрываем все шаги
        step1Container.setVisibility(View.GONE);
        step2Container.setVisibility(View.GONE);
        step3Container.setVisibility(View.GONE);
        step4Container.setVisibility(View.GONE);
        step5Container.setVisibility(View.GONE);

        // Показываем текущий шаг
        switch (currentStep) {
            case 1:
                step1Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Шаг 1/5: Выберите категорию");
                btnNext.setText("Далее");
                btnBack.setVisibility(View.GONE);
                break;
            case 2:
                step2Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Шаг 2/5: Выберите услугу");
                btnNext.setText("Далее");
                btnBack.setVisibility(View.VISIBLE);
                break;
            case 3:
                step3Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Шаг 3/5: Выберите дату");
                btnNext.setText("Далее");
                btnBack.setVisibility(View.VISIBLE);
                break;
            case 4:
                step4Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Шаг 4/5: Выберите время");
                btnNext.setText("Далее");
                btnBack.setVisibility(View.VISIBLE);
                break;
            case 5:
                step5Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Шаг 5/5: Подтверждение");
                btnNext.setVisibility(View.GONE);
                btnBack.setVisibility(View.VISIBLE);
                btnConfirmBooking.setVisibility(View.VISIBLE);
                break;
        }
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

        servicesViewModel.getServicesResult().observe(getViewLifecycleOwner(), this::handleServicesResult);
        scheduleViewModel.getAvailableDaysResult().observe(getViewLifecycleOwner(), this::handleAvailableDaysResult);
        scheduleViewModel.getAvailableSlotsResult().observe(getViewLifecycleOwner(), this::handleAvailableSlotsResult);
        viewModel.getCreateAppointmentResult().observe(getViewLifecycleOwner(), this::handleCreateResult);
    }

    private void loadCategoriesAndServices() {
        progressBar.setVisibility(View.VISIBLE);
        servicesViewModel.loadServices();
    }

    private void handleServicesResult(Resource<List<Service>> resource) {
        progressBar.setVisibility(View.GONE);

        if (resource instanceof Resource.Success) {
            List<Service> services = ((Resource.Success<List<Service>>) resource).getData();
            if (services != null && !services.isEmpty()) {
                groupServicesByCategory(services);
                setupCategories();
            } else {
                ToastUtils.show(getContext(), "Нет доступных услуг", Toast.LENGTH_SHORT);
            }
        } else if (resource instanceof Resource.Error) {
            ToastUtils.show(getContext(), "Ошибка загрузки услуг", Toast.LENGTH_SHORT);
        }
    }

    private void groupServicesByCategory(List<Service> services) {
        servicesByCategory.clear();
        categoriesList.clear();

        for (Service service : services) {
            String category = service.getCategory();
            if (!servicesByCategory.containsKey(category)) {
                servicesByCategory.put(category, new ArrayList<>());
                categoriesList.add(category);
            }
            servicesByCategory.get(category).add(service);
        }
    }

    private void setupCategories() {
        categoriesAdapter = new CategoriesAdapter(categoriesList, category -> {
            selectedCategory = category;
            // Автоматически переходим к следующему шагу после выбора
            currentStep = 2;
            showServicesForCategory(category);
            updateStepVisibility();
        });
        rvCategories.setAdapter(categoriesAdapter);
        rvCategories.setVisibility(View.VISIBLE);
    }

    private void showServicesForCategory(String category) {
        List<Service> services = servicesByCategory.get(category);
        if (services != null && !services.isEmpty()) {
            servicesAdapter = new ServicesAdapter(services, service -> {
                selectedServiceId = service.getId();
                selectedServiceName = service.getName();
                selectedServiceDuration = service.getDurationMinutes();
                // Автоматически переходим к следующему шагу после выбора
                currentStep = 3;
                loadAvailableDaysWithSlots();
                updateStepVisibility();
            });
            rvServices.setAdapter(servicesAdapter);
            rvServices.setVisibility(View.VISIBLE);
        }
    }

    private void loadAvailableDaysWithSlots() {
        progressBar.setVisibility(View.VISIBLE);
        scheduleViewModel.loadAvailableDaysWithSlots(selectedServiceId, 30);
    }

    private void handleAvailableDaysResult(Resource<List<AvailableDay>> resource) {
        progressBar.setVisibility(View.GONE);

        if (resource instanceof Resource.Success) {
            List<AvailableDay> days = ((Resource.Success<List<AvailableDay>>) resource).getData();

            if (days != null && !days.isEmpty()) {
                // Сортируем дни по дате
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                Collections.sort(days, (d1, d2) -> {
                    try {
                        java.util.Date date1 = sdf.parse(d1.getAvailableDate());
                        java.util.Date date2 = sdf.parse(d2.getAvailableDate());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                availableDaysList = days;
                availableDaysAdapter = new AvailableDaysAdapter(availableDaysList, day -> {
                    selectedDate = day.getAvailableDate();
                    // Автоматически переходим к следующему шагу после выбора
                    currentStep = 4;
                    loadAvailableSlots();
                    updateStepVisibility();
                });
                rvAvailableDays.setAdapter(availableDaysAdapter);
                rvAvailableDays.setVisibility(View.VISIBLE);
            } else {
                ToastUtils.show(getContext(), "Нет доступных дней для записи", Toast.LENGTH_SHORT);
            }
        } else if (resource instanceof Resource.Error) {
            String error = ((Resource.Error<List<AvailableDay>>) resource).getMessage();
            ToastUtils.show(getContext(), error, Toast.LENGTH_SHORT);
        }
    }

    private void loadAvailableSlots() {
        progressBar.setVisibility(View.VISIBLE);
        scheduleViewModel.loadAvailableSlots(selectedDate, selectedServiceId);
    }

    private void handleAvailableSlotsResult(Resource<AvailableSlotsResponse> resource) {
        progressBar.setVisibility(View.GONE);

        if (resource instanceof Resource.Success) {
            AvailableSlotsResponse response = ((Resource.Success<AvailableSlotsResponse>) resource).getData();
            if (response != null && response.getAvailableSlots() != null && !response.getAvailableSlots().isEmpty()) {
                List<String> slots = response.getAvailableSlots();
                slotsAdapter = new AvailableSlotsAdapter(slots, slot -> {
                    selectedSlot = extractTimeFromSlot(slot);
                    // Автоматически переходим к следующему шагу после выбора
                    currentStep = 5;
                    updateStepVisibility();
                });
                rvAvailableSlots.setAdapter(slotsAdapter);
                rvAvailableSlots.setVisibility(View.VISIBLE);
            } else {
                ToastUtils.show(getContext(), "Нет доступного времени на выбранную дату", Toast.LENGTH_SHORT);
            }
        } else if (resource instanceof Resource.Error) {
            String error = ((Resource.Error<AvailableSlotsResponse>) resource).getMessage();
            ToastUtils.show(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT);
        }
    }

    private void confirmBooking() {
        String startTime = selectedDate + " " + selectedSlot;
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setServiceId(selectedServiceId);
        request.setStartTime(startTime);
        request.setClientNotes(notes);

        progressBar.setVisibility(View.VISIBLE);
        btnConfirmBooking.setEnabled(false);
        btnConfirmBooking.setText("⏳ Сохранение...");

        viewModel.createAppointment(request);
    }

    private void handleCreateResult(Resource<Void> resource) {
        progressBar.setVisibility(View.GONE);
        btnConfirmBooking.setEnabled(true);
        btnConfirmBooking.setText("✅ Подтвердить запись");

        if (resource instanceof Resource.Success) {
            ToastUtils.show(getContext(), "✅ Запись успешно создана!", Toast.LENGTH_LONG);

            // Сбрасываем форму, но остаемся на месте
            resetForm();

        } else if (resource instanceof Resource.Error) {
            String error = ((Resource.Error<Void>) resource).getMessage();
            ToastUtils.show(getContext(), "❌ Ошибка: " + error, Toast.LENGTH_LONG);
        }
    }

    private void resetForm() {
        selectedCategory = null;
        selectedServiceId = null;
        selectedDate = null;
        selectedSlot = null;

        currentStep = 1;

        // Очищаем данные
        categoriesList.clear();
        servicesByCategory.clear();
        availableDaysList.clear();

        // Перезагружаем категории
        loadCategoriesAndServices();

        // Обновляем UI
        updateStepVisibility();
    }

    private String extractTimeFromSlot(String slot) {
        if (slot != null && slot.contains(" ")) {
            String[] parts = slot.split(" ");
            if (parts.length >= 2) {
                String time = parts[1];
                if (time.length() > 5) {
                    time = time.substring(0, 5);
                }
                return time;
            }
        }
        return slot;
    }

    // ========== АДАПТЕРЫ ==========

    private static class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
        private final List<String> categories;
        private final OnCategoryClickListener listener;

        interface OnCategoryClickListener {
            void onCategoryClick(String category);
        }

        CategoriesAdapter(List<String> categories, OnCategoryClickListener listener) {
            this.categories = categories;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String category = categories.get(position);
            holder.tvCategory.setText(category);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategory;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategory = itemView.findViewById(R.id.tvCategory);
            }
        }
    }

    private static class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ViewHolder> {
        private final List<Service> services;
        private final OnServiceClickListener listener;

        interface OnServiceClickListener {
            void onServiceClick(Service service);
        }

        ServicesAdapter(List<Service> services, OnServiceClickListener listener) {
            this.services = services;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_service_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Service service = services.get(position);
            holder.tvServiceName.setText(service.getName());
            holder.tvServiceDuration.setText(service.getDurationMinutes() + " мин");
            holder.tvServicePrice.setText(service.getPrice() + " ₽");
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onServiceClick(service);
                }
            });
        }

        @Override
        public int getItemCount() {
            return services.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvServiceName, tvServiceDuration, tvServicePrice;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvServiceName = itemView.findViewById(R.id.tvServiceName);
                tvServiceDuration = itemView.findViewById(R.id.tvServiceDuration);
                tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            }
        }
    }

    private static class AvailableDaysAdapter extends RecyclerView.Adapter<AvailableDaysAdapter.ViewHolder> {
        private final List<AvailableDay> days;
        private final OnDayClickListener listener;

        interface OnDayClickListener {
            void onDayClick(AvailableDay day);
        }

        AvailableDaysAdapter(List<AvailableDay> days, OnDayClickListener listener) {
            this.days = days;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_day_small, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AvailableDay day = days.get(position);
            holder.tvDate.setText(day.getAvailableDate());
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }

    private static class AvailableSlotsAdapter extends RecyclerView.Adapter<AvailableSlotsAdapter.ViewHolder> {
        private final List<String> slots;
        private final OnSlotClickListener listener;

        interface OnSlotClickListener {
            void onSlotClick(String slot);
        }

        AvailableSlotsAdapter(List<String> slots, OnSlotClickListener listener) {
            this.slots = slots;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_slot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String slot = slots.get(position);
            String time = extractTime(slot);
            holder.tvSlot.setText(time);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSlotClick(slot);
                }
            });
        }

        private String extractTime(String slot) {
            if (slot != null && slot.contains(" ")) {
                String[] parts = slot.split(" ");
                if (parts.length >= 2) {
                    String time = parts[1];
                    if (time.length() > 5) time = time.substring(0, 5);
                    return time;
                }
            }
            return slot;
        }

        @Override
        public int getItemCount() {
            return slots.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSlot;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSlot = itemView.findViewById(R.id.tvSlot);
            }
        }
    }
}