package com.example.budgetmaster.expense;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ExpensePagerAdapter extends FragmentStateAdapter {

    public ExpensePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ExpenseAllFragment();
            case 1:
                return new ExpenseDaysFragment();
            case 2:
                return new ExpenseCategoriesFragment();
            case 3:
                return new ExpenseChartsFragment();
            default:
                return new ExpenseAllFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // 4 вкладки: ВСЕ, ДНИ, КАТЕГОРИИ, ГРАФИКИ
    }
} 