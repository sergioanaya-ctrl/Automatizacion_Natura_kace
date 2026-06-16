# ========================================================
# SCRIPT: CONSOLIDACIÓN DE TIEMPOS DE PASOS
# ========================================================
# Genera un reporte consolidado mostrando:
# - Tiempo de cada paso en cada test
# - Estadísticas de pasos (promedio, máximo, mínimo)
# - Identificar cuál paso fue el más lento

param(
    [string]$OutputFormat = "CSV"  # CSV, HTML o BOTH
)

# Importar funciones auxiliares
. "$PSScriptRoot\report_utilities.ps1"

# Configuración de rutas
$performanceLogsPath = "target\app_performance_logs"
$testResultsPath = "build\test-results\test"
$reportFolder = "target\reports"
$csvOutput = "$reportFolder\step_consolidation_report.csv"
$htmlOutput = "$reportFolder\step_consolidation_report.html"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Crear carpeta de reportes
if (-not (Test-Path $reportFolder)) {
    New-Item -ItemType Directory -Path $reportFolder | Out-Null
}

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "CONSOLIDACIÓN DE TIEMPOS DE PASOS" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# =====================================================
# 1. CARGAR DATOS DE PERFORMANCE (TIEMPOS DE PASOS)
# =====================================================

Write-Host "Cargando datos de performance de pasos..." -ForegroundColor Yellow

$stepData = Load-PerformanceStepData -PerformanceLogsPath $performanceLogsPath

Write-Host "  [OK] Se cargaron datos de $($stepData.Keys.Count) test(s)" -ForegroundColor Green

# =====================================================
# 2. ANALIZAR ESTADÍSTICAS DE PASOS
# =====================================================

Write-Host ""
Write-Host "Analizando estadísticas de pasos..." -ForegroundColor Yellow

$analysis = Analyze-StepPerformance -StepData $stepData
$stepAnalysis = $analysis.Analysis
$totalTestsWithSteps = $analysis.TotalTestsWithSteps

Write-Host "  [OK] Se analizaron $($stepAnalysis.Count) paso(s) único(s)" -ForegroundColor Green
Write-Host "  [OK] Total de tests con datos de pasos: $totalTestsWithSteps" -ForegroundColor Green

# =====================================================
# 3. IDENTIFICAR PASO MÁS LENTO
# =====================================================

if ($stepAnalysis.Count -gt 0) {
    $slowestStep = $stepAnalysis[0]
    $fastestStep = $stepAnalysis[-1]
    
    Write-Host ""
    Write-Host "PASO MÁS LENTO:" -ForegroundColor Red
    Write-Host "  Paso: $($slowestStep.Paso)" -ForegroundColor Red
    Write-Host "  Promedio: $($slowestStep.'Tiempo Promedio (s)') s" -ForegroundColor Red
    Write-Host "  Máximo: $($slowestStep.'Tiempo Máximo (s)') s" -ForegroundColor Red
    Write-Host "  Ejecutado: $($slowestStep.Ejecuciones) veces" -ForegroundColor Red
}

# =====================================================
# 4. EXPORTAR A CSV
# =====================================================

if ($OutputFormat -eq "CSV" -or $OutputFormat -eq "BOTH") {
    Write-Host ""
    Write-Host "Exportando a CSV..." -ForegroundColor Cyan
    
    $stepAnalysis | Export-Csv -Path $csvOutput -NoTypeInformation -Encoding UTF8
    Write-Host "  [OK] Archivo guardado: $csvOutput" -ForegroundColor Green
}

# =====================================================
# 5. GENERAR REPORTE HTML
# =====================================================

if ($OutputFormat -eq "HTML" -or $OutputFormat -eq "BOTH") {
    Write-Host ""
    Write-Host "Generando reporte HTML..." -ForegroundColor Cyan
    
    $htmlContent = @"
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consolidación de Tiempos de Pasos - Sara3</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            min-height: 100vh;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .header p {
            font-size: 1.1em;
            opacity: 0.9;
        }
        
        .content {
            padding: 40px;
        }
        
        .section {
            margin-bottom: 40px;
        }
        
        .section h2 {
            color: #333;
            border-bottom: 3px solid #667eea;
            padding-bottom: 10px;
            margin-bottom: 20px;
            font-size: 1.8em;
        }
        
        .metrics {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .metric-card {
            background: #f8f9fa;
            border-left: 5px solid #667eea;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        
        .metric-card h3 {
            color: #666;
            font-size: 0.9em;
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .metric-card .value {
            color: #667eea;
            font-size: 1.8em;
            font-weight: bold;
        }
        
        .metric-card.critical {
            border-left-color: #ff6b6b;
            background: #ffe0e0;
        }
        
        .metric-card.critical .value {
            color: #ff6b6b;
        }
        
        .metric-card.warning {
            border-left-color: #ffa500;
            background: #fff5e6;
        }
        
        .metric-card.warning .value {
            color: #ffa500;
        }
        
        .data-table {
            width: 100%;
            border-collapse: collapse;
            background: white;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            border-radius: 8px;
            overflow: hidden;
        }
        
        .data-table thead {
            background: #f8f9fa;
            border-bottom: 2px solid #667eea;
        }
        
        .data-table th {
            padding: 15px;
            text-align: left;
            font-weight: 600;
            color: #333;
            border-right: 1px solid #e9ecef;
        }
        
        .data-table td {
            padding: 12px 15px;
            border-bottom: 1px solid #e9ecef;
        }
        
        .data-table tbody tr:hover {
            background: #f8f9fa;
        }
        
        .data-table .good {
            color: #28a745;
            font-weight: 600;
        }
        
        .data-table .warning {
            color: #ffa500;
            font-weight: 600;
        }
        
        .data-table .critical {
            color: #ff6b6b;
            font-weight: 600;
        }
        
        .footer {
            background: #f8f9fa;
            padding: 20px;
            text-align: center;
            color: #999;
            border-top: 1px solid #e9ecef;
            font-size: 0.9em;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>⏱️ Consolidación de Tiempos de Pasos</h1>
            <p>Sara3 - Análisis de Performance</p>
        </div>
        
        <div class="content">
            <!-- RESUMEN EJECUTIVO -->
            <div class="section">
                <h2>📊 Resumen Ejecutivo</h2>
                <div class="metrics">
"@
    
    # Agregar métricas resumen
    if ($stepAnalysis.Count -gt 0) {
        $totalSteps = $stepAnalysis.Count
        $avgTimeAll = ($stepAnalysis."Tiempo Promedio (s)" | ForEach-Object { [double]($_ -replace ",", ".") } | Measure-Object -Average).Average
        
        $htmlContent += @"
                    <div class="metric-card">
                        <h3>Total de Pasos Únicos</h3>
                        <div class="value">$totalSteps</div>
                    </div>
                    <div class="metric-card">
                        <h3>Tests Analizados</h3>
                        <div class="value">$totalTestsWithSteps</div>
                    </div>
                    <div class="metric-card">
                        <h3>Tiempo Promedio Total</h3>
                        <div class="value">$(Format-DecimalWithComma -Value $avgTimeAll -Decimals 2) s</div>
                    </div>
"@
    }
    
    # Paso más lento
    if ($stepAnalysis.Count -gt 0) {
        $slowestStep = $stepAnalysis[0]
        $slowestTimeValue = [double]($slowestStep."Tiempo Promedio (s)" -replace ",", ".")
        $cardClass = "critical"
        
        $htmlContent += @"
                    <div class="metric-card $cardClass">
                        <h3>⚠️ Paso Más Lento</h3>
                        <div class="value">$($slowestStep.'Tiempo Promedio (s)') s</div>
                        <div style="font-size: 0.85em; color: #666; margin-top: 10px;">$($slowestStep.Paso)</div>
                    </div>
"@
    }
    
    $htmlContent += @"
                </div>
            </div>
            
            <!-- TABLA DE PASOS -->
            <div class="section">
                <h2>📋 Detalle de Pasos (Ordenado por Tiempo Promedio)</h2>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Paso</th>
                            <th>Ejecutado (veces)</th>
                            <th>Promedio</th>
                            <th>Máximo</th>
                            <th>Mínimo</th>
                            <th>Total</th>
                        </tr>
                    </thead>
                    <tbody>
"@
    
    foreach ($step in $stepAnalysis) {
        $timeValue = [double]($step."Tiempo Promedio (s)" -replace ",", ".")
        $colorClass = "good"
        if ($timeValue -gt 30) { $colorClass = "critical" }
        elseif ($timeValue -gt 10) { $colorClass = "warning" }
        
        $htmlContent += @"
                        <tr>
                            <td><strong>$($step.Paso)</strong></td>
                            <td>$($step.Ejecuciones)</td>
                            <td class="$colorClass">$($step.'Tiempo Promedio (s)') s</td>
                            <td>$($step.'Tiempo Máximo (s)') s</td>
                            <td>$($step.'Tiempo Mínimo (s)') s</td>
                            <td>$($step.'Tiempo Total (s)') s</td>
                        </tr>
"@
    }
    
    $htmlContent += @"
                    </tbody>
                </table>
            </div>
            
            <!-- CONCLUSIONES -->
            <div class="section">
                <h2>🎯 Recomendaciones de Optimización</h2>
                <div class="metrics">
"@
    
    if ($stepAnalysis.Count -gt 0) {
        $slowestStep = $stepAnalysis[0]
        $secondSlowest = if ($stepAnalysis.Count -gt 1) { $stepAnalysis[1] } else { $null }
        
        $htmlContent += @"
                    <div class="metric-card critical">
                        <h3>Prioridad #1</h3>
                        <div style="font-size: 0.9em; line-height: 1.6; color: #333;">
                            <strong>Optimizar:</strong><br>
                            $($slowestStep.Paso)<br>
                            <span style="color: #ff6b6b; font-weight: bold;">$($slowestStep.'Tiempo Promedio (s)') s de promedio</span>
                        </div>
                    </div>
"@
        
        if ($secondSlowest) {
            $htmlContent += @"
                    <div class="metric-card warning">
                        <h3>Prioridad #2</h3>
                        <div style="font-size: 0.9em; line-height: 1.6; color: #333;">
                            <strong>Optimizar:</strong><br>
                            $($secondSlowest.Paso)<br>
                            <span style="color: #ffa500; font-weight: bold;">$($secondSlowest.'Tiempo Promedio (s)') s de promedio</span>
                        </div>
                    </div>
"@
        }
    }
    
    $htmlContent += @"
                </div>
            </div>
        </div>
        
        <div class="footer">
            <p>Reporte generado: $(Get-Date -Format "dd/MM/yyyy HH:mm:ss")</p>
            <p>Sara3 - Sistema de Consolidación de Informes de Performance</p>
        </div>
    </div>
</body>
</html>
"@
    
    $htmlContent | Out-File -FilePath $htmlOutput -Encoding UTF8 -Force
    Write-Host "  [OK] Archivo guardado: $htmlOutput" -ForegroundColor Green
}

# =====================================================
# 6. RESUMEN FINAL
# =====================================================

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "✓ CONSOLIDACIÓN COMPLETADA" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""

if ($stepAnalysis.Count -gt 0) {
    Write-Host "Total de pasos únicos: $($stepAnalysis.Count)" -ForegroundColor Cyan
    Write-Host "Tests analizados: $totalTestsWithSteps" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "⚠️  PASO MÁS LENTO:" -ForegroundColor Red
    Write-Host "    $($stepAnalysis[0].Paso)" -ForegroundColor Red
    Write-Host "    Tiempo promedio: $($stepAnalysis[0].'Tiempo Promedio (s)') s" -ForegroundColor Red
} else {
    Write-Host "⚠️  No se encontraron datos de pasos para analizar" -ForegroundColor Yellow
    Write-Host "Asegúrate de que los tests hayan generado archivos CSV en: $performanceLogsPath" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Reportes generados en: $reportFolder" -ForegroundColor Cyan
Write-Host ""
