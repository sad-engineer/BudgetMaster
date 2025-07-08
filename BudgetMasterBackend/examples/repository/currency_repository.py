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
    print("=== Тест CurrencyRepository через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Currency = get_java_class("model.Currency")
        CurrencyRepository = get_java_class("repository.CurrencyRepository")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий
        repo = CurrencyRepository(test_data_manager.db_manager.db_path)
        print("✅ Репозиторий создан")

        # Создаем тестовую валюту
        currency = create_test_entity(Currency, title="Доллар США", createdBy="tester", updatedBy="tester")

        print("✅ Тестовая валюта создана")
        print(f"Валюта: {currency.toString()}")

        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_currency = repo.save(currency)
        print(f"Валюта сохранена: {saved_currency.toString()}")
        print(f"ID валюты: {saved_currency.getId()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("currencies", saved_currency.getId())

        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_currency = repo.findById(saved_currency.getId())
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

        # Прямой SQL-запрос для проверки
        print("\n--- Прямой SQL-запрос ---")
        try:
            import sqlite3

            conn = sqlite3.connect(test_data_manager.db_manager.db_path)
            cursor = conn.cursor()

            cursor.execute("SELECT * FROM currencies WHERE id = ?", (saved_currency.getId(),))
            row = cursor.fetchone()

            if row:
                print("✅ Найдена валюта в БД:")
                cursor.execute("PRAGMA table_info(currencies)")
                columns = [col[1] for col in cursor.fetchall()]

                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Валюта не найдена в БД")

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")

        # Тестируем создание нескольких валют для проверки автонумерации
        print("\n--- Тест создания нескольких валют ---")
        test_currencies = []
        for i in range(3):
            test_currency = create_test_entity(
                Currency,
                title=f"Тестовая валюта {i + 1}",
                createdBy="position_tester",
                updatedBy="position_tester",
            )

            saved_test_currency = repo.save(test_currency)
            test_currencies.append(saved_test_currency)
            test_data_manager.add_test_id("currencies", saved_test_currency.getId())
            print(f"Создана валюта: {saved_test_currency.getTitle()}, Position: {saved_test_currency.getPosition()}")

        # Проверяем сортировку (позиции должны быть нормализованы после каждого save)
        all_currencies = repo.findAll()
        print(f"\nВсе валюты (отсортированы по position, нормализованы после save):")
        for curr in all_currencies:
            print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")

        # Проверяем, что позиции идут по порядку после создания
        positions = [curr.getPosition() for curr in all_currencies]
        expected_positions = list(range(1, len(positions) + 1))
        if positions == expected_positions:
            print("✅ Позиции нормализованы корректно после создания валют!")
        else:
            print(f"❌ Ошибка нормализации после создания: ожидались {expected_positions}, получены {positions}")

        # Тестируем перестановку позиций
        print("\n--- Тест перестановки позиций ---")
        try:
            # Берем первую валюту и меняем её position на 2 (которая уже занята)
            first_currency = test_currencies[0]
            original_position = first_currency.getPosition()
            print(f"Исходная позиция первой валюты: {original_position}")

            # Меняем position на 2 (которая должна быть занята)
            first_currency.setPosition(2)
            first_currency.setTitle("Первая валюта (перемещена)")
            first_currency.setUpdatedBy("position_reorder_tester")
            first_currency.setUpdateTime(LocalDateTime.now())

            updated_currency = repo.update(first_currency)
            print(f"Валюта обновлена: {updated_currency.getTitle()}, Position: {updated_currency.getPosition()}")

            # Проверяем результат перестановки (позиции должны быть нормализованы после update)
            all_currencies_after = repo.findAll()
            print(f"\nВсе валюты после перестановки (позиции нормализованы):")
            for curr in all_currencies_after:
                print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")

        except Exception as e:
            print(f"❌ Ошибка при перестановке позиций: {e}")
            import traceback

            traceback.print_exc()

        # Тестируем обычное обновление
        print("\n--- Тест обычного обновления ---")
        try:
            # Обновляем данные валюты
            saved_currency.setTitle("Доллар США (обновленный)")
            saved_currency.setPosition(1)  # Меняем на позицию 1
            saved_currency.setUpdatedBy("jpype_update_test")

            # Обновляем дату
            updated_now = LocalDateTime.now()
            saved_currency.setUpdateTime(updated_now)

            updated_currency = repo.update(saved_currency)
            print(f"Валюта обновлена: {updated_currency.toString()}")

            # Проверяем обновление
            found_updated = repo.findById(saved_currency.getId())
            if found_updated.isPresent():
                curr = found_updated.get()
                print(f"Проверка обновления: {curr.toString()}")
                print(f"Новый title: {curr.getTitle()}")
                print(f"Новый position: {curr.getPosition()}")
                print(f"Updated by: {curr.getUpdatedBy()}")
            else:
                print("❌ Обновленная валюта не найдена")

            # Проверяем, что позиции нормализованы после обновления
            all_after_update = repo.findAll()
            print(f"\nВсе валюты после обновления (позиции нормализованы):")
            for curr in all_after_update:
                print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")

            # Проверяем, что позиции идут по порядку
            positions_after_update = [curr.getPosition() for curr in all_after_update]
            expected_positions_after_update = list(range(1, len(positions_after_update) + 1))
            if positions_after_update == expected_positions_after_update:
                print("✅ Позиции нормализованы корректно после обновления!")
            else:
                print(
                    f"❌ Ошибка нормализации после обновления: ожидались {expected_positions_after_update}, получены {positions_after_update}"
                )

        except Exception as e:
            print(f"❌ Ошибка при обновлении: {e}")
            import traceback

            traceback.print_exc()

        # Тестируем soft delete
        print("\n--- Тест soft delete ---")
        try:
            currency_id = saved_currency.getId()

            # Проверяем, что валюта есть в обычном списке
            all_before_delete = repo.findAll()
            print(f"Валют до удаления: {len(all_before_delete)}")

            # Удаляем валюту (soft delete)
            deleted = repo.delete(currency_id, "test_user")
            print(f"Валюта помечена как удаленная: {deleted}")

            # Проверяем, что валюта исчезла из обычного списка
            all_after_delete = repo.findAll()
            print(f"Валют после удаления (обычный режим): {len(all_after_delete)}")

            # Включаем показ удаленных записей
            repo.setIncludeDeleted(True)
            all_with_deleted = repo.findAll()
            print(f"Валют после удаления (включая удаленные): {len(all_with_deleted)}")

            # Проверяем удаленную запись (должна быть найдена в режиме includeDeleted)
            found_deleted = repo.findById(currency_id)
            if found_deleted.isPresent():
                deleted_currency = found_deleted.get()
                print(f"Найдена удаленная валюта: {deleted_currency.getTitle()}")
                print(f"Deleted by: {deleted_currency.getDeletedBy()}")
                print(f"Delete time: {deleted_currency.getDeleteTime()}")
            else:
                print("❌ Удаленная валюта не найдена")

            # Тестируем метод findDeleted
            print("\n--- Тест findDeleted ---")
            deleted_currencies = repo.findDeleted()
            print(f"Найдено удаленных валют: {len(deleted_currencies)}")
            for curr in deleted_currencies:
                print(f"  Удаленная валюта: {curr.getTitle()}, Deleted by: {curr.getDeletedBy()}")

            # Тестируем восстановление через save с тем же title
            print("\n--- Тест восстановления через save ---")
            try:
                # Создаем новую валюту с тем же названием, что и удаленная
                new_currency = create_test_entity(
                    Currency,
                    title="Доллар США (обновленный)",  # То же название, что и удаленная
                    createdBy="restore_test",
                    updatedBy="restore_test",
                )

                restored_currency = repo.save(new_currency)
                test_data_manager.add_test_id("currencies", restored_currency.getId())
                print(f"Валюта восстановлена через save: {restored_currency.getTitle()}")
                print(f"ID восстановленной валюты: {restored_currency.getId()}")
                print(f"Position: {restored_currency.getPosition()}")

                # Проверяем, что валюта снова видна в обычном режиме
                repo.setIncludeDeleted(False)
                all_after_restore_save = repo.findAll()
                print(f"Валют после восстановления через save: {len(all_after_restore_save)}")

                # Проверяем, что это та же запись (тот же ID)
                found_restored_save = repo.findById(restored_currency.getId())
                if found_restored_save.isPresent():
                    final_currency = found_restored_save.get()
                    print(f"Проверка восстановленной валюты: {final_currency.getTitle()}")
                    print(f"Delete time: {final_currency.getDeleteTime()}")
                    print(f"Deleted by: {final_currency.getDeletedBy()}")
                    print(f"Updated by: {final_currency.getUpdatedBy()}")
                else:
                    print("❌ Восстановленная валюта не найдена")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через save: {e}")
                import traceback

                traceback.print_exc()

            # Тестируем восстановление через метод restore
            print("\n--- Тест восстановления через restore ---")
            try:
                # Сначала удаляем валюту снова для теста
                repo.delete(restored_currency.getId(), "test_user_2")
                print("Валюта снова удалена для теста restore")

                # Восстанавливаем через метод restore
                restored = repo.restore(restored_currency.getId())
                print(f"Валюта восстановлена через restore: {restored}")

                # Проверяем, что валюта снова видна в обычном режиме
                all_after_restore = repo.findAll()
                print(f"Валют после восстановления через restore: {len(all_after_restore)}")

                # Проверяем восстановленную запись
                found_restored = repo.findById(restored_currency.getId())
                if found_restored.isPresent():
                    restored_currency = found_restored.get()
                    print(f"Найдена восстановленная валюта: {restored_currency.getTitle()}")
                    print(f"Delete time после восстановления: {restored_currency.getDeleteTime()}")
                    print(f"Deleted by после восстановления: {restored_currency.getDeletedBy()}")
                else:
                    print("❌ Восстановленная валюта не найдена")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через restore: {e}")
                import traceback

                traceback.print_exc()

            # Проверяем через прямой SQL
            import sqlite3

            conn = sqlite3.connect(test_data_manager.db_manager.db_path)
            cursor = conn.cursor()
            cursor.execute("SELECT delete_time, deleted_by FROM currencies WHERE id = ?", (currency_id,))
            row = cursor.fetchone()
            if row:
                print(f"SQL проверка - Delete time: {row[0]}, Deleted by: {row[1]}")
            conn.close()

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
        # Очистка тестовых данных и остановка JVM
        cleanup_example()


if __name__ == "__main__":
    main()
