#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
from abc import ABC, abstractmethod
from typing import Generic, List, Optional, TypeVar

from pydantic import BaseModel

T = TypeVar('T', bound=BaseModel)


class BaseRepository(ABC, Generic[T]):
    """Базовый интерфейс репозитория для работы с данными"""

    @abstractmethod
    async def create(self, entity: T) -> T:
        """Создать новую запись"""
        pass

    @abstractmethod
    async def get_by_id(self, entity_id: int) -> Optional[T]:
        """Получить запись по ID"""
        pass

    @abstractmethod
    async def get_all(self) -> List[T]:
        """Получить все записи"""
        pass

    @abstractmethod
    async def update(self, entity: T) -> T:
        """Обновить запись"""
        pass

    @abstractmethod
    async def delete(self, entity_id: int) -> bool:
        """Удалить запись по ID"""
        pass

    @abstractmethod
    async def exists(self, entity_id: int) -> bool:
        """Проверить существование записи"""
        pass
