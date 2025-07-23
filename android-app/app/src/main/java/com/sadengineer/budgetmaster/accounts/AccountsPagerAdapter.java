package com.sadengineer.budgetmaster.accounts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AccountsPagerAdapter extends FragmentStateAdapter {
    public AccountsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CurrentAccountsFragment();
            case 1:
                return new SavingsAccountsFragment();
            case 2:
                return new TransfersAccountsFragment();
            default:
                return new CurrentAccountsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
} 