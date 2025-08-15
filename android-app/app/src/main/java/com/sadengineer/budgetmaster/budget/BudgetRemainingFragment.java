package com.sadengineer.budgetmaster.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sadengineer.budgetmaster.R;

/**
 * Фрагмент для отображения остатков бюджета
 */
public class BudgetRemainingFragment extends Fragment {

    private RecyclerView recyclerView;

    /**
     * Создает представление фрагмента
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_remaining, container, false);
        
        recyclerView = view.findViewById(R.id.budget_remaining_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // TODO: Установить адаптер для RecyclerView
        
        return view;
    }
} 