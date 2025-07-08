import os

import jpype
import jpype.imports

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежат скомпилированные классы
BUILD_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))

# Путь к библиотекам (SQLite драйвер)
LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "lib"))

# Classpath с библиотеками
CLASSPATH = (
    BUILD_PATH
    + os.pathsep
    + os.path.join(LIB_PATH, "sqlite-jdbc-3.45.1.0.jar")
    + os.pathsep
    + os.path.join(LIB_PATH, "slf4j-api-2.0.13.jar")
    + os.pathsep
    + os.path.join(LIB_PATH, "slf4j-simple-2.0.13.jar")
)

# Путь к базе данных
DB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "budget_master.db"))


def main():
    print("=== Тест AccountRepository через JPype ===")
    print(f"Classpath: {CLASSPATH}")
    print(f"Database: {DB_PATH}")

    # Проверяем существование файлов
    if not os.path.exists(DB_PATH):
        print(f"❌ База данных не найдена: {DB_PATH}")
        return

    # Проверяем наличие всех необходимых JAR файлов
    required_jars = ["sqlite-jdbc-3.45.1.0.jar", "slf4j-api-2.0.13.jar", "slf4j-simple-2.0.13.jar"]

    for jar in required_jars:
        jar_path = os.path.join(LIB_PATH, jar)
        if not os.path.exists(jar_path):
            print(f"❌ JAR файл не найден: {jar_path}")
            return

    print("✅ Все необходимые JAR файлы найдены")

    # Запуск JVM
    jpype.startJVM(jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"), classpath=CLASSPATH, convertStrings=True)

    try:
        # Загружаем SQLite драйвер
        Class = jpype.JClass("java.lang.Class")
        Class.forName("org.sqlite.JDBC")
        print("✅ SQLite драйвер загружен")

        # Импортируем классы
        Account = jpype.JClass("model.Account")
        AccountRepository = jpype.JClass("repository.AccountRepository")
        LocalDateTime = jpype.JClass("java.time.LocalDateTime")

        print("✅ Классы импортированы")

        # Создаем репозиторий
        repo = AccountRepository(DB_PATH)
        print("✅ Репозиторий создан")

        # Создаем тестовый счет
        account = Account()
        account.setPosition(1)
        account.setTitle("Наличные (тест)")
        account.setAmount(100000)  # 1000.00 в копейках
        account.setType(1)  # Текущий счет
        account.setCurrencyId(1)  # RUB
        account.setClosed(0)  # Не закрыт

        # Устанавливаем базовые поля (наследуемые от BaseEntity)
        account.setCreatedBy("tester")
        account.setUpdatedBy("tester")

        # Устанавливаем даты
        now = LocalDateTime.now()
        account.setCreateTime(now)
        account.setUpdateTime(now)
        account.setDeleteTime(None)

        print("✅ Тестовый счет создан")
        print(f"Счет: {account.toString()}")

        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_account = repo.save(account)
        print(f"Счет сохранен: {saved_account.toString()}")
        print(f"ID счета: {saved_account.getId()}")

        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_account = repo.findById(saved_account.getId())
            if found_account.isPresent():
                acc = found_account.get()
                print(f"Найден счет: {acc.toString()}")
                print(f"ID: {acc.getId()}")
                print(f"Position: {acc.getPosition()}")
                print(f"Title: {acc.getTitle()}")
                print(f"Amount: {acc.getAmount()} копеек")
                print(f"Type: {acc.getType()}")
                print(f"Currency ID: {acc.getCurrencyId()}")
                print(f"Closed: {acc.getClosed()}")
                print(f"Created by: {acc.getCreatedBy()}")
            else:
                print("❌ Счет не найден")
        except Exception as e:
            print(f"❌ Ошибка при поиске: {e}")
            import traceback

            traceback.print_exc()

        # Прямой SQL-запрос для проверки
        print("\n--- Прямой SQL-запрос ---")
        try:
            import sqlite3

            conn = sqlite3.connect(DB_PATH)
            cursor = conn.cursor()

            cursor.execute("SELECT * FROM accounts WHERE id = ?", (saved_account.getId(),))
            row = cursor.fetchone()

            if row:
                print("✅ Найден счет в БД:")
                cursor.execute("PRAGMA table_info(accounts)")
                columns = [col[1] for col in cursor.fetchall()]

                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Счет не найден в БД")

            conn.close()

        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")
        #
        # # Тестируем обновление
        # print("\n--- Тест обновления ---")
        # try:
        #     # Обновляем данные счета
        #     saved_account.setTitle("Основной счет (обновленный)")
        #     saved_account.setPosition(2)
        #     saved_account.setAmount(150000)  # 1500.00 в копейках
        #     saved_account.setUpdatedBy("jpype_update_test")
        #
        #     # Обновляем дату
        #     updated_now = LocalDateTime.now()
        #     saved_account.setUpdateTime(updated_now)
        #
        #     updated_account = repo.update(saved_account)
        #     print(f"Счет обновлен: {updated_account.toString()}")
        #
        #     # Проверяем обновление
        #     found_updated = repo.findById(saved_account.getId())
        #     if found_updated.isPresent():
        #         acc = found_updated.get()
        #         print(f"Проверка обновления: {acc.toString()}")
        #         print(f"Новый title: {acc.getTitle()}")
        #         print(f"Новый position: {acc.getPosition()}")
        #         print(f"Новый amount: {acc.getAmount()} копеек")
        #         print(f"Updated by: {acc.getUpdatedBy()}")
        #     else:
        #         print("❌ Обновленный счет не найден")
        #
        # except Exception as e:
        #     print(f"❌ Ошибка при обновлении: {e}")
        #     import traceback
        #     traceback.print_exc()
        #
        # # Тестируем создание кредитной карты
        # print("\n--- Тест создания кредитной карты ---")
        # try:
        #     credit_card = Account()
        #     credit_card.setPosition(3)
        #     credit_card.setTitle("Кредитная карта")
        #     credit_card.setAmount(0)  # Баланс 0
        #     credit_card.setType(2)  # Кредитная карта
        #     credit_card.setCurrencyId(1)  # RUB
        #     credit_card.setClosed(0)  # Не закрыта
        #     credit_card.setCreditCardLimit(500000)  # Лимит 5000.00 в копейках
        #     credit_card.setCreditCardCategoryId(1)  # Категория для комиссий
        #     credit_card.setCreditCardCommissionCategoryId(2)  # Категория для комиссий
        #
        #     # Устанавливаем базовые поля
        #     credit_card.setCreatedBy("jpype_test")
        #     credit_card.setUpdatedBy("jpype_test")
        #     credit_card.setCreateTime(LocalDateTime.now())
        #     credit_card.setUpdateTime(LocalDateTime.now())
        #     credit_card.setDeleteTime(None)
        #
        #     saved_credit_card = repo.save(credit_card)
        #     print(f"Кредитная карта создана: {saved_credit_card.toString()}")
        #     print(f"ID кредитной карты: {saved_credit_card.getId()}")
        #     print(f"Лимит: {saved_credit_card.getCreditCardLimit()} копеек")
        #
        # except Exception as e:
        #     print(f"❌ Ошибка при создании кредитной карты: {e}")
        #     import traceback
        #     traceback.print_exc()
        #
        # # Тестируем поиск всех счетов
        # print("\n--- Тест поиска всех счетов ---")
        # try:
        #     all_accounts = repo.findAll()
        #     print(f"Найдено счетов: {len(all_accounts)}")
        #     for i, acc in enumerate(all_accounts, 1):
        #         print(f"  {i}. ID: {acc.getId()}, Title: {acc.getTitle()}, Amount: {acc.getAmount()} копеек")
        # except Exception as e:
        #     print(f"❌ Ошибка при поиске всех счетов: {e}")
        #     import traceback
        #     traceback.print_exc()
        #
        # # Тестируем удаление
        # print("\n--- Тест удаления ---")
        # try:
        #     account_id = saved_account.getId()
        #     deleted = repo.delete(account_id)
        #     print(f"Счет удален: {deleted}")
        #
        #     # Проверяем удаление
        #     found_deleted = repo.findById(account_id)
        #     if found_deleted.isPresent():
        #         print("❌ Счет все еще существует")
        #     else:
        #         print("✅ Счет успешно удален")
        #
        #     # Проверяем через прямой SQL
        #     import sqlite3
        #     conn = sqlite3.connect(DB_PATH)
        #     cursor = conn.cursor()
        #     cursor.execute("SELECT COUNT(*) FROM accounts WHERE id = ?", (account_id,))
        #     count = cursor.fetchone()[0]
        #     print(f"Количество записей с ID {account_id} в БД: {count}")
        #     conn.close()
        #
        # except Exception as e:
        #     print(f"❌ Ошибка при удалении: {e}")
        #     import traceback
        #     traceback.print_exc()
        #
        # print("\n✅ Все тесты выполнены успешно!")

    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback

        traceback.print_exc()

    finally:
        # Останавливаем JVM
        if jpype.isJVMStarted():
            jpype.shutdownJVM()
            print("JVM остановлена")


if __name__ == "__main__":
    main()
