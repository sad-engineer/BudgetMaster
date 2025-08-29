package com.sadengineer.budgetmaster.navigation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationTree;
import com.sadengineer.budgetmaster.navigation.navigation_tree.NavigationNode;

/**
 * Контроллер навигации
 * Отвечает за навигацию между экранами и управление вкладками
 */
public class NavigationController {
    
    private static final String TAG = "NavigationController";
    
    private final Context context;
    private final Class<?> currentActivityClass;
    
    public NavigationController(Context context, Class<?> currentActivityClass) {
        this.context = context;
        this.currentActivityClass = currentActivityClass;
    }
    
    /**
     * Навигация вверх по дереву
     * @param clearTop - флаг, определяющий, нужно ли очистить стек активностей
     * @param name - имя параметра
     * @param value - значение параметра
     */
    public void goUp(boolean clearTop, String name, Integer value) { 
        NavigationNode node = NavigationTree.navigateUp(currentActivityClass);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, 0);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                if (name != null && value != null) {
                    intent.putExtra(name, value);
                }
                context.startActivity(intent);
                Log.d(TAG, "Навигация вверх к " + node.name);
            }
        }
    }
    
    /**
     * Навигация вниз по дереву
     * @param clearTop - флаг, определяющий, нужно ли очистить стек активностей
     * @param name - имя параметра
     * @param value - значение параметра
     */
    public void goDown(boolean clearTop, String name, Integer value) { 
        NavigationNode node = NavigationTree.navigateDown(currentActivityClass);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, 0);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                if (name != null && value != null) {
                    intent.putExtra(name, value);
                }
                context.startActivity(intent);
                Log.d(TAG, "Навигация вниз к " + node.name);
            }
        }
    }
    
    /**
     * Навигация влево (переход к предыдущей вкладке)
     */
    public void goLeft(int currentTabIndex, boolean clearTop, String name, Integer value) { 
        NavigationNode node = NavigationTree.navigateLeft(currentActivityClass, currentTabIndex);
        if (node != null) {
            int previousTab = NavigationTree.getPreviousTabIndex(currentActivityClass, currentTabIndex);
            Intent intent = NavigationTree.createIntent(context, node, previousTab);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                if (name != null && value != null) {
                    intent.putExtra(name, value);
                }
                context.startActivity(intent);
                Log.d(TAG, "Навигация влево к вкладке " + previousTab);
            }
        }
    }
    
    /**
     * Навигация вправо (переход к следующей вкладке)
     */
    public void goRight(int currentTabIndex, boolean clearTop, String name, Integer value) { 
        NavigationNode node = NavigationTree.navigateRight(currentActivityClass, currentTabIndex);
        if (node != null) {
            int nextTab = NavigationTree.getNextTabIndex(currentActivityClass, currentTabIndex);
            Intent intent = NavigationTree.createIntent(context, node, nextTab);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                if (name != null && value != null) {
                    intent.putExtra(name, value);
                }
                context.startActivity(intent);
                Log.d(TAG, "Навигация вправо к вкладке " + nextTab);
            }
        }
    }
    
    /**
     * Переход к конкретной Activity
     */
    public void goTo(Class<?> targetActivity, boolean clearTop, String name, Integer value) { 
        NavigationNode node = NavigationTree.navigateToActivity(targetActivity);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, 0);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                if (name != null && value != null) {
                    intent.putExtra(name, value);
                }
                context.startActivity(intent);
                Log.d(TAG, "Переход к " + node.name);
            }
        }
    }
    
    /**
     * Переход к конкретной Activity с вкладкой
     */
    public void goTo(Class<?> targetActivity, int tabIndex, boolean clearTop, String name, Integer value) { 
        NavigationNode node = NavigationTree.navigateToActivity(targetActivity);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, tabIndex);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                if (name != null && value != null) {
                    intent.putExtra(name, value);
                }
                context.startActivity(intent);
                Log.d(TAG, "Переход к " + node.name + " на вкладку " + tabIndex);
            }
        }
    }

    /**
     * Переход к главному экрану
     */
    public void goToRoot() {
        NavigationNode node = NavigationTree.getRootNode();
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, 0);
            context.startActivity(intent);
            Log.d(TAG, "Переход к главному экрану");
        }
    }
}
