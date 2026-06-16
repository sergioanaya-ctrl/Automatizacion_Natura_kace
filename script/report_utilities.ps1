# ========================================================
# FUNCIONES AUXILIARES PARA REPORTING
# ========================================================
# Contiene funciones reutilizables para todos los reportes

###############################################################
# Función: Convertir número a formato con coma
###############################################################
function Format-DecimalWithComma {
    param([double]$Value, [int]$Decimals = 2)
    
    $rounded = [math]::Round($Value, $Decimals)
    $formatted = $rounded.ToString("N$Decimals", [System.Globalization.CultureInfo]::GetCultureInfo("es-CO"))
    return $formatted
}

###############################################################
# Función: Convertir milisegundos a formato legible (s o min)
###############################################################
function Format-TimeDisplay {
    param([double]$Milliseconds)
    
    $seconds = $Milliseconds / 1000
    
    if ($seconds -ge 60) {
        # Mostrar en minutos
        $minutes = $seconds / 60
        return "$(Format-DecimalWithComma -Value $minutes -Decimals 2) min"
    } else {
        # Mostrar en segundos
        return "$(Format-DecimalWithComma -Value $seconds -Decimals 2) s"
    }
}

###############################################################
# Función: Convertir milisegundos a segundos con coma
###############################################################
function Format-SecondsWithComma {
    param([double]$Milliseconds)
    $seconds = $Milliseconds / 1000
    return Format-DecimalWithComma -Value $seconds -Decimals 2
}

###############################################################
# Función: Convertir milisegundos a minutos con coma
###############################################################
function Format-MinutesWithComma {
    param([double]$Milliseconds)
    $minutes = $Milliseconds / 1000 / 60
    return Format-DecimalWithComma -Value $minutes -Decimals 2
}

###############################################################
# Función: Cargar datos de performance logs (pasos)
###############################################################
function Load-PerformanceStepData {
    param([string]$PerformanceLogsPath)
    
    $stepData = @{}
    
    if (Test-Path $PerformanceLogsPath) {
        Get-ChildItem "$PerformanceLogsPath/*.csv" -ErrorAction SilentlyContinue | ForEach-Object {
            $testFileName = $_.BaseName
            $stepData[$testFileName] = @()
            
            try {
                $csvData = Import-Csv -Path $_.FullName -Encoding UTF8 -ErrorAction SilentlyContinue
                
                foreach ($row in $csvData) {
                    $propertyNames = $row | Get-Member -MemberType NoteProperty | Select-Object -ExpandProperty Name
                    $metricColumn = $propertyNames | Where-Object { $_ -like "*Metrica*" -or $_ -like "*etrica*" } | Select-Object -First 1
                    
                    if ($metricColumn) {
                        $metricValue = $row.$metricColumn
                        if ($metricValue -like "Step*" -or $metricValue -like "*Duration*") {
                            $stepData[$testFileName] += @{
                                Paso = $metricValue
                                Tiempo_ms = [int]($row.Valor -replace "[^0-9]", "")
                                Unidad = $row.Unidad
                            }
                        }
                    }
                }
            } catch {
                # Ignorar errores de lectura
            }
        }
    }
    
    return $stepData
}

###############################################################
# Función: Analizar y consolidar datos de pasos
###############################################################
function Analyze-StepPerformance {
    param([hashtable]$StepData)
    
    $stepStats = @{}
    $totalTestsWithSteps = 0
    
    foreach ($testFile in $StepData.Keys) {
        $steps = $StepData[$testFile]
        
        if ($steps.Count -gt 0) {
            $totalTestsWithSteps++
            
            foreach ($step in $steps) {
                $stepName = $step.Paso
                
                if (-not $stepStats.ContainsKey($stepName)) {
                    $stepStats[$stepName] = @{
                        Times = @()
                        Count = 0
                        Total = 0
                    }
                }
                
                $stepStats[$stepName].Times += $step.Tiempo_ms
                $stepStats[$stepName].Count += 1
                $stepStats[$stepName].Total += $step.Tiempo_ms
            }
        }
    }
    
    # Calcular estadísticas
    $stepAnalysis = @()
    foreach ($stepName in $stepStats.Keys) {
        $stats = $stepStats[$stepName]
        $avg = $stats.Total / $stats.Count
        $max = ($stats.Times | Measure-Object -Maximum).Maximum
        $min = ($stats.Times | Measure-Object -Minimum).Minimum
        
        $stepAnalysis += [PSCustomObject]@{
            "Paso" = $stepName
            "Ejecuciones" = $stats.Count
            "Tiempo Promedio (s)" = Format-SecondsWithComma -Milliseconds $avg
            "Tiempo Máximo (s)" = Format-SecondsWithComma -Milliseconds $max
            "Tiempo Mínimo (s)" = Format-SecondsWithComma -Milliseconds $min
            "Tiempo Total (s)" = Format-SecondsWithComma -Milliseconds $stats.Total
        }
    }
    
    # Ordenar por tiempo promedio descendente
    $stepAnalysis = $stepAnalysis | Sort-Object -Property "Tiempo Promedio (s)" -Descending
    
    return @{
        Analysis = $stepAnalysis
        TotalTestsWithSteps = $totalTestsWithSteps
        StepStats = $stepStats
    }
}

###############################################################
# Función: Crear HTML table para pasos
###############################################################
function Create-StepTable {
    param([array]$StepAnalysis, [string]$Title = "Análisis de Pasos")
    
    $html = @"
        <h3>$Title</h3>
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
    
    foreach ($step in $StepAnalysis) {
        # Determinar color según tiempo promedio
        $timeValue = $step."Tiempo Promedio (s)".Replace(",", ".")
        $color = "good"
        if ([double]$timeValue -gt 30) { $color = "critical" }
        elseif ([double]$timeValue -gt 10) { $color = "warning" }
        
        $html += @"
                <tr>
                    <td>$($step.Paso)</td>
                    <td>$($step.Ejecuciones)</td>
                    <td class="$color">$($step."Tiempo Promedio (s)") s</td>
                    <td>$($step."Tiempo Máximo (s)") s</td>
                    <td>$($step."Tiempo Mínimo (s)") s</td>
                    <td>$($step."Tiempo Total (s)") s</td>
                </tr>
"@
    }
    
    $html += @"
            </tbody>
        </table>
"@
    
    return $html
}

# Este archivo se usa como script de utilidades (dot-source) desde los reportes.
# No se necesita Export-ModuleMember aquí, y causa error cuando no se ejecuta como módulo.
