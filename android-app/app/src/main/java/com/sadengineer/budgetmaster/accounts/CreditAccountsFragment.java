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
        this.layoutResourceId = R.layout.fragment_transfers_accounts;
        this.recyclerViewId = R.id.accounts_transfers_recycler;
        this.accountType = AccountTypeFilter.CREDIT;
    }
} 