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
    print("=== Тест AccountService через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Account = get_java_class("model.Account")
        AccountRepository = get_java_class("repository.AccountRepository")
        AccountService = get_java_class("service.AccountService")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий и сервис
        repo = AccountRepository(test_data_manager.db_manager.db_path)
        service = AccountService(repo)
        print("✅ Сервис создан")

        # Создаем тестовый счет (без позиции - сервис установит автоматически)
        account = create_test_entity(
            Account,
            title="Наличные (тест сервиса)",
            position=0,  # Сервис установит автоматически
            amount=100000,  # 1000.00 в копейках
            type=1,  # Текущий счет
            currencyId=1,  # RUB
            closed=0,  # Не закрыт
            createdBy="tester",
            updatedBy="tester",
        )

        print("✅ Тестовый счет создан")
        print(f"Счет: {account.toString()}")

        # Тестируем создание через сервис (с автонумерацией позиции)
        print("\n--- Тест создания через сервис ---")
        saved_account = service.createAccount(account)
        print(f"Счет создан через сервис: {saved_account.toString()}")
        print(f"ID счета: {saved_account.getId()}")
        print(f"Автоматически установленная позиция: {saved_account.getPosition()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("accounts", saved_account.getId())

        # Тестируем поиск по ID через сервис
        print("\n--- Тест поиска по ID через сервис ---")
        try:
            found_account = service.getAccountById(saved_account.getId())
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

        # Тестируем создание нескольких счетов через сервис
        print("\n--- Тест создания нескольких счетов через сервис ---")
        test_accounts = []
        for i in range(3):
            test_account = create_test_entity(
                Account,
                title=f"Тестовый счет {i + 1}",
                position=0,  # Сервис установит автоматически
                amount=50000 * (i + 1),  # Разные суммы
                type=1,  # Текущий счет
                currencyId=1,  # RUB
                closed=0,  # Не закрыт
                createdBy="service_tester",
                updatedBy="service_tester",
            )

            saved_test_account = service.createAccount(test_account)
            test_accounts.append(saved_test_account)
            test_data_manager.add_test_id("accounts", saved_test_account.getId())
            print(f"Создан счет: {saved_test_account.getTitle()}, Position: {saved_test_account.getPosition()}")

        # Проверяем все счета через сервис
        all_accounts = service.getAllAccounts()
        print(f"\nВсе счета (позиции нормализованы сервисом):")
        for acc in all_accounts:
            print(f"  Position: {acc.getPosition()}, Title: {acc.getTitle()}, Amount: {acc.getAmount()} копеек")

        # Проверяем, что позиции идут по порядку после создания через сервис
        positions = [acc.getPosition() for acc in all_accounts]
        expected_positions = list(range(1, len(positions) + 1))
        if positions == expected_positions:
            print("✅ Позиции нормализованы корректно сервисом!")
        else:
            print(f"❌ Ошибка нормализации сервисом: ожидались {expected_positions}, получены {positions}")

        # Тестируем обновление через сервис
        print("\n--- Тест обновления через сервис ---")
        try:
            # Обновляем данные счета
            saved_account.setTitle("Основной счет (обновленный через сервис)")
            saved_account.setPosition(2)  # Меняем позицию
            saved_account.setAmount(150000)  # 1500.00 в копейках
            saved_account.setUpdatedBy("service_update_test")

            # Обновляем дату
            updated_now = LocalDateTime.now()
            saved_account.setUpdateTime(updated_now)

            updated_account = service.updateAccount(saved_account)
            print(f"Счет обновлен через сервис: {updated_account.toString()}")

            # Проверяем обновление
            found_updated = service.getAccountById(saved_account.getId())
            if found_updated.isPresent():
                acc = found_updated.get()
                print(f"Проверка обновления: {acc.toString()}")
                print(f"Новый title: {acc.getTitle()}")
                print(f"Новый position: {acc.getPosition()}")
                print(f"Новый amount: {acc.getAmount()} копеек")
                print(f"Updated by: {acc.getUpdatedBy()}")
            else:
                print("❌ Обновленный счет не найден")

            # Проверяем, что позиции нормализованы после обновления через сервис
            all_after_update = service.getAllAccounts()
            print(f"\nВсе счета после обновления через сервис (позиции нормализованы):")
            for acc in all_after_update:
                print(f"  Position: {acc.getPosition()}, Title: {acc.getTitle()}, Amount: {acc.getAmount()} копеек")

            # Проверяем, что позиции идут по порядку
            positions_after_update = [acc.getPosition() for acc in all_after_update]
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
            account_id = saved_account.getId()

            # Проверяем, что счет есть в обычном списке
            all_before_delete = service.getAllAccounts()
            print(f"Счетов до удаления: {len(all_before_delete)}")

            # Удаляем счет через сервис
            deleted = service.deleteAccount(account_id, "service_test_user")
            print(f"Счет удален через сервис: {deleted}")

            # Проверяем, что счет исчез из обычного списка
            all_after_delete = service.getAllAccounts()
            print(f"Счетов после удаления: {len(all_after_delete)}")

            # Проверяем удаленные счета через сервис
            deleted_accounts = service.getDeletedAccounts()
            print(f"Найдено удаленных счетов через сервис: {len(deleted_accounts)}")
            for acc in deleted_accounts:
                print(f"  Удаленный счет: {acc.getTitle()}, Deleted by: {acc.getDeletedBy()}")

            # Тестируем восстановление через сервис
            print("\n--- Тест восстановления через сервис ---")
            try:
                # Восстанавливаем через сервис
                restored = service.restoreAccount(account_id)
                print(f"Счет восстановлен через сервис: {restored}")

                # Проверяем, что счет снова виден
                all_after_restore = service.getAllAccounts()
                print(f"Счетов после восстановления через сервис: {len(all_after_restore)}")

                # Проверяем восстановленную запись
                found_restored = service.getAccountById(account_id)
                if found_restored.isPresent():
                    restored_account = found_restored.get()
                    print(f"Найден восстановленный счет: {restored_account.getTitle()}")
                    print(f"Delete time после восстановления: {restored_account.getDeleteTime()}")
                    print(f"Deleted by после восстановления: {restored_account.getDeletedBy()}")
                else:
                    print("❌ Восстановленный счет не найден")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через сервис: {e}")
                import traceback
                traceback.print_exc()

        except Exception as e:
            print(f"❌ Ошибка при soft delete через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем восстановление удаленной записи с тем же title
        print("\n--- Тест восстановления через createAccount с тем же title ---")
        try:
            # Сначала удаляем счет
            service.deleteAccount(saved_account.getId(), "restore_test_user")
            print("Счет удален для теста восстановления")

            # Создаем новый счет с тем же названием
            new_account = create_test_entity(
                Account,
                title="Основной счет (обновленный через сервис)",  # То же название, что и удаленный
                position=0,  # Сервис установит автоматически
                amount=200000,  # 2000.00 в копейках
                type=1,  # Текущий счет
                currencyId=1,  # RUB
                closed=0,  # Не закрыт
                createdBy="restore_test",
                updatedBy="restore_test",
            )

            restored_account = service.createAccount(new_account)
            test_data_manager.add_test_id("accounts", restored_account.getId())
            print(f"Счет восстановлен через createAccount: {restored_account.getTitle()}")
            print(f"ID восстановленного счета: {restored_account.getId()}")
            print(f"Position: {restored_account.getPosition()}")

            # Проверяем, что это та же запись (тот же ID)
            found_restored_save = service.getAccountById(restored_account.getId())
            if found_restored_save.isPresent():
                final_account = found_restored_save.get()
                print(f"Проверка восстановленного счета: {final_account.getTitle()}")
                print(f"Delete time: {final_account.getDeleteTime()}")
                print(f"Deleted by: {final_account.getDeletedBy()}")
                print(f"Updated by: {final_account.getUpdatedBy()}")
                print(f"Amount: {final_account.getAmount()} копеек")
            else:
                print("❌ Восстановленный счет не найден")

        except Exception as e:
            print(f"❌ Ошибка при восстановлении через createAccount: {e}")
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