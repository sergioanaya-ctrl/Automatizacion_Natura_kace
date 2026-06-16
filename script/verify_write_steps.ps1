# Verificar pasos de "Escribe" con valores extraídos
Import-Module ImportExcel -ErrorAction SilentlyContinue

$excelFile = Get-ChildItem ".\target\reports\step_details_*.xlsx" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if ($excelFile) {
    Write-Host "====== PASOS DE ESCRITURA EXTRAIDOS ======"
    
    # Importar los datos de la hoja 'Todos los Pasos'
    $data = Import-Excel -Path $excelFile.FullName -WorksheetName 'Todos los Pasos'
    
    # Filtrar solo los pasos de Escribe
    $writeSteps = $data | Where-Object { $_.'Acción' -eq 'Escribe' }
    
    Write-Host "Total pasos de escritura: $($writeSteps.Count)`n"
    
    $writeSteps | ForEach-Object {
        Write-Host "Descripción: $($_.'Descripción Completa')"
        Write-Host "  Campo: $($_.'Elemento/Campo')"
        Write-Host "  Valor: $($_.'Valor Ingresado')"
        Write-Host "  Tiempo: $($_.'Tiempo (s)') s`n"
    }
    
} else {
    Write-Host "No se encontró archivo Excel"
}
