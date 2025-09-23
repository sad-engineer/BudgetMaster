package com.sadengineer.budgetmaster.accounts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Адаптер для отображения вкладок с счетами
 */
public class AccountsPagerAdapter extends FragmentStateAdapter {
    
    // Константы для индексов вкладок
    private static final int TAB_CURRENT = 0;
    private static final int TAB_SAVINGS = 1;
    private static final int TAB_CREDIT = 2;
    private static final int TAB_COUNT = 3;
    
    public AccountsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    /**
     * Создает фрагмент для указанной позиции
     * @param position позиция вкладки
     * @return соответствующий фрагмент
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_CURRENT:
                return new CurrentAccountsFragment(TAB_CURRENT);
            case TAB_SAVINGS:
                return new SavingsAccountsFragment(TAB_SAVINGS);
            case TAB_CREDIT:
                return new CreditAccountsFragment(TAB_CREDIT);
            default:
                return new CurrentAccountsFragment(TAB_CURRENT);
        }
    }

    /**
     * Возвращает количество вкладок
     * @return количество вкладок
     */
    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
} 