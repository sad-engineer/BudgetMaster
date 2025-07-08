package service;

import model.Account;
import repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account) {
        // Проверяем, есть ли удаленная запись с таким же title, и восстанавливаем её
        Optional<Integer> deletedId = accountRepository.findDeletedByTitle(account.getTitle());
        if (deletedId.isPresent()) {
            // Восстанавливаем удаленную запись
            accountRepository.restore(deletedId.get());
            
            // Получаем восстановленную запись
            Optional<Account> restoredAccount = accountRepository.findById(deletedId.get());
            if (restoredAccount.isPresent()) {
                Account restored = restoredAccount.get();
                
                // Обновляем данные восстановленной записи
                restored.setUpdateTime(LocalDateTime.now());
                restored.setUpdatedBy(account.getUpdatedBy());
                
                // Обновляем запись в БД
                accountRepository.update(restored);
                
                // Устанавливаем ID восстановленной записи
                account.setId(restored.getId());
                account.setPosition(restored.getPosition());
                
                return account;
            }
        }
        
        // Удаленной записи не найдено, продолжаем обычное сохранение
        // Автоматически устанавливаем позицию, если она не установлена (равна 0)
        if (account.getPosition() == 0) {
            account.setPosition(accountRepository.getNextPosition());
        }
        
        // Сохраняем новую запись
        Account savedAccount = accountRepository.save(account);
        
        // Нормализуем позиции после сохранения
        accountRepository.normalizePositions();
        
        return savedAccount;
    }

    public Optional<Account> getAccountById(int id) {
        return accountRepository.findById(id);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account updateAccount(Account account) {
        // Корректируем позиции, если это необходимо перед обновлением
        accountRepository.adjustPositionsForUpdate(account);
        
        // Обновляем запись
        Account updatedAccount = accountRepository.update(account);
        
        // Нормализуем позиции после обновления
        accountRepository.normalizePositions();
        
        return updatedAccount;
    }

    public boolean deleteAccount(int id) {
        return accountRepository.delete(id);
    }

    public boolean deleteAccount(int id, String deletedBy) {
        return accountRepository.delete(id, deletedBy);
    }

    public boolean restoreAccount(int id) {
        boolean restored = accountRepository.restore(id);
        if (restored) {
            // Нормализуем позиции после восстановления
            accountRepository.normalizePositions();
        }
        return restored;
    }

    public List<Account> getDeletedAccounts() {
        return accountRepository.findDeleted();
    }

    /**
     * Нормализует позиции всех активных счетов
     */
    public void normalizePositions() {
        accountRepository.normalizePositions();
    }
} 