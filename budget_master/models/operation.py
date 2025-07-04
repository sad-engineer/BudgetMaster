#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
from datetime import datetime
from typing import Optional

from pydantic import Field

from .base import BaseEntity


class Operation(BaseEntity):
    """Модель операции (доход/расход)"""

    type: int = Field(..., description="Тип операции (0 - доход, 1 - расход)")
    date: datetime = Field(..., description="Дата операции")
    amount: int = Field(..., description="Сумма операции в копейках")
    comment: str = Field(..., description="Комментарий к операции")
    category_id: int = Field(..., description="ID категории")
    account_id: int = Field(..., description="ID счета")
    currency_id: int = Field(..., description="ID валюты")
    to_account_id: Optional[int] = Field(None, description="ID счета назначения (для переводов)")
    to_currency_id: Optional[int] = Field(None, description="ID валюты назначения (для переводов)")
    to_amount: Optional[int] = Field(None, description="Сумма в валюте назначения (для переводов)")

    class Config:
        table_name = "operations"
