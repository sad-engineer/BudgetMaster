package com.sadengineer.budgetmaster.navigation.navigation_tree;

import android.content.Context;
 
import com.sadengineer.budgetmaster.utils.LogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Загрузчик конфигурации навигации из JSON файла
 * Содержит метод для загрузки конфигурации навигации из JSON файла
 * и метод для парсинга конфигурации Activity из JSON
 */
public class NavigationConfigLoader {
    
    private static final String TAG = "NavigationConfigLoader";
    private static final String CONFIG_FILE = "navigation_config.json";
    
    /**
     * Загружает конфигурацию навигации из JSON файла
     * @param context контекст приложения
     * @return карта узлов навигации по классу Activity
     */
    public static Map<Class<?>, NavigationNode> loadConfig(Context context) {
        LogManager.d(TAG, "Загрузка конфигурации навигации из JSON файла...");
        Map<Class<?>, NavigationNode> nodesByClass = new HashMap<>();
        Map<String, NavigationNode> nodesByClassName = new HashMap<>();
        
        try {
            String jsonString = loadJsonFromAssets(context, CONFIG_FILE);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray activitiesArray = jsonObject.getJSONArray("activities");
            
            // Первый проход: создаем все узлы
            for (int i = 0; i < activitiesArray.length(); i++) {
                JSONObject activityJson = activitiesArray.getJSONObject(i);
                NavigationNode node = parseNavigationNode(activityJson);
                if (node != null) {
                    nodesByClass.put(node.activityClass, node);
                    nodesByClassName.put(node.activityClass.getName(), node);
                }
            }
            
            // Второй проход: устанавливаем связи
            for (int i = 0; i < activitiesArray.length(); i++) {
                JSONObject activityJson = activitiesArray.getJSONObject(i);
                String className = activityJson.getString("class");
                NavigationNode currentNode = nodesByClassName.get(className);
                
                if (currentNode != null) {
                    // Устанавливаем связь вверх
                    if (activityJson.has("up") && !activityJson.isNull("up")) {
                        String upClassName = activityJson.getString("up");
                        NavigationNode upNode = nodesByClassName.get(upClassName);
                        if (upNode != null) {
                            currentNode.up = upNode;
                        }
                    }
                    
                    // Устанавливаем связь вниз
                    if (activityJson.has("down") && !activityJson.isNull("down")) {
                        String downClassName = activityJson.getString("down");
                        NavigationNode downNode = nodesByClassName.get(downClassName);
                        if (downNode != null) {
                            currentNode.down = downNode;
                        }
                    }
                }
            }
            
            LogManager.d(TAG, "Загружено " + nodesByClass.size() + " узлов навигации");
            
        } catch (JSONException e) {
            LogManager.e(TAG, "Ошибка парсинга JSON конфигурации", e);
        } catch (IOException e) {
            LogManager.e(TAG, "Ошибка загрузки файла конфигурации", e);
        }
        
        LogManager.d(TAG, "Конфигурация навигации загружена успешно");
        return nodesByClass;
    }
    
    /**
     * Загружает JSON строку из assets
     */
    private static String loadJsonFromAssets(Context context, String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }
    
    /**
     * Парсит узел навигации из JSON
     */
    private static NavigationNode parseNavigationNode(JSONObject json) throws JSONException {
        String className = json.getString("class");
        String name = json.getString("name");
        
        // Парсим вкладки
        JSONArray tabsArray = json.getJSONArray("tabs");
        String[] tabs = new String[tabsArray.length()];
        for (int i = 0; i < tabsArray.length(); i++) {
            tabs[i] = tabsArray.getString(i);
        }
        
        // Парсим ID пункта меню
        String menuId = null;
        if (json.has("menu_id") && !json.isNull("menu_id")) {
            menuId = json.getString("menu_id");
        }
        
        try {
            // Получаем класс Activity
            Class<?> activityClass = Class.forName(className);
            
            // Создаем узел навигации
            NavigationNode node = new NavigationNode(
                    activityClass,
                    name,
                    tabs.length,
                    tabs,
                    menuId,
                    null, // up - будет установлено позже
                    null, // down - будет установлено позже
                    null, // left - не используется
                    null  // right - не используется
            );
            
            return node;
            
        } catch (ClassNotFoundException e) {
            LogManager.e("NavigationConfigLoader", "Класс Activity не найден: " + className, e);
            return null;
        }
    }
    

}
