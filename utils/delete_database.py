#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для удаления базы данных budget_master.db
"""

import os
import shutil
import sys
from pathlib import Path


def close_sqlite_connections():
    """Пытается закрыть SQLite соединения через Python"""
    try:
        import sqlite3

        # Путь к базе данных
        db_path = Path(__file__).parent.parent / "backend" / "budget_master.db"
        if db_path.exists():
            print("🔌 Закрытие SQLite соединений...")
            conn = sqlite3.connect(db_path)
            conn.close()
            print("✅ SQLite соединения закрыты")
            return True
    except Exception as e:
        print(f"⚠️  Ошибка при закрытии SQLite соединений: {e}")
        return False


def force_close_connections():
    """Принудительно закрывает все возможные соединения"""
    print("🔧 Принудительное закрытие соединений...")

    # Закрываем SQLite соединения
    close_sqlite_connections()

    # Небольшая пауза для освобождения файла
    import time

    time.sleep(1)


def delete_database():
    """Удаляет базу данных budget_master.db"""

    # Путь к базе данных
    db_path = Path(__file__).parent.parent / "backend" / "budget_master.db"

    print(f"🔍 Проверяем наличие базы данных: {db_path}")

    if not db_path.exists():
        print("❌ База данных не найдена")
        return False

    try:
        # Получаем размер файла
        file_size = db_path.stat().st_size
        print(f"📊 Размер базы данных: {file_size} байт")

        # Закрываем все соединения перед удалением
        force_close_connections()

        # Пытаемся удалить файл
        try:
            db_path.unlink()
            print(f"✅ База данных успешно удалена: {db_path}")
            return True
        except PermissionError:
            print("❌ Файл все еще заблокирован")
            print("💡 Попробуйте:")
            print("   1. Закрыть все программы, использующие базу данных")
            print("   2. Перезапустить IDE/терминал")
            print("   3. Перезагрузить компьютер")
            return False

    except PermissionError:
        print("❌ Ошибка: нет прав для удаления файла")
        print("💡 Возможно, база данных используется другим процессом")
        return False
    except Exception as e:
        print(f"❌ Ошибка при удалении: {e}")
        return False


def main():
    """Основная функция"""
    print("🗑️  Удаление базы данных BudgetMaster")
    print("=" * 50)

    success = delete_database()

    if success:
        print("\n✅ Операция завершена успешно")
        print("💡 Для создания новой базы данных запустите create_database.py")
    else:
        print("\n❌ Операция не выполнена")
        print("💡 Убедитесь, что все программы, использующие базу данных, закрыты")
        sys.exit(1)


if __name__ == "__main__":
    main()
