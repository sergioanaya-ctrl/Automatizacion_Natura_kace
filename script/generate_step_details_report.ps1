# SCRIPT PARA EXTRAER DETALLES DE PASOS DESDE JSON DE SERENITY
param([string]$serenityPath = "target\site\serenity", [string]$outputPath = "target\reports")

# Definir funciones directamente aquí para evitar problemas de encoding
function Format-SecondsWithComma {
    param([int]$Milliseconds)
    $seconds = [math]::Round($Milliseconds / 1000, 2)
    return $seconds.ToString().Replace(".", ",")
}

if (!(Test-Path $outputPath)) { New-Item -ItemType Directory -Path $outputPath -Force | Out-Null }

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$dateFormatted = Get-Date -Format 'dd/MM/yyyy HH:mm:ss'

Write-Host ""
Write-Host "====== Extrayendo detalles de pasos desde Serenity JSON ======" -ForegroundColor Cyan

# FUNCION: Extraer pasos recursivamente
function Extract-TestSteps {
    param([array]$steps, [int]$level = 0)
    $result = @()
    foreach ($step in $steps) {
        $result += [PSCustomObject]@{
            Nivel = $level
            Descripcion = $step.description.Substring(0, [Math]::Min(150, $step.description.Length))
            Tiempo_ms = $step.duration
            Tiempo_Formateado = Format-SecondsWithComma -Milliseconds $step.duration
            Estado = $step.result
        }
        if ($step.children -and $step.children.Count -gt 0) {
            $result += Extract-TestSteps -steps $step.children -level ($level + 1)
        }
    }
    return $result
}

$allTestSteps = @()
$testNames = @()
$slowestSteps = @()

if (Test-Path $serenityPath) {
    Get-ChildItem "$serenityPath/*.json" -ErrorAction SilentlyContinue | ForEach-Object {
        $jsonContent = Get-Content $_.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
        if ($jsonContent.testSteps) {
            $testName = $jsonContent.title
            $testDuration = $jsonContent.duration
            Write-Host "  OK: $testName" -ForegroundColor Green
            
            $steps = Extract-TestSteps -steps $jsonContent.testSteps
            foreach ($step in $steps) {
                $step | Add-Member -NotePropertyName "TestName" -NotePropertyValue $testName -Force
                $allTestSteps += $step
                if ($step.Tiempo_ms -gt 5000) {
                    $slowestSteps += [PSCustomObject]@{
                        TestName = $testName
                        Paso = $step.Descripcion
                        Tiempo = $step.Tiempo_Formateado
                        Tiempo_ms = $step.Tiempo_ms
                    }
                }
            }
        }
    }
}

if ($allTestSteps.Count -eq 0) { Write-Host "ERROR: No data found" -ForegroundColor Red; exit 1 }
$slowestSteps = $slowestSteps | Sort-Object -Property Tiempo_ms -Descending | Select-Object -First 20

# GENERAR HTML
$htmlPath = "$outputPath/step_details_report_$timestamp.html"
$html = @"
<!DOCTYPE html>
<html>
<head><meta charset=UTF-8><title>SARA3 - Step Details</title>
<style>
body{font-family:monospace;background:#1e1e1e;color:#d4d4d4;padding:20px}
.container{max-width:1200px;margin:0 auto}
h1{color:#667eea;border-bottom:2px solid #667eea}
.test{background:#252526;padding:15px;margin:15px 0;border:1px solid #3e3e42}
.step{padding:8px;margin:3px 0;border-left:3px solid #666}
.step-slow{border-left-color:#ff6b6b;background:#2d1a1a}
.step-medium{border-left-color:#ffa500;background:#2d2520}
.step-ok{border-left-color:#52c41a}
.step-failed{border-left-color:#f5222d;background:#3a1f1f}
.time{color:#98c379;font-weight:bold}
.slowest{background:#2d2520;border-left:4px solid #ff6b6b;padding:15px;margin:20px 0}
.slowest-item{padding:10px;margin:5px 0;border-left:3px solid #ff9999;background:#1e1e1e}
</style></head>
<body><div class=container>
<h1>Test Steps Report - SARA3</h1>
<p>Generated: $dateFormatted | Tests: $($testNames | Select -Unique -ExpandProperty Count) | Steps: $($allTestSteps.Count)</p>
"@

$testGroups = $allTestSteps | Group-Object -Property TestName
foreach ($group in $testGroups) {
    $html += "<div class=test><h2>$($group.Name)</h2>"
    foreach ($step in $group.Group | Sort-Object -Property Descripcion) {
        $cls = "step-ok"
        if ($step.Tiempo_ms -gt 15000) { $cls = "step-slow" }
        elseif ($step.Tiempo_ms -gt 5000) { $cls = "step-medium" }
        if ($step.Estado -eq "FAILED") { $cls = "step-failed" }
        $indent = "&nbsp;" * ($step.Nivel * 4)
        $html += "<div class='step $cls'>$indent$($step.Descripcion)<br>$indent<span class=time>$($step.Tiempo_Formateado)</span> ($($step.Tiempo_ms) ms)</div>"
    }
    $html += "</div>"
}

if ($slowestSteps.Count -gt 0) {
    $html += "<h2>Top Slowest Steps</h2><div class=slowest>"
    foreach ($step in $slowestSteps) {
        $html += "<div class=slowest-item><strong>$($step.Paso)</strong><br>Test: $($step.TestName)<br><span class=time>$($step.Tiempo)</span> ($($step.Tiempo_ms) ms)</div>"
    }
    $html += "</div>"
}

$html += "</div></body></html>"
$html | Out-File -FilePath $htmlPath -Encoding UTF8 -Force
Write-Host "OK: HTML report: $htmlPath" -ForegroundColor Green

# GENERAR CSV
$csvPath = "$outputPath/step_details_$timestamp.csv"
"Test Name,Descripcion,Tiempo (ms),Tiempo Formateado,Estado" + "`n" | Out-File -FilePath $csvPath -Encoding UTF8
foreach ($step in ($allTestSteps | Sort-Object -Property TestName)) {
    $desc = $step.Descripcion -replace '"', '""'
    "`"$($step.TestName)`",`"$desc`",$($step.Tiempo_ms),$($step.Tiempo_Formateado),$($step.Estado)" | Add-Content -Path $csvPath
}
Write-Host "OK: CSV report: $csvPath" -ForegroundColor Green

# GENERAR CSV DE PASOS LENTOS
$slowestCsvPath = "$outputPath/slowest_steps_$timestamp.csv"
"Test Name,Paso,Tiempo (ms),Tiempo Formateado" + "`n" | Out-File -FilePath $slowestCsvPath -Encoding UTF8
foreach ($step in $slowestSteps) {
    $desc = $step.Paso -replace '"', '""'
    "`"$($step.TestName)`",`"$desc`",$($step.Tiempo_ms),$($step.Tiempo)" | Add-Content -Path $slowestCsvPath
}
Write-Host "OK: Slowest steps CSV: $slowestCsvPath" -ForegroundColor Green

Write-Host ""
Write-Host "====== REPORTE COMPLETADO ======" -ForegroundColor Green
Start-Process $htmlPath
