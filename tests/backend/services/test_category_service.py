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


class TestCategoryService(unittest.TestCase):
    """Юнит-тесты для CategoryService"""

    @classmethod
    def setUpClass(cls):
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.CategoryService = get_java_class("com.sadengineer.budgetmaster.backend.service.CategoryService")
        cls.CategoryRepository = get_java_class("com.sadengineer.budgetmaster.backend.repository.CategoryRepository")
        cls.Category = get_java_class("com.sadengineer.budgetmaster.backend.model.Category")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")
        cls.Optional = get_java_class("java.util.Optional")

        cls.test_category_ids = []
        cls.db_path = cls.db_manager.db_path
        cls.repository = cls.CategoryRepository(cls.db_path)
        cls.service = cls.CategoryService(cls.repository, "test_user")

    @classmethod
    def tearDownClass(cls):
        try:
            deleted_count = 0
            for category_id in cls.test_category_ids:
                try:
                    success = cls.db_manager.execute_update("DELETE FROM categories WHERE id = ?", (category_id,))
                    if success:
                        deleted_count += 1
                    else:
                        print(f"Ошибка при удалении категории {category_id}")
                except Exception as e:
                    print(f"Ошибка при удалении категории {category_id}: {e}")
            if deleted_count > 0:
                print(f"Удалено {deleted_count} тестовых категорий из базы данных")
            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            pass

    def test_01_create_category(self):
        """Тест 01: Получение новой (не существующей) категории"""
        title = "Категория 1"
        position = self.repository.getMaxPosition() + 1

        category = self.service.get(title, 1, 0, None)
        self.test_category_ids.append(category.getId())

        self.assertIsNotNone(category)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getPosition(), position)
        self.assertEqual(category.getCreatedBy(), "test_user")
        self.assertIsNone(category.getUpdatedBy())
        self.assertIsNone(category.getDeletedBy())
        self.assertIsNotNone(category.getCreateTime())
        self.assertIsNone(category.getUpdateTime())
        self.assertIsNone(category.getDeleteTime())

    def test_02_get_category_by_title(self):
        """Тест 02: Получение существующей категории по Title"""
        title = "Категория 2"
        category_new = self.service.get(title, 1, 0, None)
        self.test_category_ids.append(category_new.getId())

        # Категория создана, ее позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        category = self.service.get(title)
        self.test_category_ids.append(category.getId())

        self.assertIsNotNone(category)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getPosition(), position)
        self.assertEqual(category.getCreatedBy(), "test_user")
        self.assertIsNone(category.getUpdatedBy())
        self.assertIsNone(category.getDeletedBy())
        self.assertIsNotNone(category.getCreateTime())
        self.assertIsNone(category.getUpdateTime())
        self.assertIsNone(category.getDeleteTime())

    def test_03_get_deleted_category_by_title(self):
        """Тест 03: Получение удаленной категории по Title"""
        title = "Категория 3"
        category_new = self.service.get(title, 1, 0, None)
        self.test_category_ids.append(category_new.getId())
        self.repository.deleteById(category_new.getId(), "test_user")

        # Категория не удалена физически из таблицы, ее позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        category = self.service.get(title)
        self.test_category_ids.append(category.getId())

        self.assertIsNotNone(category)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getPosition(), position)
        self.assertEqual(category.getCreatedBy(), "test_user")
        self.assertEqual(category.getUpdatedBy(), "test_user")
        self.assertIsNone(category.getDeletedBy())
        self.assertIsNotNone(category.getCreateTime())
        self.assertIsNotNone(category.getUpdateTime())
        self.assertIsNone(category.getDeleteTime())
        self.assertNotEqual(category.getCreateTime(), category.getUpdateTime())

    def test_04_get_category_by_id(self):
        """Тест 04: Получение категории по ID"""
        category = self.service.get(self.Integer(1))
        self.assertIsNotNone(category)
        self.assertEqual(category.getId(), 1)
        self.assertEqual(category.getTitle(), "Доходы")
        self.assertEqual(category.getPosition(), 1)
        self.assertEqual(category.getCreatedBy(), "initializer")
        self.assertIsNone(category.getUpdatedBy())
        self.assertIsNone(category.getDeletedBy())
        self.assertIsNotNone(category.getCreateTime())
        self.assertIsNone(category.getUpdateTime())
        self.assertIsNone(category.getDeleteTime())

    def test_05_get_category_by_id_not_found(self):
        """Тест 05: Получение категории по несуществующему ID"""
        category = self.service.get(self.Integer(999999))
        self.assertIsNone(category)

    def test_06_create_category_with_special_title(self):
        """Тест 06: Создание категории с необычным названием"""
        category = self.service.get("12123", 1, 0, None)
        self.test_category_ids.append(category.getId())
        self.assertEqual(category.getTitle(), "12123")

        category2 = self.service.get("Категория с цифрами 123", 1, 0, None)
        self.test_category_ids.append(category2.getId())
        self.assertEqual(category2.getTitle(), "Категория с цифрами 123")

    def test_07_create_category_with_invalid_title(self):
        """Тест 07: Создание категории с недопустимым названием"""
        with self.assertRaises(Exception):
            self.service.createCategory("""Категория с недопустимым названием {123}""")

    def test_08_delete_category_by_id(self):
        """Тест 08: Удаление категории по ID"""
        category = self.service.get("Категория 4", 1, 0, None)
        self.test_category_ids.append(category.getId())
        result = self.service.delete(category.getId())
        self.assertTrue(result)
        # Проверяем, что категория помечена как удалённая
        found = self.repository.findById(category.getId())
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_09_delete_category_by_title(self):
        """Тест 09: Удаление категории по названию"""
        title = "Категория 5"
        category = self.service.get(title, 1, 0, None)
        self.test_category_ids.append(category.getId())
        result = self.service.delete(title)
        self.assertTrue(result)
        # Проверяем, что категория помечена как удалённая
        found = self.repository.findByTitle(title)
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_10_change_position(self):
        """Тест 10: Изменение позиции категории"""
        position = self.repository.getMaxPosition()
        c1 = self.service.get("Категория х1", 1, 0, None)
        c2 = self.service.get("Категория х2", 1, 0, None)
        c3 = self.service.get("Категория х3", 1, 0, None)
        self.test_category_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c1 на позицию 3
        result = self.service.changePosition(c1, position + 3)
        self.assertEqual(result.getPosition(), position + 3)
        # Проверяем, что другие категории сдвинулись
        updated_c2 = self.repository.findById(c2.getId()).get()
        updated_c3 = self.repository.findById(c3.getId()).get()
        self.assertEqual(updated_c2.getPosition(), position + 1)
        self.assertEqual(updated_c3.getPosition(), position + 2)

    def test_11_change_position_up(self):
        """Тест 11: Перемещение категории вверх"""
        position = self.repository.getMaxPosition()
        c1 = self.service.get("Категория A", 1, 0, None)
        c2 = self.service.get("Категория B", 1, 0, None)
        c3 = self.service.get("Категория C", 1, 0, None)
        self.test_category_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c3 на позицию 1
        result = self.service.changePosition(c3, position + 1)
        self.assertEqual(result.getPosition(), position + 1)
        updated_c1 = self.repository.findById(c1.getId()).get()
        updated_c2 = self.repository.findById(c2.getId()).get()
        self.assertEqual(updated_c1.getPosition(), position + 2)
        self.assertEqual(updated_c2.getPosition(), position + 3)

    def test_12_change_position_by_old_new(self):
        """Тест 12: Изменение позиции по старой и новой позиции"""
        position = self.repository.getMaxPosition()
        c1 = self.service.get("Категория X", 1, 0, None)
        c2 = self.service.get("Категория Y", 1, 0, None)
        c3 = self.service.get("Категория Z", 1, 0, None)
        self.test_category_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        result = self.service.changePosition(position + 1, position + 3)
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), position + 3)

    def test_13_change_position_by_old_new_not_found(self):
        """Тест 13: Изменение позиции по несуществующей старой позиции"""
        result = self.service.changePosition(999, 1)
        self.assertIsNone(result)

    def test_14_change_position_same_position(self):
        """Тест 14: Изменение позиции на ту же позицию"""
        category = self.service.get("Категория без изменений", 1, 0, None)
        self.test_category_ids.append(category.getId())
        old_position = category.getPosition()
        result = self.service.changePosition(category, old_position)
        self.assertEqual(result.getPosition(), old_position)

    def test_15_change_position_invalid_position(self):
        """Тест 15: Изменение позиции на недопустимую"""
        category = self.service.get("Категория для теста", 1, 0, None)
        self.test_category_ids.append(category.getId())
        with self.assertRaises(Exception):
            self.service.changePosition(category, 999)

    def test_16_get_all_categories(self):
        """Тест 16: Получение всех категорий"""
        c1 = self.service.get("Категория 111", 1, 0, None)
        c2 = self.service.get("Категория 211", 1, 0, None)
        self.test_category_ids.append(c1.getId())
        self.test_category_ids.append(c2.getId())
        categories = self.service.getAll()
        self.assertIsNotNone(categories)
        self.assertGreater(len(categories), 0)
        ids = [c.getId() for c in categories]
        self.assertIn(c1.getId(), ids)
        self.assertIn(c2.getId(), ids)

    def test_17_is_category_deleted(self):
        """Тест 17: Проверка удаления категории"""
        category = self.service.get("Категория для проверки удаления", 1, 0, None)
        self.test_category_ids.append(category.getId())
        self.repository.deleteById(category.getId(), "test_user")
        deleted = self.repository.findById(category.getId()).get()
        self.assertTrue(self.service.isCategoryDeleted(deleted))

    def test_18_create_category_with_unicode_title(self):
        """Тест 18: Создание категории с Unicode названием"""
        title = "Категория с кириллицей и символами: !@#$%^&*()"
        with self.assertRaises(Exception):
            self.service.get(title)

    def test_19_get_category_with_special_characters(self):
        """Тест 19: Получение категории со специальными символами"""
        title = "Категория с символами: €$¥£₽"
        with self.assertRaises(Exception):
            self.service.get(title)

    def test_20_get_category_with_operation_type(self):
        """Тест 20: Получение категории с указанным типом операций"""
        title = "Категория расходов"
        category = self.service.get(title, 1)  # Тип операций: расход
        self.test_category_ids.append(category.getId())

        self.assertIsNotNone(category)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getOperationType(), 1)
        self.assertEqual(category.getType(), 1)  # По умолчанию дочерняя
        self.assertIsNone(category.getParentId())  # По умолчанию null

    def test_21_get_category_with_operation_type_and_type(self):
        """Тест 21: Получение категории с указанным типом операций и типом категории"""
        title = "Родительская категория доходов"
        category = self.service.get(title, 2, 0)  # Доход, родительская
        self.test_category_ids.append(category.getId())

        self.assertIsNotNone(category)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getOperationType(), 2)
        self.assertEqual(category.getType(), 0)
        self.assertIsNone(category.getParentId())

    def test_22_get_category_with_all_parameters(self):
        """Тест 22: Получение категории со всеми параметрами"""
        parent_title = "Родительская категория"
        parent = self.service.get(parent_title, 1, 0, None)
        self.test_category_ids.append(parent.getId())

        child_title = "Дочерняя категория"
        child = self.service.get(child_title, 1, 1, parent.getId())
        self.test_category_ids.append(child.getId())

        self.assertIsNotNone(child)
        self.assertEqual(child.getTitle(), child_title)
        self.assertEqual(child.getOperationType(), 1)
        self.assertEqual(child.getType(), 1)
        self.assertEqual(child.getParentId(), parent.getId())

    def test_23_get_existing_category_with_different_parameters(self):
        """Тест 23: Получение существующей категории с другими параметрами (должно обновить)"""
        title = "Категория для обновления"

        # Создаем категорию с параметрами по умолчанию
        category1 = self.service.get(title, 1, 0, None)
        self.test_category_ids.append(category1.getId())

        # Получаем ту же категорию с другими параметрами
        category2 = self.service.get(title, 2, 1, self.Integer(1))
        self.test_category_ids.append(category2.getId())

        # Должна быть та же категория, но с обновленными параметрами
        self.assertEqual(category1.getId(), category2.getId())
        self.assertEqual(category2.getOperationType(), 2)
        self.assertEqual(category2.getType(), 1)
        self.assertEqual(category2.getParentId(), self.Integer(1))

    def test_24_get_all_by_operation_type(self):
        """Тест 24: Получение категорий по типу операции"""
        # Создаем категории с разными типами операций
        c1 = self.service.get("Категория типа 1", 1)
        c2 = self.service.get("Категория типа 2", 2)
        self.test_category_ids.append(c1.getId())
        self.test_category_ids.append(c2.getId())

        categories_type_1 = self.service.getAllByOperationType(1)
        categories_type_2 = self.service.getAllByOperationType(2)

        self.assertGreater(len(categories_type_1), 0)
        self.assertGreater(len(categories_type_2), 0)

        # Проверяем, что наши категории есть в соответствующих списках
        type_1_ids = [c.getId() for c in categories_type_1]
        type_2_ids = [c.getId() for c in categories_type_2]

        self.assertIn(c1.getId(), type_1_ids)
        self.assertIn(c2.getId(), type_2_ids)

    def test_25_get_all_by_type(self):
        """Тест 25: Получение категорий по типу категории"""
        # Создаем категории с разными типами
        c1 = self.service.get("Категория типа A", 1, 0)  # Родительская
        c2 = self.service.get("Категория типа B", 1, 1)  # Дочерняя
        self.test_category_ids.append(c1.getId())
        self.test_category_ids.append(c2.getId())

        categories_type_0 = self.service.getAllByType(0)
        categories_type_1 = self.service.getAllByType(1)

        self.assertGreater(len(categories_type_0), 0)
        self.assertGreater(len(categories_type_1), 0)

        # Проверяем, что наши категории есть в соответствующих списках
        type_0_ids = [c.getId() for c in categories_type_0]
        type_1_ids = [c.getId() for c in categories_type_1]

        self.assertIn(c1.getId(), type_0_ids)
        self.assertIn(c2.getId(), type_1_ids)

    def test_26_get_all_by_parent_id(self):
        """Тест 26: Получение категорий по ID родительской категории"""
        # Создаем родительскую и дочерние категории
        parent = self.service.get("Родительская категория", 1, 0, None)
        child1 = self.service.get("Дочерняя категория 1", 1, 1, parent.getId())
        child2 = self.service.get("Дочерняя категория 2", 1, 1, parent.getId())
        self.test_category_ids.extend([parent.getId(), child1.getId(), child2.getId()])

        children = self.service.getAllByParentId(parent.getId())
        self.assertGreater(len(children), 0)

        # Проверяем, что дочерние категории есть в списке
        child_ids = [c.getId() for c in children]

        self.assertIn(child1.getId(), child_ids)
        self.assertIn(child2.getId(), child_ids)

    def test_27_update_category_with_optional_parameters(self):
        """Тест 27: Обновление категории с Optional параметрами"""

        # Создаем категорию
        category = self.service.get("Категория для обновления")
        self.test_category_ids.append(category.getId())

        # Обновляем только название
        updated = self.service.update(category, "Новое название", None, None, None)

        self.assertEqual(updated.getTitle(), "Новое название")
        self.assertEqual(updated.getOperationType(), category.getOperationType())  # Не изменилось
        self.assertEqual(updated.getType(), category.getType())  # Не изменилось

    def test_28_update_category_with_all_parameters(self):
        """Тест 28: Обновление категории со всеми параметрами"""

        # Создаем категорию
        category = self.service.get("Категория для полного обновления", 1, 0, None)
        self.test_category_ids.append(category.getId())

        # Обновляем все параметры
        updated = self.service.update(
            category,
            "Полностью новое название",
            self.Integer(2),  # Доход - Integer
            self.Integer(1),  # Дочерняя - Integer
            self.Integer(1),
        )  # Родитель - Integer

        self.assertEqual(updated.getTitle(), "Полностью новое название")
        self.assertEqual(updated.getOperationType(), 2)
        self.assertEqual(updated.getType(), 1)
        self.assertEqual(updated.getParentId(), 1)


if __name__ == '__main__':
    unittest.main()
