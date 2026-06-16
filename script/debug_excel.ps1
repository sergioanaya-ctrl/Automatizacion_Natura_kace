Import-Module ImportExcel -WarningAction SilentlyContinue

# Leer la hoja de detalles
$datos = Import-Excel 'reports_consolidation/consolidated_report_20260513_165028.xlsx' -WorksheetName 'Detalles Paso a Paso' -ErrorAction SilentlyContinue

Write-Host 'Columnas disponibles:' -ForegroundColor Yellow
$datos[0].PSObject.Properties.Name

Write-Host ''
Write-Host 'Primeros 10 registros (ver Error Type):' -ForegroundColor Cyan
$datos | Select-Object -First 10 | Select-Object Test, Descripcion, 'Error Type', 'Error Message' | Format-Table -AutoSize

Write-Host ''
Write-Host 'Valores únicos en Error Type:' -ForegroundColor Yellow
$errorTypes = $datos | Select-Object -ExpandProperty 'Error Type' -Unique
$errorTypes | Sort-Object | ForEach-Object { Write-Host "  - '$_'" }

Write-Host ''
Write-Host 'Cantidad de pasos por Error Type:' -ForegroundColor Yellow
$datos | Group-Object -Property 'Error Type' | Select-Object Name, Count | Sort-Object Count -Descending | Format-Table -AutoSize

Write-Host ''
Write-Host 'Registros que NO son vacíos en Error Type:' -ForegroundColor Yellow
$conError = $datos | Where-Object { $_.'Error Type' -and $_.'Error Type'.Trim() -ne "" }
Write-Host "Total: $($conError.Count) de $($datos.Count)"
