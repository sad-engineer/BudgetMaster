@echo off
echo Building BudgetMaster Backend JAR...

REM Создаем папку для классов
if not exist "build\classes" mkdir build\classes

REM Компилируем все Java файлы с кодировкой UTF-8
javac -encoding UTF-8 -cp ".;lib/*" -d build\classes ^
    Main.java ^
    model\*.java ^
    repository\*.java ^
    service\*.java ^
    constants\*.java ^
    util\*.java ^
    validator\*.java

REM Создаем JAR файл
jar cf build\budgetmaster-backend.jar -C build\classes .

echo JAR file created: build\budgetmaster-backend.jar
pause 