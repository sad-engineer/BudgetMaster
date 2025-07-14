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


class TestCurrency(unittest.TestCase):
    """Юнит-тесты для Currency"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        if not setup_example():
            raise Exception("Не удалось настроить окружение для тестов")

        # Импортируем Java классы
        cls.Currency = get_java_class("model.Currency")
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
        currency = self.Currency()

        # Assert
        self.assertIsNotNone(currency)
        self.assertEqual(currency.getId(), 0)
        self.assertEqual(currency.getPosition(), 0)
        self.assertIsNone(currency.getTitle())

    def test_02_full_constructor(self):
        """Тест 02: Полный конструктор"""
        # Arrange
        now = self.LocalDateTime.now()
        id_val = self.Integer(123)
        position = self.Integer(5)
        title = "RUB"

        # Act
        currency = self.Currency(id_val, now, now, None, "user", "user", None, position, title)

        # Assert
        self.assertEqual(currency.getId(), id_val)
        self.assertEqual(currency.getPosition(), position)
        self.assertEqual(currency.getTitle(), title)

    def test_03_setters_and_getters(self):
        """Тест 03: Сеттеры и геттеры"""
        # Arrange
        currency = self.Currency()
        position = self.Integer(10)
        title = "USD"

        # Act
        currency.setPosition(position)
        currency.setTitle(title)

        # Assert
        self.assertEqual(currency.getPosition(), position)
        self.assertEqual(currency.getTitle(), title)

    def test_04_equals_same_object(self):
        """Тест 04: equals - тот же объект"""
        # Arrange
        currency = self.Currency()

        # Act & Assert
        self.assertTrue(currency.equals(currency))

    def test_05_equals_different_type(self):
        """Тест 05: equals - другой тип объекта"""
        # Arrange
        currency = self.Currency()
        different_object = "string"

        # Act & Assert
        self.assertFalse(currency.equals(different_object))

    def test_06_equals_same_values(self):
        """Тест 06: equals - одинаковые значения"""
        # Arrange
        now = self.LocalDateTime.now()
        currency1 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")
        currency2 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")

        # Act & Assert
        self.assertTrue(currency1.equals(currency2))
        self.assertTrue(currency2.equals(currency1))

    def test_07_equals_different_values(self):
        """Тест 07: equals - разные значения"""
        # Arrange
        now = self.LocalDateTime.now()
        currency1 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")
        currency2 = self.Currency(self.Integer(2), now, now, None, "user", "user", None, self.Integer(2), "USD")

        # Act & Assert
        self.assertFalse(currency1.equals(currency2))
        self.assertFalse(currency2.equals(currency1))

    def test_08_equals_different_position(self):
        """Тест 08: equals - разные позиции"""
        # Arrange
        now = self.LocalDateTime.now()
        currency1 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")
        currency2 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(2), "RUB")

        # Act & Assert
        self.assertFalse(currency1.equals(currency2))
        self.assertFalse(currency2.equals(currency1))

    def test_09_equals_different_title(self):
        """Тест 09: equals - разные названия"""
        # Arrange
        now = self.LocalDateTime.now()
        currency1 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")
        currency2 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "USD")

        # Act & Assert
        self.assertFalse(currency1.equals(currency2))
        self.assertFalse(currency2.equals(currency1))

    def test_10_equals_null_title(self):
        """Тест 10: equals - null названия"""
        # Arrange
        now = self.LocalDateTime.now()
        currency1 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), None)
        currency2 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), None)

        # Act & Assert
        self.assertTrue(currency1.equals(currency2))
        self.assertTrue(currency2.equals(currency1))

    def test_11_hash_code_consistency(self):
        """Тест 11: Консистентность hashCode"""
        # Arrange
        now = self.LocalDateTime.now()
        currency1 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")
        currency2 = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")

        # Act & Assert
        self.assertEqual(currency1.hashCode(), currency2.hashCode())

        # hashCode должен быть одинаковым при повторных вызовах
        hash1 = currency1.hashCode()
        hash2 = currency1.hashCode()
        self.assertEqual(hash1, hash2)

    def test_12_to_string(self):
        """Тест 12: toString"""
        # Arrange
        now = self.LocalDateTime.now()
        currency = self.Currency(self.Integer(123), now, now, None, "creator", "updater", None, self.Integer(5), "EUR")

        # Act
        result = currency.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=123", result)
        self.assertIn("position=5", result)
        self.assertIn("title='EUR'", result)
        self.assertTrue(result.startswith("Currency{"))

    def test_13_to_string_with_null_title(self):
        """Тест 13: toString с null названием"""
        # Arrange
        currency = self.Currency()
        currency.setTitle(None)

        # Act
        result = currency.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("title='null'", result)

    def test_14_position_edge_cases(self):
        """Тест 14: Граничные случаи для позиции"""
        # Arrange
        currency = self.Currency()

        # Act - устанавливаем экстремальные значения
        currency.setPosition(self.Integer(0))
        self.assertEqual(currency.getPosition(), 0)

        currency.setPosition(self.Integer(-1))
        self.assertEqual(currency.getPosition(), -1)

        currency.setPosition(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(currency.getPosition(), 2147483647)

    def test_15_title_edge_cases(self):
        """Тест 15: Граничные случаи для названия"""
        # Arrange
        currency = self.Currency()

        # Act - устанавливаем различные значения
        currency.setTitle("")
        self.assertEqual(currency.getTitle(), "")

        currency.setTitle("A")
        self.assertEqual(currency.getTitle(), "A")

        long_title = "Очень длинное название валюты с множеством символов для тестирования граничных случаев"
        currency.setTitle(long_title)
        self.assertEqual(currency.getTitle(), long_title)

    def test_16_special_characters_in_title(self):
        """Тест 16: Специальные символы в названии"""
        # Arrange
        currency = self.Currency()
        special_title = "€ Евро (EUR) - €, ₽, $, %, &, <, >, \"', (тест)"

        # Act
        currency.setTitle(special_title)

        # Assert
        self.assertEqual(currency.getTitle(), special_title)

    def test_17_unicode_characters_in_title(self):
        """Тест 17: Unicode символы в названии"""
        # Arrange
        currency = self.Currency()
        unicode_title = "Рубль ₽ - Российский рубль"

        # Act
        currency.setTitle(unicode_title)

        # Assert
        self.assertEqual(currency.getTitle(), unicode_title)

    def test_18_equals_with_null_values(self):
        """Тест 18: equals с null значениями"""
        # Arrange
        currency1 = self.Currency(self.Integer(1), None, None, None, None, None, None, self.Integer(1), "RUB")
        currency2 = self.Currency(self.Integer(1), None, None, None, None, None, None, self.Integer(1), "RUB")

        # Act & Assert
        self.assertTrue(currency1.equals(currency2))
        self.assertTrue(currency2.equals(currency1))

    def test_19_hash_code_with_null_values(self):
        """Тест 19: hashCode с null значениями"""
        # Arrange
        currency1 = self.Currency(self.Integer(1), None, None, None, None, None, None, self.Integer(1), "RUB")
        currency2 = self.Currency(self.Integer(1), None, None, None, None, None, None, self.Integer(1), "RUB")

        # Act & Assert
        self.assertEqual(currency1.hashCode(), currency2.hashCode())

    def test_20_inheritance_from_base_entity(self):
        """Тест 20: Наследование от BaseEntity"""
        # Arrange
        currency = self.Currency()

        # Act & Assert
        # Проверяем, что Currency наследует базовые методы
        currency.setId(999)
        currency.setCreateTime(self.LocalDateTime.now())
        currency.setCreatedBy("test_user")

        self.assertEqual(currency.getId(), 999)
        self.assertIsNotNone(currency.getCreateTime())
        self.assertEqual(currency.getCreatedBy(), "test_user")

    def test_21_currency_comparison(self):
        """Тест 21: Сравнение валют"""
        # Arrange
        now = self.LocalDateTime.now()
        rub = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")
        usd = self.Currency(self.Integer(2), now, now, None, "user", "user", None, self.Integer(2), "USD")
        eur = self.Currency(self.Integer(3), now, now, None, "user", "user", None, self.Integer(3), "EUR")

        # Act & Assert
        # RUB не равен USD
        self.assertFalse(rub.equals(usd))
        # USD не равен EUR
        self.assertFalse(usd.equals(eur))
        # RUB не равен EUR
        self.assertFalse(rub.equals(eur))

        # Создаем копию RUB
        rub_copy = self.Currency(self.Integer(1), now, now, None, "user", "user", None, self.Integer(1), "RUB")
        self.assertTrue(rub.equals(rub_copy))

    def test_22_currency_with_empty_strings(self):
        """Тест 22: Валюта с пустыми строками"""
        # Arrange
        currency = self.Currency()

        # Act
        currency.setCreatedBy("")
        currency.setUpdatedBy("")
        currency.setDeletedBy("")
        currency.setTitle("")

        # Assert
        self.assertEqual(currency.getCreatedBy(), "")
        self.assertEqual(currency.getUpdatedBy(), "")
        self.assertEqual(currency.getDeletedBy(), "")
        self.assertEqual(currency.getTitle(), "")

    def test_23_currency_serialization_format(self):
        """Тест 23: Формат сериализации валюты"""
        # Arrange
        currency = self.Currency(self.Integer(123), None, None, None, "user", "user", None, self.Integer(5), "RUB")

        # Act
        result = currency.toString()

        # Assert
        # Проверяем формат вывода
        expected_parts = ["Currency{", "id=123", "position=5", "title='RUB'"]
        for part in expected_parts:
            self.assertIn(part, result)

    def test_24_currency_immutability_after_construction(self):
        """Тест 24: Неизменяемость после конструктора"""
        # Arrange
        now = self.LocalDateTime.now()
        currency = self.Currency(self.Integer(123), now, now, None, "user", "user", None, self.Integer(5), "RUB")

        # Act - изменяем поля
        currency.setPosition(self.Integer(10))
        currency.setTitle("USD")

        # Assert
        # Поля должны измениться
        self.assertEqual(currency.getPosition(), 10)
        self.assertEqual(currency.getTitle(), "USD")
        # Но базовые поля остаются
        self.assertEqual(currency.getId(), 123)

    def test_25_currency_edge_case_positions(self):
        """Тест 25: Граничные случаи позиций"""
        # Arrange
        currency = self.Currency()

        # Act & Assert
        # Минимальное значение
        currency.setPosition(self.Integer(-2147483648))  # MIN_INT
        self.assertEqual(currency.getPosition(), -2147483648)

        # Максимальное значение
        currency.setPosition(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(currency.getPosition(), 2147483647)


if __name__ == '__main__':
    unittest.main(verbosity=2)
