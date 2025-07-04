#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
from typing import Optional

from pydantic import Field

from budget_master.models.base import BaseEntity


class Category(BaseEntity):
    """Модель категории"""

    position: int = Field(..., description="Позиция для сортировки")
    title: str = Field(..., description="Название категории")
    operation_type: int = Field(..., description="Тип операции (0 - доход, 1 - расход)")
    type: int = Field(..., description="Тип категории (0 - родительская, 1 - дочерняя)")
    parent_id: Optional[int] = Field(None, description="ID родительской категории")

    class Config:
        table_name = "categories"
