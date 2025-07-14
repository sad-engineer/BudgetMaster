@echo off
chcp 65001 >nul
echo ========================================
echo Запуск Java приложения BudgetMaster
echo ========================================

REM Добавляем путь к JDK
set "JDK_PATH=C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"
set "PATH=%JDK_PATH%;%PATH%"

echo Используется JDK: %JDK_PATH%
echo.

java -Dfile.encoding=UTF-8 -cp "lib\sqlite-jdbc-3.45.1.0.jar;lib\slf4j-api-2.0.13.jar;lib\slf4j-simple-2.0.13.jar;build" Main

echo.
echo ========================================
echo Программа завершена
echo ========================================
pause 