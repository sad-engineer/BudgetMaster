#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
import asyncio
import sqlite3
from pathlib import Path
from typing import Optional


class DatabaseService:
    """Сервис для создания и управления базой данных SQLite"""

    def __init__(self, db_path: str):
        self.db_path = db_path

    def _get_connection(self) -> sqlite3.Connection:
        """Получить соединение с базой данных"""
        conn = sqlite3.connect(self.db_path)
        conn.execute("PRAGMA foreign_keys = ON")
        return conn

    async def create_database(self) -> bool:
        """Создать базу данных с таблицами"""

        def _create_database():
            conn = self._get_connection()
            try:
                # Создаем таблицу валют
                conn.execute(
                    """
                    CREATE TABLE IF NOT EXISTS currencies (
                        id INTEGER PRIMARY KEY,
                        create_time INTEGER NOT NULL,
                        update_time INTEGER,
                        delete_time INTEGER,
                        created_by TEXT,
                        updated_by TEXT,
                        deleted_by TEXT,
                        position INTEGER NOT NULL,
                        title TEXT NOT NULL
                    )
                """
                )

                # Создаем таблицу счетов
                conn.execute(
                    """
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY,
                        create_time INTEGER NOT NULL,
                        update_time INTEGER,
                        delete_time INTEGER,
                        created_by TEXT,
                        updated_by TEXT,
                        deleted_by TEXT,
                        position INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        type INTEGER NOT NULL,
                        currency_id INTEGER NOT NULL,
                        closed INTEGER NOT NULL DEFAULT 0,
                        credit_card_limit INTEGER,
                        credit_card_category_id INTEGER,
                        credit_card_commission_category_id INTEGER,
                        FOREIGN KEY (currency_id) REFERENCES currencies (id)
                    )
                """
                )

                # Создаем таблицу категорий
                conn.execute(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY,
                        create_time INTEGER NOT NULL,
                        update_time INTEGER,
                        delete_time INTEGER,
                        created_by TEXT,
                        updated_by TEXT,
                        deleted_by TEXT,
                        position INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        operation_type INTEGER NOT NULL,
                        type INTEGER NOT NULL,
                        parent_id INTEGER,
                        FOREIGN KEY (parent_id) REFERENCES categories (id)
                    )
                """
                )

                # Создаем таблицу бюджетов
                conn.execute(
                    """
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY,
                        create_time INTEGER NOT NULL,
                        update_time INTEGER,
                        delete_time INTEGER,
                        created_by TEXT,
                        updated_by TEXT,
                        deleted_by TEXT,
                        amount INTEGER NOT NULL,
                        currency_id INTEGER NOT NULL,
                        category_id INTEGER,
                        FOREIGN KEY (currency_id) REFERENCES currencies (id),
                        FOREIGN KEY (category_id) REFERENCES categories (id)
                    )
                """
                )

                # Создаем таблицу операций
                conn.execute(
                    """
                    CREATE TABLE IF NOT EXISTS operations (
                        id INTEGER PRIMARY KEY,
                        create_time INTEGER NOT NULL,
                        update_time INTEGER,
                        delete_time INTEGER,
                        created_by TEXT,
                        updated_by TEXT,
                        deleted_by TEXT,
                        type INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        comment TEXT NOT NULL,
                        category_id INTEGER NOT NULL,
                        account_id INTEGER NOT NULL,
                        currency_id INTEGER NOT NULL,
                        to_account_id INTEGER,
                        to_currency_id INTEGER,
                        to_amount INTEGER,
                        FOREIGN KEY (category_id) REFERENCES categories (id),
                        FOREIGN KEY (account_id) REFERENCES accounts (id),
                        FOREIGN KEY (currency_id) REFERENCES currencies (id),
                        FOREIGN KEY (to_account_id) REFERENCES accounts (id),
                        FOREIGN KEY (to_currency_id) REFERENCES currencies (id)
                    )
                """
                )

                # Создаем индексы для улучшения производительности
                conn.execute("CREATE INDEX IF NOT EXISTS idx_operations_date ON operations (date)")
                conn.execute("CREATE INDEX IF NOT EXISTS idx_operations_category ON operations (category_id)")
                conn.execute("CREATE INDEX IF NOT EXISTS idx_operations_account ON operations (account_id)")
                conn.execute("CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories (parent_id)")
                conn.execute("CREATE INDEX IF NOT EXISTS idx_accounts_currency ON accounts (currency_id)")

                conn.commit()
                return True
            except Exception as e:
                print(f"Ошибка при создании базы данных: {e}")
                return False
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _create_database)

    async def database_exists(self) -> bool:
        """Проверить существование базы данных"""

        def _check_exists():
            return Path(self.db_path).exists()

        return await asyncio.get_event_loop().run_in_executor(None, _check_exists)

    async def get_database_info(self) -> dict:
        """Получить информацию о базе данных"""

        def _get_info():
            conn = self._get_connection()
            try:
                # Получаем список таблиц
                cursor = conn.execute("SELECT name FROM sqlite_master WHERE type='table'")
                tables = [row[0] for row in cursor.fetchall()]

                # Получаем количество записей в каждой таблице
                table_counts = {}
                for table in tables:
                    cursor = conn.execute(f"SELECT COUNT(*) FROM {table}")
                    count = cursor.fetchone()[0]
                    table_counts[table] = count

                return {
                    'tables': tables,
                    'table_counts': table_counts,
                    'database_size': Path(self.db_path).stat().st_size if Path(self.db_path).exists() else 0,
                }
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _get_info)

    async def clear_database(self) -> bool:
        """Очистить все данные из базы"""

        def _clear_database():
            conn = self._get_connection()
            try:
                # Отключаем проверку внешних ключей для очистки
                conn.execute("PRAGMA foreign_keys = OFF")

                # Получаем список таблиц
                cursor = conn.execute("SELECT name FROM sqlite_master WHERE type='table'")
                tables = [row[0] for row in cursor.fetchall()]

                # Очищаем каждую таблицу
                for table in tables:
                    conn.execute(f"DELETE FROM {table}")

                # Сбрасываем автоинкремент
                for table in tables:
                    conn.execute(f"DELETE FROM sqlite_sequence WHERE name='{table}'")

                conn.commit()
                return True
            except Exception as e:
                print(f"Ошибка при очистке базы данных: {e}")
                return False
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _clear_database)
