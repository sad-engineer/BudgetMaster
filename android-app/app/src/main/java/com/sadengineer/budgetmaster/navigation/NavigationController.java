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
     * @param params - массив параметров в формате [name1, value1, name2, value2, ...]
     */
    public void goUp(boolean clearTop, String[] params) { 
        NavigationNode node = NavigationTree.navigateUp(currentActivityClass);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, 0);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                setIntentParameters(intent, params);
                context.startActivity(intent);
                Log.d(TAG, "Навигация вверх к " + node.name);
            }
        }
    }
    
    /**
     * Навигация вниз по дереву
     * @param clearTop - флаг, определяющий, нужно ли очистить стек активностей
     * @param params - массив параметров в формате [name1, value1, name2, value2, ...]
     */
    public void goDown(boolean clearTop, String[] params) { 
        NavigationNode node = NavigationTree.navigateDown(currentActivityClass);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, 0);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                setIntentParameters(intent, params);
                context.startActivity(intent);
                Log.d(TAG, "Навигация вниз к " + node.name);
            }
        }
    }
    
    /**
     * Навигация влево (переход к предыдущей вкладке)
     */
    public void goLeft(int currentTabIndex, boolean clearTop, String[] params) { 
        NavigationNode node = NavigationTree.navigateLeft(currentActivityClass, currentTabIndex);
        if (node != null) {
            int previousTab = NavigationTree.getPreviousTabIndex(currentActivityClass, currentTabIndex);
            Intent intent = NavigationTree.createIntent(context, node, previousTab);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                setIntentParameters(intent, params);
                context.startActivity(intent);
                Log.d(TAG, "Навигация влево к вкладке " + previousTab);
            }
        }
    }
    
    /**
     * Навигация вправо (переход к следующей вкладке)
     */
    public void goRight(int currentTabIndex, boolean clearTop, String[] params) { 
        NavigationNode node = NavigationTree.navigateRight(currentActivityClass, currentTabIndex);
        if (node != null) {
            int nextTab = NavigationTree.getNextTabIndex(currentActivityClass, currentTabIndex);
            Intent intent = NavigationTree.createIntent(context, node, nextTab);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                setIntentParameters(intent, params);
                context.startActivity(intent);
                Log.d(TAG, "Навигация вправо к вкладке " + nextTab);
            }
        }
    }
    
    /**
     * Переход к конкретной Activity
     */
    public void goTo(Class<?> targetActivity, boolean clearTop, String[] params) { 
        Log.d(TAG, "NavigationController.goTo: Попытка перехода к " + targetActivity.getSimpleName());
        NavigationNode node = NavigationTree.navigateToActivity(targetActivity);
        if (node != null) {
            Log.d(TAG, "NavigationController.goTo: Узел найден: " + node.name);
            Intent intent = NavigationTree.createIntent(context, node, 0);
            if (intent != null) {
                Log.d(TAG, "NavigationController.goTo: Intent создан успешно");
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                setIntentParameters(intent, params);
                Log.d(TAG, "NavigationController.goTo: Запускаем Activity");
                context.startActivity(intent);
                Log.d(TAG, "NavigationController.goTo: Activity запущена: " + node.name);
            } else {
                Log.e(TAG, "NavigationController.goTo: Не удалось создать Intent для " + targetActivity.getSimpleName());
            }
        } else {
            Log.e(TAG, "NavigationController.goTo: Узел не найден для " + targetActivity.getSimpleName());
        }
    }
    
    /**
     * Переход к конкретной Activity с вкладкой
     */
    public void goTo(Class<?> targetActivity, int tabIndex, boolean clearTop, String[] params) { 
        NavigationNode node = NavigationTree.navigateToActivity(targetActivity);
        if (node != null) {
            Intent intent = NavigationTree.createIntent(context, node, tabIndex);
            if (intent != null) {
                if (clearTop) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                setIntentParameters(intent, params);
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

    /**
     * Устанавливает значения нескольких параметров в intent
     * @param intent - Intent для установки параметров
     * @param params - массив параметров в формате [name1, value1, name2, value2, ...]
     */
    private void setIntentParameters(Intent intent, String[] params) {
        if (params == null || params.length == 0) {
            return;
        }
        
        // Добавляем параметры в Intent
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                String name = params[i];
                String valueStr = params[i + 1];
                
                if (name == null || valueStr == null) {
                    Log.w(TAG, "Пропускаем null параметр: name=" + name + ", value=" + valueStr);
                    continue;
                }
                
                // Пытаемся определить тип значения и установить его
                try {
                    // Проверяем, является ли значение числом
                    if (valueStr.matches("-?\\d+")) {
                        // Целое число
                        intent.putExtra(name, Integer.parseInt(valueStr));
                    } else if (valueStr.matches("-?\\d*\\.\\d+")) {
                        // Число с плавающей точкой
                        intent.putExtra(name, Double.parseDouble(valueStr));
                    } else if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false")) {
                        // Булево значение
                        intent.putExtra(name, Boolean.parseBoolean(valueStr));
                    } else {
                        // Строка (по умолчанию)
                        intent.putExtra(name, valueStr);
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Не удалось преобразовать значение '" + valueStr + "' для параметра '" + name + "', используем как строку");
                    intent.putExtra(name, valueStr);
                }
            }
        }
    }
}
