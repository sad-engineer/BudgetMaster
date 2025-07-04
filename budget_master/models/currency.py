#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
from typing import Optional

from pydantic import Field

from budget_master.models.base import BaseEntity


class Currency(BaseEntity):
    """Модель валюты"""

    position: int = Field(..., description="Позиция для сортировки")
    title: str = Field(..., description="Название валюты (например, RUB, USD)")

    class Config:
        table_name = "currencies"
