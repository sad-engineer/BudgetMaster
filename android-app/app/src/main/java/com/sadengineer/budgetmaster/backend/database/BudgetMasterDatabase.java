
package com.sadengineer.budgetmaster.backend.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

import com.sadengineer.budgetmaster.backend.dao.AccountDao;
import com.sadengineer.budgetmaster.backend.dao.BudgetDao;
import com.sadengineer.budgetmaster.backend.dao.CategoryDao;
import com.sadengineer.budgetmaster.backend.dao.CurrencyDao;
import com.sadengineer.budgetmaster.backend.dao.OperationDao;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.converter.DateTimeConverter;

/**
 * Основной класс базы данных Room для BudgetMaster
 */
@Database(
    entities = {
        Account.class,
        Operation.class,
        Category.class,
        Budget.class,
        Currency.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverter.class)
public abstract class BudgetMasterDatabase extends RoomDatabase {
    
    /**
     * DAO интерфейсы
     */
    public abstract AccountDao accountDao();
    public abstract OperationDao operationDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetDao budgetDao();
    public abstract CurrencyDao currencyDao();
    
    /**
     * Singleton для базы данных
     */
    private static volatile BudgetMasterDatabase INSTANCE;
    
    /**
     * Получение экземпляра базы данных
     * @param context контекст приложения
     * @return экземпляр базы данных
     */
    public static BudgetMasterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BudgetMasterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        BudgetMasterDatabase.class,
                        "budget_master_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
} 