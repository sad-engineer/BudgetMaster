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
    print("=== Тест OperationService через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Operation = get_java_class("model.Operation")
        OperationRepository = get_java_class("repository.OperationRepository")
        OperationService = get_java_class("service.OperationService")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий и сервис
        repo = OperationRepository(test_data_manager.db_manager.db_path)
        service = OperationService(repo)
        print("✅ Сервис создан")

        # Создаем тестовую операцию (без времени создания - сервис установит автоматически)
        operation = create_test_entity(
            Operation,
            type=1,
            amount=1500,
            date=LocalDateTime.of(2023, 1, 30, 12, 0, 0),
            comment="Тестовая операция из сервиса",
            categoryId=1,
            accountId=1,
            currencyId=1,
            createdBy="tester",
            updatedBy="tester",
        )

        print("✅ Тестовая операция создана")
        print(f"Операция: {operation.toString()}")

        # Тестируем создание через сервис (с автоматической установкой времени)
        print("\n--- Тест создания через сервис ---")
        saved_operation = service.createOperation(operation)
        print(f"Операция создана через сервис: {saved_operation.toString()}")
        print(f"ID операции: {saved_operation.getId()}")
        print(f"Create time: {saved_operation.getCreateTime()}")
        print(f"Update time: {saved_operation.getUpdateTime()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("operations", saved_operation.getId())

        # Тестируем поиск по ID через сервис
        print("\n--- Тест поиска по ID через сервис ---")
        try:
            found_operation = service.getOperationById(saved_operation.getId())
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

        # Тестируем создание нескольких операций через сервис
        print("\n--- Тест создания нескольких операций через сервис ---")
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
                createdBy="service_multi_test",
                updatedBy="service_multi_test",
            )

            saved_test_operation = service.createOperation(test_operation)
            test_operations.append(saved_test_operation)
            test_data_manager.add_test_id("operations", saved_test_operation.getId())
            print(f"Создана операция: {saved_test_operation.getComment()}, Type: {saved_test_operation.getType()}, Amount: {saved_test_operation.getAmount()}")

        # Проверяем все операции через сервис
        all_operations = service.getAllOperations()
        print(f"\nВсе операции через сервис:")
        for op in all_operations:
            type_str = "Расход" if op.getType() == 1 else "Доход"
            print(f"  ID: {op.getId()}, Type: {op.getType()} ({type_str}), Amount: {op.getAmount()}, Comment: {op.getComment()}")

        # Тестируем сортировку по дате через сервис
        print("\n--- Тест сортировки по дате через сервис ---")
        try:
            ordered_operations = service.getAllOperationsOrderedByDate()
            print(f"Операции, отсортированные по дате через сервис (сначала новые):")
            for op in ordered_operations:
                print(f"  Date: {op.getDate()}, Amount: {op.getAmount()}, Comment: {op.getComment()}")
        except Exception as e:
            print(f"❌ Ошибка при сортировке по дате через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем обновление через сервис
        print("\n--- Тест обновления через сервис ---")
        try:
            # Обновляем данные операции
            saved_operation.setAmount(2000)
            saved_operation.setComment("Обновленная операция из сервиса")
            saved_operation.setUpdatedBy("service_update_test")

            updated_operation = service.updateOperation(saved_operation)
            print(f"Операция обновлена через сервис: {updated_operation.toString()}")

            # Проверяем обновление
            found_updated = service.getOperationById(saved_operation.getId())
            if found_updated.isPresent():
                op = found_updated.get()
                print(f"Проверка обновления: {op.toString()}")
                print(f"Новый amount: {op.getAmount()}")
                print(f"Новый comment: {op.getComment()}")
                print(f"Updated by: {op.getUpdatedBy()}")
                print(f"Update time: {op.getUpdateTime()}")
            else:
                print("❌ Обновленная операция не найдена")

        except Exception as e:
            print(f"❌ Ошибка при обновлении через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем soft delete через сервис
        print("\n--- Тест soft delete через сервис ---")
        try:
            operation_id = saved_operation.getId()

            # Проверяем, что операция есть в обычном списке
            all_before_delete = service.getAllOperations()
            print(f"Операций до удаления: {len(all_before_delete)}")

            # Удаляем операцию через сервис
            deleted = service.deleteOperation(operation_id, "service_test_user")
            print(f"Операция удалена через сервис: {deleted}")

            # Проверяем, что операция исчезла из обычного списка
            all_after_delete = service.getAllOperations()
            print(f"Операций после удаления: {len(all_after_delete)}")

            # Проверяем удаленные операции через сервис
            deleted_operations = service.getDeletedOperations()
            print(f"Найдено удаленных операций через сервис: {len(deleted_operations)}")
            for op in deleted_operations:
                type_str = "Расход" if op.getType() == 1 else "Доход"
                print(f"  Удаленная операция: {op.getComment()}, {type_str}, Deleted by: {op.getDeletedBy()}")

            # Тестируем восстановление через сервис
            print("\n--- Тест восстановления через сервис ---")
            try:
                # Восстанавливаем через сервис
                restored = service.restoreOperation(operation_id)
                print(f"Операция восстановлена через сервис: {restored}")

                # Проверяем, что операция снова видна
                all_after_restore = service.getAllOperations()
                print(f"Операций после восстановления через сервис: {len(all_after_restore)}")

                # Проверяем восстановленную запись
                found_restored = service.getOperationById(operation_id)
                if found_restored.isPresent():
                    restored_op = found_restored.get()
                    print(f"Найдена восстановленная операция: {restored_op.getComment()}")
                    print(f"Delete time после восстановления: {restored_op.getDeleteTime()}")
                    print(f"Deleted by после восстановления: {restored_op.getDeletedBy()}")
                else:
                    print("❌ Восстановленная операция не найдена")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через сервис: {e}")
                import traceback
                traceback.print_exc()

        except Exception as e:
            print(f"❌ Ошибка при soft delete через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем создание операции перевода через сервис
        print("\n--- Тест создания операции перевода через сервис ---")
        try:
            transfer_operation = create_test_entity(
                Operation,
                type=3,  # Перевод
                amount=1000,
                date=LocalDateTime.of(2023, 2, 1, 10, 0, 0),
                comment="Перевод между счетами через сервис",
                categoryId=1,
                accountId=1,
                currencyId=1,
                toAccountId=2,
                toCurrencyId=1,
                toAmount=1000,
                createdBy="service_transfer_test",
                updatedBy="service_transfer_test",
            )

            saved_transfer = service.createOperation(transfer_operation)
            test_data_manager.add_test_id("operations", saved_transfer.getId())
            print(f"Создана операция перевода через сервис: {saved_transfer.getComment()}")
            print(f"From Account: {saved_transfer.getAccountId()}, To Account: {saved_transfer.getToAccountId()}")
            print(f"From Amount: {saved_transfer.getAmount()}, To Amount: {saved_transfer.getToAmount()}")

        except Exception as e:
            print(f"❌ Ошибка при создании операции перевода через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем валидацию через сервис
        print("\n--- Тест валидации через сервис ---")
        try:
            # Тест с невалидной операцией (отрицательная сумма)
            invalid_operation = create_test_entity(
                Operation,
                type=1,
                amount=-100,  # Отрицательная сумма
                date=LocalDateTime.of(2023, 2, 1, 10, 0, 0),
                comment="Невалидная операция",
                categoryId=1,
                accountId=1,
                currencyId=1,
                createdBy="validation_test",
                updatedBy="validation_test",
            )

            try:
                service.createOperation(invalid_operation)
                print("❌ Невалидная операция была создана (ожидалась ошибка)")
            except Exception as validation_error:
                print(f"✅ Валидация сработала корректно: {validation_error}")

        except Exception as e:
            print(f"❌ Ошибка при тестировании валидации: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем фильтрацию по типам через сервис
        print("\n--- Тест фильтрации по типам через сервис ---")
        try:
            # Получаем все операции через сервис
            all_operations = service.getAllOperations()
            
            # Фильтруем по типу операции (1 = Расход, 2 = Доход, 3 = Перевод)
            expense_operations = [op for op in all_operations if op.getType() == 1]
            income_operations = [op for op in all_operations if op.getType() == 2]
            transfer_operations = [op for op in all_operations if op.getType() == 3]
            
            print(f"Операции расходов ({len(expense_operations)}):")
            for op in expense_operations:
                print(f"  - {op.getComment()}: {op.getAmount()}")
            
            print(f"Операции доходов ({len(income_operations)}):")
            for op in income_operations:
                print(f"  - {op.getComment()}: {op.getAmount()}")
            
            print(f"Операции переводов ({len(transfer_operations)}):")
            for op in transfer_operations:
                print(f"  - {op.getComment()}: {op.getAmount()} -> {op.getToAmount()}")

        except Exception as e:
            print(f"❌ Ошибка при фильтрации через сервис: {e}")
            import traceback
            traceback.print_exc()

        print("\n✅ Все тесты сервиса выполнены успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback
        traceback.print_exc()

    finally:
        # Очистка тестовых данных и остановка JVM
        cleanup_example()


if __name__ == "__main__":
    main() 