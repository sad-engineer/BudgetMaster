package com.sadengineer.budgetmaster.accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.sadengineer.budgetmaster.R;
import java.util.ArrayList;
import java.util.List;

public class TransfersAccountsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfers_accounts, container, false);
        RecyclerView recycler = view.findViewById(R.id.accounts_transfers_recycler);

        List<AccountsAdapter.AccountItem> items = new ArrayList<>();
        items.add(new AccountsAdapter.AccountItem("июль 2025", ""));

        AccountsAdapter adapter = new AccountsAdapter(items);
        recycler.setAdapter(adapter);

        return view;
    }
} 