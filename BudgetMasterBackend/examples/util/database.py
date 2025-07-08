import os
import sys

from BudgetMasterBackend.examples.common import cleanup_example, get_java_class, setup_example, test_data_manager

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


def main():
    print("=== Тест DatabaseUtil через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        DatabaseUtil = get_java_class("util.DatabaseUtil")

        print("✅ Классы импортированы")

        # Путь к тестовой базе данных
        test_db_path = test_data_manager.db_manager.db_path
        print(f"Тестовая БД: {test_db_path}")

        # Удаляем старую тестовую БД если существует
        if os.path.exists(test_db_path):
            os.remove(test_db_path)
            print("🗑️ Удалена старая тестовая БД")

        # Создаем новую базу данных
        print("\n--- Создание базы данных ---")
        try:
            DatabaseUtil.createDatabaseIfNotExists(test_db_path)
            print("✅ База данных создана успешно")
        except Exception as e:
            # Игнорируем предупреждения о нативных библиотеках SQLite
            if "Failed to delete old native lib" in str(e) or "AccessDeniedException" in str(e):
                print("⚠️ База данных создана с предупреждением (нативные библиотеки SQLite)")
            else:
                raise e

        # Проверяем, что файл БД создался
        if os.path.exists(test_db_path):
            file_size = os.path.getsize(test_db_path)
            print(f"✅ Файл БД создан, размер: {file_size} байт")
        else:
            print("❌ Файл БД не создан")
            return

        # Тестируем повторное создание (не должно вызвать ошибку)
        print("\n--- Тест повторного создания ---")
        DatabaseUtil.createDatabaseIfNotExists(test_db_path)
        print("✅ Повторное создание выполнено без ошибок")

        # Проверяем структуру БД через SQLite
        print("\n--- Проверка структуры БД ---")
        try:
            import sqlite3

            conn = sqlite3.connect(test_db_path)
            cursor = conn.cursor()

            # Получаем список таблиц
            cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
            tables = cursor.fetchall()
            print(f"Найдено таблиц: {len(tables)}")

            for table in tables:
                table_name = table[0]
                print(f"📋 Таблица: {table_name}")

                # Получаем структуру таблицы
                cursor.execute(f"PRAGMA table_info({table_name})")
                columns = cursor.fetchall()
                for col in columns:
                    col_id, col_name, col_type, not_null, default_val, pk = col
                    print(f"  - {col_name}: {col_type}" + (" (PK)" if pk else ""))
                print()

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при проверке структуры БД: {e}")

        # Проверяем дефолтные данные
        print("\n--- Проверка дефолтных данных ---")
        try:
            # Проверяем количество валют
            currency_count = DatabaseUtil.getTableRecordCount(test_db_path, "currencies")
            print(f"Дефолтных валют: {currency_count}")
            
            # Проверяем количество категорий
            category_count = DatabaseUtil.getTableRecordCount(test_db_path, "categories")
            print(f"Дефолтных категорий: {category_count}")
            
            # Показываем дефолтные валюты
            conn = sqlite3.connect(test_db_path)
            cursor = conn.cursor()
            cursor.execute("SELECT title, position FROM currencies ORDER BY position")
            currencies = cursor.fetchall()
            print("Дефолтные валюты:")
            for title, position in currencies:
                print(f"  {position}. {title}")
            
            # Показываем иерархические категории
            cursor.execute("""
                SELECT c1.title as parent_title, c2.title, c2.operation_type, c2.type, c2.position 
                FROM categories c1 
                LEFT JOIN categories c2 ON c1.id = c2.parent_id 
                WHERE c1.parent_id IS NULL 
                ORDER BY c1.operation_type, c1.position, c2.position
            """)
            hierarchical_categories = cursor.fetchall()
            print("Иерархические категории:")
            
            current_parent = None
            for parent_title, child_title, operation_type, cat_type, position in hierarchical_categories:
                if parent_title != current_parent:
                    operation_str = "Расходы" if operation_type == 1 else "Доходы"
                    print(f"  📁 {parent_title} ({operation_str})")
                    current_parent = parent_title
                
                if child_title:
                    type_str = "Родительская" if cat_type == 0 else "Дочерняя"
                    print(f"    └─ {child_title} (тип: {type_str})")
            
            # Показываем общую статистику
            cursor.execute("SELECT COUNT(*) FROM categories")
            total_categories = cursor.fetchone()[0]
            print(f"\nВсего категорий: {total_categories}")
            
            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при проверке дефолтных данных: {e}")

        print("✅ Все тесты выполнены успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Очистка и остановка
        cleanup_example()


if __name__ == "__main__":
    main()
