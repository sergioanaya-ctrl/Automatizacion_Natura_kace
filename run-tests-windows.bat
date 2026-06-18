@echo off
REM ============================================================================
REM SCRIPT PARA EJECUTAR AUTOMATIZACIÓN SARA3 EN WINDOWS
REM ============================================================================
REM Uso: run-tests-windows.bat [runner_class]
REM Ejemplo: run-tests-windows.bat com.natura.automation.runners.CasesRunner15
REM
REM Este script es un wrapper para facilitar la ejecución en Windows
REM ============================================================================

setlocal enabledelayedexpansion

REM Configuración
set RUNNER_CLASS=%1
if "%RUNNER_CLASS%"=="" set RUNNER_CLASS=com.natura.automation.runners.CasesRunner15

REM Información de sistema
cls
echo.
echo ============================================================================
echo SARA3 - AUTOMATIZACIÓN DE CASES (WINDOWS)
echo ============================================================================
echo.

echo [SETUP] Sistema: WINDOWS
echo [SETUP] Runner: %RUNNER_CLASS%
echo [SETUP] Java Version:
java -version 2>&1

REM Configurar variables de entorno para mejor rendimiento
set GRADLE_OPTS=-Xmx2g -Xms512m
set JAVA_TOOL_OPTIONS=-Xmx2g -Xms512m

echo.
echo [SETUP] Iniciando ejecución de tests...
echo.

REM Limpiar y ejecutar
call gradlew.bat clean test --tests "%RUNNER_CLASS%" --info --continue --no-daemon

REM Capturar resultado
set TEST_RESULT=%ERRORLEVEL%

echo.
if %TEST_RESULT% equ 0 (
    echo [RESULT] ✓ TESTS EJECUTADOS EXITOSAMENTE
    echo [RESULT] Reporte disponible en: target\site\serenity\index.html
) else (
    echo [RESULT] ✗ FALLÓ LA EJECUCIÓN (Exit Code: %TEST_RESULT%)
)

echo.
pause
exit /b %TEST_RESULT%
