import os
import sys

from BudgetMasterBackend.examples.common import cleanup_example, get_java_class, setup_example

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


def main():
    print("=== Тест модели Budget ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем класс Budget
        Budget = get_java_class("model.Budget")

        # Создаем объект
        budget = Budget()
        budget.setId(1)
        budget.setAmount(50000)
        budget.setCurrencyId(1)

        print("id:", budget.getId())
        print("amount:", budget.getAmount())
        print("currencyId:", budget.getCurrencyId())
        print("categoryId:", budget.getCategoryId())

        print("\n✅ Тест модели Budget выполнен успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Очистка и остановка
        cleanup_example()


if __name__ == "__main__":
    main()
