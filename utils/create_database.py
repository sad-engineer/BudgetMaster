#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для создания базы данных budget_master.db с дефолтными данными
"""

import os
import sys
from pathlib import Path

import jpype
import jpype.imports


def setup_jpype():
    """Настраивает JPype для работы с Java классами через JAR"""

    # Пути к файлам
    current_dir = Path(__file__).parent
    project_root = current_dir.parent
    lib_path = project_root / "lib"
    backend_jar_path = project_root / "backend-jar"
    backend_path = project_root / "backend" / "com" / "sadengineer" / "budgetmaster" / "backend"

    # Получаем версию backend из файла VERSION
    version_file = backend_path / "VERSION"
    if not version_file.exists():
        print(f"❌ Не найден файл версии: {version_file}")
        return False
    with open(version_file, "r", encoding="utf-8") as f:
        backend_version = f.read().strip()
    jar_path = backend_jar_path / f"budgetmaster-backend-{backend_version}.jar"

    # Classpath с библиотеками
    classpath = (
        str(jar_path)
        + os.pathsep
        + str(lib_path / "sqlite-jdbc-3.45.1.0.jar")
        + os.pathsep
        + str(lib_path / "slf4j-api-2.0.13.jar")
        + os.pathsep
        + str(lib_path / "slf4j-simple-2.0.13.jar")
    )

    print(f"🔧 Настройка JPype...")
    print(f"   JAR path: {jar_path}")
    print(f"   Lib path: {lib_path}")
    print(f"   Database path: {backend_path / 'budget_master.db'}")
    print(f"   Classpath: {classpath}")

    # Проверяем наличие необходимых файлов
    required_files = [
        jar_path,
        lib_path / "sqlite-jdbc-3.45.1.0.jar",
        lib_path / "slf4j-api-2.0.13.jar",
        lib_path / "slf4j-simple-2.0.13.jar",
    ]

    for file_path in required_files:
        if not file_path.exists():
            print(f"❌ Файл не найден: {file_path}")
            return False

    try:
        # Запускаем JVM
        jdk_path = r"C:\Users\Korenyk.A\Documents\Prodjects\jdk-17.0.12\bin"
        jpype.startJVM(jvmpath=os.path.join(jdk_path, "server", "jvm.dll"), classpath=classpath, convertStrings=True)

        # Загружаем SQLite драйвер
        Class = jpype.JClass("java.lang.Class")
        Class.forName("org.sqlite.JDBC")
        print("✅ SQLite драйвер загружен")

        return True

    except Exception as e:
        print(f"❌ Ошибка при настройке JPype: {e}")
        return False


def create_database():
    """Создает базу данных с дефолтными данными"""

    try:
        # Получаем Java классы
        DatabaseUtil = jpype.JClass("com.sadengineer.budgetmaster.backend.util.DatabaseUtil")

        # Путь к базе данных
        current_dir = Path(__file__).parent
        project_root = current_dir.parent
        db_path = project_root / "backend" / "com" / "sadengineer" / "budgetmaster" / "backend" / "budget_master.db"

        print(f"🗄️  Создание базы данных: {db_path}")

        # Проверяем, существует ли уже база данных
        if os.path.exists(db_path):
            print("⚠️  База данных уже существует")
            response = input("Перезаписать существующую базу данных? (y/N): ")

            if response.lower() not in ['y', 'yes', 'да', 'д']:
                print("❌ Операция отменена")
                return False

            # Удаляем существующий файл
            os.remove(db_path)
            print("🗑️  Существующая база данных удалена")

        # Создаем базу данных
        print("🔧 Создание новой базы данных...")
        DatabaseUtil.createDatabaseIfNotExists(str(db_path))

        # Восстанавливаем дефолтные данные
        print("📊 Восстановление дефолтных данных...")
        DatabaseUtil.restoreDefaults(str(db_path))

        # Проверяем размер созданного файла
        if os.path.exists(db_path):
            file_size = os.path.getsize(db_path)
            print(f"✅ База данных создана успешно")
            print(f"📊 Размер файла: {file_size} байт")
            return True
        else:
            print("❌ База данных не была создана")
            return False

    except Exception as e:
        print(f"❌ Ошибка при создании базы данных: {e}")
        return False


def verify_database():
    """Проверяет созданную базу данных"""

    try:
        import sqlite3

        current_dir = Path(__file__).parent
        project_root = current_dir.parent
        db_path = project_root / "backend" / "com" / "sadengineer" / "budgetmaster" / "backend" / "budget_master.db"

        if not db_path.exists():
            print("❌ База данных не найдена для проверки")
            return False

        # Подключаемся к базе данных
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()

        # Получаем список таблиц
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = cursor.fetchall()

        print(f"📋 Найдено таблиц: {len(tables)}")

        # Проверяем каждую таблицу
        for table in tables:
            table_name = table[0]
            cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
            count = cursor.fetchone()[0]
            print(f"   {table_name}: {count} записей")

        conn.close()
        return True

    except Exception as e:
        print(f"❌ Ошибка при проверке базы данных: {e}")
        return False


def main():
    """Основная функция"""
    print("🗄️  Создание базы данных BudgetMaster")
    print("=" * 50)

    # Настраиваем JPype
    if not setup_jpype():
        print("❌ Не удалось настроить JPype")
        sys.exit(1)

    # Создаем базу данных
    if not create_database():
        print("❌ Не удалось создать базу данных")
        sys.exit(1)

    # Проверяем созданную базу данных
    print("\n🔍 Проверка созданной базы данных...")
    if not verify_database():
        print("❌ Ошибка при проверке базы данных")
        sys.exit(1)

    print("\n✅ База данных успешно создана с дефолтными данными!")
    print("💡 Теперь можно запускать приложение")


if __name__ == "__main__":
    main()
