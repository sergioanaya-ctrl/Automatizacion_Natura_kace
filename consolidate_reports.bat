@echo off
:: ============================================
:: CONSOLIDADOR DE REPORTES XLSX - AUTOMATICO
:: ============================================
:: Este script consolida reportes XLSX de múltiples máquinas
:: en un solo reporte unificado
::
:: USO:
:: 1. Copia los archivos step_details_*.xlsx de cada máquina
::    a la carpeta: .\reports_consolidation\
:: 2. Ejecuta este archivo .bat
:: 3. Los reportes se generarán AUTOMATICAMENTE

setlocal enabledelayedexpansion

echo.
echo ================================================
echo   CONSOLIDADOR DE REPORTES XLSX - MULTIPLES MAQUINAS
echo ================================================
echo.
echo Procesando archivos XLSX...
echo.

:: Ejecutar el script PowerShell de consolidación (sin pausa)
powershell -ExecutionPolicy Bypass -NoProfile -File "script\\consolidate_reports_xlsx.ps1" -NoWait

echo.
echo ================================================
echo   CONSOLIDACION FINALIZADA
echo ================================================
echo.
echo Los reportes consolidados estan en:
echo   .\reports_consolidation\
echo.
echo Archivos generados:
echo   - consolidated_report_*.csv (Todos los pasos)
echo   - consolidated_report_stats_*.csv (Estadisticas por test)
echo   - consolidated_report_by_machine_*.csv (Por maquina)
echo   - consolidated_report_by_user_*.csv (Por usuario)
echo   - consolidated_report_*.xlsx (Excel - 8 hojas)
echo.

:: Buscar el archivo Excel mas reciente
setlocal enabledelayedexpansion
set LATEST_EXCEL=
for /f "tokens=*" %%F in ('powershell -Command "Get-ChildItem reports_consolidation\consolidated_report_*.xlsx -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty FullName"') do (
    set LATEST_EXCEL=%%F
)

if defined LATEST_EXCEL (
    echo.
    echo Abriendo archivo Excel: !LATEST_EXCEL!
    echo.
    start "" "!LATEST_EXCEL!"
    timeout /t 2 >nul
) else (
    echo.
    echo NOTA: No se genero archivo Excel
    echo.
)

echo Presiona cualquier tecla para salir...
pause >nul


