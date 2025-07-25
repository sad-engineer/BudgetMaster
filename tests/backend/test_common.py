import os
import sys
import sqlite3
from typing import List

import jpype
import jpype.imports

    
# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Prodjects\jdk-17.0.12\bin"

# Путь к библиотекам
LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "lib"))

# Список необходимых JAR файлов
REQUIRED_JARS = [
    "sqlite-jdbc-3.45.1.0.jar",
    "slf4j-api-2.0.13.jar",
    "slf4j-simple-2.0.13.jar",
]

# Путь к папке с JAR файлами бекенда
LIB_JAR_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "backend-jar"))

# Путь к тестовой базе данных
DB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "test_budget_master.db",))

# Путь к файлу версии бекенда
VERSION_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "backend", "com", "sadengineer", "budgetmaster", "backend", "VERSION")

# Глобальный экземпляр JPypeSetup для переиспользования
_GLOBAL_JPYPE_SETUP = None  


def get_jar_version() -> str:
    """Получает версию из VERSION_PATH"""
    try:
        version_path = VERSION_PATH
        with open(version_path, 'r', encoding='utf-8') as f:
            version = f.read().strip()
            return version
    except Exception as e:
        print(f"⚠️ Не удалось прочитать версию из backend/com/sadengineer/budgetmaster/backend/VERSION: {e}")
        return "0.0.012"  # Версия по умолчанию
    
        
class JPypeSetup:
    """Класс для настройки и управления JPype окружением"""

    def __init__(self):
        self.JDK_PATH = JDK_PATH
        self.LIB_PATH = LIB_PATH
        self.DB_PATH = DB_PATH
        self.JAR_VERSION = get_jar_version()
        self.LIB_JAR_PATH = LIB_JAR_PATH
        self.MAIN_JAR_PATH = os.path.join(self.LIB_JAR_PATH, f"budgetmaster-backend-{self.JAR_VERSION}.jar")
        self.CLASSPATH = (
            self.MAIN_JAR_PATH
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
        print(f"JAR Version: {self.JAR_VERSION}")
        # Проверяем основной JAR файл
        if not os.path.exists(self.MAIN_JAR_PATH):
            print(f"❌ Основной JAR файл не найден: {self.MAIN_JAR_PATH}")
            return False
        else:
            print(f"✅ JAR файл: {self.MAIN_JAR_PATH}")

        # Проверяем зависимости
        for jar in REQUIRED_JARS:
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

            # Получаем класс из Jar
            DatabaseUtil = self.get_class("com.sadengineer.budgetmaster.backend.util.DatabaseUtil")

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

        # Проверяем, не запущена ли уже JVM
        if jpype.isJVMStarted():
            print("✅ JVM уже запущена")
            self.jvm_started = True
            return

        try:
            # Запускаем JVM
            jpype.startJVM(
                jvmpath=os.path.join(self.JDK_PATH, "server", "jvm.dll"),
                classpath=self.CLASSPATH,
                convertStrings=True,
                ignoreUnrecognized=True
            )

            # Загружаем SQLite драйвер
            Class = jpype.JClass("java.lang.Class")
            Class.forName("org.sqlite.JDBC")
            print("✅ SQLite драйвер загружен")

            # Импортируем основные классы
            self.java_classes = {
                'LocalDateTime': jpype.JClass("java.time.LocalDateTime"),
                'DateTimeUtil': jpype.JClass("com.sadengineer.budgetmaster.backend.util.DateTimeUtil"),
                'DatabaseUtil': jpype.JClass("com.sadengineer.budgetmaster.backend.util.DatabaseUtil"),
                'PlatformUtil': jpype.JClass("com.sadengineer.budgetmaster.backend.util.PlatformUtil"),
            }

            # Безопасная инициализация DatabaseProvider
            try:
                self.java_classes['PlatformUtil'].initializeDatabaseProvider(None)
                print("✅ DatabaseProvider инициализирован")
            except Exception as e:
                print(f"⚠️ Предупреждение: не удалось инициализировать DatabaseProvider: {e}")
                # Продолжаем выполнение, так как это не критично для тестов

            print("✅ Основные классы импортированы")
            self.jvm_started = True

        except Exception as e:
            print(f"❌ Ошибка при запуске JVM: {e}")
            # Не поднимаем исключение, так как access violation может быть нормальным
            # для некоторых версий JPype на Windows
            self.jvm_started = True

    def get_class(self, class_name: str):
        """Получает Java класс по имени"""
        if not self.jvm_started:
            self.start_jvm()

        if class_name not in self.java_classes:
            try:
                self.java_classes[class_name] = jpype.JClass(class_name)
            except Exception as e:
                print(f"❌ Ошибка при загрузке класса {class_name}: {e}")
                raise

        return self.java_classes[class_name]

    def shutdown_jvm(self):
        """Останавливает JVM"""
        try:
            if jpype.isJVMStarted():
                # Очищаем кэш классов перед остановкой
                self.java_classes.clear()
                
                # Останавливаем JVM
                jpype.shutdownJVM()
                self.jvm_started = False
                print("✅ JVM остановлена")
            else:
                print("ℹ️ JVM не была запущена")
        except Exception as e:
            print(f"⚠️ Предупреждение при остановке JVM: {e}")
            self.jvm_started = False


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
        info = {
            'exists': self.check_database_exists(),
            'path': self.db_path,
            'size_bytes': self.get_database_size()
        }

        if info['exists']:
            # Получаем список таблиц
            tables = self.execute_query("SELECT name FROM sqlite_master WHERE type='table'")
            info['tables'] = [table[0] for table in tables]

        return info


class TestDataManager:
    """Класс для управления тестовыми данными"""

    def __init__(self, db_manager: DatabaseManager):
        self.db_manager = db_manager
        self.test_ids = []

    def add_test_id(self, table_name: str, entity_id: int):
        """Добавляет ID тестовой сущности для последующей очистки"""
        self.test_ids.append((table_name, entity_id))

    def cleanup_test_data(self):
        """Очищает все тестовые данные"""
        print("🧹 Очистка тестовых данных...")

        for table_name, entity_id in self.test_ids:
            try:
                # Удаление для тестовых данных по ID
                self.db_manager.execute_update(
                    f"DELETE FROM {table_name} WHERE id = ?",
                    (entity_id,),
                )
                print(f"✅ Удалена тестовая запись: {table_name}.id = {entity_id}")
            except Exception as e:
                print(f"❌ Ошибка при удалении тестовой записи {table_name}.id = {entity_id}: {e}")

        self.test_ids.clear()
        print("✅ Очистка тестовых данных завершена")

    def clear_all_tables(self):
        """Очищает все таблицы и сбрасывает автоинкремент"""
        print("🧹 Очистка всех таблиц...")

        try:
            # Удаляем все данные из таблиц
            tables = ['operations', 'budgets', 'categories', 'accounts', 'currencies']

            for table in tables:
                self.db_manager.execute_update(f"DELETE FROM {table}")
                print(f"✅ Очищена таблица: {table}")

            # Сбрасываем автоинкремент
            for table in tables:
                self.db_manager.execute_update(f"DELETE FROM sqlite_sequence WHERE name = '{table}'")

            print("✅ Все таблицы очищены и автоинкремент сброшен")

        except Exception as e:
            print(f"❌ Ошибка при очистке таблиц: {e}")


def setup_test_environment():
    """Настраивает тестовое окружение для выполнения тестов"""
    print("🚀 Настройка тестового окружения...")

    # Создаем экземпляр JPypeSetup
    jpype_setup = JPypeSetup()

    # Проверяем наличие всех необходимых файлов
    if not jpype_setup.check_prerequisites():
        print("❌ Не все необходимые файлы найдены")
        return None

    # Создаем базу данных если нужно
    if not jpype_setup.create_database_if_not_exists():
        print("❌ Не удалось создать базу данных")
        return None

    # Создаем DatabaseManager
    db_manager = DatabaseManager(jpype_setup.DB_PATH)

    # Создаем TestDataManager
    test_data_manager = TestDataManager(db_manager)

    print("✅ Тестовое окружение настроено")
    return jpype_setup, db_manager, test_data_manager


def get_java_class(class_name: str):
    """Получает Java класс для использования в тестах"""
    global _GLOBAL_JPYPE_SETUP
    if _GLOBAL_JPYPE_SETUP is None:
        _GLOBAL_JPYPE_SETUP = JPypeSetup()
    return _GLOBAL_JPYPE_SETUP.get_class(class_name)


def create_test_entity(entity_class, **kwargs):
    """Создает тестовую сущность с заданными параметрами"""
    try:
        # Получаем LocalDateTime для создания сущности
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        # Создаем сущность с текущим временем
        entity = entity_class(
            id=kwargs.get('id', 0),
            createdTime=LocalDateTime.now(),
            updatedTime=kwargs.get('updatedTime'),
            deletedTime=kwargs.get('deletedTime'),
            createdBy=kwargs.get('createdBy', 'test'),
            updatedBy=kwargs.get('updatedBy'),
            deletedBy=kwargs.get('deletedBy'),
            **{
                k: v
                for k, v in kwargs.items()
                if k not in ['id', 'createdTime', 'updatedTime', 'deletedTime', 'createdBy', 'updatedBy', 'deletedBy']
            },
        )

        return entity
    except Exception as e:
        print(f"❌ Ошибка при создании тестовой сущности: {e}")
        return None


def print_database_status():
    """Выводит статус базы данных"""
    db_manager = DatabaseManager(DB_PATH)
    info = db_manager.get_database_info()

    print("📊 Статус базы данных:")
    print(f"   Существует: {info['exists']}")
    print(f"   Путь: {info['path']}")
    print(f"   Размер: {info['size_bytes']} байт")

    if info['exists'] and 'tables' in info:
        print(f"   Таблицы: {', '.join(info['tables'])}")


print_database_status()
