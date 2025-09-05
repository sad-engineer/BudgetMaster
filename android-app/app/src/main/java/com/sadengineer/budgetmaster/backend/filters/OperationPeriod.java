package com.sadengineer.budgetmaster.backend.filters;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Периоды для расчета операций
 */
public enum OperationPeriod {

    /**
     * За день (конкретная дата)
     */
    DAY {
        @Override
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate;
        }
        
        @Override
        public LocalDate getEndDate(LocalDate baseDate) {
            return baseDate;
        }
    },
    
    /**
     * За месяц (текущий месяц)
     */
    MONTH {
        @Override
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate.withDayOfMonth(1);
        }
        
        @Override
        public LocalDate getEndDate(LocalDate baseDate) {
            return baseDate.withDayOfMonth(baseDate.lengthOfMonth());
        }
    },
    
    /**
     * За 6 месяцев (последние 6 месяцев)
     */
    SIX_MONTHS {
        @Override
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate.minusMonths(5).withDayOfMonth(1);
        }
        
        @Override
        public LocalDate getEndDate(LocalDate baseDate) {
            return baseDate.withDayOfMonth(baseDate.lengthOfMonth());
        }
    },
    
    /**
     * За 9 месяцев (последние 9 месяцев)
     */
    NINE_MONTHS {
        @Override
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate.minusMonths(8).withDayOfMonth(1);
        }
        
        @Override
        public LocalDate getEndDate(LocalDate baseDate) {
            return baseDate.withDayOfMonth(baseDate.lengthOfMonth());
        }
    },
    
    /**
     * За год (текущий год)
     */
    YEAR {
        @Override
        public LocalDate getStartDate(LocalDate baseDate) {
            return baseDate.withDayOfYear(1);
        }
        
        @Override
        public LocalDate getEndDate(LocalDate baseDate) {
            return baseDate.withDayOfYear(baseDate.lengthOfYear());
        }
    },
    
    /**
     * За все время
     */
    ALL_TIME {
        @Override
        public LocalDate getStartDate(LocalDate baseDate) {
            return LocalDate.of(1900, 1, 1); // Начало эпохи
        }
        
        @Override
        public LocalDate getEndDate(LocalDate baseDate) {
            return LocalDate.of(2100, 12, 31); // Конец эпохи
        }
    };

    /**
     * Получить начальную дату периода
     * @param baseDate базовая дата для расчета
     * @return начальная дата периода
     */
    public abstract LocalDate getStartDate(LocalDate baseDate);
    
    /**
     * Получить конечную дату периода
     * @param baseDate базовая дата для расчета
     * @return конечная дата периода
     */
    public abstract LocalDate getEndDate(LocalDate baseDate);
    
    /**
     * Получить период для конкретной даты
     * @param date конкретная дата
     * @return период для этой даты
     */
    public static OperationPeriod forDate(LocalDate date) {
        return DAY;
    }
    
    /**
     * Получить период для текущего месяца
     * @return период для текущего месяца
     */
    public static OperationPeriod forCurrentMonth() {
        return MONTH;
    }
    
    /**
     * Получить период для последних 6 месяцев
     * @return период для последних 6 месяцев
     */
    public static OperationPeriod forLastSixMonths() {
        return SIX_MONTHS;
    }
    
    /**
     * Получить период для последних 9 месяцев
     * @return период для последних 9 месяцев
     */
    public static OperationPeriod forLastNineMonths() {
        return NINE_MONTHS;
    }
    
    /**
     * Получить период для текущего года
     * @return период для текущего года
     */
    public static OperationPeriod forCurrentYear() {
        return YEAR;
    }
    
    /**
     * Получить период для всего времени
     * @return период для всего времени
     */
    public static OperationPeriod forAllTime() {
        return ALL_TIME;
    }
}
