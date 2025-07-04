import gzip
import json
from pprint import pprint

# путь к файлу
filename = '../resorses/FinArchitect-2025-07-04_15-17.json.gz'

# открыть и прочитать содержимое
with gzip.open(filename, 'rt', encoding='utf-8') as f:
    data = json.load(f)

# теперь data — это обычный Python-объект (dict, list и т.п.)
pprint(data)
