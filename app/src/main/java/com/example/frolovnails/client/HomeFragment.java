package com.example.frolovnails.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.frolovnails.R;
import com.example.frolovnails.adapters.ServicesAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Service;
import com.example.frolovnails.network.models.response.SliderItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // Приветствие
    private TextView tvGreeting;

    // Слайдер
    private ViewPager2 viewPagerSlider;
    private TabLayout tabLayoutSlider;
    private View progressBarSlider;
    private SliderAdapter sliderAdapter;
    private ContentViewModel contentViewModel;

    // Услуги
    private RecyclerView rvServices;
    private ServicesAdapter servicesAdapter;
    private ServicesViewModel servicesViewModel;
    private View progressBarServices;
    private TextView tvEmptyServices;

    // Инфо-карточки
    private MaterialCardView cardAddress, cardWorkHours, cardPhone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModels();
        loadSliderFromServer();
        loadServices();
        setupClickListeners();
        setGreeting();
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);

        viewPagerSlider = view.findViewById(R.id.viewPagerSlider);
        tabLayoutSlider = view.findViewById(R.id.tabLayoutSlider);
        progressBarSlider = view.findViewById(R.id.progressBarSlider);

        rvServices = view.findViewById(R.id.rvServices);
        progressBarServices = view.findViewById(R.id.progressBar);
        tvEmptyServices = view.findViewById(R.id.tvEmptyServices);

        cardAddress = view.findViewById(R.id.cardAddress);
        cardWorkHours = view.findViewById(R.id.cardWorkHours);
        cardPhone = view.findViewById(R.id.cardPhone);

        rvServices.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        servicesAdapter = new ServicesAdapter();
        rvServices.setAdapter(servicesAdapter);

        // Клик по услуге для быстрого перехода к записи
        servicesAdapter.setOnItemClickListener(service -> {
            navigateToBooking(service);
        });
    }

    private void initViewModels() {
        // ViewModel для слайдера
        try {
            TokenManager tokenManager = new TokenManager(requireContext());
            contentViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
                @NonNull
                @Override
                public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) new ContentViewModel(tokenManager);
                }
            }).get(ContentViewModel.class);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        // ViewModel для услуг
        servicesViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                try {
                    TokenManager tokenManager = new TokenManager(requireContext());
                    return (T) new ServicesViewModel(tokenManager);
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }).get(ServicesViewModel.class);

        servicesViewModel.getServicesResult().observe(getViewLifecycleOwner(), this::handleServicesResult);
    }

    private void loadSliderFromServer() {
        if (contentViewModel == null) return;

        progressBarSlider.setVisibility(View.VISIBLE);
        contentViewModel.loadSliderItems();
        contentViewModel.getSliderResult().observe(getViewLifecycleOwner(), this::handleSliderResult);
    }

    private void handleSliderResult(Resource<List<SliderItem>> resource) {
        progressBarSlider.setVisibility(View.GONE);

        if (resource instanceof Resource.Success) {
            List<SliderItem> slides = ((Resource.Success<List<SliderItem>>) resource).getData();
            if (slides != null && !slides.isEmpty()) {
                setupSlider(slides);
                return;
            }
        }
        // Если данных нет или ошибка - показываем заглушку
        setupEmptySlider();
    }

    private void setupSlider(List<SliderItem> slides) {
        sliderAdapter = new SliderAdapter(slides);
        viewPagerSlider.setAdapter(sliderAdapter);

        new TabLayoutMediator(tabLayoutSlider, viewPagerSlider,
                (tab, position) -> {}).attach();

        viewPagerSlider.setVisibility(View.VISIBLE);
        tabLayoutSlider.setVisibility(View.VISIBLE);
    }

    private void setupEmptySlider() {
        // Скрываем слайдер или показываем сообщение
        viewPagerSlider.setVisibility(View.GONE);
        tabLayoutSlider.setVisibility(View.GONE);
    }

    private void loadServices() {
        servicesViewModel.loadServices();
    }

    private void handleServicesResult(Resource<List<Service>> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            if (progressBarServices != null) {
                progressBarServices.setVisibility(View.VISIBLE);
            }
            if (rvServices != null) {
                rvServices.setVisibility(View.GONE);
            }
            if (tvEmptyServices != null) {
                tvEmptyServices.setVisibility(View.GONE);
            }
        } else if (resource instanceof Resource.Success) {
            if (progressBarServices != null) {
                progressBarServices.setVisibility(View.GONE);
            }
            List<Service> services = ((Resource.Success<List<Service>>) resource).getData();

            if (services == null || services.isEmpty()) {
                if (tvEmptyServices != null) {
                    tvEmptyServices.setVisibility(View.VISIBLE);
                    tvEmptyServices.setText("Нет доступных услуг");
                }
                if (rvServices != null) {
                    rvServices.setVisibility(View.GONE);
                }
            } else {
                if (tvEmptyServices != null) {
                    tvEmptyServices.setVisibility(View.GONE);
                }
                if (rvServices != null) {
                    rvServices.setVisibility(View.VISIBLE);
                    servicesAdapter.setServices(services);
                }
            }
        } else if (resource instanceof Resource.Error) {
            if (progressBarServices != null) {
                progressBarServices.setVisibility(View.GONE);
            }
            if (tvEmptyServices != null) {
                tvEmptyServices.setVisibility(View.VISIBLE);
                tvEmptyServices.setText("Ошибка загрузки услуг");
            }
            if (rvServices != null) {
                rvServices.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickListeners() {
        cardAddress.setOnClickListener(v -> {
            Toast.makeText(getContext(), "г. Москва, ул. Примерная, д. 123", Toast.LENGTH_SHORT).show();
        });

        cardWorkHours.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Пн-Пт: 10:00-20:00\nСб-Вс: 10:00-18:00", Toast.LENGTH_SHORT).show();
        });

        cardPhone.setOnClickListener(v -> {
            Toast.makeText(getContext(), "+7 (916) 123-45-67", Toast.LENGTH_SHORT).show();
        });
    }

    private void setGreeting() {
        String name = getUserName();
        String timeGreeting = getTimeGreeting();
        tvGreeting.setText(timeGreeting + ", " + name + "!");
    }

    private String getUserName() {
        try {
            TokenManager tokenManager = new TokenManager(requireContext());
            // TODO: получить имя из профиля
            return "Гость";
        } catch (Exception e) {
            return "Гость";
        }
    }

    private String getTimeGreeting() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Доброе утро";
        } else if (hour >= 12 && hour < 17) {
            return "Добрый день";
        } else if (hour >= 17 && hour < 22) {
            return "Добрый вечер";
        } else {
            return "Доброй ночи";
        }
    }

    private void navigateToBooking(Service service) {
        Bundle args = new Bundle();
        args.putLong("selected_service_id", service.getId());
        args.putString("selected_service_name", service.getName());
        args.putInt("selected_service_duration", service.getDurationMinutes());

        androidx.navigation.fragment.NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_booking, args);
    }

    // ========== АДАПТЕР ДЛЯ СЛАЙДЕРА (РАБОТАЕТ С URL) ==========

    private static class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

        private final List<SliderItem> items;
        private final String baseUrl = "http://192.168.0.111:8080";

        SliderAdapter(List<SliderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_slider, parent, false);
            return new SliderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
            SliderItem item = items.get(position);

            holder.tvTitle.setText(item.getTitle());
            holder.tvDescription.setText(item.getDescription());

            String fullUrl = baseUrl + item.getImageUrl();

            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class SliderViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView tvTitle;
            TextView tvDescription;

            SliderViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivSliderImage);
                tvTitle = itemView.findViewById(R.id.tvSliderTitle);
                tvDescription = itemView.findViewById(R.id.tvSliderDescription);
            }
        }
    }
}