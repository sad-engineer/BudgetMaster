import os
import sys
import unittest

from tests.backend.test_common import (
    cleanup_example,
    get_java_class,
    setup_example,
)

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestAccountService(unittest.TestCase):
    """Юнит-тесты для AccountService"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружения для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.AccountService = get_java_class("service.AccountService")
        cls.AccountRepository = get_java_class("repository.AccountRepository")
        cls.Account = get_java_class("model.Account")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        # Список ID тестовых записей для очистки
        cls.test_account_ids = []

        # Используем существующую базу данных
        cls.db_path = cls.db_manager.db_path

        # Создаем репозиторий и сервис
        cls.repository = cls.AccountRepository(cls.db_path)
        cls.service = cls.AccountService(cls.repository, "test_user")

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            # Удаляем тестовые записи по ID
            deleted_count = 0
            for account_id in cls.test_account_ids:
                try:
                    success = cls.db_manager.execute_update("DELETE FROM accounts WHERE id = ?", (account_id,))
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

    def test_03_get_account_by_title(self):
        """Тест 03: Получение нового (не существующего) счета по названию"""
        title = "Основной счет"
        position = self.repository.getMaxPosition() + 1

        account = self.service.get(title, 0, 1, 1, 0)
        self.test_account_ids.append(account.getId())

        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)
        self.assertEqual(account.getPosition(), position)
        self.assertEqual(account.getCreatedBy(), "test_user")
        self.assertIsNone(account.getUpdatedBy())
        self.assertIsNone(account.getDeletedBy())
        self.assertIsNotNone(account.getCreateTime())
        self.assertIsNone(account.getUpdateTime())
        self.assertIsNone(account.getDeleteTime())

    def test_04_get_existing_account_by_title(self):
        """Тест 04: Получение существующего счета по названию"""
        title = "Существующий счет"

        account_new = self.service.get(title, 0, 1, 1, 0)
        self.test_account_ids.append(account_new.getId())

        # Счет создан, его позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        account = self.service.get(title)
        self.test_account_ids.append(account.getId())

        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)
        self.assertEqual(account.getPosition(), position)
        self.assertEqual(account.getCreatedBy(), "test_user")
        self.assertIsNone(account.getUpdatedBy())
        self.assertIsNone(account.getDeletedBy())
        self.assertIsNotNone(account.getCreateTime())
        self.assertIsNone(account.getUpdateTime())
        self.assertIsNone(account.getDeleteTime())

    def test_05_get_deleted_account_by_title(self):
        """Тест 05: Получение удаленного счета по названию"""
        title = "Удаленный счет"

        account_new = self.service.get(title, 0, 1, 1, 0)
        self.test_account_ids.append(account_new.getId())
        self.repository.deleteByTitle(title, "test_user")

        # Счет не удален физически из таблицы, его позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        account = self.service.get(title)
        self.test_account_ids.append(account.getId())

        self.assertIsNotNone(account)
        self.assertEqual(account.getTitle(), title)
        self.assertEqual(account.getPosition(), position)
        self.assertEqual(account.getCreatedBy(), "test_user")
        self.assertEqual(account.getUpdatedBy(), "test_user")
        self.assertIsNone(account.getDeletedBy())
        self.assertIsNotNone(account.getCreateTime())
        self.assertIsNotNone(account.getUpdateTime())
        self.assertIsNone(account.getDeleteTime())
        self.assertNotEqual(account.getCreateTime(), account.getUpdateTime())

    def test_06_get_account_by_id(self):
        """Тест 06: Получение счета по ID"""
        account = self.service.get(self.Integer(1))
        self.assertIsNotNone(account)
        self.assertEqual(account.getId(), 1)
        self.assertEqual(account.getTitle(), "Наличные")
        self.assertEqual(account.getCreatedBy(), "initializer")
        self.assertIsNone(account.getDeletedBy())
        self.assertIsNotNone(account.getCreateTime())
        self.assertIsNone(account.getDeleteTime())

    def test_07_get_account_by_id_not_found(self):
        """Тест 07: Получение счета по несуществующему ID"""
        account = self.service.get(self.Integer(999999))
        self.assertIsNone(account)

    def test_08_create_account_with_special_title(self):
        """Тест 08: Создание счета с необычным названием"""
        account = self.service.get("12123", 0, 1, 1, 0)
        self.test_account_ids.append(account.getId())
        self.assertEqual(account.getTitle(), "12123")

        account2 = self.service.get("Счет с цифрами 123", 0, 1, 1, 0)
        self.test_account_ids.append(account2.getId())
        self.assertEqual(account2.getTitle(), "Счет с цифрами 123")

    def test_09_delete_account_by_id(self):
        """Тест 09: Удаление счета по ID"""
        account = self.service.get("Счет 4", 0, 1, 1, 0)
        self.test_account_ids.append(account.getId())
        result = self.service.delete(account.getId())
        self.assertTrue(result)
        # Проверяем, что счет помечен как удалённый
        found = self.repository.findById(account.getId())
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_10_delete_account_by_title(self):
        """Тест 10: Удаление счета по названию"""
        title = "Счет 5"
        account = self.service.get(title, 0, 1, 1, 0)
        self.test_account_ids.append(account.getId())
        result = self.service.delete(title)
        self.assertTrue(result)

        # Проверяем, что счет удален
        found = self.repository.findByTitle(title).get()
        self.assertIsNotNone(found)
        self.assertIsNotNone(found.getDeleteTime())
        self.assertEqual(found.getDeletedBy(), "test_user")

    def test_11_is_account_deleted(self):
        """Тест 11: Проверка удаления счета"""
        account = self.service.get("Счет 6", 0, 1, 1, 0)
        self.test_account_ids.append(account.getId())
        self.repository.deleteById(account.getId(), "test_user")
        deleted = self.repository.findById(account.getId()).get()
        self.assertTrue(self.service.isAccountDeleted(deleted))

    def test_12_change_position(self):
        """Тест 12: Изменение позиции счета"""
        position = self.repository.getMaxPosition()
        a1 = self.service.get("Счет х1", 0, 1, 1, 0)
        a2 = self.service.get("Счет х2", 0, 1, 1, 0)
        a3 = self.service.get("Счет х3", 0, 1, 1, 0)
        self.test_account_ids.extend([a1.getId(), a2.getId(), a3.getId()])
        # Перемещаем a1 на позицию 3
        result = self.service.changePosition(a1, position + 3)
        self.assertEqual(result.getPosition(), position + 3)
        # Проверяем, что другие счета сдвинулись
        updated_a2 = self.repository.findById(a2.getId()).get()
        updated_a3 = self.repository.findById(a3.getId()).get()
        self.assertEqual(updated_a2.getPosition(), position + 1)
        self.assertEqual(updated_a3.getPosition(), position + 2)

    def test_13_change_position_up(self):
        """Тест 13: Перемещение счета вверх"""
        position = self.repository.getMaxPosition()
        a1 = self.service.get("Счет A", 0, 1, 1, 0)
        a2 = self.service.get("Счет B", 0, 1, 1, 0)
        a3 = self.service.get("Счет C", 0, 1, 1, 0)
        self.test_account_ids.extend([a1.getId(), a2.getId(), a3.getId()])
        # Перемещаем a3 на позицию 1
        result = self.service.changePosition(a3, position + 1)
        self.assertEqual(result.getPosition(), position + 1)
        updated_a1 = self.repository.findById(a1.getId()).get()
        updated_a2 = self.repository.findById(a2.getId()).get()
        self.assertEqual(updated_a1.getPosition(), position + 2)
        self.assertEqual(updated_a2.getPosition(), position + 3)

    def test_14_get_all_accounts(self):
        """Тест 14: Получение всех счетов"""
        a1 = self.service.get("Счет 111", 0, 1, 1, 0)
        a2 = self.service.get("Счет 211", 0, 1, 1, 0)
        self.test_account_ids.append(a1.getId())
        self.test_account_ids.append(a2.getId())
        accounts = self.service.getAll()
        self.assertIsNotNone(accounts)
        self.assertGreater(len(accounts), 0)
        ids = [a.getId() for a in accounts]
        self.assertIn(a1.getId(), ids)
        self.assertIn(a2.getId(), ids)

    def test_15_get_all_by_type(self):
        """Тест 15: Получение счетов по типу"""
        a1 = self.service.get("Счет типа 1", 1000, 1, 1, 0)
        a2 = self.service.get("Счет типа 2", 2000, 2, 1, 0)
        self.test_account_ids.append(a1.getId())
        self.test_account_ids.append(a2.getId())

        accounts_type_1 = self.service.getAllByType(1)
        accounts_type_2 = self.service.getAllByType(2)

        self.assertGreater(len(accounts_type_1), 0)
        self.assertGreater(len(accounts_type_2), 0)

        # Проверяем, что наши счета есть в соответствующих списках
        type_1_ids = [a.getId() for a in accounts_type_1]
        type_2_ids = [a.getId() for a in accounts_type_2]

        self.assertIn(a1.getId(), type_1_ids)
        self.assertIn(a2.getId(), type_2_ids)

    def test_16_get_all_by_currency_id(self):
        """Тест 16: Получение счетов по ID валюты"""
        a1 = self.service.get("Счет валюты 1", 1000, 1, 1, 0)
        a2 = self.service.get("Счет валюты 2", 2000, 1, 2, 0)
        self.test_account_ids.append(a1.getId())
        self.test_account_ids.append(a2.getId())

        accounts_currency_1 = self.service.getAllByCurrencyId(1)
        accounts_currency_2 = self.service.getAllByCurrencyId(2)

        self.assertGreater(len(accounts_currency_1), 0)
        self.assertGreater(len(accounts_currency_2), 0)

        # Проверяем, что наши счета есть в соответствующих списках
        currency_1_ids = [a.getId() for a in accounts_currency_1]
        currency_2_ids = [a.getId() for a in accounts_currency_2]

        self.assertIn(a1.getId(), currency_1_ids)
        self.assertIn(a2.getId(), currency_2_ids)

    def test_17_get_existing_account_with_different_parameters(self):
        """Тест 17: Получение существующего счета с другими параметрами (должно обновить)"""
        title = "Счет для обновления"

        # Создаем счет с параметрами по умолчанию
        account1 = self.service.get(title, 0, 1, 1, 0)
        self.test_account_ids.append(account1.getId())

        # Получаем тот же счет с другими параметрами
        account2 = self.service.get(title, 5000, 2, 2, 1)
        self.test_account_ids.append(account2.getId())

        # Должен быть тот же счет, но с обновленными параметрами
        self.assertEqual(account1.getId(), account2.getId())
        self.assertEqual(account2.getAmount(), 5000)
        self.assertEqual(account2.getType(), 2)
        self.assertEqual(account2.getCurrencyId(), 2)
        self.assertEqual(account2.getClosed(), 1)

    def test_18_update_account_with_all_parameters(self):
        """Тест 18: Обновление счета со всеми параметрами"""

        # Создаем счет
        account = self.service.get("Счет для полного обновления", 0, 1, 1, 0)
        self.test_account_ids.append(account.getId())

        # Обновляем все параметры
        updated = self.service.get("Счет для полного обновления", 15000, 2, 2, 1)

        self.assertEqual(updated.getTitle(), "Счет для полного обновления")
        self.assertEqual(updated.getAmount(), 15000)
        self.assertEqual(updated.getType(), 2)
        self.assertEqual(updated.getCurrencyId(), 2)
        self.assertEqual(updated.getClosed(), 1)

    def test_19_update_account_with_no_parameters(self):
        """Тест 19: Обновление счета без параметров (должно вернуть null)"""

        # Создаем счет
        account = self.service.get("Счет без изменений", 0, 1, 1, 0)
        self.test_account_ids.append(account.getId())

        # Обновляем без параметров
        updated = self.service.update("Счет без изменений", None, None, None, None)

        self.assertIsNone(updated)

    def test_20_delete_account_not_found(self):
        """Тест 20: Удаление несуществующего счета"""
        # Act
        result = self.service.delete("Несуществующий счет")

        # Assert
        self.assertFalse(result)

    def test_21_change_position_same_position(self):
        """Тест 21: Изменение позиции на ту же позицию"""
        # Arrange
        account = self.service.get("Тестовый счет", 0, 1, 1, 0)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        original_position = account.getPosition()

        # Act
        result = self.service.changePosition(account, original_position)

        # Assert
        self.assertEqual(result.getPosition(), original_position)

    def test_22_change_position_invalid_position(self):
        """Тест 22: Изменение позиции на недопустимую"""
        # Arrange
        account = self.service.get("Тестовый счет", 0, 1, 1, 0)
        position = self.repository.getMaxPosition()

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Act & Assert
        with self.assertRaises(Exception):
            self.service.changePosition(account, 0)  # Позиция должна быть >= 1

        with self.assertRaises(Exception):
            self.service.changePosition(account, position + 1)  # Позиция больше количества счетов

    def test_23_change_position_move_down(self):
        """Тест 23: Перемещение счета вниз"""
        # Arrange
        position = self.repository.getMaxPosition()
        account1 = self.service.get("Счет 1", 0, 1, 1, 0)
        account2 = self.service.get("Счет 2", 0, 1, 1, 0)
        account3 = self.service.get("Счет 3", 0, 1, 1, 0)

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

    def test_24_change_position_move_up(self):
        """Тест 24: Перемещение счета вверх"""
        # Arrange
        account1 = self.service.get("Счет 11", 0, 1, 1, 0)
        account2 = self.service.get("Счет 21", 0, 1, 1, 0)
        account3 = self.service.get("Счет 31", 0, 1, 1, 0)

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

    def test_25_change_position_by_old_new(self):
        """Тест 25: Изменение позиции по старой и новой позиции"""
        # Arrange
        account1 = self.service.get("Счет 511", 0, 1, 1, 0)
        account2 = self.service.get("Счет 521", 0, 1, 1, 0)
        account3 = self.service.get("Счет 531", 0, 1, 1, 0)
        position = self.repository.getMaxPosition()

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())
        self.test_account_ids.append(account3.getId())

        # Act
        result = self.service.changePosition(position, position - 2)

        # Assert
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), position - 2)

    def test_26_change_position_by_old_new_not_found(self):
        """Тест 26: Изменение позиции по несуществующей старой позиции"""
        # Act
        result = self.service.changePosition(999, 1)

        # Assert
        self.assertIsNone(result)

    def test_27_set_user_unsupported(self):
        """Тест 27: Попытка установить нового пользователя"""
        # Act & Assert
        with self.assertRaises(Exception):
            self.service.setUser("new_user")

    def test_28_create_account_with_special_characters(self):
        """Тест 28: Создание счета со специальными символами"""
        # Arrange
        title = "Счет с символами: !@#$%^&*()"

        # Act & Assert
        with self.assertRaises(Exception) as context:
            self.service.get(title, 0, 1, 1, 0)
        self.assertIn("Название счета содержит недопустимые символы", str(context.exception))

    def test_30_create_account_empty_title(self):
        """Тест 30: Создание счета с пустым названием"""
        # Arrange
        title = ""

        # Act & Assert
        with self.assertRaises(Exception) as context:
            self.service.get(title, 0, 1, 1, 0)
        self.assertIn("Название счета не может быть пустым", str(context.exception))

    def test_31_create_account_null_title(self):
        """Тест 31: Создание счета с null названием"""
        # Arrange
        title = None

        # Act & Assert
        with self.assertRaises(Exception) as context:
            self.service.get(title, 0, 1, 1, 0)
        self.assertIn("Название счета не может быть null", str(context.exception))

    def test_32_multiple_operations_same_account(self):
        """Тест 32: Множественные операции с одним счетом"""
        # Arrange
        title = "Многофункциональный счет"

        # Act
        account1 = self.service.get(title, 0, 1, 1, 0)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())

        account2 = self.service.get(title)
        account3 = self.service.get(title)

        # Assert
        self.assertEqual(account1.getId(), account2.getId())
        self.assertEqual(account2.getId(), account3.getId())
        self.assertEqual(account1.getTitle(), title)

    def test_33_account_position_sequence(self):
        """Тест 33: Последовательность позиций счетов"""
        # Arrange
        titles = ["Счет AA", "Счет BB", "Счет CC", "Счет DD"]
        position = self.repository.getMaxPosition()
        # Act
        accounts = []
        for title in titles:
            account = self.service.get(title, 0, 1, 1, 0)
            accounts.append(account)

            # Сохраняем ID для очистки сразу после создания
            self.test_account_ids.append(account.getId())

        # Assert
        for i, account in enumerate(accounts):
            position += 1
            self.assertEqual(account.getPosition(), position)

    def test_34_delete_and_restore_cycle(self):
        """Тест 34: Цикл удаления и восстановления"""
        # Arrange
        title = "Циклический счет"
        account = self.service.get(title, 0, 1, 1, 0)

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

    def test_35_edge_case_positions(self):
        """Тест 35: Граничные случаи позиций"""
        # Arrange
        account = self.service.get("Единственный счет", 0, 1, 1, 0)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account.getId())

        # Act & Assert - позиция 1 (первая)
        result1 = self.service.changePosition(account, 1)
        self.assertEqual(result1.getPosition(), 1)

        # Act & Assert - позиция 1 (последняя, так как счет один)
        result2 = self.service.changePosition(account, 1)
        self.assertEqual(result2.getPosition(), 1)

    def test_36_service_with_different_users(self):
        """Тест 36: Сервис с разными пользователями"""
        # Arrange
        service1 = self.AccountService(self.repository, "user1")
        service2 = self.AccountService(self.repository, "user2")

        # Act
        account1 = service1.get("Счет пользователя 1", 0, 1, 1, 0)
        account2 = service2.get("Счет пользователя 2", 0, 1, 1, 0)

        # Сохраняем ID для очистки сразу после создания
        self.test_account_ids.append(account1.getId())
        self.test_account_ids.append(account2.getId())

        # Assert
        self.assertEqual(account1.getCreatedBy(), "user1")
        self.assertEqual(account2.getCreatedBy(), "user2")

    def test_37_repository_independence(self):
        """Тест 37: Независимость репозиториев"""
        # Arrange
        repository2 = self.AccountRepository(self.db_path)
        service2 = self.AccountService(repository2, "user2")

        # Act
        account1 = self.service.get("Счет в БД 1", 0, 1, 1, 0)
        account2 = service2.get("Счет в БД 2", 0, 1, 1, 0)

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
