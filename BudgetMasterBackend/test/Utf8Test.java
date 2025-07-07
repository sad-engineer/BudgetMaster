package test;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Utf8Test {
    public static void main(String[] args) {
        System.out.println("=== Тест UTF-8 вывода в консоль ===");
        
        // Тест 1: Обычный вывод через System.out
        System.out.println("1. Обычный вывод System.out:");
        System.out.println("   Привет, мир! Hello, world!");
        System.out.println("   Рубль, Доллар, Евро");
        System.out.println("   Счёт, Категория, Бюджет");
        
        // Тест 2: Вывод через PrintWriter с UTF-8
        System.out.println("\n2. PrintWriter с UTF-8:");
        try {
            PrintWriter out = new PrintWriter(new java.io.OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
            out.println("   Привет, мир! Hello, world!");
            out.println("   Рубль, Доллар, Евро");
            out.println("   Счёт, Категория, Бюджет");
        } catch (Exception e) {
            System.err.println("Ошибка PrintWriter: " + e.getMessage());
        }
        
        // Тест 3: Вывод через System.err
        System.err.println("\n3. Вывод через System.err:");
        System.err.println("   Привет, мир! Hello, world!");
        System.err.println("   Рубль, Доллар, Евро");
        System.err.println("   Счёт, Категория, Бюджет");
        
        // Тест 4: Вывод с эскейп-последовательностями
        System.out.println("\n4. Unicode escape sequences:");
        System.out.println("   \\u0420\\u0443\\u0431\\u043b\\u044c = " + "\u0420\u0443\u0431\u043b\u044c");
        System.out.println("   \\u0414\\u043e\\u043b\\u043b\\u0430\\u0440 = " + "\u0414\u043e\u043b\u043b\u0430\u0440");
        System.out.println("   \\u0415\\u0432\\u0440\\u043e = " + "\u0415\u0432\u0440\u043e");
        
        // Тест 5: Проверка кодировки системы
        System.out.println("\n5. Информация о кодировке:");
        System.out.println("   file.encoding: " + System.getProperty("file.encoding"));
        System.out.println("   sun.jnu.encoding: " + System.getProperty("sun.jnu.encoding"));
        System.out.println("   console.encoding: " + System.getProperty("console.encoding"));
        
        // Тест 6: Вывод байтов UTF-8
        System.out.println("\n6. Байты UTF-8:");
        String testString = "Рубль";
        byte[] bytes = testString.getBytes(StandardCharsets.UTF_8);
        System.out.println("   Строка: " + testString);
        System.out.println("   Байты UTF-8: ");
        for (byte b : bytes) {
            System.out.printf("   %02X ", b & 0xFF);
        }
        System.out.println();
        
        // Тест 7: Создание строки из байтов
        System.out.println("\n7. Создание строки из байтов:");
        String reconstructed = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("   Восстановленная строка: " + reconstructed);
        
        System.out.println("\n=== Тест завершен ===");
    }
} 