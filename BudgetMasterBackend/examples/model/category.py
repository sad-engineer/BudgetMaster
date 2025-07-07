import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежит model/Category.class
CLASSPATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))


def main():
    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=[CLASSPATH], convertStrings=True)

    # Импортируем класс Category
    Category = jpype.JClass("model.Category")

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

    jpype.shutdownJVM()


if __name__ == "__main__":
    main()
