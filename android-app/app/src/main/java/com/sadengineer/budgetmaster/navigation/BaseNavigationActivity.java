package com.sadengineer.budgetmaster.navigation;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import com.sadengineer.budgetmaster.R;

import com.sadengineer.budgetmaster.navigation.MenuBuilder;

/**
 * Базовый класс для всех Activity с навигацией
 * Использует отдельные менеджеры для управления тулбаром, меню и навигацией
 */
public abstract class BaseNavigationActivity extends AppCompatActivity {

    private static final String TAG = "BaseNavigationActivity";
    
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    
    protected ToolbarManager toolbarManager;
    protected MenuHandler menuHandler;
    protected NavigationController navigationController;


    /**
     * Инициализация навигационного меню
     * Вызывается в onCreate() дочерних классов
     */
    protected void initializeNavigation(Boolean isMainScreen) {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        // Инициализируем навигационное дерево и меню
        MenuBuilder.initialize(this);
        
        // Создаем менеджеры
        navigationController = new NavigationController(this, this.getClass());
        toolbarManager = new ToolbarManager(this, drawerLayout);
        menuHandler = new MenuHandler(this, drawerLayout);
        
        // Устанавливаем связи между менеджерами
        toolbarManager.setNavigationController(navigationController);
        if (isMainScreen != null && isMainScreen) {
            toolbarManager.setupMainToolbar();
        } else {
            toolbarManager.setupStandardToolbar();
        }

        // Устанавливаем обработчик меню
        navigationView.setNavigationItemSelectedListener(menuHandler);
        
        // Логируем доступные пункты меню
        MenuBuilder.logAvailableMenuItems();
        
        Log.d(TAG, "Навигация инициализирована");
    }
    
    /**
     * Инициализация навигационного меню без указания флага главного экрана
     * (для совместимости с дочерними классами)
     */
    protected void initializeNavigation() {
        initializeNavigation(null);
    }
    
    /**
     * Получает индекс текущей вкладки
     */
    protected int getCurrentTabIndex() {
        // Дочерние классы должны переопределить этот метод
        return 0;
    }
    
    /**
     * Переключение на вкладку (должно быть переопределено в дочерних классах)
     */
    protected void switchToTab(int tabIndex) {
        Log.d(TAG, "Переключение на вкладку " + tabIndex + " (базовая реализация)");
        // Дочерние классы должны переопределить этот метод для работы с ViewPager
    }
        
    /**
     * Навигация вверх по дереву
     */
    public void goUp(boolean clearTop, String name, Integer value) { 
        if (navigationController != null) {
            navigationController.goUp(clearTop, name, value);
        }
    }

    /**
     * Навигация вверх по дереву без параметров
     */
    public void goUp() { 
        if (navigationController != null) {
            navigationController.goUp(false, null, null);
        }
    }
    
    /**
     * Навигация вниз по дереву
     */
    public void goDown(boolean clearTop, String name, Integer value) { 
        if (navigationController != null) {
            navigationController.goDown(clearTop, name, value);
        }
    }

    /**
     * Навигация вниз по дереву без параметров
     */
    public void goDown() { 
        if (navigationController != null) {
            navigationController.goDown(false, null, null);
        }
    }
    
    /**
     * Навигация влево (переход к предыдущей вкладке)
     */
    public void goLeft(boolean clearTop, String name, Integer value) { 
        if (navigationController != null) {
            navigationController.goLeft(getCurrentTabIndex(), clearTop, name, value);
        }
    }

    /**
     * Навигация влево (переход к предыдущей вкладке) без параметров
     */
    public void goLeft() { 
        if (navigationController != null) {
            navigationController.goLeft(getCurrentTabIndex(), false, null, null);
        }
    }

    /**
     * Навигация вправо (переход к следующей вкладке)
     */
    public void goRight(boolean clearTop, String name, Integer value) { 
        if (navigationController != null) {
            navigationController.goRight(getCurrentTabIndex(), clearTop, name, value);
        }
    }
    
    /**
     * Навигация вправо (переход к следующей вкладке) без параметров
     */
    public void goRight() { 
        if (navigationController != null) {
            navigationController.goRight(getCurrentTabIndex(), false, null, null);
        }
    }

    /**
     * Переход к конкретной Activity
     */
    public void goTo(Class<?> targetActivity, boolean clearTop, String name, Integer value) { 
        if (navigationController != null) {
            navigationController.goTo(targetActivity, clearTop, name, value);
        }
    }

    /**
     * Переход к конкретной Activity без параметров
     */
    public void goTo(Class<?> targetActivity) { 
        if (navigationController != null) {
            navigationController.goTo(targetActivity, false, null, null);
        }
    }

    /**
     * Переход к конкретной Activity с указанием вкладки
     */
    public void goTo(Class<?> targetActivity, int tabIndex, boolean clearTop, String name, Integer value) { 
        if (navigationController != null) {
            navigationController.goTo(targetActivity, tabIndex, clearTop, name, value);
        }
    }

    /**
     * Переход к конкретной Activity с указанием вкладки без параметров
     */
    public void goTo(Class<?> targetActivity, int tabIndex) { 
        if (navigationController != null) {
            navigationController.goTo(targetActivity, tabIndex, false, null, null);
        }
    }

    /**
     * Переход к главному экрану
     */
    public void goToRoot() { 
        if (navigationController != null) {
            navigationController.goToRoot();
        }
    }

    /**
     * Установка заголовка тулбара
     */
    protected void setToolbarTitle(int titleResId, int textSizeResId) {
        toolbarManager.setToolbarTitle(titleResId, textSizeResId);
    }
    
    /**
     * Установка заголовка тулбара
     */
    protected void setToolbarTitle(String titleText, float textSize) {
        toolbarManager.setToolbarTitle(titleText, textSize);
    }
}

// // В любой Activity:
// public class MyActivity extends BaseNavigationActivity {
    
//     @Override
//     protected void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         setContentView(R.layout.activity_my);
        
//         initializeNavigation(true/false); // ← Автоматически инициализирует NavigationTree
        
//         // Простые методы навигации:
//         goUp();        // переход вверх
//         goUp(true, "name", 1); // переход вверх с параметрами
//         goDown();      // переход вниз  
//         goDown(true, "name", 1); // переход вниз с параметрами
//         goLeft();      // переход к предыдущей вкладке
//         goLeft(true, "name", 1); // переход к предыдущей вкладке с параметрами
//         goRight();     // переход к следующей вкладке
//         goRight(true, "name", 1); // переход к следующей вкладке с параметрами
//         goTo(MainActivity.class); // переход к конкретной Activity
//         goTo(MainActivity.class, true, "name", 1); // переход к конкретной Activity с параметрами
//         goTo(AccountsActivity.class, 1); // переход к Activity с вкладкой 1
//         goTo(AccountsActivity.class, 1, true, "name", 1); // переход к Activity с вкладкой 1 с параметрами
//         goToRoot(); // переход к главному экрану
//         setToolbarTitle(R.string.app_name, R.dimen.toolbar_title_size); // установка заголовка тулбара
//         setToolbarTitle("My Activity", 16); // установка заголовка тулбара
//     }
    
//     // Если есть вкладки, переопределяем:
//     @Override
//     protected int getCurrentTabIndex() {
//         return viewPager.getCurrentItem();
//     }
    
//     @Override
//     protected void switchToTab(int tabIndex) {
//         viewPager.setCurrentItem(tabIndex);
//     }
// }