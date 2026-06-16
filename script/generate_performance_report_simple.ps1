# ============================================
# Performance Report - Version Simplificada
# ============================================

$appPerfLogsPath = ".\target\app_performance_logs"
$reportPath = ".\target\reports"
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'

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

# Función para parsear CSV respetando comillas
function Parse-CSVLine {
    param([string]$line)
    $result = @()
    $current = ""
    $inQuotes = $false
    
    for ($i = 0; $i -lt $line.Length; $i++) {
        $char = $line[$i]
        
        if ($char -eq '"') {
            $inQuotes = -not $inQuotes
        }
        elseif ($char -eq ',' -and -not $inQuotes) {
            $result += $current.Trim('"').Trim()
            $current = ""
        }
        else {
            $current += $char
        }
    }
    
    $result += $current.Trim('"').Trim()
    return $result
}

Write-Host "====== Generando reporte de performance ======"

# Cargar y consolidar datos de NETWORK endpoints
$allEndpoints = @{}
$allMetrics = @{}
$testCount = 0

if (Test-Path $appPerfLogsPath) {
    $csvFiles = Get-ChildItem "$appPerfLogsPath\*.csv" -ErrorAction SilentlyContinue
    
    foreach ($csvFile in $csvFiles) {
        $testCount++
        Write-Host "Procesando: $($csvFile.Name)"
        
        try {
            $lines = Get-Content -Path $csvFile.FullName -Encoding UTF8
            
            # Parsear header
            $headerParts = Parse-CSVLine $lines[0]
            $idxTipo = [array]::IndexOf($headerParts, "Tipo")
            $idxMetrica = [array]::IndexOf($headerParts, "Métrica")
            $idxEndpoint = [array]::IndexOf($headerParts, "Endpoint/Acción")
            $idxTiempo = [array]::IndexOf($headerParts, "Tiempo_ms")
            
            # Procesar cada línea
            for ($i = 1; $i -lt $lines.Count; $i++) {
                $parts = Parse-CSVLine $lines[$i]
                
                $maxIdx = $idxTipo
                if ($idxMetrica -gt $maxIdx) { $maxIdx = $idxMetrica }
                if ($idxEndpoint -gt $maxIdx) { $maxIdx = $idxEndpoint }
                if ($idxTiempo -gt $maxIdx) { $maxIdx = $idxTiempo }
                
                if ($parts.Count -gt $maxIdx -and $idxTiempo -ge 0) {
                    $tipo = $parts[$idxTipo]
                    $metrica = $parts[$idxMetrica]
                    $endpoint = $parts[$idxEndpoint]
                    $timeStr = $parts[$idxTiempo]
                    
                    if ([int]::TryParse($timeStr, [ref]0)) {
                        $tiempo = [int]$timeStr
                        
                        if ($tipo -eq "NETWORK") {
                            if (-not $allEndpoints[$endpoint]) {
                                $allEndpoints[$endpoint] = @{ times = @(); count = 0 }
                            }
                            $allEndpoints[$endpoint].times += $tiempo
                            $allEndpoints[$endpoint].count++
                        }
                        elseif ($tipo -eq "RENDER" -or $tipo -eq "TOTAL") {
                            $metricName = "$metrica - $endpoint"
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

# ============================================
# PROCESAR ENDPOINTS (NETWORK)
# ============================================

$endpointStats = @()

foreach ($ep in $allEndpoints.Keys) {
    $times = $allEndpoints[$ep].times
    $count = $times.Count
    
    if ($count -gt 0) {
        $avg = $times | Measure-Object -Average | Select-Object -ExpandProperty Average
        $min = $times | Measure-Object -Minimum | Select-Object -ExpandProperty Minimum
        $max = $times | Measure-Object -Maximum | Select-Object -ExpandProperty Maximum
        
        $degradation = if ($min -gt 0) { (($max / $min - 1) * 100) } else { 0 }
        
        $endpointStats += [PSCustomObject]@{
            Endpoint = $ep
            Promedio_ms = [int]$avg
            Promedio_Formateado = Format-WithComma -Value ($avg / 1000) -Decimals 2
            Min_ms = $min
            Max_ms = $max
            Ejecuciones = $count
            Degradacion_pct = [math]::Round($degradation, 2)
        }
    }
}

# Ordenar por promedio
$endpointStats = $endpointStats | Sort-Object Promedio_ms -Descending

# ============================================
# PROCESAR METRICAS (RENDER + TOTAL)
# ============================================

$metricStats = @()

foreach ($metric in $allMetrics.Keys) {
    $times = $allMetrics[$metric].times
    $count = $times.Count
    
    if ($count -gt 0) {
        $avg = $times | Measure-Object -Average | Select-Object -ExpandProperty Average
        $min = $times | Measure-Object -Minimum | Select-Object -ExpandProperty Minimum
        $max = $times | Measure-Object -Maximum | Select-Object -ExpandProperty Maximum
        
        $metricStats += [PSCustomObject]@{
            Metrica = $metric
            Tipo = $allMetrics[$metric].tipo
            Promedio_ms = [int]$avg
            Promedio_Formateado = Format-WithComma -Value ($avg / 1000) -Decimals 2
            Min_ms = $min
            Max_ms = $max
            Ejecuciones = $count
        }
    }
}

# ============================================
# GENERAR CSV DE ENDPOINTS
# ============================================

if ($endpointStats.Count -gt 0) {
    $epPath = "$reportPath\performance_endpoints_$timestamp.csv"
    $lines = New-Object System.Collections.ArrayList
    
    $null = $lines.Add('"Endpoint","Promedio (ms)","Promedio (s)","Min (ms)","Max (ms)","Ejecuciones","Degradacion %"')
    
    foreach ($ep in $endpointStats) {
        $endpointName = $ep.Endpoint -replace '"', '""'
        $line = "`"$endpointName`",$($ep.Promedio_ms),$($ep.Promedio_Formateado),$($ep.Min_ms),$($ep.Max_ms),$($ep.Ejecuciones),$($ep.Degradacion_pct)"
        $null = $lines.Add($line)
    }
    
    $lines | Out-File -FilePath $epPath -Encoding UTF8
    Write-Host "OK: CSV Endpoints: $epPath ($($endpointStats.Count) endpoints)"
}

# ============================================
# GENERAR CSV DE METRICAS
# ============================================

if ($metricStats.Count -gt 0) {
    $metPath = "$reportPath\performance_metrics_$timestamp.csv"
    $lines = New-Object System.Collections.ArrayList
    
    $null = $lines.Add('"Metrica","Tipo","Promedio (ms)","Promedio (s)","Min (ms)","Max (ms)","Ejecuciones"')
    
    foreach ($m in $metricStats) {
        $metricName = $m.Metrica -replace '"', '""'
        $line = "`"$metricName`",$($m.Tipo),$($m.Promedio_ms),$($m.Promedio_Formateado),$($m.Min_ms),$($m.Max_ms),$($m.Ejecuciones)"
        $null = $lines.Add($line)
    }
    
    $lines | Out-File -FilePath $metPath -Encoding UTF8
    Write-Host "OK: CSV Metricas: $metPath ($($metricStats.Count) metricas)"
}

# ============================================
# GENERAR HTML DASHBOARD
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
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
            color: #333;
            padding: 20px;
        }
        .container {
            max-width: 1400px;
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
            margin-bottom: 10px;
        }
        .info {
            color: #6b7280;
            margin-bottom: 20px;
            font-size: 14px;
        }
        h2 {
            color: #2563eb;
            margin-top: 30px;
            margin-bottom: 15px;
            font-size: 18px;
        }
        .summary {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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
            font-size: 28px;
            font-weight: bold;
            margin: 10px 0;
        }
        .stat-box .label {
            font-size: 13px;
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
        .time-fast {
            color: #059669;
            font-weight: 500;
        }
        .time-warning {
            color: #d97706;
            font-weight: 500;
        }
        .time-slow {
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
    </style>
</head>
<body>
    <div class="container">
        <h1>Performance Consolidado - SARA3</h1>
        <div class="info">
            Fecha: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss') | Tests: $testCount | Endpoints: $($endpointStats.Count) | Metricas: $($metricStats.Count)
        </div>

        <div class="summary">
            <div class="stat-box" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <div class="label">Tests Procesados</div>
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

        <h2>Endpoints - Resumen</h2>
        <table>
            <thead>
                <tr>
                    <th>Endpoint</th>
                    <th>Promedio (ms)</th>
                    <th>Promedio (s)</th>
                    <th>Min (ms)</th>
                    <th>Max (ms)</th>
                    <th>Ejecuciones</th>
                    <th>Degradacion %</th>
                </tr>
            </thead>
            <tbody>
"@

        foreach ($ep in $endpointStats | Select-Object -First 20) {
            $timeClass = if ($ep.Promedio_ms -lt 500) { "time-fast" } elseif ($ep.Promedio_ms -lt 1500) { "time-warning" } else { "time-slow" }
            $epName = $ep.Endpoint
            if ($epName.Length -gt 60) { $epName = $epName.Substring(0, 57) + "..." }
            
            $htmlContent += @"
                <tr>
                    <td>$epName</td>
                    <td>$($ep.Promedio_ms)</td>
                    <td class="$timeClass">$($ep.Promedio_Formateado) s</td>
                    <td>$($ep.Min_ms)</td>
                    <td>$($ep.Max_ms)</td>
                    <td>$($ep.Ejecuciones)</td>
                    <td>$($ep.Degradacion_pct)%</td>
                </tr>
"@
        }

        $htmlContent += @"
            </tbody>
        </table>

        <h2>Metricas de Render y Total</h2>
        <table>
            <thead>
                <tr>
                    <th>Metrica</th>
                    <th>Tipo</th>
                    <th>Promedio (ms)</th>
                    <th>Promedio (s)</th>
                    <th>Min (ms)</th>
                    <th>Max (ms)</th>
                </tr>
            </thead>
            <tbody>
"@

        foreach ($m in $metricStats | Sort-Object Promedio_ms -Descending | Select-Object -First 15) {
            $timeClass = if ($m.Promedio_ms -lt 1000) { "time-fast" } elseif ($m.Promedio_ms -lt 5000) { "time-warning" } else { "time-slow" }
            
            $htmlContent += @"
                <tr>
                    <td>$($m.Metrica)</td>
                    <td>$($m.Tipo)</td>
                    <td>$($m.Promedio_ms)</td>
                    <td class="$timeClass">$($m.Promedio_Formateado) s</td>
                    <td>$($m.Min_ms)</td>
                    <td>$($m.Max_ms)</td>
                </tr>
"@
        }

        $htmlContent += @"
            </tbody>
        </table>

        <div class="footer">
            <p>SARA3 Automation Test Suite - Performance Analysis</p>
            <p>Reporte generado: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')</p>
        </div>
    </div>
</body>
</html>
"@

$htmlContent | Out-File -FilePath $htmlPath -Encoding UTF8
Write-Host "OK: HTML dashboard: $htmlPath"

Write-Host ""
Write-Host "====== REPORTE COMPLETADO ======"
Write-Host "Archivos generados en: $reportPath"
Write-Host ""
