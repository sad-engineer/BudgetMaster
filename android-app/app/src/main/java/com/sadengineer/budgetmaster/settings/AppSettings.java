package com.sadengineer.budgetmaster.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Класс для управления настройками приложения
 */
public class AppSettings {
    private static final String TAG = "AppSettings";
    private static final String PREF_NAME = "app_settings";
    
    // Ключи для настроек
    private static final String KEY_SHOW_POSITION = "show_position";
    private static final String KEY_SHOW_ID = "show_id";
    
    // Значения по умолчанию
    private static final boolean DEFAULT_SHOW_POSITION = true;
    private static final boolean DEFAULT_SHOW_ID = true;
    
    private SharedPreferences preferences;
    
    public AppSettings(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "AppSettings инициализированы");
    }
    
    /**
     * Получает настройку отображения позиции
     */
    public boolean isShowPosition() {
        boolean value = preferences.getBoolean(KEY_SHOW_POSITION, DEFAULT_SHOW_POSITION);
        Log.d(TAG, "Получена настройка show_position: " + value);
        return value;
    }
    
    /**
     * Устанавливает настройку отображения позиции
     */
    public void setShowPosition(boolean show) {
        preferences.edit().putBoolean(KEY_SHOW_POSITION, show).apply();
        Log.d(TAG, "Установлена настройка show_position: " + show);
    }
    
    /**
     * Получает настройку отображения ID
     */
    public boolean isShowId() {
        boolean value = preferences.getBoolean(KEY_SHOW_ID, DEFAULT_SHOW_ID);
        Log.d(TAG, "Получена настройка show_id: " + value);
        return value;
    }
    
    /**
     * Устанавливает настройку отображения ID
     */
    public void setShowId(boolean show) {
        preferences.edit().putBoolean(KEY_SHOW_ID, show).apply();
        Log.d(TAG, "Установлена настройка show_id: " + show);
    }
    
    /**
     * Сбрасывает все настройки к значениям по умолчанию
     */
    public void resetToDefaults() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_SHOW_POSITION, DEFAULT_SHOW_POSITION);
        editor.putBoolean(KEY_SHOW_ID, DEFAULT_SHOW_ID);
        editor.apply();
        Log.d(TAG, "Настройки сброшены к значениям по умолчанию");
    }
}
