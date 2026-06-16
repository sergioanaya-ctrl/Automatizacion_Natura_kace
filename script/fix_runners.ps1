# Script para reemplazar RunnerContext con System.setProperty en todos los CasesRunners

$runnerDir = "src\test\java\com\sara\automation\runners"

for ($i = 4; $i -le 50; $i++) {
    $paddedNum = "{0:D2}" -f $i
    $file = "$runnerDir\CasesRunner$paddedNum.java"
    
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        
        # Reemplazar RunnerContext.setRunnerNumber con System.setProperty
        $content = $content -replace `
            "RunnerContext\.setRunnerNumber\($i\);", `
            "System.setProperty(`"runnerNumber`", `"$i`");"
        
        $content = $content -replace `
            "System\.out\.println\(`"====== RUNNERCONTEXT ESTABLECIDO: #$i ======`"\);", `
            "System.out.println(`"====== SYSTEM PROPERTY ESTABLECIDO: runnerNumber=$i ======`");"
        
        # Remover import RunnerContext si existe
        $content = $content -replace "import com\.sara\.automation\.utils\.RunnerContext;`r?`n", ""
        
        Set-Content $file $content -Encoding UTF8
        Write-Host "✓ CasesRunner$paddedNum actualizado"
    }
}

Write-Host "Completado"
