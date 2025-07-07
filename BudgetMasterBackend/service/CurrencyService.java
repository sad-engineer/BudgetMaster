package service;

import model.Currency;
import repository.CurrencyRepository;

import java.util.List;
import java.util.Optional;

public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Currency createCurrency(Currency currency) {
        // Бизнес-валидация перед сохранением
        return currencyRepository.save(currency);
    }

    public Optional<Currency> getCurrencyById(int id) {
        return currencyRepository.findById(id);
    }

    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    public Currency updateCurrency(Currency currency) {
        // Бизнес-валидация перед обновлением
        return currencyRepository.update(currency);
    }

    public boolean deleteCurrency(int id) {
        return currencyRepository.delete(id);
    }
} 