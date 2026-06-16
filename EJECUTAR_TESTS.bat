@echo off
REM ============================================================
REM SARA3 - EJECUTAR TESTS EN DOCKER
REM Doble click para ejecutar todos los tests
REM ============================================================

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║  🚀 SARA3 - EJECUTANDO TESTS EN DOCKER              ║
echo ╚════════════════════════════════════════════════════════╝
echo.

REM Verificar si Docker está corriendo
docker ps >nul 2>&1
if errorlevel 1 (
    echo ❌ ERROR: Docker no está corriendo
    echo 💡 Inicia Docker Desktop y vuelve a intentar
    pause
    exit /b 1
)

REM Verificar si existe la imagen
docker images sara3:latest -q >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Imagen sara3:latest no encontrada
    echo 🔨 Construyendo imagen Docker (esto puede tardar)...
    docker build -t sara3:latest .
    if errorlevel 1 (
        echo ❌ ERROR al construir la imagen
        pause
        exit /b 1
    )
)

echo ✅ Imagen Docker lista
echo 🎬 Iniciando menú interactivo...
echo.
echo 💡 TIP: Podrás seleccionar qué tests ejecutar
echo.

REM Ejecutar contenedor en modo INTERACTIVO con volúmenes montados
docker run --rm -it ^
    -v "%CD%\target:/app/target" ^
    -v "%CD%\logs:/app/logs" ^
    sara3:latest

set EXIT_CODE=%errorlevel%

echo.
echo ╔════════════════════════════════════════════════════════╗
if %EXIT_CODE%==0 (
    echo ║  ✅ TESTS COMPLETADOS                                ║
    echo ║  📁 Reportes: target\site\serenity\index.html       ║
) else (
    echo ║  ❌ TESTS FINALIZARON CON ERRORES                    ║
    echo ║  📁 Ver logs en: logs\                              ║
)
echo ╚════════════════════════════════════════════════════════╝
echo.

pause
exit /b %EXIT_CODE%
