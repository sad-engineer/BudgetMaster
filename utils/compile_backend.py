import os
import subprocess

# Папки с исходниками (относительно backend)
SRC_DIRS = [
    "../backend/model",
    "../backend/repository",
    "../backend/service",
    "../backend/util",
    "../backend/tools",
    "../backend/constants",
    "../backend/validator",
]
# Явно указываем отдельные файлы для компиляции
EXTRA_JAVA_FILES = [
    "../backend/Main.java",
    "../backend/BackendVersion.java"
]

PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
BUILD_DIR = os.path.join(PROJECT_ROOT, "../backend/build/backend")

# JDK bin (если нужно явно)
JDK_PATH = r"C:\Users\Korenyk.A\Documents\Prodjects\jdk-17.0.12\bin"
os.environ["PATH"] = JDK_PATH + os.pathsep + os.environ["PATH"]

# Создать папку для .class файлов
if not os.path.exists(BUILD_DIR):
    os.makedirs(BUILD_DIR)

# Собрать все .java файлы из папок
java_files = []
for src in SRC_DIRS:
    src_path = os.path.join(PROJECT_ROOT, src)
    if os.path.exists(src_path):
        for root, dirs, files in os.walk(src_path):
            for file in files:
                if file.endswith(".java"):
                    java_files.append(os.path.join(root, file))

# Добавить явно указанные файлы
for extra_file in EXTRA_JAVA_FILES:
    extra_path = os.path.join(PROJECT_ROOT, extra_file)
    if os.path.exists(extra_path):
        java_files.append(extra_path)

# Компилировать
if java_files:
    compile_cmd = ["javac", "-encoding", "UTF-8", "-d", BUILD_DIR] + java_files
    print("Компиляция:", "\n".join(compile_cmd))
    result = subprocess.run(compile_cmd, capture_output=True, text=True)
    print(result.stdout)
    print(result.stderr)
    if result.returncode == 0:
        print("✅ Компиляция завершена успешно. .class файлы в папке build/backend/")
    else:
        print("❌ Ошибка компиляции.")
else:
    print("Не найдено .java файлов для компиляции.")
