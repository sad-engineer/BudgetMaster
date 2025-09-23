package com.sadengineer.budgetmaster.accounts;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;

/**
 * Фрагмент для отображения сберегательных счетов (тип 2)
 */
public class SavingsAccountsFragment extends BaseAccountsFragment {
    
    public SavingsAccountsFragment(int sourceTab) {
        super(sourceTab);
    }
    
    @Override
    protected void initializeResources() {
        this.mLayoutResourceId = R.layout.fragment_savings_accounts;
        this.mRecyclerViewId = R.id.accounts_savings_recycler;
        this.mAccountType = AccountTypeFilter.SAVINGS;
    }
} 