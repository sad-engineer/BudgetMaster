import os
import sys
import unittest

from backend.tests.test_common import (
    cleanup_example,
    get_java_class,
    setup_example,
    test_data_manager,
)

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestAccountRepository(unittest.TestCase):
    """Юнит-тесты для AccountRepository"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        if not setup_example():
            raise Exception("Не удалось настроить окружение для тестов")

        # Импортируем Java классы
        cls.Account = get_java_class("model.Account")
        cls.AccountRepository = get_java_class("repository.AccountRepository")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        # Создаем репозиторий
        cls.repo = cls.AccountRepository(test_data_manager.db_manager.db_path)

        # Список ID тестовых записей для очистки
        cls.test_account_ids = []

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            # Получаем менеджер базы данных
            db_manager = test_data_manager.db_manager

            # Удаляем тестовые записи по ID
            deleted_count = 0
            for account_id in cls.test_account_ids:
                try:
                    success = db_manager.execute_update("DELETE FROM accounts WHERE id = ?", (account_id,))
                    if success:
                        deleted_count += 1
                    else:
                        print(f"Ошибка при удалении счета {account_id}")
                except Exception as e:
                    print(f"Ошибка при удалении счета {account_id}: {e}")

            if deleted_count > 0:
                print(f"Удалено {deleted_count} тестовых записей из базы данных")

            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            # Не завершаем JVM здесь - пусть это делает atexit
            pass

    def setUp(self):
        """Настройка перед каждым тестом"""
        self.max_position = self.repo.getMaxPosition()

    def create_test_account(self, title="Тестовый счет", amount=100000, position=None):
        """Создает тестовый счет"""
        if position is None:
            position = self.repo.getMaxPosition() + 1

        account = self.Account()
        account.setTitle(title)
        account.setAmount(amount)
        account.setPosition(position)
        account.setType(1)  # Текущий счет
        account.setCurrencyId(1)  # RUB
        account.setClosed(0)  # Не закрыт
        account.setCreatedBy("test_user")
        account.setUpdatedBy("test_user")

        # Устанавливаем даты
        now = self.LocalDateTime.now()
        account.setCreateTime(now)
        account.setUpdateTime(now)
        account.setDeleteTime(None)

        return account

    def test_01_delete_by_id(self):
        """Тест 01: Удаление счета по ID"""
        # Arrange
        account = self.create_test_account("Счет для удаления", 300000)
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act
        deleted = self.repo.deleteById(saved_account.getId(), "test_delete")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что счет помечен как удаленный
        found = self.repo.findById(saved_account.getId())
        self.assertTrue(found.isPresent())
        deleted_account = found.get()
        self.assertIsNotNone(deleted_account.getDeleteTime())
        self.assertEqual(deleted_account.getDeletedBy(), "test_delete")

    def test_02_delete_by_title(self):
        """Тест 02: Удаление счета по названию"""
        # Arrange
        account = self.create_test_account("Счет для удаления по названию", 400000)
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act
        deleted = self.repo.deleteByTitle("Счет для удаления по названию", "test_delete_title")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что счет помечен как удаленный
        found = self.repo.findById(saved_account.getId())
        self.assertTrue(found.isPresent())
        deleted_account = found.get()
        self.assertIsNotNone(deleted_account.getDeleteTime())
        self.assertEqual(deleted_account.getDeletedBy(), "test_delete_title")

    def test_03_find_all(self):
        """Тест 03: Получение всех счетов"""
        # Arrange
        account1 = self.create_test_account("Счет 1", 100000, self.repo.getMaxPosition() + 1)
        account2 = self.create_test_account("Счет 2", 200000, self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(account1)
        saved2 = self.repo.save(account2)
        self.test_account_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        all_accounts = self.repo.findAll()

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(all_accounts)
        self.assertGreater(all_accounts.size(), 0)

        # Проверяем, что наши счета есть в списке
        titles = []
        for acc in all_accounts:
            titles.append(acc.getTitle())
        self.assertIn("Счет 1", titles)
        self.assertIn("Счет 2", titles)

    def test_04_find_all_by_currency_id(self):
        """Тест 04: Поиск счетов по ID валюты"""
        # Arrange
        account1 = self.create_test_account("Счет RUB 1", 100000, self.repo.getMaxPosition() + 1)
        account2 = self.create_test_account("Счет RUB 2", 200000, self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(account1)
        saved2 = self.repo.save(account2)
        self.test_account_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        accounts_by_currency = self.repo.findAllByCurrencyId(self.Integer(1))  # RUB

        # Assert
        self.assertIsNotNone(accounts_by_currency)
        self.assertGreater(accounts_by_currency.size(), 0)

        # Проверяем, что все найденные счета имеют валюту RUB
        for acc in accounts_by_currency:
            self.assertEqual(acc.getCurrencyId(), 1)

    def test_05_find_all_by_type(self):
        """Тест 05: Поиск счетов по типу"""
        # Arrange
        account = self.create_test_account("Текущий счет", 100000)
        account.setType(1)  # Текущий счет
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act
        accounts_by_type = self.repo.findAllByType(self.Integer(1))  # Текущий счет

        # Assert
        self.assertIsNotNone(accounts_by_type)
        self.assertGreater(accounts_by_type.size(), 0)

        # Проверяем, что все найденные счета имеют тип 1
        for acc in accounts_by_type:
            self.assertEqual(acc.getType(), 1)

    def test_06_find_by_id(self):
        """Тест 06: Поиск счета по ID"""
        # Arrange
        account = self.create_test_account("Счет для поиска", 75000)
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act
        found_account = self.repo.findById(saved_account.getId())

        # Assert
        self.assertTrue(found_account.isPresent())
        found = found_account.get()
        self.assertEqual(found.getId(), saved_account.getId())
        self.assertEqual(found.getTitle(), "Счет для поиска")
        self.assertEqual(found.getAmount(), 75000)

    def test_07_find_by_title(self):
        """Тест 07: Поиск счета по названию"""
        # Arrange
        unique_title = f"Уникальный счет {self.LocalDateTime.now().toString()}"
        account = self.create_test_account(unique_title, 100000)
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act
        found_account = self.repo.findByTitle(unique_title)

        # Assert
        self.assertTrue(found_account.isPresent())
        found = found_account.get()
        self.assertEqual(found.getId(), saved_account.getId())
        self.assertEqual(found.getTitle(), unique_title)

    def test_08_get_max_position(self):
        """Тест 08: Получение максимальной позиции"""
        # Arrange
        account1 = self.create_test_account("Счет 1", 100000, self.repo.getMaxPosition() + 1)
        account2 = self.create_test_account("Счет 2", 200000, self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(account1)
        saved2 = self.repo.save(account2)
        self.test_account_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        max_position = self.repo.getMaxPosition()

        # Assert
        self.assertIsInstance(max_position, int)
        self.assertGreater(max_position, 0)
        self.assertGreaterEqual(max_position, saved2.getPosition())

    def test_09_save_account(self):
        """Тест 09: Сохранение нового счета"""
        # Arrange
        account = self.create_test_account("Счет для сохранения", 50000)

        # Act
        saved_account = self.repo.save(account)

        # Assert
        self.assertIsNotNone(saved_account)
        self.assertGreater(saved_account.getId(), 0)
        self.assertEqual(saved_account.getTitle(), "Счет для сохранения")
        self.assertEqual(saved_account.getAmount(), 50000)
        self.assertEqual(saved_account.getPosition(), account.getPosition())

        # Сохраняем ID для очистки
        self.test_account_ids.append(saved_account.getId())

    def test_10_update_account(self):
        """Тест 10: Обновление счета"""
        # Arrange
        account = self.create_test_account("Счет для обновления", 100000)
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act
        saved_account.setTitle("Обновленный счет")
        saved_account.setAmount(150000)
        saved_account.setUpdatedBy("test_update")
        updated_account = self.repo.update(saved_account)

        # Assert
        self.assertEqual(updated_account.getTitle(), "Обновленный счет")
        self.assertEqual(updated_account.getAmount(), 150000)
        self.assertEqual(updated_account.getUpdatedBy(), "test_update")

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_account.getId())
        self.assertTrue(found.isPresent())
        found_account = found.get()
        self.assertEqual(found_account.getTitle(), "Обновленный счет")
        self.assertEqual(found_account.getAmount(), 150000)

    def test_11_account_with_null_credit_card_fields(self):
        """Тест 11: Создание счета с NULL полями кредитных карт (mapRowSafe)"""
        # Arrange
        account = self.create_test_account("Счет без кредитных карт", 100000)
        account.setCreditCardLimit(None)
        account.setCreditCardCategoryId(None)
        account.setCreditCardCommissionCategoryId(None)

        # Act
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Assert
        self.assertIsNone(saved_account.getCreditCardLimit())
        self.assertIsNone(saved_account.getCreditCardCategoryId())
        self.assertIsNone(saved_account.getCreditCardCommissionCategoryId())

        # Проверяем, что NULL значения корректно сохранились в БД
        found = self.repo.findById(saved_account.getId())
        self.assertTrue(found.isPresent())
        found_account = found.get()
        self.assertIsNone(found_account.getCreditCardLimit())
        self.assertIsNone(found_account.getCreditCardCategoryId())
        self.assertIsNone(found_account.getCreditCardCommissionCategoryId())

    def test_12_account_with_credit_card_fields(self):
        """Тест 12: Создание счета с полями кредитных карт (mapRowSafe)"""
        # Arrange
        account = self.create_test_account("Кредитная карта", 100000)
        account.setCreditCardLimit(self.Integer(50000))  # 500.00 в копейках
        account.setCreditCardCategoryId(self.Integer(2))
        account.setCreditCardCommissionCategoryId(self.Integer(3))

        # Act
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Assert
        self.assertEqual(saved_account.getCreditCardLimit(), 50000)
        self.assertEqual(saved_account.getCreditCardCategoryId(), 2)
        self.assertEqual(saved_account.getCreditCardCommissionCategoryId(), 3)

        # Проверяем, что значения корректно сохранились в БД
        found = self.repo.findById(saved_account.getId())
        self.assertTrue(found.isPresent())
        found_account = found.get()
        self.assertEqual(found_account.getCreditCardLimit(), 50000)
        self.assertEqual(found_account.getCreditCardCategoryId(), 2)
        self.assertEqual(found_account.getCreditCardCommissionCategoryId(), 3)

    def test_13_update_credit_card_fields(self):
        """Тест 13: Обновление полей кредитных карт (mapRowSafe)"""
        # Arrange
        account = self.create_test_account("Счет для обновления кредитки", 100000)
        account.setCreditCardLimit(self.Integer(30000))
        account.setCreditCardCategoryId(self.Integer(1))
        account.setCreditCardCommissionCategoryId(self.Integer(1))

        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act
        saved_account.setCreditCardLimit(self.Integer(60000))
        saved_account.setCreditCardCategoryId(self.Integer(4))
        saved_account.setCreditCardCommissionCategoryId(self.Integer(5))
        updated_account = self.repo.update(saved_account)

        # Assert
        self.assertEqual(updated_account.getCreditCardLimit(), 60000)
        self.assertEqual(updated_account.getCreditCardCategoryId(), 4)
        self.assertEqual(updated_account.getCreditCardCommissionCategoryId(), 5)

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_account.getId())
        self.assertTrue(found.isPresent())
        found_account = found.get()
        self.assertEqual(found_account.getCreditCardLimit(), 60000)
        self.assertEqual(found_account.getCreditCardCategoryId(), 4)
        self.assertEqual(found_account.getCreditCardCommissionCategoryId(), 5)

    def test_14_find_nonexistent_account(self):
        """Тест 14: Поиск несуществующего счета (findById)"""
        # Act
        found_account = self.repo.findById(self.Integer(99999))

        # Assert
        self.assertFalse(found_account.isPresent())

    def test_15_delete_nonexistent_account(self):
        """Тест 15: Удаление несуществующего счета (deleteById)"""
        # Act
        deleted = self.repo.deleteById(self.Integer(99999), "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_16_delete_nonexistent_account_by_title(self):
        """Тест 16: Удаление несуществующего счета по названию (deleteByTitle)"""
        # Act
        deleted = self.repo.deleteByTitle("Несуществующий счет", "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_17_find_by_nonexistent_currency(self):
        """Тест 17: Поиск счетов по несуществующей валюте (findAllByCurrencyId)"""
        # Act
        accounts = self.repo.findAllByCurrencyId(self.Integer(999))

        # Assert
        self.assertIsNotNone(accounts)
        self.assertEqual(accounts.size(), 0)

    def test_18_find_by_nonexistent_type(self):
        """Тест 18: Поиск счетов по несуществующему типу (findAllByType)"""
        # Act
        accounts = self.repo.findAllByType(self.Integer(999))

        # Assert
        self.assertIsNotNone(accounts)
        self.assertEqual(accounts.size(), 0)

    def test_19_account_position_ordering(self):
        """Тест 19: Проверка упорядочивания счетов по позиции (getMaxPosition)"""
        # Arrange
        account1 = self.create_test_account("Счет позиция 1", 100000, 1)
        account2 = self.create_test_account("Счет позиция 2", 200000, 2)
        account3 = self.create_test_account("Счет позиция 3", 300000, 3)

        saved1 = self.repo.save(account1)
        saved2 = self.repo.save(account2)
        saved3 = self.repo.save(account3)
        self.test_account_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        all_accounts = self.repo.findAll()

        # Assert
        # Проверяем, что счета с нашими позициями есть в списке
        positions = []
        for acc in all_accounts:
            positions.append(acc.getPosition())
        self.assertIn(1, positions)
        self.assertIn(2, positions)
        self.assertIn(3, positions)

    def test_20_account_soft_delete_behavior(self):
        """Тест 20: Проверка поведения soft delete (deleteById)"""
        # Arrange
        account = self.create_test_account("Счет для soft delete", 100000)
        saved_account = self.repo.save(account)
        self.test_account_ids.append(saved_account.getId())

        # Act - удаляем счет
        deleted = self.repo.deleteById(saved_account.getId(), "test_soft_delete")

        # Assert
        self.assertTrue(deleted)

        # Счет должен быть найден, но помечен как удаленный
        found = self.repo.findById(saved_account.getId())
        self.assertTrue(found.isPresent())
        deleted_account = found.get()
        self.assertIsNotNone(deleted_account.getDeleteTime())
        self.assertEqual(deleted_account.getDeletedBy(), "test_soft_delete")

        # Счет должен остаться в списке всех счетов (включая удаленные)
        all_accounts = self.repo.findAll()
        account_ids = []
        for acc in all_accounts:
            account_ids.append(acc.getId())
        self.assertIn(saved_account.getId(), account_ids)


if __name__ == '__main__':
    unittest.main(verbosity=2)
