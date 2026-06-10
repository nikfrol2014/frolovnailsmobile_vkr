package com.example.frolovnails.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.frolovnails.R;
import com.example.frolovnails.network.models.response.SliderItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import lombok.NonNull;

public class FullscreenImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGES = "extra_images";
    public static final String EXTRA_POSITION = "extra_position";

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        // Полноэкранный режим
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Убираем ActionBar (если он есть)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        viewPager = findViewById(R.id.viewPagerFullscreen);
        tabLayout = findViewById(R.id.tabLayoutFullscreen);
        btnClose = findViewById(R.id.btnClose);

        // Получаем данные
        List<SliderItem> images = (List<SliderItem>) getIntent().getSerializableExtra(EXTRA_IMAGES);
        int startPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);

        if (images != null && !images.isEmpty()) {
            FullscreenAdapter adapter = new FullscreenAdapter(images);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(startPosition, false);

            // TabLayout для индикаторов
            new TabLayoutMediator(tabLayout, viewPager,
                    (tab, position) -> {}).attach();
        }

        // Закрытие по кнопке
        btnClose.setOnClickListener(v -> finish());

        // Закрытие по клику на ViewPager
        viewPager.setOnClickListener(v -> finish());
    }

    private class FullscreenAdapter extends RecyclerView.Adapter<FullscreenAdapter.FullscreenViewHolder> {

        private final List<SliderItem> items;
        private final String baseUrl = "http://192.168.0.111:8080";

        FullscreenAdapter(List<SliderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public FullscreenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_fullscreen_image, parent, false);
            return new FullscreenViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FullscreenViewHolder holder, int position) {
            SliderItem item = items.get(position);
            String fullUrl = baseUrl + item.getImageUrl();

            Glide.with(holder.itemView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class FullscreenViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            FullscreenViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivFullscreenImage);
            }
        }
    }
}