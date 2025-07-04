#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class BaseEntity(BaseModel):
    """Базовая модель для всех сущностей с общими полями"""

    id: int = Field(..., description="Уникальный идентификатор")
    create_time: datetime = Field(..., description="Время создания записи")
    update_time: Optional[datetime] = Field(None, description="Время последнего обновления")
    delete_time: Optional[datetime] = Field(None, description="Время удаления (soft delete)")
    created_by: Optional[str] = Field(None, description="Кто создал запись")
    updated_by: Optional[str] = Field(None, description="Кто обновил запись")
    deleted_by: Optional[str] = Field(None, description="Кто удалил запись")

    class Config:
        from_attributes = True
        json_encoders = {datetime: lambda v: v.isoformat() if v else None}
