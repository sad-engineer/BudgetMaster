// -*- coding: utf-8 -*-
package service;

import model.Account;
import repository.AccountRepository;
import validator.AccountValidator;
import validator.BaseEntityValidator;
import validator.CommonValidator;
import constants.ServiceConstants;
import constants.ModelConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

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
        this.accountRepository = new AccountRepository(ServiceConstants.DEFAULT_DATABASE_NAME);
        this.user = user;
    }



    /**
     * Удаляет счет по id
     * @param id id счета
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id) {
        BaseEntityValidator.validatePositiveId(id, "ID счета");
        return accountRepository.deleteById(id, user);
    }

    /**
     * Удаляет счет по title
     * @param title название счета
     * @return true, если удаление успешно
     */
    public boolean delete(String title) {
        CommonValidator.validateAccountTitle(title);
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
            throw new IllegalArgumentException(ServiceConstants.ERROR_POSITION_OUT_OF_RANGE + accountRepository.getMaxPosition());
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
     * Создает новый счет без валидации (для внутреннего использования)
     * @param title название счета
     * @param amount начальный баланс
     * @param type тип счета
     * @param currencyId ID валюты
     * @param closed закрытый счет
     * @return созданный счет
     */
    private Account create(String title, int amount, int type, int currencyId, int closed) {
        Account newAccount = new Account();
        int nextPosition = accountRepository.getMaxPosition() + 1;
        newAccount.setTitle(title);
        newAccount.setPosition(nextPosition);
        newAccount.setCreateTime(LocalDateTime.now());
        newAccount.setCreatedBy(user);
        newAccount.setType(type);
        newAccount.setCurrencyId(currencyId);
        newAccount.setAmount(amount);
        newAccount.setClosed(closed);

        // Валидация счета
        AccountValidator.validateForCreate(newAccount);

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
     * Получает все счета по типу
     * @param type тип счета
     * @return список счетов
     */
    public List<Account> getAllByType(int type) {
        CommonValidator.validateAccountType(type);
        return accountRepository.findAllByType(type);
    }

    /**
     * Получает все счета по ID валюты
     * @param currencyId ID валюты
     * @return список счетов
     */
    public List<Account> getAllByCurrencyId(int currencyId) {
        CommonValidator.validateCurrencyId(currencyId);
        return accountRepository.findAllByCurrencyId(currencyId);
    }

    /**
     * Получает все счета по закрытому статусу
     * @param closed закрытый статус
     * @return список счетов
     */
    public List<Account> getAllByClosed(int closed) {
        CommonValidator.validateClosedStatus(closed);
        return accountRepository.findAllByClosed(closed);
    }   

    /**
     * Получает счет по ID. 
     * Если счет с таким ID существует, возвращает его.
     * Если счет с таким ID существует, но удален, восстанавливает его.
     * Если счет с таким ID не существует, вернет null.
     * @param id ID счета
     * @return счет
     */
    public Account get(Integer id) { 
        BaseEntityValidator.validatePositiveId(id, "ID счета");
        Optional<Account> account = accountRepository.findById(id);
        if (account.isPresent()) {
            Account accountObj = account.get();
            if (isAccountDeleted(accountObj)) {
                return restore(accountObj);
            }
            return accountObj;
        }
        return null;
    }

    

    /**
     * Получает счет по названию. 
     * Если счет с таким названием существует, возвращает его.
     * Если счет с таким названием существует, но удален, восстанавливает его.
     * Если счет с таким названием не существует, возвращает null.
     * @param title название счета
     * @return счет
     */
    public Account get(String title) {
        CommonValidator.validateAccountTitle(title);
        Optional<Account> account = accountRepository.findByTitle(title);
        if (account.isPresent()) {
            Account accountObj = account.get();
            if (isAccountDeleted(accountObj)) {
                return restore(accountObj);
            }
            return accountObj;
        }
        return null;
    }

    /**
     * Проверяет, удален ли счет
     * @param account счет для проверки
     * @return true, если счет удален
     */
    public boolean isAccountDeleted(Account account) {
        return account.isDeleted();
    }

    /**
     * Получает счет по названию или создает новый с указанными параметрами.
     * Если счет с таким названием существует, и параметры совпадают с указанными, возвращает его.
     * Если счет с таким названием существует, но параметры отличные от указанных, обновляет его.
     * Если счет с таким названием существует, но удален, восстанавливает его.
     * Если счет с таким названием не существует, создает новый с указанными параметрами.
     * @param title название счета
     * @param amount начальный баланс
     * @param type тип счета
     * @param currencyId ID валюты
     * @param closed закрытый счет
     * @return счет
     */
    public Account get(String title, int amount, int type, int currencyId, int closed) {
        CommonValidator.validateAccountTitle(title);
        CommonValidator.validateAccountAmount(amount);
        CommonValidator.validateAccountType(type);
        CommonValidator.validateCurrencyId(currencyId);
        CommonValidator.validateClosedStatus(closed);

        Optional<Account> account = accountRepository.findByTitle(title);
        if (account.isPresent()) {
            Account accountObj = account.get();
            
            if (accountObj.getTitle().equals(title) &&
                accountObj.getAmount() == amount &&
                accountObj.getType() == type &&
                accountObj.getCurrencyId() == currencyId &&
                accountObj.getClosed() == closed) {
                
                return accountObj;
            }

            // Обновляем существующий счет с новыми параметрами
            accountObj.setAmount(amount);
            accountObj.setType(type);
            accountObj.setCurrencyId(currencyId);
            accountObj.setClosed(closed);
            return updateInternal(accountObj);
        }

        return create(title, amount, type, currencyId, closed);
    }

    /**
     * Получает счет по названию или создает новый с указанным балансом и типом.
     * @param title название счета
     * @param amount начальный баланс
     * @param type тип счета
     * @return счет
     */
    public Account get(String title, int amount, int type) {
        return get(title, amount, type, ModelConstants.DEFAULT_CURRENCY_ID, ModelConstants.DEFAULT_ACCOUNT_CLOSED);
    }

    /**
     * Получает счет по названию или создает новый с указанным балансом.
     * @param title название счета
     * @param amount начальный баланс
     * @return счет
     */
    public Account get(String title, int amount) {
        return get(title, amount, ModelConstants.DEFAULT_ACCOUNT_TYPE, ModelConstants.DEFAULT_CURRENCY_ID, ModelConstants.DEFAULT_ACCOUNT_CLOSED);
    }

    /**
     * Восстанавливает удаленный счет (для внутреннего использования)
     * @param restoredAccount счет для восстановления
     * @return восстановленный счет
     */
    private Account restore(Account restoredAccount) {
        restoredAccount.setDeletedBy(null);
        restoredAccount.setDeleteTime(null);
        return updateInternal(restoredAccount);
    }

    /**
     * Восстанавливает удаленный счет по ID
     * @param id ID счета
     * @return восстановленный счет или null, если счет не найден
     */
    public Account restore(Integer id) {
        BaseEntityValidator.validatePositiveId(id, "ID счета");
        Optional<Account> account = accountRepository.findById(id);
        if (account.isPresent()) {
            return restore(account.get());
        }
        return null;
    }

    /**
     * Обновляет счет по названию
     * @param title название счета для обновления
     * @param newAmount новое значение баланса (может быть null)
     * @param newType новое значение типа счета (может быть null)
     * @param newCurrencyId новое значение ID валюты (может быть null)
     * @param newClosed новое значение статуса закрытия (может быть null)
     * @return обновленный счет или null, если счет не найден
     */
    public Account update(String title,
                         Integer newAmount,
                         Integer newType,
                         Integer newCurrencyId,
                         Integer newClosed) {
        CommonValidator.validateAccountTitle(title);

        Optional<Account> accountOpt = accountRepository.findByTitle(title);
        if (!accountOpt.isPresent()) {
            return null;
        }

        Account updatedAccount = accountOpt.get();

        if (newAmount != null) {
            CommonValidator.validateAccountAmount(newAmount);
            updatedAccount.setAmount(newAmount);
        }
        
        if (newType != null) {
            CommonValidator.validateAccountType(newType);
            updatedAccount.setType(newType);
        }
        
        if (newCurrencyId != null) {
            CommonValidator.validateCurrencyId(newCurrencyId);
            updatedAccount.setCurrencyId(newCurrencyId);
        }
        
        if (newClosed != null) {
            CommonValidator.validateClosedStatus(newClosed);
            updatedAccount.setClosed(newClosed);
        }

        if (updatedAccount.isDeleted()) {
            return restore(updatedAccount);
        }
        
        // Проверяем, был ли задан хотя бы один параметр для обновления
        if (newAmount != null || newType != null || newCurrencyId != null || newClosed != null) {
            return updateInternal(updatedAccount);
        }
        
        // Если ни один параметр не задан, возвращаем null
        return null;
    }

    /**
     * Обновляет счет без новых параметров (для внутреннего использования)
     * @param updatedAccount счет для обновления
     * @return обновленный счет
     */
    private Account updateInternal(Account updatedAccount) {
        updatedAccount.setUpdateTime(LocalDateTime.now());
        updatedAccount.setUpdatedBy(user);
        
        return accountRepository.update(updatedAccount);
    }
} 