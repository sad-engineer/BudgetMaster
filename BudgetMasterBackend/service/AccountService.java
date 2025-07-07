package service;

import model.Account;
import repository.AccountRepository;

import java.util.List;
import java.util.Optional;

public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account) {
        // Бизнес-валидация перед сохранением
        return accountRepository.save(account);
    }

    public Optional<Account> getAccountById(int id) {
        return accountRepository.findById(id);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account updateAccount(Account account) {
        // Бизнес-валидация перед обновлением
        return accountRepository.update(account);
    }

    public boolean deleteAccount(int id) {
        return accountRepository.delete(id);
    }
} 