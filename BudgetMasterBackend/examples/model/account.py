import os
import sys

from BudgetMasterBackend.examples.common import cleanup_example, get_java_class, setup_example

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


def main():
    print("=== Тест модели Account ===")

    # Настройка окружения
    if not setup_example():
        return

    try:
        # Импортируем класс Account
        Account = get_java_class("model.Account")

        # Создаем объект
        account = Account()
        account.setId(1)
        account.setPosition(1)
        account.setTitle("Основной счет")
        account.setAmount(10000)
        account.setType(1)  # 1 - банковский счет
        account.setCurrencyId(1)
        account.setClosed(0)  # 0 - активный счет

        print("id:", account.getId())
        print("position:", account.getPosition())
        print("title:", account.getTitle())
        print("amount:", account.getAmount())
        print("type:", account.getType())
        print("currencyId:", account.getCurrencyId())
        print("closed:", account.getClosed())
        print("toString:", account.toString())

        print("\n✅ Тест модели Account выполнен успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Очистка и остановка
        cleanup_example()


if __name__ == "__main__":
    main()
