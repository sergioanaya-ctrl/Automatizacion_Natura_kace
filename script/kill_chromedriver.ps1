# kill_chromedriver.ps1
# Mata todos los procesos de chromedriver en Windows.

param(
    [switch]$Force
)

$processes = Get-Process -Name chromedriver -ErrorAction SilentlyContinue

if (-not $processes) {
    Write-Host "No se encontraron procesos chromedriver." -ForegroundColor Green
    exit 0
}

foreach ($proc in $processes) {
    try {
        if ($Force) {
            Stop-Process -Id $proc.Id -Force -ErrorAction Stop
        } else {
            Stop-Process -Id $proc.Id -ErrorAction Stop
        }
        Write-Host "[OK] chromedriver PID=$($proc.Id) terminado." -ForegroundColor Green
    } catch {
        Write-Warning "No se pudo terminar chromedriver PID=$($proc.Id): $($_.Exception.Message)"
    }
}
