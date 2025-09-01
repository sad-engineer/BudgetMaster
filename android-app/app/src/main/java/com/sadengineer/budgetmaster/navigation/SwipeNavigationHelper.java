package com.sadengineer.budgetmaster.navigation;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;

import com.sadengineer.budgetmaster.start.MainActivity;
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
    private static final int SWIPE_THRESHOLD = 50;
    // Порог скорости для определения свайпа
    private static final int SWIPE_VELOCITY_THRESHOLD = 50;
    // Количество карточек, при котором включается двойная логика свайпа (больше не используется)
    // private static final int CARDS_THRESHOLD = 15;
    // Задержка между свайпами для навигации (в миллисекундах) (больше не используется)
    // private static final long NAVIGATION_SWIPE_DELAY = 500;
    
    private final BaseNavigationActivity activity;
    private final GestureDetector gestureDetector;
    private boolean isEnabled = true;
    
    // Конкретный RecyclerView для проверки (может быть null для окон без списков)
    private RecyclerView targetRecyclerView = null;
    
    // Счетчики для двойной логики свайпа (больше не используются)
    // private int swipeUpCount = 0;
    // private long lastSwipeUpTime = 0;
    // private boolean shouldNavigateOnNextSwipe = false;
    
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
        Log.d(TAG, "SwipeNavigationHelper создан для активности: " + activity.getClass().getSimpleName());
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
     * Устанавливает конкретный RecyclerView для проверки свайпов
     * @param recyclerView RecyclerView для проверки (null для окон без списков)
     */
    public void setTargetRecyclerView(RecyclerView recyclerView) {
        this.targetRecyclerView = recyclerView;
        if (recyclerView != null) {
            Log.d(TAG, "Установлен targetRecyclerView: " + recyclerView.getClass().getSimpleName());
            if (recyclerView.getAdapter() != null) {
                Log.d(TAG, "Адаптер: " + recyclerView.getAdapter().getClass().getSimpleName() + 
                          ", элементов: " + recyclerView.getAdapter().getItemCount());
            }
        } else {
            Log.d(TAG, "targetRecyclerView установлен в null (окно без списка)");
        }
    }
    
    /**
     * Получает установленный RecyclerView для проверки
     * @return RecyclerView или null, если не установлен
     */
    public RecyclerView getTargetRecyclerView() {
        return targetRecyclerView;
    }
    
    /**
     * Сбрасывает счетчик свайпов (вызывается при изменении содержимого списка)
     * Метод оставлен для совместимости, но больше не выполняет действий
     */
    public void resetSwipeCount() {
        // Счетчики больше не используются в новой логике
        Log.d(TAG, "Счетчик свайпов сброшен (метод оставлен для совместимости)");
    }
    
    /**
     * Проверяет, нужно ли использовать двойную логику свайпа
     * Метод больше не используется в новой логике
     */
    /*
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
    */
    
    /**
     * Ищет RecyclerView в иерархии View
     * @param view текущий View
     * @return RecyclerView или null, если RecyclerView не найден
     */
    private RecyclerView findRecyclerView(View view) {
        return findRecyclerViewRecursive(view, 0);
    }
    
    /**
     * Рекурсивный поиск RecyclerView с подробным логированием
     * @param view текущий View
     * @param depth глубина вложенности
     * @return RecyclerView или null, если RecyclerView не найден
     */
    private RecyclerView findRecyclerViewRecursive(View view, int depth) {
        String indent = "  ".repeat(depth);
        
        // Игнорируем NavigationView и его дочерние элементы
        if (view instanceof com.google.android.material.navigation.NavigationView) {
            Log.d(TAG, indent + "Найден NavigationView - игнорируем");
            return null;
        }
        
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            
            // Проверяем, не является ли этот RecyclerView частью NavigationView
            View parent = (View) view.getParent();
            int parentDepth = 0;
            while (parent != null) {
                if (parent instanceof com.google.android.material.navigation.NavigationView) {
                    Log.d(TAG, indent + "RecyclerView является частью NavigationView (глубина " + parentDepth + ") - игнорируем");
                    return null;
                }
                parent = (View) parent.getParent();
                parentDepth++;
            }
            
            // Получаем информацию о RecyclerView
            String viewId = "";
            if (view.getId() != android.view.View.NO_ID) {
                try {
                    viewId = view.getResources().getResourceEntryName(view.getId());
                } catch (Exception e) {
                    viewId = "ID_" + view.getId();
                }
            }
            
            // Получаем информацию об адаптере
            String adapterInfo = "без адаптера";
            int itemCount = 0;
            if (recyclerView.getAdapter() != null) {
                adapterInfo = recyclerView.getAdapter().getClass().getSimpleName();
                itemCount = recyclerView.getAdapter().getItemCount();
            }
            
            Log.d(TAG, indent + "Найден RecyclerView вне NavigationView:");
            Log.d(TAG, indent + "  - ID: " + viewId);
            Log.d(TAG, indent + "  - Класс: " + view.getClass().getSimpleName());
            Log.d(TAG, indent + "  - Адаптер: " + adapterInfo);
            Log.d(TAG, indent + "  - Количество элементов: " + itemCount);
            Log.d(TAG, indent + "  - Глубина: " + depth);
            
            return recyclerView;
        }
        
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
            String viewGroupInfo = "";
            if (view.getId() != android.view.View.NO_ID) {
                try {
                    viewGroupInfo = view.getResources().getResourceEntryName(view.getId());
                } catch (Exception e) {
                    viewGroupInfo = "ID_" + view.getId();
                }
            }
            
            Log.d(TAG, indent + "Проверяем ViewGroup: " + viewGroup.getClass().getSimpleName() + 
                      " (ID: " + viewGroupInfo + ", дочерних: " + viewGroup.getChildCount() + ")");
            
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                RecyclerView found = findRecyclerViewRecursive(viewGroup.getChildAt(i), depth + 1);
                if (found != null) {
                    return found;
                }
            }
        } else {
            String viewInfo = "";
            if (view.getId() != android.view.View.NO_ID) {
                try {
                    viewInfo = view.getResources().getResourceEntryName(view.getId());
                } catch (Exception e) {
                    viewInfo = "ID_" + view.getId();
                }
            }
            Log.d(TAG, indent + "Проверяем View: " + view.getClass().getSimpleName() + " (ID: " + viewInfo + ")");
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
        
        // Простая проверка через canScrollVertically
        boolean canScrollUp = recyclerView.canScrollVertically(-1);
        Log.d(TAG, "canScrollUp: " + canScrollUp);
        
        return canScrollUp;
    }
    
    /**
     * Проверяет, можно ли прокрутить список вниз
     * @param recyclerView RecyclerView
     * @return true - можно прокрутить список вниз, false - нельзя прокрутить список вниз
     */
    private boolean canScrollDown(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        
        // Простая проверка через canScrollVertically
        boolean canScrollDown = recyclerView.canScrollVertically(1);
        Log.d(TAG, "canScrollDown: " + canScrollDown);
        
        return canScrollDown;
    }
    
    /**
     * Проверяет, действительно ли список можно прокрутить
     * @param recyclerView RecyclerView
     * @return true - список можно прокрутить, false - список нельзя прокрутить
     */
    private boolean isListScrollable(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        
        // Проверяем, есть ли адаптер и элементы
        if (recyclerView.getAdapter() == null) return false;
        
        int itemCount = recyclerView.getAdapter().getItemCount();
        if (itemCount == 0) return false;
        
        // Проверяем, есть ли LayoutManager
        if (recyclerView.getLayoutManager() == null) return false;
        
        // Проверяем, можно ли прокрутить в любом направлении
        boolean canScrollUp = recyclerView.canScrollVertically(-1);
        boolean canScrollDown = recyclerView.canScrollVertically(1);
        
        Log.d(TAG, "isListScrollable: itemCount=" + itemCount + ", canScrollUp=" + canScrollUp + ", canScrollDown=" + canScrollDown);
        
        return canScrollUp || canScrollDown;
    }
    
    /**
     * Обрабатывает касание экрана
     * @param event MotionEvent
     * @return true - обработка свайпа, false - нет
     */
    public boolean onTouchEvent(MotionEvent event) {
        // Если свайпы отключены, не обрабатываем
        if (!isEnabled) {
            Log.d(TAG, "Свайпы отключены, игнорируем событие: " + event.getAction());
            return false;
        }
        
        // Проверяем, открыто ли меню
        if (activity.drawerLayout != null && activity.drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            Log.d(TAG, "Меню открыто, игнорируем событие: " + event.getAction());
            return false;
        }
        
        Log.d(TAG, "Обрабатываем событие касания: " + event.getAction() + 
                  " x=" + event.getX() + " y=" + event.getY());
        
        boolean result = gestureDetector.onTouchEvent(event);
        Log.d(TAG, "Результат обработки жеста: " + result);
        
        return result;
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
                
                Log.d(TAG, "Жест: diffX=" + diffX + ", diffY=" + diffY + 
                          ", velocityX=" + velocityX + ", velocityY=" + velocityY);
                
                // Проверяем, что свайп вертикальный (больше вертикального движения)
                if (Math.abs(diffY) > Math.abs(diffX)) {
                    Log.d(TAG, "Вертикальный свайп: |diffY|=" + Math.abs(diffY) + 
                              " > |diffX|=" + Math.abs(diffX));
                    
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && 
                        Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        
                        Log.d(TAG, "Свайп прошел пороги: |diffY|=" + Math.abs(diffY) + 
                                  " > " + SWIPE_THRESHOLD + ", |velocityY|=" + Math.abs(velocityY) + 
                                  " > " + SWIPE_VELOCITY_THRESHOLD);
                        
                        if (diffY < 0) {
                            // Свайп вверх
                            Log.d(TAG, "Обрабатываем свайп вверх");
                            onSwipeUp();
                            return true;
                        } else {
                            // Свайп вниз - переходим на предыдущий экран
                            Log.d(TAG, "Обрабатываем свайп вниз");
                            onSwipeDown();
                            return true;
                        }
                    } else {
                        Log.d(TAG, "Свайп не прошел пороги: |diffY|=" + Math.abs(diffY) + 
                                  " <= " + SWIPE_THRESHOLD + " или |velocityY|=" + Math.abs(velocityY) + 
                                  " <= " + SWIPE_VELOCITY_THRESHOLD);
                    }
                } else {
                    Log.d(TAG, "Не вертикальный свайп: |diffY|=" + Math.abs(diffY) + 
                              " <= |diffX|=" + Math.abs(diffX));
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
        
        if (targetRecyclerView != null) {
            // Есть целевой список - используем двойную логику
            Log.d(TAG, "Используем targetRecyclerView для двойной логики");
            handleDoubleSwipeLogic();
        } else {
            // Нет целевого списка - сразу переходим на следующий экран
            Log.d(TAG, "targetRecyclerView не установлен - используем обычную навигацию");
            navigateToNextScreen();
        }
    }
    
    /**
     * Обрабатывает двойную логику свайпа
     */
    private void handleDoubleSwipeLogic() {
        // Используем целевой RecyclerView
        RecyclerView recyclerView = targetRecyclerView;
        
        if (recyclerView == null) {
            Log.d(TAG, "targetRecyclerView равен null, используем обычную навигацию");
            navigateToNextScreen();
            return;
        }
        
        // Проверяем количество элементов в списке
        int itemCount = recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : 0;
        Log.d(TAG, "Количество элементов в списке: " + itemCount);
        
        // Проверяем, можно ли прокрутить список вверх и вниз
        boolean canScrollUp = canScrollUp(recyclerView);
        boolean canScrollDown = canScrollDown(recyclerView);
        
        // Дополнительная проверка - действительно ли список можно прокрутить
        boolean isScrollable = isListScrollable(recyclerView);
        Log.d(TAG, "Список прокручиваемый: " + isScrollable);
        
        // Логика: 
        // 1. Если не можем прокрутить вниз или вверх - значит списка нет или он помещается на экран
        // свайп вверх - переход на следующий экран, свайп вниз - переход на предыдущий экран
        
        // 2. Если можем прокрутить вниз, но не можем прокрутить вверх - значит мы в начале списка, и список не помещается на экран
        // свайп вверх - прокрутка вниз, свайп вниз - переход на предыдущий экран

        // 3. Если можем прокрутить вверх или вниз - значит мы в середине списка, и список помещается на экран
        // свайп вверх - прокрутка вниз, свайп вниз - прокрутка вверх

        // 4. Если не можем прокрутить вниз, но можем прокрутить вверх - значит мы в конце списка, и список не помещается на экран
        // свайп вверх - переход на следующий экран, свайп вниз - прокрутка вверх

        if (itemCount == 0) {
            // Список пустой
            Log.d(TAG, "Список пустой - переходим на следующий экран");
            navigateToNextScreen();
            
        } else if (!canScrollUp && !canScrollDown) {
            // Список помещается на экран полностью
            Log.d(TAG, "Список помещается на экран полностью - переходим на следующий экран");
            navigateToNextScreen();
            
        } else if (!canScrollUp && canScrollDown) {
            // Мы в начале списка
            Log.d(TAG, "Мы в начале списка - прокручиваем список вниз");
            int scrollDistance = 400;
            recyclerView.smoothScrollBy(0, scrollDistance);
            Log.d(TAG, "Список прокручен вниз");
            
        } else if (canScrollUp && canScrollDown) {
            // Мы в середине списка
            Log.d(TAG, "Мы в середине списка - прокручиваем список вниз");
            int scrollDistance = 400;
            recyclerView.smoothScrollBy(0, scrollDistance);
            Log.d(TAG, "Список прокручен вниз");
            
        } else if (canScrollUp && !canScrollDown) {
            // Мы в конце списка
            Log.d(TAG, "Мы в конце списка - переходим на следующий экран");
            navigateToNextScreen();
            
        } else {
            // Неожиданная ситуация - переходим на следующий экран
            Log.w(TAG, "Неожиданная ситуация: canScrollUp=" + canScrollUp + ", canScrollDown=" + canScrollDown + " - переходим на следующий экран");
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
        Log.d(TAG, "Текущий экран: " + currentActivity.getSimpleName());
        
        int currentIndex = -1;
        
        for (int i = 0; i < navigationOrder.length; i++) {
            Log.d(TAG, "Проверяем " + i + ": " + navigationOrder[i].getSimpleName());
            if (navigationOrder[i] == currentActivity) {
                currentIndex = i;
                Log.d(TAG, "Найден текущий экран на позиции: " + i);
                break;
            }
        }
        
        // Если текущий экран найден и есть следующий экран
        if (currentIndex >= 0 && currentIndex < navigationOrder.length - 1) {
            Class<?> nextActivity = navigationOrder[currentIndex + 1];
            Log.d(TAG, "Переходим на следующий экран: " + nextActivity.getSimpleName());
            navigateToActivity(nextActivity);
        } else {
            Log.d(TAG, "Достигнут конец навигации или экран не найден в списке. currentIndex: " + currentIndex);
        }
    }
    
    /**
     * Обработка свайпа вниз с двойной логикой
     */
    private void onSwipeDown() {
        Log.d(TAG, "Свайп вниз - обрабатываем с двойной логикой");
        
        if (targetRecyclerView != null) {
            // Есть целевой список - используем двойную логику
            Log.d(TAG, "Используем targetRecyclerView для двойной логики свайпа вниз");
            handleDoubleSwipeDownLogic();
        } else {
            // Нет целевого списка - сразу переходим на предыдущий экран
            Log.d(TAG, "targetRecyclerView не установлен - используем обычную навигацию назад");
            navigateToPreviousScreen();
        }
    }
    
    /**
     * Обрабатывает двойную логику свайпа вниз
     */
    private void handleDoubleSwipeDownLogic() {
        // Используем целевой RecyclerView
        RecyclerView recyclerView = targetRecyclerView;
        
        if (recyclerView == null) {
            Log.d(TAG, "targetRecyclerView равен null, используем обычную навигацию назад");
            navigateToPreviousScreen();
            return;
        }
        
        // Проверяем количество элементов в списке
        int itemCount = recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : 0;
        Log.d(TAG, "Количество элементов в списке: " + itemCount);
        
        // Проверяем, можно ли прокрутить список вверх и вниз
        boolean canScrollUp = canScrollUp(recyclerView);
        boolean canScrollDown = canScrollDown(recyclerView);
        
        // Дополнительная проверка - действительно ли список можно прокрутить
        boolean isScrollable = isListScrollable(recyclerView);
        Log.d(TAG, "Список прокручиваемый: " + isScrollable);
        
        // Улучшенная логика для свайпа вниз:
        // 1. Если список пустой или помещается на экран - переходим на предыдущий экран
        // 2. Если мы в начале списка - переходим на предыдущий экран
        // 3. Если мы в середине списка - прокручиваем вверх
        // 4. Если мы в конце списка - прокручиваем вверх

        if (itemCount == 0) {
            // Список пустой
            Log.d(TAG, "Список пустой - переходим на предыдущий экран");
            navigateToPreviousScreen();
            
        } else if (!canScrollUp && !canScrollDown) {
            // Список помещается на экран полностью
            Log.d(TAG, "Список помещается на экран полностью - переходим на предыдущий экран");
            navigateToPreviousScreen();
            
        } else if (!canScrollUp && canScrollDown) {
            // Мы в начале списка
            Log.d(TAG, "Мы в начале списка - переходим на предыдущий экран");
            navigateToPreviousScreen();
            
        } else if (canScrollUp && canScrollDown) {
            // Мы в середине списка
            Log.d(TAG, "Мы в середине списка - прокручиваем список вверх");
            int scrollDistance = -400;
            recyclerView.smoothScrollBy(0, scrollDistance);
            Log.d(TAG, "Список прокручен вверх");
            
        } else if (canScrollUp && !canScrollDown) {
            // Мы в конце списка
            Log.d(TAG, "Мы в конце списка - прокручиваем список вверх");
            int scrollDistance = -400;
            recyclerView.smoothScrollBy(0, scrollDistance);
            Log.d(TAG, "Список прокручен вверх");
            
        } else {
            // Неожиданная ситуация - переходим на предыдущий экран
            Log.w(TAG, "Неожиданная ситуация: canScrollUp=" + canScrollUp + ", canScrollDown=" + canScrollDown + " - переходим на предыдущий экран");
            navigateToPreviousScreen();
        }
    }
    
    /**
     * Переход на предыдущий экран
     */
    private void navigateToPreviousScreen() {
        Log.d(TAG, "Свайп вниз - переходим на предыдущий экран");
        
        // Находим текущий экран в порядке навигации
        Class<?> currentActivity = activity.getClass();
        Log.d(TAG, "Текущий экран: " + currentActivity.getSimpleName());
        
        int currentIndex = -1;
        
        for (int i = 0; i < navigationOrder.length; i++) {
            Log.d(TAG, "Проверяем " + i + ": " + navigationOrder[i].getSimpleName());
            if (navigationOrder[i] == currentActivity) {
                currentIndex = i;
                Log.d(TAG, "Найден текущий экран на позиции: " + i);
                break;
            }
        }
        
        // Если текущий экран найден и есть предыдущий экран
        if (currentIndex > 0) {
            Class<?> previousActivity = navigationOrder[currentIndex - 1];
            Log.d(TAG, "Переходим на предыдущий экран: " + previousActivity.getSimpleName());
            navigateToActivity(previousActivity);
        } else {
            Log.d(TAG, "Достигнут начало навигации или экран не найден в списке. currentIndex: " + currentIndex);
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