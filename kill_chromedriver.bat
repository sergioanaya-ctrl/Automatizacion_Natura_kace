@echo off
REM kill_chromedriver.bat
REM Mata todos los procesos chromedriver en Windows.

echo Buscando procesos chromedriver...
for /f "tokens=2 delims=," %%P in ('tasklist /FI "IMAGENAME eq chromedriver.exe" /FO CSV /NH 2^>nul') do (
    echo Matando chromedriver PID=%%~P...
    taskkill /PID %%~P /F >nul 2>&1
    if errorlevel 1 (
        echo No se pudo matar chromedriver PID=%%~P
    ) else (
        echo chromedriver PID=%%~P terminado.
    )
)

echo Proceso completado.
pause
