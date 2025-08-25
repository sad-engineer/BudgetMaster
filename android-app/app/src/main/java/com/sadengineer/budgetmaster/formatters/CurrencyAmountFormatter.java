package com.sadengineer.budgetmaster.formatters;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.util.ULocale;

import java.util.Locale;
import java.text.ParseException;

/**
 * Форматтер для отображения валютных сумм с поддержкой различных локалей
 * Использует ICU4J для точного форматирования в соответствии с международными стандартами
 */
public class CurrencyAmountFormatter {
    
    private NumberFormat formatter;
    private NumberFormat compactFormatter;
    private NumberFormat currencyFormatter;
    private ULocale currentULocale;
    private Locale currentLocale;
    
    /**
     * Конструктор с указанием локали
     * @param locale локаль для форматирования (например, new Locale("ru", "RU"))
     */
    public CurrencyAmountFormatter(Locale locale) {
        this.currentLocale = locale;
        this.currentULocale = new ULocale(locale.toString().replace("_", "-"));
        initializeFormatters();
    }
    
    /**
     * Конструктор по умолчанию с русской локалью
     */
    public CurrencyAmountFormatter() {
        this(new Locale("ru", "RU"));
    }
    
    /**
     * Инициализирует форматтеры ICU4J
     */
    private void initializeFormatters() {
        // Основной форматтер для чисел
        this.formatter = NumberFormat.getNumberInstance(currentULocale);
        this.formatter.setMinimumFractionDigits(2);
        this.formatter.setMaximumFractionDigits(2);
        
        // Компактный форматтер (1.2K, 1.5M) - используем DecimalFormat с паттерном
        this.compactFormatter = new DecimalFormat("#,##0.#K;#,##0.#M", new DecimalFormatSymbols(currentULocale));
        
        // Валютный форматтер
        this.currencyFormatter = NumberFormat.getCurrencyInstance(currentULocale);
    }
    
    /**
     * Создает форматтер для указанной валюты
     * @param currencyCode код валюты (например, "RUB", "USD", "EUR")
     * @return форматтер с соответствующей локалью
     */
    public static CurrencyAmountFormatter forCurrency(String currencyCode) {
        Locale locale = getLocaleForCurrency(currencyCode);
        return new CurrencyAmountFormatter(locale);
    }
    
    /**
     * Определяет локаль по коду валюты
     * @param currencyCode код валюты
     * @return соответствующая локаль
     */
    public static Locale getLocaleForCurrency(String currencyCode) {
        if (currencyCode == null) {
            return new Locale("ru", "RU"); // По умолчанию русская локаль
        }
        
        switch (currencyCode.toUpperCase()) {
            case "RUB":
                return new Locale("ru", "RU");
            case "USD":
                return new Locale("en", "US");
            case "EUR":
                return new Locale("de", "DE"); // Германия как представитель еврозоны
            case "GBP":
                return new Locale("en", "GB");
            case "JPY":
                return new Locale("ja", "JP");
            case "CNY":
                return new Locale("zh", "CN");
            case "KRW":
                return new Locale("ko", "KR");
            case "UAH":
                return new Locale("uk", "UA");
            case "PLN":
                return new Locale("pl", "PL");
            case "BRL":
                return new Locale("pt", "BR");
            case "CAD":
                return new Locale("en", "CA");
            case "AUD":
                return new Locale("en", "AU");
            case "CHF":
                return new Locale("de", "CH");
            case "SEK":
                return new Locale("sv", "SE");
            case "NOK":
                return new Locale("no", "NO");
            case "DKK":
                return new Locale("da", "DK");
            case "TRY":
                return new Locale("tr", "TR");
            case "INR":
                return new Locale("hi", "IN");
            default:
                return new Locale("ru", "RU"); // По умолчанию русская локаль
        }
    }
    
    /**
     * Форматирует сумму в соответствии с установленной локалью
     * @param amount сумма для форматирования
     * @return отформатированная строка
     */
    public String format(double amount) {
        return formatter.format(amount);
    }
    
    /**
     * Форматирует сумму в соответствии с установленной локалью
     * @param amount сумма для форматирования
     * @return отформатированная строка
     */
    public String format(long amount) {
        return formatter.format(amount);
    }
    
    /**
     * Форматирует сумму как валюту с символом валюты
     * @param amount сумма для форматирования
     * @param currencyCode код валюты
     * @return отформатированная строка с символом валюты
     */
    public String formatCurrency(double amount, String currencyCode) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(currentULocale);
        currencyFormat.setCurrency(com.ibm.icu.util.Currency.getInstance(currencyCode));
        return currencyFormat.format(amount);
    }
    
    /**
     * Форматирует сумму в компактном виде (например, 1.2K, 1.5M)
     * @param amount сумма для форматирования
     * @return отформатированная строка
     */
    public String formatCompact(double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000);
        } else {
            return String.format("%.2f", amount);
        }
    }
    
    /**
     * Парсит отформатированную строку обратно в число
     * @param formattedText отформатированная строка (например, "123 456,79")
     * @return число в виде double
     * @throws ParseException если не удалось распарсить
     */
    public double parse(String formattedText) throws ParseException {
        if (currentLocale.getLanguage().equals("ru")) {
            formattedText = formattedText.replace(".", ",");
        }
        return formatter.parse(formattedText).doubleValue();
    }
    
    /**
     * Парсит отформатированную строку обратно в число (без исключений)
     * @param formattedText отформатированная строка
     * @return число в виде double или 0.0 если не удалось распарсить
     */
    public double parseSafe(String formattedText) {
        try {
            return parse(formattedText);
        } catch (ParseException e) {
            return 0.0;
        }
    }
    
    /**
     * Парсит отформатированную валютную строку обратно в число
     * @param formattedText отформатированная валютная строка (например, "123 456,79 ₽")
     * @return число в виде double
     * @throws ParseException если не удалось распарсить
     */
    public double parseCurrency(String formattedText) throws ParseException {
        return currencyFormatter.parse(formattedText).doubleValue();
    }
    
    /**
     * Парсит отформатированную валютную строку обратно в число (без исключений)
     * @param formattedText отформатированная валютная строка
     * @return число в виде double или 0.0 если не удалось распарсить
     */
    public double parseCurrencySafe(String formattedText) {
        try {
            return parseCurrency(formattedText);
        } catch (ParseException e) {
            return 0.0;
        }
    }
    
    /**
     * Преобразует отформатированную строку в простой числовой формат
     * @param formattedText отформатированная строка
     * @return строка в простом формате (например, "123456.79")
     */
    public String toPlainText(String formattedText) {
        try {
            double value = parse(formattedText);
            return String.valueOf(value);
        } catch (ParseException e) {
            // Если не удалось распарсить, пытаемся очистить от форматирования
            return formattedText.replaceAll("[\\s\\u00A0]", "").replace(",", ".");
        }
    }
    
    /**
     * Обновляет локаль форматтера
     * @param locale новая локаль
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        this.currentULocale = new ULocale(locale.toString().replace("_", "-"));
        initializeFormatters();
    }
    
    /**
     * Обновляет локаль форматтера по коду валюты
     * @param currencyCode код валюты
     */
    public void setCurrency(String currencyCode) {
        Locale locale = getLocaleForCurrency(currencyCode);
        setLocale(locale);
    }
    
    /**
     * Устанавливает минимальное количество знаков после запятой
     * @param minDigits минимальное количество знаков
     */
    public void setMinimumFractionDigits(int minDigits) {
        this.formatter.setMinimumFractionDigits(minDigits);
    }
    
    /**
     * Устанавливает максимальное количество знаков после запятой
     * @param maxDigits максимальное количество знаков
     */
    public void setMaximumFractionDigits(int maxDigits) {
        this.formatter.setMaximumFractionDigits(maxDigits);
    }
    
    /**
     * Получает текущую локаль форматтера
     * @return текущая локаль
     */
    public Locale getLocale() {
        return currentLocale;
    }
    
    /**
     * Получает текущую ULocale форматтера
     * @return текущая ULocale
     */
    public ULocale getULocale() {
        return currentULocale;
    }
}
