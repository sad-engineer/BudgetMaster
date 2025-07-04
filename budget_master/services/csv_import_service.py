#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
import csv
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Union

from budget_master.models import Category, Currency, Operation
from budget_master.models.account import Account
from budget_master.models.budget import Budget

from ..repositories.base import BaseRepository


class CSVImportService:
    """Сервис для импорта данных из CSV файлов в базу данных"""

    def __init__(
        self,
        currency_repo: BaseRepository[Currency],
        account_repo: BaseRepository[Account],
        category_repo: BaseRepository[Category],
        budget_repo: BaseRepository[Budget],
        operation_repo: BaseRepository[Operation],
    ):
        self.currency_repo = currency_repo
        self.account_repo = account_repo
        self.category_repo = category_repo
        self.budget_repo = budget_repo
        self.operation_repo = operation_repo

    def _parse_datetime(self, date_str: str) -> datetime:
        """Парсинг даты из строки"""
        if not date_str or date_str.strip() == '':
            return datetime.now()
        return datetime.strptime(date_str, '%Y-%m-%d %H:%M:%S')

    def _parse_int_or_none(self, value: str) -> Union[int, None]:
        """Парсинг целого числа или None"""
        if not value or value.strip() == '':
            return None
        return int(value)

    async def import_currencies(self, csv_path: str) -> List[Currency]:
        """Импорт валют из CSV"""
        currencies = []
        with open(csv_path, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                currency = Currency(
                    id=int(row['id']),
                    create_time=self._parse_datetime(row['createTime']),
                    update_time=self._parse_datetime(row['updateTime']) if row['updateTime'] else None,
                    delete_time=self._parse_datetime(row['deleteTime']) if row['deleteTime'] else None,
                    created_by=row['createdBy'] if row['createdBy'] else None,
                    updated_by=row['updatedBy'] if row['updatedBy'] else None,
                    deleted_by=row['deletedBy'] if row['deletedBy'] else None,
                    position=int(row['position']),
                    title=row['title'],
                )
                currencies.append(currency)
                await self.currency_repo.create(currency)

        return currencies

    async def import_accounts(self, csv_path: str) -> List[Account]:
        """Импорт счетов из CSV"""
        accounts = []
        with open(csv_path, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                account = Account(
                    id=int(row['id']),
                    create_time=self._parse_datetime(row['createTime']),
                    update_time=self._parse_datetime(row['updateTime']) if row['updateTime'] else None,
                    delete_time=self._parse_datetime(row['deleteTime']) if row['deleteTime'] else None,
                    created_by=row['createdBy'] if row['createdBy'] else None,
                    updated_by=row['updatedBy'] if row['updatedBy'] else None,
                    deleted_by=row['deletedBy'] if row['deletedBy'] else None,
                    position=int(row['position']),
                    title=row['title'],
                    amount=int(row['amount']),
                    type=int(row['type']),
                    currency_id=int(row['currencyId']),
                    closed=int(row['closed']),
                    credit_card_limit=self._parse_int_or_none(row['creditCardLimit']),
                    credit_card_category_id=self._parse_int_or_none(row['creditCardCategoryId']),
                    credit_card_commission_category_id=self._parse_int_or_none(row['creditCardCommissionCategoryId']),
                )
                accounts.append(account)
                await self.account_repo.create(account)

        return accounts

    async def import_categories(self, csv_path: str) -> List[Category]:
        """Импорт категорий из CSV"""
        categories = []
        with open(csv_path, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                category = Category(
                    id=int(row['id']),
                    create_time=self._parse_datetime(row['createTime']),
                    update_time=self._parse_datetime(row['updateTime']) if row['updateTime'] else None,
                    delete_time=self._parse_datetime(row['deleteTime']) if row['deleteTime'] else None,
                    created_by=row['createdBy'] if row['createdBy'] else None,
                    updated_by=row['updatedBy'] if row['updatedBy'] else None,
                    deleted_by=row['deletedBy'] if row['deletedBy'] else None,
                    position=int(row['position']),
                    title=row['title'],
                    operation_type=int(row['operationType']),
                    type=int(row['type']),
                    parent_id=self._parse_int_or_none(row['parentId']),
                )
                categories.append(category)
                await self.category_repo.create(category)

        return categories

    async def import_budgets(self, csv_path: str) -> List[Budget]:
        """Импорт бюджетов из CSV"""
        budgets = []
        with open(csv_path, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                budget = Budget(
                    id=int(row['id']),
                    create_time=self._parse_datetime(row['createTime']),
                    update_time=self._parse_datetime(row['updateTime']) if row['updateTime'] else None,
                    delete_time=self._parse_datetime(row['deleteTime']) if row['deleteTime'] else None,
                    created_by=row['createdBy'] if row['createdBy'] else None,
                    updated_by=row['updatedBy'] if row['updatedBy'] else None,
                    deleted_by=row['deletedBy'] if row['deletedBy'] else None,
                    amount=int(row['amount']),
                    currency_id=int(row['currencyId']),
                    category_id=self._parse_int_or_none(row['categoryId']),
                )
                budgets.append(budget)
                await self.budget_repo.create(budget)

        return budgets

    async def import_operations(self, csv_path: str) -> List[Operation]:
        """Импорт операций из CSV"""
        operations = []
        with open(csv_path, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                operation = Operation(
                    id=int(row['id']),
                    create_time=self._parse_datetime(row['createTime']),
                    update_time=self._parse_datetime(row['updateTime']) if row['updateTime'] else None,
                    delete_time=self._parse_datetime(row['deleteTime']) if row['deleteTime'] else None,
                    created_by=row['createdBy'] if row['createdBy'] else None,
                    updated_by=row['updatedBy'] if row['updatedBy'] else None,
                    deleted_by=row['deletedBy'] if row['deletedBy'] else None,
                    type=int(row['type']),
                    date=self._parse_datetime(row['date']),
                    amount=int(row['amount']),
                    comment=row['comment'],
                    category_id=int(row['categoryId']),
                    account_id=int(row['accountId']),
                    currency_id=int(row['currencyId']),
                    to_account_id=self._parse_int_or_none(row['toAccountId']),
                    to_currency_id=self._parse_int_or_none(row['toCurrencyId']),
                    to_amount=self._parse_int_or_none(row['toAmount']),
                )
                operations.append(operation)
                await self.operation_repo.create(operation)

        return operations

    async def import_all_data(self, data_dir: str) -> Dict[str, int]:
        """Импорт всех данных из CSV файлов"""
        data_path = Path(data_dir)

        results = {}

        # Импортируем в правильном порядке (с учетом зависимостей)
        if (data_path / 'currencies.csv').exists():
            currencies = await self.import_currencies(str(data_path / 'currencies.csv'))
            results['currencies'] = len(currencies)

        if (data_path / 'accounts.csv').exists():
            accounts = await self.import_accounts(str(data_path / 'accounts.csv'))
            results['accounts'] = len(accounts)

        if (data_path / 'categories.csv').exists():
            categories = await self.import_categories(str(data_path / 'categories.csv'))
            results['categories'] = len(categories)

        if (data_path / 'budgets.csv').exists():
            budgets = await self.import_budgets(str(data_path / 'budgets.csv'))
            results['budgets'] = len(budgets)

        if (data_path / 'operations.csv').exists():
            operations = await self.import_operations(str(data_path / 'operations.csv'))
            results['operations'] = len(operations)

        return results
