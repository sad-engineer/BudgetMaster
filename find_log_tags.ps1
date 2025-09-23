# Скрипт для поиска всех тегов логирования в проекте
# Запуск: .\find_log_tags.ps1

Write-Host "🔍 Поиск всех тегов логирования в проекте..." -ForegroundColor Green

# Переходим в папку проекта
$projectPath = "D:\src\BudgetMaster\android-app"
Set-Location $projectPath

Write-Host "📁 Поиск в папке: $projectPath" -ForegroundColor Yellow

# Ищем все Java файлы и извлекаем теги
$javaFiles = Get-ChildItem -Recurse -Include "*.java" -Path "app\src\main\java"

Write-Host "📄 Найдено Java файлов: $($javaFiles.Count)" -ForegroundColor Cyan

$allTags = @()

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
    
    if ($content) {
        # Ищем паттерн: TAG = "что-то"
        $matches = [regex]::Matches($content, 'TAG\s*=\s*"([^"]+)"')
        
        foreach ($match in $matches) {
            $tag = $match.Groups[1].Value
            if ($tag -and $tag -ne "") {
                $allTags += $tag
            }
        }
    }
}

# Убираем дубликаты и сортируем
$uniqueTags = $allTags | Sort-Object -Unique

Write-Host "`n🏷️  Найденные теги ($($uniqueTags.Count)):" -ForegroundColor Green
Write-Host "=" * 50

foreach ($tag in $uniqueTags) {
    Write-Host "  $tag" -ForegroundColor White
}

Write-Host "`n📋 JSON для log_config.json:" -ForegroundColor Green
Write-Host "=" * 50

# Генерируем JSON
$jsonLines = @()
foreach ($tag in $uniqueTags) {
    $jsonLines += "        `"$tag`": `"DEBUG`""
}

$jsonContent = @"
{
    "description": "Конфигурация логирования для BudgetMaster",
    "default_log_level": "DEBUG",
    "log_levels": {
$($jsonLines -join ",`n")
    }
}
"@

Write-Host $jsonContent -ForegroundColor Cyan

# Сохраняем в файл
$outputFile = "found_log_tags.json"
$jsonContent | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "`n💾 Результат сохранен в: $outputFile" -ForegroundColor Green
Write-Host "`n✅ Готово! Скопируйте содержимое в log_config.json" -ForegroundColor Green
