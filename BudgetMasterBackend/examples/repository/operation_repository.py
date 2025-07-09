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
    print("=== Тест OperationRepository через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Operation = get_java_class("model.Operation")
        OperationRepository = get_java_class("repository.OperationRepository")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий
        repo = OperationRepository(test_data_manager.db_manager.db_path)
        print("✅ Репозиторий создан")

        # Создаем тестовую операцию
        operation = create_test_entity(
            Operation,
            type=1,
            amount=1500,
            date=LocalDateTime.of(2023, 1, 30, 12, 0, 0),
            comment="Тестовая операция из JPype",
            categoryId=1,
            accountId=1,
            currencyId=1,
            createdBy="tester",
            updatedBy="tester",
        )

        print("✅ Тестовая операция создана")
        print(f"Операция: {operation.toString()}")

        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_operation = repo.save(operation)
        print(f"Операция сохранена: {saved_operation.toString()}")
        test_data_manager.add_test_id("operations", saved_operation.getId())

        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_operation = repo.findById(saved_operation.getId())
            if found_operation.isPresent():
                op = found_operation.get()
                print(f"Найдена операция: {op.toString()}")
                print(f"ID: {op.getId()}")
                print(f"Type: {op.getType()}")
                print(f"Amount: {op.getAmount()}")
                print(f"Date: {op.getDate()}")
                print(f"Comment: {op.getComment()}")
                print(f"Category ID: {op.getCategoryId()}")
                print(f"Account ID: {op.getAccountId()}")
                print(f"Currency ID: {op.getCurrencyId()}")
            else:
                print("❌ Операция не найдена")
        except Exception as e:
            print(f"❌ Ошибка при поиске: {e}")
            import traceback
            traceback.print_exc()

        # Прямой SQL-запрос к базе данных
        print("\n--- Прямой SQL-запрос ---")
        try:
            import sqlite3

            conn = sqlite3.connect(test_data_manager.db_manager.db_path)
            cursor = conn.cursor()

            cursor.execute("SELECT * FROM operations WHERE id = ?", (saved_operation.getId(),))
            row = cursor.fetchone()

            if row:
                print("✅ Найдена операция в БД:")
                cursor.execute("PRAGMA table_info(operations)")
                columns = [col[1] for col in cursor.fetchall()]

                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Операция не найдена в БД")

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем создание нескольких операций
        print("\n--- Тест создания нескольких операций ---")
        test_operations = []
        
        # Создаем операции разных типов
        operation_data = [
            (1, 2000, "Расход на продукты", 1, 1, 1),      # Расход
            (2, 5000, "Зарплата", 2, 1, 1),               # Доход
            (1, 1500, "Расход на транспорт", 1, 1, 1),     # Расход
            (2, 3000, "Подработка", 2, 1, 1),             # Доход
        ]
        
        for i, (op_type, amount, comment, category_id, account_id, currency_id) in enumerate(operation_data):
            test_operation = create_test_entity(
                Operation,
                type=op_type,
                amount=amount,
                date=LocalDateTime.of(2023, 1, 30 + i, 12, 0, 0),
                comment=comment,
                categoryId=category_id,
                accountId=account_id,
                currencyId=currency_id,
                createdBy="multi_test",
                updatedBy="multi_test",
            )

            saved_test_operation = repo.save(test_operation)
            test_operations.append(saved_test_operation)
            test_data_manager.add_test_id("operations", saved_test_operation.getId())
            print(f"Создана операция: {saved_test_operation.getComment()}, Type: {saved_test_operation.getType()}, Amount: {saved_test_operation.getAmount()}")

        # Проверяем все операции
        all_operations = repo.findAll()
        print(f"\nВсе операции:")
        for op in all_operations:
            type_str = "Расход" if op.getType() == 1 else "Доход"
            print(f"  ID: {op.getId()}, Type: {op.getType()} ({type_str}), Amount: {op.getAmount()}, Comment: {op.getComment()}")

        # Тестируем сортировку по дате
        print("\n--- Тест сортировки по дате ---")
        try:
            ordered_operations = repo.findAllOrderedByDate()
            print(f"Операции, отсортированные по дате (сначала новые):")
            for op in ordered_operations:
                print(f"  Date: {op.getDate()}, Amount: {op.getAmount()}, Comment: {op.getComment()}")
        except Exception as e:
            print(f"❌ Ошибка при сортировке по дате: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем обновление
        print("\n--- Тест обновления ---")
        try:
            saved_operation.setAmount(2000)
            saved_operation.setComment("Обновленная операция из JPype")
            saved_operation.setUpdatedBy("jpype_update_test")
            saved_operation.setUpdateTime(LocalDateTime.now())

            updated_operation = repo.update(saved_operation)
            print(f"Операция обновлена: {updated_operation.toString()}")

            found_updated = repo.findById(saved_operation.getId())
            if found_updated.isPresent():
                op = found_updated.get()
                print(f"Проверка обновления: {op.toString()}")
                print(f"Новый amount: {op.getAmount()}")
                print(f"Новый comment: {op.getComment()}")
                print(f"Updated by: {op.getUpdatedBy()}")
            else:
                print("❌ Обновленная операция не найдена")
        except Exception as e:
            print(f"❌ Ошибка при обновлении: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем soft delete
        print("\n--- Тест soft delete ---")
        try:
            operation_id = saved_operation.getId()
            all_before_delete = repo.findAll()
            print(f"Операций до удаления: {len(all_before_delete)}")

            deleted = repo.delete(operation_id, "test_user")
            print(f"Операция помечена как удаленная: {deleted}")

            all_after_delete = repo.findAll()
            print(f"Операций после удаления (обычный режим): {len(all_after_delete)}")

            repo.setIncludeDeleted(True)
            all_with_deleted = repo.findAll()
            print(f"Операций после удаления (включая удаленные): {len(all_with_deleted)}")

            found_deleted = repo.findById(operation_id)
            if found_deleted.isPresent():
                deleted_op = found_deleted.get()
                print(f"Найдена удаленная операция: {deleted_op.getComment()}")
                print(f"Deleted by: {deleted_op.getDeletedBy()}")
                print(f"Delete time: {deleted_op.getDeleteTime()}")
            else:
                print("❌ Удаленная операция не найдена")

            # Тестируем метод findDeleted
            print("\n--- Тест findDeleted ---")
            deleted_operations = repo.findDeleted()
            print(f"Найдено удаленных операций: {len(deleted_operations)}")
            for op in deleted_operations:
                type_str = "Расход" if op.getType() == 1 else "Доход"
                print(f"  Удаленная операция: {op.getComment()}, {type_str}, Deleted by: {op.getDeletedBy()}")

            # Тестируем восстановление через метод restore
            print("\n--- Тест восстановления через restore ---")
            try:
                # Восстанавливаем через метод restore
            restored = repo.restore(operation_id)
            print(f"Операция восстановлена через restore: {restored}")

                # Проверяем, что операция снова видна в обычном режиме
                repo.setIncludeDeleted(False)
            all_after_restore = repo.findAll()
                print(f"Операций после восстановления через restore: {len(all_after_restore)}")

                # Проверяем восстановленную запись
            found_restored = repo.findById(operation_id)
            if found_restored.isPresent():
                restored_op = found_restored.get()
                print(f"Найдена восстановленная операция: {restored_op.getComment()}")
                print(f"Delete time после восстановления: {restored_op.getDeleteTime()}")
                print(f"Deleted by после восстановления: {restored_op.getDeletedBy()}")
            else:
                print("❌ Восстановленная операция не найдена")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через restore: {e}")
                import traceback
                traceback.print_exc()

        except Exception as e:
            print(f"❌ Ошибка при soft delete: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем создание операции перевода
        print("\n--- Тест создания операции перевода ---")
        try:
            transfer_operation = create_test_entity(
                Operation,
                type=3,  # Перевод
                amount=1000,
                date=LocalDateTime.of(2023, 2, 1, 10, 0, 0),
                comment="Перевод между счетами",
                categoryId=1,
                accountId=1,
                currencyId=1,
                toAccountId=2,
                toCurrencyId=1,
                toAmount=1000,
                createdBy="transfer_test",
                updatedBy="transfer_test",
            )

            saved_transfer = repo.save(transfer_operation)
            test_data_manager.add_test_id("operations", saved_transfer.getId())
            print(f"Создана операция перевода: {saved_transfer.getComment()}")
            print(f"From Account: {saved_transfer.getAccountId()}, To Account: {saved_transfer.getToAccountId()}")
            print(f"From Amount: {saved_transfer.getAmount()}, To Amount: {saved_transfer.getToAmount()}")

        except Exception as e:
            print(f"❌ Ошибка при создании операции перевода: {e}")
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
