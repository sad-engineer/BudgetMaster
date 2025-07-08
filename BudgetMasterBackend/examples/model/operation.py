import os
import sys

from BudgetMasterBackend.examples.common import cleanup_example, get_java_class, setup_example

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


def main():
    print("=== Тест модели Operation ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем класс Operation
        Operation = get_java_class("model.Operation")

        # Создаем объект
        op = Operation()
        op.setAmount(1234)
        op.setComment("Бензак")
        op.setType(1)

        print("amount:", op.getAmount())
        print("comment:", op.getComment())
        print("type:", op.getType())
        print("toString:", op.toString())

        print("\n✅ Тест модели Operation выполнен успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Очистка и остановка
        cleanup_example()


if __name__ == "__main__":
    main()
