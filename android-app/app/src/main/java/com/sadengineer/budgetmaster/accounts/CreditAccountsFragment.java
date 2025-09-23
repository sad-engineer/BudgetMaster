package com.sadengineer.budgetmaster.accounts;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;

/**
 * Фрагмент для отображения кредитных счетов (тип 3)
 */
public class CreditAccountsFragment extends BaseAccountsFragment {
    
    public CreditAccountsFragment(int sourceTab) {
        super(sourceTab);
    }
    
    @Override
    protected void initializeResources() {
        this.mLayoutResourceId = R.layout.fragment_credit_accounts;
        this.mRecyclerViewId = R.id.accounts_credit_recycler;
        this.mAccountType = AccountTypeFilter.CREDIT;
    }
} 