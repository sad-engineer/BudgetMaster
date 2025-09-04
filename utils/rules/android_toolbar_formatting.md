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

#### Главный экран и экраны с drawer layout:
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

### 1. Главный экран (StartActivity)
**Структура:** Меню → Заголовок → Доходы → Расходы

```xml
<ImageButton id="@+id/menu_button" />
<TextView id="@+id/toolbar_title" />
<ImageButton id="@+id/income_button" />
<ImageButton id="@+id/expense_button" />
```

**Особенности:**
- Использует ID `@+id/toolbar`
- Имеет внутренний контейнер `@+id/toolbar_container`
- Кнопки доход/расход остаются неизменными

### 2. Экраны с вкладками (Accounts, Budget, Income, Expense)
**Структура:** Назад → Меню → Заголовок → Пустое место → Изменить позицию

```xml
<ImageButton id="@+id/back_button" />
<ImageButton id="@+id/menu_button" />
<TextView id="@+id/toolbar_title" />
<View /> <!-- Пустое место (резерв на будущее) -->
<ImageButton id="@+id/position_change_button" />
```

**Особенности:**
- Кнопки добавления и удаления перемещены вниз экрана справа
- Пустое место зарезервировано для будущих функций
- Кнопка изменения позиции использует иконку `ic_position_change`

### 3. Простые экраны с действиями (Currencies)
**Структура:** Назад → Меню → Заголовок → Пустое место → Изменить позицию

```xml
<ImageButton id="@+id/back_button" />
<ImageButton id="@+id/menu_button" />
<TextView id="@+id/toolbar_title" />
<View /> <!-- Пустое место (резерв на будущее) -->
<ImageButton id="@+id/position_change_button" />
```

**Особенности:**
- Кнопки добавления и удаления перемещены вниз экрана справа
- Пустое место зарезервировано для будущих функций
- Кнопка изменения позиции использует иконку `ic_position_change`

### 4. Простые экраны без действий (Version, Authors, Settings, BackendTest)
**Структура:** Назад → Меню → Заголовок → Пустая кнопка → Пустая кнопка

```xml
<ImageButton id="@+id/back_button" />
<ImageButton id="@+id/menu_button" />
<TextView id="@+id/toolbar_title" />
<ImageButton id="@+id/empty_button_1" />
<ImageButton id="@+id/empty_button_2" />
```

**Особенности:**
- Используют ID `@+id/toolbar` (с drawer layout)
- Имеют две пустые кнопки для центрирования заголовка
- Пустые кнопки невидимы (`android:visibility="invisible"`)
- Размер пустых кнопок: `@dimen/menu_button_size`


## Цветовая схема

Цветовая политика описана в android_colors_organization.md

### 1. Применение цветов
- `android:background="@color/[screen_name]_toolbar_background"`
- `app:tint="@color/[screen_name]_toolbar_icons"` для иконок
- `android:textColor="@color/[screen_name]_toolbar_texts"` для текста

## Размеры элементов

### 1. Кнопки
- `android:layout_width="@dimen/menu_button_size"`
- `android:layout_height="match_parent"`

### 2. Заголовок
- `android:layout_width="0dp"`
- `android:layout_weight="1"`
- `android:textSize="@dimen/toolbar_text"`
- `android:textStyle="bold"`


## Иконки

### 1. Обязательные иконки
- `@drawable/ic_back` - кнопка назад
- `@drawable/ic_menu` - кнопка меню
- `@drawable/ic_income` - кнопка доходов (только главный экран)
- `@drawable/ic_expense` - кнопка расходов (только главный экран)
- `@drawable/ic_add` - кнопка добавления
- `@drawable/ic_delete` - кнопка удаления

### 2. Настройки иконок
```xml
android:background="@android:color/transparent"
android:contentDescription="@string/icon_[name]"
app:tint="@color/[screen_name]_toolbar_icons"
```

## Строки для contentDescription

### 1. Общие строки в `strings_toolbar.xml`:
```xml
<string name="icon_back">Кнопка назад</string>
<string name="icon_menu">Кнопка меню</string>
<string name="icon_income_toolbar">Кнопка дохода</string>
<string name="icon_expense_toolbar">Кнопка расхода</string>
```

### 2. Строки по экранам:
- **Accounts**: `strings_accounts.xml` - `icon_add_account`, `icon_delete_account`
- **Income/Expense**: `strings_income_and_expense.xml` - `icon_add_income`, `icon_delete_income`, `icon_add_expense`, `icon_delete_expense`
- **Budget**: `strings_budget.xml` - `icon_add_budget`, `icon_delete_budget`
- **Currencies**: `strings_currency.xml` - `icon_add_currency`, `icon_delete_currency`

## Обработчики кнопок

### 1. Кнопка "Назад"
**Назначение:** Возврат на главный экран
**Логика:** Завершение текущего Activity и переход на StartActivity

```java
backButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Возвращаемся на главный экран
        Intent intent = new Intent(CurrentActivity.this, StartActivity.class);
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
        Intent intent = new Intent(CurrentActivity.this, StartActivity.class);
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

### 4. Кнопки действий
**Назначение:** Добавление/удаление элементов
**Логика:** Показ Toast сообщения с TODO комментарием

```java
addButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // TODO: Реализовать добавление элемента
        android.widget.Toast.makeText(CurrentActivity.this, "Добавить элемент", android.widget.Toast.LENGTH_SHORT).show();
    }
});

deleteButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // TODO: Реализовать удаление элемента
        android.widget.Toast.makeText(CurrentActivity.this, "Удалить элемент", android.widget.Toast.LENGTH_SHORT).show();
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
12. [ ] Кнопки действий показывают соответствующие Toast сообщения
13. [ ] Все contentDescription используют строки из `strings_toolbar.xml`


## Примеры

### Правильный тулбар для экрана с действиями:
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

        <ImageButton
            android:background="@android:color/transparent"
            android:contentDescription="@string/icon_add_item"
            android:id="@+id/add_item_button"
            android:layout_height="match_parent"
            android:layout_width="@dimen/menu_button_size"
            android:src="@drawable/ic_add"
            app:tint="@color/screen_toolbar_icons" />

        <ImageButton
            android:background="@android:color/transparent"
            android:contentDescription="@string/icon_delete_item"
            android:id="@+id/delete_item_button"
            android:layout_height="match_parent"
            android:layout_width="@dimen/menu_button_size"
            android:src="@drawable/ic_delete"
            app:tint="@color/screen_toolbar_icons" />

    </LinearLayout>

</androidx.appcompat.widget.Toolbar>
```

### Правильные обработчики кнопок:
```java
// Обработчики кнопок toolbar
ImageButton backButton = findViewById(R.id.back_button);
ImageButton menuButton = findViewById(R.id.menu_button);
ImageButton addButton = findViewById(R.id.add_item_button);
ImageButton deleteButton = findViewById(R.id.delete_item_button);

backButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Возвращаемся на главный экран
        Intent intent = new Intent(CurrentActivity.this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
});

menuButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Открываем меню (если есть drawer layout) или идем на главный экран
        Intent intent = new Intent(CurrentActivity.this, StartActivity.class);
        startActivity(intent);
    }
});

addButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // TODO: Реализовать добавление элемента
        android.widget.Toast.makeText(CurrentActivity.this, "Добавить элемент", android.widget.Toast.LENGTH_SHORT).show();
    }
});

deleteButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // TODO: Реализовать удаление элемента
        android.widget.Toast.makeText(CurrentActivity.this, "Удалить элемент", android.widget.Toast.LENGTH_SHORT).show();
    }
});
``` 