# Verificar pasos de "Click" con elementos extraídos
Import-Module ImportExcel -ErrorAction SilentlyContinue

$excelFile = Get-ChildItem ".\target\reports\step_details_*.xlsx" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if ($excelFile) {
    Write-Host "====== PASOS DE CLICK EXTRAIDOS ======"
    
    # Importar los datos de la hoja 'Todos los Pasos'
    $data = Import-Excel -Path $excelFile.FullName -WorksheetName 'Todos los Pasos'
    
    # Filtrar solo los pasos de Click
    $clickSteps = $data | Where-Object { $_.'Acción' -eq 'Click/Selecciona' }
    
    Write-Host "Total pasos de click: $($clickSteps.Count)`n"
    
    $clickSteps | ForEach-Object {
        Write-Host "Descripción: $($_.'Descripción Completa')"
        Write-Host "  Elemento: $($_.'Elemento/Campo')"
        Write-Host "  Tiempo: $($_.'Tiempo (s)') s`n"
    }
    
} else {
    Write-Host "No se encontró archivo Excel"
}
