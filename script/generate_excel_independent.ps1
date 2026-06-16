# Genera EXCEL independiente de Office usando ImportExcel
# Compatible con LibreOffice y máquinas sin Office instalado

param(
    [string]$csvPath,
    [string]$outputPath
)

Write-Host ""
Write-Host "Intentando generar Excel sin dependencia de Office..." -ForegroundColor Cyan

# Verificar si ImportExcel está disponible
$importExcelAvailable = $false
try {
    Import-Module ImportExcel -ErrorAction SilentlyContinue
    $importExcelAvailable = $true
    Write-Host "✓ Módulo ImportExcel encontrado" -ForegroundColor Green
} catch {
    Write-Host "⚠ ImportExcel no instalado" -ForegroundColor Yellow
}

if ($importExcelAvailable) {
    try {
        # Leer CSV
        $data = Import-Csv -Path $csvPath -Encoding UTF8
        
        # Generar XLSX
        $timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
        $xlsxPath = "$outputPath\app_performance_consolidated_$timestamp.xlsx"
        
        Write-Host "Generando Excel con ImportExcel..." -ForegroundColor Gray
        
        # Exportar a Excel con formato
        $data | Export-Excel -Path $xlsxPath -WorksheetName "Performance" -AutoSize -TableName "PerformanceData" -Encoding UTF8
        
        Write-Host "✓ Excel generado: $xlsxPath" -ForegroundColor Green
        Write-Host ""
        Write-Host "Abriendo Excel..." -ForegroundColor Cyan
        Start-Process $xlsxPath
        
    } catch {
        Write-Host "⚠ Error generando Excel: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "   CSV disponible para abrir en Excel/LibreOffice manualmente" -ForegroundColor Gray
    }
} else {
    Write-Host ""
    Write-Host "Para instalar ImportExcel (módulo gratuito):" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  powershell -Command 'Install-Module -Name ImportExcel -Repository PSGallery -Force'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "O descárgalo desde: https://github.com/dfinke/ImportExcel" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Por ahora, los datos están en CSV y pueden abrirse en Excel/LibreOffice" -ForegroundColor Cyan
}
