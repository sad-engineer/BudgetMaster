import os
import sys

from BudgetMasterBackend.examples.common import cleanup_example, get_java_class, setup_example

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


def main():
    print("=== Тест модели Currency ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем класс Currency
        Currency = get_java_class("model.Currency")

        # Создаем объект
        currency = Currency()
        currency.setId(1)
        currency.setPosition(1)
        currency.setTitle("Доллар США")

        print("id:", currency.getId())
        print("position:", currency.getPosition())
        print("title:", currency.getTitle())
        print("toString:", currency.toString())

        print("\n✅ Тест модели Currency выполнен успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Очистка и остановка
        cleanup_example()


if __name__ == "__main__":
    main()
