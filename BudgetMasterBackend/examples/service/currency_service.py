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

        # Создаем репозиторий и сервис с пользователем
        repo = CurrencyRepository(test_data_manager.db_manager.db_path)
        service = CurrencyService(repo, "test_user")
        print("✅ Сервис создан с пользователем 'test_user'")

        # Тестируем создание валюты через сервис
        print("\n--- Тест создания валюты через сервис ---")
        created_currency = service.create("Евро")
        print(f"Валюта создана: {created_currency.toString()}")
        print(f"ID валюты: {created_currency.getId()}")
        print(f"Автоматически установленная позиция: {created_currency.getPosition()}")
        print(f"Created by: {created_currency.getCreatedBy()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("currencies", created_currency.getId())

        # Тестируем поиск по ID через сервис
        print("\n--- Тест поиска по ID через сервис ---")
        try:
            found_currency = service.getById(created_currency.getId())
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
            currency_name = f"Тестовая валюта {i + 1}"
            saved_currency = service.create(currency_name)
            test_currencies.append(saved_currency)
            test_data_manager.add_test_id("currencies", saved_currency.getId())
            print(f"Создана валюта: {saved_currency.getTitle()}, Position: {saved_currency.getPosition()}")

        # Проверяем все валюты через сервис
        all_currencies = service.getAll()
        print(f"\nВсе валюты:")
        for curr in all_currencies:
            print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")

        # Тестируем получение валюты по названию (создание если не существует)
        print("\n--- Тест получения валюты по названию ---")
        try:
            # Получаем существующую валюту
            existing_currency = service.get("Евро")
            print(f"Получена существующая валюта: {existing_currency.getTitle()}")
            
            # Получаем несуществующую валюту (создается автоматически)
            new_currency = service.get("Рубль")
            print(f"Создана новая валюта: {new_currency.getTitle()}, Position: {new_currency.getPosition()}")
            test_data_manager.add_test_id("currencies", new_currency.getId())
            
        except Exception as e:
            print(f"❌ Ошибка при получении валюты по названию: {e}")

        # Тестируем изменение позиции
        print("\n--- Тест изменения позиции ---")
        try:
            # Меняем позицию валюты
            currency_to_move = all_currencies[0]
            old_position = currency_to_move.getPosition()
            new_position = 3
            
            moved_currency = service.changePosition(currency_to_move, new_position)
            print(f"Валюта перемещена с позиции {old_position} на позицию {new_position}")
            print(f"Updated by: {moved_currency.getUpdatedBy()}")
            
            # Проверяем результат
            all_after_move = service.getAll()
            print(f"\nВсе валюты после перемещения:")
            for curr in all_after_move:
                print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")
                
        except Exception as e:
            print(f"❌ Ошибка при изменении позиции: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем soft delete через сервис
        print("\n--- Тест soft delete через сервис ---")
        try:
            currency_id = created_currency.getId()

            # Проверяем, что валюта есть в списке
            all_before_delete = service.getAll()
            print(f"Валют до удаления: {len(all_before_delete)}")

            # Удаляем валюту через сервис
            deleted = service.delete("Евро")
            print(f"Валюта удалена через сервис: {deleted}")

            # Проверяем, что валюта все еще в списке (включая удаленные)
            all_after_delete = service.getAll()
            print(f"Валют после удаления: {len(all_after_delete)}")

            # Проверяем удаленную запись
            found_deleted = service.getById(currency_id)
            if found_deleted.isPresent():
                deleted_currency = found_deleted.get()
                print(f"Найдена удаленная валюта: {deleted_currency.getTitle()}")
                print(f"Deleted by: {deleted_currency.getDeletedBy()}")
                print(f"Delete time: {deleted_currency.getDeleteTime()}")
            else:
                print("❌ Удаленная валюта не найдена")

            # Тестируем восстановление через сервис
            print("\n--- Тест восстановления через сервис ---")
            try:
                # Восстанавливаем через сервис
                restored = service.restore(currency_id)
                if restored:
                    print(f"Валюта восстановлена через сервис: {restored.getTitle()}")
                    print(f"Updated by: {restored.getUpdatedBy()}")
                else:
                    print("❌ Не удалось восстановить валюту")

                # Проверяем восстановленную запись
                found_restored = service.getById(currency_id)
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
        print("\n--- Тест восстановления через get с тем же title ---")
        try:
            # Сначала удаляем валюту
            service.delete("Рубль")
            print("Валюта удалена для теста восстановления")

            # Получаем валюту с тем же названием (должна восстановиться)
            restored_currency = service.get("Рубль")
            print(f"Валюта восстановлена через get: {restored_currency.getTitle()}")
            print(f"ID восстановленной валюты: {restored_currency.getId()}")
            print(f"Position: {restored_currency.getPosition()}")
            print(f"Updated by: {restored_currency.getUpdatedBy()}")

            # Проверяем, что это та же запись (тот же ID)
            found_restored_save = service.getById(restored_currency.getId())
            if found_restored_save.isPresent():
                final_currency = found_restored_save.get()
                print(f"Проверка восстановленной валюты: {final_currency.getTitle()}")
                print(f"Delete time: {final_currency.getDeleteTime()}")
                print(f"Deleted by: {final_currency.getDeletedBy()}")
                print(f"Updated by: {final_currency.getUpdatedBy()}")
            else:
                print("❌ Восстановленная валюта не найдена")

        except Exception as e:
            print(f"❌ Ошибка при восстановлении через get: {e}")
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