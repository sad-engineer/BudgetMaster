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
        currency = create_test_entity(
            Currency, 
            title="Доллар США", 
            position=1,
            createdBy="tester", 
            updatedBy="tester"
        )
        
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
        
        # Тестируем поиск по названию
        print("\n--- Тест поиска по названию ---")
        try:
            found_by_title = repo.findByTitle("Доллар США")
            if found_by_title.isPresent():
                curr = found_by_title.get()
                print(f"Найдена валюта по названию: {curr.getTitle()}")
            else:
                print("❌ Валюта не найдена по названию")
        except Exception as e:
            print(f"❌ Ошибка при поиске по названию: {e}")
        
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
        
        # Тестируем создание нескольких валют
        print("\n--- Тест создания нескольких валют ---")
        test_currencies = []
        for i in range(3):
            test_currency = create_test_entity(
                Currency,
                title=f"Тестовая валюта {i + 1}",
                position=i + 2,  # Явно указываем позиции
                createdBy="position_tester",
                updatedBy="position_tester",
            )

            saved_test_currency = repo.save(test_currency)
            test_currencies.append(saved_test_currency)
            test_data_manager.add_test_id("currencies", saved_test_currency.getId())
            print(f"Создана валюта: {saved_test_currency.getTitle()}, Position: {saved_test_currency.getPosition()}")

        # Проверяем все валюты
        all_currencies = repo.findAll()
        print(f"\nВсе валюты:")
        for curr in all_currencies:
            print(f"  Position: {curr.getPosition()}, Title: {curr.getTitle()}")

        # Тестируем получение максимальной позиции
        print("\n--- Тест получения максимальной позиции ---")
        try:
            max_position = repo.getMaxPosition()
            print(f"Максимальная позиция: {max_position}")
        except Exception as e:
            print(f"❌ Ошибка при получении максимальной позиции: {e}")

        # Тестируем обычное обновление
        print("\n--- Тест обычного обновления ---")
        try:
            # Обновляем данные валюты
            saved_currency.setTitle("Доллар США (обновленный)")
            saved_currency.setPosition(5)  # Меняем позицию
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
                
        except Exception as e:
            print(f"❌ Ошибка при обновлении: {e}")
            import traceback
            traceback.print_exc()
        
        # Тестируем soft delete
        print("\n--- Тест soft delete ---")
        try:
            currency_id = saved_currency.getId()

            # Проверяем, что валюта есть в списке
            all_before_delete = repo.findAll()
            print(f"Валют до удаления: {len(all_before_delete)}")

            # Удаляем валюту (soft delete)
            deleted = repo.deleteById(currency_id, "test_user")
            print(f"Валюта помечена как удаленная: {deleted}")
            
            # Проверяем, что валюта все еще в списке (репозиторий возвращает все записи)
            all_after_delete = repo.findAll()
            print(f"Валют после удаления: {len(all_after_delete)}")

            # Проверяем удаленную запись
            found_deleted = repo.findById(currency_id)
            if found_deleted.isPresent():
                deleted_currency = found_deleted.get()
                print(f"Найдена удаленная валюта: {deleted_currency.getTitle()}")
                print(f"Deleted by: {deleted_currency.getDeletedBy()}")
                print(f"Delete time: {deleted_currency.getDeleteTime()}")
            else:
                print("❌ Удаленная валюта не найдена")
                
            # Тестируем удаление по названию
            print("\n--- Тест удаления по названию ---")
            deleted_by_title = repo.deleteByTitle("Тестовая валюта 1", "test_user")
            print(f"Валюта удалена по названию: {deleted_by_title}")
            
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
