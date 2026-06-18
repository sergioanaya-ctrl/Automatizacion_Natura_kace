@echo off
REM ========================================================
REM Script para Ejecutar Pruebas en Paralelo con Multi-Usuario
REM 50 Usuarios Configurados (BOT01 - BOT50)
REM ========================================================

echo.
echo ========================================================
echo   EJECUCION PARALELA - SARA3 AUTOMATION
echo   50 USUARIOS DISPONIBLES (BOT01 - BOT50)
echo ========================================================
echo.

:menu
echo Selecciona el modo de ejecucion:
echo.
echo [1] 50 NAVEGADORES EN PARALELO - TODOS LOS USUARIOS
echo [2] 12 navegadores en paralelo (Usuarios 01-12)
echo [3] 6 navegadores en paralelo (Usuarios 01-06)
echo [4] 3 navegadores en paralelo (Usuarios 01-03)
echo [5] 1 navegador SECUENCIAL (modo debug)
echo [6] Ver reporte de resultados
echo [7] Limpiar y ejecutar tests
echo [8] Salir
echo.
set /p opcion="Ingresa tu opcion (1-8): "

if "%opcion%"=="1" goto paralelo_50
if "%opcion%"=="2" goto paralelo_12
if "%opcion%"=="3" goto paralelo_6
if "%opcion%"=="4" goto paralelo_3
if "%opcion%"=="5" goto secuencial
if "%opcion%"=="6" goto reporte
if "%opcion%"=="7" goto clean_test
if "%opcion%"=="8" goto fin
echo Opcion invalida, intenta de nuevo
goto menu

:paralelo_50
echo.
echo ========================================================
echo   EJECUTANDO: 50 USUARIOS EN PARALELO
echo ========================================================
echo   - Runners: CasesRunner01 - CasesRunner50
echo   - Usuarios: BOT01 - BOT50 (asignacion automatica)
echo   - Navegadores simultaneos: 12 (maxParallelForks)
echo ========================================================
echo.
echo Los 50 tests se ejecutaran en lotes de 12
echo Tiempo estimado: 20-25 minutos
echo.
pause
call gradlew.bat test --tests "com.natura.automation.runners.CasesRunner01" --tests "com.natura.automation.runners.CasesRunner02" --tests "com.natura.automation.runners.CasesRunner03" --tests "com.natura.automation.runners.CasesRunner04" --tests "com.natura.automation.runners.CasesRunner05" --tests "com.natura.automation.runners.CasesRunner06" --tests "com.natura.automation.runners.CasesRunner07" --tests "com.natura.automation.runners.CasesRunner08" --tests "com.natura.automation.runners.CasesRunner09" --tests "com.natura.automation.runners.CasesRunner10" --tests "com.natura.automation.runners.CasesRunner11" --tests "com.natura.automation.runners.CasesRunner12" --tests "com.natura.automation.runners.CasesRunner13" --tests "com.natura.automation.runners.CasesRunner14" --tests "com.natura.automation.runners.CasesRunner15" --tests "com.natura.automation.runners.CasesRunner16" --tests "com.natura.automation.runners.CasesRunner17" --tests "com.natura.automation.runners.CasesRunner18" --tests "com.natura.automation.runners.CasesRunner19" --tests "com.natura.automation.runners.CasesRunner20" --tests "com.natura.automation.runners.CasesRunner21" --tests "com.natura.automation.runners.CasesRunner22" --tests "com.natura.automation.runners.CasesRunner23" --tests "com.natura.automation.runners.CasesRunner24" --tests "com.natura.automation.runners.CasesRunner25" --tests "com.natura.automation.runners.CasesRunner26" --tests "com.natura.automation.runners.CasesRunner27" --tests "com.natura.automation.runners.CasesRunner28" --tests "com.natura.automation.runners.CasesRunner29" --tests "com.natura.automation.runners.CasesRunner30" --tests "com.natura.automation.runners.CasesRunner31" --tests "com.natura.automation.runners.CasesRunner32" --tests "com.natura.automation.runners.CasesRunner33" --tests "com.natura.automation.runners.CasesRunner34" --tests "com.natura.automation.runners.CasesRunner35" --tests "com.natura.automation.runners.CasesRunner36" --tests "com.natura.automation.runners.CasesRunner37" --tests "com.natura.automation.runners.CasesRunner38" --tests "com.natura.automation.runners.CasesRunner39" --tests "com.natura.automation.runners.CasesRunner40" --tests "com.natura.automation.runners.CasesRunner41" --tests "com.natura.automation.runners.CasesRunner42" --tests "com.natura.automation.runners.CasesRunner43" --tests "com.natura.automation.runners.CasesRunner44" --tests "com.natura.automation.runners.CasesRunner45" --tests "com.natura.automation.runners.CasesRunner46" --tests "com.natura.automation.runners.CasesRunner47" --tests "com.natura.automation.runners.CasesRunner48" --tests "com.natura.automation.runners.CasesRunner49" --tests "com.natura.automation.runners.CasesRunner50"
goto resultado

:paralelo_12
echo.
echo ========================================================
echo   EJECUTANDO: 12 NAVEGADORES EN PARALELO
echo ========================================================
echo   - Usuarios: BOT01 - BOT12
echo ========================================================
echo.
call gradlew.bat test --tests "com.natura.automation.runners.CasesRunner01" --tests "com.natura.automation.runners.CasesRunner02" --tests "com.natura.automation.runners.CasesRunner03" --tests "com.natura.automation.runners.CasesRunner04" --tests "com.natura.automation.runners.CasesRunner05" --tests "com.natura.automation.runners.CasesRunner06" --tests "com.natura.automation.runners.CasesRunner07" --tests "com.natura.automation.runners.CasesRunner08" --tests "com.natura.automation.runners.CasesRunner09" --tests "com.natura.automation.runners.CasesRunner10" --tests "com.natura.automation.runners.CasesRunner11" --tests "com.natura.automation.runners.CasesRunner12"
goto resultado

:paralelo_6
echo.
echo ========================================================
echo   EJECUTANDO: 6 NAVEGADORES EN PARALELO
echo ========================================================
echo   - Usuarios: BOT01 - BOT06
echo ========================================================
echo.
call gradlew.bat test --tests "com.natura.automation.runners.CasesRunner01" --tests "com.natura.automation.runners.CasesRunner02" --tests "com.natura.automation.runners.CasesRunner03" --tests "com.natura.automation.runners.CasesRunner04" --tests "com.natura.automation.runners.CasesRunner05" --tests "com.natura.automation.runners.CasesRunner06"
goto resultado

:paralelo_3
echo.
echo ========================================================
echo   EJECUTANDO: 3 NAVEGADORES EN PARALELO
echo ========================================================
echo   - Usuarios: BOT01 - BOT03
echo ========================================================
echo.
call gradlew.bat test --tests "com.natura.automation.runners.CasesRunner01" --tests "com.natura.automation.runners.CasesRunner02" --tests "com.natura.automation.runners.CasesRunner03"
goto resultado

:secuencial
echo.
echo ========================================================
echo   EJECUTANDO: MODO SECUENCIAL (DEBUG)
echo ========================================================
echo   - Usuario: BOT01
echo ========================================================
echo.
call gradlew.bat test --tests CasesRunner01
goto resultado

:reporte
echo.
echo Generando reporte de Serenity...
call gradlew.bat aggregate
echo.
echo Abriendo reporte en navegador...
start target\site\serenity\index.html
goto menu

:clean_test
echo.
echo Limpiando proyecto y ejecutando tests...
call gradlew.bat clean test --tests "com.natura.automation.runners.CasesRunner01" --tests "com.natura.automation.runners.CasesRunner02" --tests "com.natura.automation.runners.CasesRunner03" --tests "com.natura.automation.runners.CasesRunner04" --tests "com.natura.automation.runners.CasesRunner05" --tests "com.natura.automation.runners.CasesRunner06" --tests "com.natura.automation.runners.CasesRunner07" --tests "com.natura.automation.runners.CasesRunner08" --tests "com.natura.automation.runners.CasesRunner09" --tests "com.natura.automation.runners.CasesRunner10" --tests "com.natura.automation.runners.CasesRunner11" --tests "com.natura.automation.runners.CasesRunner12" --tests "com.natura.automation.runners.CasesRunner13" --tests "com.natura.automation.runners.CasesRunner14" --tests "com.natura.automation.runners.CasesRunner15" --tests "com.natura.automation.runners.CasesRunner16" --tests "com.natura.automation.runners.CasesRunner17" --tests "com.natura.automation.runners.CasesRunner18" --tests "com.natura.automation.runners.CasesRunner19" --tests "com.natura.automation.runners.CasesRunner20" --tests "com.natura.automation.runners.CasesRunner21" --tests "com.natura.automation.runners.CasesRunner22" --tests "com.natura.automation.runners.CasesRunner23" --tests "com.natura.automation.runners.CasesRunner24" --tests "com.natura.automation.runners.CasesRunner25" --tests "com.natura.automation.runners.CasesRunner26" --tests "com.natura.automation.runners.CasesRunner27" --tests "com.natura.automation.runners.CasesRunner28" --tests "com.natura.automation.runners.CasesRunner29" --tests "com.natura.automation.runners.CasesRunner30" --tests "com.natura.automation.runners.CasesRunner31" --tests "com.natura.automation.runners.CasesRunner32" --tests "com.natura.automation.runners.CasesRunner33" --tests "com.natura.automation.runners.CasesRunner34" --tests "com.natura.automation.runners.CasesRunner35" --tests "com.natura.automation.runners.CasesRunner36" --tests "com.natura.automation.runners.CasesRunner37" --tests "com.natura.automation.runners.CasesRunner38" --tests "com.natura.automation.runners.CasesRunner39" --tests "com.natura.automation.runners.CasesRunner40" --tests "com.natura.automation.runners.CasesRunner41" --tests "com.natura.automation.runners.CasesRunner42" --tests "com.natura.automation.runners.CasesRunner43" --tests "com.natura.automation.runners.CasesRunner44" --tests "com.natura.automation.runners.CasesRunner45" --tests "com.natura.automation.runners.CasesRunner46" --tests "com.natura.automation.runners.CasesRunner47" --tests "com.natura.automation.runners.CasesRunner48" --tests "com.natura.automation.runners.CasesRunner49" --tests "com.natura.automation.runners.CasesRunner50"
goto resultado

:resultado
echo.
echo ========================================================
echo   EJECUCION COMPLETADA
echo ========================================================
echo.
echo Deseas generar el reporte de Serenity? (S/N)
set /p generar_reporte="Respuesta: "
if /i "%generar_reporte%"=="S" (
    call gradlew.bat aggregate
    echo.
    echo Abriendo reporte...
    start target\site\serenity\index.html
)
echo.
goto menu

:fin
echo.
echo Saliendo...
exit /b 0
