package com.sadengineer.budgetmaster.base;

import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

/**
 * Базовый Activity для отображения экранов с собственным контентом
 * (не списками карточек). Предоставляет общие элементы управления и навигации.
 */
public abstract class BaseContentActivity extends BaseNavigationActivity {

    protected final String TAG = getClass().getSimpleName();

    /**
     * Устанавливает заголовок тулбара
     * @param titleResId - ресурс строки для заголовка
     * @param textSizeResId - ресурс размера шрифта
     */
    protected void setToolbarTitle(int titleResId, int textSizeResId) {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleResId);
            Log.d(TAG, "Заголовок тулбара установлен");
            
            // Устанавливаем размер шрифта
            float textSize = getResources().getDimension(textSizeResId);
            toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            Log.d(TAG, "Размер шрифта установлен");
        }
    }
   
}
