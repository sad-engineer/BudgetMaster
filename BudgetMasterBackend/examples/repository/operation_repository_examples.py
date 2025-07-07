import jpype
import jpype.imports
import os

# Путь к JDK (где лежит jvm.dll)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"

# Путь к build, где лежат скомпилированные классы
BUILD_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "build"))

# Путь к библиотекам (SQLite драйвер)
LIB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "lib"))

# Classpath с библиотеками (объединяем в строку с разделителем)
CLASSPATH = (BUILD_PATH + os.pathsep + 
             os.path.join(LIB_PATH, "sqlite-jdbc-3.45.1.0.jar") + os.pathsep +
             os.path.join(LIB_PATH, "slf4j-api-2.0.13.jar") + os.pathsep +
             os.path.join(LIB_PATH, "slf4j-simple-2.0.13.jar"))

# Путь к базе данных
DB_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "../..", "budget_master.db"))


def main():
    print("=== Тест OperationRepository через JPype ===")
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
        Operation = jpype.JClass("model.Operation")
        OperationRepository = jpype.JClass("repository.OperationRepository")
        DateTimeUtil = jpype.JClass("util.DateTimeUtil")
        
        print("✅ Классы импортированы")
        
        # Создаем репозиторий
        repo = OperationRepository(DB_PATH)
        print("✅ Репозиторий создан")
        
        # Создаем тестовую операцию
        operation = Operation()
        operation.setId(1289)  # Уникальный ID для теста
        operation.setType(1)  # Тип операции
        operation.setAmount(1500)  # Сумма

        LocalDateTime = jpype.JClass("java.time.LocalDateTime")
        date = LocalDateTime.of(2023, 1, 30, 12, 0, 0)  # 30 января 2023, 12:00:00
        operation.setDate(date)

        operation.setComment("Тестовая операция из JPype")
        operation.setCategoryId(1)
        operation.setAccountId(1)
        operation.setCurrencyId(1)
        
        # Устанавливаем базовые поля (наследуемые от BaseEntity)
        operation.setCreatedBy("jpype_test")
        operation.setUpdatedBy("jpype_test")
        
        # Устанавливаем даты (используем уже созданный LocalDateTime)
        now = LocalDateTime.now()
        operation.setCreateTime(now)
        operation.setUpdateTime(now)
        operation.setDeleteTime(None)  # если поле допускает null
        
        print("✅ Тестовая операция создана")
        print(f"Операция: {operation.toString()}")
        
        # Тестируем сохранение
        print("\n--- Тест сохранения ---")
        saved_operation = repo.save(operation)
        print(f"Операция сохранена: {saved_operation.toString()}")
        
        # Тестируем поиск по ID
        print("\n--- Тест поиска по ID ---")
        try:
            found_operation = repo.findById(1289)
            if found_operation.isPresent():
                op = found_operation.get()
                print(f"Найдена операция: {op.toString()}")
            else:
                print("❌ Операция не найдена")
        except Exception as e:
            print(f"❌ Ошибка при поиске: {e}")
        
        # Прямой SQL-запрос к базе данных
        print("\n--- Прямой SQL-запрос ---")
        try:
            import sqlite3
            conn = sqlite3.connect(DB_PATH)
            cursor = conn.cursor()
            
            # Запрос всех операций
            cursor.execute("SELECT * FROM operations WHERE id = 1289")
            row = cursor.fetchone()
            
            if row:
                print("✅ Найдена операция в БД:")
                # Получаем названия колонок
                cursor.execute("PRAGMA table_info(operations)")
                columns = [col[1] for col in cursor.fetchall()]
                
                for i, (col_name, value) in enumerate(zip(columns, row)):
                    print(f"  {col_name}: {value} (тип: {type(value).__name__})")
            else:
                print("❌ Операция с ID=1289 не найдена в БД")
                
                # Показываем все операции
                cursor.execute("SELECT id, type, amount, comment, date FROM operations LIMIT 5")
                all_ops = cursor.fetchall()
                print(f"Всего операций в БД: {len(all_ops)}")
                for op in all_ops:
                    print(f"  ID={op[0]}, Type={op[1]}, Amount={op[2]}, Comment='{op[3]}', Date='{op[4]}'")
            
            conn.close()
            
        except Exception as e:
            print(f"❌ Ошибка при прямом SQL-запросе: {e}")
            import traceback
            traceback.print_exc()
        
        # # Тестируем получение всех операций
        # print("\n--- Тест получения всех операций ---")
        # try:
        #     all_operations = repo.findAll()
        #     print(f"Всего операций в БД: {len(all_operations)}")
        # except Exception as e:
        #     print(f"❌ Ошибка при получении всех операций: {e}")
        #     all_operations = []
        #
        # # Показываем первые 3 операции (если есть)
        # if len(all_operations) > 0:
        #     for i, op in enumerate(all_operations[:min(3, len(all_operations))]):
        #         print(f"Операция {i+1}: ID={op.getId()}, Сумма={op.getAmount()}, Комментарий={op.getComment()}")
        # else:
        #     print("В базе данных нет операций")
        #
        # # Тестируем обновление
        # print("\n--- Тест обновления ---")
        # operation.setAmount(2000)
        # operation.setComment("Обновленная операция из JPype")
        # updated_operation = repo.update(operation)
        # print(f"Операция обновлена: {updated_operation.toString()}")
        #
        # # Проверяем обновление
        # found_updated = repo.findById(1289)
        # if found_updated.isPresent():
        #     op = found_updated.get()
        #     print(f"Проверка обновления: {op.toString()}")
        #
        # # Тестируем удаление
        # print("\n--- Тест удаления ---")
        # deleted = repo.delete(1289)
        # print(f"Операция удалена: {deleted}")
        #
        # # Проверяем удаление
        # found_deleted = repo.findById(1289)
        # if found_deleted.isPresent():
        #     print("❌ Операция все еще существует")
        # else:
        #     print("✅ Операция успешно удалена")
        #
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