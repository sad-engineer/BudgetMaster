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
    print("=== Тест CategoryService через JPype ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем классы
        Category = get_java_class("model.Category")
        CategoryRepository = get_java_class("repository.CategoryRepository")
        CategoryService = get_java_class("service.CategoryService")
        LocalDateTime = get_java_class("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий и сервис
        repo = CategoryRepository(test_data_manager.db_manager.db_path)
        service = CategoryService(repo)
        print("✅ Сервис создан")

        # Создаем тестовую категорию (без позиции - сервис установит автоматически)
        category = create_test_entity(
            Category, 
            title="Продукты питания", 
            operationType=1,  # Расходы
            type=1,           # Основные
            parentId=None,
            position=0,  # Сервис установит автоматически
            createdBy="tester", 
            updatedBy="tester"
        )

        print("✅ Тестовая категория создана")
        print(f"Категория: {category.toString()}")

        # Тестируем создание через сервис (с автонумерацией позиции)
        print("\n--- Тест создания через сервис ---")
        saved_category = service.createCategory(category)
        print(f"Категория создана через сервис: {saved_category.toString()}")
        print(f"ID категории: {saved_category.getId()}")
        print(f"Автоматически установленная позиция: {saved_category.getPosition()}")

        # Сохраняем ID для последующего удаления
        test_data_manager.add_test_id("categories", saved_category.getId())

        # Тестируем поиск по ID через сервис
        print("\n--- Тест поиска по ID через сервис ---")
        try:
            found_category = service.getCategoryById(saved_category.getId())
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

        # Тестируем создание нескольких категорий через сервис
        print("\n--- Тест создания нескольких категорий через сервис ---")
        test_categories = []
        
        # Создаем категории разных типов
        category_data = [
            ("Транспорт", 1, 1, None),      # Расходы, Основные
            ("Развлечения", 1, 1, None),    # Расходы, Основные
            ("Зарплата", 2, 1, None),       # Доходы, Основные
            ("Подработка", 2, 1, None),     # Доходы, Основные
        ]
        
        for i, (title, operation_type, cat_type, parent_id) in enumerate(category_data):
            test_category = create_test_entity(
                Category,
                title=title,
                operationType=operation_type,
                type=cat_type,
                parentId=parent_id,
                position=0,  # Сервис установит автоматически
                createdBy="service_tester",
                updatedBy="service_tester",
            )

            saved_test_category = service.createCategory(test_category)
            test_categories.append(saved_test_category)
            test_data_manager.add_test_id("categories", saved_test_category.getId())
            print(f"Создана категория: {saved_test_category.getTitle()}, Position: {saved_test_category.getPosition()}, Type: {saved_test_category.getOperationType()}")

        # Проверяем все категории через сервис
        all_categories = service.getAllCategories()
        print(f"\nВсе категории (позиции нормализованы сервисом):")
        for cat in all_categories:
            operation_type_str = "Расходы" if cat.getOperationType() == 1 else "Доходы"
            type_str = "Основные" if cat.getType() == 1 else "Дополнительные"
            print(f"  Position: {cat.getPosition()}, Title: {cat.getTitle()}, {operation_type_str}, {type_str}")

        # Проверяем, что позиции идут по порядку после создания через сервис
        positions = [cat.getPosition() for cat in all_categories]
        expected_positions = list(range(1, len(positions) + 1))
        if positions == expected_positions:
            print("✅ Позиции нормализованы корректно сервисом!")
        else:
            print(f"❌ Ошибка нормализации сервисом: ожидались {expected_positions}, получены {positions}")

        # Тестируем создание иерархических категорий через сервис
        print("\n--- Тест создания иерархических категорий через сервис ---")
        try:
            # Создаем родительскую категорию
            parent_category = create_test_entity(
                Category,
                title="Продукты",
                operationType=1,  # Расходы
                type=1,           # Основные
                parentId=None,
                position=0,  # Сервис установит автоматически
                createdBy="hierarchy_service_tester",
                updatedBy="hierarchy_service_tester",
            )
            
            saved_parent = service.createCategory(parent_category)
            test_data_manager.add_test_id("categories", saved_parent.getId())
            print(f"Создана родительская категория: {saved_parent.getTitle()}, ID: {saved_parent.getId()}")

            # Создаем дочерние категории
            child_categories = [
                ("Молочные продукты", 1, 2, saved_parent.getId()),
                ("Мясо", 1, 2, saved_parent.getId()),
                ("Овощи", 1, 2, saved_parent.getId()),
            ]
            
            for title, operation_type, cat_type, parent_id in child_categories:
                child_category = create_test_entity(
                    Category,
                    title=title,
                    operationType=operation_type,
                    type=cat_type,
                    parentId=parent_id,
                    position=0,  # Сервис установит автоматически
                    createdBy="hierarchy_service_tester",
                    updatedBy="hierarchy_service_tester",
                )
                
                saved_child = service.createCategory(child_category)
                test_data_manager.add_test_id("categories", saved_child.getId())
                print(f"Создана дочерняя категория: {saved_child.getTitle()}, Parent ID: {saved_child.getParentId()}")

        except Exception as e:
            print(f"❌ Ошибка при создании иерархических категорий через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем обновление через сервис
        print("\n--- Тест обновления через сервис ---")
        try:
            # Обновляем данные категории
            saved_category.setTitle("Продукты питания (обновленные)")
            saved_category.setPosition(2)  # Меняем позицию
            saved_category.setOperationType(1)  # Расходы
            saved_category.setType(2)  # Дополнительные
            saved_category.setUpdatedBy("service_update_test")

            # Обновляем дату
            updated_now = LocalDateTime.now()
            saved_category.setUpdateTime(updated_now)

            updated_category = service.updateCategory(saved_category)
            print(f"Категория обновлена через сервис: {updated_category.toString()}")

            # Проверяем обновление
            found_updated = service.getCategoryById(saved_category.getId())
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

            # Проверяем, что позиции нормализованы после обновления через сервис
            all_after_update = service.getAllCategories()
            print(f"\nВсе категории после обновления через сервис (позиции нормализованы):")
            for cat in all_after_update:
                operation_type_str = "Расходы" if cat.getOperationType() == 1 else "Доходы"
                type_str = "Основные" if cat.getType() == 1 else "Дополнительные"
                print(f"  Position: {cat.getPosition()}, Title: {cat.getTitle()}, {operation_type_str}, {type_str}")

            # Проверяем, что позиции идут по порядку
            positions_after_update = [cat.getPosition() for cat in all_after_update]
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
            category_id = saved_category.getId()

            # Проверяем, что категория есть в обычном списке
            all_before_delete = service.getAllCategories()
            print(f"Категорий до удаления: {len(all_before_delete)}")

            # Удаляем категорию через сервис
            deleted = service.deleteCategory(category_id, "service_test_user")
            print(f"Категория удалена через сервис: {deleted}")

            # Проверяем, что категория исчезла из обычного списка
            all_after_delete = service.getAllCategories()
            print(f"Категорий после удаления: {len(all_after_delete)}")

            # Проверяем удаленные категории через сервис
            deleted_categories = service.getDeletedCategories()
            print(f"Найдено удаленных категорий через сервис: {len(deleted_categories)}")
            for cat in deleted_categories:
                operation_type_str = "Расходы" if cat.getOperationType() == 1 else "Доходы"
                print(f"  Удаленная категория: {cat.getTitle()}, {operation_type_str}, Deleted by: {cat.getDeletedBy()}")

            # Тестируем восстановление через сервис
            print("\n--- Тест восстановления через сервис ---")
            try:
                # Восстанавливаем через сервис
                restored = service.restoreCategory(category_id)
                print(f"Категория восстановлена через сервис: {restored}")

                # Проверяем, что категория снова видна
                all_after_restore = service.getAllCategories()
                print(f"Категорий после восстановления через сервис: {len(all_after_restore)}")

                # Проверяем восстановленную запись
                found_restored = service.getCategoryById(category_id)
                if found_restored.isPresent():
                    restored_category = found_restored.get()
                    print(f"Найдена восстановленная категория: {restored_category.getTitle()}")
                    print(f"Delete time после восстановления: {restored_category.getDeleteTime()}")
                    print(f"Deleted by после восстановления: {restored_category.getDeletedBy()}")
                else:
                    print("❌ Восстановленная категория не найдена")

            except Exception as e:
                print(f"❌ Ошибка при восстановлении через сервис: {e}")
                import traceback
                traceback.print_exc()

        except Exception as e:
            print(f"❌ Ошибка при soft delete через сервис: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем восстановление удаленной записи с тем же title
        print("\n--- Тест восстановления через createCategory с тем же title ---")
        try:
            # Сначала удаляем категорию
            service.deleteCategory(saved_category.getId(), "restore_test_user")
            print("Категория удалена для теста восстановления")

            # Создаем новую категорию с тем же title
            new_category = create_test_entity(
                Category,
                title="Продукты питания (обновленные)",  # То же название, что и удаленная
                operationType=1,
                type=2,
                parentId=None,
                position=0,  # Сервис установит автоматически
                createdBy="restore_test",
                updatedBy="restore_test",
            )

            restored_category = service.createCategory(new_category)
            test_data_manager.add_test_id("categories", restored_category.getId())
            print(f"Категория восстановлена через createCategory: {restored_category.getTitle()}")
            print(f"ID восстановленной категории: {restored_category.getId()}")
            print(f"Position: {restored_category.getPosition()}")

            # Проверяем, что это та же запись (тот же ID)
            found_restored_save = service.getCategoryById(restored_category.getId())
            if found_restored_save.isPresent():
                final_category = found_restored_save.get()
                print(f"Проверка восстановленной категории: {final_category.getTitle()}")
                print(f"Delete time: {final_category.getDeleteTime()}")
                print(f"Deleted by: {final_category.getDeletedBy()}")
                print(f"Updated by: {final_category.getUpdatedBy()}")
            else:
                print("❌ Восстановленная категория не найдена")

        except Exception as e:
            print(f"❌ Ошибка при восстановлении через createCategory: {e}")
            import traceback
            traceback.print_exc()

        # Тестируем фильтрацию по типам через сервис
        print("\n--- Тест фильтрации по типам через сервис ---")
        try:
            # Получаем все категории через сервис
            all_categories = service.getAllCategories()
            
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