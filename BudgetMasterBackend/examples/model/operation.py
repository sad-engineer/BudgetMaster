import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежит model/Operation.class
CLASSPATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))


def main():
    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=[CLASSPATH], convertStrings=True)

    # Импортируем класс Operation
    Operation = jpype.JClass("model.Operation")

    # Создаем объект
    op = Operation()
    op.setAmount(1234)
    op.setComment("Бензак")
    op.setType(1)

    print("amount:", op.getAmount())
    print("comment:", op.getComment())
    print("type:", op.getType())
    print("toString:", op.toString())

    jpype.shutdownJVM()


if __name__ == "__main__":
    main()
