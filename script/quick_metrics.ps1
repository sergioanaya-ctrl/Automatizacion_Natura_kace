# METRICAS RAPIDAS - Lee directamente del XLSX sin regenerar
# Tiempo esperado: 5-10 segundos (vs 2+ minutos del consolidate)

param(
    [string]$excelPath = ".\reports_consolidation\consolidated_report_20260513_112224.xlsx"
)

Write-Host ""
Write-Host "METRICAS RAPIDAS DEL CONSOLIDADO" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $excelPath)) {
    Write-Host "ERROR: $excelPath no existe" -ForegroundColor Red
    exit 1
}

# Permitir import de modulos
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force -ErrorAction SilentlyContinue

# Cargar ImportExcel
try {
    Import-Module ImportExcel -WarningAction SilentlyContinue -ErrorAction Stop
} catch {
    Write-Host "ERROR: No se puede cargar ImportExcel" -ForegroundColor Red
    exit 1
}

Write-Host "Leyendo datos del Excel..." -ForegroundColor Green
$inicio = Get-Date
Write-Host ""

# 1. ESTADISTICAS POR TEST
Write-Host "1. ESTADISTICAS POR TEST" -ForegroundColor Magenta
Write-Host "---" -ForegroundColor Magenta
try {
    $stats = Import-Excel -Path $excelPath -WorksheetName "Estadisticas por Test" -ErrorAction SilentlyContinue
    if ($stats) {
        Write-Host "Total Tests: $($stats.Count)" -ForegroundColor White
        
        $fallidos = $stats | Where-Object { $_."Estado" -eq "FAILED" }
        $exitosos = $stats | Where-Object { $_."Estado" -eq "PASSED" }
        
        Write-Host "  Exitosos: $($exitosos.Count)" -ForegroundColor Green
        Write-Host "  Fallidos: $($fallidos.Count)" -ForegroundColor Red
        
        if ($stats.Count -gt 0) {
            $tasa = [math]::Round(($exitosos.Count / $stats.Count) * 100, 2)
            Write-Host "  Tasa Exito: $tasa%" -ForegroundColor Cyan
        }
        
        Write-Host ""
        Write-Host "Top 5 Tests Fallidos:" -ForegroundColor Yellow
        $fallidos | Sort-Object "Pasos Lentos" -Descending | Select-Object -First 5 | ForEach-Object {
            $nombre = if ($_.Test.Length -gt 50) { $_.Test.Substring(0, 50) + "..." } else { $_.Test }
            Write-Host "  - $nombre" -ForegroundColor Red
            Write-Host "    Maquina: $($_.Maquina), Pasos Lentos: $($_.'Pasos Lentos')/$($_.'Total Pasos')" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2. POR MAQUINA
Write-Host "2. RENDIMIENTO POR MAQUINA" -ForegroundColor Magenta
Write-Host "---" -ForegroundColor Magenta
try {
    $maquinas = Import-Excel -Path $excelPath -WorksheetName "Por Maquina" -ErrorAction SilentlyContinue
    if ($maquinas) {
        Write-Host "Total Maquinas: $($maquinas.Count)" -ForegroundColor White
        Write-Host ""
        
        $maquinas = $maquinas | Sort-Object "Tasa Exito %" -Descending
        
        Write-Host "Ranking de Maquinas:" -ForegroundColor White
        $rank = 1
        foreach ($maq in $maquinas | Select-Object -First 10) {
            $tasa = $maq."Tasa Exito %"
            $estado = if ($tasa -ge 75) { "[OK]" } elseif ($tasa -ge 50) { "[?]" } else { "[X]" }
            $color = if ($tasa -ge 75) { "Green" } elseif ($tasa -ge 50) { "Yellow" } else { "Red" }
            Write-Host "  $rank. $($maq.Maquina)" -ForegroundColor White -NoNewline
            Write-Host " - Tests: $($maq.'Total Tests'), Exitosos: $($maq.'Tests Exitosos'), Tasa: $tasa% $estado" -ForegroundColor $color
            $rank++
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 3. TODOS LOS PASOS - Estadisticas generales
Write-Host "3. ANALISIS DE PASOS (Todos los Pasos)" -ForegroundColor Magenta
Write-Host "---" -ForegroundColor Magenta
try {
    $pasos = Import-Excel -Path $excelPath -WorksheetName "Todos los Pasos" -ErrorAction SilentlyContinue
    if ($pasos) {
        Write-Host "Total Pasos: $($pasos.Count)" -ForegroundColor White
        
        # Tiempos
        $tiempos = @($pasos | ForEach-Object { [int]$_."Tiempo (ms)" } | Where-Object { $_ -gt 0 })
        if ($tiempos.Count -gt 0) {
            $promedio = [math]::Round(($tiempos | Measure-Object -Average).Average, 2)
            $max = ($tiempos | Measure-Object -Maximum).Maximum
            $min = ($tiempos | Measure-Object -Minimum).Minimum
            
            Write-Host "Tiempos: Promedio=$promedio ms, Max=$max ms, Min=$min ms" -ForegroundColor Cyan
        }
        
        # Por tipo de error
        $errores = $pasos | Group-Object "Error Type" | Select-Object Name, Count
        Write-Host ""
        Write-Host "Por Tipo de Error:" -ForegroundColor White
        foreach ($error in $errores | Sort-Object Count -Descending) {
            $pct = [math]::Round(($error.Count / $pasos.Count) * 100, 2)
            $color = if ($error.Name -eq "ERROR") { "Red" } elseif ($error.Name -eq "SKIPPED") { "Yellow" } else { "Green" }
            Write-Host "  $($error.Name): $($error.Count) ($pct%)" -ForegroundColor $color
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 4. PASOS LENTOS
Write-Host "4. PASOS MAS LENTOS (Bottlenecks)" -ForegroundColor Magenta
Write-Host "---" -ForegroundColor Magenta
try {
    $lentos = Import-Excel -Path $excelPath -WorksheetName "Pasos Lentos" -ErrorAction SilentlyContinue
    if ($lentos) {
        Write-Host "Total Pasos Lentos (mayor a 5s): $($lentos.Count)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Top 10 Bottlenecks:" -ForegroundColor White
        $lentos | Select-Object -First 10 | ForEach-Object {
            $desc = if ($_.Descripcion.Length -gt 45) { $_.Descripcion.Substring(0, 45) + "..." } else { $_.Descripcion }
            Write-Host "  - $desc" -ForegroundColor Yellow
            Write-Host "    Tiempo: $($_.'Tiempo (ms)')ms" -ForegroundColor White
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 5. TESTS FALLIDOS
Write-Host "5. RESUMEN DE TESTS FALLIDOS" -ForegroundColor Magenta
Write-Host "---" -ForegroundColor Magenta
try {
    $fallidos = Import-Excel -Path $excelPath -WorksheetName "Tests Fallidos" -ErrorAction SilentlyContinue
    if ($fallidos) {
        Write-Host "Total Tests Fallidos: $($fallidos.Count)" -ForegroundColor Red
        
        if ($fallidos.Count -gt 0) {
            # Tipos de error mas comunes
            $tiposError = $fallidos | Group-Object "Tipo Error" | Sort-Object Count -Descending
            Write-Host ""
            Write-Host "Tipos de Error mas comunes:" -ForegroundColor Yellow
            $tiposError | Select-Object -First 5 | ForEach-Object {
                $pct = [math]::Round(($_.Count / $fallidos.Count) * 100, 2)
                Write-Host "  - $($_.Name): $($_.Count) ($pct%)" -ForegroundColor Red
            }
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 6. INDICADOR DE SALUD
Write-Host "6. INDICADOR DE SALUD" -ForegroundColor Magenta
Write-Host "---" -ForegroundColor Magenta
try {
    $stats = Import-Excel -Path $excelPath -WorksheetName "Estadisticas por Test" -ErrorAction SilentlyContinue
    if ($stats) {
        $exitosos = ($stats | Where-Object { $_."Estado" -eq "PASSED" }).Count
        $total = $stats.Count
        
        if ($total -gt 0) {
            $tasa = [math]::Round(($exitosos / $total) * 100, 2)
            
            $estado = if ($tasa -ge 90) {
                "EXCELENTE"
            } elseif ($tasa -ge 75) {
                "BUENO"
            } elseif ($tasa -ge 50) {
                "REGULAR"
            } else {
                "CRITICO"
            }
            
            $color = if ($tasa -ge 90) { "Green" } elseif ($tasa -ge 75) { "Yellow" } elseif ($tasa -ge 50) { "Yellow" } else { "Red" }
            
            Write-Host "Estado: $estado" -ForegroundColor $color
            Write-Host "Confiabilidad: $tasa%" -ForegroundColor Cyan
            
            if ($tasa -lt 50) {
                Write-Host ""
                Write-Host "ALERTAS CRITICAS:" -ForegroundColor Red
                Write-Host "  - Tasa fallo mayor a 50 porciento" -ForegroundColor Red
                Write-Host "  - Revisar maquinas con tasa menor a 50 porciento" -ForegroundColor Red
                Write-Host "  - Investigar pasos lentos (mayor a 5s)" -ForegroundColor Red
            }
        }
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
$fin = Get-Date
$tiempo = ($fin - $inicio).TotalSeconds
Write-Host "Metricas generadas en $($tiempo)s" -ForegroundColor Green
Write-Host "Abre el XLSX para ver datos completos: $excelPath" -ForegroundColor Cyan
Write-Host ""
