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


class TestOperation(unittest.TestCase):
    """Юнит-тесты для Operation"""

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
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")
        cls.String = get_java_class("java.lang.String")

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
        operation = self.Operation()

        # Assert
        self.assertIsNotNone(operation)
        self.assertEqual(operation.getId(), 0)
        self.assertEqual(operation.getType(), 0)
        self.assertIsNone(operation.getDate())
        self.assertEqual(operation.getAmount(), 0)
        self.assertIsNone(operation.getComment())
        self.assertEqual(operation.getCategoryId(), 0)
        self.assertEqual(operation.getAccountId(), 0)
        self.assertEqual(operation.getCurrencyId(), 0)
        self.assertIsNone(operation.getToAccountId())
        self.assertIsNone(operation.getToCurrencyId())
        self.assertIsNone(operation.getToAmount())

    def test_02_full_constructor(self):
        """Тест 02: Полный конструктор"""
        # Arrange
        now = self.LocalDateTime.now()
        id_val = self.Integer(123)
        type_val = self.Integer(1)
        date = now
        amount = self.Integer(5000)
        comment = "Покупка продуктов"
        category_id = self.Integer(5)
        account_id = self.Integer(1)
        currency_id = self.Integer(1)
        to_account_id = self.Integer(2)
        to_currency_id = self.Integer(2)
        to_amount = self.Integer(5000)

        # Act
        operation = self.Operation(
            id_val,
            now,
            now,
            None,
            "user",
            "user",
            None,
            type_val,
            date,
            amount,
            comment,
            category_id,
            account_id,
            currency_id,
            to_account_id,
            to_currency_id,
            to_amount,
        )

        # Assert
        self.assertEqual(operation.getId(), id_val)
        self.assertEqual(operation.getType(), type_val)
        self.assertEqual(operation.getDate(), date)
        self.assertEqual(operation.getAmount(), amount)
        self.assertEqual(operation.getComment(), comment)
        self.assertEqual(operation.getCategoryId(), category_id)
        self.assertEqual(operation.getAccountId(), account_id)
        self.assertEqual(operation.getCurrencyId(), currency_id)
        self.assertEqual(operation.getToAccountId(), to_account_id)
        self.assertEqual(operation.getToCurrencyId(), to_currency_id)
        self.assertEqual(operation.getToAmount(), to_amount)

    def test_03_setters_and_getters(self):
        """Тест 03: Сеттеры и геттеры"""
        # Arrange
        operation = self.Operation()
        type_val = self.Integer(2)
        date = self.LocalDateTime.now()
        amount = self.Integer(7500)
        comment = "Зарплата"
        category_id = self.Integer(10)
        account_id = self.Integer(2)
        currency_id = self.Integer(2)
        to_account_id = self.Integer(3)
        to_currency_id = self.Integer(1)
        to_amount = self.Integer(7500)

        # Act
        operation.setType(type_val)
        operation.setDate(date)
        operation.setAmount(amount)
        operation.setComment(comment)
        operation.setCategoryId(category_id)
        operation.setAccountId(account_id)
        operation.setCurrencyId(currency_id)
        operation.setToAccountId(to_account_id)
        operation.setToCurrencyId(to_currency_id)
        operation.setToAmount(to_amount)

        # Assert
        self.assertEqual(operation.getType(), type_val)
        self.assertEqual(operation.getDate(), date)
        self.assertEqual(operation.getAmount(), amount)
        self.assertEqual(operation.getComment(), comment)
        self.assertEqual(operation.getCategoryId(), category_id)
        self.assertEqual(operation.getAccountId(), account_id)
        self.assertEqual(operation.getCurrencyId(), currency_id)
        self.assertEqual(operation.getToAccountId(), to_account_id)
        self.assertEqual(operation.getToCurrencyId(), to_currency_id)
        self.assertEqual(operation.getToAmount(), to_amount)

    def test_04_equals_same_object(self):
        """Тест 04: equals - тот же объект"""
        # Arrange
        operation = self.Operation()

        # Act & Assert
        self.assertTrue(operation.equals(operation))

    def test_05_equals_different_type(self):
        """Тест 05: equals - другой тип объекта"""
        # Arrange
        operation = self.Operation()
        different_object = "string"

        # Act & Assert
        self.assertFalse(operation.equals(different_object))

    def test_06_equals_same_values(self):
        """Тест 06: equals - одинаковые значения"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertTrue(operation1.equals(operation2))
        self.assertTrue(operation2.equals(operation1))

    def test_07_equals_different_values(self):
        """Тест 07: equals - разные значения"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            now,
            self.Integer(7500),
            "Зарплата",
            self.Integer(10),
            self.Integer(2),
            self.Integer(2),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_08_equals_different_type(self):
        """Тест 08: equals - разные типы операций"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_09_equals_different_date(self):
        """Тест 09: equals - разные даты"""
        # Arrange
        now1 = self.LocalDateTime.now()
        now2 = now1.plusDays(1)
        operation1 = self.Operation(
            self.Integer(1),
            now1,
            now1,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now1,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now1,
            now1,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now2,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_10_equals_different_amount(self):
        """Тест 10: equals - разные суммы"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(7500),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_11_equals_different_comment(self):
        """Тест 11: equals - разные комментарии"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Продажа",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_12_equals_different_category_id(self):
        """Тест 12: equals - разные категории"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(10),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_13_equals_different_account_id(self):
        """Тест 13: equals - разные счета"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(2),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_14_equals_different_currency_id(self):
        """Тест 14: equals - разные валюты"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(2),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_15_equals_different_to_account_id(self):
        """Тест 15: equals - разные целевые счета"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            self.Integer(1),  # Разный целевой счет
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            self.Integer(2),  # Разный целевой счет
            None,
            None,
        )

        # Act & Assert
        self.assertFalse(operation1.equals(operation2))
        self.assertFalse(operation2.equals(operation1))

    def test_16_equals_null_to_fields(self):
        """Тест 16: equals - null поля перевода"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertTrue(operation1.equals(operation2))
        self.assertTrue(operation2.equals(operation1))

    def test_17_hash_code_consistency(self):
        """Тест 17: Консистентность hashCode"""
        # Arrange
        now = self.LocalDateTime.now()
        operation1 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertEqual(operation1.hashCode(), operation2.hashCode())

        # hashCode должен быть одинаковым при повторных вызовах
        hash1 = operation1.hashCode()
        hash2 = operation1.hashCode()
        self.assertEqual(hash1, hash2)

    def test_18_to_string(self):
        """Тест 18: toString"""
        # Arrange
        now = self.LocalDateTime.now()
        operation = self.Operation(
            self.Integer(123),
            now,
            now,
            None,
            "creator",
            "updater",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка продуктов",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            self.Integer(2),
            self.Integer(1),
            self.Integer(5000),
        )

        # Act
        result = operation.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=123", result)
        self.assertIn("type=1", result)
        self.assertIn("amount=5000", result)
        self.assertIn("comment='Покупка продуктов'", result)
        self.assertIn("categoryId=5", result)
        self.assertIn("accountId=1", result)
        self.assertIn("currencyId=1", result)
        self.assertIn("toAccountId=2", result)
        self.assertIn("toCurrencyId=1", result)
        self.assertIn("toAmount=5000", result)
        self.assertTrue(result.startswith("Operation{"))

    def test_19_to_string_with_null_values(self):
        """Тест 19: toString с null значениями"""
        # Arrange
        operation = self.Operation()
        operation.setDate(None)
        operation.setComment(None)
        operation.setToAccountId(None)
        operation.setToCurrencyId(None)
        operation.setToAmount(None)

        # Act
        result = operation.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=0", result)
        self.assertIn("date=null", result)
        self.assertIn("comment='null'", result)
        self.assertIn("toAccountId=null", result)
        self.assertIn("toCurrencyId=null", result)
        self.assertIn("toAmount=null", result)

    def test_20_operation_types(self):
        """Тест 20: Типы операций"""
        # Arrange
        operation = self.Operation()

        # Act & Assert
        # Расход
        operation.setType(self.Integer(1))
        self.assertEqual(operation.getType(), 1)

        # Доход
        operation.setType(self.Integer(2))
        self.assertEqual(operation.getType(), 2)

        # Перевод
        operation.setType(self.Integer(3))
        self.assertEqual(operation.getType(), 3)

    def test_21_amount_edge_cases(self):
        """Тест 21: Граничные случаи для суммы"""
        # Arrange
        operation = self.Operation()

        # Act - устанавливаем экстремальные значения
        operation.setAmount(self.Integer(0))
        self.assertEqual(operation.getAmount(), 0)

        operation.setAmount(self.Integer(-1000))
        self.assertEqual(operation.getAmount(), -1000)

        operation.setAmount(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(operation.getAmount(), 2147483647)

    def test_22_to_amount_edge_cases(self):
        """Тест 22: Граничные случаи для целевой суммы"""
        # Arrange
        operation = self.Operation()

        # Act - устанавливаем различные значения
        operation.setToAmount(self.Integer(0))
        self.assertEqual(operation.getToAmount(), 0)

        operation.setToAmount(self.Integer(-1000))
        self.assertEqual(operation.getToAmount(), -1000)

        operation.setToAmount(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(operation.getToAmount(), 2147483647)

        operation.setToAmount(None)
        self.assertIsNone(operation.getToAmount())

    def test_23_comment_edge_cases(self):
        """Тест 23: Граничные случаи для комментария"""
        # Arrange
        operation = self.Operation()

        # Act - устанавливаем различные значения
        operation.setComment(self.String(""))
        self.assertEqual(operation.getComment(), "")

        operation.setComment(self.String("А"))
        self.assertEqual(operation.getComment(), "А")

        long_comment = "Очень длинный комментарий с множеством символов для тестирования граничных случаев"
        operation.setComment(self.String(long_comment))
        self.assertEqual(operation.getComment(), long_comment)

    def test_24_special_characters_in_comment(self):
        """Тест 24: Специальные символы в комментарии"""
        # Arrange
        operation = self.Operation()
        special_comment = "Покупка & Продажа (Товары) - %, $, €, ₽, <, >, \"', (тест)"

        # Act
        operation.setComment(self.String(special_comment))

        # Assert
        self.assertEqual(operation.getComment(), special_comment)

    def test_25_unicode_characters_in_comment(self):
        """Тест 25: Unicode символы в комментарии"""
        # Arrange
        operation = self.Operation()
        unicode_comment = "Покупка продуктов 🍎 - Еда и напитки"

        # Act
        operation.setComment(self.String(unicode_comment))

        # Assert
        self.assertEqual(operation.getComment(), unicode_comment)

    def test_26_transfer_operation(self):
        """Тест 26: Операция перевода"""
        # Arrange
        now = self.LocalDateTime.now()
        operation = self.Operation()

        # Act - настраиваем перевод
        operation.setType(self.Integer(3))  # Перевод
        operation.setAmount(self.Integer(10000))
        operation.setAccountId(self.Integer(1))  # С какого счета
        operation.setCurrencyId(self.Integer(1))  # В какой валюте
        operation.setToAccountId(self.Integer(2))  # На какой счет
        operation.setToCurrencyId(self.Integer(2))  # В какой валюте
        operation.setToAmount(self.Integer(10000))  # Сумма перевода
        operation.setComment(self.String("Перевод между счетами"))

        # Assert
        self.assertEqual(operation.getType(), 3)
        self.assertEqual(operation.getAmount(), 10000)
        self.assertEqual(operation.getAccountId(), 1)
        self.assertEqual(operation.getCurrencyId(), 1)
        self.assertEqual(operation.getToAccountId(), 2)
        self.assertEqual(operation.getToCurrencyId(), 2)
        self.assertEqual(operation.getToAmount(), 10000)
        self.assertEqual(operation.getComment(), "Перевод между счетами")

    def test_27_equals_with_null_values(self):
        """Тест 27: equals с null значениями"""
        # Arrange
        operation1 = self.Operation(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            None,
            self.Integer(5000),
            None,
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            None,
            self.Integer(5000),
            None,
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertTrue(operation1.equals(operation2))
        self.assertTrue(operation2.equals(operation1))

    def test_28_hash_code_with_null_values(self):
        """Тест 28: hashCode с null значениями"""
        # Arrange
        operation1 = self.Operation(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            None,
            self.Integer(5000),
            None,
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        operation2 = self.Operation(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            None,
            self.Integer(5000),
            None,
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertEqual(operation1.hashCode(), operation2.hashCode())

    def test_29_inheritance_from_base_entity(self):
        """Тест 29: Наследование от BaseEntity"""
        # Arrange
        operation = self.Operation()

        # Act & Assert
        # Проверяем, что Operation наследует базовые методы
        operation.setId(self.Integer(999))
        operation.setCreateTime(self.LocalDateTime.now())
        operation.setCreatedBy("test_user")

        self.assertEqual(operation.getId(), 999)
        self.assertIsNotNone(operation.getCreateTime())
        self.assertEqual(operation.getCreatedBy(), "test_user")

    def test_30_operation_comparison(self):
        """Тест 30: Сравнение операций"""
        # Arrange
        now = self.LocalDateTime.now()
        expense = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        income = self.Operation(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            now,
            self.Integer(75000),
            "Зарплата",
            self.Integer(10),
            self.Integer(2),
            self.Integer(1),
            None,
            None,
            None,
        )
        transfer = self.Operation(
            self.Integer(3),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(3),
            now,
            self.Integer(10000),
            "Перевод",
            self.Integer(15),
            self.Integer(1),
            self.Integer(1),
            self.Integer(2),
            self.Integer(1),
            self.Integer(10000),
        )

        # Act & Assert
        # Расход не равен доходу
        self.assertFalse(expense.equals(income))
        # Доход не равен переводу
        self.assertFalse(income.equals(transfer))
        # Расход не равен переводу
        self.assertFalse(expense.equals(transfer))

        # Создаем копию расхода
        expense_copy = self.Operation(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            now,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            None,
            None,
            None,
        )
        self.assertTrue(expense.equals(expense_copy))

    def test_31_operation_with_empty_strings(self):
        """Тест 31: Операция с пустыми строками"""
        # Arrange
        operation = self.Operation()

        # Act
        operation.setCreatedBy("")
        operation.setUpdatedBy("")
        operation.setDeletedBy("")
        operation.setComment("")

        # Assert
        self.assertEqual(operation.getCreatedBy(), "")
        self.assertEqual(operation.getUpdatedBy(), "")
        self.assertEqual(operation.getDeletedBy(), "")
        self.assertEqual(operation.getComment(), "")

    def test_32_operation_serialization_format(self):
        """Тест 32: Формат сериализации операции"""
        # Arrange
        operation = self.Operation(
            self.Integer(123),
            None,
            None,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            None,
            self.Integer(5000),
            "Покупка",
            self.Integer(5),
            self.Integer(1),
            self.Integer(1),
            self.Integer(2),
            self.Integer(1),
            self.Integer(5000),
        )

        # Act
        result = operation.toString()

        # Assert
        # Проверяем формат вывода
        expected_parts = [
            "Operation{",
            "id=123",
            "type=1",
            "amount=5000",
            "comment='Покупка'",
            "categoryId=5",
            "accountId=1",
            "currencyId=1",
            "toAccountId=2",
            "toCurrencyId=1",
            "toAmount=5000",
        ]
        for part in expected_parts:
            self.assertIn(part, result)

    def test_33_operation_edge_case_ids(self):
        """Тест 33: Граничные случаи ID"""
        # Arrange
        operation = self.Operation()

        # Act & Assert
        # Минимальные значения
        operation.setCategoryId(self.Integer(-2147483648))  # MIN_INT
        operation.setAccountId(self.Integer(-2147483648))  # MIN_INT
        operation.setCurrencyId(self.Integer(-2147483648))  # MIN_INT
        operation.setToAccountId(self.Integer(-2147483648))  # MIN_INT
        operation.setToCurrencyId(self.Integer(-2147483648))  # MIN_INT

        self.assertEqual(operation.getCategoryId(), -2147483648)
        self.assertEqual(operation.getAccountId(), -2147483648)
        self.assertEqual(operation.getCurrencyId(), -2147483648)
        self.assertEqual(operation.getToAccountId(), -2147483648)
        self.assertEqual(operation.getToCurrencyId(), -2147483648)

        # Максимальные значения
        operation.setCategoryId(self.Integer(2147483647))  # MAX_INT
        operation.setAccountId(self.Integer(2147483647))  # MAX_INT
        operation.setCurrencyId(self.Integer(2147483647))  # MAX_INT
        operation.setToAccountId(self.Integer(2147483647))  # MAX_INT
        operation.setToCurrencyId(self.Integer(2147483647))  # MAX_INT

        self.assertEqual(operation.getCategoryId(), 2147483647)
        self.assertEqual(operation.getAccountId(), 2147483647)
        self.assertEqual(operation.getCurrencyId(), 2147483647)
        self.assertEqual(operation.getToAccountId(), 2147483647)
        self.assertEqual(operation.getToCurrencyId(), 2147483647)

    def test_34_negative_amounts(self):
        """Тест 34: Отрицательные суммы"""
        # Arrange
        operation = self.Operation()

        # Act - устанавливаем отрицательные значения
        operation.setAmount(self.Integer(-5000))
        operation.setToAmount(self.Integer(-10000))

        # Assert
        self.assertEqual(operation.getAmount(), -5000)
        self.assertEqual(operation.getToAmount(), -10000)

    def test_35_zero_values(self):
        """Тест 35: Нулевые значения"""
        # Arrange
        operation = self.Operation()

        # Act - устанавливаем нулевые значения
        operation.setAmount(self.Integer(0))
        operation.setToAmount(self.Integer(0))
        operation.setCategoryId(self.Integer(0))
        operation.setAccountId(self.Integer(0))
        operation.setCurrencyId(self.Integer(0))
        operation.setToAccountId(self.Integer(0))
        operation.setToCurrencyId(self.Integer(0))

        # Assert
        self.assertEqual(operation.getAmount(), 0)
        self.assertEqual(operation.getToAmount(), 0)
        self.assertEqual(operation.getCategoryId(), 0)
        self.assertEqual(operation.getAccountId(), 0)
        self.assertEqual(operation.getCurrencyId(), 0)
        self.assertEqual(operation.getToAccountId(), 0)
        self.assertEqual(operation.getToCurrencyId(), 0)


if __name__ == '__main__':
    unittest.main(verbosity=2)
