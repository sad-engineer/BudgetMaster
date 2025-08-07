package com.sadengineer.budgetmaster.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.service.AccountService;

import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.ArrayList;
import androidx.fragment.app.Fragment;

/**
 * Activity для отображения списка счетов
 */
public class AccountsActivity extends BaseNavigationActivity {
    
    private static final String TAG = "AccountsActivity";
    
    private ImageButton addAccountButton;
    private ImageButton deleteAccountButton;
    private AccountService accountService;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private boolean isSelectionMode = false;

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

        // Инициализация AccountService
        accountService = new AccountService(this, "default_user");

        // Настраиваем TabLayout и ViewPager2
        setupViewPager();
        
        // Обработчики кнопок счетов
        setupButtons();
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
        Log.d(TAG, "Устанавливаем вкладку: " + tabIndex);

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                switch (position) {
                    case 0: tab.setText(getString(R.string.tab_current)); break;
                    case 1: tab.setText(getString(R.string.tab_savings)); break;
                    case 2: tab.setText(getString(R.string.tab_transfers)); break;
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
                    // В режиме выбора - удаляем выбранные счета
                    deleteSelectedAccounts();
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
                    // В режиме выбора - отменяем выбор
                    cancelSelectionMode();
                } else {
                    // Включаем режим выбора
                    enableSelectionMode();
                }
            }
        });
    }
    
    /**
     * Включает режим выбора счетов
     */
    private void enableSelectionMode() {
        isSelectionMode = true;
        
        // Меняем иконки кнопок
        addAccountButton.setImageResource(R.drawable.ic_save);
        deleteAccountButton.setImageResource(R.drawable.ic_back);
        
        // Уведомляем все фрагменты о включении режима выбора
        notifyFragmentsSelectionMode(true);
        
        Log.d(TAG, "✅ Режим выбора счетов включен");
    }
    
    /**
     * Отменяет режим выбора
     */
    private void cancelSelectionMode() {
        isSelectionMode = false;
        
        // Возвращаем иконки кнопок
        addAccountButton.setImageResource(R.drawable.ic_add);
        deleteAccountButton.setImageResource(R.drawable.ic_delete);
        
        // Уведомляем все фрагменты об отмене режима выбора
        notifyFragmentsSelectionMode(false);
        
        Log.d(TAG, "❌ Режим выбора счетов отменен");
    }
    
    /**
     * Удаляет выбранные счета
     */
    private void deleteSelectedAccounts() {
        // Получаем выбранные счета из текущего фрагмента
        List<Account> selectedAccounts = getSelectedAccountsFromCurrentFragment();
        
        Log.d(TAG, "🗑️ Удаляем выбранные счета: " + selectedAccounts.size());
        
        // Удаляем счета из базы данных
        for (Account account : selectedAccounts) {
            try {
                accountService.softDelete(account);
                Log.d(TAG, "✅ Удален счет: " + account.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка удаления счета " + account.getTitle() + ": " + e.getMessage(), e);
            }
        }
        
        // Отменяем режим выбора
        cancelSelectionMode();
        Log.d(TAG, "✅ Удалено счетов: " + selectedAccounts.size());
    }
    
    /**
     * Уведомляет фрагменты о изменении режима выбора
     */
    private void notifyFragmentsSelectionMode(boolean enabled) {
        // Получаем текущий фрагмент
        int currentPosition = viewPager.getCurrentItem();
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + currentPosition);
        
        if (currentFragment instanceof CurrentAccountsFragment) {
            ((CurrentAccountsFragment) currentFragment).setSelectionMode(enabled);
        } else if (currentFragment instanceof SavingsAccountsFragment) {
            ((SavingsAccountsFragment) currentFragment).setSelectionMode(enabled);
        } else if (currentFragment instanceof TransfersAccountsFragment) {
            ((TransfersAccountsFragment) currentFragment).setSelectionMode(enabled);
        }
    }
    
    /**
     * Получает выбранные счета из текущего фрагмента
     */
    private List<Account> getSelectedAccountsFromCurrentFragment() {
        int currentPosition = viewPager.getCurrentItem();
        Fragment currentFragment = getSupportFragmentManager()
            .findFragmentByTag("f" + currentPosition);
        
        if (currentFragment instanceof CurrentAccountsFragment) {
            return ((CurrentAccountsFragment) currentFragment).getSelectedAccounts();
        } else if (currentFragment instanceof SavingsAccountsFragment) {
            return ((SavingsAccountsFragment) currentFragment).getSelectedAccounts();
        } else if (currentFragment instanceof TransfersAccountsFragment) {
            return ((TransfersAccountsFragment) currentFragment).getSelectedAccounts();
        }
        
        return new ArrayList<>();
    }
}