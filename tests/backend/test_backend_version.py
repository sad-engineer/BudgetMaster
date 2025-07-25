#!/usr/bin/env python3
"""
Тест для проверки версии backend пакета
"""
import os

import unittest

from tests.backend.test_common import JPypeSetup, get_java_class


class TestBackendVersion(unittest.TestCase):
    """Тесты для проверки версии backend"""

    @classmethod
    def setUpClass(cls):
        """Настройка тестового окружения"""
        print("🔍 НАСТРОЙКА ТЕСТОВ ВЕРСИИ BACKEND")
        print("=" * 50)

        # Создаем экземпляр JPypeSetup
        cls.jpype_setup = JPypeSetup()

        # Проверяем наличие всех необходимых файлов
        if not cls.jpype_setup.check_prerequisites():
            raise Exception("❌ Не все необходимые файлы найдены")

        # Запускаем JVM
        cls.jpype_setup.start_jvm()

        print("✅ Тестовое окружение настроено")

    @classmethod
    def tearDownClass(cls):
        """Очистка тестового окружения"""
        print("🧹 ОЧИСТКА ТЕСТОВОГО ОКРУЖЕНИЯ")
        try:
            cls.jpype_setup.shutdown_jvm()
            print("✅ Тестовое окружение очищено")
        except Exception as e:
            print(f"⚠️ Предупреждение при очистке: {e}")
        finally:
            # Принудительно очищаем глобальный экземпляр
            import tests.backend.test_common as test_common
            test_common._GLOBAL_JPYPE_SETUP = None

    def test_01_backend_version_exists(self):
        """Тест 01: проверяем, что класс BackendVersion существует"""
        print("\n📋 Тест 01: существование класса BackendVersion")

        try:
            # Получаем класс BackendVersion
            BackendVersion = get_java_class("com.sadengineer.budgetmaster.backend.BackendVersion")

            # Проверяем, что класс не None
            self.assertIsNotNone(BackendVersion, "Класс BackendVersion должен существовать")

            print("✅ Класс BackendVersion найден")

        except Exception as e:
            self.fail(f"❌ Ошибка при получении класса BackendVersion: {e}")

    def test_02_backend_version_constant_exists(self):
        """Тест 02: проверяем, что константа VERSION существует"""
        print("\n📋 Тест 02: существование константы VERSION")

        try:
            # Получаем класс BackendVersion
            BackendVersion = get_java_class("com.sadengineer.budgetmaster.backend.BackendVersion")

            # Проверяем, что константа VERSION существует
            version = BackendVersion.VERSION

            self.assertIsNotNone(version, "Константа VERSION должна существовать")
            self.assertIsInstance(version, str, "VERSION должна быть строкой")

            print(f"✅ Константа VERSION найдена: {version}")

        except Exception as e:
            self.fail(f"❌ Ошибка при получении константы VERSION: {e}")

    def test_03_backend_version_format(self):
        """Тест 03: проверяем формат версии"""
        print("\n📋 Тест 03: формат версии")

        try:
            # Получаем версию
            BackendVersion = get_java_class("com.sadengineer.budgetmaster.backend.BackendVersion")
            version = BackendVersion.VERSION

            # Проверяем формат версии (например: 0.0.009)
            self.assertRegex(version, r'^\d+\.\d+\.\d+$', "Версия должна соответствовать формату X.Y.Z")

            # Разбираем версию
            parts = version.split('.')
            self.assertEqual(len(parts), 3, "Версия должна содержать 3 части")

            # Проверяем, что все части - числа
            for part in parts:
                self.assertTrue(part.isdigit(), f"Часть версии '{part}' должна быть числом")

            print(f"✅ Формат версии корректный: {version}")

        except Exception as e:
            self.fail(f"❌ Ошибка при проверке формата версии: {e}")

    def test_04_backend_version_not_empty(self):
        """Тест 04: проверяем, что версия не пустая"""
        print("\n📋 Тест 04: версия не пустая")

        try:
            # Получаем версию
            BackendVersion = get_java_class("com.sadengineer.budgetmaster.backend.BackendVersion")
            version = BackendVersion.VERSION

            # Проверяем, что версия не пустая
            self.assertIsNotNone(version, "Версия не должна быть None")
            self.assertNotEqual(version.strip(), "", "Версия не должна быть пустой строкой")
            self.assertNotEqual(version, "unknown", "Версия не должна быть 'unknown'")

            print(f"✅ Версия не пустая: {version}")

        except Exception as e:
            self.fail(f"❌ Ошибка при проверке версии: {e}")

    def test_05_backend_version_from_file(self):
        """Тест 05: проверяем, что версия читается из файла"""
        print("\n📋 Тест 05: чтение версии из файла")

        try:
            # Получаем версию из JAR
            BackendVersion = get_java_class("com.sadengineer.budgetmaster.backend.BackendVersion")
            version = BackendVersion.VERSION

            # Читаем ожидаемую версию из файла backend/version.txt

            version_file_path = os.path.join(
                os.path.dirname(__file__),
                "..",
                "..",
                "backend",
                "com",
                "sadengineer",
                "budgetmaster",
                "backend",
                "VERSION",
            )

            with open(version_file_path, 'r', encoding='utf-8') as f:
                expected_version = f.read().strip()

            # Проверяем, что версия соответствует ожидаемой
            self.assertEqual(
                version, expected_version, f"Версия должна быть '{expected_version}', получено '{version}'"
            )

            print(f"✅ Версия соответствует файлу: {version}")

        except Exception as e:
            self.fail(f"❌ Ошибка при проверке версии из файла: {e}")

    def test_06_backend_version_main_method(self):
        """Тест 06: проверяем метод main класса BackendVersion"""
        print("\n📋 Тест 06: метод main класса BackendVersion")

        try:
            # Получаем класс BackendVersion
            BackendVersion = get_java_class("com.sadengineer.budgetmaster.backend.BackendVersion")

            # Проверяем, что метод main существует
            self.assertTrue(hasattr(BackendVersion, 'main'), "Класс BackendVersion должен иметь метод main")

            # Проверяем, что метод main статический
            self.assertTrue(BackendVersion.main.__name__ == 'main', "Метод main должен быть статическим")

            print("✅ Метод main класса BackendVersion найден")

        except Exception as e:
            self.fail(f"❌ Ошибка при проверке метода main: {e}")

    def test_07_backend_version_in_jar(self):
        """Тест 07: проверяем, что версия доступна в JAR файле"""
        print("\n📋 Тест 07: версия в JAR файле")

        try:
            # Получаем версию
            BackendVersion = get_java_class("com.sadengineer.budgetmaster.backend.BackendVersion")
            version = BackendVersion.VERSION

            # Проверяем, что версия не пустая и корректная
            self.assertIsNotNone(version)
            self.assertNotEqual(version.strip(), "")
            self.assertNotEqual(version, "unknown")

            # Проверяем формат
            self.assertRegex(version, r'^\d+\.\d+\.\d+$')

            print(f"✅ Версия доступна в JAR: {version}")

        except Exception as e:
            self.fail(f"❌ Ошибка при проверке версии в JAR: {e}")


if __name__ == '__main__':
    unittest.main()
