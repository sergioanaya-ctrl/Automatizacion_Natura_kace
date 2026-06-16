@echo off
REM ============================================
REM Script para ejecutar Sara3 en Docker
REM ============================================
REM
REM Este script inicia el contenedor Sara3
REM automaticamente sin necesidad de escribir comandos largos.
REM
REM Uso: Simplemente haz doble clic en este archivo
REM

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║     INICIANDO CONTENEDOR SARA3 DOCKER                  ║
echo ╚════════════════════════════════════════════════════════╝
echo.

REM Cambiar a la carpeta del script
cd /d "%~dp0"

REM Mostrar la ruta actual
echo Ruta: %cd%
echo.

REM Ejecutar el contenedor
docker run --rm -it -v %cd%:/app sara3:latest

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║     CONTENEDOR DETENIDO                                ║
echo ╚════════════════════════════════════════════════════════╝
echo.

pause
