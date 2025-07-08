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
    print("=== Тест AccountRepository через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Account = get_java_class("model.Account")
        AccountRepository = get_java_class("repository.AccountRepository")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий
        repo = AccountRepository(test_data_manager.db_manager.db_path)
        print("✅ Репозиторий создан")

        # Создаем тестовый счет
        account = create_test_entity(
            Account,
            title="Наличные (тест)",
            amount=100000,  # 1000.00 в копейках
            type=1,  # Текущий счет
            currencyId=1,  # RUB
            closed=0,  # Не закрыт
            createdBy="tester",
            updatedBy="tester",
        )

        print("✅ Тестовый счет создан")
        print(f"Счет: {account.toString()}")

        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_account = repo.save(account)
        print(f"Счет сохранен: {saved_account.toString()}")
        print(f"ID счета: {saved_account.getId()}")
        test_data_manager.add_test_id("accounts", saved_account.getId())

        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_account = repo.findById(saved_account.getId())
            if found_account.isPresent():
                acc = found_account.get()
                print(f"Найден счет: {acc.toString()}")
                print(f"ID: {acc.getId()}")
                print(f"Position: {acc.getPosition()}")
                print(f"Title: {acc.getTitle()}")
                print(f"Amount: {acc.getAmount()} копеек")
                print(f"Type: {acc.getType()}")
                print(f"Currency ID: {acc.getCurrencyId()}")
                print(f"Closed: {acc.getClosed()}")
                print(f"Created by: {acc.getCreatedBy()}")
            else:
                print("❌ Счет не найден")
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

            cursor.execute("SELECT * FROM accounts WHERE id = ?", (saved_account.getId(),))
            row = cursor.fetchone()

            if row:
                print("✅ Найден счет в БД:")
                cursor.execute("PRAGMA table_info(accounts)")
                columns = [col[1] for col in cursor.fetchall()]

                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Счет не найден в БД")

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")

        # Тестируем обновление
        print("\n--- Тест обновления ---")
        try:
            saved_account.setTitle("Основной счет (обновленный)")
            saved_account.setPosition(2)
            saved_account.setAmount(150000)  # 1500.00 в копейках
            saved_account.setUpdatedBy("jpype_update_test")
            saved_account.setUpdateTime(LocalDateTime.now())

            updated_account = repo.update(saved_account)
            print(f"Счет обновлен: {updated_account.toString()}")

            found_updated = repo.findById(saved_account.getId())
            if found_updated.isPresent():
                acc = found_updated.get()
                print(f"Проверка обновления: {acc.toString()}")
                print(f"Новый title: {acc.getTitle()}")
                print(f"Новый position: {acc.getPosition()}")
                print(f"Новый amount: {acc.getAmount()} копеек")
                print(f"Updated by: {acc.getUpdatedBy()}")
            else:
                print("❌ Обновленный счет не найден")
        except Exception as e:
            print(f"❌ Ошибка при обновлении: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем soft delete
        print("\n--- Тест soft delete ---")
        try:
            account_id = saved_account.getId()
            all_before_delete = repo.findAll()
            print(f"Счетов до удаления: {len(all_before_delete)}")

            deleted = repo.delete(account_id, "test_user")
            print(f"Счет помечен как удаленный: {deleted}")

            all_after_delete = repo.findAll()
            print(f"Счетов после удаления (обычный режим): {len(all_after_delete)}")

            repo.setIncludeDeleted(True)
            all_with_deleted = repo.findAll()
            print(f"Счетов после удаления (включая удаленные): {len(all_with_deleted)}")

            found_deleted = repo.findById(account_id)
            if found_deleted.isPresent():
                deleted_account = found_deleted.get()
                print(f"Найден удаленный счет: {deleted_account.getTitle()}")
                print(f"Deleted by: {deleted_account.getDeletedBy()}")
                print(f"Delete time: {deleted_account.getDeleteTime()}")
            else:
                print("❌ Удаленный счет не найден")

            # Восстановление через restore
            print("\n--- Тест восстановления через restore ---")
            restored = repo.restore(account_id)
            print(f"Счет восстановлен через restore: {restored}")

            all_after_restore = repo.findAll()
            print(f"Счетов после восстановления: {len(all_after_restore)}")

            found_restored = repo.findById(account_id)
            if found_restored.isPresent():
                restored_account = found_restored.get()
                print(f"Найден восстановленный счет: {restored_account.getTitle()}")
                print(f"Delete time после восстановления: {restored_account.getDeleteTime()}")
                print(f"Deleted by после восстановления: {restored_account.getDeletedBy()}")
            else:
                print("❌ Восстановленный счет не найден")

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
