import os
import sys
import unittest

from tests.backend.test_common import cleanup_example, get_java_class, setup_example

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestBudgetRepository(unittest.TestCase):
    """Юнит-тесты для BudgetRepository"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.Budget = get_java_class("com.sadengineer.budgetmaster.backend.model.Budget")
        cls.BudgetRepository = get_java_class("com.sadengineer.budgetmaster.backend.repository.BudgetRepository")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        # Создаем репозиторий
        cls.repo = cls.BudgetRepository(cls.db_manager.db_path)

        # Список ID тестовых записей для очистки
        cls.test_budget_ids = []

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            # Получаем менеджер базы данных
            db_manager = cls.db_manager

            # Удаляем тестовые записи по ID
            deleted_count = 0
            for budget_id in cls.test_budget_ids:
                try:
                    success = db_manager.execute_update("DELETE FROM budgets WHERE id = ?", (budget_id,))
                    if success:
                        deleted_count += 1
                    else:
                        print(f"Ошибка при удалении бюджета {budget_id}")
                except Exception as e:
                    print(f"Ошибка при удалении бюджета {budget_id}: {e}")

            if deleted_count > 0:
                print(f"Удалено {deleted_count} тестовых бюджетов из базы данных")

            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            # Не завершаем JVM здесь - пусть это делает atexit
            pass

    def setUp(self):
        """Настройка перед каждым тестом"""
        self.max_position = self.repo.getMaxPosition()

    def create_test_budget(self, amount=100000, currency_id=1, category_id=None, position=None):
        """Создает тестовый бюджет"""
        if position is None:
            position = self.repo.getMaxPosition() + 1

        budget = self.Budget()
        budget.setAmount(amount)
        budget.setCurrencyId(currency_id)
        budget.setCategoryId(category_id)
        budget.setPosition(position)
        budget.setCreatedBy("test_user")
        budget.setUpdatedBy("test_user")

        # Устанавливаем даты
        now = self.LocalDateTime.now()
        budget.setCreateTime(now)
        budget.setUpdateTime(now)
        budget.setDeleteTime(None)

        return budget

    def test_01_delete_by_id(self):
        """Тест 01: Удаление бюджета по ID"""
        # Arrange
        budget = self.create_test_budget(50000)
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Act
        deleted = self.repo.deleteById(saved_budget.getId(), "test_delete")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что бюджет помечен как удаленный
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        deleted_budget = found.get()
        self.assertIsNotNone(deleted_budget.getDeleteTime())
        self.assertEqual(deleted_budget.getDeletedBy(), "test_delete")

    def test_02_delete_by_category_id(self):
        """Тест 02: Удаление бюджета по ID категории"""
        # Arrange
        budget = self.create_test_budget(75000, category_id=self.Integer(1))
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Act
        deleted = self.repo.deleteByCategoryId(self.Integer(1), "test_delete_category")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что бюджет помечен как удаленный
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        deleted_budget = found.get()
        self.assertIsNotNone(deleted_budget.getDeleteTime())
        self.assertEqual(deleted_budget.getDeletedBy(), "test_delete_category")

    def test_03_find_all(self):
        """Тест 03: Получение всех бюджетов"""
        # Arrange
        budget1 = self.create_test_budget(100000, position=self.repo.getMaxPosition() + 1)
        budget2 = self.create_test_budget(200000, position=self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(budget1)
        saved2 = self.repo.save(budget2)
        self.test_budget_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        all_budgets = self.repo.findAll()

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(all_budgets)
        self.assertGreater(all_budgets.size(), 0)

        # Проверяем, что наши бюджеты есть в списке
        amounts = []
        for budget in all_budgets:
            amounts.append(budget.getAmount())
        self.assertIn(100000, amounts)
        self.assertIn(200000, amounts)

    def test_04_find_by_category_id(self):
        """Тест 04: Поиск бюджета по ID категории"""
        # Arrange
        budget = self.create_test_budget(150000, category_id=self.Integer(2))
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Act
        found_budget = self.repo.findByCategoryId(self.Integer(2))

        # Assert
        self.assertTrue(found_budget.isPresent())
        found = found_budget.get()
        self.assertEqual(found.getId(), saved_budget.getId())
        self.assertEqual(found.getCategoryId(), 2)
        self.assertEqual(found.getAmount(), 150000)

    def test_05_find_all_by_currency_id(self):
        """Тест 05: Поиск бюджетов по ID валюты"""
        # Arrange
        budget1 = self.create_test_budget(100000, currency_id=1)  # RUB
        budget2 = self.create_test_budget(200000, currency_id=1)  # RUB
        budget3 = self.create_test_budget(300000, currency_id=2)  # USD

        saved1 = self.repo.save(budget1)
        saved2 = self.repo.save(budget2)
        saved3 = self.repo.save(budget3)
        self.test_budget_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        rub_budgets = self.repo.findAllByCurrencyId(self.Integer(1))  # RUB

        # Assert
        self.assertIsNotNone(rub_budgets)
        self.assertGreater(rub_budgets.size(), 0)

        # Проверяем, что все найденные бюджеты имеют валюту RUB
        for budget in rub_budgets:
            self.assertEqual(budget.getCurrencyId(), 1)

    def test_06_find_by_id(self):
        """Тест 06: Поиск бюджета по ID"""
        # Arrange
        budget = self.create_test_budget(250000)
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Act
        found_budget = self.repo.findById(saved_budget.getId())

        # Assert
        self.assertTrue(found_budget.isPresent())
        found = found_budget.get()
        self.assertEqual(found.getId(), saved_budget.getId())
        self.assertEqual(found.getAmount(), 250000)

    def test_07_get_max_position(self):
        """Тест 07: Получение максимальной позиции"""
        # Arrange
        budget1 = self.create_test_budget(100000, position=self.repo.getMaxPosition() + 1)
        budget2 = self.create_test_budget(200000, position=self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(budget1)
        saved2 = self.repo.save(budget2)
        self.test_budget_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        max_position = self.repo.getMaxPosition()

        # Assert
        self.assertIsInstance(max_position, int)
        self.assertGreater(max_position, 0)
        self.assertGreaterEqual(max_position, saved2.getPosition())

    def test_08_save_budget(self):
        """Тест 08: Сохранение нового бюджета"""
        # Arrange
        budget = self.create_test_budget(300000, currency_id=self.Integer(1), category_id=self.Integer(1))

        # Act
        saved_budget = self.repo.save(budget)

        # Assert
        self.assertIsNotNone(saved_budget)
        self.assertGreater(saved_budget.getId(), 0)
        self.assertEqual(saved_budget.getAmount(), 300000)
        self.assertEqual(saved_budget.getCurrencyId(), 1)
        self.assertEqual(saved_budget.getCategoryId(), 1)
        self.assertEqual(saved_budget.getPosition(), budget.getPosition())

        # Сохраняем ID для очистки
        self.test_budget_ids.append(saved_budget.getId())

    def test_09_update_budget(self):
        """Тест 09: Обновление бюджета"""
        # Arrange
        budget = self.create_test_budget(400000, currency_id=self.Integer(1), category_id=self.Integer(1))
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Act
        saved_budget.setAmount(500000)
        saved_budget.setCurrencyId(self.Integer(2))
        saved_budget.setCategoryId(self.Integer(2))
        saved_budget.setUpdatedBy("test_update")
        updated_budget = self.repo.update(saved_budget)

        # Assert
        self.assertEqual(updated_budget.getAmount(), 500000)
        self.assertEqual(updated_budget.getCurrencyId(), 2)
        self.assertEqual(updated_budget.getCategoryId(), 2)
        self.assertEqual(updated_budget.getUpdatedBy(), "test_update")

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        found_budget = found.get()
        self.assertEqual(found_budget.getAmount(), 500000)
        self.assertEqual(found_budget.getCurrencyId(), 2)
        self.assertEqual(found_budget.getCategoryId(), 2)

    def test_10_budget_with_null_category_id(self):
        """Тест 10: Создание бюджета с NULL category_id (mapRowSafe)"""
        # Arrange
        budget = self.create_test_budget(100000, category_id=None)

        # Act
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Assert
        self.assertIsNone(saved_budget.getCategoryId())

        # Проверяем, что NULL значение корректно сохранилось в БД
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        found_budget = found.get()
        self.assertIsNone(found_budget.getCategoryId())

    def test_11_budget_with_category_id(self):
        """Тест 11: Создание бюджета с category_id (mapRowSafe)"""
        # Arrange
        budget = self.create_test_budget(200000, category_id=self.Integer(3))

        # Act
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Assert
        self.assertEqual(saved_budget.getCategoryId(), 3)

        # Проверяем, что category_id корректно сохранился в БД
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        found_budget = found.get()
        self.assertEqual(found_budget.getCategoryId(), 3)

    def test_12_update_category_id(self):
        """Тест 12: Обновление category_id бюджета (mapRowSafe)"""
        # Arrange
        budget = self.create_test_budget(300000, category_id=self.Integer(1))
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Act
        saved_budget.setCategoryId(self.Integer(4))
        updated_budget = self.repo.update(saved_budget)

        # Assert
        self.assertEqual(updated_budget.getCategoryId(), 4)

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        found_budget = found.get()
        self.assertEqual(found_budget.getCategoryId(), 4)

    def test_13_find_nonexistent_budget(self):
        """Тест 13: Поиск несуществующего бюджета (findById)"""
        # Act
        found_budget = self.repo.findById(self.Integer(99999))

        # Assert
        self.assertFalse(found_budget.isPresent())

    def test_14_delete_nonexistent_budget(self):
        """Тест 14: Удаление несуществующего бюджета (deleteById)"""
        # Act
        deleted = self.repo.deleteById(self.Integer(99999), "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_15_delete_nonexistent_budget_by_category(self):
        """Тест 15: Удаление несуществующего бюджета по category_id (deleteByCategoryId)"""
        # Act
        deleted = self.repo.deleteByCategoryId(self.Integer(999), "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_16_find_by_nonexistent_category_id(self):
        """Тест 16: Поиск бюджета по несуществующему category_id (findByCategoryId)"""
        # Act
        found_budget = self.repo.findByCategoryId(self.Integer(999))

        # Assert
        self.assertFalse(found_budget.isPresent())

    def test_17_find_by_nonexistent_currency_id(self):
        """Тест 17: Поиск бюджетов по несуществующему currency_id (findAllByCurrencyId)"""
        # Act
        budgets = self.repo.findAllByCurrencyId(self.Integer(999))

        # Assert
        self.assertIsNotNone(budgets)
        self.assertEqual(budgets.size(), 0)

    def test_18_budget_position_ordering(self):
        """Тест 18: Проверка упорядочивания бюджетов по позиции (getMaxPosition)"""
        # Arrange
        budget1 = self.create_test_budget(100000, position=1)
        budget2 = self.create_test_budget(200000, position=2)
        budget3 = self.create_test_budget(300000, position=3)

        saved1 = self.repo.save(budget1)
        saved2 = self.repo.save(budget2)
        saved3 = self.repo.save(budget3)
        self.test_budget_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        all_budgets = self.repo.findAll()

        # Assert
        # Проверяем, что бюджеты с нашими позициями есть в списке
        positions = []
        for budget in all_budgets:
            positions.append(budget.getPosition())
        self.assertIn(1, positions)
        self.assertIn(2, positions)
        self.assertIn(3, positions)

    def test_19_budget_soft_delete_behavior(self):
        """Тест 19: Проверка поведения soft delete (deleteById)"""
        # Arrange
        budget = self.create_test_budget(100000)
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Act - удаляем бюджет
        deleted = self.repo.deleteById(saved_budget.getId(), "test_soft_delete")

        # Assert
        self.assertTrue(deleted)

        # Бюджет должен быть найден, но помечен как удаленный
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        deleted_budget = found.get()
        self.assertIsNotNone(deleted_budget.getDeleteTime())
        self.assertEqual(deleted_budget.getDeletedBy(), "test_soft_delete")

        # Бюджет должен остаться в списке всех бюджетов (включая удаленные)
        all_budgets = self.repo.findAll()
        budget_ids = []
        for budget in all_budgets:
            budget_ids.append(budget.getId())
        self.assertIn(saved_budget.getId(), budget_ids)

    def test_20_budget_large_amounts(self):
        """Тест 20: Тестирование больших сумм бюджета"""
        # Arrange
        large_amount = 999999999  # Большая сумма в копейках
        budget = self.create_test_budget(large_amount)

        # Act
        saved_budget = self.repo.save(budget)
        self.test_budget_ids.append(saved_budget.getId())

        # Assert
        self.assertEqual(saved_budget.getAmount(), large_amount)

        # Проверяем, что большая сумма корректно сохранилась в БД
        found = self.repo.findById(saved_budget.getId())
        self.assertTrue(found.isPresent())
        found_budget = found.get()
        self.assertEqual(found_budget.getAmount(), large_amount)


if __name__ == '__main__':
    unittest.main(verbosity=2)
