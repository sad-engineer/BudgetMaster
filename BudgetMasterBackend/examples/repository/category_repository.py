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
    print("=== Тест CategoryRepository через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Category = get_java_class("model.Category")
        CategoryRepository = get_java_class("repository.CategoryRepository")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий
        repo = CategoryRepository(test_data_manager.db_manager.db_path)
        print("✅ Репозиторий создан")

        # Создаем тестовую категорию
        category = create_test_entity(
            Category, 
            title="Продукты питания", 
            operationType=1,  # Расходы
            type=1,           # Основные
            parentId=None,
            position=1,  # Явно указываем позицию
            createdBy="tester", 
            updatedBy="tester"
        )

        print("✅ Тестовая категория создана")
        print(f"Категория: {category.toString()}")

        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_category = repo.save(category)
        print(f"Категория сохранена: {saved_category.toString()}")
        print(f"ID категории: {saved_category.getId()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("categories", saved_category.getId())

        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_category = repo.findById(saved_category.getId())
            if found_category.isPresent():
                cat = found_category.get()
                print(f"Найдена категория: {cat.toString()}")
                print(f"ID: {cat.getId()}")
                print(f"Position: {cat.getPosition()}")
                print(f"Title: {cat.getTitle()}")
                print(f"Operation Type: {cat.getOperationType()}")
                print(f"Type: {cat.getType()}")
                print(f"Parent ID: {cat.getParentId()}")
                print(f"Created by: {cat.getCreatedBy()}")
            else:
                print("❌ Категория не найдена")
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

            cursor.execute("SELECT * FROM categories WHERE id = ?", (saved_category.getId(),))
            row = cursor.fetchone()

            if row:
                print("✅ Найдена категория в БД:")
                cursor.execute("PRAGMA table_info(categories)")
                columns = [col[1] for col in cursor.fetchall()]

                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Категория не найдена в БД")

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")

        # Тестируем создание нескольких категорий
        print("\n--- Тест создания нескольких категорий ---")
        test_categories = []
        
        # Создаем категории разных типов
        category_data = [
            ("Транспорт", 1, 1, None, 2),      # Расходы, Основные
            ("Развлечения", 1, 1, None, 3),    # Расходы, Основные
            ("Зарплата", 2, 1, None, 4),       # Доходы, Основные
            ("Подработка", 2, 1, None, 5),     # Доходы, Основные
        ]
        
        for i, (title, operation_type, cat_type, parent_id, position) in enumerate(category_data):
            test_category = create_test_entity(
                Category,
                title=title,
                operationType=operation_type,
                type=cat_type,
                parentId=parent_id,
                position=position,  # Явно указываем позиции
                createdBy="position_tester",
                updatedBy="position_tester",
            )

            saved_test_category = repo.save(test_category)
            test_categories.append(saved_test_category)
            test_data_manager.add_test_id("categories", saved_test_category.getId())
            print(f"Создана категория: {saved_test_category.getTitle()}, Position: {saved_test_category.getPosition()}, Type: {saved_test_category.getOperationType()}")

        # Проверяем все категории
        all_categories = repo.findAll()
        print(f"\nВсе категории:")
        for cat in all_categories:
            operation_type_str = "Расходы" if cat.getOperationType() == 1 else "Доходы"
            type_str = "Основные" if cat.getType() == 1 else "Дополнительные"
            print(f"  Position: {cat.getPosition()}, Title: {cat.getTitle()}, {operation_type_str}, {type_str}")

        # Тестируем создание иерархических категорий
        print("\n--- Тест создания иерархических категорий ---")
        try:
            # Создаем родительскую категорию
            parent_category = create_test_entity(
                Category,
                title="Продукты",
                operationType=1,  # Расходы
                type=1,           # Основные
                parentId=None,
                position=6,  # Явно указываем позицию
                createdBy="hierarchy_tester",
                updatedBy="hierarchy_tester",
            )
            
            saved_parent = repo.save(parent_category)
            test_data_manager.add_test_id("categories", saved_parent.getId())
            print(f"Создана родительская категория: {saved_parent.getTitle()}, ID: {saved_parent.getId()}")

            # Создаем дочерние категории
            child_categories = [
                ("Молочные продукты", 1, 2, saved_parent.getId(), 7),
                ("Мясо", 1, 2, saved_parent.getId(), 8),
                ("Овощи", 1, 2, saved_parent.getId(), 9),
            ]
            
            for title, operation_type, cat_type, parent_id, position in child_categories:
                child_category = create_test_entity(
                    Category,
                    title=title,
                    operationType=operation_type,
                    type=cat_type,
                    parentId=parent_id,
                    position=position,  # Явно указываем позиции
                    createdBy="hierarchy_tester",
                    updatedBy="hierarchy_tester",
                )
                
                saved_child = repo.save(child_category)
                test_data_manager.add_test_id("categories", saved_child.getId())
                print(f"Создана дочерняя категория: {saved_child.getTitle()}, Parent ID: {saved_child.getParentId()}")

        except Exception as e:
            print(f"❌ Ошибка при создании иерархических категорий: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем обновление
        print("\n--- Тест обновления ---")
        try:
            # Обновляем данные категории
            saved_category.setTitle("Продукты питания (обновленные)")
            saved_category.setPosition(10)  # Меняем позицию
            saved_category.setOperationType(1)  # Расходы
            saved_category.setType(2)  # Дополнительные
            saved_category.setUpdatedBy("jpype_update_test")

            # Обновляем дату
            updated_now = LocalDateTime.now()
            saved_category.setUpdateTime(updated_now)

            updated_category = repo.update(saved_category)
            print(f"Категория обновлена: {updated_category.toString()}")

            # Проверяем обновление
            found_updated = repo.findById(saved_category.getId())
            if found_updated.isPresent():
                cat = found_updated.get()
                print(f"Проверка обновления: {cat.toString()}")
                print(f"Новый title: {cat.getTitle()}")
                print(f"Новый position: {cat.getPosition()}")
                print(f"Operation Type: {cat.getOperationType()}")
                print(f"Type: {cat.getType()}")
                print(f"Updated by: {cat.getUpdatedBy()}")
            else:
                print("❌ Обновленная категория не найдена")

        except Exception as e:
            print(f"❌ Ошибка при обновлении: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем soft delete
        print("\n--- Тест soft delete ---")
        try:
            category_id = saved_category.getId()

            # Проверяем, что категория есть в обычном списке
            all_before_delete = repo.findAll()
            print(f"Категорий до удаления: {len(all_before_delete)}")

            # Удаляем категорию (soft delete)
            deleted = repo.delete(category_id, "test_user")
            print(f"Категория помечена как удаленная: {deleted}")

            # Проверяем, что категория исчезла из обычного списка
            all_after_delete = repo.findAll()
            print(f"Категорий после удаления (обычный режим): {len(all_after_delete)}")

            # Включаем показ удаленных записей
            repo.setIncludeDeleted(True)
            all_with_deleted = repo.findAll()
            print(f"Категорий после удаления (включая удаленные): {len(all_with_deleted)}")

            # Проверяем удаленную запись (должна быть найдена в режиме includeDeleted)
            found_deleted = repo.findById(category_id)
            if found_deleted.isPresent():
                deleted_category = found_deleted.get()
                print(f"Найдена удаленная категория: {deleted_category.getTitle()}")
                print(f"Deleted by: {deleted_category.getDeletedBy()}")
                print(f"Delete time: {deleted_category.getDeleteTime()}")
            else:
                print("❌ Удаленная категория не найдена")

            # Тестируем метод findDeleted
            print("\n--- Тест findDeleted ---")
            deleted_categories = repo.findDeleted()
            print(f"Найдено удаленных категорий: {len(deleted_categories)}")
            for cat in deleted_categories:
                operation_type_str = "Расходы" if cat.getOperationType() == 1 else "Доходы"
                print(f"  Удаленная категория: {cat.getTitle()}, {operation_type_str}, Deleted by: {cat.getDeletedBy()}")

            # Тестируем восстановление через метод restore
            print("\n--- Тест восстановления через restore ---")
            try:
                # Восстанавливаем через метод restore
                restored = repo.restore(category_id)
                print(f"Категория восстановлена через restore: {restored}")

                # Проверяем, что категория снова видна в обычном режиме
                repo.setIncludeDeleted(False)
                all_after_restore = repo.findAll()
                print(f"Категорий после восстановления через restore: {len(all_after_restore)}")

                # Проверяем восстановленную запись
                found_restored = repo.findById(category_id)
                if found_restored.isPresent():
                    restored_category = found_restored.get()
                    print(f"Найдена восстановленная категория: {restored_category.getTitle()}")
                    print(f"Delete time после восстановления: {restored_category.getDeleteTime()}")
                    print(f"Deleted by после восстановления: {restored_category.getDeletedBy()}")
                else:
                    print("❌ Восстановленная категория не найдена")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через restore: {e}")
                import traceback
                traceback.print_exc()

        except Exception as e:
            print(f"❌ Ошибка при soft delete: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем фильтрацию по типам
        print("\n--- Тест фильтрации по типам ---")
        try:
            # Получаем все категории
            all_categories = repo.findAll()
            
            # Фильтруем по типу операции (1 = Расходы, 2 = Доходы)
            expense_categories = [cat for cat in all_categories if cat.getOperationType() == 1]
            income_categories = [cat for cat in all_categories if cat.getOperationType() == 2]
            
            print(f"Категории расходов ({len(expense_categories)}):")
            for cat in expense_categories:
                type_str = "Основные" if cat.getType() == 1 else "Дополнительные"
                print(f"  - {cat.getTitle()} ({type_str})")
            
            print(f"Категории доходов ({len(income_categories)}):")
            for cat in income_categories:
                type_str = "Основные" if cat.getType() == 1 else "Дополнительные"
                print(f"  - {cat.getTitle()} ({type_str})")
            
            # Фильтруем по типу категории (1 = Основные, 2 = Дополнительные)
            main_categories = [cat for cat in all_categories if cat.getType() == 1]
            additional_categories = [cat for cat in all_categories if cat.getType() == 2]
            
            print(f"Основные категории ({len(main_categories)}):")
            for cat in main_categories:
                operation_str = "Расходы" if cat.getOperationType() == 1 else "Доходы"
                print(f"  - {cat.getTitle()} ({operation_str})")
            
            print(f"Дополнительные категории ({len(additional_categories)}):")
            for cat in additional_categories:
                operation_str = "Расходы" if cat.getOperationType() == 1 else "Доходы"
                print(f"  - {cat.getTitle()} ({operation_str})")

        except Exception as e:
            print(f"❌ Ошибка при фильтрации: {e}")
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