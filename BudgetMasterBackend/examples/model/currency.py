import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежит model/Currency.class
CLASSPATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))


def main():
    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=[CLASSPATH], convertStrings=True)

    # Импортируем класс Currency
    Currency = jpype.JClass("model.Currency")

    # Создаем объект
    currency = Currency()
    currency.setId(1)
    currency.setPosition(1)
    currency.setTitle("Доллар США")

    print("id:", currency.getId())
    print("position:", currency.getPosition())
    print("title:", currency.getTitle())
    print("toString:", currency.toString())

    jpype.shutdownJVM()


if __name__ == "__main__":
    main()
