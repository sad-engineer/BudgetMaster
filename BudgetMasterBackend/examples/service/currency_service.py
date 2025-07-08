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
    print("=== Тест CurrencyService через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Currency = get_java_class("model.Currency")
        CurrencyRepository = get_java_class("repository.CurrencyRepository")
        CurrencyService = get_java_class("service.CurrencyService")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий и сервис
        repo = CurrencyRepository(test_data_manager.db_manager.db_path)
        service = CurrencyService(repo)
        print("✅ Сервис создан")

        # Создаем тестовую валюту (без позиции - сервис установит автоматически)
        currency = create_test_entity(
            Currency, 
            title="Доллар США", 
            position=0,  # Сервис установит автоматически
            createdBy="tester", 
            updatedBy="tester"
        )

        print("✅ Тестовая валюта создана")
        print(f"Валюта: {currency.toString()}")

        # Тестируем создание через сервис (с автонумерацией позиции)
        print("\n--- Тест создания через сервис ---")
        saved_currency = service.createCurrency(currency)
        print(f"Валюта создана через сервис: {saved_currency.toString()}")
        print(f"ID валюты: {saved_currency.getId()}")
        print(f"Автоматически установленная позиция: {saved_currency.getPosition()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("currencies", saved_currency.getId())

        # Тестируем поиск по ID через сервис
        print("\n--- Тест поиска по ID через сервис ---")
        try:
            found_currency = service.getCurrencyById(saved_currency.getId())
            if found_currency.isPresent():
                curr = found_currency.get()
                print(f"Найдена валюта: {curr.toString()}")
                print(f"ID: {curr.getId()}")
                print(f"Position: {curr.getPosition()}")
                print(f"Title: {curr.getTitle()}")
                print(f"Created by: {curr.getCreatedBy()}")
            else:
                print("❌ Валюта не найдена")
        except Exception as e:
            print(f"❌ Ошибка при поиске: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем создание нескольких валют через сервис
        print("\n--- Тест создания нескольких валют через сервис ---")
        test_currencies = []
        for i in range(3):
            test_currency = create_test_entity(
                Currency,
                title=f"Тестовая валюта {i + 1}",
                position=0,  # Сервис установит автоматически
                createdBy="service_tester",
                updatedBy="service_tester",
            )

            saved_test_currency = service.createCurrency(test_currency)
            test_currencies.append(saved_test_currency)
            test_data_manager.add_test_id("currencies", saved_test_currency.getId())
            print(f"Создана валюта: {saved_test_currency.getTitle()}, Position: {saved_test_currency.getPosition()}")

        # Проверяем все валюты через сервис
        all_currencies = service.getAllCurrencies()
        print(f"\nВсе валюты (позиции нормализованы сервисом):")
        for curr in all_currencies:
            print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")

        # Проверяем, что позиции идут по порядку после создания через сервис
        positions = [curr.getPosition() for curr in all_currencies]
        expected_positions = list(range(1, len(positions) + 1))
        if positions == expected_positions:
            print("✅ Позиции нормализованы корректно сервисом!")
        else:
            print(f"❌ Ошибка нормализации сервисом: ожидались {expected_positions}, получены {positions}")

        # Тестируем обновление через сервис
        print("\n--- Тест обновления через сервис ---")
        try:
            # Обновляем данные валюты
            saved_currency.setTitle("Доллар США (обновленный через сервис)")
            saved_currency.setPosition(2)  # Меняем позицию
            saved_currency.setUpdatedBy("service_update_test")

            # Обновляем дату
            updated_now = LocalDateTime.now()
            saved_currency.setUpdateTime(updated_now)

            updated_currency = service.updateCurrency(saved_currency)
            print(f"Валюта обновлена через сервис: {updated_currency.toString()}")

            # Проверяем обновление
            found_updated = service.getCurrencyById(saved_currency.getId())
            if found_updated.isPresent():
                curr = found_updated.get()
                print(f"Проверка обновления: {curr.toString()}")
                print(f"Новый title: {curr.getTitle()}")
                print(f"Новый position: {curr.getPosition()}")
                print(f"Updated by: {curr.getUpdatedBy()}")
            else:
                print("❌ Обновленная валюта не найдена")

            # Проверяем, что позиции нормализованы после обновления через сервис
            all_after_update = service.getAllCurrencies()
            print(f"\nВсе валюты после обновления через сервис (позиции нормализованы):")
            for curr in all_after_update:
                print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")

            # Проверяем, что позиции идут по порядку
            positions_after_update = [curr.getPosition() for curr in all_after_update]
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
            currency_id = saved_currency.getId()

            # Проверяем, что валюта есть в обычном списке
            all_before_delete = service.getAllCurrencies()
            print(f"Валют до удаления: {len(all_before_delete)}")

            # Удаляем валюту через сервис
            deleted = service.deleteCurrency(currency_id, "service_test_user")
            print(f"Валюта удалена через сервис: {deleted}")

            # Проверяем, что валюта исчезла из обычного списка
            all_after_delete = service.getAllCurrencies()
            print(f"Валют после удаления: {len(all_after_delete)}")

            # Проверяем удаленные валюты через сервис
            deleted_currencies = service.getDeletedCurrencies()
            print(f"Найдено удаленных валют через сервис: {len(deleted_currencies)}")
            for curr in deleted_currencies:
                print(f"  Удаленная валюта: {curr.getTitle()}, Deleted by: {curr.getDeletedBy()}")

            # Тестируем восстановление через сервис
            print("\n--- Тест восстановления через сервис ---")
            try:
                # Восстанавливаем через сервис
                restored = service.restoreCurrency(currency_id)
                print(f"Валюта восстановлена через сервис: {restored}")

                # Проверяем, что валюта снова видна
                all_after_restore = service.getAllCurrencies()
                print(f"Валют после восстановления через сервис: {len(all_after_restore)}")

                # Проверяем восстановленную запись
                found_restored = service.getCurrencyById(currency_id)
                if found_restored.isPresent():
                    restored_currency = found_restored.get()
                    print(f"Найдена восстановленная валюта: {restored_currency.getTitle()}")
                    print(f"Delete time после восстановления: {restored_currency.getDeleteTime()}")
                    print(f"Deleted by после восстановления: {restored_currency.getDeletedBy()}")
                else:
                    print("❌ Восстановленная валюта не найдена")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через сервис: {e}")
                import traceback
                traceback.print_exc()

        except Exception as e:
            print(f"❌ Ошибка при soft delete через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем восстановление удаленной записи с тем же title
        print("\n--- Тест восстановления через createCurrency с тем же title ---")
        try:
            # Сначала удаляем валюту
            service.deleteCurrency(saved_currency.getId(), "restore_test_user")
            print("Валюта удалена для теста восстановления")

            # Создаем новую валюту с тем же названием
            new_currency = create_test_entity(
                Currency,
                title="Доллар США (обновленный через сервис)",  # То же название, что и удаленная
                position=0,  # Сервис установит автоматически
                createdBy="restore_test",
                updatedBy="restore_test",
            )

            restored_currency = service.createCurrency(new_currency)
            test_data_manager.add_test_id("currencies", restored_currency.getId())
            print(f"Валюта восстановлена через createCurrency: {restored_currency.getTitle()}")
            print(f"ID восстановленной валюты: {restored_currency.getId()}")
            print(f"Position: {restored_currency.getPosition()}")

            # Проверяем, что это та же запись (тот же ID)
            found_restored_save = service.getCurrencyById(restored_currency.getId())
            if found_restored_save.isPresent():
                final_currency = found_restored_save.get()
                print(f"Проверка восстановленной валюты: {final_currency.getTitle()}")
                print(f"Delete time: {final_currency.getDeleteTime()}")
                print(f"Deleted by: {final_currency.getDeletedBy()}")
                print(f"Updated by: {final_currency.getUpdatedBy()}")
            else:
                print("❌ Восстановленная валюта не найдена")

        except Exception as e:
            print(f"❌ Ошибка при восстановлении через createCurrency: {e}")
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