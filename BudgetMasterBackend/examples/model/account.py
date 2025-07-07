import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежит model/Account.class
CLASSPATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))


def main():
    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=[CLASSPATH], convertStrings=True)

    # Импортируем класс Account
    Account = jpype.JClass("model.Account")

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

    jpype.shutdownJVM()


if __name__ == "__main__":
    main()
