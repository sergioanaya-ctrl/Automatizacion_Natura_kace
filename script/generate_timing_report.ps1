# ========================================================
# Script para generar reporte de tiempos de ejecucion
# ========================================================
# Este script lee los archivos XML de resultados de Junit
# y genera un reporte CSV/XLSX con tiempos de ejecucion
# FORMATOS: Tiempos en minutos con COMA y desglose de pasos

param(
    [string]$OutputFormat = "CSV"  # CSV o XLSX
)

# Importar funciones auxiliares
. "$PSScriptRoot\report_utilities.ps1"

# Ruta de los resultados
$testResultsPath = "build\test-results\test"
$reportFolder = "target\reports"
$csvOutput = "$reportFolder\test_timings_report.csv"
$xlsxOutput = "$reportFolder\test_timings_report.xlsx"

# Crear carpeta de reportes
if (-not (Test-Path $reportFolder)) {
    New-Item -ItemType Directory -Path $reportFolder | Out-Null
}

# Verificar que existe la carpeta
if (-not (Test-Path $testResultsPath)) {
    Write-Host "ERROR: No se encontro la carpeta de resultados: $testResultsPath" -ForegroundColor Red
    exit 1
}

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Analizando resultados de tests..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$testData = @()

# Leer cada archivo XML de resultados
Get-ChildItem -Path $testResultsPath -Filter "*.xml" | ForEach-Object {
    [xml]$xmlContent = Get-Content $_.FullName
    
    # Parsear información del testsuite
    $testSuite = $xmlContent.SelectSingleNode("//testsuite")
    
    if ($testSuite) {
        $suiteName = $testSuite.GetAttribute("name")
        $totalTests = $testSuite.GetAttribute("tests")
        $failures = $testSuite.GetAttribute("failures")
        $skipped = $testSuite.GetAttribute("skipped")
        $time = [double]$testSuite.GetAttribute("time")
        
        # Analizar cada testcase
        $testSuite.SelectNodes("testcase") | ForEach-Object {
            $testName = $_.GetAttribute("name")
            $testTime = [double]$_.GetAttribute("time")
            $testClass = $_.GetAttribute("classname")
            
            # Verificar si paso o fallo
            $status = "PASSED"
            $errorMsg = ""
            $failure = $_.SelectSingleNode("failure")
            if ($failure) {
                $status = "FAILED"
                $errorMsg = $failure.GetAttribute("message")
            }
            $skipped = $_.SelectSingleNode("skipped")
            if ($skipped) {
                $status = "SKIPPED"
            }
            
            $testData += [PSCustomObject]@{
                "Suite" = $suiteName
                "Test Name" = $testName
                "Class" = $testClass
                "Duration (min)" = Format-MinutesWithComma -Milliseconds ($testTime * 1000)
                "Duration (s)" = Format-SecondsWithComma -Milliseconds ($testTime * 1000)
                "Status" = $status
                "Error Message" = $errorMsg
            }
        }
    }
}

# Ordenar por tiempo descendente (convertir a número para comparar)
$testDataSorted = $testData | Sort-Object { [double]($_.Tiempo_ms / 1000) } -Descending

# Calcular estadisticas en base a valores en segundos
$timesInSeconds = @()
foreach ($test in $testData) {
    # Buscar el campo de tiempo (Duration en segundos o milisegundos)
    $timeValue = if ($test.Tiempo_ms) { $test.Tiempo_ms / 1000 } elseif ($test."Duration (s)") { [double]($test."Duration (s)" -replace ",", ".") } else { 0 }
    $timesInSeconds += $timeValue
}

$totalTimeSeconds = ($timesInSeconds | Measure-Object -Sum).Sum
$avgTimeSeconds = if ($timesInSeconds.Count -gt 0) { $totalTimeSeconds / $timesInSeconds.Count } else { 0 }
$maxTimeSeconds = ($timesInSeconds | Measure-Object -Maximum).Maximum
$minTimeSeconds = ($timesInSeconds | Measure-Object -Minimum).Minimum

$totalTests = @($testData).Count
$passedTests = @($testData | Where-Object { $_.Status -eq "PASSED" }).Count
$failedTests = @($testData | Where-Object { $_.Status -eq "FAILED" }).Count
$skippedTests = @($testData | Where-Object { $_.Status -eq "SKIPPED" }).Count

# Mostrar estadisticas
Write-Host "ESTADISTICAS DE EJECUCION:" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host "Total de tests: $totalTests" -ForegroundColor Green
Write-Host "Tests exitosos: $passedTests" -ForegroundColor Green
Write-Host "Tests fallidos: $failedTests" -ForegroundColor Red
Write-Host "Tests omitidos: $skippedTests" -ForegroundColor Yellow
Write-Host ""
Write-Host "Tiempo total: $(Format-TimeDisplay -Milliseconds ($totalTimeSeconds * 1000))" -ForegroundColor Cyan
Write-Host "Tiempo promedio: $(Format-TimeDisplay -Milliseconds ($avgTimeSeconds * 1000))" -ForegroundColor Cyan
Write-Host "Tiempo máximo: $(Format-TimeDisplay -Milliseconds ($maxTimeSeconds * 1000))" -ForegroundColor Yellow
Write-Host "Tiempo mínimo: $(Format-TimeDisplay -Milliseconds ($minTimeSeconds * 1000))" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# Exportar a CSV
Write-Host "Exportando a CSV: $csvOutput" -ForegroundColor Cyan
$testData | Export-Csv -Path $csvOutput -NoTypeInformation -Encoding UTF8

# Crear resumen estadistico
$statsSummary = @(
    [PSCustomObject]@{
        "Metrica" = "Total de Tests"
        "Valor" = $totalTests
    },
    [PSCustomObject]@{
        "Metrica" = "Tests Exitosos"
        "Valor" = $passedTests
    },
    [PSCustomObject]@{
        "Metrica" = "Tests Fallidos"
        "Valor" = $failedTests
    },
    [PSCustomObject]@{
        "Metrica" = "Tests Omitidos"
        "Valor" = $skippedTests
    },
    [PSCustomObject]@{
        "Metrica" = "Tiempo Total (min)"
        "Valor" = Format-MinutesWithComma -Milliseconds ($totalTimeSeconds * 1000)
    },
    [PSCustomObject]@{
        "Metrica" = "Tiempo Promedio (min)"
        "Valor" = Format-MinutesWithComma -Milliseconds ($avgTimeSeconds * 1000)
    },
    [PSCustomObject]@{
        "Metrica" = "Tiempo Máximo (min)"
        "Valor" = Format-MinutesWithComma -Milliseconds ($maxTimeSeconds * 1000)
    },
    [PSCustomObject]@{
        "Metrica" = "Tiempo Mínimo (min)"
        "Valor" = Format-MinutesWithComma -Milliseconds ($minTimeSeconds * 1000)
    }
)

# Generar Excel desde CSV usando función reutilizable
Write-Host ""
Write-Host "Generando Excel desde CSV..." -ForegroundColor Cyan

. "$PSScriptRoot\\generate_excel_from_csv.ps1"
$excelSuccess = Convert-CsvToExcel -csvPath $csvOutput -outputPath $reportFolder -worksheetName "Test Timings"

if ($excelSuccess) {
    Write-Host "  OK Excel generado exitosamente" -ForegroundColor Green
} else {
    Write-Host "  Nota: No se pudo generar Excel, pero CSV esta disponible" -ForegroundColor Yellow
}

# Mostrar los 10 tests mas lentos
Write-Host ""
Write-Host "TOP 10 TESTS MAS LENTOS:" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow
$testData | Select-Object -First 10 | ForEach-Object {
    $name = $_."Test Name"
    $duration = $_."Duration (min)"
    Write-Host "$name - $duration min - $($_.Status)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "REPORTES GENERADOS EXITOSAMENTE!" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "   CSV: $csvOutput" -ForegroundColor Gray
Write-Host ""



