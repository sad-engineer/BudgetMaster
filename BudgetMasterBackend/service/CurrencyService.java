package service;

import model.Currency;
import repository.CurrencyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Currency createCurrency(Currency currency) {
        // Проверяем, есть ли удаленная запись с таким же title, и восстанавливаем её
        Optional<Integer> deletedId = currencyRepository.findDeletedByTitle(currency.getTitle());
        if (deletedId.isPresent()) {
            // Восстанавливаем удаленную запись
            currencyRepository.restore(deletedId.get());
            
            // Получаем восстановленную запись
            Optional<Currency> restoredCurrency = currencyRepository.findById(deletedId.get());
            if (restoredCurrency.isPresent()) {
                Currency restored = restoredCurrency.get();
                
                // Обновляем данные восстановленной записи
                restored.setUpdateTime(LocalDateTime.now());
                restored.setUpdatedBy(currency.getUpdatedBy());
                
                // Обновляем запись в БД
                currencyRepository.update(restored);
                
                // Устанавливаем ID восстановленной записи
                currency.setId(restored.getId());
                currency.setPosition(restored.getPosition());
                
                return currency;
            }
        }
        
        // Удаленной записи не найдено, продолжаем обычное сохранение
        // Автоматически устанавливаем позицию, если она не установлена (равна 0)
        if (currency.getPosition() == 0) {
            currency.setPosition(currencyRepository.getNextPosition());
        }
        
        // Сохраняем новую запись
        Currency savedCurrency = currencyRepository.save(currency);
        
        // Нормализуем позиции после сохранения
        currencyRepository.normalizePositions();
        
        return savedCurrency;
    }

    public Optional<Currency> getCurrencyById(int id) {
        return currencyRepository.findById(id);
    }

    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    public Currency updateCurrency(Currency currency) {
        // Корректируем позиции, если это необходимо перед обновлением
        currencyRepository.adjustPositionsForUpdate(currency);
        
        // Обновляем запись
        Currency updatedCurrency = currencyRepository.update(currency);
        
        // Нормализуем позиции после обновления
        currencyRepository.normalizePositions();
        
        return updatedCurrency;
    }

    public boolean deleteCurrency(int id) {
        return currencyRepository.delete(id);
    }

    public boolean deleteCurrency(int id, String deletedBy) {
        return currencyRepository.delete(id, deletedBy);
    }

    public boolean restoreCurrency(int id) {
        boolean restored = currencyRepository.restore(id);
        if (restored) {
            // Нормализуем позиции после восстановления
            currencyRepository.normalizePositions();
        }
        return restored;
    }

    public List<Currency> getDeletedCurrencies() {
        return currencyRepository.findDeleted();
    }

    /**
     * Нормализует позиции всех активных валют
     */
    public void normalizePositions() {
        currencyRepository.normalizePositions();
    }
} 