import os
import sys
import unittest

from BudgetMasterBackend.tests.test_common import (
    cleanup_example,
    get_java_class,
    setup_example,
    test_data_manager,
)

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestCurrencyService(unittest.TestCase):
    """Юнит-тесты для CurrencyService"""

    @classmethod
    def setUpClass(cls):
        if not setup_example():
            raise Exception("Не удалось настроить окружение для тестов")

        # Импортируем Java классы
        cls.CurrencyService = get_java_class("service.CurrencyService")
        cls.CurrencyRepository = get_java_class("repository.CurrencyRepository")
        cls.Currency = get_java_class("model.Currency")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        cls.test_currency_ids = []
        cls.db_path = test_data_manager.db_manager.db_path
        cls.repository = cls.CurrencyRepository(cls.db_path)
        cls.service = cls.CurrencyService(cls.repository, "test_user")

    @classmethod
    def tearDownClass(cls):
        try:
            db_manager = test_data_manager.db_manager
            deleted_count = 0
            for currency_id in cls.test_currency_ids:
                try:
                    success = db_manager.execute_update("DELETE FROM currencies WHERE id = ?", (currency_id,))
                    if success:
                        deleted_count += 1
                    else:
                        print(f"Ошибка при удалении валюты {currency_id}")
                except Exception as e:
                    print(f"Ошибка при удалении валюты {currency_id}: {e}")
            if deleted_count > 0:
                print(f"Удалено {deleted_count} тестовых валют из базы данных")
            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            pass

    def test_01_create_currency(self):
        """Тест 01: Создание новой валюты"""
        title = "Валюта 1"
        position = self.repository.getMaxPosition() + 1

        currency = self.service.get(title)
        self.test_currency_ids.append(currency.getId())

        self.assertIsNotNone(currency)
        self.assertEqual(currency.getTitle(), title)
        self.assertEqual(currency.getPosition(), position)
        self.assertEqual(currency.getCreatedBy(), "test_user")
        self.assertIsNone(currency.getUpdatedBy())
        self.assertIsNone(currency.getDeletedBy())
        self.assertIsNotNone(currency.getCreateTime())
        self.assertIsNone(currency.getUpdateTime())
        self.assertIsNone(currency.getDeleteTime())


    def test_02_get_all_currencies(self):
        """Тест 02: Получение всех валют"""
        c1 = self.service.create("Валюта 1")
        c2 = self.service.create("Валюта 2")
        self.test_currency_ids.append(c1.getId())
        self.test_currency_ids.append(c2.getId())
        currencies = self.service.getAll()
        self.assertIsNotNone(currencies)
        self.assertGreater(len(currencies), 0)
        ids = [c.getId() for c in currencies]
        self.assertIn(c1.getId(), ids)
        self.assertIn(c2.getId(), ids)

    def test_03_get_currency_by_id(self):
        """Тест 03: Получение валюты по ID"""
        currency = self.service.create("Валюта по ID")
        self.test_currency_ids.append(currency.getId())
        found = self.service.getById(currency.getId())
        self.assertTrue(found.isPresent())
        self.assertEqual(found.get().getId(), currency.getId())

    def test_04_get_currency_by_id_not_found(self):
        """Тест 04: Получение валюты по несуществующему ID"""
        found = self.service.getById(999999)
        self.assertFalse(found.isPresent())

    def test_05_delete_currency_by_id(self):
        """Тест 05: Удаление валюты по ID"""
        currency = self.service.create("Валюта для удаления")
        self.test_currency_ids.append(currency.getId())
        result = self.service.delete(currency.getId())
        self.assertTrue(result)
        # Проверяем, что валюта помечена как удалённая
        found = self.repository.findById(currency.getId())
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_06_delete_currency_by_title(self):
        """Тест 06: Удаление валюты по названию"""
        title = "Валюта для удаления по названию"
        currency = self.service.create(title)
        self.test_currency_ids.append(currency.getId())
        result = self.service.delete(title)
        self.assertTrue(result)
        # Проверяем, что валюта помечена как удалённая
        found = self.repository.findByTitle(title)
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_07_restore_currency(self):
        """Тест 07: Восстановление удалённой валюты"""
        currency = self.service.create("Валюта для восстановления")
        self.test_currency_ids.append(currency.getId())
        self.repository.deleteById(currency.getId(), "test_user")
        deleted = self.repository.findById(currency.getId()).get()
        restored = self.service.restore(deleted)
        self.assertIsNotNone(restored)
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())
        self.assertEqual(restored.getUpdatedBy(), "test_user")

    def test_08_restore_currency_by_id(self):
        """Тест 08: Восстановление валюты по ID"""
        currency = self.service.create("Валюта для восстановления по ID")
        self.test_currency_ids.append(currency.getId())
        self.repository.deleteById(currency.getId(), "test_user")
        restored = self.service.restore(currency.getId())
        self.assertIsNotNone(restored)
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())

    def test_09_restore_currency_by_id_not_found(self):
        """Тест 09: Восстановление валюты по несуществующему ID"""
        restored = self.service.restore(999999)
        self.assertIsNone(restored)

    def test_10_is_currency_deleted(self):
        """Тест 10: Проверка удаления валюты"""
        currency = self.service.create("Валюта для проверки удаления")
        self.test_currency_ids.append(currency.getId())
        self.repository.deleteById(currency.getId(), "test_user")
        deleted = self.repository.findById(currency.getId()).get()
        self.assertTrue(self.service.isCurrencyDeleted(deleted))
        # Восстановим для очистки
        self.service.restore(deleted)

    def test_11_change_position(self):
        """Тест 11: Изменение позиции валюты"""
        position = self.repository.getMaxPosition()
        c1 = self.service.create("Валюта 1")
        c2 = self.service.create("Валюта 2")
        c3 = self.service.create("Валюта 3")
        self.test_currency_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c1 на позицию 3
        result = self.service.changePosition(c1, position + 3)
        self.assertEqual(result.getPosition(), position + 3)
        # Проверяем, что другие валюты сдвинулись
        updated_c2 = self.repository.findById(c2.getId()).get()
        updated_c3 = self.repository.findById(c3.getId()).get()
        self.assertEqual(updated_c2.getPosition(), position + 1)
        self.assertEqual(updated_c3.getPosition(), position + 2)

    def test_12_change_position_up(self):
        """Тест 12: Перемещение валюты вверх"""
        position = self.repository.getMaxPosition()
        c1 = self.service.create("Валюта A")
        c2 = self.service.create("Валюта B")
        c3 = self.service.create("Валюта C")
        self.test_currency_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c3 на позицию 1
        result = self.service.changePosition(c3, position + 1)
        self.assertEqual(result.getPosition(), position + 1)
        updated_c1 = self.repository.findById(c1.getId()).get()
        updated_c2 = self.repository.findById(c2.getId()).get()
        self.assertEqual(updated_c1.getPosition(), position + 2)
        self.assertEqual(updated_c2.getPosition(), position + 3)

    def test_13_change_position_by_old_new(self):
        """Тест 13: Изменение позиции по старой и новой позиции"""
        position = self.repository.getMaxPosition()
        c1 = self.service.create("Валюта X")
        c2 = self.service.create("Валюта Y")
        c3 = self.service.create("Валюта Z")
        self.test_currency_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        result = self.service.changePosition(position + 1, position + 3)
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), position + 3)

    def test_14_change_position_by_old_new_not_found(self):
        """Тест 14: Изменение позиции по несуществующей старой позиции"""
        result = self.service.changePosition(999, 1)
        self.assertIsNone(result)

    def test_15_change_position_same_position(self):
        """Тест 15: Изменение позиции на ту же позицию"""
        currency = self.service.create("Валюта без изменений")
        self.test_currency_ids.append(currency.getId())
        old_position = currency.getPosition()
        result = self.service.changePosition(currency, old_position)
        self.assertEqual(result.getPosition(), old_position)

    def test_16_change_position_invalid_position(self):
        """Тест 16: Изменение позиции на недопустимую"""
        currency = self.service.create("Валюта для теста")
        self.test_currency_ids.append(currency.getId())
        with self.assertRaises(Exception):
            self.service.changePosition(currency, 999)

    def test_17_set_user_unsupported(self):
        """Тест 17: Попытка сменить пользователя"""
        with self.assertRaises(Exception):
            self.service.setUser("new_user")

    def test_18_create_currency_with_special_title(self):
        """Тест 18: Создание валюты с необычным названием"""
        currency = self.service.create("")
        self.test_currency_ids.append(currency.getId())
        self.assertEqual(currency.getTitle(), "")
        
        currency2 = self.service.create("Валюта с цифрами 123")
        self.test_currency_ids.append(currency2.getId())
        self.assertEqual(currency2.getTitle(), "Валюта с цифрами 123")

    def test_19_currency_position_sequence(self):
        """Тест 19: Последовательность позиций валют"""
        currencies = []
        position = self.repository.getMaxPosition()
        for i in range(4):
            c = self.service.create(f"Валюта {i + 1}")
            currencies.append(c)
            self.test_currency_ids.append(c.getId())
        for i, c in enumerate(currencies):
            self.assertEqual(c.getPosition(), position + i + 1)

    def test_20_delete_and_restore_cycle(self):
        """Тест 20: Цикл удаления и восстановления валюты"""
        currency = self.service.create("Валюта для цикла")
        self.test_currency_ids.append(currency.getId())
        self.service.delete(currency.getId())
        restored1 = self.service.restore(currency.getId())
        self.assertEqual(restored1.getId(), currency.getId())
        self.service.delete(currency.getId())
        restored2 = self.service.restore(currency.getId())
        self.assertEqual(restored2.getId(), currency.getId())

    def test_21_get_currency_new_creation(self):
        """Тест 21: Получение валюты - создание новой"""
        title = "Новая валюта для создания"
        currency = self.service.get(title)
        self.test_currency_ids.append(currency.getId())
        self.assertEqual(currency.getTitle(), title)
        self.assertEqual(currency.getCreatedBy(), "test_user")

    def test_22_get_currency_existing_active(self):
        """Тест 22: Получение валюты - существующая активная"""
        title = "Существующая активная валюта"
        # Создаем валюту
        created = self.service.create(title)
        self.test_currency_ids.append(created.getId())
        # Получаем ту же валюту
        found = self.service.get(title)
        self.assertEqual(found.getId(), created.getId())
        self.assertEqual(found.getTitle(), title)

    def test_23_get_currency_existing_deleted_restore(self):
        """Тест 23: Получение валюты - восстановление удалённой"""
        title = "Удалённая валюта для восстановления"
        # Создаем валюту
        created = self.service.create(title)
        self.test_currency_ids.append(created.getId())
        # Удаляем её
        self.repository.deleteById(created.getId(), "test_user")
        # Получаем через get - должна восстановиться
        restored = self.service.get(title)
        self.assertEqual(restored.getId(), created.getId())
        self.assertEqual(restored.getTitle(), title)
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())

    def test_24_get_currency_multiple_calls(self):
        """Тест 24: Множественные вызовы get для одной валюты"""
        title = "Валюта для множественных вызовов"
        # Первый вызов - создание
        currency1 = self.service.get(title)
        self.test_currency_ids.append(currency1.getId())
        # Второй вызов - получение существующей
        currency2 = self.service.get(title)
        self.assertEqual(currency1.getId(), currency2.getId())
        self.assertEqual(currency1.getTitle(), currency2.getTitle())

    def test_25_create_currency_with_unicode_title(self):
        """Тест 25: Создание валюты с Unicode названием"""
        title = "Валюта с кириллицей и символами: !@#$%^&*()"
        currency = self.service.create(title)
        self.test_currency_ids.append(currency.getId())
        self.assertEqual(currency.getTitle(), title)

    def test_26_currency_audit_fields(self):
        """Тест 26: Проверка аудит-полей валюты"""
        currency = self.service.create("Валюта для аудита")
        self.test_currency_ids.append(currency.getId())
        
        # Проверяем, что все аудит-поля заполнены
        self.assertEqual(currency.getCreatedBy(), "test_user")
        self.assertEqual(currency.getUpdatedBy(), "test_user")
        self.assertIsNotNone(currency.getCreateTime())
        self.assertIsNotNone(currency.getUpdateTime())
        
        # Проверяем, что время создания и обновления совпадают при создании
        self.assertEqual(currency.getCreateTime(), currency.getUpdateTime())

    def test_27_get_currency_with_special_characters(self):
        """Тест 27: Получение валюты со специальными символами"""
        title = "Валюта с символами: €$¥£₽"
        currency = self.service.get(title)
        self.test_currency_ids.append(currency.getId())
        self.assertEqual(currency.getTitle(), title)

    def test_28_currency_position_validation(self):
        """Тест 28: Валидация позиций валют"""
        # Создаем несколько валют
        currencies = []
        for i in range(3):
            c = self.service.create(f"Валюта для валидации {i + 1}")
            currencies.append(c)
            self.test_currency_ids.append(c.getId())
        
        # Проверяем, что позиции уникальны и последовательны
        positions = [c.getPosition() for c in currencies]
        self.assertEqual(len(positions), len(set(positions)))  # Все позиции уникальны
        self.assertEqual(min(positions) + len(positions) - 1, max(positions))  # Последовательны

    def test_29_currency_restore_audit_fields(self):
        """Тест 29: Проверка аудит-полей при восстановлении валюты"""
        currency = self.service.create("Валюта для восстановления аудита")
        self.test_currency_ids.append(currency.getId())
        
        # Удаляем валюту
        self.repository.deleteById(currency.getId(), "test_user")
        deleted = self.repository.findById(currency.getId()).get()
        
        # Восстанавливаем
        restored = self.service.restore(deleted)
        
        # Проверяем аудит-поля
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())
        self.assertEqual(restored.getUpdatedBy(), "test_user")
        self.assertIsNotNone(restored.getUpdateTime())


if __name__ == '__main__':
    unittest.main() 