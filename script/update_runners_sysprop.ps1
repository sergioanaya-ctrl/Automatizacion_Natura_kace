# Script para actualizar CasesRunner03-50 con System.setProperty en lugar de RunnerContext

$runnerDir = ".\src\test\java\com\sara\automation\runners\"

for ($i = 3; $i -le 50; $i++) {
    $num = $i
    $paddedNum = "{0:D2}" -f $num
    $file = "$runnerDir\CasesRunner$paddedNum.java"
    
    if (Test-Path $file) {
        # Reemplazar import RunnerContext y el static block
        $oldContent = Get-Content $file -Raw
        
        # Remover import RunnerContext si existe
        $newContent = $oldContent -replace "import com\.sara\.automation\.utils\.RunnerContext;", ""
        
        # Reemplazar el static block con System.setProperty
        $staticBlock = @"
    static {
        System.out.println("====== CARGANDO CASESRUNNER$paddedNum ======");
        System.setProperty("runnerNumber", "$num");
        System.out.println("====== SYSTEM PROPERTY ESTABLECIDO: runnerNumber=$num ======");
    }
"@
        
        # Buscar y reemplazar el static block completo
        $newContent = $newContent -replace "static \{[\s\S]*?System\.out\.println\(.*?RUNNERCONTEXT ESTABLECIDO.*?\);\s*\}", $staticBlock
        
        # Si el anterior regex no funcionó, intentar con pattern más flexible
        if ($newContent -eq $oldContent) {
            $newContent = $newContent -replace "static \{[\s\S]*?\}", $staticBlock
        }
        
        Set-Content $file $newContent -Encoding UTF8
        Write-Host "✓ Actualizado: CasesRunner$paddedNum → runnerNumber=$num"
    } else {
        Write-Host "✗ NO ENCONTRADO: $file"
    }
}

Write-Host ""
Write-Host "✓✓✓ Completado: Todos los runners (03-50) actualizados con System.setProperty"
