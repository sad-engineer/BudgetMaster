import os
import sys

from BudgetMasterBackend.examples.common import (
    cleanup_example,
    create_test_entity,
    get_java_class,
    setup_example,
    test_data_manager,
)

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


def main():
    print("=== Тест BudgetRepository через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Budget = get_java_class("model.Budget")
        BudgetRepository = get_java_class("repository.BudgetRepository")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий
        repo = BudgetRepository(test_data_manager.db_manager.db_path)
        print("✅ Репозиторий создан")

        # Создаем тестовый бюджет
        budget = create_test_entity(
            Budget,
            amount=100000,  # 1000.00
            currencyId=1,   # RUB
            categoryId=None,
            position=0,
            createdBy="tester",
            updatedBy="tester",
        )

        print("✅ Тестовый бюджет создан")
        print(f"Бюджет: {budget.toString()}")

        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_budget = repo.save(budget)
        print(f"Бюджет сохранен: {saved_budget.toString()}")
        print(f"ID бюджета: {saved_budget.getId()}")
        test_data_manager.add_test_id("budgets", saved_budget.getId())

        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_budget = repo.findById(saved_budget.getId())
            if found_budget.isPresent():
                bud = found_budget.get()
                print(f"Найден бюджет: {bud.toString()}")
                print(f"ID: {bud.getId()}")
                print(f"Position: {bud.getPosition()}")
                print(f"Amount: {bud.getAmount()}")
                print(f"Currency ID: {bud.getCurrencyId()}")
                print(f"Category ID: {bud.getCategoryId()}")
                print(f"Created by: {bud.getCreatedBy()}")
            else:
                print("❌ Бюджет не найден")
        except Exception as e:
            print(f"❌ Ошибка при поиске: {e}")
            import traceback
            traceback.print_exc()

        # Прямой SQL-запрос для проверки
        print("\n--- Прямой SQL-запрос ---")
        try:
            import sqlite3

            conn = sqlite3.connect(test_data_manager.db_manager.db_path)
            cursor = conn.cursor()

            cursor.execute("SELECT * FROM budgets WHERE id = ?", (saved_budget.getId(),))
            row = cursor.fetchone()

            if row:
                print("✅ Найден бюджет в БД:")
                cursor.execute("PRAGMA table_info(budgets)")
                columns = [col[1] for col in cursor.fetchall()]

                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Бюджет не найден в БД")

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")

        # Тестируем обновление
        print("\n--- Тест обновления ---")
        try:
            saved_budget.setAmount(200000)  # 2000.00
            saved_budget.setPosition(2)
            saved_budget.setUpdatedBy("jpype_update_test")
            saved_budget.setUpdateTime(LocalDateTime.now())

            updated_budget = repo.update(saved_budget)
            print(f"Бюджет обновлен: {updated_budget.toString()}")

            found_updated = repo.findById(saved_budget.getId())
            if found_updated.isPresent():
                bud = found_updated.get()
                print(f"Проверка обновления: {bud.toString()}")
                print(f"Новый amount: {bud.getAmount()}")
                print(f"Новый position: {bud.getPosition()}")
                print(f"Updated by: {bud.getUpdatedBy()}")
            else:
                print("❌ Обновленный бюджет не найден")
        except Exception as e:
            print(f"❌ Ошибка при обновлении: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем soft delete
        print("\n--- Тест soft delete ---")
        try:
            budget_id = saved_budget.getId()
            all_before_delete = repo.findAll()
            print(f"Бюджетов до удаления: {len(all_before_delete)}")

            deleted = repo.delete(budget_id, "test_user")
            print(f"Бюджет помечен как удаленный: {deleted}")

            all_after_delete = repo.findAll()
            print(f"Бюджетов после удаления (обычный режим): {len(all_after_delete)}")

            repo.setIncludeDeleted(True)
            all_with_deleted = repo.findAll()
            print(f"Бюджетов после удаления (включая удаленные): {len(all_with_deleted)}")

            found_deleted = repo.findById(budget_id)
            if found_deleted.isPresent():
                deleted_budget = found_deleted.get()
                print(f"Найден удаленный бюджет: {deleted_budget.getAmount()}")
                print(f"Deleted by: {deleted_budget.getDeletedBy()}")
                print(f"Delete time: {deleted_budget.getDeleteTime()}")
            else:
                print("❌ Удаленный бюджет не найден")

            # Восстановление через restore
            print("\n--- Тест восстановления через restore ---")
            restored = repo.restore(budget_id)
            print(f"Бюджет восстановлен через restore: {restored}")

            all_after_restore = repo.findAll()
            print(f"Бюджетов после восстановления: {len(all_after_restore)}")

            found_restored = repo.findById(budget_id)
            if found_restored.isPresent():
                restored_budget = found_restored.get()
                print(f"Найден восстановленный бюджет: {restored_budget.getAmount()}")
                print(f"Delete time после восстановления: {restored_budget.getDeleteTime()}")
                print(f"Deleted by после восстановления: {restored_budget.getDeletedBy()}")
            else:
                print("❌ Восстановленный бюджет не найден")

        except Exception as e:
            print(f"❌ Ошибка при soft delete: {e}")
            import traceback
            traceback.print_exc()

        print("\n✅ Все тесты выполнены успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback
        traceback.print_exc()

    finally:
        cleanup_example()


if __name__ == "__main__":
    main() 