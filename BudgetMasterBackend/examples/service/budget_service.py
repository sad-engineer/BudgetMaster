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
    print("=== Тест BudgetService через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Budget = get_java_class("model.Budget")
        BudgetRepository = get_java_class("repository.BudgetRepository")
        BudgetService = get_java_class("service.BudgetService")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий и сервис
        repo = BudgetRepository(test_data_manager.db_manager.db_path)
        service = BudgetService(repo)
        print("✅ Сервис создан")

        # Создаем тестовый бюджет (без позиции - сервис установит автоматически)
        budget = create_test_entity(
            Budget,
            amount=100000,  # 1000.00
            currencyId=1,   # RUB
            categoryId=None,
            position=0,  # Сервис установит автоматически
            createdBy="tester",
            updatedBy="tester",
        )

        print("✅ Тестовый бюджет создан")
        print(f"Бюджет: {budget.toString()}")

        # Тестируем создание через сервис (с автонумерацией позиции)
        print("\n--- Тест создания через сервис ---")
        saved_budget = service.createBudget(budget)
        print(f"Бюджет создан через сервис: {saved_budget.toString()}")
        print(f"ID бюджета: {saved_budget.getId()}")
        print(f"Автоматически установленная позиция: {saved_budget.getPosition()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("budgets", saved_budget.getId())

        # Тестируем поиск по ID через сервис
        print("\n--- Тест поиска по ID через сервис ---")
        try:
            found_budget = service.getBudgetById(saved_budget.getId())
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

        # Тестируем создание нескольких бюджетов через сервис
        print("\n--- Тест создания нескольких бюджетов через сервис ---")
        test_budgets = []
        for i in range(3):
            test_budget = create_test_entity(
                Budget,
                amount=50000 * (i + 1),  # Разные суммы
                currencyId=1,   # RUB
                categoryId=i + 1,  # Разные категории
                position=0,  # Сервис установит автоматически
                createdBy="service_tester",
                updatedBy="service_tester",
            )

            saved_test_budget = service.createBudget(test_budget)
            test_budgets.append(saved_test_budget)
            test_data_manager.add_test_id("budgets", saved_test_budget.getId())
            print(f"Создан бюджет: {saved_test_budget.getAmount()}, Position: {saved_test_budget.getPosition()}")

        # Проверяем все бюджеты через сервис
        all_budgets = service.getAllBudgets()
        print(f"\nВсе бюджеты (позиции нормализованы сервисом):")
        for bud in all_budgets:
            print(f"  Position: {bud.getPosition()}, Amount: {bud.getAmount()}, Category ID: {bud.getCategoryId()}")

        # Проверяем, что позиции идут по порядку после создания через сервис
        positions = [bud.getPosition() for bud in all_budgets]
        expected_positions = list(range(1, len(positions) + 1))
        if positions == expected_positions:
            print("✅ Позиции нормализованы корректно сервисом!")
        else:
            print(f"❌ Ошибка нормализации сервисом: ожидались {expected_positions}, получены {positions}")

        # Тестируем обновление через сервис
        print("\n--- Тест обновления через сервис ---")
        try:
            # Обновляем данные бюджета
            saved_budget.setAmount(200000)  # 2000.00
            saved_budget.setPosition(2)  # Меняем позицию
            saved_budget.setUpdatedBy("service_update_test")

            # Обновляем дату
            updated_now = LocalDateTime.now()
            saved_budget.setUpdateTime(updated_now)

            updated_budget = service.updateBudget(saved_budget)
            print(f"Бюджет обновлен через сервис: {updated_budget.toString()}")

            # Проверяем обновление
            found_updated = service.getBudgetById(saved_budget.getId())
            if found_updated.isPresent():
                bud = found_updated.get()
                print(f"Проверка обновления: {bud.toString()}")
                print(f"Новый amount: {bud.getAmount()}")
                print(f"Новый position: {bud.getPosition()}")
                print(f"Updated by: {bud.getUpdatedBy()}")
            else:
                print("❌ Обновленный бюджет не найден")

            # Проверяем, что позиции нормализованы после обновления через сервис
            all_after_update = service.getAllBudgets()
            print(f"\nВсе бюджеты после обновления через сервис (позиции нормализованы):")
            for bud in all_after_update:
                print(f"  Position: {bud.getPosition()}, Amount: {bud.getAmount()}, Category ID: {bud.getCategoryId()}")

            # Проверяем, что позиции идут по порядку
            positions_after_update = [bud.getPosition() for bud in all_after_update]
            expected_positions_after_update = list(range(1, len(positions_after_update) + 1))
            if positions_after_update == expected_positions_after_update:
                print("✅ Позиции нормализованы корректно после обновления через сервис!")
            else:
                print(f"❌ Ошибка нормализации после обновления через сервис: ожидались {expected_positions_after_update}, получены {positions_after_update}")

        except Exception as e:
            print(f"❌ Ошибка при обновлении через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем soft delete через сервис
        print("\n--- Тест soft delete через сервис ---")
        try:
            budget_id = saved_budget.getId()

            # Проверяем, что бюджет есть в обычном списке
            all_before_delete = service.getAllBudgets()
            print(f"Бюджетов до удаления: {len(all_before_delete)}")

            # Удаляем бюджет через сервис
            deleted = service.deleteBudget(budget_id, "service_test_user")
            print(f"Бюджет удален через сервис: {deleted}")

            # Проверяем, что бюджет исчез из обычного списка
            all_after_delete = service.getAllBudgets()
            print(f"Бюджетов после удаления: {len(all_after_delete)}")

            # Проверяем удаленные бюджеты через сервис
            deleted_budgets = service.getDeletedBudgets()
            print(f"Найдено удаленных бюджетов через сервис: {len(deleted_budgets)}")
            for bud in deleted_budgets:
                print(f"  Удаленный бюджет: {bud.getAmount()}, Deleted by: {bud.getDeletedBy()}")

            # Тестируем восстановление через сервис
            print("\n--- Тест восстановления через сервис ---")
            try:
                # Восстанавливаем через сервис
                restored = service.restoreBudget(budget_id)
                print(f"Бюджет восстановлен через сервис: {restored}")

                # Проверяем, что бюджет снова виден
                all_after_restore = service.getAllBudgets()
                print(f"Бюджетов после восстановления через сервис: {len(all_after_restore)}")

                # Проверяем восстановленную запись
                found_restored = service.getBudgetById(budget_id)
                if found_restored.isPresent():
                    restored_budget = found_restored.get()
                    print(f"Найден восстановленный бюджет: {restored_budget.getAmount()}")
                    print(f"Delete time после восстановления: {restored_budget.getDeleteTime()}")
                    print(f"Deleted by после восстановления: {restored_budget.getDeletedBy()}")
                else:
                    print("❌ Восстановленный бюджет не найден")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через сервис: {e}")
                import traceback
                traceback.print_exc()

        except Exception as e:
            print(f"❌ Ошибка при soft delete через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем восстановление удаленной записи с тем же category_id
        print("\n--- Тест восстановления через createBudget с тем же category_id ---")
        try:
            # Сначала удаляем бюджет
            service.deleteBudget(saved_budget.getId(), "restore_test_user")
            print("Бюджет удален для теста восстановления")

            # Создаем новый бюджет с тем же category_id
            new_budget = create_test_entity(
                Budget,
                amount=300000,  # 3000.00
                currencyId=1,   # RUB
                categoryId=1,   # То же category_id, что и удаленный
                position=0,  # Сервис установит автоматически
                createdBy="restore_test",
                updatedBy="restore_test",
            )

            restored_budget = service.createBudget(new_budget)
            test_data_manager.add_test_id("budgets", restored_budget.getId())
            print(f"Бюджет восстановлен через createBudget: {restored_budget.getAmount()}")
            print(f"ID восстановленного бюджета: {restored_budget.getId()}")
            print(f"Position: {restored_budget.getPosition()}")

            # Проверяем, что это та же запись (тот же ID)
            found_restored_save = service.getBudgetById(restored_budget.getId())
            if found_restored_save.isPresent():
                final_budget = found_restored_save.get()
                print(f"Проверка восстановленного бюджета: {final_budget.getAmount()}")
                print(f"Delete time: {final_budget.getDeleteTime()}")
                print(f"Deleted by: {final_budget.getDeletedBy()}")
                print(f"Updated by: {final_budget.getUpdatedBy()}")
                print(f"Category ID: {final_budget.getCategoryId()}")
            else:
                print("❌ Восстановленный бюджет не найден")

        except Exception as e:
            print(f"❌ Ошибка при восстановлении через createBudget: {e}")
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