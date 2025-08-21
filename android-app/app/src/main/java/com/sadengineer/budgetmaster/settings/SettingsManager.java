package com.sadengineer.budgetmaster.settings;

import android.content.Context;

/**
 * Утилитарный класс для управления настройками в адаптерах
 */
public class SettingsManager {
    private static AppSettings appSettings;
    
    /**
     * Инициализирует менеджер настроек
     */
    public static void init(Context context) {
        if (appSettings == null) {
            appSettings = new AppSettings(context);
        }
    }
    
    /**
     * Получает настройку отображения позиции
     */
    public static boolean isShowPosition() {
        if (appSettings == null) {
            return true; // По умолчанию показываем
        }
        return appSettings.isShowPosition();
    }
    
    /**
     * Получает настройку отображения ID
     */
    public static boolean isShowId() {
        if (appSettings == null) {
            return true; // По умолчанию показываем
        }
        return appSettings.isShowId();
    }
    
    /**
     * Очищает ссылку на настройки (для предотвращения утечек памяти)
     */
    public static void clear() {
        appSettings = null;
    }
}
