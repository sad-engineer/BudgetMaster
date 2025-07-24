# Правила организации строк в Android приложении

## Структура файлов строк

### 1. Общие файлы строк

#### `strings_toolbar.xml` - Общие строки тулбара
```xml
<resources>
    <!-- Заголовки тулбара -->
    <string name="toolbar_title_[screen_name]">Заголовок экрана</string>
    
    <!-- Общие описания иконок -->
    <string name="icon_back">Кнопка назад</string>
    <string name="icon_menu">Кнопка меню</string>
    <string name="icon_income_toolbar">Кнопка дохода</string>
    <string name="icon_expense_toolbar">Кнопка расхода</string>
</resources>
```

#### `strings_main.xml` - Главный экран
```xml
<resources>
    <!-- Названия разделов -->
    <string name="app_name">Название приложения</string>
    <string name="home">Главная</string>
    <string name="accounts">Счета</string>
    <string name="income">Доходы</string>
    <string name="expense">Расходы</string>
    <string name="budget">Бюджет</string>
    <string name="currencies">Валюты</string>
    <string name="settings">Настройки</string>
    
    <!-- Кнопки главного экрана -->
    <string name="btn_[action]">Текст кнопки</string>
    
    <!-- Описания иконок главного экрана -->
    <string name="icon_[element]">Описание иконки</string>
</resources>
```

#### `strings_menu.xml` - Навигационное меню
```xml
<resources>
    <!-- Пункты меню -->
    <string name="menu_[section]">Название раздела</string>
    <string name="menu_[action]">Действие</string>
</resources>
```

### 2. Специфичные файлы строк по экранам

#### `strings_accounts.xml` - Экран счетов
```xml
<resources>
    <!-- Названия вкладок -->
    <string name="tab_[type]">Название вкладки</string>
    
    <!-- Элементы списка -->
    <string name="[element]_example">Пример элемента</string>
    
    <!-- Кнопки действий -->
    <string name="icon_add_[type]">Добавить элемент</string>
    <string name="icon_delete_[type]">Удалить элемент</string>
</resources>
```

#### `strings_income_and_expense.xml` - Экраны доходов и расходов
```xml
<resources>
    <!-- Названия вкладок -->
    <string name="tab_[view]">Название вкладки</string>
    
    <!-- Кнопки действий -->
    <string name="icon_add_[type]">Добавить доход/расход</string>
    <string name="icon_delete_[type]">Удалить доход/расход</string>
</resources>
```

#### `strings_budget.xml` - Экран бюджета
```xml
<resources>
    <!-- Названия вкладок -->
    <string name="tab_[view]">Название вкладки</string>
    
    <!-- Кнопки действий -->
    <string name="icon_add_[type]">Добавить бюджет</string>
    <string name="icon_delete_[type]">Удалить бюджет</string>
</resources>
```

#### `strings_currency.xml` - Экран валют
```xml
<resources>
    <!-- Сообщения о пустых списках -->
    <string name="currencies_[type]_empty">Сообщение о пустом списке</string>
    
    <!-- Кнопки действий -->
    <string name="icon_add_[type]">Добавить валюту</string>
    <string name="icon_delete_[type]">Удалить валюту</string>
</resources>
```

#### `strings_version.xml` - Экран версии
```xml
<resources>
    <!-- Заголовки -->
    <string name="[section]_info_title">Заголовок раздела</string>
    <string name="[section]_info_description">Описание</string>
    
    <!-- Строки -->
    <string name="[type]_version">Версия компонента</string>
    
    <!-- Описания иконок -->
    <string name="icon_[component]">Описание иконки</string>
</resources>
```

#### `strings_authors.xml` - Экран авторов
```xml
<resources>
    <!-- Заголовки -->
    <string name="authors_info_title">Заголовок</string>
    <string name="authors_info_description">Описание</string>
    
    <!-- Строки -->
    <string name="authors_[section]_text">Текст раздела</string>
    <string name="authors_[section]_title">Заголовок раздела</string>
    <string name="authors_[section]_list">Список элементов</string>
    
    <!-- Описания иконок -->
    <string name="authors_icon_[element]">Описание иконки</string>
</resources>
```

## Конвенции именования

### 1. Префиксы по типам строк

#### Общие строки:
- `toolbar_title_[screen_name]` - заголовки тулбара
- `icon_[element]` - описания иконок
- `btn_[action]` - кнопки главного экрана
- `menu_[section]` - пункты меню

#### Специфичные строки:
- `tab_[type]` - названия вкладок
- `[element]_example` - примеры элементов
- `icon_add_[type]` - кнопки добавления
- `icon_delete_[type]` - кнопки удаления
- `[type]_[view]_empty` - сообщения о пустых списках
- `[section]_info_title` - заголовки разделов
- `[section]_info_description` - описания разделов
- `[type]_version` - версии компонентов
- `authors_[section]_[type]` - строки экрана авторов

### 2. Организация по файлам

#### Общие файлы:
- `strings_toolbar.xml` - общие строки тулбара
- `strings_main.xml` - главный экран
- `strings_menu.xml` - навигационное меню

#### Специфичные файлы:
- `strings_[screen_name].xml` - строки конкретного экрана
- `strings_[feature].xml` - строки функциональности

### 3. Структура комментариев

```xml
<resources>
    <!-- Заголовки -->
    <string name="[type]_title">Заголовок</string>
    
    <!-- Описания -->
    <string name="[type]_description">Описание</string>
    
    <!-- Кнопки -->
    <string name="btn_[action]">Кнопка</string>
    
    <!-- Описания иконок -->
    <string name="icon_[element]">Описание иконки</string>
    
    <!-- Сообщения -->
    <string name="[type]_message">Сообщение</string>
</resources>
```

## Правила организации

### 1. Принципы разделения

#### По функциональности:
- **Общие строки** - в `strings_toolbar.xml`, `strings_main.xml`, `strings_menu.xml`
- **Специфичные строки** - в отдельных файлах по экранам

#### По типу контента:
- **Заголовки** - `[type]_title`
- **Описания** - `[type]_description`
- **Кнопки** - `btn_[action]` или `icon_[action]`
- **Сообщения** - `[type]_message`
- **Примеры** - `[element]_example`

### 2. Иерархия файлов

```
strings_toolbar.xml     - Общие строки тулбара
strings_main.xml        - Главный экран
strings_menu.xml        - Навигационное меню
strings_accounts.xml    - Экран счетов
strings_income_and_expense.xml - Экраны доходов/расходов
strings_budget.xml      - Экран бюджета
strings_currency.xml    - Экран валют
strings_version.xml     - Экран версии
strings_authors.xml     - Экран авторов
```

### 3. Правила дублирования

#### Допустимое дублирование:
- Общие описания иконок (`icon_back`, `icon_menu`)
- Названия разделов в меню и главном экране

#### Недопустимое дублирование:
- Специфичные строки экранов
- Описания кнопок действий
- Сообщения об ошибках

## Проверочный список

### ✅ Обязательные проверки:
1.  [ ] Строки разделены по функциональности
2.  [ ] Используются правильные префиксы
3.  [ ] Комментарии структурированы
4.  [ ] Нет недопустимого дублирования
5.  [ ] Специфичные строки в отдельных файлах
6.  [ ] Общие строки в общих файлах
7.  [ ] Имена строк соответствуют конвенции
8.  [ ] Все строки имеют описательные имена
9.  [ ] Нет орфографических ошибок
10. [ ] Строки локализованы на русский язык

### 🔧 Типичные ошибки:
- Дублирование специфичных строк в разных файлах
- Неправильные префиксы для типов строк
- Отсутствие комментариев для группировки
- Смешивание общих и специфичных строк
- Неописательные имена строк
- Отсутствие локализации

## Примеры правильной организации

### ✅ Правильно:
```xml
<!-- strings_toolbar.xml -->
<string name="toolbar_title_accounts">Счета</string>
<string name="icon_back">Кнопка назад</string>

<!-- strings_accounts.xml -->
<string name="tab_current">Текущие</string>
<string name="icon_add_account">Добавить счет</string>
```

### ❌ Неправильно:
```xml
<!-- Дублирование в разных файлах -->
<string name="add_button">Добавить</string>

<!-- Неправильный префикс -->
<string name="button_back">Кнопка назад</string>

<!-- Отсутствие комментариев -->
<string name="title">Заголовок</string>
``` 