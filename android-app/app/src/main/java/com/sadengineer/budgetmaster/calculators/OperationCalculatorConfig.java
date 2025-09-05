package com.sadengineer.budgetmaster.calculators;

import com.sadengineer.budgetmaster.backend.filters.OperationPeriod;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

import java.time.LocalDate;

/**
 * Конфигурация для калькулятора операций
 */
public class OperationCalculatorConfig {
    
    private OperationPeriod period;
    private LocalDate baseDate;
    private OperationTypeFilter operationType;
    private Integer categoryId;
    private int currencyId;
    private EntityFilter entityFilter;
    
    /**
     * Конструктор по умолчанию
     */
    public OperationCalculatorConfig() {
        this.period = OperationPeriod.MONTH;
        this.baseDate = LocalDate.now();
        this.operationType = OperationTypeFilter.ALL;
        this.categoryId = null;
        this.currencyId = ModelConstants.DEFAULT_CURRENCY_ID; // ID валюты по умолчанию
        this.entityFilter = EntityFilter.ACTIVE;
    }
    
    /**
     * Конструктор с параметрами
     * @param period период расчета
     * @param baseDate базовая дата для расчета
     * @param operationType тип операций
     * @param categoryId ID категории (null = все категории)
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     */
    public OperationCalculatorConfig(OperationPeriod period, 
                                   LocalDate baseDate, 
                                   OperationTypeFilter operationType, 
                                   Integer categoryId, 
                                   int currencyId,
                                   EntityFilter entityFilter) {
        this.period = period;
        this.baseDate = baseDate;
        this.operationType = operationType;
        this.categoryId = categoryId;
        this.currencyId = currencyId;
        this.entityFilter = entityFilter;
    }
    
    /**
     * Создать конфигурацию для расчета за день
     * @param date конкретная дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для дня
     */
    public static OperationCalculatorConfig forDay(LocalDate date, 
                                                  OperationTypeFilter operationType, 
                                                  int currencyId,
                                                  EntityFilter entityFilter) {
        return new OperationCalculatorConfig(
            OperationPeriod.DAY, 
            date, 
            operationType, 
            null, 
            currencyId,
            entityFilter
        );
    }        
    
    /**
     * Создать конфигурацию для расчета за месяц
     * @param baseDate базовая дата (месяц будет рассчитан от неё)
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для месяца
     */
    public static OperationCalculatorConfig forMonth(LocalDate baseDate, 
                                                    OperationTypeFilter operationType, 
                                                    int currencyId,
                                                    EntityFilter entityFilter) {
        return new OperationCalculatorConfig(
            OperationPeriod.MONTH, 
            baseDate, 
            operationType, 
            null, 
            currencyId,
            entityFilter
        );
    }
    
    /**
     * Создать конфигурацию для расчета по категории за месяц
     * @param baseDate базовая дата (месяц будет рассчитан от неё)
     * @param categoryId ID категории
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для категории за месяц
     */
    public static OperationCalculatorConfig forCategoryByMonth(LocalDate baseDate, 
                                                              Integer categoryId, 
                                                              int currencyId,
                                                              EntityFilter entityFilter) {
        return new OperationCalculatorConfig(
            OperationPeriod.MONTH, 
            baseDate, 
            OperationTypeFilter.ALL, 
            categoryId, 
            currencyId,
            entityFilter
        );
    }
    
    /**
     * Создать конфигурацию для расчета за 6 месяцев
     * @param baseDate базовая дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для 6 месяцев
     */
    public static OperationCalculatorConfig forSixMonths(LocalDate baseDate, 
                                                        OperationTypeFilter operationType, 
                                                        int currencyId,
                                                        EntityFilter entityFilter) {
        return new OperationCalculatorConfig(
            OperationPeriod.SIX_MONTHS, 
            baseDate, 
            operationType, 
            null, 
            currencyId,
            entityFilter
        );
    }
    
    /**
     * Создать конфигурацию для расчета за 9 месяцев
     * @param baseDate базовая дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для 9 месяцев
     */
    public static OperationCalculatorConfig forNineMonths(LocalDate baseDate, 
                                                         OperationTypeFilter operationType, 
                                                         int currencyId,
                                                         EntityFilter entityFilter) {
        return new OperationCalculatorConfig(
            OperationPeriod.NINE_MONTHS, 
            baseDate, 
            operationType, 
            null, 
            currencyId,
            entityFilter
        );
    }
    
    /**
     * Создать конфигурацию для расчета за год
     * @param baseDate базовая дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для года
     */
    public static OperationCalculatorConfig forYear(LocalDate baseDate, 
                                                   OperationTypeFilter operationType, 
                                                   int currencyId,
                                                   EntityFilter entityFilter) {
        return new OperationCalculatorConfig(
            OperationPeriod.YEAR, 
            baseDate, 
            operationType, 
            null, 
            currencyId,
            entityFilter
        );
    }
    
    /**
     * Создать конфигурацию для расчета за все время
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для всего времени
     */
    public static OperationCalculatorConfig forAllTime(OperationTypeFilter operationType, 
                                                      int currencyId,
                                                      EntityFilter entityFilter) {
        return new OperationCalculatorConfig(
            OperationPeriod.ALL_TIME, 
            LocalDate.now(), 
            operationType, 
            null, 
            currencyId,
            entityFilter
        );
    }
    
    // Геттеры и сеттеры
    
    public OperationPeriod getPeriod() {
        return period;
    }
    
    public void setPeriod(OperationPeriod period) {
        this.period = period;
    }
    
    public LocalDate getBaseDate() {
        return baseDate;
    }
    
    public void setBaseDate(LocalDate baseDate) {
        this.baseDate = baseDate;
    }
    
    public OperationTypeFilter getOperationType() {
        return operationType;
    }
    
    public void setOperationType(OperationTypeFilter operationType) {
        this.operationType = operationType;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public int getCurrencyId() {
        return currencyId;
    }
    
    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }
    
    public EntityFilter getEntityFilter() {
        return entityFilter;
    }
    
    public void setEntityFilter(EntityFilter entityFilter) {
        this.entityFilter = entityFilter;
    }
    
    /**
     * Получить начальную дату периода
     * @return начальная дата периода
     */
    public LocalDate getStartDate() {
        return period.getStartDate(baseDate);
    }
    
    /**
     * Получить конечную дату периода
     * @return конечная дата периода
     */
    public LocalDate getEndDate() {
        return period.getEndDate(baseDate);
    }
    
    /**
     * Проверить, является ли конфигурация валидной
     * @return true если конфигурация валидна
     */
    public boolean isValid() {
        return period != null && 
               baseDate != null && 
               operationType != null && 
               entityFilter != null;
    }
    
    @Override
    public String toString() {
        return "OperationCalculatorConfig{" +
                "period=" + period +
                ", baseDate=" + baseDate +
                ", operationType=" + operationType +
                ", categoryId=" + categoryId +
                ", currencyId=" + currencyId +
                ", entityFilter=" + entityFilter +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        OperationCalculatorConfig that = (OperationCalculatorConfig) o;
        
        if (period != that.period) return false;
        if (baseDate != null ? !baseDate.equals(that.baseDate) : that.baseDate != null) return false;
        if (operationType != that.operationType) return false;
        if (categoryId != null ? !categoryId.equals(that.categoryId) : that.categoryId != null) return false;
        if (currencyId != that.currencyId) return false;
        return entityFilter == that.entityFilter;
    }
    
    @Override
    public int hashCode() {
        int result = period != null ? period.hashCode() : 0;
        result = 31 * result + (baseDate != null ? baseDate.hashCode() : 0);
        result = 31 * result + (operationType != null ? operationType.hashCode() : 0);
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        result = 31 * result + currencyId;
        result = 31 * result + (entityFilter != null ? entityFilter.hashCode() : 0);
        return result;
    }
}
