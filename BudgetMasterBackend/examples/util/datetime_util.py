import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежат скомпилированные классы
BUILD_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))

# Classpath
CLASSPATH = BUILD_PATH


def main():
    print("=== Тест DateTimeUtil через JPype ===")
    print(f"Classpath: {CLASSPATH}")

    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=CLASSPATH, convertStrings=True)

    try:
        # Импортируем классы
        DateTimeUtil = jpype.JClass("util.DateTimeUtil")
        LocalDateTime = jpype.JClass("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем тестовую дату
        test_date = LocalDateTime.of(2024, 7, 7, 15, 30, 45, 123000000)  # 7 июля 2024, 15:30:45.123
        print(f"Тестовая дата: {test_date}")

        # Тестируем форматирование для SQLite
        print("\n--- Тест форматирования для SQLite ---")
        formatted_date = DateTimeUtil.formatForSqlite(test_date)
        print(f"Отформатированная дата: '{formatted_date}'")

        # Тестируем парсинг из SQLite
        print("\n--- Тест парсинга из SQLite ---")
        parsed_date = DateTimeUtil.parseFromSqlite(formatted_date)
        print(f"Распарсенная дата: {parsed_date}")

        # Проверяем, что даты равны
        print(f"Даты равны: {test_date.equals(parsed_date)}")

        # Тестируем с null значениями
        print("\n--- Тест с null значениями ---")
        null_formatted = DateTimeUtil.formatForSqlite(None)
        print(f"Форматирование null: '{null_formatted}'")

        null_parsed = DateTimeUtil.parseFromSqlite(None)
        print(f"Парсинг null: {null_parsed}")

        # Тестируем с пустой строкой
        empty_parsed = DateTimeUtil.parseFromSqlite("")
        print(f"Парсинг пустой строки: {empty_parsed}")

        # Тестируем с текущим временем
        print("\n--- Тест с текущим временем ---")
        now = LocalDateTime.now()
        now_formatted = DateTimeUtil.formatForSqlite(now)
        print(f"Текущее время: {now}")
        print(f"Отформатированное: '{now_formatted}'")

        now_parsed = DateTimeUtil.parseFromSqlite(now_formatted)
        print(f"Распарсенное: {now_parsed}")
        print(f"Равны: {now.equals(now_parsed)}")

        print("\n✅ Все тесты выполнены успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Останавливаем JVM
        if jpype.isJVMStarted():
            jpype.shutdownJVM()
            print("JVM остановлена")


if __name__ == "__main__":
    main()
