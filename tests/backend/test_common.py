import os
import sqlite3
from typing import List

import jpype
import jpype.imports


class JPypeSetup:
    """Класс для настройки и управления JPype окружением"""

    def __init__(self):
        # Путь к JDK (где лежит jvm.dll)
        self.JDK_PATH = r"C:\Users\Korenyk.A\Documents\Prodjects\jdk-17.0.12\bin"

        # Путь к библиотекам (включая наш JAR файл)
        self.LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "lib"))

        # Путь к базе данных
        self.DB_PATH = os.path.abspath(
            os.path.join(os.path.dirname(__file__), "..", "..", "backend", "com", "sadengineer", "budgetmaster",
                         "backend", "budget_master.db")
        )

        # Получаем версию из pyproject.toml
        self.JAR_VERSION = self._get_jar_version()

        # Classpath с библиотеками и нашим JAR файлом
        backend_jar_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "backend-jar"))
        self.CLASSPATH = (
            os.path.join(backend_jar_path, f"budgetmaster-backend-{self.JAR_VERSION}.jar")
            + os.pathsep
            + os.path.join(self.LIB_PATH, "sqlite-jdbc-3.45.1.0.jar")
            + os.pathsep
            + os.path.join(self.LIB_PATH, "slf4j-api-2.0.13.jar")
            + os.pathsep
            + os.path.join(self.LIB_PATH, "slf4j-simple-2.0.13.jar")
        )

        self.jvm_started = False
        self.java_classes = {}

    def _get_jar_version(self) -> str:
        """Получает версию из backend/com/sadengineer/budgetmaster/backend/VERSION"""
        try:
            version_path = os.path.join(os.path.dirname(__file__), "..", "..", "backend", "com", "sadengineer", 
                                        "budgetmaster", "backend", "VERSION")
            with open(version_path, 'r', encoding='utf-8') as f:
                version = f.read().strip()
                return version
        except Exception as e:
            print(f"⚠️ Не удалось прочитать версию из backend/com/sadengineer/budgetmaster/backend/VERSION: {e}")
            return "0.0.012"  # Версия по умолчанию

    def check_prerequisites(self) -> bool:
        """Проверяет наличие всех необходимых файлов"""
        print(f"Classpath: {self.CLASSPATH}")
        print(f"Database: {self.DB_PATH}")
        print(f"JAR Version: {self.JAR_VERSION}")

        # Проверяем наличие всех необходимых JAR файлов
        backend_jar_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "backend-jar"))
        
        # Проверяем основной JAR файл
        main_jar = os.path.join(backend_jar_path, f"budgetmaster-backend-{self.JAR_VERSION}.jar")
        if not os.path.exists(main_jar):
            print(f"❌ Основной JAR файл не найден: {main_jar}")
            return False
            
        # Проверяем зависимости
        required_jars = [
            "sqlite-jdbc-3.45.1.0.jar",
            "slf4j-api-2.0.13.jar",
            "slf4j-simple-2.0.13.jar",
        ]

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
                'DateTimeUtil': jpype.JClass("com.sadengineer.budgetmaster.backend.util.DateTimeUtil"),
                'DatabaseUtil': jpype.JClass("com.sadengineer.budgetmaster.backend.util.DatabaseUtil"),
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
                # Мягкое удаление (soft delete)
                self.db_manager.execute_update(
                    f"UPDATE {table_name} SET deleted_time = datetime('now'), deleted_by = 'test' WHERE id = ?",
                    (entity_id,),
                )
                print(f"✅ Удалена тестовая запись: {table_name}.id = {entity_id}")
            except Exception as e:
                print(f"❌ Ошибка при удалении тестовой записи {table_name}.id = {entity_id}: {e}")

        self.test_ids.clear()
        print("✅ Очистка тестовых данных завершена")

    def reset_database_to_defaults(self):
        """Сбрасывает базу данных к состоянию по умолчанию"""
        print("🔄 Сброс базы данных к состоянию по умолчанию...")

        try:
            # Удаляем все данные из таблиц
            tables = ['operations', 'budgets', 'categories', 'accounts', 'currencies']

            for table in tables:
                self.db_manager.execute_update(f"DELETE FROM {table}")
                print(f"✅ Очищена таблица: {table}")

            # Сбрасываем автоинкремент
            for table in tables:
                self.db_manager.execute_update(f"DELETE FROM sqlite_sequence WHERE name = '{table}'")

            print("✅ База данных сброшена к состоянию по умолчанию")

        except Exception as e:
            print(f"❌ Ошибка при сбросе базы данных: {e}")


def setup_example():
    """Пример настройки тестового окружения"""
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


def cleanup_example():
    """Пример очистки тестового окружения"""
    print("🧹 Очистка тестового окружения...")
    # Здесь можно добавить дополнительную очистку если нужно
    print("✅ Тестовое окружение очищено")


# Глобальный экземпляр JPypeSetup для переиспользования
_global_jpype_setup = None


def get_java_class(class_name: str):
    """Получает Java класс для использования в тестах"""
    global _global_jpype_setup
    if _global_jpype_setup is None:
        _global_jpype_setup = JPypeSetup()
    return _global_jpype_setup.get_class(class_name)


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
    db_manager = DatabaseManager(
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "backend", "budget_master.db"))
    )
    info = db_manager.get_database_info()

    print("📊 Статус базы данных:")
    print(f"   Существует: {info['exists']}")
    print(f"   Путь: {info['path']}")
    print(f"   Размер: {info['size_bytes']} байт")

    if info['exists'] and 'tables' in info:
        print(f"   Таблицы: {', '.join(info['tables'])}")
