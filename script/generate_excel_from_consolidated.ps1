# Generar Excel desde CSV consolidados
# Script para crear archivo Excel con multiples hojas

param(
    [string]$consolidationFolder = ".\reports_consolidation"
)

if (-not (Test-Path $consolidationFolder)) {
    Write-Host "ERROR: Carpeta no encontrada: $consolidationFolder" -ForegroundColor Red
    exit 1
}

# Buscar archivos mas recientes
$statsFile = Get-ChildItem -Path $consolidationFolder -Filter "consolidated_report_stats_*.csv" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$machineFile = Get-ChildItem -Path $consolidationFolder -Filter "consolidated_report_by_machine_*.csv" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$allStepsFile = Get-ChildItem -Path $consolidationFolder -Filter "consolidated_report_20*.csv" -Exclude "*stats*", "*machine*" | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $statsFile) {
    Write-Host "ERROR: No se encontraron archivos consolidados" -ForegroundColor Red
    exit 1
}

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$excelOutputPath = "$consolidationFolder\consolidated_report_$timestamp.xlsx"

Write-Host ""
Write-Host "GENERADOR DE EXCEL - REPORTES CONSOLIDADOS" -ForegroundColor Cyan
Write-Host ""

Write-Host "Archivos encontrados:" -ForegroundColor Yellow
Write-Host "  Stats:  $($statsFile.Name)" -ForegroundColor White
if ($machineFile) { Write-Host "  Machines: $($machineFile.Name)" -ForegroundColor White }
if ($allStepsFile) { Write-Host "  All Steps: $($allStepsFile.Name)" -ForegroundColor White }
Write-Host ""

Write-Host "Cargando datos..." -ForegroundColor Cyan

$statsData = Import-Csv -Path $statsFile.FullName -Encoding UTF8
Write-Host "  OK Stats: $($statsData.Count) registros" -ForegroundColor Green

$machinesData = $null
if ($machineFile) {
    $machinesData = Import-Csv -Path $machineFile.FullName -Encoding UTF8
    Write-Host "  OK Machines: $($machinesData.Count) registros" -ForegroundColor Green
}

$allStepsData = $null
if ($allStepsFile) {
    $allStepsData = Import-Csv -Path $allStepsFile.FullName -Encoding UTF8
    Write-Host "  OK All Steps: $($allStepsData.Count) registros" -ForegroundColor Green
}

Write-Host ""
Write-Host "Generando Excel..." -ForegroundColor Cyan

try {
    Import-Module ImportExcel -ErrorAction Stop
    
    # Resumen general
    $fecha = Get-Date -Format "dd/MM/yyyy HH:mm:ss"
    $totalTests = $statsData.Count
    $passedTests = (@($statsData | Where-Object { $_.Estado -eq "PASSED" })).Count
    $failedTests = (@($statsData | Where-Object { $_.Estado -eq "FAILED" })).Count
    
    $resumen = @(
        [PSCustomObject]@{ Metrica = "Fecha"; Valor = $fecha }
        [PSCustomObject]@{ Metrica = "Total Tests"; Valor = $totalTests }
        [PSCustomObject]@{ Metrica = "Tests PASSED"; Valor = $passedTests }
        [PSCustomObject]@{ Metrica = "Tests FAILED"; Valor = $failedTests }
        [PSCustomObject]@{ Metrica = "Tasa Exito "; Valor = if($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 2) } else { 0 } }
    )
    
    # Hoja 1
    $resumen | Export-Excel -Path $excelOutputPath -WorksheetName "Resumen" -AutoSize -TableStyle "Medium2" -Force
    Write-Host "  OK Hoja 1: Resumen" -ForegroundColor Green
    
    # Hoja 2
    if ($machinesData) {
        $machinesData | Export-Excel -Path $excelOutputPath -WorksheetName "Por Maquina" -AutoSize -TableStyle "Medium2" -Append
        Write-Host "  OK Hoja 2: Por Maquina ($($machinesData.Count))" -ForegroundColor Green
    }
    
    # Hoja 3
    if ($statsData.Count -le 50000) {
        $statsData | Export-Excel -Path $excelOutputPath -WorksheetName "Tests" -AutoSize -TableStyle "Light1" -Append
        Write-Host "  OK Hoja 3: Tests ($($statsData.Count))" -ForegroundColor Green
    }
    
    # Hoja 4
    if ($allStepsData) {
        if ($allStepsData.Count -le 100000) {
            $allStepsData | Export-Excel -Path $excelOutputPath -WorksheetName "Todos los Pasos" -AutoSize -TableStyle "Light1" -Append
            Write-Host "  OK Hoja 4: Todos los Pasos ($($allStepsData.Count))" -ForegroundColor Green
        }
    }
    
    Write-Host ""
    Write-Host "EXITO" -ForegroundColor Green
    Write-Host "Excel: $excelOutputPath" -ForegroundColor Cyan
    
    $info = Get-Item $excelOutputPath
    Write-Host "Tamano: $([math]::Round($info.Length/1MB, 2)) MB" -ForegroundColor Cyan
    
} catch {
    Write-Host "ERROR: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
