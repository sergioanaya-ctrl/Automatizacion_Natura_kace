. "$PSScriptRoot\\report_utilities.ps1"

# ============================================
# SCRIPT: Generar Excel con Detalles de Pasos
# ============================================

$serenityPath = ".\target\site\serenity"
$reportPath = ".\target\reports"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Crear carpeta si no existe
if (-not (Test-Path $reportPath)) {
    New-Item -ItemType Directory -Path $reportPath | Out-Null
}

# Función para extraer pasos recursivamente
function Extract-TestSteps {
    param([array]$steps, [int]$level = 0)
    $result = @()
    
    foreach ($step in $steps) {
        $timeMs = [int]$step.duration
        $result += [PSCustomObject]@{
            Nivel = $level
            Descripcion = $step.description
            Tiempo_ms = $timeMs
            Tiempo_s = [math]::Round($timeMs / 1000, 2)
            Tiempo_Formateado = Format-SecondsWithComma -Milliseconds $timeMs
            Estado = $step.result
        }
        
        if ($step.children -and $step.children.Count -gt 0) {
            $result += Extract-TestSteps -steps $step.children -level ($level + 1)
        }
    }
    return $result
}

# Procesar todos los JSON files
$allSteps = @()
$testStats = @()

Write-Host "====== Extrayendo detalles de pasos desde Serenity JSON ======"

if (Test-Path $serenityPath) {
    $jsonFiles = Get-ChildItem "$serenityPath\*.json" -ErrorAction SilentlyContinue
    
    foreach ($jsonFile in $jsonFiles) {
        try {
            $content = Get-Content $jsonFile -Raw -Encoding UTF8 | ConvertFrom-Json
            
            if ($content.testSteps) {
                $steps = Extract-TestSteps -steps $content.testSteps
                $testName = $content.title
                
                # Agregar nombre de test a cada paso
                foreach ($step in $steps) {
                    $step | Add-Member -NotePropertyName "Test" -NotePropertyValue $testName
                }
                
                $allSteps += $steps
                
                # Estadísticas por test
                $slowSteps = $steps | Where-Object { $_.Tiempo_ms -gt 5000 }
                
                $testStats += [PSCustomObject]@{
                    Test = $testName
                    TotalPasos = $steps.Count
                    PasosLentos = $slowSteps.Count
                    TiempoTotal_ms = ($steps | Measure-Object -Property Tiempo_ms -Sum).Sum
                    TiempoTotal_Formateado = Format-MinutesWithComma -Milliseconds (($steps | Measure-Object -Property Tiempo_ms -Sum).Sum)
                    PasoMasLento = ($steps | Sort-Object Tiempo_ms -Descending | Select-Object -First 1).Descripcion.Substring(0, 60)
                    TiempoMasLento = Format-SecondsWithComma -Milliseconds ($steps | Measure-Object -Property Tiempo_ms -Maximum).Maximum
                }
                
                Write-Host "OK: $testName"
            }
        }
        catch {
            Write-Host "ERROR procesando $($jsonFile.Name): $_"
        }
    }
}

# Crear datos para Excel
$summary = @()
$summary += [PSCustomObject]@{
    "Metrica" = "Fecha y Hora"
    "Valor" = (Get-Date -Format "dd/MM/yyyy HH:mm:ss")
}
$summary += [PSCustomObject]@{
    "Metrica" = "Total Tests"
    "Valor" = $testStats.Count
}
$summary += [PSCustomObject]@{
    "Metrica" = "Total Pasos"
    "Valor" = $allSteps.Count
}
$summary += [PSCustomObject]@{
    "Metrica" = "Pasos Lentos (>5s)"
    "Valor" = ($allSteps | Where-Object { $_.Tiempo_ms -gt 5000 }).Count
}

$slowestSteps = $allSteps | Where-Object { $_.Tiempo_ms -gt 5000 } | Sort-Object Tiempo_ms -Descending | Select-Object -First 20

# ============================================
# GENERAR EXCEL
# ============================================

try {
    $excelPath = "$reportPath\step_details_$timestamp.xlsx"
    
    # Usar ExcelPackage para crear sin dependencias externas
    Add-Type -Path ".\target\site\serenity\..\..\..\..\packages\EPPlus\EPPlus.dll" -ErrorAction SilentlyContinue
    
    if ($null -eq ([System.Management.Automation.PSTypeName]'OfficeOpenXml.ExcelPackage').Type) {
        # Alternativa: Usar ImportExcel si está disponible
        $importExcelAvailable = Get-Module -ListAvailable -Name ImportExcel
        
        if ($importExcelAvailable) {
            Import-Module ImportExcel -ErrorAction SilentlyContinue
            
            # Crear Excel con ImportExcel
            $excelParams = @{
                Path = $excelPath
                WorksheetName = "Resumen"
                AutoSize = $true
                TableStyle = "Light1"
            }
            
            $summary | Export-Excel @excelParams
            
            # Agregar hoja de todos los pasos
            $excelParams['WorksheetName'] = "Todos los Pasos"
            $excelParams['Append'] = $true
            
            $allSteps | Select-Object @{N="Test"; E={$_.Test}},
                                       @{N="Descripción"; E={$_.Descripcion.Substring(0, 80)}},
                                       @{N="Nivel"; E={$_.Nivel}},
                                       @{N="Tiempo (ms)"; E={$_.Tiempo_ms}},
                                       @{N="Tiempo (s)"; E={$_.Tiempo_Formateado}},
                                       @{N="Estado"; E={$_.Estado}} | 
                Export-Excel @excelParams
            
            # Agregar hoja de pasos lentos
            if ($slowestSteps.Count -gt 0) {
                $excelParams['WorksheetName'] = "Top Pasos Lentos"
                
                $slowestSteps | Select-Object @{N="Test"; E={$_.Test}},
                                              @{N="Descripción"; E={$_.Descripcion.Substring(0, 80)}},
                                              @{N="Tiempo (ms)"; E={$_.Tiempo_ms}},
                                              @{N="Tiempo (s)"; E={$_.Tiempo_Formateado}},
                                              @{N="Estado"; E={$_.Estado}} | 
                    Export-Excel @excelParams
            }
            
            # Agregar hoja de estadísticas por test
            if ($testStats.Count -gt 0) {
                $excelParams['WorksheetName'] = "Estadísticas por Test"
                
                $testStats | Select-Object @{N="Test"; E={$_.Test}},
                                           @{N="Total Pasos"; E={$_.TotalPasos}},
                                           @{N="Pasos Lentos"; E={$_.PasosLentos}},
                                           @{N="Tiempo Total"; E={$_.TiempoTotal_Formateado}},
                                           @{N="Paso más Lento"; E={$_.PasoMasLento}} | 
                    Export-Excel @excelParams
            }
            
            Write-Host ""
            Write-Host "OK: Excel generado: $excelPath"
            Write-Host ""
            Write-Host "====== ESTADISTICAS ======"
            Write-Host "Tests:           $($testStats.Count)"
            Write-Host "Total Pasos:     $($allSteps.Count)"
            Write-Host "Pasos Lentos:    $($slowestSteps.Count)"
            Write-Host ""
        } else {
            Write-Host "ERROR: ImportExcel module no disponible. Instalalo con: Install-Module ImportExcel"
        }
    }
}
catch {
    Write-Host "ERROR generando Excel: $_"
}

# ============================================
# GENERAR CSV MEJORADO (con proper quoting)
# ============================================

try {
    $csvPath = "$reportPath\step_details_$timestamp.csv"
    $csvContent = @()
    $csvContent += '"Test","Descripcion","Tiempo (ms)","Tiempo (s)","Estado"'
    
    foreach ($step in $allSteps) {
        $desc = $step.Descripcion -replace '"', '""'  # Escape quotes
        $csvContent += "`"$($step.Test)`",`"$desc`",$($step.Tiempo_ms),$($step.Tiempo_Formateado),$($step.Estado)"
    }
    
    $csvContent | Out-File -FilePath $csvPath -Encoding UTF8
    Write-Host "OK: CSV generado: $csvPath"
}
catch {
    Write-Host "ERROR generando CSV: $_"
}

# ============================================
# GENERAR CSV DE PASOS LENTOS
# ============================================

if ($slowestSteps.Count -gt 0) {
    try {
        $slowPath = "$reportPath\slowest_steps_$timestamp.csv"
        $slowContent = @()
        $slowContent += '"Test","Paso","Tiempo (ms)","Tiempo (s)","Estado"'
        
        foreach ($step in $slowestSteps) {
            $desc = $step.Descripcion -replace '"', '""'
            $slowContent += "`"$($step.Test)`",`"$desc`",$($step.Tiempo_ms),$($step.Tiempo_Formateado),$($step.Estado)"
        }
        
        $slowContent | Out-File -FilePath $slowPath -Encoding UTF8
        Write-Host "OK: CSV Pasos Lentos: $slowPath"
    }
    catch {
        Write-Host "ERROR generando CSV lentos: $_"
    }
}

Write-Host ""
Write-Host "====== REPORTE COMPLETADO ======"



