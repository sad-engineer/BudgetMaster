@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Building BudgetMaster Backend JAR Package
echo ========================================

REM Установка JAVA_HOME и добавление в PATH
set JAVA_HOME=C:\Users\Korenyk.A\Documents\Prodjects\jdk-17.0.12
set PATH=%JAVA_HOME%\bin;%PATH%

REM Чтение версии backend из файла VERSION
for /f "usebackq delims=" %%i in ("..\\backend\\com\\sadengineer\\budgetmaster\\backend\\VERSION") do (
    set BACKEND_VERSION=%%i
)
REM Удаляем пробелы из версии
set BACKEND_VERSION=%BACKEND_VERSION: =%

REM Проверка наличия Java
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo ERROR: Java compiler not found at %JAVA_HOME%\bin\javac.exe
    echo Please check your JAVA_HOME path
    pause
    exit /b 1
)

echo Using Java at: %JAVA_HOME%
echo Java version:
javac -version
echo Building backend version: %BACKEND_VERSION%

REM Создание папок для сборки
if not exist "..\\backend\\build\\classes" mkdir ..\\backend\\build\\classes
if not exist "..\\backend\\build\\libs" mkdir ..\\backend\\build\\libs
if not exist "..\\lib" mkdir ..\\lib

REM Очистка предыдущей сборки
echo Cleaning previous build...
if exist "..\\backend\\build\\classes\\*" del /q ..\\backend\\build\\classes\\*
if exist "..\\backend\\build\\libs\\*.jar" del /q ..\\backend\\build\\libs\\*.jar

REM Переход в папку backend для компиляции
cd ..\backend

REM Собираем список всех .java файлов рекурсивно
dir /s /b com\sadengineer\budgetmaster\*.java > sources.txt

REM Компилируем все найденные файлы
javac -encoding UTF-8 -cp ".;*" -d build\classes @sources.txt

if %ERRORLEVEL% neq 0 (
    echo ERROR: Compilation failed!
    echo Please check your Java source files for errors.
    cd ..\utils
    pause
    exit /b 1
)

REM Удаляем временный файл
del sources.txt

echo Compilation completed successfully!

REM Создание папки для итогового JAR
if not exist "..\backend-jar" mkdir ..\backend-jar

REM Создание JAR файла с версией backend
REM Сохраняем JAR сразу в backend-jar
jar cf ..\backend-jar\budgetmaster-backend-%BACKEND_VERSION%.jar -C build\classes .

if %ERRORLEVEL% neq 0 (
    echo ERROR: JAR creation failed!
    cd ..\utils
    pause
    exit /b 1
)

REM Добавление ресурсов в JAR (если есть)
echo Adding resources to JAR...
if exist "*.properties" jar uf ..\backend-jar\budgetmaster-backend-%BACKEND_VERSION%.jar *.properties
if exist "*.xml" jar uf ..\backend-jar\budgetmaster-backend-%BACKEND_VERSION%.jar *.xml
if exist "com\sadengineer\budgetmaster\backend\VERSION" jar uf ..\backend-jar\budgetmaster-backend-%BACKEND_VERSION%.jar com\sadengineer\budgetmaster\backend\VERSION

REM Возврат в utils
cd ..\utils

REM Показываем информацию о JAR файле
if exist "..\backend-jar\budgetmaster-backend-%BACKEND_VERSION%.jar" (
    echo JAR file size:
    dir ..\backend-jar\budgetmaster-backend-%BACKEND_VERSION%.jar | findstr "budgetmaster-backend-%BACKEND_VERSION%.jar"
    
    echo.
    echo To run the JAR file, use:
    echo java -cp "..\backend-jar\budgetmaster-backend-%BACKEND_VERSION%.jar;..\lib\*" com.sadengineer.budgetmaster.Main
    echo.
    pushd ..\backend-jar
    set JAR_ABS_PATH=%CD%
    popd
    echo Folder contents %JAR_ABS_PATH%
    dir ..\backend-jar\*.jar
)

echo ========================================
echo Build completed successfully!
echo ========================================

pause 