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
 * Interaction para clickear el estado "Programado" dentro del iframe OneScript
 * Busca botón con XPath simple, clickea y guarda
 */
public class ClickEstadoProgramado implements Interaction {

    public static ClickEstadoProgramado clickEstadoProgramado() {
        return instrumented(ClickEstadoProgramado.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        try {
            System.out.println("\n  [ClickEstadoProgramado] ========== TRANSICIÓN A PROGRAMADO ==========");
            
            WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Cambiar al iframe
            driver.switchTo().defaultContent();
            WebElement iframeElement = driver.findElement(By.id("form_onescript_iframe"));
            driver.switchTo().frame(iframeElement);
            System.out.println("  [ClickEstadoProgramado] ✓ Iframe OK");
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // PASO 2: Buscar botón Programado con XPath simple
            System.out.println("  [ClickEstadoProgramado] Paso 2: Buscando botón 'Programado'...");
            
            WebElement estadoProgramado = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[contains(text(), 'Programado')]")
                )
            );
            
            // Scroll y visibilidad
            js.executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", estadoProgramado);
            
            // Esperar clickeable
            estadoProgramado = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Programado')]")
                )
            );
            
            System.out.println("  [ClickEstadoProgramado] ✓ Botón 'Programado' encontrado");
            
            // PASO 3: Clickear Programado
            System.out.println("  [ClickEstadoProgramado] Paso 3: Clickeando 'Programado'...");
            ejecutarClickConReintentos(js, estadoProgramado, "Programado");
            
            // OPTIMIZACIÓN: Esperar proactivamente a que botón Guardar esté disponible
            System.out.println("  [ClickEstadoProgramado] Esperando a que botón Guardar esté disponible...");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("kaceCustomSubmit")));
                System.out.println("  [ClickEstadoProgramado] ✓ Botón Guardar detectado, lista para guardado");
            } catch (Exception e) {
                System.out.println("  [ClickEstadoProgramado] ⚠ Botón Guardar no inmediato, procediendo...");
            }
            System.out.println("  [ClickEstadoProgramado] ✓ 'Programado' seleccionado");
            
            // PASO 4: Buscar y clickear botón de guardado
            System.out.println("  [ClickEstadoProgramado] Paso 4: Buscando botón guardado...");
            
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
            
            System.out.println("  [ClickEstadoProgramado] ✓ Botón guardado encontrado");
            
            // PASO 5: Clickear guardado
            System.out.println("  [ClickEstadoProgramado] Paso 5: Clickeando guardado...");
            ejecutarClickConReintentos(js, guardarButton, "Guardado");
            
            // OPTIMIZACIÓN: Esperar a que formulario se recargue (stale element o new form)
            try {
                wait.until(ExpectedConditions.stalenessOf(guardarButton));
                System.out.println("  [ClickEstadoProgramado] ✓ Página recargada después de guardar");
                
                // OPTIMIZACIÓN: Detectar que el estado 'Programado' desapareció (cambio exitoso)
                System.out.println("  [ClickEstadoProgramado] Detectando si estado cambió exitosamente...");
                try {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//button[contains(text(), 'Programado')]")));
                    System.out.println("  [ClickEstadoProgramado] ✓ 'Programado' desapareció - cambio exitoso! Estado listo para siguiente transición");
                } catch (Exception e) {
                    System.out.println("  [ClickEstadoProgramado] ⚠ Estado aún visible, puede estar en transición...");
                }
            } catch (Exception e) {
                System.out.println("  [ClickEstadoProgramado] ⚠ Página no recargó inmediatamente, continuando...");
            }
            System.out.println("  [ClickEstadoProgramado] ✓✓ 'Programado' guardado exitosamente");
            
            driver.switchTo().defaultContent();
            System.out.println("  [ClickEstadoProgramado] ========== COMPLETADO ==========\n");
            
        } catch (TimeoutException e) {
            System.out.println("  [ClickEstadoProgramado] ✗ TIMEOUT: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Programado' por timeout", e);
        } catch (Exception e) {
            System.out.println("  [ClickEstadoProgramado] ✗ Error: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Programado'", e);
        }
    }
    
    /**
     * Ejecuta click con múltiples reintentos
     */
    private void ejecutarClickConReintentos(JavascriptExecutor js, WebElement elemento, String nombre) throws Exception {
        boolean exitoso = false;
        
        // Intento 1: click() directo
        try {
            System.out.println("  [ClickEstadoProgramado]   Intento 1: click() directo en " + nombre + "...");
            js.executeScript("arguments[0].click();", elemento);
            System.out.println("  [ClickEstadoProgramado]   ✓ Click exitoso");
            exitoso = true;
        } catch (Exception e1) {
            // Intento 2: dispatchEvent
            try {
                System.out.println("  [ClickEstadoProgramado]   Intento 2: dispatchEvent en " + nombre + "...");
                js.executeScript(
                    "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                    "arguments[0].dispatchEvent(evt);",
                    elemento
                );
                System.out.println("  [ClickEstadoProgramado]   ✓ dispatchEvent exitoso");
                exitoso = true;
            } catch (Exception e2) {
                // Intento 3: focus + click
                try {
                    System.out.println("  [ClickEstadoProgramado]   Intento 3: focus + click en " + nombre + "...");
                    js.executeScript(
                        "arguments[0].focus(); " +
                        "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                        "arguments[0].dispatchEvent(evt);",
                        elemento
                    );
                    System.out.println("  [ClickEstadoProgramado]   ✓ focus + click exitoso");
                    exitoso = true;
                } catch (Exception e3) {
                    System.out.println("  [ClickEstadoProgramado]   ✗ Todos los intentos fallaron para " + nombre);
                }
            }
        }
        
        if (!exitoso) {
            throw new Exception("No se pudo hacer click en " + nombre);
        }
    }
}
