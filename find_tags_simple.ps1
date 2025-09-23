# Simple script to find all log tags
$projectPath = "D:\src\BudgetMaster\android-app"
Set-Location $projectPath

Write-Host "Searching for log tags..."

$javaFiles = Get-ChildItem -Recurse -Include "*.java" -Path "app\src\main\java"
$allTags = @()

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
    
    if ($content) {
        $matches = [regex]::Matches($content, 'TAG\s*=\s*"([^"]+)"')
        
        foreach ($match in $matches) {
            $tag = $match.Groups[1].Value
            if ($tag -and $tag -ne "") {
                $allTags += $tag
            }
        }
    }
}

$uniqueTags = $allTags | Sort-Object -Unique

Write-Host "Found tags:"
foreach ($tag in $uniqueTags) {
    Write-Host "  $tag"
}

Write-Host "`nJSON for log_config.json:"
Write-Host "{"
Write-Host '    "description": "Logging configuration for BudgetMaster",'
Write-Host '    "default_log_level": "DEBUG",'
Write-Host '    "log_levels": {'

$jsonLines = @()
foreach ($tag in $uniqueTags) {
    $jsonLines += "        `"$tag`": `"DEBUG`""
}

Write-Host ($jsonLines -join ",`n")
Write-Host "    }"
Write-Host "}"
