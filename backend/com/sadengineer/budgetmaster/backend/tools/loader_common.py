#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import subprocess
import sys

import jpype
import jpype.imports


class LoaderSetup:
    """Упрощенная настройка окружения для загрузчиков"""

    def __init__(self):
        # Путь к JDK
        self.JDK_PATH = r"C:\Users\Korenyk.A\Documents\Prodjects\jdk-17.0.12\bin"

        # Путь к build
        self.BUILD_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "build"))

        # Путь к библиотекам
        self.LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "lib"))

        # Classpath
        self.CLASSPATH = (
            self.BUILD_PATH
            + os.pathsep
            + os.path.join(self.LIB_PATH, "sqlite-jdbc-3.45.1.0.jar")
            + os.pathsep
            + os.path.join(self.LIB_PATH, "slf4j-api-2.0.13.jar")
            + os.pathsep
            + os.path.join(self.LIB_PATH, "slf4j-simple-2.0.13.jar")
        )

        self.jvm_started = False
        self.java_classes = {}

    def setup(self):
        """Быстрая настройка окружения"""
        try:
            # Проверяем JAR файлы
            required_jars = ["sqlite-jdbc-3.45.1.0.jar", "slf4j-api-2.0.13.jar", "slf4j-simple-2.0.13.jar"]
            for jar in required_jars:
                if not os.path.exists(os.path.join(self.LIB_PATH, jar)):
                    print(f"❌ JAR файл не найден: {jar}")
                    return False

            # Запускаем JVM
            if not self.jvm_started:
                jpype.startJVM(
                    jvmpath=os.path.join(self.JDK_PATH, "server", "jvm.dll"),
                    classpath=self.CLASSPATH,
                    convertStrings=True,
                )

                # Загружаем SQLite драйвер
                Class = jpype.JClass("java.lang.Class")
                Class.forName("org.sqlite.JDBC")

                self.jvm_started = True

            return True

        except Exception as e:
            print(f"❌ Ошибка настройки: {e}")
            return False

    def get_class(self, class_name: str):
        """Получает Java класс"""
        if not self.jvm_started:
            if not self.setup():
                return None

        if class_name not in self.java_classes:
            self.java_classes[class_name] = jpype.JClass(class_name)

        return self.java_classes[class_name]

    def compile_project(self):
        """Компилирует проект"""
        try:
            project_root = os.path.dirname(os.path.dirname(__file__))
            compiler_script = os.path.join(project_root, "compiler.py")

            result = subprocess.run([sys.executable, compiler_script], capture_output=True, text=True, cwd=project_root)

            return result.returncode == 0
        except Exception:
            return False

    def shutdown(self):
        """Останавливает JVM"""
        if jpype.isJVMStarted():
            jpype.shutdownJVM()
            self.jvm_started = False


# Глобальный экземпляр
loader_setup = LoaderSetup()


def setup_loader_environment():
    """Настройка окружения для загрузчиков"""
    print("🔧 Настройка...")

    # Компилируем проект
    if not loader_setup.compile_project():
        print("❌ Ошибка компиляции")
        return False

    # Настраиваем JVM
    if not loader_setup.setup():
        print("❌ Ошибка настройки JVM")
        return False

    print("✅ Готово")
    return True


def get_loader_class(class_name: str):
    """Получает класс для загрузчика"""
    return loader_setup.get_class(class_name)


def cleanup_loader_environment():
    """Очистка окружения"""
    loader_setup.shutdown()
