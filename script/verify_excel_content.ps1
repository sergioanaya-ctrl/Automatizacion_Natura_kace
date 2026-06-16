# Verificar contenido del Excel generado
Import-Module ImportExcel -ErrorAction SilentlyContinue

$excelFile = Get-ChildItem ".\target\reports\step_details_*.xlsx" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if ($excelFile) {
    Write-Host "====== VERIFICACION DEL EXCEL GENERADO ======"
    Write-Host "Archivo: $($excelFile.Name)`n"
    
    # Importar los datos de la hoja 'Todos los Pasos'
    $data = Import-Excel -Path $excelFile.FullName -WorksheetName 'Todos los Pasos'
    
    Write-Host "Total registros: $($data.Count)`n"
    
    Write-Host "Muestra de primeros 3 registros con detalles:"
    Write-Host "============================================"
    
    $data | Select-Object -First 3 | ForEach-Object {
        Write-Host "Descripción Completa: $($_.'Descripción Completa')"
        Write-Host "  Acción: $($_.'Acción')"
        Write-Host "  Elemento/Campo: $($_.'Elemento/Campo')"
        Write-Host "  Valor Ingresado: $($_.'Valor Ingresado')"
        Write-Host "  Tiempo: $($_.'Tiempo (s)') s ($($_.'Tiempo (ms)') ms)"
        Write-Host ""
    }
    
    Write-Host "Resumen de acciones encontradas:"
    Write-Host "================================="
    $data | Group-Object -Property 'Acción' | Select-Object Name, Count | Sort-Object Count -Descending | Format-Table
    
} else {
    Write-Host "No se encontró archivo Excel"
}
