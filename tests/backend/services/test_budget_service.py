import os
import sys
import unittest

from tests.backend.test_common import cleanup_example, get_java_class, setup_example

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestBudgetService(unittest.TestCase):
    """Юнит-тесты для BudgetService"""

    @classmethod
    def setUpClass(cls):
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.BudgetService = get_java_class("com.sadengineer.budgetmaster.backend.service.BudgetService")
        cls.BudgetRepository = get_java_class("com.sadengineer.budgetmaster.backend.repository.BudgetRepository")
        cls.Budget = get_java_class("com.sadengineer.budgetmaster.backend.model.Budget")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        cls.test_budget_ids = []
        cls.db_path = cls.db_manager.db_path
        cls.repository = cls.BudgetRepository(cls.db_path)
        cls.service = cls.BudgetService(cls.repository, "test_user")

    @classmethod
    def tearDownClass(cls):
        try:
            db_manager = cls.db_manager
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

    def test_01_get_budget_by_category_id(self):
        """Тест 01: Получение нового (не существующего) бюджета по ID категории"""
        category_id = self.Integer(999)
        amount = self.Integer(10000)
        currency_id = self.Integer(1)
        position = self.repository.getMaxPosition() + 1

        budget = self.service.get(category_id, amount, currency_id)
        self.test_budget_ids.append(budget.getId())

        self.assertIsNotNone(budget)
        self.assertEqual(budget.getCategoryId(), category_id)
        self.assertEqual(budget.getAmount(), amount)
        self.assertEqual(budget.getCurrencyId(), currency_id)
        self.assertEqual(budget.getPosition(), position)
        self.assertEqual(budget.getCreatedBy(), "test_user")
        self.assertIsNone(budget.getUpdatedBy())
        self.assertIsNone(budget.getDeletedBy())
        self.assertIsNotNone(budget.getCreateTime())
        self.assertIsNone(budget.getUpdateTime())
        self.assertIsNone(budget.getDeleteTime())

    def test_02_get_existing_budget_by_category_id(self):
        """Тест 02: Получение существующего бюджета по ID категории"""
        category_id = self.Integer(888)
        amount = self.Integer(20000)
        currency_id = self.Integer(2)

        budget_new = self.service.get(category_id, amount, currency_id)
        self.test_budget_ids.append(budget_new.getId())

        # Бюджет создан, его позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        budget = self.service.getByCategoryId(category_id)
        self.test_budget_ids.append(budget.getId())

        self.assertIsNotNone(budget)
        self.assertEqual(budget.getCategoryId(), category_id)
        self.assertEqual(budget.getAmount(), amount)
        self.assertEqual(budget.getCurrencyId(), currency_id)
        self.assertEqual(budget.getPosition(), position)
        self.assertEqual(budget.getCreatedBy(), "test_user")
        self.assertIsNone(budget.getUpdatedBy())
        self.assertIsNone(budget.getDeletedBy())
        self.assertIsNotNone(budget.getCreateTime())
        self.assertIsNone(budget.getUpdateTime())
        self.assertIsNone(budget.getDeleteTime())

    def test_03_get_deleted_budget_by_category_id(self):
        """Тест 03: Получение удаленного бюджета по ID категории"""
        category_id = self.Integer(777)
        amount = self.Integer(30000)
        currency_id = self.Integer(3)

        budget_new = self.service.get(category_id, amount, currency_id)
        self.test_budget_ids.append(budget_new.getId())
        self.repository.deleteById(budget_new.getId(), "test_user")

        # Бюджет не удален физически из таблицы, его позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        budget = self.service.getByCategoryId(category_id)
        self.test_budget_ids.append(budget.getId())

        self.assertIsNotNone(budget)
        self.assertEqual(budget.getCategoryId(), category_id)
        self.assertEqual(budget.getAmount(), amount)
        self.assertEqual(budget.getCurrencyId(), currency_id)
        self.assertEqual(budget.getPosition(), position)
        self.assertEqual(budget.getCreatedBy(), "test_user")
        self.assertEqual(budget.getUpdatedBy(), "test_user")
        self.assertIsNone(budget.getDeletedBy())
        self.assertIsNotNone(budget.getCreateTime())
        self.assertIsNotNone(budget.getUpdateTime())
        self.assertIsNone(budget.getDeleteTime())
        self.assertNotEqual(budget.getCreateTime(), budget.getUpdateTime())

    def test_04_get_budget_by_category_id_not_found(self):
        """Тест 04: Получение бюджета по несуществующему ID категории"""
        budget = self.service.getByCategoryId(self.Integer(999999))
        self.assertIsNone(budget)

    def test_05_create_budget_with_special_amount(self):
        """Тест 05: Создание бюджета с необычной суммой"""
        budget = self.service.get(self.Integer(666), self.Integer(0), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.assertEqual(budget.getAmount(), 0)

        with self.assertRaises(Exception):
            self.service.get(self.Integer(555), self.Integer(-100), self.Integer(1))

    def test_06_delete_budget_by_id(self):
        """Тест 06: Удаление бюджета по ID"""
        budget = self.service.get(self.Integer(444), self.Integer(1234), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        result = self.service.delete(budget.getId())
        self.assertTrue(result)
        # Проверяем, что бюджет помечен как удалённый
        found = self.repository.findById(budget.getId())
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_07_delete_budget_by_category_id(self):
        """Тест 07: Удаление бюджета по ID категории"""
        category_id = self.Integer(333)
        budget = self.service.get(category_id, self.Integer(8888), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        result = self.service.deleteByCategoryId(category_id)
        self.assertTrue(result)

        # Проверяем, что бюджет удален
        found = self.repository.findByCategoryId(category_id).get()
        self.assertIsNotNone(found)
        self.assertIsNotNone(found.getDeleteTime())
        self.assertEqual(found.getDeletedBy(), "test_user")

    def test_08_is_budget_deleted(self):
        """Тест 08: Проверка удаления бюджета"""
        budget = self.service.get(self.Integer(222), self.Integer(123), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.repository.deleteById(budget.getId(), "test_user")
        deleted = self.repository.findById(budget.getId()).get()
        self.assertTrue(deleted.isDeleted())

    def test_09_change_position(self):
        """Тест 09: Изменение позиции бюджета"""
        position = self.repository.getMaxPosition()
        b1 = self.service.get(self.Integer(111), self.Integer(100), self.Integer(1))
        b2 = self.service.get(self.Integer(112), self.Integer(200), self.Integer(2))
        b3 = self.service.get(self.Integer(113), self.Integer(300), self.Integer(3))
        self.test_budget_ids.extend([b1.getId(), b2.getId(), b3.getId()])
        # Перемещаем b1 на позицию 3
        result = self.service.changePosition(b1, position + 3)
        self.assertEqual(result.getPosition(), position + 3)
        # Проверяем, что другие бюджеты сдвинулись
        updated_b2 = self.repository.findById(b2.getId()).get()
        updated_b3 = self.repository.findById(b3.getId()).get()
        self.assertEqual(updated_b2.getPosition(), position + 1)
        self.assertEqual(updated_b3.getPosition(), position + 2)

    def test_10_change_position_up(self):
        """Тест 10: Перемещение бюджета вверх"""
        position = self.repository.getMaxPosition()
        b1 = self.service.get(self.Integer(121), self.Integer(100), self.Integer(1))
        b2 = self.service.get(self.Integer(122), self.Integer(200), self.Integer(2))
        b3 = self.service.get(self.Integer(123), self.Integer(300), self.Integer(3))
        self.test_budget_ids.extend([b1.getId(), b2.getId(), b3.getId()])
        # Перемещаем b3 на позицию 1
        result = self.service.changePosition(b3, position + 1)
        self.assertEqual(result.getPosition(), position + 1)
        updated_b1 = self.repository.findById(b1.getId()).get()
        updated_b2 = self.repository.findById(b2.getId()).get()
        self.assertEqual(updated_b1.getPosition(), position + 2)
        self.assertEqual(updated_b2.getPosition(), position + 3)

    def test_11_change_position_by_old_new(self):
        """Тест 11: Изменение позиции по старой и новой позиции"""
        position = self.repository.getMaxPosition()
        b1 = self.service.get(self.Integer(131), self.Integer(100), self.Integer(1))
        b2 = self.service.get(self.Integer(132), self.Integer(200), self.Integer(2))
        b3 = self.service.get(self.Integer(133), self.Integer(300), self.Integer(3))
        self.test_budget_ids.extend([b1.getId(), b2.getId(), b3.getId()])
        result = self.service.changePosition(position + 1, position + 3)
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), position + 3)

    def test_12_change_position_by_old_new_not_found(self):
        """Тест 12: Изменение позиции по несуществующей старой позиции"""
        result = self.service.changePosition(999, 1)
        self.assertIsNone(result)

    def test_13_get_all_budgets(self):
        """Тест 13: Получение всех бюджетов"""
        b1 = self.service.get(self.Integer(141), self.Integer(1000), self.Integer(1))
        b2 = self.service.get(self.Integer(142), self.Integer(2000), self.Integer(2))
        self.test_budget_ids.append(b1.getId())
        self.test_budget_ids.append(b2.getId())
        budgets = self.service.getAll()
        self.assertIsNotNone(budgets)
        self.assertGreater(len(budgets), 0)
        ids = [b.getId() for b in budgets]
        self.assertIn(b1.getId(), ids)
        self.assertIn(b2.getId(), ids)

    def test_14_get_all_by_currency_id(self):
        """Тест 14: Получение всех бюджетов по ID валюты"""
        b1 = self.service.get(self.Integer(151), self.Integer(100), self.Integer(1))
        b2 = self.service.get(self.Integer(152), self.Integer(200), self.Integer(2))
        self.test_budget_ids.append(b1.getId())
        self.test_budget_ids.append(b2.getId())
        budgets = self.service.getAllByCurrencyId(1)
        self.assertIsNotNone(budgets)
        self.assertGreater(len(budgets), 0)
        # Проверяем, что хотя бы один бюджет с нужной валютой есть
        found = False
        for b in budgets:
            if b.getCurrencyId() == self.Integer(1):
                found = True
        self.assertTrue(found)

    def test_15_get_existing_budget_with_different_parameters(self):
        """Тест 15: Получение существующего бюджета с другими параметрами (должно обновить)"""
        category_id = self.Integer(161)

        # Создаем бюджет с параметрами
        budget1 = self.service.get(category_id, self.Integer(5000), self.Integer(1))
        self.test_budget_ids.append(budget1.getId())

        # Получаем тот же бюджет с другими параметрами
        budget2 = self.service.get(category_id, self.Integer(10000), self.Integer(2))
        self.test_budget_ids.append(budget2.getId())

        # Должен быть тот же бюджет, но с обновленными параметрами
        self.assertEqual(budget1.getId(), budget2.getId())
        self.assertEqual(budget2.getAmount(), 10000)
        self.assertEqual(budget2.getCurrencyId(), self.Integer(2))

    def test_16_update_budget_with_optional_parameters(self):
        """Тест 16: Обновление бюджета с Optional параметрами"""

        # Создаем бюджет
        budget = self.service.get(self.Integer(171), self.Integer(5000), self.Integer(1))
        self.test_budget_ids.append(budget.getId())

        # Обновляем только сумму
        updated = self.service.update(budget, self.Integer(15000), None)

        self.assertEqual(updated.getAmount(), 15000)
        self.assertEqual(updated.getCurrencyId(), budget.getCurrencyId())  # Не изменилось

    def test_17_update_budget_with_all_parameters(self):
        """Тест 17: Обновление бюджета со всеми параметрами"""

        # Создаем бюджет
        budget = self.service.get(self.Integer(181), self.Integer(5000), self.Integer(1))
        self.test_budget_ids.append(budget.getId())

        # Обновляем все параметры
        updated = self.service.update(budget, self.Integer(25000), self.Integer(2))  # Новая сумма  # Новая валюта

        self.assertEqual(updated.getAmount(), 25000)
        self.assertEqual(updated.getCurrencyId(), self.Integer(2))

    def test_18_update_budget_with_no_parameters(self):
        """Тест 18: Обновление бюджета без параметров (должно вернуть null)"""

        # Создаем бюджет
        budget = self.service.get(self.Integer(191), self.Integer(5000), self.Integer(1))
        self.test_budget_ids.append(budget.getId())

        # Обновляем без параметров
        updated = self.service.update(budget, None, None)

        self.assertIsNone(updated)

    def test_19_budget_position_sequence(self):
        """Тест 19: Последовательность позиций бюджетов"""
        budgets = []
        position = self.repository.getMaxPosition()
        for i in range(4):
            b = self.service.get(self.Integer(200 + i), self.Integer(100 * (i + 1)), self.Integer(1))
            budgets.append(b)
            self.test_budget_ids.append(b.getId())
        for i, b in enumerate(budgets):
            self.assertEqual(b.getPosition(), position + i + 1)

    def test_20_delete_and_restore_cycle(self):
        """Тест 20: Цикл удаления и восстановления бюджета"""
        budget = self.service.get(self.Integer(210), self.Integer(12345), self.Integer(1))
        self.test_budget_ids.append(budget.getId())
        self.service.delete(budget.getId())
        restored1 = self.service.get(budget.getCategoryId(), budget.getAmount(), budget.getCurrencyId())
        self.assertEqual(restored1.getId(), budget.getId())
        self.service.delete(budget.getId())
        restored2 = self.service.get(budget.getCategoryId(), budget.getAmount(), budget.getCurrencyId())
        self.assertEqual(restored2.getId(), budget.getId())

    def test_21_create_budget_with_different_currencies(self):
        """Тест 21: Создание бюджетов с разными валютами"""
        budget1 = self.service.get(self.Integer(220), self.Integer(1000), self.Integer(1))
        budget2 = self.service.get(self.Integer(221), self.Integer(2000), self.Integer(2))
        self.test_budget_ids.append(budget1.getId())
        self.test_budget_ids.append(budget2.getId())

        self.assertEqual(budget1.getCurrencyId(), self.Integer(1))
        self.assertEqual(budget2.getCurrencyId(), self.Integer(2))

        # Проверяем получение бюджетов по валюте
        budgets_currency_1 = self.service.getAllByCurrencyId(1)
        budgets_currency_2 = self.service.getAllByCurrencyId(2)

        self.assertGreater(len(budgets_currency_1), 0)
        self.assertGreater(len(budgets_currency_2), 0)

        # Проверяем, что наши бюджеты есть в соответствующих списках
        currency_1_ids = [b.getId() for b in budgets_currency_1]
        currency_2_ids = [b.getId() for b in budgets_currency_2]

        self.assertIn(budget1.getId(), currency_1_ids)
        self.assertIn(budget2.getId(), currency_2_ids)


if __name__ == '__main__':
    unittest.main()
