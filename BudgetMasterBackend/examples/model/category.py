import os
import sys

from BudgetMasterBackend.examples.common import cleanup_example, get_java_class, setup_example

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


def main():
    print("=== Тест модели Category ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем класс Category
        Category = get_java_class("model.Category")

        # Создаем объект
        category = Category()
        category.setId(1)
        category.setPosition(1)
        category.setTitle("Продукты")
        category.setOperationType(1)  # 1 - расходы
        category.setType(1)  # тип категории
        category.setParentId(None)  # без родительской категории

        print("id:", category.getId())
        print("position:", category.getPosition())
        print("title:", category.getTitle())
        print("operationType:", category.getOperationType())
        print("type:", category.getType())
        print("parentId:", category.getParentId())
        print("toString:", category.toString())

        print("\n✅ Тест модели Category выполнен успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Очистка и остановка
        cleanup_example()


if __name__ == "__main__":
    main()
