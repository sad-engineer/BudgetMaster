import os
import sqlite3
from typing import List, Optional

import jpype
import jpype.imports


class JPypeSetup:
    """Класс для настройки и управления JPype окружением"""

    def __init__(self):
        # Путь к JDK (где лежит jvm.dll)
        self.JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

        # Путь к build, где лежат скомпилированные классы
        self.BUILD_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "build"))

        # Путь к библиотекам (SQLite драйвер)
        self.LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "lib"))

        # Путь к базе данных
        self.DB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "budget_master.db"))

        # Classpath с библиотеками
        self.CLASSPATH = (
            self.BUILD_PATH
            + os.pathsep
            + os.path.join(self.LIB_PATH, "sqlite-jdbc-3.45.1.0.jar")
            + os.pathsep
            + os.path.join(self.LIB_PATH, "slf4j-api-2.0.13.jar")
            + os.pathsep
            + os.path.join(self.LIB_PATH, "slf4j-simple-2.0.13.jar")
        )

        self.jvm_started = False
        self.java_classes = {}

    def check_prerequisites(self) -> bool:
        """Проверяет наличие всех необходимых файлов"""
        print(f"Classpath: {self.CLASSPATH}")
        print(f"Database: {self.DB_PATH}")

        # Проверяем наличие всех необходимых JAR файлов
        required_jars = ["sqlite-jdbc-3.45.1.0.jar", "slf4j-api-2.0.13.jar", "slf4j-simple-2.0.13.jar"]

        for jar in required_jars:
            jar_path = os.path.join(self.LIB_PATH, jar)
            if not os.path.exists(jar_path):
                print(f"❌ JAR файл не найден: {jar_path}")
                return False

        print("✅ Все необходимые файлы найдены")
        return True

    def create_database_if_not_exists(self) -> bool:
        """Создает базу данных если она не существует"""
        if os.path.exists(self.DB_PATH):
            print(f"✅ База данных уже существует: {self.DB_PATH}")
            return True

        print(f"📁 База данных не найдена: {self.DB_PATH}")
        print("🔧 Создаем новую базу данных...")

        try:
            # Запускаем JVM если еще не запущена
            if not self.jvm_started:
                self.start_jvm()

            # Получаем DatabaseUtil класс
            DatabaseUtil = self.get_class("util.DatabaseUtil")

            # Создаем базу данных
            DatabaseUtil.createDatabaseIfNotExists(self.DB_PATH)

            print(f"✅ База данных успешно создана: {self.DB_PATH}")
            return True

        except Exception as e:
            print(f"❌ Ошибка при создании базы данных: {e}")
            return False

    def start_jvm(self):
        """Запускает JVM и загружает необходимые классы"""
        if self.jvm_started:
            return

        # Запуск JVM
        jpype.startJVM(
            jvmpath=os.path.join(self.JDK_PATH, "server", "jvm.dll"), classpath=self.CLASSPATH, convertStrings=True
        )

        try:
            # Загружаем SQLite драйвер
            Class = jpype.JClass("java.lang.Class")
            Class.forName("org.sqlite.JDBC")
            print("✅ SQLite драйвер загружен")

            # Импортируем основные классы
            self.java_classes = {
                'LocalDateTime': jpype.JClass("java.time.LocalDateTime"),
                'DateTimeUtil': jpype.JClass("util.DateTimeUtil"),
                'DatabaseUtil': jpype.JClass("util.DatabaseUtil"),
            }

            print("✅ Основные классы импортированы")
            self.jvm_started = True

        except Exception as e:
            print(f"❌ Ошибка при запуске JVM: {e}")
            raise

    def get_class(self, class_name: str):
        """Получает Java класс по имени"""
        if not self.jvm_started:
            self.start_jvm()

        if class_name not in self.java_classes:
            self.java_classes[class_name] = jpype.JClass(class_name)

        return self.java_classes[class_name]

    def shutdown_jvm(self):
        """Останавливает JVM"""
        if jpype.isJVMStarted():
            jpype.shutdownJVM()
            self.jvm_started = False
            print("JVM остановлена")


class DatabaseManager:
    """Класс для работы с базой данных"""

    def __init__(self, db_path: str):
        self.db_path = db_path

    def execute_query(self, query: str, params: tuple = None) -> List[tuple]:
        """Выполняет SQL запрос и возвращает результаты"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()

            if params:
                cursor.execute(query, params)
            else:
                cursor.execute(query)

            results = cursor.fetchall()
            conn.close()
            return results

        except Exception as e:
            print(f"❌ Ошибка при выполнении SQL запроса: {e}")
            return []

    def execute_update(self, query: str, params: tuple = None) -> bool:
        """Выполняет SQL запрос на обновление"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()

            if params:
                cursor.execute(query, params)
            else:
                cursor.execute(query)

            conn.commit()
            conn.close()
            return True

        except Exception as e:
            print(f"❌ Ошибка при выполнении SQL обновления: {e}")
            return False

    def get_table_info(self, table_name: str) -> List[tuple]:
        """Получает информацию о структуре таблицы"""
        return self.execute_query(f"PRAGMA table_info({table_name})")

    def get_table_columns(self, table_name: str) -> List[str]:
        """Получает список колонок таблицы"""
        table_info = self.get_table_info(table_name)
        return [col[1] for col in table_info]

    def check_database_exists(self) -> bool:
        """Проверяет существование базы данных"""
        return os.path.exists(self.db_path)

    def get_database_size(self) -> int:
        """Получает размер базы данных в байтах"""
        if os.path.exists(self.db_path):
            return os.path.getsize(self.db_path)
        return 0

    def get_database_info(self) -> dict:
        """Получает информацию о базе данных"""
        info = {'exists': self.check_database_exists(), 'path': self.db_path, 'size_bytes': self.get_database_size()}

        if info['exists']:
            # Получаем список таблиц
            tables = self.execute_query("SELECT name FROM sqlite_master WHERE type='table'")
            info['tables'] = [table[0] for table in tables]

            # Получаем количество записей в каждой таблице
            info['table_counts'] = {}
            for table in info['tables']:
                count_result = self.execute_query(f"SELECT COUNT(*) FROM {table}")
                if count_result:
                    info['table_counts'][table] = count_result[0][0]

        return info


class TestDataManager:
    """Класс для управления тестовыми данными"""

    def __init__(self, db_manager: DatabaseManager):
        self.db_manager = db_manager
        self.test_ids = {}  # {table_name: [id1, id2, ...]}

    def add_test_id(self, table_name: str, entity_id: int):
        """Добавляет ID тестовой записи"""
        if table_name not in self.test_ids:
            self.test_ids[table_name] = []
        self.test_ids[table_name].append(entity_id)

    def cleanup_test_data(self):
        """Физически удаляет все тестовые данные"""
        print("\n--- Очистка тестовых данных ---")

        for table_name, ids in self.test_ids.items():
            if not ids:
                continue

            print(f"Удаляем {len(ids)} тестовых записей из таблицы {table_name}: {ids}")

            for entity_id in ids:
                # Проверяем, что запись существует
                result = self.db_manager.execute_query(f"SELECT * FROM {table_name} WHERE id = ?", (entity_id,))

                if result:
                    print(f"Физически удаляем запись: ID={entity_id}")
                    self.db_manager.execute_update(f"DELETE FROM {table_name} WHERE id = ?", (entity_id,))
                else:
                    print(f"Запись с ID={entity_id} не найдена в таблице {table_name}")

        # Очищаем список тестовых ID
        self.test_ids.clear()
        print("✅ Очистка тестовых данных завершена")

    def reset_database_to_defaults(self):
        """Сбрасывает базу данных к дефолтным значениям"""
        print("\n--- Сброс базы данных к дефолтам ---")

        try:
            # Получаем DatabaseUtil класс
            DatabaseUtil = jpype_setup.get_class("util.DatabaseUtil")

            # Восстанавливаем дефолтные значения
            DatabaseUtil.restoreDefaults(jpype_setup.DB_PATH)

            print("✅ База данных сброшена к дефолтным значениям")

        except Exception as e:
            print(f"❌ Ошибка при сбросе базы данных: {e}")


# Глобальные экземпляры для использования в тестах
jpype_setup = JPypeSetup()
db_manager = DatabaseManager(jpype_setup.DB_PATH)
test_data_manager = TestDataManager(db_manager)


def setup_example():
    """Настройка окружения для тестов"""
    if not jpype_setup.check_prerequisites():
        return False

    try:
        # Создаем базу данных если она не существует
        if not jpype_setup.create_database_if_not_exists():
            return False

        jpype_setup.start_jvm()

        # Выводим информацию о базе данных
        db_info = db_manager.get_database_info()
        print(f"\n📊 Информация о базе данных:")
        print(f"   Путь: {db_info['path']}")
        print(f"   Размер: {db_info['size_bytes']} байт")
        print(f"   Таблицы: {', '.join(db_info.get('tables', []))}")

        if 'table_counts' in db_info:
            print(f"   Записей в таблицах:")
            for table, count in db_info['table_counts'].items():
                print(f"     {table}: {count}")

        return True

    except Exception as e:
        print(f"❌ Ошибка при настройке: {e}")
        return False


def cleanup_example():
    """Очистка после выполнения тестов"""
    test_data_manager.cleanup_test_data()
    jpype_setup.shutdown_jvm()


def get_java_class(class_name: str):
    """Получает Java класс для использования в тестах"""
    return jpype_setup.get_class(class_name)


def create_test_entity(entity_class, **kwargs):
    """Создает тестовую сущность с базовыми полями"""
    entity = entity_class()

    # Устанавливаем базовые поля
    entity.setCreatedBy("test_user")
    entity.setUpdatedBy("test_user")

    # Устанавливаем даты
    LocalDateTime = get_java_class("java.time.LocalDateTime")
    now = LocalDateTime.now()
    entity.setCreateTime(now)
    entity.setUpdateTime(now)
    entity.setDeleteTime(None)

    # Устанавливаем переданные поля
    for key, value in kwargs.items():
        method_name = f"set{key.capitalize()}"
        if hasattr(entity, method_name):
            getattr(entity, method_name)(value)

    return entity


def print_database_status():
    """Выводит текущий статус базы данных"""
    db_info = db_manager.get_database_info()

    print(f"\n📊 Статус базы данных:")
    print(f"   Существует: {'✅' if db_info['exists'] else '❌'}")
    print(f"   Путь: {db_info['path']}")

    if db_info['exists']:
        print(f"   Размер: {db_info['size_bytes']} байт")
        print(f"   Таблицы: {', '.join(db_info.get('tables', []))}")

        if 'table_counts' in db_info:
            print(f"   Записей:")
            for table, count in db_info['table_counts'].items():
                print(f"     {table}: {count}")
    else:
        print("   База данных не существует")
