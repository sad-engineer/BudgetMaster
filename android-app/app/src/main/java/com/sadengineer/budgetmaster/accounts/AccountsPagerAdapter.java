package com.sadengineer.budgetmaster.accounts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Адаптер для отображения вкладок с счетами
 */
public class AccountsPagerAdapter extends FragmentStateAdapter {
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
            case 0:
                return new CurrentAccountsFragment(0);
            case 1:
                return new SavingsAccountsFragment(1);
            case 2:
                return new CreditAccountsFragment(2);
            default:
                return new CurrentAccountsFragment(0);
        }
    }

    /**
     * Возвращает количество вкладок
     * @return количество вкладок
     */
    @Override
    public int getItemCount() {
        return 3;
    }
} 