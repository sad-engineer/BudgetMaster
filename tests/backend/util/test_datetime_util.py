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


class TestDateTimeUtil(unittest.TestCase):
    """Юнит-тесты для DateTimeUtil"""

    @classmethod
    def setUpClass(cls):
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.DateTimeUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.DateTimeUtil")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")

    @classmethod
    def tearDownClass(cls):
        try:
            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            pass

    def test_01_format_for_sqlite_valid_datetime(self):
        """Тест 01: Форматирование валидной даты для SQLite"""
        # Создаем LocalDateTime
        java_datetime = self.LocalDateTime.of(2024, 1, 15, 14, 30, 45, 123000000)

        # Форматируем
        formatted = self.DateTimeUtil.formatForSqlite(java_datetime)

        # Проверяем формат
        self.assertEqual(formatted, "2024-01-15 14:30:45.123")

    def test_02_format_for_sqlite_null_datetime(self):
        """Тест 02: Форматирование null даты для SQLite"""
        # Форматируем null
        formatted = self.DateTimeUtil.formatForSqlite(None)

        # Проверяем, что возвращается null
        self.assertIsNone(formatted)

    def test_03_format_for_sqlite_edge_cases(self):
        """Тест 03: Форматирование граничных случаев"""
        # Начало года
        start_of_year = self.LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0)
        formatted = self.DateTimeUtil.formatForSqlite(start_of_year)
        self.assertEqual(formatted, "2024-01-01 00:00:00.000")

        # Конец года
        end_of_year = self.LocalDateTime.of(2024, 12, 31, 23, 59, 59, 999000000)
        formatted = self.DateTimeUtil.formatForSqlite(end_of_year)
        self.assertEqual(formatted, "2024-12-31 23:59:59.999")

        # Високосный год
        leap_year = self.LocalDateTime.of(2024, 2, 29, 12, 0, 0, 0)
        formatted = self.DateTimeUtil.formatForSqlite(leap_year)
        self.assertEqual(formatted, "2024-02-29 12:00:00.000")

    def test_04_parse_from_sqlite_valid_string(self):
        """Тест 04: Парсинг валидной строки из SQLite"""
        # Строка в формате SQLite
        sqlite_string = "2024-01-15 14:30:45.123"

        # Парсим
        parsed = self.DateTimeUtil.parseFromSqlite(sqlite_string)

        # Проверяем результат
        self.assertIsNotNone(parsed)
        self.assertEqual(parsed.getYear(), 2024)
        self.assertEqual(parsed.getMonthValue(), 1)
        self.assertEqual(parsed.getDayOfMonth(), 15)
        self.assertEqual(parsed.getHour(), 14)
        self.assertEqual(parsed.getMinute(), 30)
        self.assertEqual(parsed.getSecond(), 45)
        self.assertEqual(parsed.getNano(), 123000000)

    def test_05_parse_from_sqlite_null_string(self):
        """Тест 05: Парсинг null строки из SQLite"""
        # Парсим null
        parsed = self.DateTimeUtil.parseFromSqlite(None)

        # Проверяем, что возвращается null
        self.assertIsNone(parsed)

    def test_06_parse_from_sqlite_empty_string(self):
        """Тест 06: Парсинг пустой строки из SQLite"""
        # Парсим пустую строку
        parsed = self.DateTimeUtil.parseFromSqlite("")

        # Проверяем, что возвращается null
        self.assertIsNone(parsed)

    def test_07_parse_from_sqlite_whitespace_string(self):
        """Тест 07: Парсинг строки с пробелами из SQLite"""
        # Парсим строку с пробелами
        parsed = self.DateTimeUtil.parseFromSqlite("   ")

        # Проверяем, что возвращается null
        self.assertIsNone(parsed)

    def test_08_round_trip_format_parse(self):
        """Тест 08: Цикл форматирование-парсинг"""
        # Создаем LocalDateTime
        original = self.LocalDateTime.of(2024, 6, 15, 12, 34, 56, 789000000)

        # Форматируем
        formatted = self.DateTimeUtil.formatForSqlite(original)

        # Парсим обратно
        parsed = self.DateTimeUtil.parseFromSqlite(formatted)

        # Проверяем, что результат совпадает с исходным
        self.assertEqual(parsed.getYear(), original.getYear())
        self.assertEqual(parsed.getMonthValue(), original.getMonthValue())
        self.assertEqual(parsed.getDayOfMonth(), original.getDayOfMonth())
        self.assertEqual(parsed.getHour(), original.getHour())
        self.assertEqual(parsed.getMinute(), original.getMinute())
        self.assertEqual(parsed.getSecond(), original.getSecond())
        self.assertEqual(parsed.getNano(), original.getNano())

    def test_09_parse_from_sqlite_edge_cases(self):
        """Тест 09: Парсинг граничных случаев"""
        # Начало года
        start_string = "2024-01-01 00:00:00.000"
        parsed = self.DateTimeUtil.parseFromSqlite(start_string)
        self.assertEqual(parsed.getYear(), 2024)
        self.assertEqual(parsed.getMonthValue(), 1)
        self.assertEqual(parsed.getDayOfMonth(), 1)
        self.assertEqual(parsed.getHour(), 0)
        self.assertEqual(parsed.getMinute(), 0)
        self.assertEqual(parsed.getSecond(), 0)
        self.assertEqual(parsed.getNano(), 0)

        # Конец года
        end_string = "2024-12-31 23:59:59.999"
        parsed = self.DateTimeUtil.parseFromSqlite(end_string)
        self.assertEqual(parsed.getYear(), 2024)
        self.assertEqual(parsed.getMonthValue(), 12)
        self.assertEqual(parsed.getDayOfMonth(), 31)
        self.assertEqual(parsed.getHour(), 23)
        self.assertEqual(parsed.getMinute(), 59)
        self.assertEqual(parsed.getSecond(), 59)
        self.assertEqual(parsed.getNano(), 999000000)

    def test_10_parse_from_sqlite_invalid_format(self):
        """Тест 10: Парсинг неверного формата"""
        # Неверный формат даты
        invalid_string = "2024-13-45 25:70:80.999"

        # Парсинг должен вызвать исключение
        with self.assertRaises(Exception):
            self.DateTimeUtil.parseFromSqlite(invalid_string)

    def test_11_parse_from_sqlite_malformed_string(self):
        """Тест 11: Парсинг некорректной строки"""
        # Некорректная строка
        malformed_string = "not a date"

        # Парсинг должен вызвать исключение
        with self.assertRaises(Exception):
            self.DateTimeUtil.parseFromSqlite(malformed_string)

    def test_12_format_for_sqlite_current_datetime(self):
        """Тест 12: Форматирование текущей даты"""
        # Создаем текущую дату
        now = self.LocalDateTime.now()

        # Форматируем
        formatted = self.DateTimeUtil.formatForSqlite(now)

        # Проверяем, что строка не пустая и имеет правильный формат
        self.assertIsNotNone(formatted)
        self.assertGreater(len(formatted), 0)
        self.assertIn("202", formatted)  # Должен содержать год
        self.assertIn(":", formatted)  # Должен содержать двоеточия для времени

    def test_13_parse_from_sqlite_without_milliseconds(self):
        """Тест 13: Парсинг строки без миллисекунд"""
        # Строка без миллисекунд
        string_without_ms = "2024-01-15 14:30:45"

        # Парсинг должен вызвать исключение (ожидается формат с миллисекундами)
        with self.assertRaises(Exception):
            self.DateTimeUtil.parseFromSqlite(string_without_ms)

    def test_14_parse_from_sqlite_with_extra_spaces(self):
        """Тест 14: Парсинг строки с лишними пробелами"""
        # Строка с лишними пробелами
        string_with_spaces = "  2024-01-15 14:30:45.123  "

        # Парсинг должен вызвать исключение
        with self.assertRaises(Exception):
            self.DateTimeUtil.parseFromSqlite(string_with_spaces)

    def test_15_format_for_sqlite_zero_nanoseconds(self):
        """Тест 15: Форматирование даты с нулевыми наносекундами"""
        # Создаем LocalDateTime с нулевыми наносекундами
        java_datetime = self.LocalDateTime.of(2024, 1, 15, 14, 30, 45, 0)

        # Форматируем
        formatted = self.DateTimeUtil.formatForSqlite(java_datetime)

        # Проверяем формат
        self.assertEqual(formatted, "2024-01-15 14:30:45.000")

    def test_16_parse_from_sqlite_zero_milliseconds(self):
        """Тест 16: Парсинг строки с нулевыми миллисекундами"""
        # Строка с нулевыми миллисекундами
        string_with_zero_ms = "2024-01-15 14:30:45.000"

        # Парсим
        parsed = self.DateTimeUtil.parseFromSqlite(string_with_zero_ms)

        # Проверяем результат
        self.assertIsNotNone(parsed)
        self.assertEqual(parsed.getNano(), 0)

    def test_17_format_for_sqlite_max_nanoseconds(self):
        """Тест 17: Форматирование даты с максимальными наносекундами"""
        # Создаем LocalDateTime с максимальными наносекундами
        java_datetime = self.LocalDateTime.of(2024, 1, 15, 14, 30, 45, 999999999)

        # Форматируем
        formatted = self.DateTimeUtil.formatForSqlite(java_datetime)

        # Проверяем формат
        self.assertEqual(formatted, "2024-01-15 14:30:45.999")

    def test_18_parse_from_sqlite_max_milliseconds(self):
        """Тест 18: Парсинг строки с максимальными миллисекундами"""
        # Строка с максимальными миллисекундами
        string_with_max_ms = "2024-01-15 14:30:45.999"

        # Парсим
        parsed = self.DateTimeUtil.parseFromSqlite(string_with_max_ms)

        # Проверяем результат
        self.assertIsNotNone(parsed)
        self.assertEqual(parsed.getNano(), 999000000)

    def test_19_multiple_round_trips(self):
        """Тест 19: Множественные циклы форматирование-парсинг"""
        # Создаем несколько разных дат
        test_dates = [
            self.LocalDateTime.of(2024, 1, 1, 0, 0, 0, 0),
            self.LocalDateTime.of(2024, 6, 15, 12, 30, 45, 123000000),
            self.LocalDateTime.of(2024, 12, 31, 23, 59, 59, 999000000),
            self.LocalDateTime.of(2020, 2, 29, 12, 0, 0, 0),  # Високосный год
        ]

        for original in test_dates:
            # Форматируем
            formatted = self.DateTimeUtil.formatForSqlite(original)

            # Парсим обратно
            parsed = self.DateTimeUtil.parseFromSqlite(formatted)

            # Проверяем, что результат совпадает с исходным
            self.assertEqual(parsed.getYear(), original.getYear())
            self.assertEqual(parsed.getMonthValue(), original.getMonthValue())
            self.assertEqual(parsed.getDayOfMonth(), original.getDayOfMonth())
            self.assertEqual(parsed.getHour(), original.getHour())
            self.assertEqual(parsed.getMinute(), original.getMinute())
            self.assertEqual(parsed.getSecond(), original.getSecond())
            self.assertEqual(parsed.getNano(), original.getNano())

    def test_20_format_for_sqlite_consistency(self):
        """Тест 20: Консистентность форматирования"""
        # Создаем LocalDateTime
        java_datetime = self.LocalDateTime.of(2024, 1, 15, 14, 30, 45, 123000000)

        # Форматируем несколько раз
        formatted1 = self.DateTimeUtil.formatForSqlite(java_datetime)
        formatted2 = self.DateTimeUtil.formatForSqlite(java_datetime)
        formatted3 = self.DateTimeUtil.formatForSqlite(java_datetime)

        # Все результаты должны быть одинаковыми
        self.assertEqual(formatted1, formatted2)
        self.assertEqual(formatted2, formatted3)
        self.assertEqual(formatted1, formatted3)


if __name__ == '__main__':
    unittest.main()
