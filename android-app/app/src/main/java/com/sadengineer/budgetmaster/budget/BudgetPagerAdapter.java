package com.sadengineer.budgetmaster.budget;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Адаптер для отображения вкладок бюджета
 */
public class BudgetPagerAdapter extends FragmentStateAdapter {

    private final Map<Integer, Fragment> fragments = new HashMap<>();

    /**
     * Конструктор адаптера
     */
    public BudgetPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Создает фрагмент для указанной позиции
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new BudgetLimitsFragment();
                break;
            case 1:
                fragment = new BudgetRemainingFragment();
                break;
            default:
                fragment = new BudgetLimitsFragment();
                break;
        }
        fragments.put(position, fragment);
        return fragment;
    }

    /**
     * Возвращает количество вкладок
     */
    @Override
    public int getItemCount() {
        return 2; // 2 вкладки: Лимиты, Осталось
    }
    
    /**
     * Получает фрагмент по позиции
     */
    public Fragment getFragment(int position) {
        // Возвращаем фрагмент для указанной позиции
        return fragments.get(position);
    }
} 