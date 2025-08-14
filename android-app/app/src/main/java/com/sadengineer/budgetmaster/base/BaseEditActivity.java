package com.sadengineer.budgetmaster.base;

import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

/**
 * Базовый класс для экранов редактирования сущностей.
 * Предоставляет общие элементы управления: сохранить, отмена, изменение позиции (вверх/вниз),
 * а также хелперы для отображения ошибок и навигации.
 */
public abstract class BaseEditActivity<T> extends BaseNavigationActivity {

    protected final String TAG = getClass().getSimpleName();

    protected ImageButton saveButton;
    protected TextView toolbarTitle;

    /**
     * Инициализация общих кнопок. Любой из идентификаторов может быть null/0, если кнопки нет в layout.
     */
    protected void setupCommonEditActions(Integer saveButtonId) {
        if (saveButtonId != null && saveButtonId != 0) {
            saveButton = findViewById(saveButtonId);
            if (saveButton != null) {
                saveButton.setOnClickListener(v -> onSaveClicked());
            }
            //меняем иконку на сохранения
            saveButton.setImageResource(R.drawable.ic_save);
        }
    }

    /** Обработчик клика «Сохранить». */
    protected void onSaveClicked() {
        try {
            boolean success = validateAndSave();
            if (success) {
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при сохранении", e);
        }
    }

    /** Выполняет валидацию и сохранение. Должен быть реализован в наследнике. */
    protected abstract boolean validateAndSave();

    /**
     * Устанавливает заголовок тулбара
     * @param titleResId - ресурс строки для заголовка
     * @param textSizeResId - ресурс размера шрифта
     */
    protected void setToolbarTitle(int titleResId, int textSizeResId) {
        toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleResId);
            Log.d(TAG, "Заголовок тулбара установлен");
            
            // Устанавливаем размер шрифта
            float textSize = getResources().getDimension(textSizeResId);
            toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            Log.d(TAG, "Размер шрифта установлен");
        }
    }

    /** Показать ошибку у поля ввода. */
    protected void showFieldError(EditText editText, String message) {
        if (editText != null) {
            editText.setError(message);
            editText.requestFocus();
        }
    }

    /** Показать ошибку у спиннера: подсветить текущий выбранный элемент, если это TextView. */
    protected void showSpinnerError(Spinner spinner, String message) {
        if (spinner == null) return;
        View selected = spinner.getSelectedView();
        if (selected instanceof TextView) {
            TextView textView = (TextView) selected;
            textView.setError(message);
            textView.requestFocus();
        }
    }

    /** Навигация к другой активности с возвратом параметра. */
    protected void returnTo(Class<?> activityClass, String name, int value) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(name, value);
        startActivity(intent);
        finish();
    }

    /** Хелпер: отвязать фокус от поля ввода при касании других View (например, спиннеров). */
    protected void clearFocusOnTouch(EditText toClear, View... views) {
        if (toClear == null || views == null) return;
        for (View v : views) {
            if (v == null) continue;
            v.setOnTouchListener((view, event) -> {
                toClear.clearFocus();
                return false;
            });
        }
    }
}


