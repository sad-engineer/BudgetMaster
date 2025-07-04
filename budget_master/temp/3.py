import csv
import gzip
import json
from datetime import datetime

filename = '../resorses/FinArchitect-2025-07-04_15-17.json.gz'

# Чтение данных
with gzip.open(filename, 'rt', encoding='utf-8') as f:
    data = json.load(f)

# Сохраняем currencies
currencies = data['data']['currencies']
if currencies:
    with open('../resorses/currencies.csv', 'w', newline='', encoding='utf-8') as csvfile:
        fieldnames = currencies[0].keys()
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        for row in currencies:
            # Преобразуем даты, если нужно
            row = row.copy()
            for key in ['createTime', 'updateTime', 'deleteTime']:
                if row.get(key):
                    row[key] = datetime.fromtimestamp(row[key] / 1000).strftime('%Y-%m-%d %H:%M:%S')
            writer.writerow(row)

# Сохраняем accounts
accounts = data['data']['accounts']
if accounts:
    with open('../resorses/accounts.csv', 'w', newline='', encoding='utf-8') as csvfile:
        fieldnames = accounts[0].keys()
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        for row in accounts:
            row = row.copy()
            for key in ['createTime', 'updateTime', 'deleteTime']:
                if row.get(key):
                    row[key] = datetime.fromtimestamp(row[key] / 1000).strftime('%Y-%m-%d %H:%M:%S')
            writer.writerow(row)

# Сохраняем categories
categories = data['data']['categories']
if categories:
    with open('../resorses/categories.csv', 'w', newline='', encoding='utf-8') as csvfile:
        fieldnames = categories[0].keys()
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        for row in categories:
            row = row.copy()
            for key in ['createTime', 'updateTime', 'deleteTime']:
                if row.get(key):
                    row[key] = datetime.fromtimestamp(row[key] / 1000).strftime('%Y-%m-%d %H:%M:%S')
            writer.writerow(row)

# Сохраняем budgets
budgets = data['data']['budgets']
if budgets:
    with open('../resorses/budgets.csv', 'w', newline='', encoding='utf-8') as csvfile:
        fieldnames = budgets[0].keys()
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        for row in budgets:
            row = row.copy()
            for key in ['createTime', 'updateTime', 'deleteTime']:
                if row.get(key):
                    row[key] = datetime.fromtimestamp(row[key] / 1000).strftime('%Y-%m-%d %H:%M:%S')
            writer.writerow(row)

operations = data['data']['operations']

# Открываем CSV-файл для записи
with open('../resorses/operations.csv', 'w', newline='', encoding='utf-8') as csvfile:
    # Определяем заголовки (можно выбрать нужные поля)
    fieldnames = ['id', 'date', 'amount', 'comment', 'categoryId', 'accountId', 'currencyId']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()
    for op in operations:
        # Преобразуем дату в читаемый вид
        op = op.copy()
        op['date'] = datetime.fromtimestamp(op['date'] / 1000).strftime('%Y-%m-%d %H:%M:%S')
        writer.writerow({k: op.get(k, '') for k in fieldnames})
