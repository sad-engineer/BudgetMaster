package com.sadengineer.budgetmaster.accounts;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseCardsActivity;
import com.sadengineer.budgetmaster.backend.entity.Account;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity для отображения списка счетов
 */
public class AccountsActivity extends BaseCardsActivity<Account> {
    
    private static final String TAG = "AccountsActivity";
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
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
        // Устанавливаем заголовок для режима создания
        setToolbarTitle(
            R.string.toolbar_title_accounts, 
            R.dimen.toolbar_text_accounts);

        // Общая привязка кнопок и placeholder для индикатора
        // Используем резерв в тулбаре как placeholder индикатора
        setupCommonCardsUi(0, R.id.add_button_bottom, R.id.delete_button_bottom, R.id.toolbar_reserve);
        // Shared ViewModel для управления режимом выбора и мягким удалением
        viewModel = new ViewModelProvider(this).get(AccountsSharedViewModel.class);
        // Привязываем ViewModel к базовой логике кнопок/индикатора (важно: после setupCommonCardsUi)
        bindSelectionViewModel(viewModel);

        // Настраиваем TabLayout и ViewPager2
        setupViewPager();
    }

    /**
     * Обработчик клика «Добавить».
     */
    @Override
    protected void onAddClicked() {
        // Запускаем окно создания счета (режим выбора обрабатывается базовым классом)
        int source_tab = viewPager != null ? viewPager.getCurrentItem() : 0;
        String[] params = {"source_tab", String.valueOf(source_tab)};
        goTo(AccountsEditActivity.class, false, params);
    }

    /**
     * Обработчик клика «Удалить/Режим выбора».
     */
    @Override
    protected void onDeleteClicked() {
        // Поведение переключения режима выбора обрабатывается базовым классом через ViewModel
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
}
