# ============================================
# Performance Report - Versión Robusta Final
# ============================================

$appPerfLogsPath = ".\target\app_performance_logs"
$reportPath = ".\target\reports"
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'

if (-not (Test-Path $reportPath)) {
    New-Item -ItemType Directory -Path $reportPath | Out-Null
}

function Format-WithComma {
    param([double]$Value, [int]$Decimals = 2)
    $rounded = [math]::Round($Value, $Decimals)
    return $rounded.ToString("N$Decimals", [System.Globalization.CultureInfo]::GetCultureInfo("es-CO"))
}

Write-Host "====== Generando reporte de performance ======"

$allEndpoints = @{}
$allMetrics = @{}
$testCount = 0

if (Test-Path $appPerfLogsPath) {
    $csvFiles = @(Get-ChildItem "$appPerfLogsPath\*.csv" -ErrorAction SilentlyContinue)
    
    foreach ($csvFile in $csvFiles) {
        $testCount++
        Write-Host "Procesando: $($csvFile.Name)"
        
        try {
            # Usar ConvertFrom-Csv para parsing robusto
            $rawContent = Get-Content -Path $csvFile.FullName -Raw -Encoding UTF8
            $lines = $rawContent -split "`n"
            
            if ($lines.Count -gt 1) {
                $csvData = $lines | ConvertFrom-Csv -ErrorAction SilentlyContinue
                
                foreach ($row in $csvData) {
                    $tipo = $row.Tipo
                    $endpoint = $row."Endpoint/Acción"
                    $metrica = $row.PSObject.Properties | Where-Object { $_.Name -like "*trica" } | Select-Object -ExpandProperty Value
                    $timeStr = $row.Tiempo_ms
                    
                    if (-not [string]::IsNullOrWhiteSpace($tipo) -and -not [string]::IsNullOrWhiteSpace($endpoint) -and [int]::TryParse($timeStr, [ref]0)) {
                        $tiempo = [int]$timeStr
                        
                        if ($tipo -eq "NETWORK") {
                            if (-not $allEndpoints[$endpoint]) {
                                $allEndpoints[$endpoint] = @()
                            }
                            $allEndpoints[$endpoint] += $tiempo
                        }
                        elseif ($tipo -eq "RENDER" -or $tipo -eq "TOTAL") {
                            $metricName = if ($metrica) { "$metrica - $endpoint" } else { "$endpoint" }
                            if (-not $allMetrics[$metricName]) {
                                $allMetrics[$metricName] = @{ times = @(); tipo = $tipo }
                            }
                            $allMetrics[$metricName].times += $tiempo
                        }
                    }
                }
            }
        }
        catch {
            Write-Host "ERROR leyendo $($csvFile.Name): $_"
        }
    }
}

Write-Host "  OK: $($allEndpoints.Count) endpoints, $($allMetrics.Count) metricas"

# ============================================
# GENERAR CSV DE ENDPOINTS
# ============================================

$endpointStats = @()
foreach ($epName in $allEndpoints.Keys) {
    $times = $allEndpoints[$epName]
    $count = $times.Count
    
    if ($count -gt 0) {
        $avg = ($times | Measure-Object -Average).Average
        $min = ($times | Measure-Object -Minimum).Minimum
        $max = ($times | Measure-Object -Maximum).Maximum
        $degradation = if ($min -gt 0) { (($max / $min - 1) * 100) } else { 0 }
        
        $endpointStats += [PSCustomObject]@{
            Endpoint = $epName
            Promedio_ms = [int]$avg
            Promedio_s = Format-WithComma -Value ($avg / 1000)
            Min_ms = $min
            Max_ms = $max
            Ejecuciones = $count
            Degradacion = [math]::Round($degradation, 2)
        }
    }
}

if ($endpointStats.Count -gt 0) {
    $epPath = "$reportPath\performance_endpoints_$timestamp.csv"
    $epLines = @('"Endpoint","Promedio (ms)","Promedio (s)","Min (ms)","Max (ms)","Ejecuciones","Degradacion %"')
    
    foreach ($ep in ($endpointStats | Sort-Object Promedio_ms -Descending)) {
        $epName = $ep.Endpoint -replace '"', '""'
        $epLines += "`"$epName`",$($ep.Promedio_ms),$($ep.Promedio_s),$($ep.Min_ms),$($ep.Max_ms),$($ep.Ejecuciones),$($ep.Degradacion)"
    }
    
    $epLines | Out-File -FilePath $epPath -Encoding UTF8
    Write-Host "OK: CSV Endpoints ($($epLines.Count - 1) registros)"
}

# ============================================
# GENERAR CSV DE METRICAS
# ============================================

$metricStats = @()
foreach ($metName in $allMetrics.Keys) {
    $times = $allMetrics[$metName].times
    $count = $times.Count
    
    if ($count -gt 0) {
        $avg = ($times | Measure-Object -Average).Average
        $min = ($times | Measure-Object -Minimum).Minimum
        $max = ($times | Measure-Object -Maximum).Maximum
        
        $metricStats += [PSCustomObject]@{
            Metrica = $metName
            Tipo = $allMetrics[$metName].tipo
            Promedio_ms = [int]$avg
            Promedio_s = Format-WithComma -Value ($avg / 1000)
            Min_ms = $min
            Max_ms = $max
            Ejecuciones = $count
        }
    }
}

if ($metricStats.Count -gt 0) {
    $metPath = "$reportPath\performance_metrics_$timestamp.csv"
    $metLines = @('"Metrica","Tipo","Promedio (ms)","Promedio (s)","Min (ms)","Max (ms)","Ejecuciones"')
    
    foreach ($m in ($metricStats | Sort-Object Promedio_ms -Descending)) {
        $mName = $m.Metrica -replace '"', '""'
        $metLines += "`"$mName`",$($m.Tipo),$($m.Promedio_ms),$($m.Promedio_s),$($m.Min_ms),$($m.Max_ms),$($m.Ejecuciones)"
    }
    
    $metLines | Out-File -FilePath $metPath -Encoding UTF8
    Write-Host "OK: CSV Metricas ($($metLines.Count - 1) registros)"
}

# ============================================
# GENERAR HTML
# ============================================

$htmlPath = "$reportPath\performance_report_$timestamp.html"
$htmlContent = @"
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Performance Report - SARA3</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f5f5f5; padding: 20px; margin: 0; }
        .container { max-width: 1400px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 30px; }
        h1 { color: #1e3a8a; border-bottom: 3px solid #1e3a8a; padding-bottom: 10px; margin-bottom: 5px; }
        .info { color: #6b7280; margin-bottom: 20px; font-size: 14px; }
        h2 { color: #2563eb; margin-top: 30px; margin-bottom: 15px; font-size: 18px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 15px; margin-bottom: 30px; }
        .stat-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
        .stat-box .value { font-size: 28px; font-weight: bold; margin: 10px 0; }
        .stat-box .label { font-size: 13px; opacity: 0.9; }
        table { width: 100%; border-collapse: collapse; margin: 15px 0; font-size: 13px; }
        thead { background: #f3f4f6; border-bottom: 2px solid #d1d5db; }
        th { padding: 12px; text-align: left; font-weight: 600; color: #1f2937; }
        td { padding: 10px 12px; border-bottom: 1px solid #e5e7eb; }
        tr:hover { background: #f9fafb; }
        .time-fast { color: #059669; font-weight: 500; }
        .time-warn { color: #d97706; font-weight: 500; }
        .time-slow { color: #dc2626; font-weight: 500; }
        .footer { margin-top: 30px; padding-top: 15px; border-top: 1px solid #e5e7eb; text-align: center; color: #6b7280; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Performance Consolidado - SARA3</h1>
        <div class="info">Fecha: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss') | Tests: $testCount | Endpoints: $($endpointStats.Count) | Metricas: $($metricStats.Count)</div>
        
        <div class="summary">
            <div class="stat-box" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <div class="label">Tests</div>
                <div class="value">$testCount</div>
            </div>
            <div class="stat-box" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <div class="label">Endpoints</div>
                <div class="value">$($endpointStats.Count)</div>
            </div>
            <div class="stat-box" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <div class="label">Metricas</div>
                <div class="value">$($metricStats.Count)</div>
            </div>
        </div>

        <h2>Endpoints - Top Performance</h2>
        <table>
            <thead><tr><th>Endpoint</th><th>Promedio (s)</th><th>Min (ms)</th><th>Max (ms)</th><th>Ejecuciones</th><th>Degradacion %</th></tr></thead>
            <tbody>
"@

        foreach ($ep in ($endpointStats | Sort-Object Promedio_ms -Descending | Select-Object -First 15)) {
            $timeClass = if ($ep.Promedio_ms -lt 500) { "time-fast" } elseif ($ep.Promedio_ms -lt 1500) { "time-warn" } else { "time-slow" }
            $epName = if ($ep.Endpoint.Length -gt 60) { $ep.Endpoint.Substring(0, 57) + "..." } else { $ep.Endpoint }
            $htmlContent += "<tr><td>$epName</td><td class='$timeClass'>$($ep.Promedio_s) s</td><td>$($ep.Min_ms)</td><td>$($ep.Max_ms)</td><td>$($ep.Ejecuciones)</td><td>$($ep.Degradacion)%</td></tr>"
        }

        $htmlContent += "</tbody></table><h2>Metricas de Render y Total</h2><table><thead><tr><th>Metrica</th><th>Tipo</th><th>Promedio (s)</th><th>Min (ms)</th><th>Max (ms)</th></tr></thead><tbody>"

        foreach ($m in ($metricStats | Sort-Object Promedio_ms -Descending | Select-Object -First 10)) {
            $timeClass = if ($m.Promedio_ms -lt 1000) { "time-fast" } elseif ($m.Promedio_ms -lt 5000) { "time-warn" } else { "time-slow" }
            $htmlContent += "<tr><td>$($m.Metrica)</td><td>$($m.Tipo)</td><td class='$timeClass'>$($m.Promedio_s) s</td><td>$($m.Min_ms)</td><td>$($m.Max_ms)</td></tr>"
        }

        $htmlContent += "</tbody></table><div class='footer'><p>SARA3 - Performance Analysis</p><p>Generado: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')</p></div></div></body></html>"

$htmlContent | Out-File -FilePath $htmlPath -Encoding UTF8
Write-Host "OK: HTML dashboard generado"

Write-Host ""
Write-Host "====== REPORTE COMPLETADO ======"
Write-Host ""
