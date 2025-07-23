package com.sadengineer.budgetmaster.accounts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sadengineer.budgetmaster.R;
import java.util.List;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountViewHolder> {
    private final List<AccountItem> items;

    public AccountsAdapter(List<AccountItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountItem item = items.get(position);
        holder.name.setText(item.name);
        holder.sum.setText(item.sum);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView name, sum;
        AccountViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.account_name);
            sum = itemView.findViewById(R.id.account_sum);
        }
    }

    public static class AccountItem {
        public final String name;
        public final String sum;
        public AccountItem(String name, String sum) {
            this.name = name;
            this.sum = sum;
        }
    }
} 