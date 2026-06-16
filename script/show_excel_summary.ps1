# Resumen final de la generación de Excel
Import-Module ImportExcel -ErrorAction SilentlyContinue

Write-Host "====== REPORTE DE DETALLES DE PASOS - EXCEL GENERADO ======"
Write-Host ""

$excelFile = Get-ChildItem ".\target\reports\step_details_*.xlsx" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if ($excelFile) {
    Write-Host "ARCHIVO GENERADO:"
    Write-Host "  Ruta: $($excelFile.FullName)"
    Write-Host "  Tamaño: $([math]::Round($excelFile.Length/1KB, 2)) KB"
    Write-Host ""
    
    # Importar los datos de la hoja 'Todos los Pasos'
    $data = Import-Excel -Path $excelFile.FullName -WorksheetName 'Todos los Pasos'
    
    Write-Host "ESTADISTICAS GENERALES:"
    Write-Host "  Total de pasos: $($data.Count)"
    Write-Host ""
    
    Write-Host "CATEGORIZACION DE ACCIONES:"
    $actionCounts = $data | Group-Object -Property 'Acción' | Sort-Object Count -Descending
    foreach ($action in $actionCounts) {
        $percentage = [math]::Round(($action.Count / $data.Count) * 100, 1)
        Write-Host "  * $($action.Name): $($action.Count) pasos ($percentage%)"
    }
    Write-Host ""
    
    Write-Host "TIEMPOS:"
    $totalMs = ($data | Measure-Object -Property 'Tiempo (ms)' -Sum).Sum
    $totalSeconds = $totalMs / 1000
    $avgSeconds = $totalSeconds / $data.Count
    
    $totalSecondsFormatted = $totalSeconds.ToString("N2", [System.Globalization.CultureInfo]::GetCultureInfo("es-CO"))
    $avgSecondsFormatted = $avgSeconds.ToString("N2", [System.Globalization.CultureInfo]::GetCultureInfo("es-CO"))
    
    Write-Host "  Tiempo total: $totalSecondsFormatted s"
    Write-Host "  Tiempo promedio por paso: $avgSecondsFormatted s"
    
    # Pasos más lentos
    $slowestSteps = $data | Sort-Object 'Tiempo (ms)' -Descending | Select-Object -First 3
    Write-Host ""
    Write-Host "PASOS MAS LENTOS:"
    $slowestSteps | ForEach-Object {
        $desc = $_.'Descripción Completa'
        if ($desc.Length -gt 50) { $desc = $desc.Substring(0, 50) + "..." }
        Write-Host "  * $desc"
        Write-Host "    Accion: $($_.'Acción') | Tiempo: $($_.'Tiempo (s)') s"
    }
    
    Write-Host ""
    Write-Host "HOJAS DISPONIBLES EN EL EXCEL:"
    Write-Host "  [1] Resumen: Estadísticas generales (4 métricas)"
    Write-Host "  [2] Todos los Pasos: Detalle completo de cada paso (41 pasos)"
    Write-Host "  [3] Pasos Lentos: Pasos > 5 segundos ordenados por duración"
    Write-Host "  [4] Estadísticas por Test: Consolidado por test"
    
    Write-Host ""
    Write-Host "COLUMNAS DISPONIBLES:"
    Write-Host "  * Descripcion Completa: Texto original del paso"
    Write-Host "  * Accion: Tipo de accion (Escribe, Click/Selecciona, Ejecuta, Navega, Abre, Completa, Otra)"
    Write-Host "  * Elemento/Campo: Nombre del campo, boton o elemento interactuado"
    Write-Host "  * Valor Ingresado: Valor o dato introducido (para Escribe)"
    Write-Host "  * Nivel: Nivel de anidamiento en el arbol de pasos"
    Write-Host "  * Tiempo (ms): Duracion en milisegundos"
    Write-Host "  * Tiempo (s): Duracion en segundos (formato Spanish con coma)"
    Write-Host "  * Estado: Resultado del paso (Success/Failure)"
    
    Write-Host ""
    Write-Host "EJEMPLO DE PASOS DETALLADOS:"
    $examples = $data | Where-Object { $_.'Acción' -in @('Escribe', 'Click/Selecciona') } | Select-Object -First 2
    $examples | ForEach-Object {
        Write-Host ""
        Write-Host "  Accion: $($_.'Acción')"
        Write-Host "  Descripcion: $($_.'Descripción Completa')"
        Write-Host "  Elemento: $($_.'Elemento/Campo')"
        Write-Host "  Valor: $($_.'Valor Ingresado')"
        Write-Host "  Duracion: $($_.'Tiempo (s)') s ($($_.'Tiempo (ms)') ms)"
    }
    
    Write-Host ""
    Write-Host "====== REPORTE COMPLETADO EXITOSAMENTE ======"
    
} else {
    Write-Host "No se encontro archivo Excel"
}
