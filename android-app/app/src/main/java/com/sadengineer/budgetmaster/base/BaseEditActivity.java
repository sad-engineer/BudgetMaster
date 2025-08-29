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
    protected ImageButton positionChangeButton;

    /**
     * Инициализация общих кнопок. Любой из идентификаторов может быть null/0, если кнопки нет в layout.
     */
    protected void setupCommonEditActions(Integer positionChangeButtonId) {

        if (positionChangeButtonId != null && positionChangeButtonId != 0) {
            positionChangeButton = findViewById(positionChangeButtonId);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> onSaveClicked());
        }
        //меняем иконку на сохранения
        saveButton.setImageResource(R.drawable.ic_save);
        }
    }

    /** 
     * Унаследованный метод: Обработчик клика на кнопке «Сохранить». 
     * При нажатии на кнопку "Сохранить" для всех окон нужно 
     * выполнять валидацию введеных данных и оправить запрос на сохранение данных в БД
     * После чего закрываем текущую активность
     */
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

    /** 
     * Выполняет валидацию и сохранение. 
     * Должен быть реализован в наследнике. 
     * @return true - если сохранение прошло успешно, false - если есть ошибки
     * @throws UnsupportedOperationException - если метод не реализован в наследнике
     */
    protected boolean validateAndSave() {
        throw new UnsupportedOperationException(
            "Метод validateAndSave() должен быть реализован в наследнике"
        );
    }
    

    /** 
     * Показать ошибку у поля ввода. 
     * @param editText - поле ввода
     * @param message - сообщение об ошибке
     */
    protected void showFieldError(EditText editText, String message) {
        if (editText != null) {
            editText.setError(message);
            editText.requestFocus();
        }
    }

    /** 
     * Показать ошибку у спиннера: подсветить текущий выбранный элемент, если это TextView. 
     * @param spinner - спиннер
     * @param message - сообщение об ошибке
     */
    protected void showSpinnerError(Spinner spinner, String message) {
        if (spinner == null) return;
        View selected = spinner.getSelectedView();
        if (selected instanceof TextView) {
            TextView textView = (TextView) selected;
            textView.setError(message);
            textView.requestFocus();
        }
    }

    /** 
     * Навигация к другой активности с закрытием текущей активности. 
     * @param activityClass - класс активности
     * @param clearTop - флаг, определяющий, нужно ли очистить стек активностей
     * @param name - имя параметра
     * @param value - значение параметра
     */
    protected void returnTo(Class<?> activityClass, boolean clearTop, String name, Integer value) {
        goTo(activityClass, clearTop, name, value);
        //закрываем текущую активность
        finish();
    }

    /** 
     * Хелпер: отвязать фокус от поля ввода при касании других View (например, спиннеров). 
     * @param toClear - поле ввода
     * @param views - массив View
     */
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


