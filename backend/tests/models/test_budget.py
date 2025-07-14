import os
import sys
import unittest

from backend.tests.test_common import (
    cleanup_example,
    get_java_class,
    setup_example,
)

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestBudget(unittest.TestCase):
    """Юнит-тесты для Budget"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        if not setup_example():
            raise Exception("Не удалось настроить окружение для тестов")

        # Импортируем Java классы
        cls.Budget = get_java_class("model.Budget")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            # Не завершаем JVM здесь - пусть это делает atexit
            pass

    def test_01_default_constructor(self):
        """Тест 01: Конструктор по умолчанию"""
        # Act
        budget = self.Budget()

        # Assert
        self.assertIsNotNone(budget)
        self.assertEqual(budget.getId(), 0)
        self.assertEqual(budget.getAmount(), 0)
        self.assertEqual(budget.getCurrencyId(), 0)
        self.assertIsNone(budget.getCategoryId())
        self.assertEqual(budget.getPosition(), 0)

    def test_02_full_constructor(self):
        """Тест 02: Полный конструктор"""
        # Arrange
        now = self.LocalDateTime.now()
        id_val = self.Integer(123)
        amount = self.Integer(50000)
        currency_id = self.Integer(1)
        category_id = self.Integer(5)
        position = self.Integer(3)

        # Act
        budget = self.Budget(id_val, now, now, None, "user", "user", None, position, amount, currency_id, category_id)

        # Assert
        self.assertEqual(budget.getId(), id_val)
        self.assertEqual(budget.getAmount(), amount)
        self.assertEqual(budget.getCurrencyId(), currency_id)
        self.assertEqual(budget.getCategoryId(), category_id)
        self.assertEqual(budget.getPosition(), position)

    def test_03_setters_and_getters(self):
        """Тест 03: Сеттеры и геттеры"""
        # Arrange
        budget = self.Budget()
        amount = self.Integer(75000)
        currency_id = self.Integer(2)
        category_id = self.Integer(10)
        position = self.Integer(5)

        # Act
        budget.setAmount(amount)
        budget.setCurrencyId(currency_id)
        budget.setCategoryId(category_id)
        budget.setPosition(position)

        # Assert
        self.assertEqual(budget.getAmount(), amount)
        self.assertEqual(budget.getCurrencyId(), currency_id)
        self.assertEqual(budget.getCategoryId(), category_id)
        self.assertEqual(budget.getPosition(), position)

    def test_04_equals_same_object(self):
        """Тест 04: equals - тот же объект"""
        # Arrange
        budget = self.Budget()

        # Act & Assert
        self.assertTrue(budget.equals(budget))

    def test_05_equals_different_type(self):
        """Тест 05: equals - другой тип объекта"""
        # Arrange
        budget = self.Budget()
        different_object = "string"

        # Act & Assert
        self.assertFalse(budget.equals(different_object))

    def test_06_equals_same_values(self):
        """Тест 06: equals - одинаковые значения"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        self.assertTrue(budget1.equals(budget2))
        self.assertTrue(budget2.equals(budget1))

    def test_07_equals_different_values(self):
        """Тест 07: equals - разные значения"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            self.Integer(75000),
            self.Integer(2),
            self.Integer(10),
        )

        # Act & Assert
        self.assertFalse(budget1.equals(budget2))
        self.assertFalse(budget2.equals(budget1))

    def test_08_equals_different_amount(self):
        """Тест 08: equals - разные суммы"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(75000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        self.assertFalse(budget1.equals(budget2))
        self.assertFalse(budget2.equals(budget1))

    def test_09_equals_different_currency_id(self):
        """Тест 09: equals - разные валюты"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(2),
            self.Integer(5),
        )

        # Act & Assert
        self.assertFalse(budget1.equals(budget2))
        self.assertFalse(budget2.equals(budget1))

    def test_10_equals_different_category_id(self):
        """Тест 10: equals - разные категории"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertFalse(budget1.equals(budget2))
        self.assertFalse(budget2.equals(budget1))

    def test_11_equals_null_category_id(self):
        """Тест 11: equals - null category_id"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            None,
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            None,
        )

        # Act & Assert
        self.assertTrue(budget1.equals(budget2))
        self.assertTrue(budget2.equals(budget1))

    def test_12_hash_code_consistency(self):
        """Тест 12: Консистентность hashCode"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        self.assertEqual(budget1.hashCode(), budget2.hashCode())

        # hashCode должен быть одинаковым при повторных вызовах
        hash1 = budget1.hashCode()
        hash2 = budget1.hashCode()
        self.assertEqual(hash1, hash2)

    def test_13_to_string(self):
        """Тест 13: toString"""
        # Arrange
        now = self.LocalDateTime.now()
        budget = self.Budget(
            self.Integer(123),
            now,
            now,
            None,
            "creator",
            "updater",
            None,
            self.Integer(3),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act
        result = budget.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=123", result)
        self.assertIn("amount=50000", result)
        self.assertIn("currencyId=1", result)
        self.assertIn("categoryId=5", result)
        self.assertTrue(result.startswith("Budget{"))

    def test_14_to_string_with_null_category_id(self):
        """Тест 14: toString с null category_id"""
        # Arrange
        budget = self.Budget()
        budget.setCategoryId(None)

        # Act
        result = budget.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=0", result)
        self.assertIn("categoryId=null", result)

    def test_15_amount_edge_cases(self):
        """Тест 15: Граничные случаи для суммы"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем экстремальные значения
        budget.setAmount(self.Integer(0))
        self.assertEqual(budget.getAmount(), 0)

        budget.setAmount(self.Integer(-10000))
        self.assertEqual(budget.getAmount(), -10000)

        budget.setAmount(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(budget.getAmount(), 2147483647)

    def test_16_currency_id_edge_cases(self):
        """Тест 16: Граничные случаи для currency_id"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем различные значения
        budget.setCurrencyId(self.Integer(0))
        self.assertEqual(budget.getCurrencyId(), 0)

        budget.setCurrencyId(self.Integer(-1))
        self.assertEqual(budget.getCurrencyId(), -1)

        budget.setCurrencyId(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(budget.getCurrencyId(), 2147483647)

    def test_17_category_id_edge_cases(self):
        """Тест 17: Граничные случаи для category_id"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем различные значения
        budget.setCategoryId(self.Integer(0))
        self.assertEqual(budget.getCategoryId(), 0)

        budget.setCategoryId(self.Integer(-1))
        self.assertEqual(budget.getCategoryId(), -1)

        budget.setCategoryId(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(budget.getCategoryId(), 2147483647)

        budget.setCategoryId(None)
        self.assertIsNone(budget.getCategoryId())

    def test_18_position_edge_cases(self):
        """Тест 18: Граничные случаи для позиции"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем экстремальные значения
        budget.setPosition(self.Integer(0))
        self.assertEqual(budget.getPosition(), 0)

        budget.setPosition(self.Integer(-1))
        self.assertEqual(budget.getPosition(), -1)

        budget.setPosition(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(budget.getPosition(), 2147483647)

    def test_19_negative_amounts(self):
        """Тест 19: Отрицательные суммы"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем отрицательные значения
        budget.setAmount(self.Integer(-50000))

        # Assert
        self.assertEqual(budget.getAmount(), -50000)

    def test_20_zero_values(self):
        """Тест 20: Нулевые значения"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем нулевые значения
        budget.setAmount(self.Integer(0))
        budget.setCurrencyId(self.Integer(0))
        budget.setCategoryId(self.Integer(0))
        budget.setPosition(self.Integer(0))

        # Assert
        self.assertEqual(budget.getAmount(), 0)
        self.assertEqual(budget.getCurrencyId(), 0)
        self.assertEqual(budget.getCategoryId(), 0)
        self.assertEqual(budget.getPosition(), 0)

    def test_21_equals_with_null_values(self):
        """Тест 21: equals с null значениями"""
        # Arrange
        budget1 = self.Budget(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            None,
        )
        budget2 = self.Budget(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            None,
        )

        # Act & Assert
        self.assertTrue(budget1.equals(budget2))
        self.assertTrue(budget2.equals(budget1))

    def test_22_hash_code_with_null_values(self):
        """Тест 22: hashCode с null значениями"""
        # Arrange
        budget1 = self.Budget(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            None,
        )
        budget2 = self.Budget(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            None,
        )

        # Act & Assert
        self.assertEqual(budget1.hashCode(), budget2.hashCode())

    def test_23_inheritance_from_base_entity(self):
        """Тест 23: Наследование от BaseEntity"""
        # Arrange
        budget = self.Budget()

        # Act & Assert
        # Проверяем, что Budget наследует базовые методы
        budget.setId(self.Integer(999))
        budget.setCreateTime(self.LocalDateTime.now())
        budget.setCreatedBy("test_user")

        self.assertEqual(budget.getId(), 999)
        self.assertIsNotNone(budget.getCreateTime())
        self.assertEqual(budget.getCreatedBy(), "test_user")

    def test_24_budget_comparison(self):
        """Тест 24: Сравнение бюджетов"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            self.Integer(75000),
            self.Integer(2),
            self.Integer(10),
        )
        budget3 = self.Budget(
            self.Integer(3),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(3),
            self.Integer(100000),
            self.Integer(1),
            None,
        )

        # Act & Assert
        # Бюджет 1 не равен бюджету 2
        self.assertFalse(budget1.equals(budget2))
        # Бюджет 2 не равен бюджету 3
        self.assertFalse(budget2.equals(budget3))
        # Бюджет 1 не равен бюджету 3
        self.assertFalse(budget1.equals(budget3))

        # Создаем копию бюджета 1
        budget1_copy = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        self.assertTrue(budget1.equals(budget1_copy))

    def test_25_budget_serialization_format(self):
        """Тест 25: Формат сериализации бюджета"""
        # Arrange
        budget = self.Budget(
            self.Integer(123),
            None,
            None,
            None,
            "user",
            "user",
            None,
            self.Integer(3),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act
        result = budget.toString()

        # Assert
        # Проверяем формат вывода
        expected_parts = ["Budget{", "id=123", "amount=50000", "currencyId=1", "categoryId=5"]
        for part in expected_parts:
            self.assertIn(part, result)

    def test_26_budget_edge_case_positions(self):
        """Тест 26: Граничные случаи позиций"""
        # Arrange
        budget = self.Budget()

        # Act & Assert
        # Минимальное значение
        budget.setPosition(self.Integer(-2147483648))  # MIN_INT
        self.assertEqual(budget.getPosition(), -2147483648)

        # Максимальное значение
        budget.setPosition(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(budget.getPosition(), 2147483647)

    def test_27_budget_with_large_amounts(self):
        """Тест 27: Бюджет с большими суммами"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем большие значения
        budget.setAmount(self.Integer(999999999))
        budget.setCurrencyId(self.Integer(999))
        budget.setCategoryId(self.Integer(999))

        # Assert
        self.assertEqual(budget.getAmount(), 999999999)
        self.assertEqual(budget.getCurrencyId(), 999)
        self.assertEqual(budget.getCategoryId(), 999)

    def test_28_budget_null_category_handling(self):
        """Тест 28: Обработка null категории"""
        # Arrange
        budget = self.Budget()

        # Act - устанавливаем и сбрасываем category_id
        budget.setCategoryId(self.Integer(5))
        self.assertEqual(budget.getCategoryId(), 5)

        budget.setCategoryId(None)
        self.assertIsNone(budget.getCategoryId())

        budget.setCategoryId(self.Integer(0))
        self.assertEqual(budget.getCategoryId(), 0)

    def test_29_budget_equals_reflexivity(self):
        """Тест 29: Рефлексивность equals"""
        # Arrange
        budget = self.Budget(
            self.Integer(1),
            None,
            None,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        self.assertTrue(budget.equals(budget))

    def test_30_budget_equals_symmetry(self):
        """Тест 30: Симметричность equals"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        self.assertTrue(budget1.equals(budget2))
        self.assertTrue(budget2.equals(budget1))

    def test_31_budget_equals_transitivity(self):
        """Тест 31: Транзитивность equals"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget3 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        self.assertTrue(budget1.equals(budget2))
        self.assertTrue(budget2.equals(budget3))
        self.assertTrue(budget1.equals(budget3))

    def test_32_budget_equals_consistency(self):
        """Тест 32: Консистентность equals"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        # Множественные вызовы должны возвращать одинаковый результат
        self.assertTrue(budget1.equals(budget2))
        self.assertTrue(budget1.equals(budget2))
        self.assertTrue(budget1.equals(budget2))

    def test_33_budget_equals_null_consistency(self):
        """Тест 33: Консистентность equals с null"""
        # Arrange
        budget = self.Budget(
            self.Integer(1),
            None,
            None,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act & Assert
        # equals с null всегда должен возвращать false
        self.assertFalse(budget.equals(None))
        self.assertFalse(budget.equals(None))

    def test_34_budget_hash_code_contract(self):
        """Тест 34: Контракт hashCode"""
        # Arrange
        now = self.LocalDateTime.now()
        budget1 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget2 = self.Budget(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )
        budget3 = self.Budget(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            self.Integer(75000),
            self.Integer(2),
            self.Integer(10),
        )

        # Act & Assert
        # Если объекты равны, их hashCode должны быть равны
        self.assertTrue(budget1.equals(budget2))
        self.assertEqual(budget1.hashCode(), budget2.hashCode())

        # Если hashCode равны, объекты могут быть равны или нет
        # (возможны коллизии)
        if budget1.hashCode() == budget3.hashCode():
            # Если hashCode одинаковый, equals должен быть false
            self.assertFalse(budget1.equals(budget3))

    def test_35_budget_to_string_completeness(self):
        """Тест 35: Полнота toString"""
        # Arrange
        budget = self.Budget(
            self.Integer(123),
            None,
            None,
            None,
            "user",
            "user",
            None,
            self.Integer(3),
            self.Integer(50000),
            self.Integer(1),
            self.Integer(5),
        )

        # Act
        result = budget.toString()

        # Assert
        # Проверяем, что все поля присутствуют в строке
        self.assertIn("id=123", result)
        self.assertIn("amount=50000", result)
        self.assertIn("currencyId=1", result)
        self.assertIn("categoryId=5", result)

        # Проверяем, что строка заканчивается правильно
        self.assertTrue(result.endswith("}"))


if __name__ == '__main__':
    unittest.main(verbosity=2)
