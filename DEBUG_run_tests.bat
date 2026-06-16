@echo off
REM ========================================================
REM Script de DEBUG para diagnosticar problemas
REM ========================================================

echo.
echo ========================================================
echo         DIAGNOSTICO DEL PROYECTO SARA3
echo ========================================================
echo.

echo [1] Verificando estructura del proyecto...
echo     Carpeta actual: %cd%
echo.

if exist "gradlew.bat" (
    echo     [✓] gradlew.bat encontrado
) else (
    echo     [✗] gradlew.bat NO encontrado - PROBLEMA
)

if exist "build.gradle" (
    echo     [✓] build.gradle encontrado
) else (
    echo     [✗] build.gradle NO encontrado - PROBLEMA
)

if exist "src\test\java" (
    echo     [✓] src\test\java encontrado
) else (
    echo     [✗] src\test\java NO encontrado - PROBLEMA
)

if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo     [✓] gradle-wrapper.properties encontrado
) else (
    echo     [✗] gradle-wrapper.properties NO encontrado - PROBLEMA
)

echo.
echo [2] Verificando Gradle (descargará JDK automático si es necesario)...
call .\gradlew.bat --version
if %errorlevel% equ 0 (
    echo     [✓] Gradle funciona
) else (
    echo     [✗] Gradle NO funciona
)

echo.
echo [3] Intentando compilar (con salida completa)...
echo.
call .\gradlew.bat compileTestJava --stacktrace
if %errorlevel% equ 0 (
    echo.
    echo     [✓] Compilacion exitosa
) else (
    echo.
    echo     [✗] Error en compilacion - ver arriba
)

echo.
echo.
pause
