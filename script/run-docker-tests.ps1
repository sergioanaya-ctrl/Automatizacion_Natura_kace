#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Ejecuta tests SARA3 en Docker
    
.DESCRIPTION
    Script para ejecutar los tests automatizados dentro del contenedor Docker
    
.PARAMETER BuildImage
    Si se especifica, reconstruye la imagen Docker (--no-cache)
    
.PARAMETER Interactive
    Modo interactivo - accede a una terminal bash dentro del contenedor
    
.PARAMETER Test
    Número de test específico a ejecutar (1-50)
    
.EXAMPLE
    .\\script\\run-docker-tests.ps1                    # Ejecutar todos los tests
    .\\script\\run-docker-tests.ps1 -BuildImage        # Reconstruir imagen + ejecutar
    .\\script\\run-docker-tests.ps1 -Interactive       # Terminal interactiva
    .\\script\\run-docker-tests.ps1 -Test 15           # Solo test CasesRunner15
#>

param(
    [switch]$BuildImage = $false,
    [switch]$Interactive = $false,
    [int]$Test = 0
)

$ErrorActionPreference = "Stop"

# Colores
$colors = @{
    Red    = "`e[91m"
    Green  = "`e[92m"
    Yellow = "`e[93m"
    Blue   = "`e[94m"
    Reset  = "`e[0m"
}

function Write-Log {
    param([string]$Message, [string]$Type = "Info")
    $color = $colors[$Type] ?? $colors["Blue"]
    Write-Host "${color}[$Type]${$colors['Reset']} $Message"
}

# Rutas
$ProjectPath = $PSScriptRoot
$TargetMount = "${ProjectPath}\target:/app/target"
$LogsMount = "${ProjectPath}\logs:/app/logs"
$ImageName = "sara3:latest"

Write-Log "╔════════════════════════════════════════════════════════╗" "Blue"
Write-Log "║  SARA3 DOCKER TEST RUNNER                             ║" "Blue"
Write-Log "╚════════════════════════════════════════════════════════╝" "Blue"

# Rebuild image si se especifica
if ($BuildImage) {
    Write-Log "🔨 Reconstruyendo imagen Docker (--no-cache)..." "Yellow"
    & docker build --no-cache -t $ImageName .
    if ($LASTEXITCODE -ne 0) {
        Write-Log "❌ Error al construir la imagen Docker" "Red"
        exit 1
    }
    Write-Log "✅ Imagen construida exitosamente" "Green"
}

# Verificar que la imagen existe
Write-Log "📦 Verificando imagen Docker..." "Blue"
$imageExists = & docker images -q $ImageName
if (-not $imageExists) {
    Write-Log "⚠️  Imagen no encontrada. Construyendo..." "Yellow"
    & docker build -t $ImageName .
    if ($LASTEXITCODE -ne 0) {
        Write-Log "❌ Error al construir la imagen" "Red"
        exit 1
    }
}

Write-Log "✅ Imagen lista: $ImageName" "Green"

# Modo interactivo
if ($Interactive) {
    Write-Log "🚀 Iniciando contenedor en modo interactivo..." "Blue"
    Write-Log "💡 Dentro del contenedor puedes ejecutar:" "Yellow"
    Write-Log "   bash run-tests-linux.sh          # Ejecutar todos los tests" "Yellow"
    Write-Log "   ./gradlew test --tests '*CasesRunner15*'  # Test específico" "Yellow"
    Write-Log "   exit                             # Salir del contenedor" "Yellow"
    Write-Log "" "Blue"
    
    & docker run -it --rm `
        -v "${ProjectPath}\target:/app/target" `
        -v "${ProjectPath}\logs:/app/logs" `
        $ImageName bash
    exit $LASTEXITCODE
}

# Modo test específico
if ($Test -gt 0) {
    Write-Log "🎯 Ejecutando test específico: CasesRunner$Test" "Blue"
    & docker run --rm `
        -v "${ProjectPath}\target:/app/target" `
        -v "${ProjectPath}\logs:/app/logs" `
        $ImageName `
        ./gradlew test --tests "*CasesRunner$($Test.ToString('D2'))*"
    exit $LASTEXITCODE
}

# Modo normal: ejecutar todos los tests
Write-Log "🚀 Ejecutando todos los tests en paralelo..." "Blue"
Write-Log "📊 Configuración:" "Yellow"
Write-Log "   Max Parallel Forks: 8" "Yellow"
Write-Log "   Timeout por test: 2 horas" "Yellow"
Write-Log "   Navegador: Chromium (Headless)" "Yellow"
Write-Log "   Display: Xvfb :99 (Virtual)" "Yellow"
Write-Log "" "Blue"

$StartTime = Get-Date

& docker run --rm `
    -v "${ProjectPath}\target:/app/target" `
    -v "${ProjectPath}\logs:/app/logs" `
    $ImageName

$ExitCode = $LASTEXITCODE
$Duration = (Get-Date) - $StartTime

Write-Log "" "Blue"
Write-Log "╔════════════════════════════════════════════════════════╗" "Blue"

if ($ExitCode -eq 0) {
    Write-Log "║  ✅ TESTS COMPLETADOS EXITOSAMENTE                   ║" "Green"
} else {
    Write-Log "║  ❌ TESTS FINALIZARON CON ERRORES                     ║" "Red"
}

Write-Log "║  ⏱️  Duración: $($Duration.TotalMinutes.ToString('F1')) minutos" "Blue"
Write-Log "║  📁 Reportes: .\target\site\serenity\index.html" "Blue"
Write-Log "╚════════════════════════════════════════════════════════╝" "Blue"

exit $ExitCode



