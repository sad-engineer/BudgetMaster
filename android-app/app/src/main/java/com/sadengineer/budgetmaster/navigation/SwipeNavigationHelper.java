package com.sadengineer.budgetmaster.navigation;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.sadengineer.budgetmaster.MainActivity;
import com.sadengineer.budgetmaster.accounts.AccountsActivity;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.sadengineer.budgetmaster.budget.BudgetActivity;
import com.sadengineer.budgetmaster.currencies.CurrenciesActivity;
import com.sadengineer.budgetmaster.settings.SettingsActivity;
import com.sadengineer.budgetmaster.instructions.InstructionsActivity;
import com.sadengineer.budgetmaster.statistics.StatisticsActivity;
import com.sadengineer.budgetmaster.categories.IncomeCategoriesActivity;
import com.sadengineer.budgetmaster.categories.ExpenseCategoriesActivity;
import com.sadengineer.budgetmaster.import_export.ImportDataActivity;
import com.sadengineer.budgetmaster.import_export.ExportDataActivity;
import com.sadengineer.budgetmaster.BackendTestActivity;
import com.sadengineer.budgetmaster.VersionActivity;
import com.sadengineer.budgetmaster.AuthorsActivity;


/**
 * Помощник для обработки свайпов навигации
 */
public class SwipeNavigationHelper {
    private static final String TAG = "SwipeNavigationHelper";
    
    // Порог для определения свайпа
    private static final int SWIPE_THRESHOLD = 100;
    // Порог скорости для определения свайпа
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    
    private final BaseNavigationActivity activity;
    private final GestureDetector gestureDetector;
    private boolean isEnabled = true;
    
    // Порядок экранов для навигации вниз по дереву меню
    private final Class<?>[] navigationOrder = {
        MainActivity.class,
        InstructionsActivity.class,
        CurrenciesActivity.class,
        AccountsActivity.class,
        IncomeActivity.class,
        ExpenseActivity.class,
        BudgetActivity.class,
        IncomeCategoriesActivity.class,
        ExpenseCategoriesActivity.class,
        ImportDataActivity.class,
        ExportDataActivity.class,
        StatisticsActivity.class,
        BackendTestActivity.class,
        SettingsActivity.class,
        VersionActivity.class,
        AuthorsActivity.class
    };
    
    public SwipeNavigationHelper(BaseNavigationActivity activity) {
        this.activity = activity;
        this.gestureDetector = new GestureDetector(activity, new GestureListener());
    }
    
    /**
     * Включает/выключает обработку свайпов
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        Log.d(TAG, "🔄 Свайпы " + (enabled ? "включены" : "отключены"));
    }
    
    /**
     * Проверяет, включены ли свайпы
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Обрабатывает касание экрана
     */
    public boolean onTouchEvent(MotionEvent event) {
        // Если свайпы отключены, не обрабатываем
        if (!isEnabled) {
            return false;
        }
        
        // Проверяем, открыто ли меню
        if (activity.drawerLayout != null && activity.drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            return false;
        }
        
        return gestureDetector.onTouchEvent(event);
    }
    
    /**
     * Слушатель жестов для обработки свайпов
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        
        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, 
                              float velocityX, float velocityY) {
            // Дополнительная проверка на случай, если меню открылось во время жеста
            if (!isEnabled || (activity.drawerLayout != null && activity.drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START))) {
                return false;
            }
            
            try {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                
                // Проверяем, что свайп вертикальный (больше вертикального движения)
                if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && 
                        Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        
                        if (diffY < 0) {
                            // Свайп вверх - переходим на следующий экран
                            onSwipeUp();
                            return true;
                        } else {
                            // Свайп вниз - переходим на предыдущий экран
                            onSwipeDown();
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка обработки свайпа: " + e.getMessage(), e);
            }
            
            return false;
        }
    }
    
    /**
     * Обработка свайпа вверх - переход на следующий экран
     */
    private void onSwipeUp() {
        Log.d(TAG, "👆 Свайп вверх - переходим на следующий экран");
        
        // Находим текущий экран в порядке навигации
        Class<?> currentActivity = activity.getClass();
        int currentIndex = -1;
        
        for (int i = 0; i < navigationOrder.length; i++) {
            if (navigationOrder[i] == currentActivity) {
                currentIndex = i;
                break;
            }
        }
        
        // Если текущий экран найден и есть следующий экран
        if (currentIndex >= 0 && currentIndex < navigationOrder.length - 1) {
            Class<?> nextActivity = navigationOrder[currentIndex + 1];
            navigateToActivity(nextActivity);
        } else {
            Log.d(TAG, "📱 Достигнут конец навигации или экран не найден в списке");
        }
    }
    
    /**
     * Обработка свайпа вниз - переход на предыдущий экран
     */
    private void onSwipeDown() {
        Log.d(TAG, "👇 Свайп вниз - переходим на предыдущий экран");
        
        // Находим текущий экран в порядке навигации
        Class<?> currentActivity = activity.getClass();
        int currentIndex = -1;
        
        for (int i = 0; i < navigationOrder.length; i++) {
            if (navigationOrder[i] == currentActivity) {
                currentIndex = i;
                break;
            }
        }
        
        // Если текущий экран найден и есть предыдущий экран
        if (currentIndex > 0) {
            Class<?> previousActivity = navigationOrder[currentIndex - 1];
            navigateToActivity(previousActivity);
        } else {
            Log.d(TAG, "📱 Достигнут начало навигации или экран не найден в списке");
        }
    }
    
    /**
     * Переход на указанный экран
     */
    private void navigateToActivity(Class<?> targetActivity) {
        try {
            Log.d(TAG, "🔄 Переход на экран: " + targetActivity.getSimpleName());
            Intent intent = new Intent(activity, targetActivity);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка перехода на экран " + targetActivity.getSimpleName() + ": " + e.getMessage(), e);
        }
    }
} 