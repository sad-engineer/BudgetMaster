import os
import sys
import unittest

from tests.backend.test_common import cleanup_example, get_java_class, setup_example

# Добавляем путь к родительской директории для импорта
sys.path.append(os.path.dirname(os.path.dirname(__file__)))


class TestCurrencyService(unittest.TestCase):
    """Юнит-тесты для CurrencyService"""

    @classmethod
    def setUpClass(cls):
        result = setup_example()
        if result is None:
            raise Exception("Не удалось настроить окружение для тестов")

        # Получаем компоненты из setup_example
        cls.jpype_setup, cls.db_manager, cls.test_data_manager = result

        # Импортируем Java классы
        cls.CurrencyService = get_java_class("com.sadengineer.budgetmaster.backend.service.CurrencyService")
        cls.CurrencyRepository = get_java_class("com.sadengineer.budgetmaster.backend.repository.CurrencyRepository")
        cls.Currency = get_java_class("com.sadengineer.budgetmaster.backend.model.Currency")
        cls.LocalDateTime = get_java_class("java.time.LocalDateTime")
        cls.Integer = get_java_class("java.lang.Integer")
        cls.PlatformUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.PlatformUtil")

        # Инициализируем DatabaseProvider для тестов
        cls.PlatformUtil.initializeDatabaseProvider(None)

        cls.test_currency_ids = []
        # Используем DB_PATH из test_common.py
        cls.test_db_path = cls.db_manager.db_path

        # Инициализируем базу данных с таблицами
        cls.DatabaseUtil = get_java_class("com.sadengineer.budgetmaster.backend.util.DatabaseUtil")
        cls.DatabaseUtil.createDatabaseIfNotExists(cls.test_db_path)
        print(f"✅ База данных инициализирована: {cls.test_db_path}")

        cls.repository = cls.CurrencyRepository(cls.test_db_path)
        cls.service = cls.CurrencyService(cls.repository, "test_user")

    def setUp(self):
        """Настройка перед каждым тестом"""
        # Инициализируем базу данных перед первым использованием
        self.DatabaseUtil.createDatabaseIfNotExists(self.test_db_path)

        # Принудительно восстанавливаем дефолтные данные для тестов
        # Это гарантирует, что у нас всегда есть базовые данные
        self.DatabaseUtil.restoreDefaults(self.test_db_path)

    @classmethod
    def tearDownClass(cls):
        """Очистка после всех тестов"""
        try:
            # Получаем менеджер базы данных
            db_manager = cls.db_manager

            # Удаляем тестовые записи по ID
            deleted_count = 0
            for currency_id in cls.test_currency_ids:
                try:
                    success = db_manager.execute_update("DELETE FROM currencies WHERE id = ?", (currency_id,))
                    if success:
                        deleted_count += 1
                    else:
                        print(f"Ошибка при удалении валюты {currency_id}")
                except Exception as e:
                    print(f"Ошибка при удалении валюты {currency_id}: {e}")

            if deleted_count > 0:
                print(f"Удалено {deleted_count} тестовых валют из базы данных")

            cleanup_example()
        except Exception as e:
            print(f"Ошибка при очистке: {e}")
        finally:
            # Не завершаем JVM здесь - пусть это делает atexit
            pass

    def test_01_create_currency(self):
        """Тест 01: Получение новой (не существующей) валюты"""
        title = "Валюта 1"
        position = self.repository.getMaxPosition() + 1

        currency = self.service.get(title)
        self.test_currency_ids.append(currency.getId())

        self.assertIsNotNone(currency)
        self.assertEqual(currency.getTitle(), title)
        self.assertEqual(currency.getPosition(), position)
        self.assertEqual(currency.getCreatedBy(), "test_user")
        self.assertIsNone(currency.getUpdatedBy())
        self.assertIsNone(currency.getDeletedBy())
        self.assertIsNotNone(currency.getCreateTime())
        self.assertIsNone(currency.getUpdateTime())
        self.assertIsNone(currency.getDeleteTime())

    def test_02_get_currency_by_title(self):
        """Тест 02: Получение существующей валюты по Title"""
        title = "Валюта 2"
        currency_new = self.service.get(title)
        self.test_currency_ids.append(currency_new.getId())

        # Валюта создана, ее позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        currency = self.service.get(title)
        self.test_currency_ids.append(currency.getId())

        self.assertIsNotNone(currency)
        self.assertEqual(currency.getTitle(), title)
        self.assertEqual(currency.getPosition(), position)
        self.assertEqual(currency.getCreatedBy(), "test_user")
        self.assertIsNone(currency.getUpdatedBy())
        self.assertIsNone(currency.getDeletedBy())
        self.assertIsNotNone(currency.getCreateTime())
        self.assertIsNone(currency.getUpdateTime())
        self.assertIsNone(currency.getDeleteTime())

    def test_03_get_deleted_currency_by_title(self):
        """Тест 03: Получение удаленной валюты по Title"""
        title = "Валюта 3"
        currency_new = self.service.get(title)
        self.test_currency_ids.append(currency_new.getId())
        self.repository.deleteById(currency_new.getId(), "test_user")

        # Валюта не удалена физически из таблицы, ее позиция должна быть наибольшая из существующих
        position = self.repository.getMaxPosition()
        currency = self.service.get(title)
        self.test_currency_ids.append(currency.getId())

        self.assertIsNotNone(currency)
        self.assertEqual(currency.getTitle(), title)
        self.assertEqual(currency.getPosition(), position)
        self.assertEqual(currency.getCreatedBy(), "test_user")
        self.assertEqual(currency.getUpdatedBy(), "test_user")
        self.assertIsNone(currency.getDeletedBy())
        self.assertIsNotNone(currency.getCreateTime())
        self.assertIsNotNone(currency.getUpdateTime())
        self.assertIsNone(currency.getDeleteTime())
        self.assertNotEqual(currency.getCreateTime(), currency.getUpdateTime())

    def test_04_get_currency_by_id(self):
        """Тест 04: Получение валюты по ID"""
        currency = self.service.get(self.Integer(1))
        self.assertIsNotNone(currency)
        self.assertEqual(currency.getId(), 1)
        self.assertEqual(currency.getTitle(), "RUB")
        self.assertEqual(currency.getPosition(), 1)
        self.assertEqual(currency.getCreatedBy(), "initializer")
        self.assertIsNone(currency.getUpdatedBy())
        self.assertIsNone(currency.getDeletedBy())
        self.assertIsNotNone(currency.getCreateTime())
        self.assertIsNone(currency.getUpdateTime())
        self.assertIsNone(currency.getDeleteTime())

    def test_05_get_currency_by_id_not_found(self):
        """Тест 05: Получение валюты по несуществующему ID"""
        currency = self.service.get(self.Integer(999999))
        self.assertIsNone(currency)

    def test_06_create_currency_with_special_title(self):
        """Тест 06: Создание валюты с необычным названием"""
        currency = self.service.get("12123")
        self.test_currency_ids.append(currency.getId())
        self.assertEqual(currency.getTitle(), "12123")

        currency2 = self.service.get("Валюта с цифрами 123")
        self.test_currency_ids.append(currency2.getId())
        self.assertEqual(currency2.getTitle(), "Валюта с цифрами 123")

    def test_07_create_currency_with_invalid_title(self):
        """Тест 07: Создание валюты с недопустимым названием"""
        with self.assertRaises(Exception):
            self.service.create("""Валюта с недопустимым названием {123}""")

    def test_08_delete_currency_by_id(self):
        """Тест 08: Удаление валюты по ID"""
        currency = self.service.get("Валюта 4")
        self.test_currency_ids.append(currency.getId())
        result = self.service.delete(currency.getId())
        self.assertTrue(result)
        # Проверяем, что валюта помечена как удалённая
        found = self.repository.findById(currency.getId())
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_09_delete_currency_by_title(self):
        """Тест 09: Удаление валюты по названию"""
        title = "Валюта 5"
        currency = self.service.get(title)
        self.test_currency_ids.append(currency.getId())
        result = self.service.delete(title)
        self.assertTrue(result)
        # Проверяем, что валюта помечена как удалённая
        found = self.repository.findByTitle(title)
        self.assertTrue(found.isPresent())
        self.assertIsNotNone(found.get().getDeleteTime())
        self.assertEqual(found.get().getDeletedBy(), "test_user")

    def test_10_change_position(self):
        """Тест 10: Изменение позиции валюты"""
        position = self.repository.getMaxPosition()
        c1 = self.service.get("Валюта х1")
        c2 = self.service.get("Валюта х2")
        c3 = self.service.get("Валюта х3")
        self.test_currency_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c1 на позицию 3
        result = self.service.changePosition(c1, position + 3)
        self.assertEqual(result.getPosition(), position + 3)
        # Проверяем, что другие валюты сдвинулись
        updated_c2 = self.repository.findById(c2.getId()).get()
        updated_c3 = self.repository.findById(c3.getId()).get()
        self.assertEqual(updated_c2.getPosition(), position + 1)
        self.assertEqual(updated_c3.getPosition(), position + 2)

    def test_11_change_position_up(self):
        """Тест 11: Перемещение валюты вверх"""
        position = self.repository.getMaxPosition()
        c1 = self.service.get("Валюта A")
        c2 = self.service.get("Валюта B")
        c3 = self.service.get("Валюта C")
        self.test_currency_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        # Перемещаем c3 на позицию 1
        result = self.service.changePosition(c3, position + 1)
        self.assertEqual(result.getPosition(), position + 1)
        updated_c1 = self.repository.findById(c1.getId()).get()
        updated_c2 = self.repository.findById(c2.getId()).get()
        self.assertEqual(updated_c1.getPosition(), position + 2)
        self.assertEqual(updated_c2.getPosition(), position + 3)

    def test_12_change_position_by_old_new(self):
        """Тест 12: Изменение позиции по старой и новой позиции"""
        position = self.repository.getMaxPosition()
        c1 = self.service.get("Валюта X")
        c2 = self.service.get("Валюта Y")
        c3 = self.service.get("Валюта Z")
        self.test_currency_ids.extend([c1.getId(), c2.getId(), c3.getId()])
        result = self.service.changePosition(position + 1, position + 3)
        self.assertIsNotNone(result)
        self.assertEqual(result.getPosition(), position + 3)

    def test_13_change_position_by_old_new_not_found(self):
        """Тест 13: Изменение позиции по несуществующей старой позиции"""
        result = self.service.changePosition(999, 1)
        self.assertIsNone(result)

    def test_14_change_position_same_position(self):
        """Тест 14: Изменение позиции на ту же позицию"""
        currency = self.service.get("Валюта без изменений")
        self.test_currency_ids.append(currency.getId())
        old_position = currency.getPosition()
        result = self.service.changePosition(currency, old_position)
        self.assertEqual(result.getPosition(), old_position)

    def test_15_change_position_invalid_position(self):
        """Тест 15: Изменение позиции на недопустимую"""
        currency = self.service.get("Валюта для теста")
        self.test_currency_ids.append(currency.getId())
        with self.assertRaises(Exception):
            self.service.changePosition(currency, 999)

    def test_16_get_all_currencies(self):
        """Тест 16: Получение всех валют"""
        c1 = self.service.get("Валюта 111")
        c2 = self.service.get("Валюта 211")
        self.test_currency_ids.append(c1.getId())
        self.test_currency_ids.append(c2.getId())
        currencies = self.service.getAll()
        self.assertIsNotNone(currencies)
        self.assertGreater(len(currencies), 0)
        ids = [c.getId() for c in currencies]
        self.assertIn(c1.getId(), ids)
        self.assertIn(c2.getId(), ids)

    def test_17_is_currency_deleted(self):
        """Тест 17: Проверка удаления валюты"""
        currency = self.service.get("Валюта для проверки удаления")
        self.test_currency_ids.append(currency.getId())
        self.repository.deleteById(currency.getId(), "test_user")
        deleted = self.repository.findById(currency.getId()).get()
        self.assertTrue(self.service.isCurrencyDeleted(deleted))

    def test_18_create_currency_with_unicode_title(self):
        """Тест 18: Создание валюты с Unicode названием"""
        title = "Валюта с кириллицей и символами: !@#$%^&*()"
        with self.assertRaises(Exception):
            self.service.get(title)

    def test_19_get_currency_with_special_characters(self):
        """Тест 19: Получение валюты со специальными символами"""
        title = "Валюта с символами: €$¥£₽"
        with self.assertRaises(Exception):
            self.service.get(title)


if __name__ == '__main__':
    unittest.main()
