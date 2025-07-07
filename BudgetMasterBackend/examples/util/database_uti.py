import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежат скомпилированные классы
BUILD_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))

# Путь к библиотекам (SQLite драйвер)
LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "lib"))

# Classpath с библиотеками
CLASSPATH = (
    BUILD_PATH
    + os.pathsep
    + os.path.join(LIB_PATH, "sqlite-jdbc-3.45.1.0.jar")
    + os.pathsep
    + os.path.join(LIB_PATH, "slf4j-api-2.0.13.jar")
    + os.pathsep
    + os.path.join(LIB_PATH, "slf4j-simple-2.0.13.jar")
)


def main():
    print("=== Тест DatabaseUtil через JPype ===")
    print(f"Classpath: {CLASSPATH}")

    # Путь к тестовой базе данных
    test_db_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "budget_master.db"))
    print(f"Тестовая БД: {test_db_path}")

    # Проверяем наличие JAR файлов
    required_jars = ["sqlite-jdbc-3.45.1.0.jar", "slf4j-api-2.0.13.jar", "slf4j-simple-2.0.13.jar"]

    for jar in required_jars:
        jar_path = os.path.join(LIB_PATH, jar)
        if not os.path.exists(jar_path):
            print(f"❌ JAR файл не найден: {jar_path}")
            return

    print("✅ Все необходимые JAR файлы найдены")

    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=CLASSPATH, convertStrings=True)

    try:
        # Загружаем SQLite драйвер
        Class = jpype.JClass("java.lang.Class")
        Class.forName("org.sqlite.JDBC")
        print("✅ SQLite драйвер загружен")

        # Импортируем классы
        DatabaseUtil = jpype.JClass("util.DatabaseUtil")

        print("✅ Классы импортированы")

        # Удаляем старую тестовую БД если существует
        if os.path.exists(test_db_path):
            os.remove(test_db_path)
            print("🗑️ Удалена старая тестовая БД")

        # Создаем новую базу данных
        print("\n--- Создание базы данных ---")
        DatabaseUtil.createDatabaseIfNotExists(test_db_path)
        print("✅ База данных создана успешно")

        # Проверяем, что файл БД создался
        if os.path.exists(test_db_path):
            file_size = os.path.getsize(test_db_path)
            print(f"✅ Файл БД создан, размер: {file_size} байт")
        else:
            print("❌ Файл БД не создан")
            return

        # Тестируем повторное создание (не должно вызвать ошибку)
        print("\n--- Тест повторного создания ---")
        DatabaseUtil.createDatabaseIfNotExists(test_db_path)
        print("✅ Повторное создание выполнено без ошибок")

        # Проверяем структуру БД через SQLite
        print("\n--- Проверка структуры БД ---")
        try:
            import sqlite3

            conn = sqlite3.connect(test_db_path)
            cursor = conn.cursor()

            # Получаем список таблиц
            cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
            tables = cursor.fetchall()
            print(f"Найдено таблиц: {len(tables)}")

            for table in tables:
                table_name = table[0]
                print(f"📋 Таблица: {table_name}")

                # Получаем структуру таблицы
                cursor.execute(f"PRAGMA table_info({table_name})")
                columns = cursor.fetchall()
                for col in columns:
                    col_id, col_name, col_type, not_null, default_val, pk = col
                    print(f"  - {col_name}: {col_type}" + (" (PK)" if pk else ""))
                print()

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при проверке структуры БД: {e}")

        print("✅ Все тесты выполнены успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Останавливаем JVM
        if jpype.isJVMStarted():
            jpype.shutdownJVM()
            print("JVM остановлена")

        # # Удаляем тестовую БД
        # if os.path.exists(test_db_path):
        #     os.remove(test_db_path)
        #     print("🗑️ Тестовая БД удалена")


if __name__ == "__main__":
    main()
