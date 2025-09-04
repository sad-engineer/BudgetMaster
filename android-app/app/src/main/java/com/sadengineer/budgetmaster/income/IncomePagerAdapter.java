package com.sadengineer.budgetmaster.income;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class IncomePagerAdapter extends FragmentStateAdapter {

    public IncomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new IncomeAllFragment();
            case 1:
                return new IncomeDaysFragment();
            case 2:
                return new IncomeCategoriesFragment();
            case 3:
                return new IncomeChartsFragment();
            default:
                return new IncomeAllFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // 4 вкладки: ВСЕ, ДНИ, КАТЕГОРИИ, ГРАФИКИ
    }
} 