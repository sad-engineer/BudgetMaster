package com.example.budgetmaster.accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetmaster.R;
import java.util.ArrayList;
import java.util.List;

public class CurrentAccountsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_accounts, container, false);
        RecyclerView recycler = view.findViewById(R.id.accounts_recycler);

        List<AccountsAdapter.AccountItem> items = new ArrayList<>();
        items.add(new AccountsAdapter.AccountItem("Итого", "637 777.57 RUB"));
        items.add(new AccountsAdapter.AccountItem("Наличные", "499 741.38 RUB"));
        items.add(new AccountsAdapter.AccountItem("Карта", "138 036.19 RUB"));

        AccountsAdapter adapter = new AccountsAdapter(items);
        recycler.setAdapter(adapter);

        return view;
    }
} 