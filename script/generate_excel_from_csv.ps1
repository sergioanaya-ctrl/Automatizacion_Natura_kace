# Funcion reutilizable para generar Excel desde CSV
# Independiente de Office - Funciona con LibreOffice

param(
    [string]$csvPath,
    [string]$outputPath,
    [string]$worksheetName = "Datos"
)

function Convert-CsvToExcel {
    param(
        [string]$csvPath,
        [string]$outputPath,
        [string]$worksheetName = "Datos"
    )
    
    if (-not (Test-Path $csvPath)) {
        Write-Host "ERROR: CSV no encontrado: $csvPath" -ForegroundColor Red
        return $false
    }
    
    $excelGenerated = $false
    
    # Opcion 1: Intentar con ImportExcel (recomendado)
    try {
        Import-Module ImportExcel -ErrorAction SilentlyContinue
        $data = Import-Csv -Path $csvPath -Encoding UTF8
        
        $timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
        $excelPath = Join-Path $outputPath "report_$timestamp.xlsx"
        
        Write-Host "  Generando Excel con ImportExcel..." -ForegroundColor Gray
        $data | Export-Excel -Path $excelPath -WorksheetName $worksheetName -AutoSize -TableName "ReportData" -Encoding UTF8
        
        Write-Host "  OK Excel generado: $(Split-Path $excelPath -Leaf)" -ForegroundColor Green
        return $true
        
    } catch {
        Write-Host "  Nota: ImportExcel no disponible, intente instalar con: Install-Module -Name ImportExcel -Repository PSGallery -Force" -ForegroundColor Gray
    }
    
    Write-Host "  Usando fallback: GenerandoCSV..." -ForegroundColor Gray
    Write-Host "  OK CSV disponible en: $csvPath" -ForegroundColor Green
    return $false
}

# Ejecutar si se llama directamente
if ($csvPath -and $outputPath) {
    Convert-CsvToExcel -csvPath $csvPath -outputPath $outputPath -worksheetName $worksheetName
}
