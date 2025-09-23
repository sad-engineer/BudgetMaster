package com.sadengineer.budgetmaster.accounts;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;

/**
 * Фрагмент для отображения текущих счетов (тип 1)
 */
public class CurrentAccountsFragment extends BaseAccountsFragment {
    
    public CurrentAccountsFragment(int sourceTab) {
        super(sourceTab);
    }
    
    @Override
    protected void initializeResources() {
        this.mLayoutResourceId = R.layout.fragment_current_accounts;
        this.mRecyclerViewId = R.id.accounts_current_recycler;
        this.mAccountType = AccountTypeFilter.CURRENT;
    }
} 