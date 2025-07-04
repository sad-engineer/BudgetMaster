#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
from typing import Optional

from pydantic import Field

from budget_master.models.base import BaseEntity


class Account(BaseEntity):
    """Модель счета"""

    position: int = Field(..., description="Позиция для сортировки")
    title: str = Field(..., description="Название счета")
    amount: int = Field(..., description="Баланс счета в копейках")
    type: int = Field(..., description="Тип счета (0 - обычный, 1 - кредитный)")
    currency_id: int = Field(..., description="ID валюты счета")
    closed: int = Field(..., description="Закрыт ли счет (0 - нет, 1 - да)")
    credit_card_limit: Optional[int] = Field(None, description="Лимит кредитной карты")
    credit_card_category_id: Optional[int] = Field(None, description="ID категории для комиссий кредитной карты")
    credit_card_commission_category_id: Optional[int] = Field(None, description="ID категории для комиссий")

    class Config:
        table_name = "accounts"
