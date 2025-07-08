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

            # Восстановление через restore
            print("\n--- Тест восстановления через restore ---")
            restored = repo.restore(operation_id)
            print(f"Операция восстановлена через restore: {restored}")

            all_after_restore = repo.findAll()
            print(f"Операций после восстановления: {len(all_after_restore)}")

            found_restored = repo.findById(operation_id)
            if found_restored.isPresent():
                restored_op = found_restored.get()
                print(f"Найдена восстановленная операция: {restored_op.getComment()}")
                print(f"Delete time после восстановления: {restored_op.getDeleteTime()}")
                print(f"Deleted by после восстановления: {restored_op.getDeletedBy()}")
            else:
                print("❌ Восстановленная операция не найдена")

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
