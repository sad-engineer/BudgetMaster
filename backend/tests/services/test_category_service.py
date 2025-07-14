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


class TestCategoryService(unittest.TestCase):
    """Юнит-тесты для CategoryService"""

    @classmethod
    def setUpClass(cls):
        if not setup_example():
            raise Exception("Не удалось настроить окружение для тестов")

        # Импортируем Java классы
        cls.CategoryService = get_java_class("service.CategoryService")
        cls.CategoryRepository = get_java_class("repository.CategoryRepository")
        cls.Category = get_java_class("model.Category")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        cls.test_category_ids = []
        cls.db_path = test_data_manager.db_manager.db_path
        cls.repository = cls.CategoryRepository(cls.db_path)
        cls.service = cls.CategoryService(cls.repository, "test_user")

    @classmethod
    def tearDownClass(cls):
        try:
            db_manager = test_data_manager.db_manager
            deleted_count = 0
            for category_id in cls.test_category_ids:
                try:
                    success = db_manager.execute_update("DELETE FROM categories WHERE id = ?", (category_id,))
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
        """Тест 01: Создание новой категории"""
        title = "Тестовая категория"
        category = self.service.create(title)
        self.test_category_ids.append(category.getId())
        self.assertIsNotNone(category)
        self.assertEqual(category.getTitle(), title)
        self.assertEqual(category.getCreatedBy(), "test_user")
        self.assertEqual(category.getUpdatedBy(), "test_user")
        self.assertIsNotNone(category.getCreateTime())
        self.assertIsNotNone(category.getUpdateTime())
        self.assertGreater(category.getPosition(), 0)

    def test_02_get_all_categories(self):
        """Тест 02: Получение всех категорий"""
        c1 = self.service.create("Категория 1")
        c2 = self.service.create("Категория 2")
        self.test_category_ids.append(c1.getId())
        self.test_category_ids.append(c2.getId())
        categories = self.service.getAll()
        self.assertIsNotNone(categories)
        self.assertGreater(len(categories), 0)
        ids = [c.getId() for c in categories]
        self.assertIn(c1.getId(), ids)
        self.assertIn(c2.getId(), ids)

    def test_03_get_category_by_id(self):
        """Тест 03: Получение категории по ID"""
        category = self.service.create("Категория по ID")
        self.test_category_ids.append(category.getId())
        found = self.service.getById(category.getId())
        self.assertTrue(found.isPresent())
        self.assertEqual(found.get().getId(), category.getId())

    def test_04_get_category_by_id_not_found(self):
        """Тест 04: Получение категории по несуществующему ID"""
        found = self.service.getById(999999)
        self.assertFalse(found.isPresent())

    def test_05_get_category_by_title(self):
        """Тест 05: Получение категории по названию"""
        title = "Уникальная категория"
        category = self.service.create(title)
        self.test_category_ids.append(category.getId())
        found = self.service.getByTitle(title)
        self.assertTrue(found.isPresent())
        self.assertEqual(found.get().getId(), category.getId())

    def test_06_get_category_by_title_not_found(self):
        """Тест 06: Получение категории по несуществующему названию"""
        found = self.service.getByTitle("Несуществующая категория")
        self.assertFalse(found.isPresent())

    def test_07_delete_category_by_id(self):
        """Тест 07: Удаление категории по ID"""
        category = self.service.create("Категория для удаления")
        self.test_category_ids.append(category.getId())
        result = self.service.delete(category.getId())
        self.assertTrue(result)
        # Проверяем, что категория помечена как удалённая
        found = self.repository.findById(category.getId())
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_08_delete_category_by_title(self):
        """Тест 08: Удаление категории по названию"""
        title = "Категория для удаления по названию"
        category = self.service.create(title)
        self.test_category_ids.append(category.getId())
        result = self.service.delete(title)
        self.assertTrue(result)
        # Проверяем, что категория помечена как удалённая
        found = self.repository.findByTitle(title)
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_09_restore_category(self):
        """Тест 09: Восстановление удалённой категории"""
        category = self.service.create("Категория для восстановления")
        self.test_category_ids.append(category.getId())
        self.repository.deleteById(category.getId(), "test_user")
        deleted = self.repository.findById(category.getId()).get()
        restored = self.service.restore(deleted)
        self.assertIsNotNone(restored)
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())
        self.assertEqual(restored.getUpdatedBy(), "test_user")

    def test_10_restore_category_by_id(self):
        """Тест 10: Восстановление категории по ID"""
        category = self.service.create("Категория для восстановления по ID")
        self.test_category_ids.append(category.getId())
        self.repository.deleteById(category.getId(), "test_user")
        restored = self.service.restore(category.getId())
        self.assertIsNotNone(restored)
        self.assertIsNone(restored.getDeleteTime())
        self.assertIsNone(restored.getDeletedBy())

    def test_11_restore_category_by_id_not_found(self):
        """Тест 11: Восстановление категории по несуществующему ID"""
        restored = self.service.restore(999999)
        self.assertIsNone(restored)

    def test_12_is_category_deleted(self):
        """Тест 12: Проверка удаления категории"""
        category = self.service.create("Категория для проверки удаления")
        self.test_category_ids.append(category.getId())
        self.repository.deleteById(category.getId(), "test_user")
        deleted = self.repository.findById(category.getId()).get()
        self.assertTrue(self.service.isCategoryDeleted(deleted))
        # Восстановим для очистки
        self.service.restore(deleted)

    def test_13_change_position(self):
        """Тест 13: Изменение позиции категории"""
        position = self.repository.getMaxPosition()
        c1 = self.service.create("Категория 1")
        c2 = self.service.create("Категория 2")
        c3 = self.service.create("Категория 3")
        self.test_category_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c1 на позицию 3
        result = self.service.changePosition(c1, position + 3)
        self.assertEqual(result.getPosition(), position + 3)
        # Проверяем, что другие категории сдвинулись
        updated_c2 = self.repository.findById(c2.getId()).get()
        updated_c3 = self.repository.findById(c3.getId()).get()
        self.assertEqual(updated_c2.getPosition(), position + 1)
        self.assertEqual(updated_c3.getPosition(), position + 2)

    def test_14_change_position_up(self):
        """Тест 14: Перемещение категории вверх"""
        position = self.repository.getMaxPosition()
        c1 = self.service.create("Категория A")
        c2 = self.service.create("Категория B")
        c3 = self.service.create("Категория C")
        self.test_category_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c3 на позицию 1
        result = self.service.changePosition(c3, position + 1)
        self.assertEqual(result.getPosition(), position + 1)
        updated_c1 = self.repository.findById(c1.getId()).get()
        updated_c2 = self.repository.findById(c2.getId()).get()
        self.assertEqual(updated_c1.getPosition(), position + 2)
        self.assertEqual(updated_c2.getPosition(), position + 3)

    def test_15_change_position_by_old_new(self):
        """Тест 15: Изменение позиции по старой и новой позиции"""
        position = self.repository.getMaxPosition()
        c1 = self.service.create("Категория X")
        c2 = self.service.create("Категория Y")
        c3 = self.service.create("Категория Z")
        self.test_category_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        result = self.service.changePosition(position + 1, position + 3)
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), position + 3)

    def test_16_change_position_by_old_new_not_found(self):
        """Тест 16: Изменение позиции по несуществующей старой позиции"""
        result = self.service.changePosition(999, 1)
        self.assertIsNone(result)

    def test_17_change_position_same_position(self):
        """Тест 17: Изменение позиции на ту же позицию"""
        category = self.service.create("Категория без изменений")
        self.test_category_ids.append(category.getId())
        old_position = category.getPosition()
        result = self.service.changePosition(category, old_position)
        self.assertEqual(result.getPosition(), old_position)

    def test_18_change_position_invalid_position(self):
        """Тест 18: Изменение позиции на недопустимую"""
        category = self.service.create("Категория для теста")
        self.test_category_ids.append(category.getId())
        with self.assertRaises(Exception):
            self.service.changePosition(category, 999)

    def test_19_set_user_unsupported(self):
        """Тест 19: Попытка сменить пользователя"""
        with self.assertRaises(Exception):
            self.service.setUser("new_user")

    def test_20_create_category_with_special_title(self):
        """Тест 20: Создание категории с необычным названием"""
        category = self.service.create("")
        self.test_category_ids.append(category.getId())
        self.assertEqual(category.getTitle(), "")
        
        category2 = self.service.create("Категория с цифрами 123")
        self.test_category_ids.append(category2.getId())
        self.assertEqual(category2.getTitle(), "Категория с цифрами 123")

    def test_21_category_position_sequence(self):
        """Тест 21: Последовательность позиций категорий"""
        categories = []
        position = self.repository.getMaxPosition()
        for i in range(4):
            c = self.service.create(f"Категория {i + 1}")
            categories.append(c)
            self.test_category_ids.append(c.getId())
        for i, c in enumerate(categories):
            self.assertEqual(c.getPosition(), position + i + 1)

    def test_22_delete_and_restore_cycle(self):
        """Тест 22: Цикл удаления и восстановления категории"""
        category = self.service.create("Категория для цикла")
        self.test_category_ids.append(category.getId())
        self.service.delete(category.getId())
        restored1 = self.service.restore(category.getId())
        self.assertEqual(restored1.getId(), category.getId())
        self.service.delete(category.getId())
        restored2 = self.service.restore(category.getId())
        self.assertEqual(restored2.getId(), category.getId())

    def test_23_get_all_by_operation_type(self):
        """Тест 23: Получение категорий по типу операции"""
        # Создаем категории с разными типами операций
        c1 = self.service.create("Категория типа 1")
        c2 = self.service.create("Категория типа 2")
        self.test_category_ids.append(c1.getId())
        self.test_category_ids.append(c2.getId())
        
        # Устанавливаем типы операций через репозиторий
        c1.setOperationType(1)
        c2.setOperationType(2)
        self.repository.update(c1)
        self.repository.update(c2)
        
        categories_type_1 = self.service.getAllByOperationType(1)
        categories_type_2 = self.service.getAllByOperationType(2)
        
        self.assertGreater(categories_type_1.size(), 0)
        self.assertGreater(categories_type_2.size(), 0)
        
        # Проверяем, что наши категории есть в соответствующих списках
        type_1_ids = []
        type_2_ids = []
        for c in categories_type_1:
            type_1_ids.append(c.getId())
        for c in categories_type_2:
            type_2_ids.append(c.getId())
        
        self.assertIn(c1.getId(), type_1_ids)
        self.assertIn(c2.getId(), type_2_ids)

    def test_24_get_all_by_type(self):
        """Тест 24: Получение категорий по типу категории"""
        # Создаем категории с разными типами
        c1 = self.service.create("Категория типа A")
        c2 = self.service.create("Категория типа B")
        self.test_category_ids.append(c1.getId())
        self.test_category_ids.append(c2.getId())
        
        # Устанавливаем типы через репозиторий
        c1.setType(1)
        c2.setType(2)
        self.repository.update(c1)
        self.repository.update(c2)
        
        categories_type_1 = self.service.getAllByType(1)
        categories_type_2 = self.service.getAllByType(2)
        
        self.assertGreater(categories_type_1.size(), 0)
        self.assertGreater(categories_type_2.size(), 0)
        
        # Проверяем, что наши категории есть в соответствующих списках
        type_1_ids = []
        type_2_ids = []
        for c in categories_type_1:
            type_1_ids.append(c.getId())
        for c in categories_type_2:
            type_2_ids.append(c.getId())
        
        self.assertIn(c1.getId(), type_1_ids)
        self.assertIn(c2.getId(), type_2_ids)

    def test_25_get_all_by_parent_id(self):
        """Тест 25: Получение категорий по ID родительской категории"""
        # Создаем родительскую и дочерние категории
        parent = self.service.create("Родительская категория")
        child1 = self.service.create("Дочерняя категория 1")
        child2 = self.service.create("Дочерняя категория 2")
        self.test_category_ids.extend([parent.getId(), child1.getId(), child2.getId()])
        
        # Устанавливаем родительские ID через репозиторий
        child1.setParentId(parent.getId())
        child2.setParentId(parent.getId())
        self.repository.update(child1)
        self.repository.update(child2)
        
        children = self.service.getAllByParentId(parent.getId())
        self.assertGreater(children.size(), 0)
        
        # Проверяем, что дочерние категории есть в списке
        child_ids = []
        for c in children:
            child_ids.append(c.getId())
        
        self.assertIn(child1.getId(), child_ids)
        self.assertIn(child2.getId(), child_ids)

    def test_26_create_category_with_unicode_title(self):
        """Тест 26: Создание категории с Unicode названием"""
        title = "Категория с кириллицей и символами: !@#$%^&*()"
        category = self.service.create(title)
        self.test_category_ids.append(category.getId())
        self.assertEqual(category.getTitle(), title)

    def test_27_category_audit_fields(self):
        """Тест 27: Проверка аудит-полей категории"""
        category = self.service.create("Категория для аудита")
        self.test_category_ids.append(category.getId())
        
        # Проверяем, что все аудит-поля заполнены
        self.assertEqual(category.getCreatedBy(), "test_user")
        self.assertEqual(category.getUpdatedBy(), "test_user")
        self.assertIsNotNone(category.getCreateTime())
        self.assertIsNotNone(category.getUpdateTime())
        
        # Проверяем, что время создания и обновления совпадают при создании
        self.assertEqual(category.getCreateTime(), category.getUpdateTime())


if __name__ == '__main__':
    unittest.main() 