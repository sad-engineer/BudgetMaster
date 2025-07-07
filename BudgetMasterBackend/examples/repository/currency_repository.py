import jpype
import jpype.imports
import os

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежат скомпилированные классы
BUILD_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))

# Путь к библиотекам (SQLite драйвер)
LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "lib"))

# Classpath с библиотеками
CLASSPATH = (BUILD_PATH + os.pathsep + 
             os.path.join(LIB_PATH, "sqlite-jdbc-3.45.1.0.jar") + os.pathsep +
             os.path.join(LIB_PATH, "slf4j-api-2.0.13.jar") + os.pathsep +
             os.path.join(LIB_PATH, "slf4j-simple-2.0.13.jar"))

# Путь к базе данных
DB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "budget_master.db"))

def main():
    print("=== Тест CurrencyRepository через JPype ===")
    print(f"Classpath: {CLASSPATH}")
    print(f"Database: {DB_PATH}")
    
    # Проверяем существование файлов
    if not os.path.exists(DB_PATH):
        print(f"❌ База данных не найдена: {DB_PATH}")
        return
    
    # Проверяем наличие всех необходимых JAR файлов
    required_jars = [
        "sqlite-jdbc-3.45.1.0.jar",
        "slf4j-api-2.0.13.jar", 
        "slf4j-simple-2.0.13.jar"
    ]
    
    for jar in required_jars:
        jar_path = os.path.join(LIB_PATH, jar)
        if not os.path.exists(jar_path):
            print(f"❌ JAR файл не найден: {jar_path}")
            return
    
    print("✅ Все необходимые JAR файлы найдены")
    
    # Запуск JVM
    jpype.startJVM(
        jvmpath=os.path.join(JDK_PATH, "server", "jvm.dll"),
        classpath=CLASSPATH,
        convertStrings=True
    )

    try:
        # Загружаем SQLite драйвер
        Class = jpype.JClass("java.lang.Class")
        Class.forName("org.sqlite.JDBC")
        print("✅ SQLite драйвер загружен")
        
        # Импортируем классы
        Currency = jpype.JClass("model.Currency")
        CurrencyRepository = jpype.JClass("repository.CurrencyRepository")
        LocalDateTime = jpype.JClass("java.time.LocalDateTime")
        
        print("✅ Классы импортированы")
        
        # Создаем репозиторий
        repo = CurrencyRepository(DB_PATH)
        print("✅ Репозиторий создан")
        
        # Создаем тестовую валюту
        currency = Currency()
        currency.setPosition(1)
        currency.setTitle("Доллар США")
        
        # Устанавливаем базовые поля (наследуемые от BaseEntity)
        currency.setCreatedBy("tester")
        currency.setUpdatedBy("tester")
        
        # Устанавливаем даты
        now = LocalDateTime.now()
        currency.setCreateTime(now)
        currency.setUpdateTime(now)
        currency.setDeleteTime(None)
        
        print("✅ Тестовая валюта создана")
        print(f"Валюта: {currency.toString()}")
        
        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_currency = repo.save(currency)
        print(f"Валюта сохранена: {saved_currency.toString()}")
        print(f"ID валюты: {saved_currency.getId()}")
        
        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_currency = repo.findById(saved_currency.getId())
            if found_currency.isPresent():
                curr = found_currency.get()
                print(f"Найдена валюта: {curr.toString()}")
                print(f"ID: {curr.getId()}")
                print(f"Position: {curr.getPosition()}")
                print(f"Title: {curr.getTitle()}")
                print(f"Created by: {curr.getCreatedBy()}")
            else:
                print("❌ Валюта не найдена")
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
            
            cursor.execute("SELECT * FROM currencies WHERE id = ?", (saved_currency.getId(),))
            row = cursor.fetchone()
            
            if row:
                print("✅ Найдена валюта в БД:")
                cursor.execute("PRAGMA table_info(currencies)")
                columns = [col[1] for col in cursor.fetchall()]
                
                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Валюта не найдена в БД")
            
            conn.close()
            
        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")
        
        # Тестируем обновление
        print("\n--- Тест обновления ---")
        try:
            # Обновляем данные валюты
            saved_currency.setTitle("Доллар США (обновленный)")
            saved_currency.setPosition(2)
            saved_currency.setUpdatedBy("jpype_update_test")
            
            # Обновляем дату
            updated_now = LocalDateTime.now()
            saved_currency.setUpdateTime(updated_now)
            
            updated_currency = repo.update(saved_currency)
            print(f"Валюта обновлена: {updated_currency.toString()}")
            
            # Проверяем обновление
            found_updated = repo.findById(saved_currency.getId())
            if found_updated.isPresent():
                curr = found_updated.get()
                print(f"Проверка обновления: {curr.toString()}")
                print(f"Новый title: {curr.getTitle()}")
                print(f"Новый position: {curr.getPosition()}")
                print(f"Updated by: {curr.getUpdatedBy()}")
            else:
                print("❌ Обновленная валюта не найдена")
                
        except Exception as e:
            print(f"❌ Ошибка при обновлении: {e}")
            import traceback
            traceback.print_exc()
        
        # Тестируем удаление
        print("\n--- Тест удаления ---")
        try:
            currency_id = saved_currency.getId()
            deleted = repo.delete(currency_id)
            print(f"Валюта удалена: {deleted}")
            
            # Проверяем удаление
            found_deleted = repo.findById(currency_id)
            if found_deleted.isPresent():
                print("❌ Валюта все еще существует")
            else:
                print("✅ Валюта успешно удалена")
                
            # Проверяем через прямой SQL
            import sqlite3
            conn = sqlite3.connect(DB_PATH)
            cursor = conn.cursor()
            cursor.execute("SELECT COUNT(*) FROM currencies WHERE id = ?", (currency_id,))
            count = cursor.fetchone()[0]
            print(f"Количество записей с ID {currency_id} в БД: {count}")
            conn.close()
            
        except Exception as e:
            print(f"❌ Ошибка при удалении: {e}")
            import traceback
            traceback.print_exc()
        
        print("\n✅ Все тесты выполнены успешно!")
        
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
