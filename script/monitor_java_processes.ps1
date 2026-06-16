# ========================================================
# Monitor de procesos Java para detectar tests colgados
# ========================================================
# Este script monitorea procesos Java durante ejecucion paralela
# y mata procesos que se quedan colgados por mas de 10 minutos

param(
    [int]$CheckIntervalSeconds = 30,
    [int]$MaxIdleTimeMinutes = 10
)

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "MONITOR DE PROCESOS - DETECCION DE CUELGUES" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Intervalo de verificacion: $CheckIntervalSeconds segundos" -ForegroundColor Yellow
Write-Host "Tiempo maximo inactivo: $MaxIdleTimeMinutes minutos" -ForegroundColor Yellow
Write-Host ""

$processSnapshot = @{}
$processIdleTime = @{}

# Loop de monitoreo
while ($true) {
    # Obtener procesos Java actuales
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    
    if ($javaProcesses) {
        $currentTime = Get-Date
        
        # Verificar cada proceso
        foreach ($proc in $javaProcesses) {
            $pid = $proc.Id
            
            # Si es un proceso nuevo, registrarlo
            if (-not $processSnapshot.ContainsKey($pid)) {
                $processSnapshot[$pid] = $proc
                $processIdleTime[$pid] = $currentTime
                Write-Host "[+] Nuevo proceso detectado: PID=$pid, CPU=$($proc.CPU)%, Memoria=$($proc.WorkingSet/1MB)MB" -ForegroundColor Green
            } else {
                # Verificar si el proceso cambio su consumo de CPU
                $prevProc = $processSnapshot[$pid]
                $cpuChange = [Math]::Abs($proc.CPU - $prevProc.CPU)
                $memChange = [Math]::Abs($proc.WorkingSet - $prevProc.WorkingSet)
                
                if ($cpuChange -gt 5 -or $memChange -gt 10MB) {
                    # El proceso esta activo
                    $processIdleTime[$pid] = $currentTime
                    Write-Host "[+] Proceso activo: PID=$pid, CPU=$($proc.CPU)%, Memoria=$($proc.WorkingSet/1MB)MB" -ForegroundColor Green
                } else {
                    # El proceso parece inactivo
                    $idleTime = $currentTime - $processIdleTime[$pid]
                    $idleMinutes = $idleTime.TotalMinutes
                    
                    if ($idleMinutes -gt $MaxIdleTimeMinutes) {
                        Write-Host "[-] PROCESO COLGADO DETECTADO: PID=$pid, Tiempo inactivo=$([math]::Round($idleMinutes,1))min" -ForegroundColor Red
                        Write-Host "    Matando proceso..." -ForegroundColor Red
                        
                        try {
                            Stop-Process -Id $pid -Force -ErrorAction Stop
                            Write-Host "    [OK] Proceso matado" -ForegroundColor Green
                        } catch {
                            Write-Host "    [ERROR] No se pudo matar el proceso" -ForegroundColor Red
                        }
                    } else {
                        Write-Host "[~] Proceso inactivo: PID=$pid, Tiempo=$([math]::Round($idleMinutes,1))min (limite: $MaxIdleTimeMinutes min)" -ForegroundColor Yellow
                    }
                }
                
                $processSnapshot[$pid] = $proc
            }
        }
        
        # Limpiar procesos que ya no existen
        $deadPids = @()
        $processSnapshot.Keys | ForEach-Object {
            if (-not ($javaProcesses | Where-Object { $_.Id -eq $_ })) {
                $deadPids += $_
            }
        }
        
        $deadPids | ForEach-Object {
            Write-Host "[X] Proceso finalizado: PID=$_" -ForegroundColor Gray
            $processSnapshot.Remove($_)
            $processIdleTime.Remove($_)
        }
    } else {
        Write-Host "[INFO] No hay procesos Java en ejecucion" -ForegroundColor Gray
    }
    
    Write-Host ""
    
    # Esperar al siguiente intervalo
    Start-Sleep -Seconds $CheckIntervalSeconds
}
