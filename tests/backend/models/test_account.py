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


class TestAccount(unittest.TestCase):
    """Юнит-тесты для Account"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.Account = get_java_class("com.sadengineer.budgetmaster.backend.model.Account")
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
        account = self.Account()

        # Assert
        self.assertIsNotNone(account)
        self.assertEqual(account.getId(), 0)
        self.assertEqual(account.getPosition(), 0)
        self.assertIsNone(account.getTitle())
        self.assertEqual(account.getAmount(), 0)
        self.assertEqual(account.getType(), 0)
        self.assertEqual(account.getCurrencyId(), 0)
        self.assertEqual(account.getClosed(), 0)
        self.assertIsNone(account.getCreditCardLimit())
        self.assertIsNone(account.getCreditCardCategoryId())
        self.assertIsNone(account.getCreditCardCommissionCategoryId())

    def test_02_full_constructor(self):
        """Тест 02: Полный конструктор"""
        # Arrange
        now = self.LocalDateTime.now()
        id_val = self.Integer(123)
        position = self.Integer(5)
        title = "Основной счет"
        amount = self.Integer(50000)
        type_val = self.Integer(1)
        currency_id = self.Integer(1)
        closed = self.Integer(0)
        credit_limit = self.Integer(100000)
        credit_category_id = self.Integer(5)
        credit_commission_id = self.Integer(6)

        # Act
        account = self.Account(
            id_val,
            now,
            now,
            None,
            "user",
            "user",
            None,
            position,
            title,
            amount,
            type_val,
            currency_id,
            closed,
            credit_limit,
            credit_category_id,
            credit_commission_id,
        )

        # Assert
        self.assertEqual(account.getId(), id_val)
        self.assertEqual(account.getPosition(), position)
        self.assertEqual(account.getTitle(), title)
        self.assertEqual(account.getAmount(), amount)
        self.assertEqual(account.getType(), type_val)
        self.assertEqual(account.getCurrencyId(), currency_id)
        self.assertEqual(account.getClosed(), closed)
        self.assertEqual(account.getCreditCardLimit(), credit_limit)
        self.assertEqual(account.getCreditCardCategoryId(), credit_category_id)
        self.assertEqual(account.getCreditCardCommissionCategoryId(), credit_commission_id)

    def test_03_setters_and_getters(self):
        """Тест 03: Сеттеры и геттеры"""
        # Arrange
        account = self.Account()
        position = self.Integer(10)
        title = "Кредитная карта"
        amount = self.Integer(25000)
        type_val = self.Integer(2)
        currency_id = self.Integer(2)
        closed = self.Integer(1)
        credit_limit = self.Integer(150000)
        credit_category_id = self.Integer(10)
        credit_commission_id = self.Integer(11)

        # Act
        account.setPosition(position)
        account.setTitle(title)
        account.setAmount(amount)
        account.setType(type_val)
        account.setCurrencyId(currency_id)
        account.setClosed(closed)
        account.setCreditCardLimit(credit_limit)
        account.setCreditCardCategoryId(credit_category_id)
        account.setCreditCardCommissionCategoryId(credit_commission_id)

        # Assert
        self.assertEqual(account.getPosition(), position)
        self.assertEqual(account.getTitle(), title)
        self.assertEqual(account.getAmount(), amount)
        self.assertEqual(account.getType(), type_val)
        self.assertEqual(account.getCurrencyId(), currency_id)
        self.assertEqual(account.getClosed(), closed)
        self.assertEqual(account.getCreditCardLimit(), credit_limit)
        self.assertEqual(account.getCreditCardCategoryId(), credit_category_id)
        self.assertEqual(account.getCreditCardCommissionCategoryId(), credit_commission_id)

    def test_04_equals_same_object(self):
        """Тест 04: equals - тот же объект"""
        # Arrange
        account = self.Account()

        # Act & Assert
        self.assertTrue(account.equals(account))

    def test_05_equals_different_type(self):
        """Тест 05: equals - другой тип объекта"""
        # Arrange
        account = self.Account()
        different_object = "string"

        # Act & Assert
        self.assertFalse(account.equals(different_object))

    def test_06_equals_same_values(self):
        """Тест 06: equals - одинаковые значения"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertTrue(account1.equals(account2))
        self.assertTrue(account2.equals(account1))

    def test_07_equals_different_values(self):
        """Тест 07: equals - разные значения"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Основной счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            "Кредитная карта",
            self.Integer(25000),
            self.Integer(2),
            self.Integer(2),
            self.Integer(0),
            self.Integer(150000),
            self.Integer(10),
            self.Integer(11),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_08_equals_different_position(self):
        """Тест 08: equals - разные позиции"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_09_equals_different_title(self):
        """Тест 09: equals - разные названия"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Основной счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Кредитная карта",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_10_equals_different_amount(self):
        """Тест 10: equals - разные суммы"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(20000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_11_equals_different_type(self):
        """Тест 11: equals - разные типы"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(2),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_12_equals_different_currency_id(self):
        """Тест 12: equals - разные валюты"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(2),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_13_equals_different_closed(self):
        """Тест 13: equals - разные статусы закрытия"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(1),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_14_equals_different_credit_limit(self):
        """Тест 14: equals - разные кредитные лимиты"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(100000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertFalse(account1.equals(account2))
        self.assertFalse(account2.equals(account1))

    def test_15_equals_null_credit_fields(self):
        """Тест 15: equals - null кредитные поля"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertTrue(account1.equals(account2))
        self.assertTrue(account2.equals(account1))

    def test_16_hash_code_consistency(self):
        """Тест 16: Консистентность hashCode"""
        # Arrange
        now = self.LocalDateTime.now()
        account1 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )
        account2 = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(50000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act & Assert
        self.assertEqual(account1.hashCode(), account2.hashCode())

        # hashCode должен быть одинаковым при повторных вызовах
        hash1 = account1.hashCode()
        hash2 = account1.hashCode()
        self.assertEqual(hash1, hash2)

    def test_17_to_string(self):
        """Тест 17: toString"""
        # Arrange
        now = self.LocalDateTime.now()
        account = self.Account(
            self.Integer(123),
            now,
            now,
            None,
            "creator",
            "updater",
            None,
            self.Integer(5),
            "Основной счет",
            self.Integer(50000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(100000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act
        result = account.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=123", result)
        self.assertIn("position=5", result)
        self.assertIn("title='Основной счет'", result)
        self.assertIn("amount=50000", result)
        self.assertIn("type=1", result)
        self.assertIn("currencyId=1", result)
        self.assertIn("closed=0", result)
        self.assertIn("creditCardLimit=100000", result)
        self.assertIn("creditCardCategoryId=5", result)
        self.assertIn("creditCardCommissionCategoryId=6", result)
        self.assertTrue(result.startswith("Account{"))

    def test_18_to_string_with_null_values(self):
        """Тест 18: toString с null значениями"""
        # Arrange
        account = self.Account()
        account.setTitle(None)
        account.setCreditCardLimit(None)
        account.setCreditCardCategoryId(None)
        account.setCreditCardCommissionCategoryId(None)

        # Act
        result = account.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=0", result)
        self.assertIn("title='null'", result)
        self.assertIn("creditCardLimit=null", result)
        self.assertIn("creditCardCategoryId=null", result)
        self.assertIn("creditCardCommissionCategoryId=null", result)

    def test_19_account_types(self):
        """Тест 19: Типы счетов"""
        # Arrange
        account = self.Account()

        # Act & Assert
        # Обычный счет
        account.setType(1)
        self.assertEqual(account.getType(), 1)

        # Кредитная карта
        account.setType(2)
        self.assertEqual(account.getType(), 2)

        # Депозит
        account.setType(3)
        self.assertEqual(account.getType(), 3)

    def test_20_closed_status(self):
        """Тест 20: Статус закрытия счета"""
        # Arrange
        account = self.Account()

        # Act & Assert
        # Открытый счет
        account.setClosed(0)
        self.assertEqual(account.getClosed(), 0)

        # Закрытый счет
        account.setClosed(1)
        self.assertEqual(account.getClosed(), 1)

    def test_21_amount_edge_cases(self):
        """Тест 21: Граничные случаи для суммы"""
        # Arrange
        account = self.Account()

        # Act - устанавливаем экстремальные значения
        account.setAmount(0)
        self.assertEqual(account.getAmount(), 0)

        account.setAmount(-1000)
        self.assertEqual(account.getAmount(), -1000)

        account.setAmount(2147483647)  # MAX_INT
        self.assertEqual(account.getAmount(), 2147483647)

    def test_22_credit_limit_edge_cases(self):
        """Тест 22: Граничные случаи для кредитного лимита"""
        # Arrange
        account = self.Account()

        # Act - устанавливаем различные значения
        account.setCreditCardLimit(self.Integer(0))
        self.assertEqual(account.getCreditCardLimit(), 0)

        account.setCreditCardLimit(self.Integer(-1000))
        self.assertEqual(account.getCreditCardLimit(), -1000)

        account.setCreditCardLimit(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(account.getCreditCardLimit(), 2147483647)

        account.setCreditCardLimit(None)
        self.assertIsNone(account.getCreditCardLimit())

    def test_23_title_edge_cases(self):
        """Тест 23: Граничные случаи для названия"""
        # Arrange
        account = self.Account()

        # Act - устанавливаем различные значения
        account.setTitle("")
        self.assertEqual(account.getTitle(), "")

        account.setTitle("А")
        self.assertEqual(account.getTitle(), "А")

        long_title = "Очень длинное название счета с множеством символов для тестирования граничных случаев"
        account.setTitle(long_title)
        self.assertEqual(account.getTitle(), long_title)

    def test_24_special_characters_in_title(self):
        """Тест 24: Специальные символы в названии"""
        # Arrange
        account = self.Account()
        special_title = "Счет & Карта (Visa) - %, $, €, ₽, <, >, \"', (тест)"

        # Act
        account.setTitle(special_title)

        # Assert
        self.assertEqual(account.getTitle(), special_title)

    def test_25_unicode_characters_in_title(self):
        """Тест 25: Unicode символы в названии"""
        # Arrange
        account = self.Account()
        unicode_title = "Основной счет 💳 - Банковский счет"

        # Act
        account.setTitle(unicode_title)

        # Assert
        self.assertEqual(account.getTitle(), unicode_title)

    def test_26_credit_card_fields(self):
        """Тест 26: Поля кредитной карты"""
        # Arrange
        account = self.Account()

        # Act - устанавливаем поля кредитной карты
        account.setCreditCardLimit(self.Integer(100000))
        account.setCreditCardCategoryId(self.Integer(5))
        account.setCreditCardCommissionCategoryId(self.Integer(6))

        # Assert
        self.assertEqual(account.getCreditCardLimit(), 100000)
        self.assertEqual(account.getCreditCardCategoryId(), 5)
        self.assertEqual(account.getCreditCardCommissionCategoryId(), 6)

        # Сбрасываем поля
        account.setCreditCardLimit(None)
        account.setCreditCardCategoryId(None)
        account.setCreditCardCommissionCategoryId(None)

        self.assertIsNone(account.getCreditCardLimit())
        self.assertIsNone(account.getCreditCardCategoryId())
        self.assertIsNone(account.getCreditCardCommissionCategoryId())

    def test_27_equals_with_null_values(self):
        """Тест 27: equals с null значениями"""
        # Arrange
        account1 = self.Account(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )
        account2 = self.Account(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertTrue(account1.equals(account2))
        self.assertTrue(account2.equals(account1))

    def test_28_hash_code_with_null_values(self):
        """Тест 28: hashCode с null значениями"""
        # Arrange
        account1 = self.Account(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )
        account2 = self.Account(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Счет",
            self.Integer(10000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )

        # Act & Assert
        self.assertEqual(account1.hashCode(), account2.hashCode())

    def test_29_inheritance_from_base_entity(self):
        """Тест 29: Наследование от BaseEntity"""
        # Arrange
        account = self.Account()

        # Act & Assert
        # Проверяем, что Account наследует базовые методы
        account.setId(999)
        account.setCreateTime(self.LocalDateTime.now())
        account.setCreatedBy("test_user")

        self.assertEqual(account.getId(), 999)
        self.assertIsNotNone(account.getCreateTime())
        self.assertEqual(account.getCreatedBy(), "test_user")

    def test_30_account_comparison(self):
        """Тест 30: Сравнение счетов"""
        # Arrange
        now = self.LocalDateTime.now()
        main_account = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Основной счет",
            self.Integer(50000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )
        credit_card = self.Account(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            "Кредитная карта",
            self.Integer(25000),
            self.Integer(2),
            self.Integer(1),
            self.Integer(0),
            self.Integer(100000),
            self.Integer(5),
            self.Integer(6),
        )
        deposit = self.Account(
            self.Integer(3),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(3),
            "Депозит",
            self.Integer(100000),
            self.Integer(3),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )

        # Act & Assert
        # Основной счет не равен кредитной карте
        self.assertFalse(main_account.equals(credit_card))
        # Кредитная карта не равна депозиту
        self.assertFalse(credit_card.equals(deposit))
        # Основной счет не равен депозиту
        self.assertFalse(main_account.equals(deposit))

        # Создаем копию основного счета
        main_copy = self.Account(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Основной счет",
            self.Integer(50000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            None,
            None,
            None,
        )
        self.assertTrue(main_account.equals(main_copy))

    def test_31_account_with_empty_strings(self):
        """Тест 31: Счет с пустыми строками"""
        # Arrange
        account = self.Account()

        # Act
        account.setCreatedBy("")
        account.setUpdatedBy("")
        account.setDeletedBy("")
        account.setTitle("")

        # Assert
        self.assertEqual(account.getCreatedBy(), "")
        self.assertEqual(account.getUpdatedBy(), "")
        self.assertEqual(account.getDeletedBy(), "")
        self.assertEqual(account.getTitle(), "")

    def test_32_account_serialization_format(self):
        """Тест 32: Формат сериализации счета"""
        # Arrange
        account = self.Account(
            self.Integer(123),
            None,
            None,
            None,
            "user",
            "user",
            None,
            self.Integer(5),
            "Основной счет",
            self.Integer(50000),
            self.Integer(1),
            self.Integer(1),
            self.Integer(0),
            self.Integer(100000),
            self.Integer(5),
            self.Integer(6),
        )

        # Act
        result = account.toString()

        # Assert
        # Проверяем формат вывода
        expected_parts = [
            "Account{",
            "id=123",
            "position=5",
            "title='Основной счет'",
            "amount=50000",
            "type=1",
            "currencyId=1",
            "closed=0",
            "creditCardLimit=100000",
            "creditCardCategoryId=5",
            "creditCardCommissionCategoryId=6",
        ]
        for part in expected_parts:
            self.assertIn(part, result)

    def test_33_account_edge_case_positions(self):
        """Тест 33: Граничные случаи позиций"""
        # Arrange
        account = self.Account()

        # Act & Assert
        # Минимальное значение
        account.setPosition(-2147483648)  # MIN_INT
        self.assertEqual(account.getPosition(), -2147483648)

        # Максимальное значение
        account.setPosition(2147483647)  # MAX_INT
        self.assertEqual(account.getPosition(), 2147483647)

    def test_34_negative_amounts(self):
        """Тест 34: Отрицательные суммы"""
        # Arrange
        account = self.Account()

        # Act - устанавливаем отрицательные значения
        account.setAmount(self.Integer(-5000))
        account.setCreditCardLimit(self.Integer(-10000))

        # Assert
        self.assertEqual(account.getAmount(), -5000)
        self.assertEqual(account.getCreditCardLimit(), -10000)

    def test_35_zero_values(self):
        """Тест 35: Нулевые значения"""
        # Arrange
        account = self.Account()

        # Act - устанавливаем нулевые значения
        account.setAmount(self.Integer(0))
        account.setCreditCardLimit(self.Integer(0))
        account.setCreditCardCategoryId(self.Integer(0))
        account.setCreditCardCommissionCategoryId(self.Integer(0))

        # Assert
        self.assertEqual(account.getAmount(), 0)
        self.assertEqual(account.getCreditCardLimit(), 0)
        self.assertEqual(account.getCreditCardCategoryId(), 0)
        self.assertEqual(account.getCreditCardCommissionCategoryId(), 0)


if __name__ == '__main__':
    unittest.main(verbosity=2)
