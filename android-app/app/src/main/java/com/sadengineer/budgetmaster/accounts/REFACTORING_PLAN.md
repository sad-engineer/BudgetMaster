# План рефакторинга модуля Accounts

## 🎯 Цель рефакторинга
Упростить архитектуру, улучшить читаемость кода и следовать принципу разделения ответственности (Single Responsibility Principle).

## 📊 Текущие проблемы

### 1. AccountsSharedViewModel (412 строк)
**Проблемы:**
- Слишком много ответственности в одном классе
- Смешивает UI логику с бизнес-логикой
- Сложно тестировать
- Нарушает принцип единственной ответственности

**Текущие функции:**
- ✅ Режим множественного выбора
- ✅ Мягкое удаление счетов
- ✅ Кэширование валют
- ✅ CRUD операции
- ✅ Калькуляторы для 3 типов счетов
- ✅ Фоновые операции
- ✅ Навигация

### 2. AccountsEditActivity (429 строк)
**Проблемы:**
- Слишком много кода в одной активности
- Смешивает UI, валидацию и бизнес-логику
- Сложно поддерживать

### 3. BaseAccountsFragment (192 строки)
**Проблемы:**
- Сложная иерархия наследования
- Слишком много абстракций
- Наследует от BaseListFragmentN с 4 generic параметрами

### 4. BaseListFragmentN (198 строк)
**Проблемы:**
- Слишком сложная generic типизация: `BaseListFragmentN<T, A, V, S>`
- Смешивает ответственности: UI + управление данными + бизнес-логика
- Сложно тестировать из-за множества зависимостей

**Примечание:** Жесткая привязка к BaseNavigationActivity НЕ является проблемой, так как это стандартная навигация для всего приложения.

## 🏗️ Предлагаемая новая архитектура

### Этап 1: Создание Use Cases (БЕЗ Repository)
```
📁 usecase/
├── CreateAccountUseCase.java
├── UpdateAccountUseCase.java
├── DeleteAccountsUseCase.java
├── LoadAccountsUseCase.java
└── ValidateAccountUseCase.java
```

**Ответственность:**
- Инкапсуляция бизнес-логики из сервисов
- Переиспользование логики между ViewModels
- Легкое тестирование без дублирования

### Этап 2: Упрощение базовых классов
```
📁 base/
├── SimpleListFragment.java (упрощенная версия)
└── ListViewModel.java (базовый ViewModel)
```

**Проблемы BaseListFragmentN:**
- 4 generic параметра: `BaseListFragmentN<T, A, V, S>`
- Смешивает UI + управление данными + бизнес-логику
- Слишком много абстракций для простых случаев

**Решение:**
- Упростить до 2 generic параметров: `SimpleListFragment<T, V>`
- Сохранить BaseNavigationActivity как стандартную навигацию
- Разделить ответственности между UI и данными

### Этап 3: Разделение ViewModel
```
📁 viewmodel/
├── AccountsListViewModel.java
├── AccountsSelectionViewModel.java
├── AccountsEditViewModel.java
├── CurrencyCacheViewModel.java
└── AccountsCalculatorViewModel.java
```

**Разделение ответственности:**
- **AccountsListViewModel** - только отображение списка
- **AccountsSelectionViewModel** - только режим выбора и удаление
- **AccountsEditViewModel** - только редактирование
- **CurrencyCacheViewModel** - только кэширование валют
- **AccountsCalculatorViewModel** - только вычисления

### Этап 4: Упрощение UI слоя
```
📁 ui/
├── AccountsActivity.java (упрощенная)
├── AccountEditFragment.java (новый)
├── AccountEditViewModel.java (новый)
├── SimpleAccountsFragment.java (новый, без сложного наследования)
└── AccountsAdapter.java (упрощенный)
```

### Этап 5: Интеграция с существующими сервисами
```
📁 integration/
├── ServiceManagerIntegration.java
├── CurrencyCacheIntegration.java
└── ThreadManagerIntegration.java
```

**Ответственность:**
- Использование существующих сервисов
- Интеграция с ServiceManager
- Управление фоновыми операциями

## 📋 Детальный план выполнения

### Фаза 1: Подготовка 
- [ ] Создать папки для новой структуры
- [ ] Создать интерфейсы Use Cases
- [ ] Создать базовые Use Cases

### Фаза 2: Упрощение базовых классов 
- [ ] Создать SimpleListFragment с упрощенной типизацией
- [ ] Создать базовый ListViewModel
- [ ] Мигрировать BaseAccountsFragment на новую архитектуру
- [ ] Сохранить BaseNavigationActivity как стандартную навигацию

### Фаза 3: Use Cases слой 
- [ ] Реализовать все Use Cases
- [ ] Интегрировать с существующими сервисами
- [ ] Добавить unit тесты для Use Cases

### Фаза 4: Разделение ViewModel 
- [ ] Создать AccountsListViewModel
- [ ] Создать AccountsSelectionViewModel
- [ ] Создать AccountsEditViewModel
- [ ] Создать CurrencyCacheViewModel
- [ ] Создать AccountsCalculatorViewModel

### Фаза 5: Рефакторинг UI 
- [ ] Упростить AccountsActivity
- [ ] Разбить AccountsEditActivity на Fragment + ViewModel
- [ ] Создать SimpleAccountsFragment (замена BaseAccountsFragment)
- [ ] Обновить AccountsAdapter

### Фаза 6: Тестирование и оптимизация  
- [ ] Интеграционные тесты
- [ ] Performance тесты
- [ ] Исправление багов
- [ ] Документация

## 📊 Ожидаемые результаты

### До рефакторинга:
- AccountsSharedViewModel: **412 строк**
- AccountsEditActivity: **429 строк**
- Сложная иерархия наследования
- Смешанная ответственность

### После рефакторинга:
- 5 ViewModel по **~80 строк каждая**
- AccountEditFragment + ViewModel по **~150 строк**
- Простые, независимые классы
- Четкое разделение задач

## 🔍 Детальный анализ BaseListFragmentN

### Текущие проблемы:

#### 1. **Слишком сложная типизация:**
```java
// СЛОЖНО: 4 generic параметра
BaseListFragmentN<T extends Serializable, 
                  A extends RecyclerView.Adapter & ISelectionAdapter<T>,
                  V extends ViewModel, 
                  S extends IService<T>>
```

#### 2. **Нарушение принципа единственной ответственности:**
```java
// BaseListFragmentN делает ВСЕ:
- Управление RecyclerView (UI)
- Работа с ViewModel (данные) 
- Управление адаптером (логика)
- Загрузка данных (бизнес-логика)
// Навигация через BaseNavigationActivity - это НОРМАЛЬНО
```

#### 3. **Сложность тестирования:**
- Множество зависимостей
- Смешанные ответственности
- Сложная типизация

### Предлагаемое решение:

#### 1. **Упрощенная типизация:**
```java
// ПРОСТО: только 2 generic параметра
SimpleListFragment<T, V extends ViewModel>
```

#### 2. **Разделение ответственности:**
```java
// Каждый класс отвечает за одну вещь:
SimpleListFragment<T, V>     // Только UI и RecyclerView
ListViewModel<T>            // Только данные
Use Cases                   // Только бизнес-логика
```

#### 3. **Сохранение стандартной навигации:**
```java
// ХОРОШО: сохраняем BaseNavigationActivity как стандарт
protected void goToEdit(T item, Class<?> editActivityClass) {
    if (getActivity() instanceof BaseNavigationActivity) {
        BaseNavigationActivity navActivity = (BaseNavigationActivity) getActivity();
        navActivity.goTo(editActivityClass, false, item, getSourceTab());
    }
}
```

### 4. **Пример упрощенного фрагмента:**
```java
// ДО: BaseAccountsFragment extends BaseListFragmentN<Account, AccountsAdapter, AccountsSharedViewModel, AccountService>
public abstract class BaseAccountsFragment extends BaseListFragmentN<Account,
        AccountsAdapter, AccountsSharedViewModel, AccountService> {
    // 192 строки сложного кода
}

// ПОСЛЕ: SimpleAccountsFragment
public class CurrentAccountsFragment extends SimpleListFragment<Account, AccountsListViewModel> {
    
    @Override
    protected void setupAdapter() {
        AccountsAdapter adapter = new AccountsAdapter(
            account -> goToEdit(account, AccountsEditActivity.class),
            currencyId -> viewModel.getCurrencyShortName(currencyId)
        );
        recyclerView.setAdapter(adapter);
    }
    
    @Override
    protected void loadData() {
        viewModel.getCurrentAccounts().observe(getViewLifecycleOwner(), accounts -> {
            // Обновление адаптера
        });
    }
}
```

## 🎯 Преимущества новой архитектуры

### 1. Тестируемость
- Каждый класс тестируется отдельно
- Mock объекты легко создавать
- Unit тесты изолированы

### 2. Переиспользование
- Use Cases можно использовать в других модулях
- ViewModel логика не привязана к конкретному UI
- Простые фрагменты легко адаптировать

### 3. Читаемость
- Код легче понимать
- Четкие границы ответственности
- Простая навигация по коду

### 4. Расширяемость
- Новые функции добавляются проще
- Минимальное влияние на существующий код
- Гибкая архитектура

### 5. Поддерживаемость
- Легче находить и исправлять баги
- Изменения локализованы
- Меньше побочных эффектов

## ⚠️ Риски и митигация

### Риски:
1. **Временные затраты** - рефакторинг займет 2-3 недели
2. **Временные баги** - возможны проблемы при переходе
3. **Сложность миграции** - нужно аккуратно переносить логику

### Митигация:
1. **Поэтапный подход** - рефакторинг по фазам
2. **Сохранение старого кода** - до полного тестирования
3. **Комплексное тестирование** - на каждом этапе

