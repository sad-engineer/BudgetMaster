# Общие требования к блокам в Android Layout файлах

## Структура Activity

### 1. Корневой контейнер

#### Главный экран (MainActivity):
```xml
<androidx.drawerlayout.widget.DrawerLayout 
    android:id="@+id/drawer_layout"
    android:layout_height="match_parent"
    android:layout_width="match_parent">
    
    <androidx.constraintlayout.widget.ConstraintLayout 
        android:id="@+id/main"
        android:layout_height="match_parent"
        android:layout_width="match_parent">
        <!-- Содержимое -->
    </androidx.constraintlayout.widget.ConstraintLayout>
    
    <!-- NavigationView для drawer -->
</androidx.drawerlayout.widget.DrawerLayout>
```

#### Экраны с вкладками (Accounts, Budget, Income, Expense):
```xml
<androidx.drawerlayout.widget.DrawerLayout 
    android:id="@+id/drawer_layout"
    android:layout_height="match_parent"
    android:layout_width="match_parent">
    
    <androidx.constraintlayout.widget.ConstraintLayout 
        android:id="@+id/main"
        android:layout_height="match_parent"
        android:layout_width="match_parent">
        <!-- Содержимое -->
    </androidx.constraintlayout.widget.ConstraintLayout>
    
    <!-- NavigationView для drawer -->
</androidx.drawerlayout.widget.DrawerLayout>
```

#### Простые экраны с drawer (Version, Authors):
```xml
<androidx.drawerlayout.widget.DrawerLayout 
    android:id="@+id/drawer_layout"
    android:layout_height="match_parent"
    android:layout_width="match_parent">
    
    <LinearLayout
        android:layout_height="match_parent"
        android:layout_width="match_parent"
        android:orientation="vertical">
        <!-- Содержимое -->
    </LinearLayout>
    
    <!-- NavigationView для drawer -->
</androidx.drawerlayout.widget.DrawerLayout>
```

#### Простые экраны без drawer (Currencies, Settings, BackendTest):
```xml
<androidx.drawerlayout.widget.DrawerLayout 
    android:id="@+id/drawer_layout"
    android:layout_height="match_parent"
    android:layout_width="match_parent">
    
    <androidx.constraintlayout.widget.ConstraintLayout 
        android:id="@+id/main"
        android:layout_height="match_parent"
        android:layout_width="match_parent">
        <!-- Содержимое -->
    </androidx.constraintlayout.widget.ConstraintLayout>
    
    <!-- NavigationView для drawer -->
</androidx.drawerlayout.widget.DrawerLayout>
```

### 2. Блок тулбара

#### Обязательные атрибуты:
```xml
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar" или "@+id/toolbar2"
    android:layout_width="0dp"
    android:layout_height="@dimen/toolbar_height"
    android:layout_marginTop="@dimen/toolbar_margin_top"
    android:background="@color/[screen_name]_toolbar_background"
    android:elevation="4dp"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
    app:contentInsetStart="0dp"
    app:contentInsetEnd="0dp"
    app:title="">
```

#### Внутренний LinearLayout:
```xml
<LinearLayout
    android:gravity="center_vertical"
    android:layout_height="match_parent"
    android:layout_width="match_parent"
    android:orientation="horizontal"
    android:paddingStart="0dp"
    android:paddingEnd="0dp">
    <!-- Кнопки и заголовок -->
</LinearLayout>
```

### 3. Блок контента

#### Экраны с вкладками:
```xml
<androidx.constraintlayout.widget.ConstraintLayout 
    android:id="@+id/[screen_name]_content_bg"
    android:background="@color/[screen_name]_content_background"
    android:layout_height="0dp"
    android:layout_width="0dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toBottomOf="@id/toolbar2">
    
    <!-- TabLayout -->
    <com.google.android.material.tabs.TabLayout 
        android:id="@+id/[screen_name]_tab_layout"
        android:layout_height="wrap_content"
        android:layout_width="0dp"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    
    <!-- ViewPager2 -->
    <androidx.viewpager2.widget.ViewPager2 
        android:id="@+id/[screen_name]_view_pager"
        android:layout_height="0dp"
        android:layout_width="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@id/[screen_name]_tab_layout" />
    
    <!-- Кнопки внизу справа -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="16dp"
        android:layout_marginBottom="16dp"
        android:orientation="vertical"
        android:gravity="end"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent">
        
        <!-- Кнопка удаления (меньше) -->
        <ImageButton
            android:id="@+id/delete_[type]_button_bottom"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginBottom="8dp"
            android:background="@drawable/version_item_background"
            android:src="@drawable/ic_delete"
            app:tint="@color/[screen_name]_toolbar_icons" />
        
        <!-- Кнопка создания (больше) -->
        <ImageButton
            android:id="@+id/add_[type]_button_bottom"
            android:layout_width="76dp"
            android:layout_height="76dp"
            android:background="@drawable/version_item_background"
            android:src="@drawable/ic_add"
            app:tint="@color/[screen_name]_toolbar_icons" />
            
    </LinearLayout>
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### Простые экраны:
```xml
<androidx.constraintlayout.widget.ConstraintLayout 
    android:id="@+id/[screen_name]_content_bg"
    android:background="@color/[screen_name]_content_background"
    android:layout_height="0dp"
    android:layout_width="0dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toBottomOf="@id/toolbar2">
    
    <!-- RecyclerView или другой контент -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/[screen_name]_recycler_view"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:padding="8dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    
    <!-- Кнопки внизу справа -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="16dp"
        android:layout_marginBottom="16dp"
        android:orientation="vertical"
        android:gravity="end"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent">
        
        <!-- Кнопка удаления (меньше) -->
        <ImageButton
            android:id="@+id/delete_[type]_button_bottom"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginBottom="8dp"
            android:background="@drawable/version_item_background"
            android:src="@drawable/ic_delete"
            app:tint="@color/[screen_name]_toolbar_icons" />
        
        <!-- Кнопка создания (больше) -->
        <ImageButton
            android:id="@+id/add_[type]_button_bottom"
            android:layout_width="76dp"
            android:layout_height="76dp"
            android:background="@drawable/version_item_background"
            android:src="@drawable/ic_add"
            app:tint="@color/[screen_name]_toolbar_icons" />
            
    </LinearLayout>
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

## Структура фрагментов

### 1. Корневой контейнер фрагмента
```xml
<LinearLayout
    android:id="@+id/[screen_name]_[type]_fragment_container"
    android:background="@color/fragment_background"
    android:layout_height="match_parent"
    android:layout_width="match_parent"
    android:orientation="vertical">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/[screen_name]_[type]_recycler"
        android:layout_height="match_parent"
        android:layout_width="match_parent"/>

</LinearLayout>
```

## Обязательные ID для блоков

### 1. Корневые контейнеры
- **Главный экран**: `@+id/drawer_layout`, `@+id/main`
- **Экраны с вкладками**: `@+id/main`
- **Простые экраны с drawer**: `@+id/drawer_layout`, `@+id/main`
- **Простые экраны без drawer**: `@+id/main`

### 2. Тулбар
- **Тулбар**: `@+id/toolbar` или `@+id/toolbar2`
- **Контейнер тулбара**: `@+id/toolbar_container` (только главный экран)

### 3. Кнопки тулбара
- **Навигация**: `@+id/back_button`, `@+id/menu_button`
- **Действия**: `@+id/income_button`, `@+id/expense_button` (только главный экран)
- **Добавление/удаление**: `@+id/add_[type]_button`, `@+id/delete_[type]_button`
- **Пустые кнопки**: `@+id/empty_[screen_name]_button_1`, `@+id/empty_[screen_name]_button_2`

### 4. Заголовок
- **Заголовок**: `@+id/toolbar_title`

### 5. Контент
- **Контейнер контента**: `@+id/[screen_name]_content_bg`
- **Контейнер содержимого**: `@+id/[screen_name]_content_container`
- **Вкладки**: `@+id/[screen_name]_tab_layout`
- **ViewPager**: `@+id/[screen_name]_view_pager`
- **RecyclerView**: `@+id/[screen_name]_recycler_view`
- **ScrollView**: `@+id/[screen_name]_scroll_view`

### 6. Фрагменты
- **Контейнер фрагмента**: `@+id/[screen_name]_[type]_fragment_container`
- **RecyclerView фрагмента**: `@+id/[screen_name]_[type]_recycler`

## ID конвенции

### 1. Activity контейнеры
- `@+id/main` - главный контейнер (стандартный для всех экранов)
- `@+id/drawer_layout` - drawer layout (стандартный для экранов с drawer)
- `@+id/[screen_name]_content_bg` - контейнер контента
- `@+id/[screen_name]_content_container` - контейнер содержимого

### 2. Тулбар
- `@+id/toolbar` - главный экран и экраны с drawer
- `@+id/toolbar2` - остальные экраны
- `@+id/toolbar_container` - внутренний контейнер тулбара (только главный экран)

### 3. Кнопки тулбара
- `@+id/back_button` - кнопка назад
- `@+id/menu_button` - кнопка меню
- `@+id/income_button` - кнопка доходов (только главный экран)
- `@+id/expense_button` - кнопка расходов (только главный экран)
- `@+id/add_[type]_button` - кнопка добавления
- `@+id/delete_[type]_button` - кнопка удаления
- `@+id/empty_[screen_name]_button_1` - пустая кнопка 1
- `@+id/empty_[screen_name]_button_2` - пустая кнопка 2

### 4. Заголовок
- `@+id/toolbar_title` - заголовок тулбара

### 5. Вкладки и контент
- `@+id/[screen_name]_tab_layout` - TabLayout
- `@+id/[screen_name]_view_pager` - ViewPager2
- `@+id/[screen_name]_recycler_view` - RecyclerView
- `@+id/[screen_name]_scroll_view` - ScrollView

### 6. Фрагменты
- `@+id/[screen_name]_[type]_fragment_container` - контейнер фрагмента
- `@+id/[screen_name]_[type]_recycler` - RecyclerView фрагмента

### 7. Элементы списка
- `@+id/[type]_name` - название элемента
- `@+id/[type]_value` - значение элемента
- `@+id/[type]_position` - позиция элемента
- `@+id/[type]_id` - ID элемента

## Цветовая схема

Цветовая политика описана в android_colors_organization.md

## Размеры

### 1. Высота элементов
- `@dimen/toolbar_height` - высота тулбара
- `@dimen/menu_button_size` - размер кнопок меню
- `@dimen/income_button_size` - размер кнопки доходов
- `@dimen/expense_button_size` - размер кнопки расходов

### 2. Отступы
- `@dimen/toolbar_margin_top` - отступ тулбара сверху
- `@dimen/toolbar_gap` - разрыв в тулбаре (только главный экран)

### 3. Размеры текста
- `@dimen/toolbar_text` - размер текста тулбара
- `18sp` - размер текста элементов списка
- `16sp` - размер текста позиции
- `12sp` - размер текста ID

## Проверочный список

### ✅ Обязательные проверки:
1.  [ ] Используется правильный корневой контейнер
2.  [ ] Тулбар имеет правильные атрибуты
3.  [ ] ID соответствуют конвенции
4.  [ ] Все блоки имеют обязательные ID
5.  [ ] Размеры используют `@dimen/*`
6.  [ ] Контент имеет правильную структуру
7.  [ ] Фрагменты используют `fragment_background`
8.  [ ] Элементы списка имеют правильную высоту
9.  [ ] Отступы соответствуют дизайну
10. [ ] Все ID уникальны в пределах файла
