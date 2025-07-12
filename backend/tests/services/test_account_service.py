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


class TestAccountService(unittest.TestCase):
    """Юнит-тесты для AccountService"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        if not setup_example():
            raise Exception("Не удалось настроить окружения для тестов")

        # Импортируем Java классы
        cls.AccountService = get_java_class("service.AccountService")
        cls.AccountRepository = get_java_class("repository.AccountRepository")
        cls.Account = get_java_class("model.Account")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        # Список ID тестовых записей для очистки
        cls.test_account_ids = []

        """Настройка перед каждым теста"""
        # Используем существующую базу данных
        cls.db_path = test_data_manager.db_manager.db_path

        # Создаем репозиторий и сервис
        cls.repository = cls.AccountRepository(cls.db_path)
        cls.service = cls.AccountService(cls.repository, "test_user")

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

    def test_01_constructor_with_repository(self):
        """Тест 01: Конструктор с репозиторием"""
        # Act
        service = self.AccountService(self.repository, "test_user")

        # Assert
        self.assertIsNotNone(service)

    def test_02_constructor_with_user_only(self):
        """Тест 02: Конструктор только с пользователем"""
        # Act
        service = self.AccountService("test_user")

        # Assert
        self.assertIsNotNone(service)

    def test_03_create_account(self):
        """Тест 03: Создание нового счета"""
        # Arrange
        title = "Основной счет"
        type_ = self.Integer(1)
        currency_id = self.Integer(1)

        # Act
        account = self.service.create(title, type_, currency_id)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        position = self.repository.getMaxPosition()
        # Assert
        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)
        self.assertEqual(account.getPosition(), position)
        self.assertEqual(account.getAmount(), 0)
        self.assertEqual(account.getType(), 1)
        self.assertEqual(account.getCurrencyId(), 1)
        self.assertEqual(account.getClosed(), 0)
        self.assertIsNotNone(account.getCreateTime())
        self.assertEqual(account.getCreatedBy(), "test_user")
        self.assertIsNotNone(account.getUpdateTime())
        self.assertEqual(account.getUpdatedBy(), "test_user")

    def test_04_create_multiple_accounts(self):
        """Тест 04: Создание нескольких счетов"""
        # Arrange
        titles = ["Счет 1", "Счет 2", "Счет 3"]
        position = self.repository.getMaxPosition()

        # Act
        accounts = []
        positions = []
        for title in titles:
            account = self.service.create(title)
            accounts.append(account)
            position += 1
            positions.append(position)

            # Сохраняем ID для очистки сразу после создания
            self.test_account_ids.append(account.getId())

        # Assert
        self.assertEqual(len(accounts), 3)
        for i, account in enumerate(accounts):
            self.assertEqual(account.getTitle(), titles[i])
            self.assertEqual(account.getPosition(), positions[i])

    def test_05_get_all_accounts(self):
        """Тест 05: Получение всех счетов"""
        # Arrange
        account1 = self.service.create("Счет 21")
        account2 = self.service.create("Счет 22")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())

        # Act
        accounts = self.service.getAll()

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(accounts)
        self.assertGreater(accounts.size(), 0)

        # Проверяем, что наши счета есть в списке
        titles = []
        for acc in accounts:
            titles.append(acc.getTitle())
        self.assertIn("Счет 1", titles)
        self.assertIn("Счет 2", titles)

    def test_06_get_by_id(self):
        """Тест 06: Получение счета по ID"""
        # Arrange
        created_account = self.service.create("Тестовый счет")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(created_account.getId())

        # Act
        found_account = self.service.getById(created_account.getId())

        # Assert
        self.assertTrue(found_account.isPresent())
        self.assertEqual(found_account.get().getTitle(), "Тестовый счет")

    def test_07_get_by_id_not_found(self):
        """Тест 07: Получение счета по несуществующему ID"""
        # Act
        found_account = self.service.getById(999)

        # Assert
        self.assertFalse(found_account.isPresent())

    def test_08_get_by_currency_id(self):
        """Тест 08: Получение счетов по ID валюты"""
        # Arrange
        account1 = self.service.create("Счет 31", self.Integer(1), self.Integer(1))
        account2 = self.service.create("Счет 31", self.Integer(1), self.Integer(2))

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())

        # Act
        accounts = self.service.getByCurrencyId(1)

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(accounts)
        self.assertGreater(accounts.size(), 0)

        # Проверяем, что наши счета есть в списке
        titles = []
        for acc in accounts:
            titles.append(acc.getTitle())
        self.assertIn(account1.getTitle(), titles)

        # account2 нет в списке из-за другого ID валюты
        self.assertNotIn(account2.getTitle(), titles)

    def test_09_get_by_type(self):
        """Тест 09: Получение счетов по типу"""
        # Arrange
        account1 = self.service.create("Счет 41", self.Integer(1), self.Integer(1))
        account2 = self.service.create("Счет 42", self.Integer(2), self.Integer(1))

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())

        # Act
        accounts = self.service.getByType(1)

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(accounts)
        self.assertGreater(accounts.size(), 0)

        # Проверяем, что наши счета есть в списке
        titles = []
        for acc in accounts:
            titles.append(acc.getTitle())
        self.assertIn(account1.getTitle(), titles)

        # account2 нет в списке из-за другого ID валюты
        self.assertNotIn(account2.getTitle(), titles)

    def test_10_get_account_by_title_new(self):
        """Тест 10: Получение счета по названию - новый счет"""
        # Arrange
        title = "Новый счет"

        # Act
        account = self.service.get(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Assert
        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)
        self.assertGreater(account.getPosition(), 0)

    def test_11_get_account_by_title_existing(self):
        """Тест 11: Получение счета по названию - существующий счет"""
        # Arrange
        title = "Существующий счет"
        created_account = self.service.get(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(created_account.getId())

        # Act
        found_account = self.service.get(title)

        # Assert
        self.assertIsNotNone(found_account)
        self.assertEqual(found_account.getId(), created_account.getId())
        self.assertEqual(found_account.getTitle(), title)

    def test_12_get_account_by_title_deleted(self):
        """Тест 12: Получение счета по названию - удаленный счет"""
        # Arrange
        title = "Удаленный счет"
        account = self.service.get(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Удаляем счет
        self.repository.deleteByTitle(title, "test_user")

        # Act
        restored_account = self.service.get(title)

        # Assert
        self.assertIsNotNone(restored_account)
        self.assertEqual(restored_account.getTitle(), title)
        self.assertIsNone(restored_account.getDeleteTime())
        self.assertIsNone(restored_account.getDeletedBy())

    def test_13_is_account_deleted(self):
        """Тест 13: Проверка удаления счета"""
        # Arrange
        account = self.service.create("Тестовый счет")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Act & Assert - активный счет
        self.assertFalse(self.service.isAccountDeleted(account))

        # Удаляем счет
        self.repository.deleteByTitle("Тестовый счет", "test_user")
        deleted_account = self.repository.findByTitle("Тестовый счет").get()

        # Act & Assert - удаленный счет
        self.assertTrue(self.service.isAccountDeleted(deleted_account))

    def test_14_restore_account(self):
        """Тест 14: Восстановление счета"""
        # Arrange
        account = self.service.create("Тестовый счет")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        self.repository.deleteByTitle("Тестовый счет", "test_user")
        deleted_account = self.repository.findByTitle("Тестовый счет").get()

        # Act
        restored_account = self.service.restore(deleted_account)

        # Assert
        self.assertIsNotNone(restored_account)
        self.assertIsNone(restored_account.getDeleteTime())
        self.assertIsNone(restored_account.getDeletedBy())
        self.assertIsNotNone(restored_account.getUpdateTime())
        self.assertEqual(restored_account.getUpdatedBy(), "test_user")

    def test_15_restore_account_by_id(self):
        """Тест 15: Восстановление счета по ID"""
        # Arrange
        account = self.service.create("Тестовый счет")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        self.repository.deleteByTitle("Тестовый счет", "test_user")

        # Act
        restored_account = self.service.restore(account.getId())

        # Assert
        self.assertIsNotNone(restored_account)
        self.assertIsNone(restored_account.getDeleteTime())
        self.assertIsNone(restored_account.getDeletedBy())

    def test_16_restore_account_by_id_not_found(self):
        """Тест 16: Восстановление счета по несуществующему ID"""
        # Act
        restored_account = self.service.restore(999)

        # Assert
        self.assertIsNone(restored_account)

    def test_17_delete_account(self):
        """Тест 17: Удаление счета"""
        # Arrange
        title = "Счет для удаления"
        account = self.service.create(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Act
        result = self.service.delete(title)

        # Assert
        self.assertTrue(result)

        # Проверяем, что счет действительно удален
        deleted_account = self.repository.findByTitle(title).get()
        self.assertIsNotNone(deleted_account.getDeleteTime())
        self.assertEqual(deleted_account.getDeletedBy(), "test_user")

    def test_18_delete_account_not_found(self):
        """Тест 18: Удаление несуществующего счета"""
        # Act
        result = self.service.delete("Несуществующий счет")

        # Assert
        self.assertFalse(result)

    def test_19_change_position_same_position(self):
        """Тест 19: Изменение позиции на ту же позицию"""
        # Arrange
        account = self.service.create("Тестовый счет")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        original_position = account.getPosition()

        # Act
        result = self.service.changePosition(account, original_position)

        # Assert
        self.assertEqual(result.getPosition(), original_position)

    def test_20_change_position_invalid_position(self):
        """Тест 20: Изменение позиции на недопустимую"""
        # Arrange
        account = self.service.create("Тестовый счет")
        position = self.repository.getMaxPosition()

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Act & Assert
        with self.assertRaises(Exception):
            self.service.changePosition(account, 0)  # Позиция должна быть >= 1

        with self.assertRaises(Exception):
            self.service.changePosition(account, position + 1)  # Позиция больше количества счетов

    def test_21_change_position_move_down(self):
        """Тест 21: Перемещение счета вниз"""
        # Arrange
        position = self.repository.getMaxPosition()
        account1 = self.service.create("Счет 1", )
        account2 = self.service.create("Счет 2")
        account3 = self.service.create("Счет 3")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())
        self.test_account_ids.append(account3.getId())

        # Act
        result = self.service.changePosition(account1, position + 3)

        # Assert
        self.assertEqual(result.getPosition(), position + 3)

        # Проверяем, что другие счета сдвинулись
        updated_account2 = self.repository.findById(account2.getId()).get()
        updated_account3 = self.repository.findById(account3.getId()).get()
        self.assertEqual(updated_account2.getPosition(), position + 1)
        self.assertEqual(updated_account3.getPosition(), position + 2)

    def test_22_change_position_move_up(self):
        """Тест 22: Перемещение счета вверх"""
        # Arrange
        account1 = self.service.create("Счет 1")
        account2 = self.service.create("Счет 2")
        account3 = self.service.create("Счет 3")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())
        self.test_account_ids.append(account3.getId())
        position = self.repository.getMaxPosition()
        # Act
        result = self.service.changePosition(account3, position - 2)

        # Assert
        self.assertEqual(result.getPosition(), position - 2)

        # Проверяем, что другие счета сдвинулись
        updated_account1 = self.repository.findById(account1.getId()).get()
        updated_account2 = self.repository.findById(account2.getId()).get()
        self.assertEqual(updated_account1.getPosition(), position - 1)
        self.assertEqual(updated_account2.getPosition(), position)

    def test_23_change_position_by_old_new(self):
        """Тест 23: Изменение позиции по старой и новой позиции"""
        # Arrange
        account1 = self.service.create("Счет 51")
        account2 = self.service.create("Счет 52")
        account3 = self.service.create("Счет 53")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())
        self.test_account_ids.append(account3.getId())

        # Act
        result = self.service.changePosition(1, 3)

        # Assert
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), 3)

    def test_24_change_position_by_old_new_not_found(self):
        """Тест 24: Изменение позиции по несуществующей старой позиции"""
        # Act
        result = self.service.changePosition(999, 1)

        # Assert
        self.assertIsNone(result)

    def test_25_set_user_unsupported(self):
        """Тест 25: Попытка установить нового пользователя"""
        # Act & Assert
        with self.assertRaises(Exception):
            self.service.setUser("new_user")

    def test_26_create_account_with_special_characters(self):
        """Тест 26: Создание счета со специальными символами"""
        # Arrange
        title = "Счет с символами: !@#$%^&*()"

        # Act
        account = self.service.create(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Assert
        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)

    def test_27_create_account_with_unicode(self):
        """Тест 27: Создание счета с Unicode символами"""
        # Arrange
        title = "Счет с кириллицей: Привет мир! 🌍"

        # Act
        account = self.service.create(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Assert
        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)

    def test_28_create_account_empty_title(self):
        """Тест 28: Создание счета с пустым названием"""
        # Arrange
        title = ""

        # Act
        account = self.service.create(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Assert
        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)

    def test_29_create_account_null_title(self):
        """Тест 29: Создание счета с null названием"""
        # Arrange
        title = None

        # Act
        account = self.service.create(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Assert
        self.assertIsNotNone(account)
        self.assertIsNone(account.getTitle())

    def test_30_multiple_operations_same_account(self):
        """Тест 30: Множественные операции с одним счетом"""
        # Arrange
        title = "Многофункциональный счет"

        # Act
        account1 = self.service.create(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())

        account2 = self.service.get(title)
        account3 = self.service.get(title)

        # Assert
        self.assertEqual(account1.getId(), account2.getId())
        self.assertEqual(account2.getId(), account3.getId())
        self.assertEqual(account1.getTitle(), title)

    def test_31_account_position_sequence(self):
        """Тест 31: Последовательность позиций счетов"""
        # Arrange
        titles = ["Счет A", "Счет B", "Счет C", "Счет D"]
        position = self.repository.getMaxPosition()
        # Act
        accounts = []
        for title in titles:
            account = self.service.create(title)
            accounts.append(account)

            # Сохраняем ID для очистки сразу после создания
            self.test_account_ids.append(account.getId())

        # Assert
        for i, account in enumerate(accounts):
            position += 1
            self.assertEqual(account.getPosition(), position)

    def test_32_delete_and_restore_cycle(self):
        """Тест 32: Цикл удаления и восстановления"""
        # Arrange
        title = "Циклический счет"
        account = self.service.create(title)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Act & Assert - первый цикл
        self.service.delete(title)
        restored1 = self.service.get(title)
        self.assertEqual(restored1.getId(), account.getId())

        # Act & Assert - второй цикл
        self.service.delete(title)
        restored2 = self.service.get(title)
        self.assertEqual(restored2.getId(), account.getId())

    def test_33_edge_case_positions(self):
        """Тест 33: Граничные случаи позиций"""
        # Arrange
        account = self.service.create("Единственный счет")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Act & Assert - позиция 1 (первая)
        result1 = self.service.changePosition(account, 1)
        self.assertEqual(result1.getPosition(), 1)

        # Act & Assert - позиция 1 (последняя, так как счет один)
        result2 = self.service.changePosition(account, 1)
        self.assertEqual(result2.getPosition(), 1)

    def test_34_service_with_different_users(self):
        """Тест 34: Сервис с разными пользователями"""
        # Arrange
        service1 = self.AccountService(self.repository, "user1")
        service2 = self.AccountService(self.repository, "user2")

        # Act
        account1 = service1.create("Счет пользователя 1")
        account2 = service2.create("Счет пользователя 2")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())

        # Assert
        self.assertEqual(account1.getCreatedBy(), "user1")
        self.assertEqual(account2.getCreatedBy(), "user2")

    def test_35_repository_independence(self):
        """Тест 35: Независимость репозиториев"""
        # Arrange
        repository2 = self.AccountRepository(self.db_path)
        service2 = self.AccountService(repository2, "user2")

        # Act
        account1 = self.service.create("Счет в БД 1")
        account2 = service2.create("Счет в БД 2")

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())

        # Assert
        self.assertNotEqual(account1.getId(), account2.getId())

        # Проверяем, что оба счета видны в общей базе
        accounts = self.service.getAll()
        account_titles = [acc.getTitle() for acc in accounts]
        self.assertIn("Счет в БД 1", account_titles)
        self.assertIn("Счет в БД 2", account_titles)


if __name__ == '__main__':
    unittest.main()
