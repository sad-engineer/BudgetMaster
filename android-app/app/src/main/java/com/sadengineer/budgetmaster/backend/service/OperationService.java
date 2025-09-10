package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.calculators.OperationCalculatorConfig;
import com.sadengineer.budgetmaster.backend.repository.OperationRepository;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;
import com.sadengineer.budgetmaster.backend.repository.CategoryRepository;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.backend.validator.OperationValidator;
import com.sadengineer.budgetmaster.backend.interfaces.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Service класс для бизнес-логики работы с Operation
 */
public class OperationService implements IService<Operation> {

    private static final String TAG = "OperationService";
    
    private final OperationRepository repo;
    private final ExecutorService executorService;
    private final String user;

    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final CurrencyRepository currencyRepo;
    
    public OperationService(Context context, String user) {
        this.repo = new OperationRepository(context);
        this.accountRepo = new AccountRepository(context);
        this.categoryRepo = new CategoryRepository(context);
        this.currencyRepo = new CurrencyRepository(context);
        this.executorService = ThreadManager.getExecutor();
        this.user = user;
    }
    
    /**
     * Операция создания новой операции (с проверками значений)
     * @param type тип операции
     * @param date дата операции
     * @param amount сумма
     * @param comment комментарий
     * @param categoryId ID категории
     * @param accountId ID счета
     * @param currencyId ID валюты
     */
    public void create(Integer type, LocalDateTime date, Long amount, String comment, Integer categoryId, Integer accountId, Integer currencyId) {
        OperationValidator.validateType(type);
        OperationValidator.validateDate(date);
        OperationValidator.validateAmount(amount);
        OperationValidator.validateComment(comment);
        OperationValidator.validateCategoryId(categoryId, categoryRepo.getCount(EntityFilter.ALL));
        OperationValidator.validateAccountId(accountId, accountRepo.getCount(EntityFilter.ALL));
        OperationValidator.validateCurrencyId(currencyId, currencyRepo.getCount(EntityFilter.ALL));

        executorService.execute(() -> createOperationInTransaction(type, date, amount, comment,
                categoryId, accountId, currencyId));
    }   

    /**
     * Транзакция для создания новой операции
     * @param type тип операции
     * @param date дата операции
     * @param amount сумма
     * @param comment комментарий
     * @param categoryId ID категории
     * @param accountId ID счета
     * @param currencyId ID валюты
     */
    @Transaction
    private void createOperationInTransaction(int type, LocalDateTime date, long amount, String comment, int categoryId, int accountId, int currencyId) {
        Log.d(TAG, ServiceConstants.MSG_CREATE_OPERATION_REQUEST);
        Operation operation = new Operation();
        operation.setType(type);
        operation.setOperationDate(date);
        operation.setAmount(amount);
        operation.setDescription(comment);
        operation.setCategoryId(categoryId);
        operation.setAccountId(accountId);
        operation.setCurrencyId(currencyId);
        try {
        repo.insert(operation);
        Log.d(TAG, ServiceConstants.MSG_CREATE_OPERATION_SUCCESS);
        } catch (Exception e) {
            Log.e(TAG, ServiceConstants.MSG_CREATE_OPERATION_ERROR + e.getMessage(), e);
        }
    }

    /**
     * Создать новую операцию без проверок значений
     * @param type тип операции
     * @param date дата операции
     * @param amount сумма
     * @param comment комментарий
     */
    public void createWithoutValidation(int type, LocalDateTime date, long amount, String comment, int categoryId, int accountId, int currencyId) {
        executorService.execute(() -> createOperationInTransaction(type, date, amount, comment,
                categoryId, accountId, currencyId));
    }

    /**
     * Удалить операцию по условию
     * @param operation операция
     * @param softDelete Условие удаления: true - soft delete, false - полное удаление
     */
    public void delete(Operation operation, boolean softDelete) {
        if (operation == null) {
            Log.e(TAG, ServiceConstants.MSG_DELETE_OPERATION_NOT_FOUND);
            return;
        }
        if (softDelete) {
            softDelete(operation);
        } else {
            delete(operation);
        }
    }

    /**
     * Удалить операцию  (полное удаление - удаление строки из БД)
     * @param operation операция
     */
    private void delete(Operation operation) {
        executorService.execute(() -> deleteOperationInTransaction(operation));
    }

    /**
     * Транзакция для удаления операции
     * @param operation операция
     */
    @Transaction
    public void deleteOperationInTransaction(Operation operation) {
        Log.d(TAG, ServiceConstants.MSG_DELETE_OPERATION_REQUEST + getOperationText(operation));
        try {
            repo.delete(operation);
            Log.d(TAG, ServiceConstants.MSG_DELETE_OPERATION_SUCCESS + " " + getOperationText(operation));
        } catch (Exception e) {
            Log.e(TAG, String.format(ServiceConstants.MSG_DELETE_OPERATION_ERROR, getOperationText(operation)) + e.getMessage(), e);
        }
    }   

    /**
     * Получить все операции
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAll(EntityFilter filter) {
        return repo.getAll(filter);
    } 
    
    /**
     * Получить все операции
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAll() {
        return repo.getAll(EntityFilter.ALL);
    }
    /**
     * Получить все операции по типу
     * @param type тип операции
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAllByType(int type, EntityFilter filter) {
        return repo.getAllByType(type, filter);
    }

    /**
     * Получить все операции по счету
     * @param accountId ID счета
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAllByAccount(int accountId, EntityFilter filter) {
        return repo.getAllByAccount(accountId, filter);
    }

    /**
     * Получить все операции по счету (синхронно)
     * @param accountId ID счета
     * @param filter фильтр для выборки операций
     * @return список операций
     */
    public List<Operation> getAllByAccountSync(int accountId, EntityFilter filter) {
        return repo.getAllByAccountSync(accountId, filter);
    }

    /**
     * Получить все операции по категории
     * @param categoryId ID категории
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAllByCategory(int categoryId, EntityFilter filter) {
        return repo.getAllByCategory(categoryId, filter);
    }   

    /**
     * Получить все операции по валюте
     * @param currencyId ID валюты
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAllByCurrency(int currencyId, EntityFilter filter) {
        return repo.getAllByCurrency(currencyId, filter);
    }
    
    /**
     * Получить все операции по ID валюты (синхронно)
     * @param currencyId ID валюты
     * @param filter фильтр для выборки операций
     * @return список операций
     */
    public List<Operation> getAllByCurrencySync(int currencyId, EntityFilter filter) {
        return repo.getAllByCurrencySync(currencyId, filter);
    }
    
    /**
     * Получить все операции по дате
     * @param date дата
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAllByDate(LocalDateTime date, EntityFilter filter) {
        return repo.getAllByDate(date, filter);
    }

    /**
     * Получить все операции по месяцу
     * @param year год
     * @param month месяц
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAllByMonth(String year, String month, EntityFilter filter) {
        return repo.getAllByMonth(year, month, filter);
    }

    /**
     * Получить все операции по году
     * @param year год
     * @param filter фильтр для выборки операций
     * @return LiveData со списком всех операций
     */
    public LiveData<List<Operation>> getAllByYear(String year, EntityFilter filter) {
        return repo.getAllByYear(year, filter);
    }
    
    /**
     * Получает все операции по периоду с фильтром
     * @param startDate начало периода
     * @param endDate конец периода
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return repo.getAllByDateRange(startDate, endDate, filter);
    }
    
    /**
     * Получает операцию по ID
     * @param id ID операции
     * @return операция
     */
    public LiveData<Operation> getById(int id) {
        return repo.getById(id);
    }

    /**
     * Подсчет операций с фильтром
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций
     */
    public int getCount(EntityFilter filter) {
        return repo.count(filter);
    }

    /**
     * Подсчет операций
     * @return количество операций
     */ 
    public int getCount() {
        return repo.count(EntityFilter.ALL);
    }
    
    /**
     * Подсчет операций по типу с фильтром
     * @param type тип операции
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по типу
     */
    public int getCountByType(int type, EntityFilter filter) {
        return repo.countByType(type, filter);
    }

    /**
     * Подсчет операций по типу
     * @param type тип операции
     * @return количество операций по типу
     */
    public int getCountByType(int type) {
        return repo.countByType(type, EntityFilter.ALL);
    }
    
    /**
     * Подсчет операций по счету с фильтром
     * @param accountId ID счета
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по счету
     */
    public int getCountByAccount(int accountId, EntityFilter filter) {
        return repo.countByAccount(accountId, filter);
    }
    
    /**
     * Подсчет операций по категории с фильтром
     * @param categoryId ID категории
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по категории
     */
    public int getCountByCategory(int categoryId, EntityFilter filter) {
        return repo.countByCategory(categoryId, filter);
    }
    
    /**
     * Подсчет операций по валюте с фильтром
     * @param currencyId ID валюты
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по валюте
     */
    public int getCountByCurrency(int currencyId, EntityFilter filter) {
        return repo.countByCurrency(currencyId, filter);
    }
    
    /**
     * Подсчет операций за день с фильтром
     * @param date дата
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за день
     */
    public int getCountByDate(LocalDateTime date, EntityFilter filter) {
        return repo.countByDate(date, filter);
    }

    /**
     * Подсчет операций за месяц с фильтром
     * @param year год
     * @param month месяц
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за месяц 
     */
    public int getCountByMonth(String year, String month, EntityFilter filter) {
        return repo.countByMonth(year, month, filter);
    }
    
    /**
     * Подсчет операций за год с фильтром
     * @param year год
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за год
     */
    public int getCountByYear(String year, EntityFilter filter) {
        return repo.countByYear(year, filter);
    }

    /**
     * Подсчет операций за диапазон дат с фильтром
     * @param startDate начало диапазона
     * @param endDate конец диапазона
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за диапазон дат
     */
    public int getCountByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return repo.countByDateRange(startDate, endDate, filter);
    }

    /**
     * Получить текст операции для вывода в лог
     * @param operation операция
     * @return текст операции
     */
    private String getOperationText(Operation operation) {
        int type = operation.getType();
        LocalDateTime date = operation.getOperationDate();
        long amount = operation.getAmount();
        int categoryId = operation.getCategoryId();
        return "Тип: " + type + ", Дата: " + date + ", Сумма: " + amount + ", Категория: " + categoryId;
    }

    /**
     * Получает общую сумму операций по типу с фильтром
     * @param type тип операции
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по типу
     */
    public LiveData<Long> getTotalAmountByType(int type, EntityFilter filter) {
        return repo.getTotalAmountByType(type, filter);
    }

    /**
     * Получает общую сумму операций по счету с фильтром
     * @param accountId ID счета
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по счету
     */
    public LiveData<Long> getTotalAmountByAccount(int accountId, EntityFilter filter) {
        return repo.getTotalAmountByAccount(accountId, filter);
    }

    /**
     * Получает общую сумму операций по категории с фильтром
     * @param categoryId ID категории
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по категории
     */ 
    public LiveData<Long> getTotalAmountByCategory(int categoryId, EntityFilter filter) {
        return repo.getTotalAmountByCategory(categoryId, filter);
    }

    /**
     * Получает общую сумму операций по валюте с фильтром
     * @param currencyId ID валюты
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по валюте
     */
    public LiveData<Long> getTotalAmountByCurrency(int currencyId, EntityFilter filter) {
        return repo.getTotalAmountByCurrency(currencyId, filter);
    }

    

    /**
     * Восстановить удаленную операцию (soft delete)
     * @param deletedOperation удаленная операция
     */
    public void restore(Operation deletedOperation) {
        if (deletedOperation == null) {
            Log.e(TAG, ServiceConstants.MSG_RESTORE_OPERATION_NOT_FOUND);
            return;
        }
        executorService.execute(() -> restoreOperationInTransaction(deletedOperation));
    }
    
    /**
     * Транзакция для восстановления операции
     * @param deletedOperation удаленная операция
     */
    @Transaction
    private void restoreOperationInTransaction(Operation deletedOperation) {
        Log.d(TAG, ServiceConstants.MSG_RESTORE_OPERATION_REQUEST + getOperationText(deletedOperation));
        deletedOperation.setDeleteTime(null);
        deletedOperation.setDeletedBy(null);
        deletedOperation.setUpdateTime(LocalDateTime.now());
        deletedOperation.setUpdatedBy(user);
        try {
            repo.update(deletedOperation);
            Log.d(TAG, ServiceConstants.MSG_RESTORE_OPERATION_SUCCESS + getOperationText(deletedOperation));
        } catch (Exception e) {
            Log.e(TAG, String.format(ServiceConstants.MSG_RESTORE_OPERATION_ERROR, getOperationText(deletedOperation)) + e.getMessage(), e);
        }
    }

    /**
     * Удалить операцию (soft delete)
     * @param operation операция
     */
    private void softDelete(Operation operation) {
        executorService.execute(() -> softDeleteOperationInTransaction(operation));
    }

    /**
     * Транзакция для удаления операции (soft delete)
     * Операция не удаляется из БД, а только помечается как удаленная
     * @param operation операция
     */
    @Transaction
    public void softDeleteOperationInTransaction(Operation operation) {
        Log.d(TAG, ServiceConstants.MSG_SOFT_DELETE_OPERATION_REQUEST + getOperationText(operation));
        operation.setDeleteTime(LocalDateTime.now());
        operation.setDeletedBy(user);
        try {
            repo.update(operation);
            Log.d(TAG, ServiceConstants.MSG_SOFT_DELETE_OPERATION_SUCCESS + getOperationText(operation));
        } catch (Exception e) {
            Log.e(TAG, String.format(ServiceConstants.MSG_SOFT_DELETE_OPERATION_ERROR, getOperationText(operation)) + e.getMessage(), e);
        }
    }

    /**
     * Обновить операцию
     * @param operation операция
     */
    public void update(Operation operation) {
        if (operation == null) {
            Log.e(TAG, ServiceConstants.MSG_UPDATE_OPERATION_NOT_FOUND);
            return;
        }

        executorService.execute(() -> {
            try {
                Log.d(TAG, ServiceConstants.MSG_UPDATE_OPERATION_REQUEST + getOperationText(operation));
                operation.setUpdateTime(LocalDateTime.now());
                operation.setUpdatedBy(user);
                repo.update(operation);
                Log.d(TAG, ServiceConstants.MSG_UPDATE_OPERATION_SUCCESS + getOperationText(operation));
            } catch (Exception e) {
                Log.e(TAG, String.format(ServiceConstants.MSG_UPDATE_OPERATION_ERROR, getOperationText(operation)) + e.getMessage(), e);
            }
        });
    }

    /**
     * Получает сумму доходов за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @return сумма доходов за период
     */
    public LiveData<Long> getIncomeSumByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return repo.getIncomeSumByDateRange(startDate, endDate, EntityFilter.ACTIVE);
    }
    
    /**
     * Получает общую сумму операций по конфигурации
     * @param config конфигурация калькулятора операций
     * @return общая сумма операций
     */
    public LiveData<Long> getTotalAmountByConfig(OperationCalculatorConfig config) {
        // TODO: сделать валидатор для конфигурации
        if (config == null || !config.isValid()) {
            Log.e(TAG, "Invalid OperationCalculatorConfig provided");
            return new androidx.lifecycle.MutableLiveData<>(0L);
        }
        
        LocalDateTime startDate = config.getStartDate().atStartOfDay();
        LocalDateTime endDate = config.getEndDate().atTime(23, 59, 59);
        
        // Если указана категория, используем метод по категории
        if (config.getCategoryId() != null) {
            return repo.getTotalAmountByCategoryAndDateRange(
                config.getCategoryId(), 
                startDate, 
                endDate, 
                config.getCurrencyId(),
                config.getEntityFilter()
            );
        }
        
        // Если указан тип операций, используем метод по типу
        if (config.getOperationType() != OperationTypeFilter.ALL) {
            return repo.getTotalAmountByTypeAndDateRange(
                config.getOperationType().getIndex(), 
                startDate, 
                endDate, 
                config.getCurrencyId(),
                config.getEntityFilter()
            );
        }
        
        // Иначе используем общий метод по периоду
        return repo.getTotalAmountByDateRange(startDate, endDate, config.getCurrencyId(), config.getEntityFilter());
    }
    
    /**
     * Получает общую сумму операций по типу за период
     * @param type тип операций
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return общая сумма операций по типу за период
     */
        public LiveData<Long> getTotalAmountByTypeAndDateRange(int type,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate,
                                                           int currencyId,
                                                           EntityFilter entityFilter) {
        return repo.getTotalAmountByTypeAndDateRange(type, startDate, endDate, currencyId, entityFilter);
    }

    /**
     * Получить операции по типу и диапазону дат
     * @param type тип операции
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @param filter фильтр для выборки операций
     * @return LiveData со списком операций
     */
    public LiveData<List<Operation>> getByTypeAndDateRange(int type, LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return repo.getByTypeAndDateRange(type, startDate, endDate, filter);
    }
    
    /**
     * Получает общую сумму операций по категории за период
     * @param categoryId ID категории
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return общая сумма операций по категории за период
     */
    public LiveData<Long> getTotalAmountByCategoryAndDateRange(Integer categoryId, 
                                                             LocalDateTime startDate, 
                                                             LocalDateTime endDate, 
                                                             int currencyId,
                                                             EntityFilter entityFilter) {
        return repo.getTotalAmountByCategoryAndDateRange(categoryId, startDate, endDate, currencyId, entityFilter);
    }
    
    /**
     * Получает общую сумму операций за день
     * @param date дата 
     * @param operationType тип операций (ALL, INCOME, EXPENSE)
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей (ACTIVE, DELETED, ALL)
     * @return общая сумма операций за день
     */
    public LiveData<Long> getTotalAmountByDay(LocalDateTime date, 
                                             OperationTypeFilter operationType, 
                                             int currencyId,
                                             EntityFilter entityFilter) {
        LocalDateTime startDate = date.toLocalDate().atStartOfDay();
        LocalDateTime endDate = date.toLocalDate().atTime(23, 59, 59);
        
        if (operationType == OperationTypeFilter.ALL) {
            return repo.getTotalAmountByDateRange(startDate, endDate, currencyId, entityFilter);
        } else {
            return repo.getTotalAmountByTypeAndDateRange(operationType.getIndex(), startDate, endDate, currencyId, entityFilter);
        }
    }
    
    /**
     * Получает общую сумму операций за месяц
     * @param year год
     * @param month месяц
     * @param operationType тип операций (ALL, INCOME, EXPENSE)
     * @param entityFilter фильтр сущностей (ACTIVE, DELETED, ALL)
     * @return общая сумма операций за месяц
     */
    public LiveData<Long> getTotalAmountByMonth(String year, 
                                               String month, 
                                               OperationTypeFilter operationType, 
                                               EntityFilter entityFilter) {
        if (operationType == OperationTypeFilter.ALL) {
            return repo.getTotalAmountByMonth(year, month, entityFilter);
        } else {
            return repo.getTotalAmountByTypeAndMonth(operationType.getIndex(), year, month, entityFilter);
        }
    }
    
    /**
     * Получает общую сумму операций за год
     * @param year год
     * @param operationType тип операций (ALL, INCOME, EXPENSE)
     * @param entityFilter фильтр сущностей (ACTIVE, DELETED, ALL)
     * @return общая сумма операций за год
     */
    public LiveData<Long> getTotalAmountByYear(String year, 
                                              OperationTypeFilter operationType, 
                                              EntityFilter entityFilter) {
        if (operationType == OperationTypeFilter.ALL) {
            return repo.getTotalAmountByYear(year, entityFilter);
        } else {
            return repo.getTotalAmountByTypeAndYear(operationType.getIndex(), year, entityFilter);
        }
    }
    
    /**
     * Получает общую сумму операций за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей (ACTIVE, DELETED, ALL)  
     * @return общая сумма операций за период
     */
    public LiveData<Long> getTotalAmountByDateRange(LocalDateTime startDate, 
                                                   LocalDateTime endDate, 
                                                   int currencyId,
                                                   EntityFilter entityFilter) {
        return repo.getTotalAmountByDateRange(startDate, endDate, currencyId, entityFilter);
    }

} 