package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.entity.BudgetSummary;

import java.util.List;

/**
 * Координатор финансовых расчетов
 */
public class FinancialCalculationCoordinator {

    private static final String TAG = "FinancialCalculationCoordinator";

    private final ServiceManager sm;

    public FinancialCalculationCoordinator(Context context, String userName) {
        this.sm = ServiceManager.getInstance(context, userName);
    }

    /**
     * Получить полную сводку по бюджету
     * 
     * @return сводка по бюджету
     */
    public BudgetSummary getBudgetSummary() {
    long totalAmount = 0L;

    CurrencyService currencyService = sm.currencies;
    BudgetService budgetService = sm.budgets;
    
    LiveData<List<Integer>> currencyIdsLiveData = currencyService.getAvalibleIds(EntityFilter.ACTIVE);
    List<Integer> currencyIds = currencyIdsLiveData.getValue();
    
    if (currencyIds == null || currencyIds.isEmpty()) {
        Log.d(TAG, "Список доступных ID валют пуст");
        return new BudgetSummary();
    }
    
    for (int currencyId : currencyIds) {
        LiveData<Long> amountLiveData = budgetService.getTotalAmountByCurrency(currencyId, EntityFilter.ACTIVE);
        Long amountValue = amountLiveData.getValue();
        if (amountValue != null) {
            totalAmount += amountValue;
        }
    }

    return new BudgetSummary(totalAmount);
}



















    // /**
    //  * Получить общую сумму на счетах по типу с конвертацией валют
    //  * Координирует работу AccountService и CurrencyService
    //  * 
    //  * @param type тип счета (1 - текущие, 2 - сбережения, 3 - кредитные)
    //  * @param filter фильтр для выборки счетов
    //  * @return сумма на счетах в базовой валюте
    //  */
    // public long getTotalAmountByTypeWithCurrencyConversion(int type, EntityFilter filter) {
    //     // Получаем сводку по валютам из AccountService
    //     List<KeyValuePair> summary = accounts.service().getCurrencySummaryByType(type, filter);
    //     if (summary == null || summary.isEmpty()) {
    //         Log.d(TAG, "Сводка по валютам пуста для типа: " + type);
    //         return 0L;
    //     }
        
    //     long totalAmount = 0;
    //     for (KeyValuePair pair : summary) {
    //         int currencyId = pair.getKey();
    //         long amount = pair.getValue();
            
    //         // Получаем курс валюты через CurrencyService
    //         double exchangeRate = currencies.service().getExchangeRateById(currencyId);
            
    //         // Конвертируем в базовую валюту
    //         BigDecimal amountBD = BigDecimal.valueOf(amount);
    //         BigDecimal rateBD = BigDecimal.valueOf(exchangeRate);
    //         long convertedAmount = amountBD.multiply(rateBD)
    //                 .setScale(0, RoundingMode.HALF_UP)
    //                 .longValue();
            
    //         totalAmount += convertedAmount;
    //     }
    //     return totalAmount;
    // }

    // /**
    //  * Создать операцию с автоматической конвертацией валют
    //  * Координирует работу OperationService, AccountService и CurrencyService
    //  * 
    //  * @param operation операция для создания
    //  * @param targetCurrencyId ID целевой валюты для конвертации
    //  * @return созданная операция
    //  */
    // public void createOperationWithCurrencyConversion(Operation operation, int targetCurrencyId) {
    //     // Здесь можно добавить логику конвертации валют перед созданием операции
    //     // Например, если операция в одной валюте, а счет в другой
        
    //     // Получаем валюту операции
    //     int operationCurrencyId = operation.getCurrencyId();
        
    //     if (operationCurrencyId != targetCurrencyId) {
    //         // Получаем курс конвертации
    //         double exchangeRate = currencies.service().getExchangeRateById(operationCurrencyId);
    //         double targetRate = currencies.service().getExchangeRateById(targetCurrencyId);
            
    //         // Конвертируем сумму
    //         long originalAmount = operation.getAmount();
    //         double conversionRate = targetRate / exchangeRate;
    //         long convertedAmount = BigDecimal.valueOf(originalAmount)
    //                 .multiply(BigDecimal.valueOf(conversionRate))
    //                 .setScale(0, RoundingMode.HALF_UP)
    //                 .longValue();
            
    //         operation.setAmount(convertedAmount);
    //         operation.setCurrencyId(targetCurrencyId);
    //     }
        
    //     // Создаем операцию
    //     operations.service().create(operation);
    // }

    // /**
    //  * Получить полную сводку по всем счетам с конвертацией валют
    //  * Координирует работу всех сервисов для создания комплексного отчета
    //  * 
    //  * @return сводка по всем счетам
    //  */
    // public AccountSummary getCompleteAccountSummary() {
    //     AccountSummary summary = new AccountSummary();
        
    //     // Получаем данные по типам счетов
    //     summary.setCurrentAccountsAmount(getTotalAmountByTypeWithCurrencyConversion(1, EntityFilter.ACTIVE));
    //     summary.setSavingsAccountsAmount(getTotalAmountByTypeWithCurrencyConversion(2, EntityFilter.ACTIVE));
    //     summary.setCreditAccountsAmount(getTotalAmountByTypeWithCurrencyConversion(3, EntityFilter.ACTIVE));
        
    //     // Получаем общую сумму
    //     summary.setTotalAmount(summary.getCurrentAccountsAmount() + 
    //                           summary.getSavingsAccountsAmount() + 
    //                           summary.getCreditAccountsAmount());
        
    //     return summary;
    // }
}