#!/usr/bin/env python3
"""
Скрипт для запуска тестов с подавлением access violation через переменные окружения
"""
import sys
import os
import subprocess

def run_tests_with_env():
    """Запускает тесты с переменными окружения для подавления сообщений"""
    
    # Получаем аргументы командной строки
    args = sys.argv[1:] if len(sys.argv) > 1 else []
    
    # Команда для запуска pytest
    cmd = [sys.executable, "-m", "pytest"] + args
    
    # Переменные окружения для подавления сообщений
    env = os.environ.copy()
    env['PYTHONWARNINGS'] = 'ignore'
    env['JPYPE_SILENT'] = '1'  # Если поддерживается
    
    print("🚀 Запуск тестов с подавлением сообщений...")
    
    try:
        # Запускаем процесс с модифицированными переменными окружения
        result = subprocess.run(cmd, env=env, capture_output=True, text=True)
        
        # Выводим результат
        print(result.stdout)
        
        # Возвращаем код выхода
        sys.exit(result.returncode)
        
    except Exception as e:
        print(f"❌ Ошибка при запуске тестов: {e}")
        sys.exit(1)

if __name__ == "__main__":
    run_tests_with_env() 