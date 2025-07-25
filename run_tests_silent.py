#!/usr/bin/env python3
"""
Скрипт для запуска тестов с подавлением access violation сообщений
"""
import sys
import os
import subprocess

def run_tests_silent():
    """Запускает тесты с подавлением stderr"""
    
    # Получаем аргументы командной строки
    args = sys.argv[1:] if len(sys.argv) > 1 else []
    
    # Команда для запуска pytest
    cmd = [sys.executable, "-m", "pytest"] + args
    
    print("🚀 Запуск тестов с подавлением access violation...")
    
    try:
        # Запускаем процесс с перенаправлением stderr в null
        if os.name == 'nt':  # Windows
            with open(os.devnull, 'w') as null_file:
                result = subprocess.run(cmd, stderr=null_file, stdout=subprocess.PIPE, text=True)
        else:  # Linux/Mac
            result = subprocess.run(cmd, stderr=subprocess.DEVNULL, stdout=subprocess.PIPE, text=True)
        
        # Выводим результат
        print(result.stdout)
        
        # Возвращаем код выхода
        sys.exit(result.returncode)
        
    except Exception as e:
        print(f"❌ Ошибка при запуске тестов: {e}")
        sys.exit(1)

if __name__ == "__main__":
    run_tests_silent() 