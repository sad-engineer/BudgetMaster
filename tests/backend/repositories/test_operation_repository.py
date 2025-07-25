import os
import sys
import unittest

from tests.backend.test_common import cleanup_example, get_java_class, setup_example

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestOperationRepository(unittest.TestCase):
    """Юнит-тесты для OperationRepository"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.Operation = get_java_class("com.sadengineer.budgetmaster.backend.model.Operation")
        cls.OperationRepository = get_java_class("com.sadengineer.budgetmaster.backend.repository.OperationRepository")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")
        cls.PlatformUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.PlatformUtil")

        # Инициализируем DatabaseProvider для тестов
        cls.PlatformUtil.initializeDatabaseProvider(None)

        # Используем DB_PATH из test_common.py
        cls.test_db_path = cls.db_manager.db_path

        # Инициализируем базу данных с таблицами
        cls.DatabaseUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.DatabaseUtil")
        cls.DatabaseUtil.createDatabaseIfNotExists(cls.test_db_path)
        print(f"✅ База данных инициализирована: {cls.test_db_path}")

        cls.repo = cls.OperationRepository(cls.test_db_path)

        # Список ID тестовых записей для очистки
        cls.test_operation_ids = []

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            # Получаем менеджер базы данных
            db_manager = cls.db_manager

            # Удаляем тестовые записи по ID
            deleted_count = 0
            for operation_id in cls.test_operation_ids:
                try:
                    success = db_manager.execute_update("DELETE FROM operations WHERE id = ?", (operation_id,))
                    if success:
                        deleted_count += 1
                    else:
                        print(f"Ошибка при удалении операции {operation_id}")
                except Exception as e:
                    print(f"Ошибка при удалении операции {operation_id}: {e}")

            if deleted_count > 0:
                print(f"Удалено {deleted_count} тестовых операций из базы данных")

            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            # Не завершаем JVM здесь - пусть это делает atexit
            pass

    def setUp(self):
        """Настройка перед каждым тестом"""
        # Инициализируем базу данных перед первым использованием
        self.DatabaseUtil.createDatabaseIfNotExists(self.test_db_path)

    def create_test_operation(
        self,
        amount=100000,
        type=1,
        category_id=1,
        account_id=1,
        currency_id=1,
        comment="Тестовая операция",
        to_account_id=None,
        to_currency_id=None,
        to_amount=None,
    ):
        """Создает тестовую операцию"""
        type = self.Integer(type)
        category_id = self.Integer(category_id)
        account_id = self.Integer(account_id)
        currency_id = self.Integer(currency_id)
        to_account_id = self.Integer(to_account_id) if to_account_id is not None else None
        to_currency_id = self.Integer(to_currency_id) if to_currency_id is not None else None
        to_amount = self.Integer(to_amount) if to_amount is not None else None

        operation = self.Operation()
        operation.setAmount(amount)
        operation.setType(type)
        operation.setCategoryId(category_id)
        operation.setAccountId(account_id)
        operation.setCurrencyId(currency_id)
        operation.setComment(comment)
        operation.setToAccountId(to_account_id)
        operation.setToCurrencyId(to_currency_id)
        operation.setToAmount(to_amount)
        operation.setCreatedBy("test_user")
        operation.setUpdatedBy("test_user")

        # Устанавливаем даты
        now = self.LocalDateTime.now()
        operation.setCreateTime(now)
        operation.setUpdateTime(now)
        operation.setDeleteTime(None)
        operation.setDate(now)

        return operation

    def test_01_delete_by_id(self):
        """Тест 01: Удаление операции по ID"""
        # Arrange
        operation = self.create_test_operation(50000)
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Act
        deleted = self.repo.deleteById(saved_operation.getId(), "test_delete")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что операция помечена как удаленная
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        deleted_operation = found.get()
        self.assertIsNotNone(deleted_operation.getDeleteTime())
        self.assertEqual(deleted_operation.getDeletedBy(), "test_delete")

    def test_02_find_all(self):
        """Тест 02: Получение всех операций"""
        # Arrange
        operation1 = self.create_test_operation(100000, comment="Операция 1")
        operation2 = self.create_test_operation(200000, comment="Операция 2")

        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        all_operations = self.repo.findAll()

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(all_operations)
        self.assertGreater(all_operations.size(), 0)

        # Проверяем, что наши операции есть в списке
        comments = []
        for op in all_operations:
            comments.append(op.getComment())
        self.assertIn("Операция 1", comments)
        self.assertIn("Операция 2", comments)

    def test_03_find_all_by_account_id(self):
        """Тест 03: Поиск операций по ID счета"""
        # Arrange
        operation1 = self.create_test_operation(100000, account_id=1, comment="Операция счета 1")
        operation2 = self.create_test_operation(200000, account_id=1, comment="Операция счета 1 еще")
        operation3 = self.create_test_operation(300000, account_id=2, comment="Операция счета 2")

        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        saved3 = self.repo.save(operation3)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        account1_operations = self.repo.findAllByAccountId(self.Integer(1))

        # Assert
        self.assertIsNotNone(account1_operations)
        self.assertGreater(account1_operations.size(), 0)

        # Проверяем, что все найденные операции имеют account_id = 1
        for op in account1_operations:
            self.assertEqual(op.getAccountId(), 1)

    def test_04_find_all_by_category_id(self):
        """Тест 04: Поиск операций по ID категории"""
        # Arrange
        operation1 = self.create_test_operation(100000, category_id=1, comment="Операция категории 1")
        operation2 = self.create_test_operation(200000, category_id=1, comment="Операция категории 1 еще")
        operation3 = self.create_test_operation(300000, category_id=2, comment="Операция категории 2")

        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        saved3 = self.repo.save(operation3)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        category1_operations = self.repo.findAllByCategoryId(self.Integer(1))

        # Assert
        self.assertIsNotNone(category1_operations)
        self.assertGreater(category1_operations.size(), 0)

        # Проверяем, что все найденные операции имеют category_id = 1
        for op in category1_operations:
            self.assertEqual(op.getCategoryId(), 1)

    def test_05_find_all_by_comment(self):
        """Тест 05: Поиск операций по комментарию"""
        # Arrange
        unique_comment = f"Уникальный комментарий {self.LocalDateTime.now().toString()}"
        operation1 = self.create_test_operation(100000, comment=unique_comment)
        operation2 = self.create_test_operation(200000, comment=unique_comment)

        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        found_operations = self.repo.findAllByComment(unique_comment)

        # Assert
        self.assertIsNotNone(found_operations)
        self.assertEqual(found_operations.size(), 2)

        # Проверяем, что все найденные операции имеют правильный комментарий
        for op in found_operations:
            self.assertEqual(op.getComment(), unique_comment)

    def test_06_find_all_by_currency_id(self):
        """Тест 06: Поиск операций по ID валюты"""
        # Arrange
        operation1 = self.create_test_operation(100000, currency_id=1, comment="Операция RUB")
        operation2 = self.create_test_operation(200000, currency_id=1, comment="Операция RUB еще")
        operation3 = self.create_test_operation(300000, currency_id=2, comment="Операция USD")

        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        saved3 = self.repo.save(operation3)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        rub_operations = self.repo.findAllByCurrencyId(self.Integer(1))  # RUB

        # Assert
        self.assertIsNotNone(rub_operations)
        self.assertGreater(rub_operations.size(), 0)

        # Проверяем, что все найденные операции имеют currency_id = 1
        for op in rub_operations:
            self.assertEqual(op.getCurrencyId(), 1)

    def test_07_find_all_by_date(self):
        """Тест 07: Поиск операций по дате"""
        # Arrange
        # Создаем дату с текущим временем для более реалистичного теста
        now = self.LocalDateTime.now()
        test_date = now.withHour(0).withMinute(0).withSecond(0).withNano(0)  # Сегодня в 00:00

        operation1 = self.create_test_operation(100000, comment="Операция на дату")
        operation1.setDate(test_date)

        operation2 = self.create_test_operation(200000, comment="Операция на другую дату")
        operation2.setDate(test_date.plusDays(1))

        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        # Ищем по дате в начале дня (00:00:00)
        date_operations = self.repo.findAllByDate(test_date)

        # Assert
        self.assertIsNotNone(date_operations)
        self.assertGreater(date_operations.size(), 0)

        # Проверяем, что все найденные операции имеют правильную дату
        # Сравниваем только дату (без времени)
        for op in date_operations:
            op_date = op.getDate()
            # Сравниваем только дату, игнорируя время
            self.assertEqual(op_date.getYear(), test_date.getYear())
            self.assertEqual(op_date.getMonthValue(), test_date.getMonthValue())
            self.assertEqual(op_date.getDayOfMonth(), test_date.getDayOfMonth())

    def test_08_find_all_by_type(self):
        """Тест 08: Поиск операций по типу"""
        # Arrange
        operation1 = self.create_test_operation(100000, type=1, comment="Операция типа 1")
        operation2 = self.create_test_operation(200000, type=1, comment="Операция типа 1 еще")
        operation3 = self.create_test_operation(300000, type=2, comment="Операция типа 2")

        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        saved3 = self.repo.save(operation3)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        type1_operations = self.repo.findAllByType(self.Integer(1))

        # Assert
        self.assertIsNotNone(type1_operations)
        self.assertGreater(type1_operations.size(), 0)

        # Проверяем, что все найденные операции имеют type = 1
        for op in type1_operations:
            self.assertEqual(op.getType(), 1)

    def test_09_find_by_id(self):
        """Тест 09: Поиск операции по ID"""
        # Arrange
        operation = self.create_test_operation(250000, comment="Операция для поиска")
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Act
        found_operation = self.repo.findById(saved_operation.getId())

        # Assert
        self.assertTrue(found_operation.isPresent())
        found = found_operation.get()
        self.assertEqual(found.getId(), saved_operation.getId())
        self.assertEqual(found.getAmount(), 250000)
        self.assertEqual(found.getComment(), "Операция для поиска")

    def test_10_save_operation(self):
        """Тест 10: Сохранение новой операции"""
        # Arrange
        operation = self.create_test_operation(300000, comment="Операция для сохранения")

        # Act
        saved_operation = self.repo.save(operation)

        # Assert
        self.assertIsNotNone(saved_operation)
        self.assertGreater(saved_operation.getId(), 0)
        self.assertEqual(saved_operation.getAmount(), 300000)
        self.assertEqual(saved_operation.getComment(), "Операция для сохранения")
        self.assertEqual(saved_operation.getType(), 1)
        self.assertEqual(saved_operation.getCategoryId(), 1)
        self.assertEqual(saved_operation.getAccountId(), 1)
        self.assertEqual(saved_operation.getCurrencyId(), 1)

        # Сохраняем ID для очистки
        self.test_operation_ids.append(saved_operation.getId())

    def test_11_update_operation(self):
        """Тест 11: Обновление операции"""
        # Arrange
        operation = self.create_test_operation(400000, comment="Операция для обновления")
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Act
        saved_operation.setAmount(500000)
        saved_operation.setComment("Обновленная операция")
        saved_operation.setType(2)
        saved_operation.setUpdatedBy("test_update")
        updated_operation = self.repo.update(saved_operation)

        # Assert
        self.assertEqual(updated_operation.getAmount(), 500000)
        self.assertEqual(updated_operation.getComment(), "Обновленная операция")
        self.assertEqual(updated_operation.getType(), 2)
        self.assertEqual(updated_operation.getUpdatedBy(), "test_update")

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        found_operation = found.get()
        self.assertEqual(found_operation.getAmount(), 500000)
        self.assertEqual(found_operation.getComment(), "Обновленная операция")
        self.assertEqual(found_operation.getType(), 2)

    def test_12_operation_with_transfer_fields(self):
        """Тест 12: Создание операции с полями перевода (mapRowSafe)"""
        # Arrange
        operation = self.create_test_operation(
            amount=100000,
            to_account_id=self.Integer(2),
            to_currency_id=self.Integer(2),
            to_amount=150000,
            comment="Операция перевода",
        )

        # Act
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Assert
        self.assertEqual(saved_operation.getToAccountId(), 2)
        self.assertEqual(saved_operation.getToCurrencyId(), 2)
        self.assertEqual(saved_operation.getToAmount(), 150000)

        # Проверяем, что поля перевода корректно сохранились в БД
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        found_operation = found.get()
        self.assertEqual(found_operation.getToAccountId(), 2)
        self.assertEqual(found_operation.getToCurrencyId(), 2)
        self.assertEqual(found_operation.getToAmount(), 150000)

    def test_13_operation_with_null_transfer_fields(self):
        """Тест 13: Создание операции с NULL полями перевода (mapRowSafe)"""
        # Arrange
        operation = self.create_test_operation(
            amount=100000, to_account_id=None, to_currency_id=None, to_amount=None, comment="Операция без перевода"
        )

        # Act
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Assert
        self.assertIsNone(saved_operation.getToAccountId())
        self.assertIsNone(saved_operation.getToCurrencyId())
        self.assertIsNone(saved_operation.getToAmount())

        # Проверяем, что NULL значения корректно сохранились в БД
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        found_operation = found.get()
        self.assertIsNone(found_operation.getToAccountId())
        self.assertIsNone(found_operation.getToCurrencyId())
        self.assertIsNone(found_operation.getToAmount())

    def test_14_update_transfer_fields(self):
        """Тест 14: Обновление полей перевода (mapRowSafe)"""
        # Arrange
        operation = self.create_test_operation(100000, comment="Операция для обновления перевода")
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Act
        saved_operation.setToAccountId(self.Integer(3))
        saved_operation.setToCurrencyId(self.Integer(3))
        saved_operation.setToAmount(self.Integer(200000))
        updated_operation = self.repo.update(saved_operation)

        # Assert
        self.assertEqual(updated_operation.getToAccountId(), 3)
        self.assertEqual(updated_operation.getToCurrencyId(), 3)
        self.assertEqual(updated_operation.getToAmount(), 200000)

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        found_operation = found.get()
        self.assertEqual(found_operation.getToAccountId(), 3)
        self.assertEqual(found_operation.getToCurrencyId(), 3)
        self.assertEqual(found_operation.getToAmount(), 200000)

    def test_15_find_nonexistent_operation(self):
        """Тест 15: Поиск несуществующей операции (findById)"""
        # Act
        found_operation = self.repo.findById(self.Integer(99999))

        # Assert
        self.assertFalse(found_operation.isPresent())

    def test_16_delete_nonexistent_operation(self):
        """Тест 16: Удаление несуществующей операции (deleteById)"""
        # Act
        deleted = self.repo.deleteById(self.Integer(99999), "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_17_find_by_nonexistent_account_id(self):
        """Тест 17: Поиск операций по несуществующему account_id (findAllByAccountId)"""
        # Act
        operations = self.repo.findAllByAccountId(self.Integer(999))

        # Assert
        self.assertIsNotNone(operations)
        self.assertEqual(operations.size(), 0)

    def test_18_find_by_nonexistent_category_id(self):
        """Тест 18: Поиск операций по несуществующему category_id (findAllByCategoryId)"""
        # Act
        operations = self.repo.findAllByCategoryId(self.Integer(999))

        # Assert
        self.assertIsNotNone(operations)
        self.assertEqual(operations.size(), 0)

    def test_19_find_by_nonexistent_currency_id(self):
        """Тест 19: Поиск операций по несуществующему currency_id (findAllByCurrencyId)"""
        # Act
        operations = self.repo.findAllByCurrencyId(self.Integer(999))

        # Assert
        self.assertIsNotNone(operations)
        self.assertEqual(operations.size(), 0)

    def test_20_find_by_nonexistent_type(self):
        """Тест 20: Поиск операций по несуществующему типу (findAllByType)"""
        # Act
        operations = self.repo.findAllByType(self.Integer(999))

        # Assert
        self.assertIsNotNone(operations)
        self.assertEqual(operations.size(), 0)

    def test_21_operation_soft_delete_behavior(self):
        """Тест 21: Проверка поведения soft delete (deleteById)"""
        # Arrange
        operation = self.create_test_operation(100000, comment="Операция для soft delete")
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Act - удаляем операцию
        deleted = self.repo.deleteById(saved_operation.getId(), "test_soft_delete")

        # Assert
        self.assertTrue(deleted)

        # Операция должна быть найдена, но помечена как удаленная
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        deleted_operation = found.get()
        self.assertIsNotNone(deleted_operation.getDeleteTime())
        self.assertEqual(deleted_operation.getDeletedBy(), "test_soft_delete")

        # Операция должна остаться в списке всех операций (включая удаленные)
        all_operations = self.repo.findAll()
        operation_ids = []
        for op in all_operations:
            operation_ids.append(op.getId())
        self.assertIn(saved_operation.getId(), operation_ids)

    def test_22_operation_with_special_characters(self):
        """Тест 22: Создание операции со специальными символами в комментарии (mapRowSafe)"""
        # Arrange
        special_comment = "Операция с символами: €, ₽, $, %, &, <, >, \"', (тест)"
        operation = self.create_test_operation(100000, comment=special_comment)

        # Act
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Assert
        self.assertEqual(saved_operation.getComment(), special_comment)

        # Проверяем, что специальные символы корректно сохранились в БД
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        found_operation = found.get()
        self.assertEqual(found_operation.getComment(), special_comment)

    def test_23_operation_with_long_comment(self):
        """Тест 23: Создание операции с длинным комментарием (mapRowSafe)"""
        # Arrange
        long_comment = "Очень длинный комментарий к операции с множеством символов для тестирования граничных случаев и проверки корректной работы с большими текстовыми полями в базе данных"
        operation = self.create_test_operation(100000, comment=long_comment)

        # Act
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Assert
        self.assertEqual(saved_operation.getComment(), long_comment)

        # Проверяем, что длинный комментарий корректно сохранился в БД
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        found_operation = found.get()
        self.assertEqual(found_operation.getComment(), long_comment)

    def test_24_operation_large_amounts(self):
        """Тест 24: Тестирование больших сумм операций"""
        # Arrange
        large_amount = 999999999  # Большая сумма в копейках
        operation = self.create_test_operation(large_amount, comment="Операция с большой суммой")

        # Act
        saved_operation = self.repo.save(operation)
        self.test_operation_ids.append(saved_operation.getId())

        # Assert
        self.assertEqual(saved_operation.getAmount(), large_amount)

        # Проверяем, что большая сумма корректно сохранилась в БД
        found = self.repo.findById(saved_operation.getId())
        self.assertTrue(found.isPresent())
        found_operation = found.get()
        self.assertEqual(found_operation.getAmount(), large_amount)

    def test_25_operation_different_types(self):
        """Тест 25: Тестирование различных типов операций"""
        # Arrange
        operation1 = self.create_test_operation(100000, type=1, comment="Доход")
        operation2 = self.create_test_operation(200000, type=2, comment="Расход")
        operation3 = self.create_test_operation(300000, type=3, comment="Перевод")

        # Act
        saved1 = self.repo.save(operation1)
        saved2 = self.repo.save(operation2)
        saved3 = self.repo.save(operation3)
        self.test_operation_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Assert
        self.assertEqual(saved1.getType(), 1)
        self.assertEqual(saved2.getType(), 2)
        self.assertEqual(saved3.getType(), 3)

        # Проверяем поиск по типам
        type1_ops = self.repo.findAllByType(self.Integer(1))
        type2_ops = self.repo.findAllByType(self.Integer(2))
        type3_ops = self.repo.findAllByType(self.Integer(3))

        self.assertGreater(type1_ops.size(), 0)
        self.assertGreater(type2_ops.size(), 0)
        self.assertGreater(type3_ops.size(), 0)


if __name__ == '__main__':
    unittest.main(verbosity=2)
