# ========================================================
# Setup Script - Configuración Automática del Proyecto
# ========================================================
# Este script configura automáticamente el proyecto para ejecutar
# en cualquier máquina con todas las dependencias necesarias
# ========================================================

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "SETUP - Configuración Automática de Automatización SARA" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar Java
Write-Host "1. Verificando Java..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Java está instalado:" -ForegroundColor Green
    Write-Host $javaVersion[0]
} else {
    Write-Host "✗ Java NO está instalado. Por favor instale Java 11 o superior" -ForegroundColor Red
    Write-Host "  Descargue desde: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# 2. Descargar dependencias con Gradle
Write-Host "2. Descargando dependencias (esto puede tomar unos minutos)..." -ForegroundColor Yellow
Write-Host ""

if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat build -x test --refresh-dependencies 2>&1 | Select-String "BUILD SUCCESS|BUILD FAILED|Downloading|Downloaded" | Select-Object -First 30
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✓ Dependencias descargadas exitosamente" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "⚠ Hubo un error descargando dependencias, pero el proyecto puede funcionar" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ No se encuentra gradlew.bat" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 3. Compilar proyecto
Write-Host "3. Compilando proyecto..." -ForegroundColor Yellow
.\gradlew.bat compileTestJava 2>&1 | Select-String "BUILD SUCCESS|BUILD FAILED" | Select-Object -First 1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Proyecto compilado exitosamente" -ForegroundColor Green
} else {
    Write-Host "✗ Error compilando proyecto" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "✓ SETUP COMPLETADO EXITOSAMENTE" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "PRÓXIMOS PASOS:" -ForegroundColor Yellow
Write-Host "1. Configurar número de runners paralelos:"
Write-Host "   - Edita: gradle.properties"
Write-Host "   - Cambia: maxParallelForks=2 (cambia el número según tus necesidades)"
Write-Host ""
Write-Host "2. Ejecutar pruebas con 2 runners:"
Write-Host "   .\gradlew.bat test"
Write-Host ""
Write-Host "3. Ejecutar prueba individual:"
Write-Host "   .\gradlew.bat test --tests 'com.natura.automation.runners.CasesRunner01'"
Write-Host ""
Write-Host "4. Ver reporte de resultados:"
Write-Host "   target\site\serenity\index.html"
Write-Host ""
