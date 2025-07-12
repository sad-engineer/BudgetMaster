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


class TestCategoryRepository(unittest.TestCase):
    """Юнит-тесты для CategoryRepository"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        if not setup_example():
            raise Exception("Не удалось настроить окружение для тестов")

        # Импортируем Java классы
        cls.Category = get_java_class("model.Category")
        cls.CategoryRepository = get_java_class("repository.CategoryRepository")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")

        # Создаем репозиторий
        cls.repo = cls.CategoryRepository(test_data_manager.db_manager.db_path)

        # Список ID тестовых записей для очистки
        cls.test_category_ids = []

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            # Получаем менеджер базы данных
            db_manager = test_data_manager.db_manager

            # Удаляем тестовые записи по ID
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
            # Не завершаем JVM здесь - пусть это делает atexit
            pass

    def setUp(self):
        """Настройка перед каждым тестом"""
        self.max_position = self.repo.getMaxPosition()

    def create_test_category(self, title="Тестовая категория", position=None, operation_type=1, type=1, parent_id=None):
        """Создает тестовую категорию"""
        if position is None:
            position = self.repo.getMaxPosition() + 1

        category = self.Category()
        category.setTitle(title)
        category.setPosition(position)
        category.setOperationType(operation_type)
        category.setType(type)
        category.setParentId(parent_id)
        category.setCreatedBy("test_user")
        category.setUpdatedBy("test_user")

        # Устанавливаем даты
        now = self.LocalDateTime.now()
        category.setCreateTime(now)
        category.setUpdateTime(now)
        category.setDeleteTime(None)

        return category

    def test_01_delete_by_id(self):
        """Тест 01: Удаление категории по ID"""
        # Arrange
        category = self.create_test_category("Категория для удаления")
        saved_category = self.repo.save(category)
        self.test_category_ids.append(saved_category.getId())

        # Act
        deleted = self.repo.deleteById(saved_category.getId(), "test_delete")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что категория помечена как удаленная
        found = self.repo.findById(saved_category.getId())
        self.assertTrue(found.isPresent())
        deleted_category = found.get()
        self.assertIsNotNone(deleted_category.getDeleteTime())
        self.assertEqual(deleted_category.getDeletedBy(), "test_delete")

    def test_02_delete_by_title(self):
        """Тест 02: Удаление категории по названию"""
        # Arrange
        category = self.create_test_category("Категория для удаления по названию")
        saved_category = self.repo.save(category)
        self.test_category_ids.append(saved_category.getId())

        # Act
        deleted = self.repo.deleteByTitle("Категория для удаления по названию", "test_delete_title")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что категория помечена как удаленная
        found = self.repo.findById(saved_category.getId())
        self.assertTrue(found.isPresent())
        deleted_category = found.get()
        self.assertIsNotNone(deleted_category.getDeleteTime())
        self.assertEqual(deleted_category.getDeletedBy(), "test_delete_title")

    def test_03_find_all(self):
        """Тест 03: Получение всех категорий"""
        # Arrange
        category1 = self.create_test_category("Категория 1", self.repo.getMaxPosition() + 1)
        category2 = self.create_test_category("Категория 2", self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(category1)
        saved2 = self.repo.save(category2)
        self.test_category_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        all_categories = self.repo.findAll()

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(all_categories)
        self.assertGreater(all_categories.size(), 0)

        # Проверяем, что наши категории есть в списке
        titles = []
        for cat in all_categories:
            titles.append(cat.getTitle())
        self.assertIn("Категория 1", titles)
        self.assertIn("Категория 2", titles)

    def test_04_find_all_by_operation_type(self):
        """Тест 04: Поиск категорий по типу операции"""
        # Arrange
        category1 = self.create_test_category("Категория доходов 1", operation_type=1)
        category2 = self.create_test_category("Категория доходов 2", operation_type=1)
        category3 = self.create_test_category("Категория расходов", operation_type=2)

        saved1 = self.repo.save(category1)
        saved2 = self.repo.save(category2)
        saved3 = self.repo.save(category3)
        self.test_category_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        income_categories = self.repo.findAllByOperationType(self.Integer(1))  # Доходы

        # Assert
        self.assertIsNotNone(income_categories)
        self.assertGreater(income_categories.size(), 0)

        # Проверяем, что все найденные категории имеют тип операции 1
        for cat in income_categories:
            self.assertEqual(cat.getOperationType(), 1)

    def test_05_find_all_by_parent_id(self):
        """Тест 05: Поиск категорий по ID родительской категории"""
        # Arrange
        parent_category = self.create_test_category("Родительская категория")
        saved_parent = self.repo.save(parent_category)
        self.test_category_ids.append(saved_parent.getId())

        child1 = self.create_test_category("Дочерняя категория 1", parent_id=saved_parent.getId())
        child2 = self.create_test_category("Дочерняя категория 2", parent_id=saved_parent.getId())

        saved_child1 = self.repo.save(child1)
        saved_child2 = self.repo.save(child2)
        self.test_category_ids.extend([saved_child1.getId(), saved_child2.getId()])

        # Act
        child_categories = self.repo.findAllByParentId(self.Integer(saved_parent.getId()))

        # Assert
        self.assertIsNotNone(child_categories)
        self.assertGreater(child_categories.size(), 0)

        # Проверяем, что все найденные категории имеют правильный parent_id
        for cat in child_categories:
            self.assertEqual(cat.getParentId(), saved_parent.getId())

    def test_06_find_all_by_type(self):
        """Тест 06: Поиск категорий по типу"""
        # Arrange
        category1 = self.create_test_category("Категория типа 1", type=1)
        category2 = self.create_test_category("Категория типа 1 еще", type=1)
        category3 = self.create_test_category("Категория типа 2", type=2)

        saved1 = self.repo.save(category1)
        saved2 = self.repo.save(category2)
        saved3 = self.repo.save(category3)
        self.test_category_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        type1_categories = self.repo.findAllByType(self.Integer(1))

        # Assert
        self.assertIsNotNone(type1_categories)
        self.assertGreater(type1_categories.size(), 0)

        # Проверяем, что все найденные категории имеют тип 1
        for cat in type1_categories:
            self.assertEqual(cat.getType(), 1)

    def test_07_find_by_id(self):
        """Тест 07: Поиск категории по ID"""
        # Arrange
        category = self.create_test_category("Категория для поиска")
        saved_category = self.repo.save(category)
        self.test_category_ids.append(saved_category.getId())

        # Act
        found_category = self.repo.findById(saved_category.getId())

        # Assert
        self.assertTrue(found_category.isPresent())
        found = found_category.get()
        self.assertEqual(found.getId(), saved_category.getId())
        self.assertEqual(found.getTitle(), "Категория для поиска")

    def test_08_find_by_title(self):
        """Тест 08: Поиск категории по названию"""
        # Arrange
        unique_title = f"Уникальная категория {self.LocalDateTime.now().toString()}"
        category = self.create_test_category(unique_title)
        saved_category = self.repo.save(category)
        self.test_category_ids.append(saved_category.getId())

        # Act
        found_category = self.repo.findByTitle(unique_title)

        # Assert
        self.assertTrue(found_category.isPresent())
        found = found_category.get()
        self.assertEqual(found.getId(), saved_category.getId())
        self.assertEqual(found.getTitle(), unique_title)

    def test_09_get_max_position(self):
        """Тест 09: Получение максимальной позиции"""
        # Arrange
        category1 = self.create_test_category("Категория 1", self.repo.getMaxPosition() + 1)
        category2 = self.create_test_category("Категория 2", self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(category1)
        saved2 = self.repo.save(category2)
        self.test_category_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        max_position = self.repo.getMaxPosition()

        # Assert
        self.assertIsInstance(max_position, int)
        self.assertGreater(max_position, 0)
        self.assertGreaterEqual(max_position, saved2.getPosition())

    def test_10_save_category(self):
        """Тест 10: Сохранение новой категории"""
        # Arrange
        category = self.create_test_category("Категория для сохранения")

        # Act
        saved_category = self.repo.save(category)

        # Assert
        self.assertIsNotNone(saved_category)
        self.assertGreater(saved_category.getId(), 0)
        self.assertEqual(saved_category.getTitle(), "Категория для сохранения")
        self.assertEqual(saved_category.getPosition(), category.getPosition())
        self.assertEqual(saved_category.getOperationType(), 1)
        self.assertEqual(saved_category.getType(), 1)

        # Сохраняем ID для очистки
        self.test_category_ids.append(saved_category.getId())

    def test_11_update_category(self):
        """Тест 11: Обновление категории"""
        # Arrange
        category = self.create_test_category("Категория для обновления")
        saved_category = self.repo.save(category)
        self.test_category_ids.append(saved_category.getId())

        # Act
        saved_category.setTitle("Обновленная категория")
        saved_category.setOperationType(2)
        saved_category.setType(2)
        saved_category.setUpdatedBy("test_update")
        updated_category = self.repo.update(saved_category)

        # Assert
        self.assertEqual(updated_category.getTitle(), "Обновленная категория")
        self.assertEqual(updated_category.getOperationType(), 2)
        self.assertEqual(updated_category.getType(), 2)
        self.assertEqual(updated_category.getUpdatedBy(), "test_update")

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_category.getId())
        self.assertTrue(found.isPresent())
        found_category = found.get()
        self.assertEqual(found_category.getTitle(), "Обновленная категория")
        self.assertEqual(found_category.getOperationType(), 2)
        self.assertEqual(found_category.getType(), 2)

    def test_12_category_with_parent_id(self):
        """Тест 12: Создание категории с parent_id (mapRowSafe)"""
        # Arrange
        parent_category = self.create_test_category("Родитель")
        saved_parent = self.repo.save(parent_category)
        self.test_category_ids.append(saved_parent.getId())

        child_category = self.create_test_category("Дочерняя", parent_id=saved_parent.getId())

        # Act
        saved_child = self.repo.save(child_category)
        self.test_category_ids.append(saved_child.getId())

        # Assert
        self.assertEqual(saved_child.getParentId(), saved_parent.getId())

        # Проверяем, что parent_id корректно сохранился в БД
        found = self.repo.findById(saved_child.getId())
        self.assertTrue(found.isPresent())
        found_category = found.get()
        self.assertEqual(found_category.getParentId(), saved_parent.getId())

    def test_13_category_with_null_parent_id(self):
        """Тест 13: Создание категории с NULL parent_id (mapRowSafe)"""
        # Arrange
        category = self.create_test_category("Категория без родителя")
        category.setParentId(None)

        # Act
        saved_category = self.repo.save(category)
        self.test_category_ids.append(saved_category.getId())

        # Assert
        self.assertIsNone(saved_category.getParentId())

        # Проверяем, что NULL значение корректно сохранилось в БД
        found = self.repo.findById(saved_category.getId())
        self.assertTrue(found.isPresent())
        found_category = found.get()
        self.assertIsNone(found_category.getParentId())

    def test_14_find_nonexistent_category(self):
        """Тест 14: Поиск несуществующей категории (findById)"""
        # Act
        found_category = self.repo.findById(self.Integer(99999))

        # Assert
        self.assertFalse(found_category.isPresent())

    def test_15_delete_nonexistent_category(self):
        """Тест 15: Удаление несуществующей категории (deleteById)"""
        # Act
        deleted = self.repo.deleteById(self.Integer(99999), "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_16_delete_nonexistent_category_by_title(self):
        """Тест 16: Удаление несуществующей категории по названию (deleteByTitle)"""
        # Act
        deleted = self.repo.deleteByTitle("Несуществующая категория", "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_17_find_by_nonexistent_operation_type(self):
        """Тест 17: Поиск категорий по несуществующему типу операции (findAllByOperationType)"""
        # Act
        categories = self.repo.findAllByOperationType(self.Integer(999))

        # Assert
        self.assertIsNotNone(categories)
        self.assertEqual(categories.size(), 0)

    def test_18_find_by_nonexistent_parent_id(self):
        """Тест 18: Поиск категорий по несуществующему parent_id (findAllByParentId)"""
        # Act
        categories = self.repo.findAllByParentId(self.Integer(999))

        # Assert
        self.assertIsNotNone(categories)
        self.assertEqual(categories.size(), 0)

    def test_19_find_by_nonexistent_type(self):
        """Тест 19: Поиск категорий по несуществующему типу (findAllByType)"""
        # Act
        categories = self.repo.findAllByType(self.Integer(999))

        # Assert
        self.assertIsNotNone(categories)
        self.assertEqual(categories.size(), 0)

    def test_20_category_hierarchy(self):
        """Тест 20: Проверка иерархии категорий"""
        # Arrange
        # Создаем корневую категорию
        root_category = self.create_test_category("Корневая категория", parent_id=None)
        saved_root = self.repo.save(root_category)
        self.test_category_ids.append(saved_root.getId())

        # Создаем дочерние категории
        child1 = self.create_test_category("Дочерняя 1", parent_id=saved_root.getId())
        child2 = self.create_test_category("Дочерняя 2", parent_id=saved_root.getId())

        saved_child1 = self.repo.save(child1)
        saved_child2 = self.repo.save(child2)
        self.test_category_ids.extend([saved_child1.getId(), saved_child2.getId()])

        # Создаем внучатые категории
        grandchild1 = self.create_test_category("Внучатая 1", parent_id=saved_child1.getId())
        grandchild2 = self.create_test_category("Внучатая 2", parent_id=saved_child1.getId())

        saved_grandchild1 = self.repo.save(grandchild1)
        saved_grandchild2 = self.repo.save(grandchild2)
        self.test_category_ids.extend([saved_grandchild1.getId(), saved_grandchild2.getId()])

        # Act & Assert
        # Проверяем корневую категорию
        root_found = self.repo.findById(saved_root.getId())
        self.assertTrue(root_found.isPresent())
        self.assertIsNone(root_found.get().getParentId())

        # Проверяем дочерние категории
        children = self.repo.findAllByParentId(self.Integer(saved_root.getId()))
        self.assertEqual(children.size(), 2)

        # Проверяем внучатые категории
        grandchildren = self.repo.findAllByParentId(self.Integer(saved_child1.getId()))
        self.assertEqual(grandchildren.size(), 2)


if __name__ == '__main__':
    unittest.main(verbosity=2)
