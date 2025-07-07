import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежит model/Budget.class
CLASSPATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))


def main():
    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=[CLASSPATH], convertStrings=True)

    # Импортируем класс Budget
    Budget = jpype.JClass("model.Budget")

    # Создаем объект
    budget = Budget()
    budget.setId(1)
    budget.setAmount(50000)
    budget.setCurrencyId(1)

    print("id:", budget.getId())
    print("amount:", budget.getAmount())
    print("currencyId:", budget.getCurrencyId())
    print("categoryId:", budget.getCategoryId())

    jpype.shutdownJVM()


if __name__ == "__main__":
    main()
