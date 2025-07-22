#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys

from loader_common import cleanup_loader_environment, get_loader_class, setup_loader_environment


def run_currency_loader():
    """Запускает CurrencyDataLoader"""
    print("Загрузка валют из CSV...")

    try:
        # Настройка окружения
        if not setup_loader_environment():
            return False

        # Получаем Java класс CurrencyDataLoader
        CurrencyDataLoader = get_loader_class("tools.CurrencyDataLoader")

        if not CurrencyDataLoader:
            print("❌ Не удалось загрузить CurrencyDataLoader")
            return False

        # Запускаем загрузчик
        CurrencyDataLoader.main([])

        return True

    except Exception as e:
        print(f"❌ Ошибка при выполнении: {e}")
        import traceback

        traceback.print_exc()
        return False

    finally:
        # Очистка
        cleanup_loader_environment()


def main():
    """Главная функция"""
    success = run_currency_loader()
    return 0 if success else 1


if __name__ == "__main__":
    exit_code = main()
    sys.exit(exit_code)
