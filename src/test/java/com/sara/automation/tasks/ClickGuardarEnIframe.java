package com.sara.automation.tasks;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class ClickGuardarEnIframe implements Task {
    
    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = Serenity.getDriver();
        
        try {
            // Intentar cambiar al iframe
            driver.switchTo().frame("form_onescript_iframe");
            System.out.println("[ClickGuardarEnIframe] Cambiado al iframe exitosamente");
        } catch (Exception e) {
            System.out.println("[ClickGuardarEnIframe] No se pudo cambiar al iframe: " + e.getMessage());
            driver.switchTo().defaultContent();
        }
        
        // JavaScript para hacer click al botón por ID
        String jsScript = "var btn = document.getElementById('kaceCustomSubmit'); " +
                          "if (btn) { " +
                          "  btn.scrollIntoView(true); " +
                          "  btn.click(); " +
                          "  console.log('Botón kaceCustomSubmit clickeado con éxito'); " +
                          "  return 'SUCCESS'; " +
                          "} else { " +
                          "  console.error('Botón kaceCustomSubmit no encontrado'); " +
                          "  return 'NOT_FOUND'; " +
                          "}";
        
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(jsScript);
            System.out.println("[ClickGuardarEnIframe] Resultado del click: " + result);
            
            if ("NOT_FOUND".equals(result)) {
                System.out.println("[ClickGuardarEnIframe] ERROR: Botón no encontrado en el DOM");
                throw new AssertionError("Botón kaceCustomSubmit no encontrado en el iframe");
            }
        } catch (Exception e) {
            System.out.println("[ClickGuardarEnIframe] Error ejecutando JavaScript: " + e.getMessage());
            throw new AssertionError("No se pudo hacer click al botón: " + e.getMessage());
        } finally {
            // Volver al contenido principal
            try {
                driver.switchTo().defaultContent();
                System.out.println("[ClickGuardarEnIframe] Vuelto al contenido principal");
            } catch (Exception e) {
                System.out.println("[ClickGuardarEnIframe] Error al volver al contenido principal: " + e.getMessage());
            }
        }
    }
    
    public static ClickGuardarEnIframe clickGuardarEnIframe() {
        return instrumented(ClickGuardarEnIframe.class);
    }
}
