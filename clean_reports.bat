@echo off
setlocal enabledelayedexpansion

cls
echo.
echo ========================================================
echo    LIMPIAR TODOS LOS REPORTES - NATURA
echo ========================================================
echo.
echo Este script limpia TODOS los tipos de reportes:
echo   - Reportes de Serenity BDD
echo   - Resultados de tests (JUnit XML)
echo   - Reportes de performance y timing
echo   - Reportes de Cucumber
echo   - Logs de Gradle y test output
echo   - Archivos Excel, CSV, HTML generados
echo.
echo Analizando carpetas y archivos...
echo.
timeout /t 2 >nul

set deleted=0
set errors=0

REM =====================================================
REM Limpiar carpetas principales
REM =====================================================

echo Limpiando carpetas de reportes...

REM target\site\serenity
if exist "target\site\serenity" (
    rmdir /s /q "target\site\serenity" 2>nul
    echo [OK] target\site\serenity
    set /a deleted+=1
)

REM target\test-results
if exist "target\test-results" (
    rmdir /s /q "target\test-results" 2>nul
    echo [OK] target\test-results
    set /a deleted+=1
)

REM target\reports
if exist "target\reports" (
    rmdir /s /q "target\reports" 2>nul
    echo [OK] target\reports
    set /a deleted+=1
)

REM target\reports_historic
if exist "target\reports_historic" (
    rmdir /s /q "target\reports_historic" 2>nul
    echo [OK] target\reports_historic
    set /a deleted+=1
)

REM target\cucumber-reports
if exist "target\cucumber-reports" (
    rmdir /s /q "target\cucumber-reports" 2>nul
    echo [OK] target\cucumber-reports
    set /a deleted+=1
)

REM target\app_performance_logs
if exist "target\app_performance_logs" (
    rmdir /s /q "target\app_performance_logs" 2>nul
    echo [OK] target\app_performance_logs
    set /a deleted+=1
)

REM build\reports
if exist "build\reports" (
    rmdir /s /q "build\reports" 2>nul
    echo [OK] build\reports
    set /a deleted+=1
)

REM build\test-results
if exist "build\test-results" (
    rmdir /s /q "build\test-results" 2>nul
    echo [OK] build\test-results
    set /a deleted+=1
)

echo.
echo Limpiando archivos en raiz...

REM =====================================================
REM Limpiar archivos en raiz
REM =====================================================

REM Test timing reports
if exist "test_timings_report.xlsx" del /f /q "test_timings_report.xlsx" && echo [OK] test_timings_report.xlsx && set /a deleted+=1
if exist "test_timings_report.csv" del /f /q "test_timings_report.csv" && echo [OK] test_timings_report.csv && set /a deleted+=1
if exist "test_timings_report.html" del /f /q "test_timings_report.html" && echo [OK] test_timings_report.html && set /a deleted+=1

REM Gradle output
if exist "gradle-test-output.txt" del /f /q "gradle-test-output.txt" && echo [OK] gradle-test-output.txt && set /a deleted+=1
if exist "gradle-test-output-current.txt" del /f /q "gradle-test-output-current.txt" && echo [OK] gradle-test-output-current.txt && set /a deleted+=1

REM Test output
if exist "test_output.txt" del /f /q "test_output.txt" && echo [OK] test_output.txt && set /a deleted+=1
if exist "test_log.txt" del /f /q "test_log.txt" && echo [OK] test_log.txt && set /a deleted+=1

REM Performance files with wildcard
for %%f in (app_performance_*.csv app_performance_*.html app_performance_*.xlsx) do (
    if exist "%%f" (
        del /f /q "%%f"
        echo [OK] %%f
        set /a deleted+=1
    )
)

REM Report files with timestamp
for %%f in (report_*.xlsx report_*.csv report_*.html) do (
    if exist "%%f" (
        del /f /q "%%f"
        echo [OK] %%f
        set /a deleted+=1
    )
)

echo.
echo ========================================================
echo RESUMEN DE LIMPIEZA
echo ========================================================
echo Items limpiados: %deleted%
echo ========================================================
echo.

if %deleted% gtr 0 (
    echo [SUCCESS] Se limpiaron %deleted% elementos exitosamente!
    echo.
) else (
    echo [INFO] No habia archivos/carpetas para limpiar
    echo.
)

pause
exit /b 0
