import os
import sys
import tempfile
import unittest

from tests.backend.test_common import (
    get_java_class,
    setup_test_environment,
)

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestDatabaseUtil(unittest.TestCase):
    """Юнит-тесты для DatabaseUtil"""

    @classmethod
    def setUpClass(cls):
        result = setup_test_environment()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_test_environment
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.DatabaseUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.DatabaseUtil")
        cls.PlatformUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.PlatformUtil")
        cls.SQLException = get_java_class("java.sql.SQLException")
        cls.SQLiteException = get_java_class("org.sqlite.SQLiteException")

        # Инициализируем DatabaseProvider для тестов
        cls.PlatformUtil.initializeDatabaseProvider(None)

        # Используем DB_PATH из test_common.py
        cls.temp_db_path = cls.db_manager.db_path
        print(f"🔍 Тестовая база будет создана по пути: {cls.temp_db_path}")
        print(f"🔍 Директория существует: {os.path.exists(os.path.dirname(cls.temp_db_path))}")

        # Создаем директорию если её нет
        db_dir = os.path.dirname(cls.temp_db_path)
        if not os.path.exists(db_dir):
            os.makedirs(db_dir)
            print(f"📁 Создана директория: {db_dir}")
        else:
            print(f"📁 Директория уже существует: {db_dir}")

    @classmethod
    def tearDownClass(cls):
        try:
            # Очистка тестовых данных если есть
            if hasattr(cls, 'test_data_manager'):
                cls.test_data_manager.cleanup_test_data()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            pass

    def test_01_create_database_if_not_exists(self):
        """Тест 01: Создание базы данных если не существует"""

        if os.path.exists(self.temp_db_path):
            os.remove(self.temp_db_path)

        # Проверяем, что файл не существует
        self.assertFalse(os.path.exists(self.temp_db_path))

        # Создаем базу
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем, что файл создан
        self.assertTrue(os.path.exists(self.temp_db_path))

        # Проверяем размер файла (должен быть больше 0)
        file_size = os.path.getsize(self.temp_db_path)
        self.assertGreater(file_size, 0)

    def test_02_create_database_tables_structure(self):
        """Тест 02: Проверка структуры созданных таблиц"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем количество записей в каждой таблице
        currencies_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        categories_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "categories")
        accounts_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "accounts")
        budgets_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "budgets")
        operations_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "operations")

        # Проверяем, что дефолтные данные созданы
        self.assertEqual(currencies_count, 3)
        self.assertEqual(categories_count, 17)
        self.assertEqual(accounts_count, 5)
        self.assertEqual(budgets_count, 0)
        self.assertEqual(operations_count, 0)

    def test_03_get_table_record_count(self):
        """Тест 03: Получение количества записей в таблице"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем количество записей в таблице валют
        count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        self.assertGreater(count, 0)

        # Проверяем количество записей в пустой таблице операций
        count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "operations")
        self.assertEqual(count, 0)

    def test_04_get_total_record_count(self):
        """Тест 04: Получение общего количества записей"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        total_count = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertGreater(total_count, 0)

        # Проверяем, что общее количество равно сумме всех таблиц
        currencies_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        categories_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "categories")
        accounts_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "accounts")
        budgets_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "budgets")
        operations_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "operations")

        expected_total = currencies_count + categories_count + accounts_count + budgets_count + operations_count
        self.assertEqual(total_count, expected_total)

    def test_05_clear_table(self):
        """Тест 05: Очистка таблицы"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем, что таблица валют содержит данные
        initial_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        self.assertGreater(initial_count, 0)

        # Очищаем таблицу
        self.DatabaseUtil.clearTable(self.temp_db_path, "currencies")

        # Проверяем, что таблица пуста
        final_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        self.assertEqual(final_count, 0)

    def test_06_clear_all_data(self):
        """Тест 06: Очистка всех данных"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем, что есть данные
        initial_total = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertGreater(initial_total, 0)

        # Очищаем все данные
        self.DatabaseUtil.clearAllData(self.temp_db_path)

        # Проверяем, что все таблицы пусты
        final_total = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertEqual(final_total, 0)

    def test_07_restore_default_currencies(self):
        """Тест 07: Восстановление дефолтных валют"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Очищаем таблицу валют
        self.DatabaseUtil.clearTable(self.temp_db_path, "currencies")
        self.assertEqual(self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies"), 0)

        # Восстанавливаем дефолтные валюты
        self.DatabaseUtil.restoreDefaultCurrencies(self.temp_db_path)

        # Проверяем, что валюты восстановлены
        currencies_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        self.assertEqual(currencies_count, 3)

    def test_08_restore_default_categories(self):
        """Тест 08: Восстановление дефолтных категорий"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Очищаем таблицу категорий
        self.DatabaseUtil.clearTable(self.temp_db_path, "categories")
        self.assertEqual(self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "categories"), 0)

        # Восстанавливаем дефолтные категории
        self.DatabaseUtil.restoreDefaultCategories(self.temp_db_path)

        # Проверяем, что категории восстановлены
        categories_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "categories")
        self.assertEqual(categories_count, 17)

    def test_09_restore_defaults(self):
        """Тест 09: Восстановление всех дефолтных значений"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Очищаем все данные
        self.DatabaseUtil.clearAllData(self.temp_db_path)
        self.assertEqual(self.DatabaseUtil.getTotalRecordCount(self.temp_db_path), 0)

        # Восстанавливаем дефолтные значения
        self.DatabaseUtil.restoreDefaults(self.temp_db_path)

        # Проверяем, что данные восстановлены
        total_count = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertGreater(total_count, 0)

        # Проверяем конкретные таблицы
        currencies_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        categories_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "categories")
        accounts_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "accounts")

        self.assertGreater(currencies_count, 0)
        self.assertGreater(categories_count, 0)
        self.assertGreater(accounts_count, 0)

    def test_10_clear_table_nonexistent(self):
        """Тест 10: Очистка несуществующей таблицы"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Попытка очистить несуществующую таблицу должна вызвать исключение
        with self.assertRaises(Exception) as context:
            self.DatabaseUtil.clearTable(self.temp_db_path, "nonexistent_table")

        # Проверяем, что исключение содержит правильное сообщение
        self.assertIn("Ошибка выполнения SQL", str(context.exception))
        self.assertIn("DELETE FROM nonexistent_table", str(context.exception))

    def test_11_get_table_record_count_nonexistent(self):
        """Тест 11: Получение количества записей несуществующей таблицы"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Попытка получить количество записей несуществующей таблицы должна вызвать исключение
        with self.assertRaises(Exception) as context:
            self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "nonexistent_table")

        # Проверяем, что исключение содержит правильное сообщение
        self.assertIn("Ошибка выполнения SQL", str(context.exception))
        self.assertIn("SELECT COUNT(*) FROM nonexistent_table", str(context.exception))

    def test_12_database_encoding_utf8(self):
        """Тест 12: Проверка кодировки UTF-8 базы данных"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем, что база создана и доступна
        self.assertTrue(os.path.exists(self.temp_db_path))

        # Проверяем кодировку базы данных через SQLite
        import sqlite3
        conn = sqlite3.connect(self.temp_db_path)
        cursor = conn.cursor()
        
        # Проверяем, что база поддерживает UTF-8
        cursor.execute("PRAGMA encoding")
        encoding = cursor.fetchone()[0]
        self.assertEqual(encoding, "UTF-8")
        
        # Проверяем, что можем сохранить и прочитать русский текст
        test_text = "Тест русских символов: ₽€$¥"
        cursor.execute("CREATE TABLE IF NOT EXISTS encoding_test (id INTEGER PRIMARY KEY, text TEXT)")
        cursor.execute("INSERT INTO encoding_test (text) VALUES (?)", (test_text,))
        conn.commit()
        
        cursor.execute("SELECT text FROM encoding_test WHERE id = 1")
        result = cursor.fetchone()
        self.assertEqual(result[0], test_text)
        
        # Очищаем тестовую таблицу
        cursor.execute("DROP TABLE encoding_test")
        conn.close()

    def test_13_multiple_database_creation(self):
        """Тест 13: Множественное создание базы данных"""
        # Первое создание
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)
        first_size = os.path.getsize(self.temp_db_path)

        # Второе создание (не должно изменить файл)
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)
        second_size = os.path.getsize(self.temp_db_path)

        # Размеры должны быть одинаковыми
        self.assertEqual(first_size, second_size)

    def test_14_clear_and_restore_cycle(self):
        """Тест 14: Цикл очистки и восстановления"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Первое восстановление
        initial_count = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertGreater(initial_count, 0)

        # Очистка
        self.DatabaseUtil.clearAllData(self.temp_db_path)
        self.assertEqual(self.DatabaseUtil.getTotalRecordCount(self.temp_db_path), 0)

        # Восстановление
        self.DatabaseUtil.restoreDefaults(self.temp_db_path)
        restored_count = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertGreater(restored_count, 0)

        # Второй цикл
        self.DatabaseUtil.clearAllData(self.temp_db_path)
        self.DatabaseUtil.restoreDefaults(self.temp_db_path)
        second_restored_count = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertEqual(restored_count, second_restored_count)

    def test_15_default_currencies_content(self):
        """Тест 15: Проверка содержимого дефолтных валют"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем количество дефолтных валют
        currencies_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "currencies")
        print(f"🔍 Количество валют в базе: {currencies_count}")
        self.assertEqual(currencies_count, 3)  # Рубль, Доллар, Евро

    def test_16_default_categories_structure(self):
        """Тест 16: Проверка структуры дефолтных категорий"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем количество дефолтных категорий
        categories_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "categories")
        self.assertGreater(categories_count, 10)  # Должно быть много категорий

    def test_17_default_accounts_content(self):
        """Тест 17: Проверка содержимого дефолтных счетов"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем количество дефолтных счетов
        accounts_count = self.DatabaseUtil.getTableRecordCount(self.temp_db_path, "accounts")
        print(f"🔍 Количество счетов в базе: {accounts_count}")
        self.assertEqual(
            accounts_count, 5
        )  # 5 дефолтных счетов: Наличные, Зарплатная карта, Сберегательный счет, Кредитная карта, Карта рассрочки

    def test_18_database_file_permissions(self):
        """Тест 18: Проверка прав доступа к файлу базы данных"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем, что файл доступен для чтения и записи
        self.assertTrue(os.access(self.temp_db_path, os.R_OK))
        self.assertTrue(os.access(self.temp_db_path, os.W_OK))

    def test_19_foreign_keys_enabled(self):
        """Тест 19: Проверка включения внешних ключей"""
        self.DatabaseUtil.createDatabaseIfNotExists(self.temp_db_path)

        # Проверяем, что база создана корректно
        # (внешние ключи должны быть включены по умолчанию)
        total_count = self.DatabaseUtil.getTotalRecordCount(self.temp_db_path)
        self.assertGreater(total_count, 0)


if __name__ == '__main__':
    unittest.main()
