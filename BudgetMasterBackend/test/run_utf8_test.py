import os
import subprocess
import sys

# Настройка кодировки для Windows
if sys.platform.startswith('win'):
    os.environ['PYTHONIOENCODING'] = 'utf-8'
    os.environ['PYTHONLEGACYWINDOWSSTDIO'] = 'utf-8'

# Пути
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BUILD_DIR = os.path.join(PROJECT_ROOT, "build")
TEST_BUILD_DIR = os.path.join(PROJECT_ROOT, "test", "build")
LIB_DIR = os.path.join(PROJECT_ROOT, "lib")

# Создаем папку для тестовых классов
os.makedirs(TEST_BUILD_DIR, exist_ok=True)

# JDK путь
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Проекты\jdk-17.0.12\bin"
os.environ["PATH"] = JDK_PATH + os.pathsep + os.environ["PATH"]

print("=== Компиляция UTF-8 теста ===")

# Компилируем тест
javac_cmd = [
    "javac",
    "-encoding",
    "UTF-8",
    "-d",
    TEST_BUILD_DIR,
    "-cp",
    BUILD_DIR,
    os.path.join(PROJECT_ROOT, "test", "Utf8Test.java"),
]

print("Компиляция:", " ".join(javac_cmd))
result = subprocess.run(javac_cmd, capture_output=True, text=True, encoding='utf-8')

if result.returncode == 0:
    print("✅ Компиляция успешна")
else:
    print("❌ Ошибка компиляции:")
    print(result.stderr)
    sys.exit(1)

print("\n=== Запуск UTF-8 теста ===")

# Настройка кодировки консоли
try:
    subprocess.run(["chcp", "65001"], capture_output=True, shell=True, check=True)
    print("✅ Кодировка консоли установлена в UTF-8")
except:
    print("⚠️ Не удалось установить кодировку консоли")

# Запускаем тест
java_cmd = [
    "java",
    "-Dfile.encoding=UTF-8",
    "-Dconsole.encoding=UTF-8",
    "-cp",
    f"{BUILD_DIR}{os.pathsep}{TEST_BUILD_DIR}",
    "test.Utf8Test",
]

print("Запуск:", " ".join(java_cmd))

# Запускаем без capture_output для прямого вывода в консоль
result = subprocess.run(java_cmd)

print(f"\nКод завершения: {result.returncode}")
