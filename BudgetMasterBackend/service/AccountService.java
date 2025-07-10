package service;

import model.Account;
import repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы со счетами
 */
public class AccountService {
    /**
     * Репозиторий для работы со счетами
     */
    private final AccountRepository accountRepository;
    
    /**
     * Пользователь, выполняющий операции
     */
    private final String user;

    /**
     * Конструктор для сервиса
     * @param accountRepository репозиторий для работы со счетами
     * @param user пользователь, выполняющий операции
     */
    public AccountService(AccountRepository accountRepository, String user) {
        this.accountRepository = accountRepository;
        this.user = user;
    }

    /**
     * Конструктор для сервиса с автоматическим созданием репозитория
     * @param user пользователь, выполняющий операции
     */
    public AccountService(String user) {
        this.accountRepository = new AccountRepository("budget_master.db");
        this.user = user;
    }

    /**
     * Удаляет счет по title
     * @param title название счета
     * @return true, если удаление успешно
     */
    public boolean delete(String title) {
        return accountRepository.deleteByTitle(title, user);
    }

    /**
     * Изменяет порядок счета с переупорядочиванием других счетов
     * @param account счет для изменения позиции
     * @param newPosition новая позиция
     * @return счет с новой позицией
     */
    public Account changePosition(Account account, int newPosition) {
        int oldPosition = account.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return account;
        }
        
        // Получаем все счета для переупорядочивания
        List<Account> allAccounts = getAll();
        
        // Проверяем, что новая позиция валидна
        if (newPosition < 1 || newPosition > accountRepository.getMaxPosition()) {
            throw new IllegalArgumentException("Новая позиция должна быть от 1 до " + accountRepository.getMaxPosition());
        }
        
        // Переупорядочиваем позиции
        if (oldPosition < newPosition) {
            // Двигаем счет вниз: сдвигаем счета между старой и новой позицией вверх
            for (Account a : allAccounts) {
                if (a.getId() != account.getId() && 
                    a.getPosition() > oldPosition && 
                    a.getPosition() <= newPosition) {
                    a.setPosition(a.getPosition() - 1);
                    a.setUpdateTime(LocalDateTime.now());
                    a.setUpdatedBy(user);
                    accountRepository.update(a);
                }
            }
        } else {
            // Двигаем счет вверх: сдвигаем счета между новой и старой позицией вниз
            for (Account a : allAccounts) {
                if (a.getId() != account.getId() && 
                    a.getPosition() >= newPosition && 
                    a.getPosition() < oldPosition) {
                    a.setPosition(a.getPosition() + 1);
                    a.setUpdateTime(LocalDateTime.now());
                    a.setUpdatedBy(user);
                    accountRepository.update(a);
                }
            }
        }
        
        // Устанавливаем новую позицию для целевого счета
        account.setPosition(newPosition);
        account.setUpdateTime(LocalDateTime.now());    
        account.setUpdatedBy(user);
        return accountRepository.update(account);
    }

    /**
     * Изменяет порядок счета с переупорядочиванием других счетов
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     * @return счет с новой позицией
     */
    public Account changePosition(int oldPosition, int newPosition) {   
        Optional<Account> accountOpt = accountRepository.findByPosition(oldPosition);
        if (accountOpt.isPresent()) {
            return changePosition(accountOpt.get(), newPosition);
        }
        return null;
    }

    /**
     * Создает новый счет
     * @param title название счета
     * @return счет
     */
    public Account create(String title) {
        Account newAccount = new Account();
        int nextPosition = accountRepository.getMaxPosition() + 1;
        newAccount.setTitle(title);
        newAccount.setPosition(nextPosition);
        newAccount.setCreateTime(LocalDateTime.now());
        newAccount.setCreatedBy(user);
        newAccount.setUpdateTime(LocalDateTime.now());
        newAccount.setUpdatedBy(user);
        
        return accountRepository.save(newAccount);
    }

    /**
     * Получает все счета
     * @return список счетов
     */
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    /**
     * Получает счет по ID
     * @param id ID счета
     * @return счет
     */
    public Optional<Account> getById(int id) { 
        return accountRepository.findById(id);
    }

    /**
     * Получает счета по ID валюты
     * @param currencyId ID валюты
     * @return список счетов
     */
    public List<Account> getByCurrencyId(int currencyId) {
        return accountRepository.findAllByCurrencyId(currencyId);
    }

    /**
     * Получает счета по типу счета
     * @param type тип счета
     * @return список счетов
     */
    public List<Account> getByType(int type) {
        return accountRepository.findAllByType(type);
    }

    /**
     * Получает счет по title.
     * Если счет не существует, создает новый.
     * Если счет существует и удален, восстанавливает его.
     * Если счет существует и активен, возвращает его.
     * @param title название счета
     * @return счет
     */
    public Account get(String title) {
        Optional<Account> account = accountRepository.findByTitle(title);
        if (account.isPresent()) {
            if (isAccountDeleted(account.get())) {
                return restore(account.get());
            }
            return account.get();
        }
        return create(title);
    }

    /**
     * Проверка счета на удаление
     * @param account класс счета
     * @return true, если счет удален
     */
    public boolean isAccountDeleted(Account account) {
        return account.getDeleteTime() != null;
    }

    /**
     * Восстанавливает счет
     * @param restoredAccount счет
     * @return счет
     */
    public Account restore(Account restoredAccount) {
        restoredAccount.setDeleteTime(null);
        restoredAccount.setDeletedBy(null);
        restoredAccount.setUpdateTime(LocalDateTime.now());
        restoredAccount.setUpdatedBy(user);
        return accountRepository.update(restoredAccount);
    }

    /**
     * Восстанавливает счет по id
     * @param id id счета
     * @return счет или null, если счет не найден
     */
    public Account restore(int id) {
        Optional<Account> accountOpt = getById(id);
        if (accountOpt.isPresent()) {
            return restore(accountOpt.get());
        }
        return null;
    }

    /**
     * Устанавливает нового пользователя для операций
     * @param newUser новый пользователь
     */
    public void setUser(String newUser) {
        // Обратите внимание: поле user final, поэтому нужно создать новый экземпляр сервиса
        // или использовать другой подход для смены пользователя
        throw new UnsupportedOperationException("Для смены пользователя создайте новый экземпляр AccountService");
    }
} 