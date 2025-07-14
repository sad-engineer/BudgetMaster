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


class TestBudgetService(unittest.TestCase):
    """Юнит-тесты для BudgetService"""

    @classmethod
    def setUpClass(cls):
        if not setup_example():
            raise Exception("Не удалось настроить окружение для тестов")

        # Импортируем Java классы
        cls.BudgetService = get_java_class("service.BudgetService")
        cls.BudgetRepository = get_java_class("repository.BudgetRepository")
        cls.Budget = get_java_class("model.Budget")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        cls.test_budget_ids = []
        cls.db_path = test_data_manager.db_manager.db_path
        cls.repository = cls.BudgetRepository(cls.db_path)
        cls.service = cls.BudgetService(cls.repository, "test_user")

    @classmethod
    def tearDownClass(cls):
        try:
            db_manager = test_data_manager.db_manager
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
            pass

    def test_01_create_budget(self):
        """Тест 01: Создание нового бюджета"""
        category_id = self.Integer(1)
        amount = self.Integer(10000)
        currency_id = self.Integer(1)
        budget = self.service.create(category_id, amount, currency_id)
        self.test_budget_ids.append(budget.getId())
        self.assertIsNotNone(budget)
        self.assertEqual(budget.getCategoryId(), category_id)
        self.assertEqual(budget.getAmount(), amount)
        self.assertEqual(budget.getCurrencyId(), currency_id)
        self.assertEqual(budget.getCreatedBy(), "test_user")
        self.assertEqual(budget.getUpdatedBy(), "test_user")
        self.assertIsNotNone(budget.getCreateTime())
        self.assertIsNotNone(budget.getUpdateTime())
        self.assertGreater(budget.getPosition(), 0)

    def test_02_get_all_budgets(self):
        """Тест 02: Получение всех бюджетов"""
        b1 = self.service.create(self.Integer(1), self.Integer(1000), self.Integer(1))
        b2 = self.service.create(self.Integer(2), self.Integer(2000), self.Integer(2))
        self.test_budget_ids.append(b1.getId())
        self.test_budget_ids.append(b2.getId())
        budgets = self.service.getAll()
        self.assertIsNotNone(budgets)
        self.assertGreater(len(budgets), 0)
        ids = [b.getId() for b in budgets]
        self.assertIn(b1.getId(), ids)
        self.assertIn(b2.getId(), ids)

    def test_03_get_budget_by_id(self):
        """Тест 03: Получение бюджета по ID"""
        budget = self.service.create(self.Integer(1), self.Integer(5000), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        found = self.service.getById(budget.getId())
        self.assertTrue(found.isPresent())
        self.assertEqual(found.get().getId(), budget.getId())

    def test_04_get_budget_by_id_not_found(self):
        """Тест 04: Получение бюджета по несуществующему ID"""
        found = self.service.getById(999999)
        self.assertFalse(found.isPresent())

    def test_05_delete_budget(self):
        """Тест 05: Удаление бюджета по ID"""
        budget = self.service.create(self.Integer(1), self.Integer(1234), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        result = self.service.delete(budget.getId())
        self.assertTrue(result)
        # Проверяем, что бюджет помечен как удалённый
        found = self.repository.findById(budget.getId())
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_06_restore_budget(self):
        """Тест 06: Восстановление удалённого бюджета"""
        budget = self.service.create(self.Integer(1), self.Integer(2222), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.repository.deleteById(budget.getId(), "test_user")
        deleted = self.repository.findById(budget.getId()).get()
        restored = self.service.restore(deleted)
        self.assertIsNotNone(restored)
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())
        self.assertEqual(restored.getUpdatedBy(), "test_user")

    def test_07_restore_budget_by_id(self):
        """Тест 07: Восстановление бюджета по ID"""
        budget = self.service.create(self.Integer(1), self.Integer(3333), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.repository.deleteById(budget.getId(), "test_user")
        restored = self.service.restore(budget.getId())
        self.assertIsNotNone(restored)
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())

    def test_08_restore_budget_by_id_not_found(self):
        """Тест 08: Восстановление бюджета по несуществующему ID"""
        restored = self.service.restore(999999)
        self.assertIsNone(restored)

    def test_09_set_amount(self):
        """Тест 09: Установка суммы бюджета"""
        budget = self.service.create(self.Integer(1), self.Integer(100), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        new_amount = 9999
        updated = self.service.setAmount(budget.getId(), new_amount)
        self.assertIsNotNone(updated)
        self.assertEqual(updated.getAmount(), new_amount)
        self.assertEqual(updated.getUpdatedBy(), "test_user")

    def test_10_set_amount_not_found(self):
        """Тест 10: Установка суммы для несуществующего бюджета"""
        updated = self.service.setAmount(999999, 12345)
        self.assertIsNone(updated)

    def test_11_get_by_category_id(self):
        """Тест 11: Получение бюджета по ID категории"""
        category_id = self.Integer(42)
        budget = self.service.create(category_id, self.Integer(5555), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        found = self.service.getByCategoryId(category_id)
        self.assertTrue(found.isPresent())
        self.assertEqual(found.get().getId(), budget.getId())

    def test_12_delete_by_category_id(self):
        """Тест 12: Удаление бюджета по ID категории"""
        category_id = self.Integer(77)
        budget = self.service.create(category_id, self.Integer(8888), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        result = self.service.deleteByCategoryId(category_id)
        self.assertTrue(result)

        # Проверяем, что бюджет удален
        found = self.service.getByCategoryId(category_id)
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_13_get_all_by_currency_id(self):
        """Тест 13: Получение всех бюджетов по ID валюты"""

        b1 = self.service.create(self.Integer(1), self.Integer(100), self.Integer(1))
        b2 = self.service.create(self.Integer(2), self.Integer(200), self.Integer(2))
        self.test_budget_ids.append(b1.getId())
        self.test_budget_ids.append(b2.getId())
        budgets = self.service.getAllByCurrencyId(1)
        self.assertIsNotNone(budgets)
        self.assertGreater(budgets.size(), 0)
        # Проверяем, что хотя бы один бюджет с нужной валютой есть
        found = False
        for b in budgets:
            if b.getCurrencyId() == self.Integer(1):
                found = True
        self.assertTrue(found)

    def test_14_is_budget_deleted(self):
        """Тест 14: Проверка удаления бюджета"""
        budget = self.service.create(self.Integer(1), self.Integer(123), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.repository.deleteById(budget.getId(), "test_user")
        deleted = self.repository.findById(budget.getId()).get()
        self.assertTrue(self.service.isBudgetDeleted(deleted))
        # Восстановим для очистки
        self.service.restore(deleted)

    def test_15_change_position(self):
        """Тест 15: Изменение позиции бюджета"""
        position = self.repository.getMaxPosition()
        b1 = self.service.create(self.Integer(1), self.Integer(100), self.Integer(1))
        b2 = self.service.create(self.Integer(2), self.Integer(200), self.Integer(2))
        b3 = self.service.create(self.Integer(3), self.Integer(300), self.Integer(3))
        self.test_budget_ids.extend([b1.getId(), b2.getId(), b3.getId()])
        # Перемещаем b1 на позицию 3
        result = self.service.changePosition(b1, position + 3)
        self.assertEqual(result.getPosition(), position + 3)
        # Проверяем, что другие бюджеты сдвинулись
        updated_b2 = self.repository.findById(b2.getId()).get()
        updated_b3 = self.repository.findById(b3.getId()).get()
        self.assertEqual(updated_b2.getPosition(), position + 1)
        self.assertEqual(updated_b3.getPosition(), position + 2)

    def test_16_change_position_up(self):
        """Тест 16: Перемещение бюджета вверх"""
        position = self.repository.getMaxPosition()
        b1 = self.service.create(self.Integer(1), self.Integer(100), self.Integer(1))
        b2 = self.service.create(self.Integer(2), self.Integer(200), self.Integer(2))
        b3 = self.service.create(self.Integer(3), self.Integer(300), self.Integer(3))
        self.test_budget_ids.extend([b1.getId(), b2.getId(), b3.getId()])
        # Перемещаем b3 на позицию 1
        result = self.service.changePosition(b3, position + 1)
        self.assertEqual(result.getPosition(), position + 1)
        updated_b1 = self.repository.findById(b1.getId()).get()
        updated_b2 = self.repository.findById(b2.getId()).get()
        self.assertEqual(updated_b1.getPosition(), position + 2)
        self.assertEqual(updated_b2.getPosition(), position + 3)

    def test_17_change_position_by_old_new(self):
        """Тест 17: Изменение позиции по старой и новой позиции"""
        position = self.repository.getMaxPosition()
        b1 = self.service.create(self.Integer(1), self.Integer(100), self.Integer(1))
        b2 = self.service.create(self.Integer(2), self.Integer(200), self.Integer(2))
        b3 = self.service.create(self.Integer(3), self.Integer(300), self.Integer(3))
        self.test_budget_ids.extend([b1.getId(), b2.getId(), b3.getId()])
        result = self.service.changePosition(position + 1, position + 3)
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), position + 3)

    def test_18_change_position_by_old_new_not_found(self):
        """Тест 18: Изменение позиции по несуществующей старой позиции"""
        result = self.service.changePosition(999, 1)
        self.assertIsNone(result)

    def test_19_set_user_unsupported(self):
        """Тест 19: Попытка сменить пользователя"""
        with self.assertRaises(Exception):
            self.service.setUser("new_user")

    def test_20_create_budget_with_special_amount(self):
        """Тест 20: Создание бюджета с необычной суммой"""
        budget = self.service.create(self.Integer(1), self.Integer(0), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.assertEqual(budget.getAmount(), 0)
        budget2 = self.service.create(self.Integer(1), self.Integer(-100), self.Integer(1))
        self.test_budget_ids.append(budget2.getId())
        self.assertEqual(budget2.getAmount(), -100)

    def test_21_budget_position_sequence(self):
        """Тест 21: Последовательность позиций бюджетов"""
        budgets = []
        position = self.repository.getMaxPosition()
        for i in range(4):
            b = self.service.create(self.Integer(i + 1), self.Integer(100 * (i + 1)), self.Integer(1))
            budgets.append(b)
            self.test_budget_ids.append(b.getId())
        for i, b in enumerate(budgets):
            self.assertEqual(b.getPosition(), position + i + 1)

    def test_22_delete_and_restore_cycle(self):
        """Тест 22: Цикл удаления и восстановления бюджета"""
        budget = self.service.create(self.Integer(1), self.Integer(12345), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.service.delete(budget.getId())
        restored1 = self.service.restore(budget.getId())
        self.assertEqual(restored1.getId(), budget.getId())
        self.service.delete(budget.getId())
        restored2 = self.service.restore(budget.getId())
        self.assertEqual(restored2.getId(), budget.getId())

    def test_23_create_budget_with_different_currencies(self):
        """Тест 23: Создание бюджетов с разными валютами"""
        budget1 = self.service.create(self.Integer(1), self.Integer(1000), self.Integer(1))
        budget2 = self.service.create(self.Integer(2), self.Integer(2000), self.Integer(2))
        self.test_budget_ids.append(budget1.getId())
        self.test_budget_ids.append(budget2.getId())

        self.assertEqual(budget1.getCurrencyId(), self.Integer(1))
        self.assertEqual(budget2.getCurrencyId(), self.Integer(2))

        # Проверяем получение бюджетов по валюте
        budgets_currency_1 = self.service.getAllByCurrencyId(1)
        budgets_currency_2 = self.service.getAllByCurrencyId(2)

        self.assertGreater(budgets_currency_1.size(), 0)
        self.assertGreater(budgets_currency_2.size(), 0)

        # Проверяем, что наши бюджеты есть в соответствующих списках
        currency_1_ids = []
        currency_2_ids = []
        for b in budgets_currency_1:
            currency_1_ids.append(b.getId())
        for b in budgets_currency_2:
            currency_2_ids.append(b.getId())

        self.assertIn(budget1.getId(), currency_1_ids)
        self.assertIn(budget2.getId(), currency_2_ids)


if __name__ == '__main__':
    unittest.main()
