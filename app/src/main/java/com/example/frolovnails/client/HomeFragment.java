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

import com.example.frolovnails.R;
import com.example.frolovnails.adapters.ServicesAdapter;
import com.example.frolovnails.common.Resource;
import com.example.frolovnails.common.TokenManager;
import com.example.frolovnails.network.models.response.Service;
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
    private SliderAdapter sliderAdapter;

    // Услуги
    private RecyclerView rvServices;
    private ServicesAdapter servicesAdapter;
    private ServicesViewModel servicesViewModel;
    private View progressBar;
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
        initSlider();
        initServices();
        initViewModel();
        loadServices();
        setupClickListeners();
        setGreeting();
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);

        viewPagerSlider = view.findViewById(R.id.viewPagerSlider);
        tabLayoutSlider = view.findViewById(R.id.tabLayoutSlider);

        rvServices = view.findViewById(R.id.rvServices);
        progressBar = view.findViewById(R.id.progressBar);
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

    private void initSlider() {
        // Слайды с акциями (можно загружать с сервера или статические)
        List<SliderItem> slides = new ArrayList<>();
        slides.add(new SliderItem(R.drawable.ic_launcher_foreground, "Акция: Маникюр + Педикюр = -20%"));
        slides.add(new SliderItem(R.drawable.ic_launcher_foreground, "Новинка: Дизайн с фольгой"));
        slides.add(new SliderItem(R.drawable.ic_launcher_foreground, "Подарок имениннику: скидка 15%"));

        sliderAdapter = new SliderAdapter(slides);
        viewPagerSlider.setAdapter(sliderAdapter);

        new TabLayoutMediator(tabLayoutSlider, viewPagerSlider,
                (tab, position) -> {
                    // Можно установить иконки или текст
                }).attach();
    }

    private void initServices() {
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
    }

    private void initViewModel() {
        servicesViewModel.getServicesResult().observe(getViewLifecycleOwner(), this::handleServicesResult);
    }

    private void loadServices() {
        servicesViewModel.loadServices();
    }

    private void handleServicesResult(Resource<List<Service>> resource) {
        if (resource == null) return;

        if (resource instanceof Resource.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            rvServices.setVisibility(View.GONE);
            tvEmptyServices.setVisibility(View.GONE);
        } else if (resource instanceof Resource.Success) {
            progressBar.setVisibility(View.GONE);
            List<Service> services = ((Resource.Success<List<Service>>) resource).getData();

            if (services == null || services.isEmpty()) {
                tvEmptyServices.setVisibility(View.VISIBLE);
                rvServices.setVisibility(View.GONE);
            } else {
                tvEmptyServices.setVisibility(View.GONE);
                rvServices.setVisibility(View.VISIBLE);
                servicesAdapter.setServices(services);
            }
        } else if (resource instanceof Resource.Error) {
            progressBar.setVisibility(View.GONE);
            tvEmptyServices.setVisibility(View.VISIBLE);
            tvEmptyServices.setText("Ошибка загрузки услуг");
        }
    }

    private void setupClickListeners() {
        cardAddress.setOnClickListener(v -> {
            // Показать адрес на карте
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
            // Пока заглушка, позже можно получить из профиля
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
        // Переход на фрагмент записи с выбранной услугой
        Bundle args = new Bundle();
        args.putLong("selected_service_id", service.getId());
        args.putString("selected_service_name", service.getName());
        args.putInt("selected_service_duration", service.getDurationMinutes());

        androidx.navigation.fragment.NavHostFragment.findNavController(this)
                .navigate(R.id.action_home_to_booking, args);
    }

    // Внутренний класс для слайдера
    private static class SliderItem {
        int imageRes;
        String title;

        SliderItem(int imageRes, String title) {
            this.imageRes = imageRes;
            this.title = title;
        }

        public int getImageRes() { return imageRes; }
        public String getTitle() { return title; }
    }

    private static class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

        private List<SliderItem> items;

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
            holder.imageView.setImageResource(item.getImageRes());
            holder.textView.setText(item.getTitle());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class SliderViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;

            SliderViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivSliderImage);
                textView = itemView.findViewById(R.id.tvSliderTitle);
            }
        }
    }
}