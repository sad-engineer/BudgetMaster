#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
from typing import Optional

from pydantic import Field

from budget_master.models.base import BaseEntity


class Budget(BaseEntity):
    """Модель бюджета"""

    amount: int = Field(..., description="Сумма бюджета в копейках")
    currency_id: int = Field(..., description="ID валюты бюджета")
    category_id: Optional[int] = Field(None, description="ID категории (None для общего бюджета)")

    class Config:
        table_name = "budgets"
