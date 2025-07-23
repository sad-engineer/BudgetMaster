# Правила форматирования тулбара в Android-приложении

## Общие принципы

### 1. Структура тулбара
Все тулбары должны следовать единой структуре:
- Использовать `androidx.appcompat.widget.Toolbar`
- Содержать внутренний `LinearLayout` с горизонтальной ориентацией
- Иметь правильные отступы и настройки

### 2. Базовые атрибуты тулбара

#### Обязательные атрибуты:
```xml
android:layout_height="@dimen/toolbar_height"
android:layout_marginTop="@dimen/toolbar_margin_top"
app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
app:contentInsetStart="0dp"
app:contentInsetEnd="0dp"
app:title=""
```

#### Запрещенные атрибуты:
- `android:theme="@style/ThemeOverlay.AppCompat.ActionBar"` - может переопределять цвета
- `android:layout_height="?attr/actionBarSize"` - использовать `@dimen/toolbar_height`

### 3. ID тулбара

#### Главный экран:
- ID: `@+id/toolbar`

#### Все остальные экраны:
- ID: `@+id/toolbar2`

### 4. Внутренний LinearLayout

#### Обязательные атрибуты:
```xml
android:gravity="center_vertical"
android:layout_height="match_parent"
android:layout_width="match_parent"
android:orientation="horizontal"
android:paddingStart="0dp"
android:paddingEnd="0dp"
```

## Типы тулбаров

### 1. Главный экран (MainActivity)
**Структура:** Меню → Заголовок → Доходы → Разрыв → Расходы

```xml
<ImageButton id="@+id/menu_button" />
<TextView id="@+id/toolbar_title" />
<ImageButton id="@+id/income_button" />
<View id="@+id/toolbar_gap" />
<ImageButton id="@+id/expense_button" />
```

### 2. Экраны с вкладками (Accounts, Budget, Income, Expense)
**Структура:** Назад → Меню → Заголовок → Доходы → Разрыв → Расходы

```xml
<ImageButton id="@+id/back_button" />
<ImageButton id="@+id/menu_button" />
<TextView id="@+id/toolbar_title" />
<ImageButton id="@+id/income_button" />
<View id="@+id/toolbar_gap" />
<ImageButton id="@+id/expense_button" />
```

### 3. Простые экраны (Currencies, Version, Authors)
**Структура:** Назад → Меню → Заголовок

```xml
<ImageButton id="@+id/back_button" />
<ImageButton id="@+id/menu_button" />
<TextView id="@+id/toolbar_title" />
```

### 4. Служебные экраны (Settings, BackendTest)
**Структура:** Назад → Меню → Заголовок
*Примечание: Эти экраны могут использовать старую структуру с темой*

## Цветовая схема

### 1. Цвета тулбара
Каждый экран должен иметь свой блок цветов в `colors.xml`:

```xml
<!-- Цвета для экрана [screen_name] -->
<color name="[screen_name]_toolbar_background">@color/black</color>
<color name="[screen_name]_toolbar_icons">@color/green</color>
<color name="[screen_name]_toolbar_texts">@color/white</color>
```

### 2. Применение цветов
- `android:background="@color/[screen_name]_toolbar_background"`
- `app:tint="@color/[screen_name]_toolbar_icons"` для иконок
- `android:textColor="@color/[screen_name]_toolbar_texts"` для текста

### 3. Исключения
- Settings и BackendTest используют белые иконки (`@color/white`)
- Остальные экраны используют зеленые иконки (`@color/green`)

## Размеры элементов

### 1. Кнопки
- `android:layout_width="@dimen/menu_button_size"`
- `android:layout_height="match_parent"`

### 2. Заголовок
- `android:layout_width="0dp"`
- `android:layout_weight="1"`
- `android:textSize="@dimen/toolbar_text"`
- `android:textStyle="bold"`

### 3. Разрыв (для экранов с вкладками)
- `android:layout_width="@dimen/toolbar_gap"`

## Иконки

### 1. Обязательные иконки
- `@drawable/ic_back` - кнопка назад
- `@drawable/ic_menu` - кнопка меню
- `@drawable/ic_income` - кнопка доходов
- `@drawable/ic_expense` - кнопка расходов

### 2. Настройки иконок
```xml
android:background="@android:color/transparent"
android:contentDescription="@string/icon_[name]"
app:tint="@color/[screen_name]_toolbar_icons"
```

## Обработчики кнопок

### 1. Кнопка "Назад"
**Назначение:** Возврат на главный экран
**Логика:** Завершение текущего Activity и переход на MainActivity

```java
backButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Возвращаемся на главный экран
        Intent intent = new Intent(CurrentActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
});
```

### 2. Кнопка "Меню"
**Назначение:** Открытие бокового меню или переход на главный экран
**Логика:** Если есть drawer layout - открыть его, иначе перейти на главный экран

```java
menuButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Открываем меню (если есть drawer layout) или идем на главный экран
        Intent intent = new Intent(CurrentActivity.this, MainActivity.class);
        startActivity(intent);
    }
});
```

### 3. Экраны с drawer layout
Для экранов с drawer layout (Version, Authors) кнопка "меню" открывает drawer:

```java
menuButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Открываем меню (если есть drawer layout)
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }
});
```

## Проверочный список

### ✅ Обязательные проверки:
1. [ ] Используется правильный ID тулбара
2. [ ] Убрана тема `ThemeOverlay.AppCompat.ActionBar`
3. [ ] Используется `@dimen/toolbar_height`
4. [ ] Добавлен `android:layout_marginTop="@dimen/toolbar_margin_top"`
5. [ ] Установлены `app:contentInsetStart="0dp"` и `app:contentInsetEnd="0dp"`
6. [ ] Внутренний LinearLayout имеет правильные отступы
7. [ ] Цвета ссылаются на палитру через `@color/[screen_name]_toolbar_*`
8. [ ] Иконки имеют правильный `app:tint`
9. [ ] Заголовок имеет правильные размеры и стиль
10. [ ] Кнопка "назад" ведет на главный экран
11. [ ] Кнопка "меню" открывает drawer или ведет на главный экран

### 🔧 Исправления для старых экранов:
- Settings и BackendTest требуют обновления структуры
- Убрать `android:theme` и `?attr/actionBarSize`
- Добавить правильные отступы и цвета
- Добавить обработчики кнопок "назад" и "меню"

## Примеры

### Правильный тулбар для простого экрана:
```xml
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar2"
    android:layout_width="match_parent"
    android:layout_height="@dimen/toolbar_height"
    android:layout_marginTop="@dimen/toolbar_margin_top"
    android:background="@color/screen_toolbar_background"
    android:elevation="4dp"
    app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
    app:contentInsetStart="0dp"
    app:contentInsetEnd="0dp"
    app:title="">

    <LinearLayout
        android:gravity="center_vertical"
        android:layout_height="match_parent"
        android:layout_width="match_parent"
        android:orientation="horizontal"
        android:paddingStart="0dp"
        android:paddingEnd="0dp">

        <ImageButton
            android:background="@android:color/transparent"
            android:contentDescription="@string/icon_back"
            android:id="@+id/back_button"
            android:layout_height="match_parent"
            android:layout_width="@dimen/menu_button_size"
            android:src="@drawable/ic_back"
            app:tint="@color/screen_toolbar_icons" />

        <ImageButton
            android:background="@android:color/transparent"
            android:contentDescription="@string/icon_menu"
            android:id="@+id/menu_button"
            android:layout_height="match_parent"
            android:layout_width="@dimen/menu_button_size"
            android:src="@drawable/ic_menu"
            app:tint="@color/screen_toolbar_icons" />

        <TextView
            android:gravity="center"
            android:id="@+id/toolbar_title"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:layout_width="0dp"
            android:text="@string/toolbar_title_screen"
            android:textColor="@color/screen_toolbar_texts"
            android:textSize="@dimen/toolbar_text"
            android:textStyle="bold" />

    </LinearLayout>

</androidx.appcompat.widget.Toolbar>
```

### Правильные обработчики кнопок:
```java
// Обработчики кнопок toolbar
ImageButton backButton = findViewById(R.id.back_button);
ImageButton menuButton = findViewById(R.id.menu_button);

backButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Возвращаемся на главный экран
        Intent intent = new Intent(CurrentActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
});

menuButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Открываем меню (если есть drawer layout) или идем на главный экран
        Intent intent = new Intent(CurrentActivity.this, MainActivity.class);
        startActivity(intent);
    }
});
``` 