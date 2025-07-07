#!/usr/bin/env python
# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------------------------------------------------
import asyncio

from budget_master.models import Account, Budget, Category, Currency, Operation
from budget_master.repositories import SQLiteRepository
from budget_master.services import CSVImportService, DatabaseService


async def main():
    """Главная функция для создания БД и импорта данных"""

    # Пути к файлам
    db_path = "../BudgetMasterBackend/budget_master.db"
    csv_dir = "../app"  # Текущая папка с CSV файлами

    print("🚀 Запуск импорта данных в SQLite базу данных...")

    # Создаем сервис базы данных
    db_service = DatabaseService(db_path)

    # Проверяем существование БД
    if await db_service.database_exists():
        print(f"📁 База данных уже существует: {db_path}")
        response = input("Очистить существующую базу данных? (y/N): ")
        if response.lower() == 'y':
            print("🗑️ Очищаем базу данных...")
            await db_service.clear_database()
        else:
            print("❌ Импорт отменен")
            return
    else:
        print("📁 Создаем новую базу данных...")

    # Создаем структуру БД
    if not await db_service.create_database():
        print("❌ Ошибка при создании базы данных")
        return

    print("✅ База данных создана успешно")

    # Создаем репозитории
    currency_repo = SQLiteRepository(db_path, Currency)
    account_repo = SQLiteRepository(db_path, Account)
    category_repo = SQLiteRepository(db_path, Category)
    budget_repo = SQLiteRepository(db_path, Budget)
    operation_repo = SQLiteRepository(db_path, Operation)

    # Создаем сервис импорта
    import_service = CSVImportService(
        currency_repo=currency_repo,
        account_repo=account_repo,
        category_repo=category_repo,
        budget_repo=budget_repo,
        operation_repo=operation_repo,
    )

    # Импортируем данные
    print("📥 Начинаем импорт данных из CSV файлов...")

    try:
        results = await import_service.import_all_data(csv_dir)

        print("✅ Импорт завершен успешно!")
        print("\n📊 Статистика импорта:")
        for entity_type, count in results.items():
            print(f"   {entity_type}: {count} записей")

        # Получаем информацию о БД
        db_info = await db_service.get_database_info()
        print(f"\n📁 Размер базы данных: {db_info['database_size'] / 1024:.2f} KB")

    except Exception as e:
        print(f"❌ Ошибка при импорте данных: {e}")
        return

    print("\n🎉 Все готово! База данных создана и заполнена данными.")


if __name__ == "__main__":
    asyncio.run(main())
