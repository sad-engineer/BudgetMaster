package com.sadengineer.budgetmaster.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.entity.Account;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.ArrayList;


/**
 * Activity для отображения списка счетов
 */
public class AccountsActivity extends BaseNavigationActivity {
    
    private static final String TAG = "AccountsActivity";
    
    private ImageButton addAccountButton;
    private ImageButton deleteAccountButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View emptySpace;
    private ProgressBar loadingIndicator;
    private boolean isSelectionMode = false;
    private AccountsSharedViewModel viewModel;

    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Shared ViewModel для управления режимом выбора и мягким удалением
        viewModel = new ViewModelProvider(this).get(AccountsSharedViewModel.class);

        // Настраиваем TabLayout и ViewPager2
        setupViewPager();
        
        // Обработчики кнопок счетов
        setupButtons();
        
        // Инициализируем пустое место
        emptySpace = findViewById(R.id.empty_space);

        // Наблюдаем за режимом выбора, чтобы обновлять иконки
        viewModel.getSelectionMode().observe(this, enabled -> {
            isSelectionMode = Boolean.TRUE.equals(enabled);
            if (isSelectionMode) {
                addAccountButton.setImageResource(R.drawable.ic_save);
                deleteAccountButton.setImageResource(R.drawable.ic_back);
            } else {
                addAccountButton.setImageResource(R.drawable.ic_add);
                deleteAccountButton.setImageResource(R.drawable.ic_delete);
            }
        });

        // Логируем результат мягкого удаления
        viewModel.getSoftDeletionDone().observe(this, count -> {
            if (count != null) {
                Log.d(TAG, "Удалено счетов: " + count);
            }
        });
        
        // Наблюдаем за состоянием удаления для показа индикатора
        viewModel.getDeleting().observe(this, isDeleting -> {
            if (isDeleting != null) {
                if (isDeleting) {
                    showLoadingIndicator();
                } else {
                    hideLoadingIndicator();
                }
            }
        });
    }
    
    /**
     * Настраивает ViewPager2 и TabLayout
     */
    private void setupViewPager() {
        tabLayout = findViewById(R.id.accounts_tab_layout);
        viewPager = findViewById(R.id.accounts_view_pager);

        AccountsPagerAdapter adapter = new AccountsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Получаем индекс вкладки из Intent (по умолчанию 0)
        int tabIndex = getIntent().getIntExtra("selected_tab", 0);
        viewPager.setCurrentItem(tabIndex, false);
        Log.d(TAG, "Открыта вкладка: " + tabIndex);
        
        // Массив названий вкладок
        String[] tabTitles = {
            getString(R.string.tab_current),
            getString(R.string.tab_savings),
            getString(R.string.tab_transfers)
        };

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                if (position >= 0 && position < tabTitles.length) {
                    tab.setText(tabTitles[position]);
                }
            }
        ).attach();
    }
    
    /**
     * Настраивает кнопки
     */
    private void setupButtons() {
        addAccountButton = findViewById(R.id.add_account_button_bottom);
        deleteAccountButton = findViewById(R.id.delete_account_button_bottom);

        /**
         * Обработчик нажатия на кнопку добавления счета
         */
        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - мягко удаляем выбранные счета через ViewModel
                    viewModel.deleteSelectedAccountsSoft();
                } else {
                    // Запускаем окно создания счета
                    Intent intent = new Intent(AccountsActivity.this, AccountsEditActivity.class);
                    // Передаем текущую вкладку
                    intent.putExtra("source_tab", viewPager.getCurrentItem());
                    startActivity(intent);
                }
            }
        });

        /**
         * Обработчик нажатия на кнопку удаления счетов
         */
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - отменяем выбор через ViewModel
                    viewModel.cancelSelectionMode();
                } else {
                    // Включаем режим выбора
                    viewModel.enableSelectionMode();
                }
            }
        });
    }    
    
    /**
     * Показывает индикатор загрузки, заменяя пустое место
     */
    private void showLoadingIndicator() {
        if (loadingIndicator == null) {
            // Создаем индикатор загрузки
            LayoutInflater inflater = LayoutInflater.from(this);
            loadingIndicator = (ProgressBar) inflater.inflate(R.layout.loading_indicator, null);
            
            // Заменяем пустое место на индикатор
            ViewGroup parent = (ViewGroup) emptySpace.getParent();
            int index = parent.indexOfChild(emptySpace);
            parent.removeView(emptySpace);
            parent.addView(loadingIndicator, index);
        }
    }
    
    /**
     * Скрывает индикатор загрузки, возвращая пустое место
     */
    private void hideLoadingIndicator() {
        if (loadingIndicator != null) {
            // Возвращаем пустое место
            ViewGroup parent = (ViewGroup) loadingIndicator.getParent();
            int index = parent.indexOfChild(loadingIndicator);
            parent.removeView(loadingIndicator);
            parent.addView(emptySpace, index);
            loadingIndicator = null;
        }
    }
    
    /**
     * Обработчик нажатия на кнопку "Назад"
     */
    @Override
    public void onBackPressed() {
        if (isSelectionMode) {
            viewModel.cancelSelectionMode();
        } else {
            super.onBackPressed();
        }
    }
}