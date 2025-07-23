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


class TestCategory(unittest.TestCase):
    """Юнит-тесты для Category"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.Category = get_java_class("com.sadengineer.budgetmaster.backend.model.Category")
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
        category = self.Category()

        # Assert
        self.assertIsNotNone(category)
        self.assertEqual(category.getId(), 0)
        self.assertEqual(category.getPosition(), 0)
        self.assertIsNone(category.getTitle())
        self.assertEqual(category.getOperationType(), 0)
        self.assertEqual(category.getType(), 0)
        self.assertIsNone(category.getParentId())

    def test_02_full_constructor(self):
        """Тест 02: Полный конструктор"""
        # Arrange
        now = self.LocalDateTime.now()
        id_val = self.Integer(123)
        position = self.Integer(5)
        title = "Продукты"
        operation_type = self.Integer(1)  # Расходы
        type_val = self.Integer(1)  # Дочерняя категория
        parent_id = self.Integer(10)

        # Act
        category = self.Category(
            id_val, now, now, None, "user", "user", None, position, title, operation_type, type_val, parent_id
        )

        # Assert
        self.assertEqual(category.getId(), id_val)
        self.assertEqual(category.getPosition(), position)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getOperationType(), operation_type)
        self.assertEqual(category.getType(), type_val)
        self.assertEqual(category.getParentId(), parent_id)

    def test_03_setters_and_getters(self):
        """Тест 03: Сеттеры и геттеры"""
        # Arrange
        category = self.Category()
        position = self.Integer(10)
        title = "Транспорт"
        operation_type = self.Integer(2)  # Доходы
        type_val = self.Integer(0)  # Родительская категория
        parent_id = self.Integer(5)

        # Act
        category.setPosition(position)
        category.setTitle(title)
        category.setOperationType(operation_type)
        category.setType(type_val)
        category.setParentId(parent_id)

        # Assert
        self.assertEqual(category.getPosition(), position)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getOperationType(), operation_type)
        self.assertEqual(category.getType(), type_val)
        self.assertEqual(category.getParentId(), parent_id)

    def test_04_equals_same_object(self):
        """Тест 04: equals - тот же объект"""
        # Arrange
        category = self.Category()

        # Act & Assert
        self.assertTrue(category.equals(category))

    def test_05_equals_different_type(self):
        """Тест 05: equals - другой тип объекта"""
        # Arrange
        category = self.Category()
        different_object = "string"

        # Act & Assert
        self.assertFalse(category.equals(different_object))

    def test_06_equals_same_values(self):
        """Тест 06: equals - одинаковые значения"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertTrue(category1.equals(category2))
        self.assertTrue(category2.equals(category1))

    def test_07_equals_different_values(self):
        """Тест 07: equals - разные значения"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            "Транспорт",
            self.Integer(2),
            self.Integer(0),
            None,
        )

        # Act & Assert
        self.assertFalse(category1.equals(category2))
        self.assertFalse(category2.equals(category1))

    def test_08_equals_different_position(self):
        """Тест 08: equals - разные позиции"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertFalse(category1.equals(category2))
        self.assertFalse(category2.equals(category1))

    def test_09_equals_different_title(self):
        """Тест 09: equals - разные названия"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Транспорт",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertFalse(category1.equals(category2))
        self.assertFalse(category2.equals(category1))

    def test_10_equals_different_operation_type(self):
        """Тест 10: equals - разные типы операций"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(2),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertFalse(category1.equals(category2))
        self.assertFalse(category2.equals(category1))

    def test_11_equals_different_type(self):
        """Тест 11: equals - разные типы категорий"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(0),
            self.Integer(10),
        )

        # Act & Assert
        self.assertFalse(category1.equals(category2))
        self.assertFalse(category2.equals(category1))

    def test_12_equals_different_parent_id(self):
        """Тест 12: equals - разные parent_id"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(20),
        )

        # Act & Assert
        self.assertFalse(category1.equals(category2))
        self.assertFalse(category2.equals(category1))

    def test_13_equals_null_parent_id(self):
        """Тест 13: equals - null parent_id"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(0),
            None,
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(0),
            None,
        )

        # Act & Assert
        self.assertTrue(category1.equals(category2))
        self.assertTrue(category2.equals(category1))

    def test_14_hash_code_consistency(self):
        """Тест 14: Консистентность hashCode"""
        # Arrange
        now = self.LocalDateTime.now()
        category1 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertEqual(category1.hashCode(), category2.hashCode())

        # hashCode должен быть одинаковым при повторных вызовах
        hash1 = category1.hashCode()
        hash2 = category1.hashCode()
        self.assertEqual(hash1, hash2)

    def test_15_to_string(self):
        """Тест 15: toString"""
        # Arrange
        now = self.LocalDateTime.now()
        category = self.Category(
            self.Integer(123),
            now,
            now,
            None,
            "creator",
            "updater",
            None,
            self.Integer(5),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act
        result = category.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=123", result)
        self.assertIn("position=5", result)
        self.assertIn("title='Продукты'", result)
        self.assertIn("operationType=1", result)
        self.assertIn("type=1", result)
        self.assertIn("parentId=10", result)
        self.assertTrue(result.startswith("Category{"))

    def test_16_to_string_with_null_values(self):
        """Тест 16: toString с null значениями"""
        # Arrange
        category = self.Category()
        category.setTitle(None)
        category.setParentId(None)

        # Act
        result = category.toString()

        # Assert
        self.assertIsInstance(result, str)
        self.assertIn("id=0", result)
        self.assertIn("title='null'", result)
        self.assertIn("parentId=null", result)

    def test_17_operation_type_values(self):
        """Тест 17: Значения типов операций"""
        # Arrange
        category = self.Category()

        # Act & Assert
        # Расходы
        category.setOperationType(1)
        self.assertEqual(category.getOperationType(), 1)

        # Доходы
        category.setOperationType(2)
        self.assertEqual(category.getOperationType(), 2)

        # Перемещение
        category.setOperationType(3)
        self.assertEqual(category.getOperationType(), 3)

    def test_18_category_type_values(self):
        """Тест 18: Значения типов категорий"""
        # Arrange
        category = self.Category()

        # Act & Assert
        # Родительская категория
        category.setType(0)
        self.assertEqual(category.getType(), 0)

        # Дочерняя категория
        category.setType(1)
        self.assertEqual(category.getType(), 1)

    def test_19_parent_id_edge_cases(self):
        """Тест 19: Граничные случаи для parent_id"""
        # Arrange
        category = self.Category()

        # Act - устанавливаем различные значения
        category.setParentId(self.Integer(0))
        self.assertEqual(category.getParentId(), 0)

        category.setParentId(self.Integer(-1))
        self.assertEqual(category.getParentId(), -1)

        category.setParentId(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(category.getParentId(), 2147483647)

        category.setParentId(None)
        self.assertIsNone(category.getParentId())

    def test_20_title_edge_cases(self):
        """Тест 20: Граничные случаи для названия"""
        # Arrange
        category = self.Category()

        # Act - устанавливаем различные значения
        category.setTitle("")
        self.assertEqual(category.getTitle(), "")

        category.setTitle("А")
        self.assertEqual(category.getTitle(), "А")

        long_title = "Очень длинное название категории с множеством символов для тестирования граничных случаев"
        category.setTitle(long_title)
        self.assertEqual(category.getTitle(), long_title)

    def test_21_special_characters_in_title(self):
        """Тест 21: Специальные символы в названии"""
        # Arrange
        category = self.Category()
        special_title = "Продукты & Питание (Еда) - %, $, €, ₽, <, >, \"', (тест)"

        # Act
        category.setTitle(special_title)

        # Assert
        self.assertEqual(category.getTitle(), special_title)

    def test_22_unicode_characters_in_title(self):
        """Тест 22: Unicode символы в названии"""
        # Arrange
        category = self.Category()
        unicode_title = "Продукты питания 🍎 - Еда и напитки"

        # Act
        category.setTitle(unicode_title)

        # Assert
        self.assertEqual(category.getTitle(), unicode_title)

    def test_23_category_hierarchy(self):
        """Тест 23: Иерархия категорий"""
        # Arrange
        now = self.LocalDateTime.now()
        parent_category = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Расходы",
            self.Integer(1),
            self.Integer(0),
            None,
        )
        child_category = self.Category(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(1),
        )

        # Act & Assert
        self.assertIsNone(parent_category.getParentId())
        self.assertEqual(child_category.getParentId(), 1)
        self.assertEqual(parent_category.getType(), 0)  # Родительская
        self.assertEqual(child_category.getType(), 1)  # Дочерняя

    def test_24_equals_with_null_values(self):
        """Тест 24: equals с null значениями"""
        # Arrange
        category1 = self.Category(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertTrue(category1.equals(category2))
        self.assertTrue(category2.equals(category1))

    def test_25_hash_code_with_null_values(self):
        """Тест 25: hashCode с null значениями"""
        # Arrange
        category1 = self.Category(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )
        category2 = self.Category(
            self.Integer(1),
            None,
            None,
            None,
            None,
            None,
            None,
            self.Integer(1),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act & Assert
        self.assertEqual(category1.hashCode(), category2.hashCode())

    def test_26_inheritance_from_base_entity(self):
        """Тест 26: Наследование от BaseEntity"""
        # Arrange
        category = self.Category()

        # Act & Assert
        # Проверяем, что Category наследует базовые методы
        category.setId(999)
        category.setCreateTime(self.LocalDateTime.now())
        category.setCreatedBy("test_user")

        self.assertEqual(category.getId(), 999)
        self.assertIsNotNone(category.getCreateTime())
        self.assertEqual(category.getCreatedBy(), "test_user")

    def test_27_category_comparison(self):
        """Тест 27: Сравнение категорий"""
        # Arrange
        now = self.LocalDateTime.now()
        expenses = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Расходы",
            self.Integer(1),
            self.Integer(0),
            None,
        )
        income = self.Category(
            self.Integer(2),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(2),
            "Доходы",
            self.Integer(2),
            self.Integer(0),
            None,
        )
        products = self.Category(
            self.Integer(3),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(3),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(1),
        )

        # Act & Assert
        # Расходы не равны доходам
        self.assertFalse(expenses.equals(income))
        # Продукты не равны расходам
        self.assertFalse(products.equals(expenses))
        # Продукты не равны доходам
        self.assertFalse(products.equals(income))

        # Создаем копию расходов
        expenses_copy = self.Category(
            self.Integer(1),
            now,
            now,
            None,
            "user",
            "user",
            None,
            self.Integer(1),
            "Расходы",
            self.Integer(1),
            self.Integer(0),
            None,
        )
        self.assertTrue(expenses.equals(expenses_copy))

    def test_28_category_with_empty_strings(self):
        """Тест 28: Категория с пустыми строками"""
        # Arrange
        category = self.Category()

        # Act
        category.setCreatedBy("")
        category.setUpdatedBy("")
        category.setDeletedBy("")
        category.setTitle("")

        # Assert
        self.assertEqual(category.getCreatedBy(), "")
        self.assertEqual(category.getUpdatedBy(), "")
        self.assertEqual(category.getDeletedBy(), "")
        self.assertEqual(category.getTitle(), "")

    def test_29_category_serialization_format(self):
        """Тест 29: Формат сериализации категории"""
        # Arrange
        category = self.Category(
            self.Integer(123),
            None,
            None,
            None,
            "user",
            "user",
            None,
            self.Integer(5),
            "Продукты",
            self.Integer(1),
            self.Integer(1),
            self.Integer(10),
        )

        # Act
        result = category.toString()

        # Assert
        # Проверяем формат вывода
        expected_parts = [
            "Category{",
            "id=123",
            "position=5",
            "title='Продукты'",
            "operationType=1",
            "type=1",
            "parentId=10",
        ]
        for part in expected_parts:
            self.assertIn(part, result)

    def test_30_category_edge_case_positions(self):
        """Тест 30: Граничные случаи позиций"""
        # Arrange
        category = self.Category()

        # Act & Assert
        # Минимальное значение
        category.setPosition(self.Integer(-2147483648))  # MIN_INT
        self.assertEqual(category.getPosition(), -2147483648)

        # Максимальное значение
        category.setPosition(self.Integer(2147483647))  # MAX_INT
        self.assertEqual(category.getPosition(), 2147483647)


if __name__ == '__main__':
    unittest.main(verbosity=2)
