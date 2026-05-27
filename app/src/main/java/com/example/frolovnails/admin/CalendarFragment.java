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

    public void switchToTimeline(long dateMillis) {
        viewPager.setCurrentItem(0, true);
        Fragment fragment = getChildFragmentManager().findFragmentByTag("f0");
        if (fragment instanceof TimelineFragment) {
            ((TimelineFragment) fragment).setDate(dateMillis);
        }
    }

    @Override
    public void onDaySelected(long dateMillis) {
        switchToTimeline(dateMillis);
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