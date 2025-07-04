#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
import asyncio
import sqlite3
from datetime import datetime
from typing import Any, Dict, Generic, List, Optional, TypeVar

from pydantic import BaseModel

from .base import BaseRepository

T = TypeVar('T', bound=BaseModel)


class SQLiteRepository(BaseRepository[T]):
    """Базовая реализация репозитория для SQLite"""

    def __init__(self, db_path: str, model_class: type[T]):
        self.db_path = db_path
        self.model_class = model_class
        self.table_name = model_class.Config.table_name

    def _get_connection(self) -> sqlite3.Connection:
        """Получить соединение с базой данных"""
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    def _convert_timestamp_to_datetime(self, timestamp_ms: Optional[int]) -> Optional[datetime]:
        """Конвертировать timestamp в миллисекундах в datetime"""
        if timestamp_ms is None:
            return None
        return datetime.fromtimestamp(timestamp_ms / 1000)

    def _convert_datetime_to_timestamp(self, dt: Optional[datetime]) -> Optional[int]:
        """Конвертировать datetime в timestamp в миллисекундах"""
        if dt is None:
            return None
        return int(dt.timestamp() * 1000)

    def _row_to_model(self, row: sqlite3.Row) -> T:
        """Конвертировать строку БД в модель"""
        data = dict(row)

        # Конвертируем timestamp поля в datetime
        for field in ['create_time', 'update_time', 'delete_time']:
            if field in data and data[field] is not None:
                data[field] = self._convert_timestamp_to_datetime(data[field])

        # Конвертируем date поле в datetime
        if 'date' in data and data['date'] is not None:
            data['date'] = self._convert_timestamp_to_datetime(data['date'])

        return self.model_class(**data)

    def _model_to_dict(self, model: T) -> Dict[str, Any]:
        """Конвертировать модель в словарь для БД"""
        data = model.dict()

        # Конвертируем datetime поля в timestamp
        for field in ['create_time', 'update_time', 'delete_time', 'date']:
            if field in data and data[field] is not None:
                data[field] = self._convert_datetime_to_timestamp(data[field])

        return data

    async def create(self, entity: T) -> T:
        """Создать новую запись"""

        def _create():
            conn = self._get_connection()
            try:
                data = self._model_to_dict(entity)
                fields = ', '.join(data.keys())
                placeholders = ', '.join(['?' for _ in data])
                values = list(data.values())

                query = f"INSERT INTO {self.table_name} ({fields}) VALUES ({placeholders})"
                cursor = conn.execute(query, values)
                entity.id = cursor.lastrowid
                conn.commit()
                return entity
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _create)

    async def get_by_id(self, entity_id: int) -> Optional[T]:
        """Получить запись по ID"""

        def _get_by_id():
            conn = self._get_connection()
            try:
                query = f"SELECT * FROM {self.table_name} WHERE id = ?"
                cursor = conn.execute(query, (entity_id,))
                row = cursor.fetchone()
                return self._row_to_model(row) if row else None
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _get_by_id)

    async def get_all(self) -> List[T]:
        """Получить все записи"""

        def _get_all():
            conn = self._get_connection()
            try:
                query = f"SELECT * FROM {self.table_name} ORDER BY id"
                cursor = conn.execute(query)
                rows = cursor.fetchall()
                return [self._row_to_model(row) for row in rows]
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _get_all)

    async def update(self, entity: T) -> T:
        """Обновить запись"""

        def _update():
            conn = self._get_connection()
            try:
                data = self._model_to_dict(entity)
                fields = ', '.join([f"{k} = ?" for k in data.keys() if k != 'id'])
                values = [v for k, v in data.items() if k != 'id']
                values.append(entity.id)

                query = f"UPDATE {self.table_name} SET {fields} WHERE id = ?"
                conn.execute(query, values)
                conn.commit()
                return entity
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _update)

    async def delete(self, entity_id: int) -> bool:
        """Удалить запись по ID"""

        def _delete():
            conn = self._get_connection()
            try:
                query = f"DELETE FROM {self.table_name} WHERE id = ?"
                cursor = conn.execute(query, (entity_id,))
                conn.commit()
                return cursor.rowcount > 0
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _delete)

    async def exists(self, entity_id: int) -> bool:
        """Проверить существование записи"""

        def _exists():
            conn = self._get_connection()
            try:
                query = f"SELECT COUNT(*) FROM {self.table_name} WHERE id = ?"
                cursor = conn.execute(query, (entity_id,))
                count = cursor.fetchone()[0]
                return count > 0
            finally:
                conn.close()

        return await asyncio.get_event_loop().run_in_executor(None, _exists)
