@echo off
chcp 65001 >nul
echo Загрузка валют из CSV файла
echo.

REM Проверяем наличие Python
python --version >nul 2>&1
if errorlevel 1 (
    echo ОШИБКА: Python не найден в PATH
    pause
    exit /b 1
)

REM Запускаем Python скрипт
python run_currency_loader.py

pause 