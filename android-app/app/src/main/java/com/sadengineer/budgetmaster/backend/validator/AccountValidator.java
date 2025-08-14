
package com.sadengineer.budgetmaster.backend.validator;

/**
 * Валидатор для счетов
 */
public class AccountValidator {
    
    private static final String TAG = "AccountValidator";
    
    // Минимальная длина названия счета
    private static final int MIN_TITLE_LENGTH = 1;
    
    // Максимальная длина названия счета
    private static final int MAX_TITLE_LENGTH = 50;
    
    /**
     * Валидирует название счета
     * @param title - название счета для валидации
     * @throws IllegalArgumentException если название невалидно
     */
    public static void validateTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        
        String trimmedTitle = title.trim();
        
        if (trimmedTitle.isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        
        if (trimmedTitle.length() < MIN_TITLE_LENGTH) {
            throw new IllegalArgumentException("Название счета должно содержать минимум " + MIN_TITLE_LENGTH + " символ");
        }
        
        if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Название счета не может быть длиннее " + MAX_TITLE_LENGTH + " символов");
        }
        
        // Проверяем на специальные символы
        if (!trimmedTitle.matches("^[a-zA-Zа-яА-Я0-9\\s\\-_\\.]+$")) {
            throw new IllegalArgumentException("Название счета содержит недопустимые символы");
        }
    }
    
    /**
     * Валидирует баланс счета
     * @param balance - баланс для валидации
     * @throws IllegalArgumentException если баланс невалиден
     */
    public static void validateBalance(double balance) {
        if (Double.isNaN(balance)) {
            throw new IllegalArgumentException("Баланс не может быть NaN");
        }
        
        if (Double.isInfinite(balance)) {
            throw new IllegalArgumentException("Баланс не может быть бесконечным");
        }
        
        // Можно добавить дополнительные проверки, например максимальный баланс
        if (balance > 999999999.99) {
            throw new IllegalArgumentException("Баланс не может превышать 999,999,999.99");
        }
    }
    
    /**
     * Валидирует тип счета
     * @param type - тип счета для валидации
     * @throws IllegalArgumentException если тип невалиден
     */
    public static void validateType(Integer type) {
        if (type == null) {
            throw new IllegalArgumentException("Тип счета не может быть пустым");
        }
        
        if (type != 1 && type != 2 && type != 3) {
            throw new IllegalArgumentException("Тип счета должен быть 1, 2 или 3");
        }
    }
    
    /**
     * Валидирует ID валюты счета
     * @param currencyID - валюта для валидации
     * @throws IllegalArgumentException если валюта невалидна
     */
    public static void validateCurrencyID(int currencyID) {
        if (currencyID < 1) {
            throw new IllegalArgumentException("ID валюты не может быть меньше 1");
        }
        
        // Проверяем, что ID валюты состоит из цифр
        if (!String.valueOf(currencyID).matches("^[0-9]+$")) {
            throw new IllegalArgumentException("ID валюты должен состоять из цифр");
        }
    }
    


} 