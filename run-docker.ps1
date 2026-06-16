#!/usr/bin/env pwsh
# ============================================
# Script para ejecutar Sara3 en Docker
# ============================================
#
# Uso: .\run-docker.ps1
#

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     INICIANDO CONTENEDOR SARA3 DOCKER                  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Obtener la ruta del script
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Definition
$appPath = $scriptPath

Write-Host "Ruta del proyecto: $appPath" -ForegroundColor Green
Write-Host ""

# Verificar que Docker esté corriendo
Write-Host "Verificando Docker..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✓ Docker está corriendo" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker no está corriendo. Abre Docker Desktop primero." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Ejecutar el contenedor
docker run --rm -it -v "$appPath`:/app" sara3:latest

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     CONTENEDOR DETENIDO                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
