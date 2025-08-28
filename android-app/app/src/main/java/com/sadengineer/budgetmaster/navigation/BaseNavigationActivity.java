package com.sadengineer.budgetmaster.navigation;

import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import com.sadengineer.budgetmaster.R;

import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationTree;
import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationNode;
import com.sadengineer.budgetmaster.navigation.MenuBuilder;

/**
 * Базовый класс для всех Activity с навигацией
 * Содержит методы для инициализации навигации, обработки свайпов и переходов между экранами
 */
public abstract class BaseNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "BaseNavigationActivity";
    
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;


    /**
     * Инициализация навигационного меню
     * Вызывается в onCreate() дочерних классов
     */
    protected void initializeNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        // Инициализируем навигационное дерево и меню
        MenuBuilder.initialize(this);
        
        // Логируем доступные пункты меню
        MenuBuilder.logAvailableMenuItems();
        
        Log.d(TAG, "Навигация инициализирована");
    }
        
    /**
     * Настройка кнопки меню для открытия бокового меню
     * @param menuButtonId ID кнопки меню в toolbar
     */
    protected void setupMenuButton(int menuButtonId) {
        ImageButton menuButton = findViewById(menuButtonId);
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                    Log.d(TAG, "Нажата кнопка меню");
                }
            });
        }
    }

    /**
     * Настройка кнопки "назад" для возврата на главный экран
     * @param backButtonId ID кнопки назад в toolbar
     */
    protected void setupBackButton(int backButtonId) {
        ImageButton backButton = findViewById(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Получаем главный экран из NavigationTree
                NavigationNode mainNode = NavigationTree.getRootNode();
                if (mainNode != null) {
                    Intent intent = NavigationTree.createIntent(this, mainNode, 0);
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        Log.d(TAG, "Нажата кнопка назад");
                    }
                }
            });
        }
    }

    /**
     * Комплексная инициализация стандартного тулбара по фиксированным ID
     * back_button, menu_button, toolbar_title, position_change_button.
     * Любой из элементов может отсутствовать в конкретном layout.
     */
    protected void setupStandardToolbar() {
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        // Кнопка смены позиции — просто логируем клик по умолчанию
        ImageButton positionButton = findViewById(R.id.position_change_button);
        if (positionButton != null) {
            positionButton.setOnClickListener(v -> Log.d(TAG, "Нажата кнопка смены позиции"));
        }
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
     * Обработка выбора пунктов меню
     * @param item - выбранный пункт меню
     * @return true, если обработка выполнена успешно
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Используем NavigationTree для обработки выбора пунктов меню
        boolean handled = handleNavigationItemSelection(id);

        // Закрываем боковое меню
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return handled;
    }
    
    /**
     * Обрабатывает выбор пункта меню через NavigationTree
     */
    private boolean handleNavigationItemSelection(int menuItemId) {
        // Автоматически получаем класс Activity по ID пункта меню
        Class<?> targetActivityClass = MenuBuilder.getActivityForMenuId(menuItemId);
        
        if (targetActivityClass != null) {
            // Получаем узел навигации
            NavigationNode targetNode = NavigationTree.getNode(targetActivityClass);
            
            if (targetNode != null) {
                // Создаем Intent для перехода
                Intent intent = NavigationTree.createIntent(this, targetNode, 0);
                if (intent != null) {
                    startActivity(intent);
                    Log.d(TAG, "Переход к " + targetNode.name);
                    return true;
                }
            } else {
                Log.w(TAG, "Узел навигации не найден для " + targetActivityClass.getSimpleName());
            }
        } else {
            Log.w(TAG, "Activity не найден для пункта меню с ID: " + menuItemId);
        }
        
        return false;
    }
    
    /**
     * Обработка нажатия кнопки "назад"
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Log.d(TAG, "Закрыто боковое меню");
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Навигация вверх по дереву
     */
    public void goUp() { 
        NavigationNode node = NavigationTree.navigateUp(this.getClass());
        if (node != null) {
            Intent intent = NavigationTree.createIntent(this, node, 0);
            if (intent != null) startActivity(intent);
        }
    }
    
    /**
     * Навигация вниз по дереву
     */
    public void goDown() { 
        NavigationNode node = NavigationTree.navigateDown(this.getClass());
        if (node != null) {
            Intent intent = NavigationTree.createIntent(this, node, 0);
            if (intent != null) startActivity(intent);
        }
    }
    
    /**
     * Навигация влево (переход к предыдущей вкладке)
     */
    public void goLeft() { 
        NavigationNode node = NavigationTree.navigateLeft(this.getClass(), getCurrentTabIndex());
        if (node != null) {
            int previousTab = NavigationTree.getPreviousTabIndex(this.getClass(), getCurrentTabIndex());
            Intent intent = NavigationTree.createIntent(this, node, previousTab);
            if (intent != null) startActivity(intent);
        }
    }
    
    /**
     * Навигация вправо (переход к следующей вкладке)
     */
    public void goRight() { 
        NavigationNode node = NavigationTree.navigateRight(this.getClass(), getCurrentTabIndex());
        if (node != null) {
            int nextTab = NavigationTree.getNextTabIndex(this.getClass(), getCurrentTabIndex());
            Intent intent = NavigationTree.createIntent(this, node, nextTab);
            if (intent != null) startActivity(intent);
        }
    }
    
    /**
     * Переход к конкретной Activity
     */
    public void goTo(Class<?> targetActivity) { 
        NavigationNode node = NavigationTree.navigateToActivity(this.getClass(), targetActivity);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(this, node, 0);
            if (intent != null) startActivity(intent);
        }
    }

}


// // В любой Activity:
// public class MyActivity extends BaseNavigationActivity {
    
//     @Override
//     protected void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         initializeNavigation(); // ← Автоматически инициализирует NavigationTree
        
//         // Простые методы навигации:
//         goUp();        // переход вверх
//         goDown();      // переход вниз  
//         goLeft();      // переход к предыдущей вкладке
//         goRight();     // переход к следующей вкладке
//         goTo(MainActivity.class); // переход к конкретной Activity
//     }
// }