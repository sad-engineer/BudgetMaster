package com.sadengineer.budgetmaster.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Менеджер конфигурации логирования из JSON файла
 */
public class LogConfigManager {
    
    private static final String TAG = "LogConfigManager";
    private static final String CONFIG_FILE = "log_config.jsonc";
    
    private static LogConfigManager instance;
    private Map<String, String> logLevels;
    private String defaultLogLevel = "DEBUG";
    
    private LogConfigManager() {
        // Инициализация только при загрузке конфигурации
    }
    
    /**
     * Получает экземпляр синглтона
     */
    public static synchronized LogConfigManager getInstance() {
        if (instance == null) {
            instance = new LogConfigManager();
        }
        return instance;
    }
    
    /**
     * Загружает конфигурацию из JSON файла
     */
    public void loadConfig(Context context) {
        try {
            String jsonString = loadJSONFromAsset(context, CONFIG_FILE);
            if (jsonString != null) {
                parseConfig(jsonString);
                Log.d(TAG, "Конфигурация логирования загружена успешно");
            } else {
                Log.e(TAG, "Файл конфигурации не найден: " + CONFIG_FILE);
                throw new RuntimeException("Конфигурационный файл не найден");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки конфигурации: " + e.getMessage(), e);
            throw new RuntimeException("Не удалось загрузить конфигурацию логирования", e);
        }
    }
    
    /**
     * Загружает JSON из assets
     */
    private String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e(TAG, "Ошибка чтения файла: " + ex.getMessage(), ex);
        }
        return json;
    }
    
    /**
     * Удаляет комментарии из JSONC строки
     */
    private String removeComments(String jsonString) {
        if (jsonString == null) return null;
        
        // Удаляем однострочные комментарии //
        jsonString = jsonString.replaceAll("//.*", "");
        
        // Удаляем многострочные комментарии /* */
        jsonString = jsonString.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        
        return jsonString;
    }
    
    /**
     * Парсит JSON конфигурацию
     */
    private void parseConfig(String jsonString) throws JSONException {
        // Удаляем комментарии из JSONC
        String cleanJson = removeComments(jsonString);
        JSONObject jsonObject = new JSONObject(cleanJson);
        
        // Загружаем уровни логирования
        logLevels = new HashMap<>();
        if (jsonObject.has("log_levels")) {
            JSONObject logLevelsObj = jsonObject.getJSONObject("log_levels");
            Iterator<String> keys = logLevelsObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                logLevels.put(key, logLevelsObj.getString(key));
            }
        }
    }
    
    /**
     * Получает уровень логирования для тега
     */
    public String getLogLevel(String tag) {
        if (logLevels == null) {
            throw new IllegalStateException("Конфигурация не загружена. Вызовите loadConfig() сначала.");
        }
        if (tag == null) return defaultLogLevel;
        return logLevels.getOrDefault(tag, defaultLogLevel);
    }
    
    /**
     * Проверяет, нужно ли логировать с данным уровнем
     */
    public boolean shouldLogWithLevel(String tag, String level) {
        if (logLevels == null) {
            throw new IllegalStateException("Конфигурация не загружена. Вызовите loadConfig() сначала.");
        }
        //проверка наличия тега в конфигурации
        if (!logLevels.containsKey(tag)) {
            Log.w(TAG, "Тег " + tag + " не найден в конфигурации. Логирование будет включено.");
            level = defaultLogLevel;
        }
        String tagLevel = getLogLevel(tag);
        return isLevelEnabled(level, tagLevel);
    }
    
    /**
     * Проверяет, включен ли уровень логирования
     */
    private boolean isLevelEnabled(String currentLevel, String configLevel) {
        String[] levels = {"VERBOSE", "DEBUG", "INFO", "WARN", "ERROR"};
        int currentIndex = getLevelIndex(currentLevel, levels);
        int configIndex = getLevelIndex(configLevel, levels);
        return currentIndex >= configIndex;
    }
    
    /**
     * Получает индекс уровня логирования
     */
    private int getLevelIndex(String level, String[] levels) {
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(level)) {
                return i;
            }
        }
        return 0; // По умолчанию VERBOSE
    }
    
    /**
     * Добавляет или обновляет тег в конфигурации
     */
    public void addTag(String tag, String level) {
        if (logLevels == null) {
            throw new IllegalStateException("Конфигурация не загружена. Вызовите loadConfig() сначала.");
        }
        logLevels.put(tag, level);
    }
    
    /**
     * Удаляет тег из конфигурации
     */
    public void removeTag(String tag) {
        if (logLevels == null) {
            throw new IllegalStateException("Конфигурация не загружена. Вызовите loadConfig() сначала.");
        }
        logLevels.remove(tag);
    }
    
    /**
     * Получает текущую конфигурацию в виде строки
     */
    public String getConfigSummary() {
        if (logLevels == null) {
            return "Конфигурация не загружена";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== КОНФИГУРАЦИЯ ЛОГИРОВАНИЯ ===\n");
        sb.append("Уровни логирования: ").append(logLevels.size()).append("\n");
        sb.append("Уровень по умолчанию: ").append(defaultLogLevel).append("\n");
        return sb.toString();
    }
}
