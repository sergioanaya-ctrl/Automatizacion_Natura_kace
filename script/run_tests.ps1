# run_tests.ps1 - Script para ejecutar tests SARA3 en Docker desde Windows

param(
    [string]$TestMode = "menu",
    [int]$TestNumber = 0,
    [int]$StartRange = 0,
    [int]$EndRange = 0
)

$ErrorActionPreference = "Stop"
$workspaceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$targetMount = "$($workspaceRoot)\target:/app/target"
$logsMount = "$($workspaceRoot)\logs:/app/logs"
$imageTag = "sara3:latest"

function Show-Banner {
    Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║          SARA3 TEST RUNNER - Docker              ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Start-Docker-Menu {
    Show-Banner
    Write-Host "Iniciando contenedor en modo INTERACTIVO..." -ForegroundColor Yellow
    Write-Host "Tendrás acceso a menú para ejecutar tests" -ForegroundColor Gray
    Write-Host ""
    
    docker run -it --rm `
        -v "$targetMount" `
        -v "$logsMount" `
        $imageTag bash test-menu.sh
}

function Start-Docker-AllTests {
    Show-Banner
    Write-Host "🚀 Ejecutando TODOS los tests..." -ForegroundColor Green
    
    docker run --rm `
        -v "$targetMount" `
        -v "$logsMount" `
        $imageTag ./gradlew test -DmaxParallelForks=8
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Tests completados exitosamente" -ForegroundColor Green
        Show-Report-Location
    } else {
        Write-Host "❌ Tests fallaron" -ForegroundColor Red
    }
}

function Start-Docker-SpecificTest {
    param([int]$Runner)
    
    if ($Runner -lt 1 -or $Runner -gt 50) {
        Write-Host "❌ Número de test inválido (1-50)" -ForegroundColor Red
        return
    }
    
    $runnerPadded = "{0:D2}" -f $Runner
    Show-Banner
    Write-Host "🚀 Ejecutando CasesRunner$runnerPadded..." -ForegroundColor Green
    
    docker run --rm `
        -v "$targetMount" `
        -v "$logsMount" `
        $imageTag ./gradlew test --tests "*CasesRunner$runnerPadded*"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Test completado exitosamente" -ForegroundColor Green
    } else {
        Write-Host "❌ Test falló" -ForegroundColor Red
    }
}

function Start-Docker-TestRange {
    param([int]$Start, [int]$End)
    
    if ($Start -lt 1 -or $End -gt 50 -or $Start -gt $End) {
        Write-Host "❌ Rango inválido" -ForegroundColor Red
        return
    }
    
    Show-Banner
    Write-Host "🚀 Ejecutando tests del $Start al $End..." -ForegroundColor Green
    
    $filters = @()
    for ($i = $Start; $i -le $End; $i++) {
        $padded = "{0:D2}" -f $i
        $filters += "--tests '*CasesRunner$padded*'"
    }
    
    $filterString = $filters -join " "
    docker run --rm `
        -v "$targetMount" `
        -v "$logsMount" `
        $imageTag bash -c "./gradlew test $filterString"
}

function Show-Report-Location {
    Write-Host ""
    Write-Host "📊 Reporte disponible en:" -ForegroundColor Cyan
    Write-Host "   $($workspaceRoot)\target\site\serenity\index.html" -ForegroundColor Yellow
    Write-Host ""
}

function Check-Image-Exists {
    $imageCheck = docker images --quiet $imageTag 2>$null
    if (-not $imageCheck) {
        Write-Host "❌ Imagen $imageTag no encontrada" -ForegroundColor Red
        Write-Host "Construyendo imagen..." -ForegroundColor Yellow
        docker build --no-cache -t $imageTag .
    }
}

# Main
try {
    Check-Image-Exists
    
    switch ($TestMode) {
        "menu" {
            Start-Docker-Menu
        }
        "all" {
            Start-Docker-AllTests
        }
        "test" {
            Start-Docker-SpecificTest $TestNumber
        }
        "range" {
            Start-Docker-TestRange $StartRange $EndRange
        }
        default {
            Show-Banner
            Write-Host "Uso:" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "  .\\script\\run_tests.ps1                       # Menú interactivo" -ForegroundColor Gray
            Write-Host "  .\\script\\run_tests.ps1 -TestMode all        # Todos los tests" -ForegroundColor Gray
            Write-Host "  .\\script\\run_tests.ps1 -TestMode test -TestNumber 15   # Test específico" -ForegroundColor Gray
            Write-Host "  .\\script\\run_tests.ps1 -TestMode range -StartRange 10 -EndRange 20  # Rango" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}



