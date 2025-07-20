package com.example.budgetmaster.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budgetmaster.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    
    private static final String TAG = "SettingsActivity";
    private TextView infoTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        infoTextView = findViewById(R.id.info_text_view);
        
        // Проверяем доступность моделей из backend.jar
        checkBackendModels();
    }
    
    private void checkBackendModels() {
        StringBuilder info = new StringBuilder();
        info.append("=== ПРОВЕРКА BACKEND.JAR ===\n\n");
        
        // Список классов для проверки
        String[] classesToCheck = {
            "model.Currency",
            "model.Account", 
            "model.Budget",
            "model.Category",
            "model.Operation",
            "repository.CurrencyRepository",
            "repository.AccountRepository",
            "repository.BudgetRepository",
            "repository.CategoryRepository", 
            "repository.OperationRepository",
            "service.CurrencyService",
            "service.AccountService",
            "service.BudgetService",
            "service.CategoryService",
            "service.OperationService"
        };
        
        List<String> availableClasses = new ArrayList<>();
        List<String> unavailableClasses = new ArrayList<>();
        
        for (String className : classesToCheck) {
            try {
                Class<?> clazz = Class.forName(className);
                availableClasses.add(className);
                
                // Проверяем конструкторы
                Constructor<?>[] constructors = clazz.getConstructors();
                info.append("✅ ").append(className).append("\n");
                info.append("   Конструкторы: ").append(constructors.length).append("\n");
                
                // Проверяем методы
                Method[] methods = clazz.getMethods();
                info.append("   Методы: ").append(methods.length).append("\n");
                
                // Показываем первые 5 методов
                int methodCount = Math.min(5, methods.length);
                for (int i = 0; i < methodCount; i++) {
                    info.append("     - ").append(methods[i].getName()).append("\n");
                }
                if (methods.length > 5) {
                    info.append("     ... и еще ").append(methods.length - 5).append(" методов\n");
                }
                info.append("\n");
                
            } catch (ClassNotFoundException e) {
                unavailableClasses.add(className);
                info.append("❌ ").append(className).append(" - НЕ НАЙДЕН\n\n");
            } catch (Exception e) {
                unavailableClasses.add(className);
                info.append("❌ ").append(className).append(" - ОШИБКА: ").append(e.getMessage()).append("\n\n");
            }
        }
        
        // Итоговая статистика
        info.append("=== ИТОГИ ===\n");
        info.append("Доступно классов: ").append(availableClasses.size()).append("\n");
        info.append("Недоступно классов: ").append(unavailableClasses.size()).append("\n");
        info.append("Всего проверено: ").append(classesToCheck.length).append("\n\n");
        
        if (!unavailableClasses.isEmpty()) {
            info.append("Недоступные классы:\n");
            for (String className : unavailableClasses) {
                info.append("- ").append(className).append("\n");
            }
        }
        
        // Отображаем информацию
        infoTextView.setText(info.toString());
        
        // Показываем Toast с результатом
        String result = "Найдено " + availableClasses.size() + " из " + classesToCheck.length + " классов";
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        
        Log.d(TAG, "Backend check completed: " + result);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
} 