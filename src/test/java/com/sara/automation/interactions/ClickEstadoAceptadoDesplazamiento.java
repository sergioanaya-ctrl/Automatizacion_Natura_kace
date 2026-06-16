package com.sara.automation.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Interaction para clickear el estado "Aceptado" dentro del iframe OneScript
 * Busca botón con XPath simple, clickea y guarda
 */
public class ClickEstadoAceptadoDesplazamiento implements Interaction {

    public static ClickEstadoAceptadoDesplazamiento clickEstadoAceptadoDesplazamiento() {
        return instrumented(ClickEstadoAceptadoDesplazamiento.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        try {
            System.out.println("\n  [ClickEstadoAceptadoDesplazamiento] ========== TRANSICIÓN A Aceptado ==========");
            
            WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Cambiar al iframe
            driver.switchTo().defaultContent();
            WebElement iframeElement = driver.findElement(By.id("form_onescript_iframe"));
            driver.switchTo().frame(iframeElement);
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓ Iframe OK");
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // PASO 2: Buscar botón Aceptado (puede ser completo o simple)
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] Paso 2: Buscando botón 'Aceptado'...");
            
            // Intento 1: Buscar "Aceptado y en desplazamiento" exactamente
            WebElement estadoAceptado = null;
            try {
                System.out.println("  [ClickEstadoAceptadoDesplazamiento]   Sub-intento 1: Buscando 'Aceptado y en desplazamiento'...");
                estadoAceptado = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//button[contains(text(), 'Aceptado') and contains(text(), 'desplazamiento')]")
                    )
                );
                System.out.println("  [ClickEstadoAceptadoDesplazamiento]   ✓ 'Aceptado y en desplazamiento' encontrado");
            } catch (TimeoutException e1) {
                // Intento 2: Buscar solo "Aceptado"
                System.out.println("  [ClickEstadoAceptadoDesplazamiento]   Sub-intento 1 falló, buscando 'Aceptado' simple...");
                estadoAceptado = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//button[text()='Aceptado' or normalize-space(text())='Aceptado']")
                    )
                );
                System.out.println("  [ClickEstadoAceptadoDesplazamiento]   ✓ 'Aceptado' simple encontrado");
            }
            
            // Scroll y visibilidad
            js.executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", estadoAceptado);
            
            // Esperar clickeable - reutilizar el elemento ya encontrado
            wait.until(ExpectedConditions.elementToBeClickable(estadoAceptado));
            
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓ Botón 'Aceptado' encontrado");
            
            // PASO 3: Clickear
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] Paso 3: Clickeando 'Aceptado'...");
            ejecutarClickConReintentos(js, estadoAceptado, "Aceptado");
            
            // OPTIMIZACIÓN: Esperar proactivamente a que botón Guardar esté disponible
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] Esperando a que botón Guardar esté disponible...");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("kaceCustomSubmit")));
                System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓ Botón Guardar detectado, lista para guardado");
            } catch (Exception e) {
                System.out.println("  [ClickEstadoAceptadoDesplazamiento] ⚠ Botón Guardar no inmediato, procediendo...");
            }
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓ 'Aceptado' seleccionado");
            
            // PASO 4: Buscar y clickear botón de guardado
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] Paso 4: Buscando botón guardado...");
            
            WebElement guardarButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.id("kaceCustomSubmit")
                )
            );
            
            js.executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", guardarButton);
            
            guardarButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.id("kaceCustomSubmit")
                )
            );
            
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓ Botón guardado encontrado");
            
            // PASO 5: Clickear guardado
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] Paso 5: Clickeando guardado...");
            ejecutarClickConReintentos(js, guardarButton, "Guardado");
            
            // OPTIMIZACIÓN: Esperar a que formulario se recargue (stale element o new form)
            try {
                wait.until(ExpectedConditions.stalenessOf(guardarButton));
                System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓ Página recargada después de guardar");
                
                // OPTIMIZACIÓN: Detectar que el estado 'Aceptado' desapareció (cambio exitoso)
                System.out.println("  [ClickEstadoAceptadoDesplazamiento] Detectando si estado cambió exitosamente...");
                try {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//button[contains(text(), 'Aceptado')]")));
                    System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓ 'Aceptado' desapareció - cambio exitoso! Estado listo para siguiente transición");
                } catch (Exception e) {
                    System.out.println("  [ClickEstadoAceptadoDesplazamiento] ⚠ Estado aún visible, puede estar en transición...");
                }
            } catch (Exception e) {
                System.out.println("  [ClickEstadoAceptadoDesplazamiento] ⚠ Página no recargó inmediatamente, continuando...");
            }
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✓✓ 'Aceptado' guardado exitosamente");
            
            driver.switchTo().defaultContent();
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ========== COMPLETADO ==========\n");
            
        } catch (TimeoutException e) {
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✗ TIMEOUT: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Aceptado' por timeout", e);
        } catch (Exception e) {
            System.out.println("  [ClickEstadoAceptadoDesplazamiento] ✗ Error: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Aceptado'", e);
        }
    }
    
    private void ejecutarClickConReintentos(JavascriptExecutor js, WebElement elemento, String nombre) throws Exception {
        boolean exitoso = false;
        
        try {
            System.out.println("  [ClickEstadoAceptadoDesplazamiento]   Intento 1: click() directo en " + nombre + "...");
            js.executeScript("arguments[0].click();", elemento);
            System.out.println("  [ClickEstadoAceptadoDesplazamiento]   ✓ Click exitoso");
            exitoso = true;
        } catch (Exception e1) {
            try {
                System.out.println("  [ClickEstadoAceptadoDesplazamiento]   Intento 2: dispatchEvent en " + nombre + "...");
                js.executeScript(
                    "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                    "arguments[0].dispatchEvent(evt);",
                    elemento
                );
                System.out.println("  [ClickEstadoAceptadoDesplazamiento]   ✓ dispatchEvent exitoso");
                exitoso = true;
            } catch (Exception e2) {
                try {
                    System.out.println("  [ClickEstadoAceptadoDesplazamiento]   Intento 3: focus + click en " + nombre + "...");
                    js.executeScript(
                        "arguments[0].focus(); " +
                        "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                        "arguments[0].dispatchEvent(evt);",
                        elemento
                    );
                    System.out.println("  [ClickEstadoAceptadoDesplazamiento]   ✓ focus + click exitoso");
                    exitoso = true;
                } catch (Exception e3) {
                    System.out.println("  [ClickEstadoAceptadoDesplazamiento]   ✗ Todos los intentos fallaron para " + nombre);
                }
            }
        }
        
        if (!exitoso) {
            throw new Exception("No se pudo hacer click en " + nombre);
        }
    }
}