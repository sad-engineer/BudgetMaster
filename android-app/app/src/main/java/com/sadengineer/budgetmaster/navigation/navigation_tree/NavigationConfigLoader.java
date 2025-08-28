package com.sadengineer.budgetmaster.navigation.navigation_tree;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Загрузчик конфигурации навигации из JSON файла
 */
public class NavigationConfigLoader {
    
    private static final String TAG = "NavigationConfigLoader";
    private static final String CONFIG_FILE = "navigation_config.json";
    
    /**
     * Загружает конфигурацию навигации из JSON файла
     * @param context контекст приложения
     * @return список конфигураций Activity
     */
    public static List<ActivityConfig> loadConfig(Context context) {
        List<ActivityConfig> configs = new ArrayList<>();
        
        try {
            String jsonString = loadJsonFromAssets(context, CONFIG_FILE);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray activitiesArray = jsonObject.getJSONArray("activities");
            
            for (int i = 0; i < activitiesArray.length(); i++) {
                JSONObject activityJson = activitiesArray.getJSONObject(i);
                ActivityConfig config = parseActivityConfig(activityJson);
                configs.add(config);
            }
            
            Log.d(TAG, "Загружено " + configs.size() + " конфигураций Activity");
            
        } catch (JSONException e) {
            Log.e(TAG, "Ошибка парсинга JSON конфигурации", e);
        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки файла конфигурации", e);
        }
        
        return configs;
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
     * Парсит конфигурацию Activity из JSON
     */
    private static ActivityConfig parseActivityConfig(JSONObject json) throws JSONException {
        String className = json.getString("class");
        String name = json.getString("name");
        
        // Парсим вкладки
        JSONArray tabsArray = json.getJSONArray("tabs");
        String[] tabs = new String[tabsArray.length()];
        for (int i = 0; i < tabsArray.length(); i++) {
            tabs[i] = tabsArray.getString(i);
        }
        
        // Парсим связи
        String upClass = json.optString("up", null);
        String downClass = json.optString("down", null);
        
        // Парсим ID пункта меню
        String menuId = json.optString("menu_id", null);
        
        return new ActivityConfig(className, name, tabs, upClass, downClass, menuId);
    }
    

}
