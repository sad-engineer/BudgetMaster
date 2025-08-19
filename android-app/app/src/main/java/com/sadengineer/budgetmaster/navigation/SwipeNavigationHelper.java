package com.sadengineer.budgetmaster.navigation;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

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
    // Количество карточек, при котором включается двойная логика свайпа
    private static final int CARDS_THRESHOLD = 15;
    // Задержка между свайпами для навигации (в миллисекундах)
    private static final long NAVIGATION_SWIPE_DELAY = 500;
    
    private final BaseNavigationActivity activity;
    private final GestureDetector gestureDetector;
    private boolean isEnabled = true;
    
    // Счетчики для двойной логики свайпа
    private int swipeUpCount = 0;
    private long lastSwipeUpTime = 0;
    private boolean shouldNavigateOnNextSwipe = false;
    
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
        SettingsActivity.class,
        VersionActivity.class,
        AuthorsActivity.class
    };
    
    /**
     * Конструктор класса
     * @param activity текущая активность
     */
    public SwipeNavigationHelper(BaseNavigationActivity activity) {
        this.activity = activity;
        this.gestureDetector = new GestureDetector(activity, new GestureListener());
    }
    
    /**
     * Включает/выключает обработку свайпов
     * @param enabled true - включить обработку свайпов, false - выключить
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        Log.d(TAG, "Свайпы " + (enabled ? "включены" : "отключены"));
    }
    
    /**
     * Проверяет, включены ли свайпы
     * @return true - свайпы включены, false - свайпы отключены
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Сбрасывает счетчик свайпов (вызывается при изменении содержимого списка)
     * @return true - свайпы включены, false - свайпы отключены
     */
    public void resetSwipeCount() {
        swipeUpCount = 0;
        shouldNavigateOnNextSwipe = false;
        lastSwipeUpTime = 0;
        Log.d(TAG, "Счетчик свайпов сброшен");
    }
    
    /**
     * Проверяет, нужно ли использовать двойную логику свайпа
     */
    private boolean shouldUseDoubleSwipeLogic() {
        // Ищем RecyclerView в текущей Activity
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) return false;
        
        // Ищем RecyclerView по ID (попробуем несколько вариантов)
        RecyclerView recyclerView = findRecyclerView(rootView);
        if (recyclerView == null) {
            Log.d(TAG, "RecyclerView не найден");
            return false;
        }
        
        // Проверяем количество элементов
        int itemCount = recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : 0;
        Log.d(TAG, "Найдено элементов в списке: " + itemCount);
        
        // Проверяем, действительно ли список длинный и требует прокрутки
        if (itemCount >= CARDS_THRESHOLD) {
            // Дополнительная проверка - может ли список прокручиваться
            boolean canScroll = recyclerView.canScrollVertically(-1) || recyclerView.canScrollVertically(1);
            Log.d(TAG, "Список может прокручиваться: " + canScroll);
            return canScroll;
        }
        
        return false;
    }
    
    /**
     * Ищет RecyclerView в иерархии View
     * @param view текущий View
     * @return RecyclerView или null, если RecyclerView не найден
     */
    private RecyclerView findRecyclerView(View view) {
        if (view instanceof RecyclerView) {
            return (RecyclerView) view;
        }
        
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                RecyclerView found = findRecyclerView(viewGroup.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Проверяет, можно ли прокрутить список вверх
     * @param recyclerView RecyclerView
     * @return true - можно прокрутить список вверх, false - нельзя прокрутить список вверх
     */
    private boolean canScrollUp(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        
        // Проверяем, можно ли прокрутить вверх
        boolean canScrollUp = recyclerView.canScrollVertically(-1);
        
        // Дополнительная проверка - если список не может прокручиваться вверх,
        // но может прокручиваться вниз, значит мы в начале списка
        boolean canScrollDown = recyclerView.canScrollVertically(1);
        
        Log.d(TAG, "canScrollUp: " + canScrollUp + ", canScrollDown: " + canScrollDown);
        
        // Если можем прокрутить вверх - значит не в начале списка
        return canScrollUp;
    }
    
    /**
     * Обрабатывает касание экрана
     * @param event MotionEvent
     * @return true - обработка свайпа, false - нет
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
     * @param e1 MotionEvent
     * @param e2 MotionEvent
     * @param velocityX скорость свайпа по X
     * @param velocityY скорость свайпа по Y
     * @return true - обработка свайпа, false - нет
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        
        @Override
        public boolean onFling(
            @NonNull MotionEvent e1, 
            @NonNull MotionEvent e2, 
            float velocityX, 
            float velocityY) {
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
                            // Свайп вверх
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
     * Обработка свайпа вверх с двойной логикой
    */
    private void onSwipeUp() {
        Log.d(TAG, "Свайп вверх - обрабатываем с двойной логикой");
        
        // Проверяем, нужно ли использовать двойную логику
        boolean useDoubleLogic = shouldUseDoubleSwipeLogic();
        Log.d(TAG, "Использовать двойную логику: " + useDoubleLogic);
        
        if (useDoubleLogic) {
            handleDoubleSwipeLogic();
        } else {
            // Обычная логика - сразу переходим на следующий экран
            Log.d(TAG, "Используем обычную навигацию");
            navigateToNextScreen();
        }
    }
    
    /**
     * Обрабатывает двойную логику свайпа
     */
    private void handleDoubleSwipeLogic() {
        long currentTime = System.currentTimeMillis();
        
        // Ищем RecyclerView
        View rootView = activity.findViewById(android.R.id.content);
        RecyclerView recyclerView = findRecyclerView(rootView);
        
        if (recyclerView == null) {
            Log.d(TAG, "RecyclerView не найден, используем обычную навигацию");
            navigateToNextScreen();
            return;
        }
        
        // Проверяем, можно ли прокрутить список вверх
        boolean canScrollUp = canScrollUp(recyclerView);
        Log.d(TAG, "Можно прокрутить вверх: " + canScrollUp);
        
        if (canScrollUp) {
            // Можно прокрутить - первый свайп прокручивает список
            Log.d(TAG, "Первый свайп - прокручиваем список");
            
            // Прокручиваем на определенное расстояние
            int scrollDistance = -400; // Увеличиваем расстояние прокрутки
            recyclerView.smoothScrollBy(0, scrollDistance);
            
            // Устанавливаем флаг, что следующий свайп должен переходить на экран
            shouldNavigateOnNextSwipe = true;
            lastSwipeUpTime = currentTime;
            
            Log.d(TAG, "Список прокручен, следующий свайп перейдет на экран");
            
        } else if (shouldNavigateOnNextSwipe && 
                   (currentTime - lastSwipeUpTime) < NAVIGATION_SWIPE_DELAY) {
            // Нельзя прокрутить и флаг установлен - переходим на экран
            Log.d(TAG, "Второй свайп - переходим на следующий экран");
            navigateToNextScreen();
            
            // Сбрасываем флаги
            shouldNavigateOnNextSwipe = false;
            lastSwipeUpTime = 0;
            
        } else {
            // Нельзя прокрутить и флаг не установлен - переходим на экран
            Log.d(TAG, "Нельзя прокрутить список - переходим на экран");
            navigateToNextScreen();
        }
    }
    
    /**
     * Переход на следующий экран
     */
    private void navigateToNextScreen() {
        Log.d(TAG, "Свайп вверх - переходим на следующий экран");
        
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
            Log.d(TAG, "Достигнут конец навигации или экран не найден в списке");
        }
    }
    
    /**
     * Обработка свайпа вниз - переход на предыдущий экран
     */
    private void onSwipeDown() {
        Log.d(TAG, "Свайп вниз - переходим на предыдущий экран");
        
        // Сбрасываем флаги двойной логики
        shouldNavigateOnNextSwipe = false;
        lastSwipeUpTime = 0;
        
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
            Log.d(TAG, "Достигнут начало навигации или экран не найден в списке");
        }
    }
    
    /**
     * Переход на указанный экран
     */
    private void navigateToActivity(Class<?> targetActivity) {
        try {
            Log.d(TAG, "Переход на экран: " + targetActivity.getSimpleName());
            Intent intent = new Intent(activity, targetActivity);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка перехода на экран " + targetActivity.getSimpleName() + ": " + e.getMessage(), e);
        }
    }
} 