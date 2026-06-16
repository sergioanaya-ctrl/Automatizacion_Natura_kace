# Performance Report - Simple Text Based
$appPerfLogsPath = ".\target\app_performance_logs"
$reportPath = ".\target\reports"  
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'

if (-not (Test-Path $reportPath)) { New-Item -ItemType Directory -Path $reportPath | Out-Null }

function Format-WithComma { param([double]$Value, [int]$Decimals = 2)
    $rounded = [math]::Round($Value, $Decimals)
    return $rounded.ToString("N$Decimals", [System.Globalization.CultureInfo]::GetCultureInfo("es-CO"))
}

Write-Host "====== Generando reporte de performance ======"
$allEndpoints = @{}; $allMetrics = @{}; $testCount = 0

if (Test-Path $appPerfLogsPath) {
    Get-ChildItem "$appPerfLogsPath\*.csv" -ErrorAction SilentlyContinue | ForEach-Object {
        $testCount++; Write-Host "Procesando: $($_.Name)"
        $lines = @(Get-Content -Path $_.FullName -Encoding UTF8 -ErrorAction SilentlyContinue | Where-Object { $_ })
        
        if ($lines.Count -gt 1) {
            for ($i = 1; $i -lt $lines.Count; $i++) {
                $parts = $lines[$i] -split "," | ForEach-Object { $_.Trim().Trim('"') }
                
                if ($parts.Count -ge 5) {
                    $tipo = $parts[0]
                    $metrica = $parts[1]
                    $endpoint = $parts[2]
                    $tiempoMs = $parts[3]
                    
                    if ([int]::TryParse($tiempoMs, [ref]0)) {
                        $tiempo = [int]$tiempoMs
                        if ($tipo -eq "NETWORK" -and $endpoint -notlike "*1778510*") {
                            if (-not $allEndpoints[$endpoint]) { $allEndpoints[$endpoint] = @() }
                            $allEndpoints[$endpoint] += $tiempo
                        }
                        elseif (($tipo -eq "RENDER" -or $tipo -eq "TOTAL") -and $endpoint -notlike "*1778510*") {
                            $metricName = "$metrica - $endpoint"
                            if (-not $allMetrics[$metricName]) { $allMetrics[$metricName] = @{ times = @(); tipo = $tipo } }
                            $allMetrics[$metricName].times += $tiempo
                        }
                    }
                }
            }
        }
    }
}

Write-Host "  Endpoints: $($allEndpoints.Count) | Metricas: $($allMetrics.Count)"

# Estadísticas de Endpoints
$epStats = @(); foreach ($ep in $allEndpoints.Keys) {
    $times = $allEndpoints[$ep]
    $avg = ($times | Measure-Object -Average).Average
    $min = ($times | Measure-Object -Minimum).Minimum
    $max = ($times | Measure-Object -Maximum).Maximum
    $deg = if ($min -gt 0) { [math]::Round((($max / $min - 1) * 100), 2) } else { 0 }
    $epStats += [PSCustomObject]@{EP=$ep; AvgMs=[int]$avg; AvgS=Format-WithComma ($avg/1000); Min=$min; Max=$max; Count=$times.Count; Deg=$deg}
}

# CSV Endpoints
if ($epStats.Count -gt 0) {
    $path = "$reportPath\performance_endpoints_$timestamp.csv"
    $lines = @('"Endpoint","Promedio (ms)","Promedio (s)","Min (ms)","Max (ms)","Ejecuciones","Degradacion %"')
    $epStats | Sort-Object AvgMs -Descending | ForEach-Object {
        $ep = $_.EP -replace '"', '""'
        $lines += "`"$ep`",$($_.AvgMs),$($_.AvgS),$($_.Min),$($_.Max),$($_.Count),$($_.Deg)"
    }
    $lines | Out-File -FilePath $path -Encoding UTF8
    Write-Host "OK: CSV Endpoints"
}

# Estadísticas de Métricas
$mStats = @(); foreach ($m in $allMetrics.Keys) {
    $times = $allMetrics[$m].times
    $avg = ($times | Measure-Object -Average).Average
    $min = ($times | Measure-Object -Minimum).Minimum
    $max = ($times | Measure-Object -Maximum).Maximum
    $mStats += [PSCustomObject]@{M=$m; Tipo=$allMetrics[$m].tipo; AvgMs=[int]$avg; AvgS=Format-WithComma ($avg/1000); Min=$min; Max=$max; Count=$times.Count}
}

# CSV Métricas
if ($mStats.Count -gt 0) {
    $path = "$reportPath\performance_metrics_$timestamp.csv"
    $lines = @('"Metrica","Tipo","Promedio (ms)","Promedio (s)","Min (ms)","Max (ms)","Ejecuciones"')
    $mStats | Sort-Object AvgMs -Descending | ForEach-Object {
        $m = $_.M -replace '"', '""'
        $lines += "`"$m`",$($_.Tipo),$($_.AvgMs),$($_.AvgS),$($_.Min),$($_.Max),$($_.Count)"
    }
    $lines | Out-File -FilePath $path -Encoding UTF8
    Write-Host "OK: CSV Metricas"
}

# HTML
$htmlPath = "$reportPath\performance_report_$timestamp.html"
$html = @"
<!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"><title>Performance - SARA3</title><style>
body{font-family:'Segoe UI',sans-serif;background:#f5f5f5;padding:20px;margin:0}
.container{max-width:1400px;margin:0 auto;background:white;border-radius:8px;padding:30px}
h1{color:#1e3a8a;border-bottom:3px solid #1e3a8a;padding-bottom:10px}
.info{color:#6b7280;margin-bottom:20px;font-size:14px}
.summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:15px;margin-bottom:30px}
.stat-box{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:20px;border-radius:8px;text-align:center}
.stat-box .value{font-size:28px;font-weight:bold}
table{width:100%;border-collapse:collapse;margin:15px 0;font-size:13px}
th{background:#f3f4f6;padding:12px;text-align:left;font-weight:600;border-bottom:2px solid #d1d5db}
td{padding:10px 12px;border-bottom:1px solid #e5e7eb}
tr:hover{background:#f9fafb}
.time-ok{color:#059669;font-weight:500}.time-warn{color:#d97706;font-weight:500}.time-slow{color:#dc2626;font-weight:500}
</style></head><body><div class="container">
<h1>Performance Consolidado - SARA3</h1>
<div class="info">Fecha: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss') | Tests: $testCount | Endpoints: $($epStats.Count) | Metricas: $($mStats.Count)</div>
<div class="summary">
<div class="stat-box"><div style="font-size:13px">Tests</div><div style="font-size:28px;font-weight:bold">$testCount</div></div>
<div class="stat-box" style="background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%)"><div style="font-size:13px">Endpoints</div><div style="font-size:28px;font-weight:bold">$($epStats.Count)</div></div>
<div class="stat-box" style="background:linear-gradient(135deg,#4facfe 0%,#00f2fe 100%)"><div style="font-size:13px">Metricas</div><div style="font-size:28px;font-weight:bold">$($mStats.Count)</div></div>
</div>
<h2>Endpoints</h2><table><thead><tr><th>Endpoint</th><th>Promedio (s)</th><th>Min</th><th>Max</th><th>Ejecuciones</th></tr></thead><tbody>
"@

$epStats | Sort-Object AvgMs -Descending | Select-Object -First 15 | ForEach-Object {
    $class = if ($_.AvgMs -lt 500) { "time-ok" } elseif ($_.AvgMs -lt 1500) { "time-warn" } else { "time-slow" }
    $html += "<tr><td>$($_.EP.Substring(0,[Math]::Min(60,$_.EP.Length)))</td><td class='$class'>$($_.AvgS)</td><td>$($_.Min)</td><td>$($_.Max)</td><td>$($_.Count)</td></tr>"
}

$html += "</tbody></table><h2>Metricas</h2><table><thead><tr><th>Metrica</th><th>Tipo</th><th>Promedio (s)</th></tr></thead><tbody>"

$mStats | Sort-Object AvgMs -Descending | Select-Object -First 10 | ForEach-Object {
    $html += "<tr><td>$($_.M.Substring(0,[Math]::Min(60,$_.M.Length)))</td><td>$($_.Tipo)</td><td>$($_.AvgS)</td></tr>"
}

$html += "</tbody></table></div></body></html>"
$html | Out-File -FilePath $htmlPath -Encoding UTF8
Write-Host "OK: HTML dashboard"
Write-Host "====== REPORTE COMPLETADO ======"
