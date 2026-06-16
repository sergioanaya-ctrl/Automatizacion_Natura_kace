# Script para actualizar todos los runners (03-50) con RunnerContext

$runnersPath = "E:\Proyectos\Reto_Siigo\Siigo_Front\Sara3\src\test\java\com\sara\automation\runners"

for ($i = 3; $i -le 50; $i++) {
    $num = "{0:D2}" -f $i
    $file = "$runnersPath\CasesRunner$num.java"
    
    if (Test-Path $file) {
        $content = @"
package com.sara.automation.runners;

import com.sara.automation.utils.RunnerContext;
import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/cases/open_cases.feature",
        glue = "com.sara.automation.stepdefinitions",
        tags = "@batch$i",
        snippets = CucumberOptions.SnippetType.UNDERSCORE
)
public class CasesRunner$num {
    // Establecer el numero del runner cuando se carga la clase
    static {
        System.out.println("====== CARGANDO CASESRUNNER$num ======");
        RunnerContext.setRunnerNumber($i);
        System.out.println("====== RUNNERCONTEXT ESTABLECIDO: #$i ======");
    }
}
"@
        
        $enc = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($file, $content, $enc)
        Write-Host "Actualizado: CasesRunner$num a usuario #$i"
    }
}

Write-Host "Completado: Todos los runners actualizados"

