import os
import sys
import unittest

from tests.backend.test_common import cleanup_example, get_java_class, setup_example

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestCurrencyRepository(unittest.TestCase):
    """Юнит-тесты для CurrencyRepository"""

    @classmethod
    def setUpClass(cls):
        """Настройка окружения перед всеми тестами"""
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.Currency = get_java_class("com.sadengineer.budgetmaster.backend.model.Currency")
        cls.CurrencyRepository = get_java_class("com.sadengineer.budgetmaster.backend.repository.CurrencyRepository")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")
        cls.PlatformUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.PlatformUtil")

        # Инициализируем DatabaseProvider для тестов
        cls.PlatformUtil.initializeDatabaseProvider(None)

        # Используем DB_PATH из test_common.py
        cls.test_db_path = cls.db_manager.db_path

        # Инициализируем базу данных с таблицами
        cls.DatabaseUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.DatabaseUtil")
        cls.DatabaseUtil.createDatabaseIfNotExists(cls.test_db_path)
        print(f"✅ База данных инициализирована: {cls.test_db_path}")

        cls.repo = cls.CurrencyRepository(cls.test_db_path)

        # Список ID тестовых записей для очистки
        cls.test_currency_ids = []

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            # Получаем менеджер базы данных
            db_manager = cls.db_manager

            # Удаляем тестовые записи по ID
            deleted_count = 0
            for currency_id in cls.test_currency_ids:
                try:
                    success = db_manager.execute_update("DELETE FROM currencies WHERE id = ?", (currency_id,))
                    if success:
                        deleted_count += 1
                    else:
                        print(f"Ошибка при удалении валюты {currency_id}")
                except Exception as e:
                    print(f"Ошибка при удалении валюты {currency_id}: {e}")

            if deleted_count > 0:
                print(f"Удалено {deleted_count} тестовых валют из базы данных")

            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            # Не завершаем JVM здесь - пусть это делает atexit
            pass

    def setUp(self):
        """Настройка перед каждым тестом"""
        # Инициализируем базу данных перед первым использованием
        print(f"🔧 Инициализируем базу: {self.test_db_path}")
        self.DatabaseUtil.createDatabaseIfNotExists(self.test_db_path)

        # Проверяем, что база создана
        if os.path.exists(self.test_db_path):
            file_size = os.path.getsize(self.test_db_path)
            print(f"✅ База создана, размер: {file_size} байт")
        else:
            print(f"❌ База не создана: {self.test_db_path}")

        self.max_position = self.repo.getMaxPosition()

    def create_test_currency(self, title="Тестовая валюта", position=None):
        """Создает тестовую валюту"""
        if position is None:
            position = self.repo.getMaxPosition() + 1

        currency = self.Currency()
        currency.setTitle(title)
        currency.setPosition(position)
        currency.setCreatedBy("test_user")
        currency.setUpdatedBy("test_user")

        # Устанавливаем даты
        now = self.LocalDateTime.now()
        currency.setCreateTime(now)
        currency.setUpdateTime(now)
        currency.setDeleteTime(None)

        return currency

    def test_01_delete_by_id(self):
        """Тест 01: Удаление валюты по ID"""
        # Arrange
        currency = self.create_test_currency("Валюта для удаления")
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act
        deleted = self.repo.deleteById(saved_currency.getId(), "test_delete")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что валюта помечена как удаленная
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        deleted_currency = found.get()
        self.assertIsNotNone(deleted_currency.getDeleteTime())
        self.assertEqual(deleted_currency.getDeletedBy(), "test_delete")

    def test_02_delete_by_title(self):
        """Тест 02: Удаление валюты по названию"""
        # Arrange
        currency = self.create_test_currency("Валюта для удаления по названию")
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act
        deleted = self.repo.deleteByTitle("Валюта для удаления по названию", "test_delete_title")

        # Assert
        self.assertTrue(deleted)

        # Проверяем, что валюта помечена как удаленная
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        deleted_currency = found.get()
        self.assertIsNotNone(deleted_currency.getDeleteTime())
        self.assertEqual(deleted_currency.getDeletedBy(), "test_delete_title")

    def test_03_find_all(self):
        """Тест 03: Получение всех валют"""
        # Arrange
        currency1 = self.create_test_currency("Валюта 1", self.repo.getMaxPosition() + 1)
        currency2 = self.create_test_currency("Валюта 2", self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(currency1)
        saved2 = self.repo.save(currency2)
        self.test_currency_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        all_currencies = self.repo.findAll()

        # Assert
        # Java возвращает ArrayList, проверяем что это коллекция
        self.assertIsNotNone(all_currencies)
        self.assertGreater(all_currencies.size(), 0)

        # Проверяем, что наши валюты есть в списке
        titles = []
        for curr in all_currencies:
            titles.append(curr.getTitle())
        self.assertIn("Валюта 1", titles)
        self.assertIn("Валюта 2", titles)

    def test_04_find_by_id(self):
        """Тест 04: Поиск валюты по ID"""
        # Arrange
        currency = self.create_test_currency("Валюта для поиска")
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act
        found_currency = self.repo.findById(saved_currency.getId())

        # Assert
        self.assertTrue(found_currency.isPresent())
        found = found_currency.get()
        self.assertEqual(found.getId(), saved_currency.getId())
        self.assertEqual(found.getTitle(), "Валюта для поиска")

    def test_05_find_by_title(self):
        """Тест 05: Поиск валюты по названию"""
        # Arrange
        unique_title = f"Уникальная валюта {self.LocalDateTime.now().toString()}"
        currency = self.create_test_currency(unique_title)
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act
        found_currency = self.repo.findByTitle(unique_title)

        # Assert
        self.assertTrue(found_currency.isPresent())
        found = found_currency.get()
        self.assertEqual(found.getId(), saved_currency.getId())
        self.assertEqual(found.getTitle(), unique_title)

    def test_06_get_max_position(self):
        """Тест 06: Получение максимальной позиции"""
        # Arrange
        currency1 = self.create_test_currency("Валюта 1", self.repo.getMaxPosition() + 1)
        currency2 = self.create_test_currency("Валюта 2", self.repo.getMaxPosition() + 2)

        saved1 = self.repo.save(currency1)
        saved2 = self.repo.save(currency2)
        self.test_currency_ids.extend([saved1.getId(), saved2.getId()])

        # Act
        max_position = self.repo.getMaxPosition()

        # Assert
        self.assertIsInstance(max_position, int)
        self.assertGreater(max_position, 0)
        self.assertGreaterEqual(max_position, saved2.getPosition())

    def test_07_save_currency(self):
        """Тест 07: Сохранение новой валюты"""
        # Arrange
        currency = self.create_test_currency("Валюта для сохранения")

        # Act
        saved_currency = self.repo.save(currency)

        # Assert
        self.assertIsNotNone(saved_currency)
        self.assertGreater(saved_currency.getId(), 0)
        self.assertEqual(saved_currency.getTitle(), "Валюта для сохранения")
        self.assertEqual(saved_currency.getPosition(), currency.getPosition())

        # Сохраняем ID для очистки
        self.test_currency_ids.append(saved_currency.getId())

    def test_08_update_currency(self):
        """Тест 08: Обновление валюты"""
        # Arrange
        currency = self.create_test_currency("Валюта для обновления")
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act
        saved_currency.setTitle("Обновленная валюта")
        saved_currency.setPosition(999)
        saved_currency.setUpdatedBy("test_update")
        updated_currency = self.repo.update(saved_currency)

        # Assert
        self.assertEqual(updated_currency.getTitle(), "Обновленная валюта")
        self.assertEqual(updated_currency.getPosition(), 999)
        self.assertEqual(updated_currency.getUpdatedBy(), "test_update")

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        found_currency = found.get()
        self.assertEqual(found_currency.getTitle(), "Обновленная валюта")
        self.assertEqual(found_currency.getPosition(), 999)

    def test_09_find_nonexistent_currency(self):
        """Тест 09: Поиск несуществующей валюты (findById)"""
        # Act
        found_currency = self.repo.findById(self.Integer(99999))

        # Assert
        self.assertFalse(found_currency.isPresent())

    def test_10_delete_nonexistent_currency(self):
        """Тест 10: Удаление несуществующей валюты (deleteById)"""
        # Act
        deleted = self.repo.deleteById(self.Integer(99999), "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_11_delete_nonexistent_currency_by_title(self):
        """Тест 11: Удаление несуществующей валюты по названию (deleteByTitle)"""
        # Act
        deleted = self.repo.deleteByTitle("Несуществующая валюта", "test_user")

        # Assert
        self.assertFalse(deleted)

    def test_12_currency_position_ordering(self):
        """Тест 12: Проверка упорядочивания валют по позиции (getMaxPosition)"""
        # Arrange
        currency1 = self.create_test_currency("Валюта позиция 1", 1)
        currency2 = self.create_test_currency("Валюта позиция 2", 2)
        currency3 = self.create_test_currency("Валюта позиция 3", 3)

        saved1 = self.repo.save(currency1)
        saved2 = self.repo.save(currency2)
        saved3 = self.repo.save(currency3)
        self.test_currency_ids.extend([saved1.getId(), saved2.getId(), saved3.getId()])

        # Act
        all_currencies = self.repo.findAll()

        # Assert
        # Проверяем, что валюты с нашими позициями есть в списке
        positions = []
        for curr in all_currencies:
            positions.append(curr.getPosition())
        self.assertIn(1, positions)
        self.assertIn(2, positions)
        self.assertIn(3, positions)

    def test_13_currency_soft_delete_behavior(self):
        """Тест 13: Проверка поведения soft delete (deleteById)"""
        # Arrange
        currency = self.create_test_currency("Валюта для soft delete")
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act - удаляем валюту
        deleted = self.repo.deleteById(saved_currency.getId(), "test_soft_delete")

        # Assert
        self.assertTrue(deleted)

        # Валюта должна быть найдена, но помечена как удаленная
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        deleted_currency = found.get()
        self.assertIsNotNone(deleted_currency.getDeleteTime())
        self.assertEqual(deleted_currency.getDeletedBy(), "test_soft_delete")

        # Валюта должна остаться в списке всех валют (включая удаленные)
        all_currencies = self.repo.findAll()
        currency_ids = []
        for curr in all_currencies:
            currency_ids.append(curr.getId())
        self.assertIn(saved_currency.getId(), currency_ids)

    def test_14_currency_with_special_characters(self):
        """Тест 14: Создание валюты со специальными символами (mapRowSafe)"""
        # Arrange
        currency = self.create_test_currency("€ Евро (EUR)")

        # Act
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Assert
        self.assertEqual(saved_currency.getTitle(), "€ Евро (EUR)")

        # Проверяем, что специальные символы корректно сохранились в БД
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        found_currency = found.get()
        self.assertEqual(found_currency.getTitle(), "€ Евро (EUR)")

    def test_15_currency_with_long_title(self):
        """Тест 15: Создание валюты с длинным названием (mapRowSafe)"""
        # Arrange
        long_title = "Очень длинное название валюты с множеством символов для тестирования граничных случаев"
        currency = self.create_test_currency(long_title)

        # Act
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Assert
        self.assertEqual(saved_currency.getTitle(), long_title)

        # Проверяем, что длинное название корректно сохранилось в БД
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        found_currency = found.get()
        self.assertEqual(found_currency.getTitle(), long_title)

    def test_16_currency_case_sensitive_search(self):
        """Тест 16: Проверка чувствительности к регистру при поиске (findByTitle)"""
        # Arrange
        currency = self.create_test_currency("Доллар США")
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act & Assert - поиск с разным регистром
        found_exact = self.repo.findByTitle("Доллар США")
        found_upper = self.repo.findByTitle("ДОЛЛАР США")
        found_lower = self.repo.findByTitle("доллар сша")

        # Assert
        self.assertTrue(found_exact.isPresent())
        self.assertFalse(found_upper.isPresent())  # Чувствительно к регистру
        self.assertFalse(found_lower.isPresent())  # Чувствительно к регистру

    def test_17_currency_null_fields_handling(self):
        """Тест 17: Обработка NULL полей валюты (mapRowSafe)"""
        # Arrange
        currency = self.create_test_currency("Валюта с NULL полями")
        currency.setCreatedBy(None)
        currency.setUpdatedBy(None)
        currency.setDeleteTime(None)

        # Act
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Assert
        self.assertIsNone(saved_currency.getCreatedBy())
        self.assertIsNone(saved_currency.getUpdatedBy())
        self.assertIsNone(saved_currency.getDeleteTime())

        # Проверяем, что NULL значения корректно сохранились в БД
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        found_currency = found.get()
        self.assertIsNone(found_currency.getCreatedBy())
        self.assertIsNone(found_currency.getUpdatedBy())
        self.assertIsNone(found_currency.getDeleteTime())

    def test_18_currency_update_with_null_fields(self):
        """Тест 18: Обновление валюты с NULL полями (mapRowSafe)"""
        # Arrange
        currency = self.create_test_currency("Валюта для обновления NULL")
        saved_currency = self.repo.save(currency)
        self.test_currency_ids.append(saved_currency.getId())

        # Act
        saved_currency.setTitle("Обновленная с NULL")
        saved_currency.setCreatedBy(None)
        saved_currency.setUpdatedBy(None)
        updated_currency = self.repo.update(saved_currency)

        # Assert
        self.assertEqual(updated_currency.getTitle(), "Обновленная с NULL")
        self.assertIsNone(updated_currency.getCreatedBy())
        self.assertIsNone(updated_currency.getUpdatedBy())

        # Проверяем, что изменения сохранились в БД
        found = self.repo.findById(saved_currency.getId())
        self.assertTrue(found.isPresent())
        found_currency = found.get()
        self.assertEqual(found_currency.getTitle(), "Обновленная с NULL")
        self.assertIsNone(found_currency.getCreatedBy())
        self.assertIsNone(found_currency.getUpdatedBy())

    def test_19_currency_duplicate_titles(self):
        """Тест 19: Создание валют с одинаковыми названиями"""
        # Arrange
        currency1 = self.create_test_currency("Дублированная валюта")
        currency2 = self.create_test_currency("Дублированная валюта")

        # Act
        saved1 = self.repo.save(currency1)
        saved2 = self.repo.save(currency2)
        self.test_currency_ids.extend([saved1.getId(), saved2.getId()])

        # Assert
        self.assertNotEqual(saved1.getId(), saved2.getId())
        self.assertEqual(saved1.getTitle(), saved2.getTitle())

        # Проверяем, что findByTitle возвращает первую найденную
        found = self.repo.findByTitle("Дублированная валюта")
        self.assertTrue(found.isPresent())
        # findByTitle возвращает первую найденную валюту с таким названием

    def test_20_currency_edge_positions(self):
        """Тест 20: Тестирование граничных значений позиций"""
        # Arrange
        currency_min = self.create_test_currency("Валюта с минимальной позицией", 1)
        currency_max = self.create_test_currency("Валюта с максимальной позицией", 999999)
        currency_zero = self.create_test_currency("Валюта с позицией 0", 0)

        # Act
        saved_min = self.repo.save(currency_min)
        saved_max = self.repo.save(currency_max)
        saved_zero = self.repo.save(currency_zero)
        self.test_currency_ids.extend([saved_min.getId(), saved_max.getId(), saved_zero.getId()])

        # Assert
        self.assertEqual(saved_min.getPosition(), 1)
        self.assertEqual(saved_max.getPosition(), 999999)
        self.assertEqual(saved_zero.getPosition(), 0)

        # Проверяем, что все валюты найдены
        found_min = self.repo.findById(saved_min.getId())
        found_max = self.repo.findById(saved_max.getId())
        found_zero = self.repo.findById(saved_zero.getId())

        self.assertTrue(found_min.isPresent())
        self.assertTrue(found_max.isPresent())
        self.assertTrue(found_zero.isPresent())


if __name__ == '__main__':
    unittest.main(verbosity=2)
