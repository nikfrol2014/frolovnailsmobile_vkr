package com.example.frolovnails.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.frolovnails.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CalendarFragment extends Fragment implements MonthCalendarFragment.OnDaySelectedListener {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new CalendarPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Таймлайн");
            } else {
                tab.setText("Месяц");
            }
        }).attach();
    }

    @Override
    public void onDaySelected(long dateMillis) {
        Bundle bundle = new Bundle();
        bundle.putLong("selected_date_millis", dateMillis);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_calendar_to_timeline, bundle);
    }

    public void switchToTimeline(long dateMillis) {
        // Переключаемся на вкладку "Таймлайн"
        viewPager.setCurrentItem(0, true);

        // Получаем TimelineFragment из ViewPager2
        Fragment fragment = getChildFragmentManager().findFragmentByTag("f0");
        if (fragment instanceof TimelineFragment) {
            ((TimelineFragment) fragment).setDate(dateMillis);
        }
    }

    private static class CalendarPagerAdapter extends FragmentStateAdapter {
        public CalendarPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new TimelineFragment();
            } else {
                return new MonthCalendarFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}