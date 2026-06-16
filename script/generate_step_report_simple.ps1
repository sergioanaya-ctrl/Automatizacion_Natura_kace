
# ============================================
# SCRIPT: Excel Step Details - Version Simplificada
# ============================================

$serenityPath = ".\target\site\serenity"
$reportPath = ".\target\reports"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Crear carpeta si no existe
if (-not (Test-Path $reportPath)) {
    New-Item -ItemType Directory -Path $reportPath | Out-Null
}

# Función para formatear con coma decimal
function Format-WithComma {
    param([double]$Value, [int]$Decimals = 2)
    $rounded = [math]::Round($Value, $Decimals)
    return $rounded.ToString("N$Decimals", [System.Globalization.CultureInfo]::GetCultureInfo("es-CO"))
}

# Función para extraer pasos recursivamente
function Extract-TestSteps {
    param([array]$steps, [int]$level = 0, [string]$testName)
    $result = @()
    
    foreach ($step in $steps) {
        $timeMs = [int]$step.duration
        $timeS = Format-WithComma -Value ($timeMs / 1000) -Decimals 2
        
        $result += [PSCustomObject]@{
            Test = $testName
            Nivel = $level
            Descripcion = $step.description.Substring(0, [Math]::Min(150, $step.description.Length))
            Tiempo_ms = $timeMs
            Tiempo_s = $timeS
            Estado = $step.result
        }
        
        if ($step.children -and $step.children.Count -gt 0) {
            $result += Extract-TestSteps -steps $step.children -level ($level + 1) -testName $testName
        }
    }
    return $result
}

# Procesar todos los JSON files
$allSteps = @()
$testStats = @()

Write-Host "====== Generando Excel con detalles de pasos ======"

if (Test-Path $serenityPath) {
    $jsonFiles = Get-ChildItem "$serenityPath\*.json" -ErrorAction SilentlyContinue
    
    foreach ($jsonFile in $jsonFiles) {
        try {
            $content = Get-Content $jsonFile -Raw -Encoding UTF8 | ConvertFrom-Json
            
            if ($content.testSteps) {
                $testName = $content.title
                $steps = Extract-TestSteps -steps $content.testSteps -testName $testName
                $allSteps += $steps
                
                # Estadísticas por test
                $slowSteps = $steps | Where-Object { $_.Tiempo_ms -gt 5000 }
                $totalMs = ($steps | Measure-Object -Property Tiempo_ms -Sum).Sum
                $totalMin = Format-WithComma -Value ($totalMs / 60000) -Decimals 2
                
                $testStats += [PSCustomObject]@{
                    Test = $testName
                    TotalPasos = $steps.Count
                    PasosLentos = $slowSteps.Count
                    TiempoTotal_min = $totalMin
                    PasoMasLento = ($steps | Sort-Object Tiempo_ms -Descending | Select-Object -First 1).Descripcion.Substring(0, 60)
                }
                
                Write-Host "OK: $testName"
            }
        }
        catch {
            Write-Host "ERROR procesando $($jsonFile.Name): $_"
        }
    }
}

# ============================================
# GENERAR CSV CON DATOS COMPLETOS
# ============================================

$csvPath = "$reportPath\step_details_$timestamp.csv"
$csvLines = New-Object System.Collections.ArrayList

# Header
$null = $csvLines.Add('"Test","Descripcion","Nivel","Tiempo (ms)","Tiempo (s)","Estado"')

# Data
foreach ($step in $allSteps) {
    $desc = $step.Descripcion -replace '"', '""'
    $line = "`"$($step.Test)`",`"$desc`",$($step.Nivel),$($step.Tiempo_ms),$($step.Tiempo_s),$($step.Estado)"
    $null = $csvLines.Add($line)
}

$csvLines | Out-File -FilePath $csvPath -Encoding UTF8
Write-Host "OK: CSV con todos los pasos: $csvPath ($($allSteps.Count) pasos)"

# ============================================
# GENERAR CSV DE PASOS LENTOS (>5s)
# ============================================

$slowestSteps = $allSteps | Where-Object { $_.Tiempo_ms -gt 5000 } | Sort-Object Tiempo_ms -Descending | Select-Object -First 20

if ($slowestSteps.Count -gt 0) {
    $slowPath = "$reportPath\slowest_steps_$timestamp.csv"
    $slowLines = New-Object System.Collections.ArrayList
    
    $null = $slowLines.Add('"Test","Descripcion","Tiempo (ms)","Tiempo (s)","Estado"')
    
    foreach ($step in $slowestSteps) {
        $desc = $step.Descripcion -replace '"', '""'
        $line = "`"$($step.Test)`",`"$desc`",$($step.Tiempo_ms),$($step.Tiempo_s),$($step.Estado)"
        $null = $slowLines.Add($line)
    }
    
    $slowLines | Out-File -FilePath $slowPath -Encoding UTF8
    Write-Host "OK: CSV pasos lentos (>5s): $slowPath ($($slowestSteps.Count) pasos)"
}

# ============================================
# GENERAR CSV DE ESTADISTICAS POR TEST
# ============================================

if ($testStats.Count -gt 0) {
    $statsPath = "$reportPath\test_stats_$timestamp.csv"
    $statsLines = New-Object System.Collections.ArrayList
    
    $null = $statsLines.Add('"Test","Total Pasos","Pasos Lentos","Tiempo Total (min)","Paso mas Lento"')
    
    foreach ($stat in $testStats) {
        $testName = $stat.Test -replace '"', '""'
        $slowestPaso = $stat.PasoMasLento -replace '"', '""'
        $line = "`"$testName`",$($stat.TotalPasos),$($stat.PasosLentos),$($stat.TiempoTotal_min),`"$slowestPaso`""
        $null = $statsLines.Add($line)
    }
    
    $statsLines | Out-File -FilePath $statsPath -Encoding UTF8
    Write-Host "OK: CSV estadisticas por test: $statsPath ($($testStats.Count) tests)"
}

# ============================================
# GENERAR HTML DASHBOARD
# ============================================

$htmlPath = "$reportPath\step_details_report_$timestamp.html"
$htmlContent = @"
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Step Details Report - SARA3</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
            color: #333;
            padding: 20px;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            padding: 30px;
        }
        h1 {
            color: #1e3a8a;
            border-bottom: 3px solid #1e3a8a;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }
        h2 {
            color: #2563eb;
            margin-top: 30px;
            margin-bottom: 15px;
            font-size: 18px;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 30px;
        }
        .stat-box {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        .stat-box .value {
            font-size: 32px;
            font-weight: bold;
            margin: 10px 0;
        }
        .stat-box .label {
            font-size: 14px;
            opacity: 0.9;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 15px 0;
            font-size: 13px;
        }
        thead {
            background: #f3f4f6;
            border-bottom: 2px solid #d1d5db;
        }
        th {
            padding: 12px;
            text-align: left;
            font-weight: 600;
            color: #1f2937;
        }
        td {
            padding: 10px 12px;
            border-bottom: 1px solid #e5e7eb;
        }
        tr:hover {
            background: #f9fafb;
        }
        .status-success {
            color: #059669;
            font-weight: 500;
        }
        .status-failed {
            color: #dc2626;
            font-weight: 500;
        }
        .time-ok {
            color: #059669;
        }
        .time-warning {
            color: #d97706;
        }
        .time-critical {
            color: #dc2626;
            font-weight: 500;
        }
        .footer {
            margin-top: 30px;
            padding-top: 15px;
            border-top: 1px solid #e5e7eb;
            text-align: center;
            color: #6b7280;
            font-size: 12px;
        }
        .test-section {
            margin: 20px 0;
            padding: 15px;
            background: #f9fafb;
            border-left: 4px solid #2563eb;
            border-radius: 4px;
        }
        .test-title {
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Step Details Report - SARA3</h1>
        <p style="color: #6b7280; margin-bottom: 20px;">
            Fecha: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss') | Tests: $($testStats.Count) | Total Pasos: $($allSteps.Count)
        </p>

        <div class="summary">
            <div class="stat-box" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <div class="label">Total Tests</div>
                <div class="value">$($testStats.Count)</div>
            </div>
            <div class="stat-box" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <div class="label">Total Pasos</div>
                <div class="value">$($allSteps.Count)</div>
            </div>
            <div class="stat-box" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <div class="label">Pasos Lentos (&gt;5s)</div>
                <div class="value">$($slowestSteps.Count)</div>
            </div>
            <div class="stat-box" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
                <div class="label">% Pasos Lentos</div>
                <div class="value">$(if ($allSteps.Count -gt 0) { [math]::Round(($slowestSteps.Count / $allSteps.Count) * 100, 1) } else { 0 })%</div>
            </div>
        </div>

        <h2>Pasos Mas Lentos (Top 20)</h2>
        <table>
            <thead>
                <tr>
                    <th>Test</th>
                    <th>Descripcion</th>
                    <th>Tiempo (s)</th>
                    <th>Estado</th>
                </tr>
            </thead>
            <tbody>
"@

        foreach ($step in $slowestSteps) {
            $timeClass = if ($step.Tiempo_ms -gt 15000) { "time-critical" } else { "time-warning" }
            $statusClass = if ($step.Estado -eq "SUCCESS") { "status-success" } else { "status-failed" }
            $htmlContent += @"
                <tr>
                    <td>$($step.Test.Substring(0, [Math]::Min(40, $step.Test.Length)))</td>
                    <td>$($step.Descripcion.Substring(0, 60))</td>
                    <td class="$timeClass">$($step.Tiempo_s) s</td>
                    <td class="$statusClass">$($step.Estado)</td>
                </tr>
"@
        }

        $htmlContent += @"
            </tbody>
        </table>

        <h2>Estadisticas por Test</h2>
        <table>
            <thead>
                <tr>
                    <th>Test</th>
                    <th>Total Pasos</th>
                    <th>Pasos Lentos</th>
                    <th>Tiempo Total</th>
                </tr>
            </thead>
            <tbody>
"@

        foreach ($stat in $testStats) {
            $htmlContent += @"
                <tr>
                    <td>$($stat.Test)</td>
                    <td>$($stat.TotalPasos)</td>
                    <td>$($stat.PasosLentos)</td>
                    <td>$($stat.TiempoTotal_min) min</td>
                </tr>
"@
        }

        $htmlContent += @"
            </tbody>
        </table>

        <div class="footer">
            <p>SARA3 Automation Test Suite - Step Details Analysis</p>
            <p>Reporte generado: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')</p>
        </div>
    </div>
</body>
</html>
"@

$htmlContent | Out-File -FilePath $htmlPath -Encoding UTF8
Write-Host "OK: HTML dashboard: $htmlPath"

# ============================================
# RESUMEN
# ============================================
Write-Host ""
Write-Host "====== REPORTE COMPLETADO ======"
Write-Host "Archivos generados en: $reportPath"
Write-Host ""
