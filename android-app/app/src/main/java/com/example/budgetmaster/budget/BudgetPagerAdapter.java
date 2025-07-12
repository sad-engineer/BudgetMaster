package com.example.budgetmaster.budget;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BudgetPagerAdapter extends FragmentStateAdapter {

    public BudgetPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BudgetLimitsFragment();
            case 1:
                return new BudgetRemainingFragment();
            default:
                return new BudgetLimitsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 вкладки: Лимиты, Осталось
    }
} 