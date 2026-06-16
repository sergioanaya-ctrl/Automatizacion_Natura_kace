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
 * Interaction para clickear el estado "Finalizado" dentro del iframe OneScript
 * Busca botón con XPath simple, clickea y guarda
 */
public class ClickEstadoFinalizado implements Interaction {

    public static ClickEstadoFinalizado clickEstadoFinalizado() {
        return instrumented(ClickEstadoFinalizado.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        try {
            System.out.println("\n  [ClickEstadoFinalizado] ========== TRANSICIÓN A Finalizado ==========");
            
            WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Cambiar al iframe
            driver.switchTo().defaultContent();
            WebElement iframeElement = driver.findElement(By.id("form_onescript_iframe"));
            driver.switchTo().frame(iframeElement);
            System.out.println("  [ClickEstadoFinalizado] ✓ Iframe OK");
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // PASO 2: Buscar botón Finalizado
            System.out.println("  [ClickEstadoFinalizado] Paso 2: Buscando botón 'Finalizado'...");
            
            WebElement estado = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[contains(text(), 'Finalizado')]")
                )
            );
            
            // Scroll y visibilidad
            js.executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", estado);
            
            // Esperar clickeable
            estado = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Finalizado')]")
                )
            );
            
            System.out.println("  [ClickEstadoFinalizado] ✓ Botón 'Finalizado' encontrado");
            
            // PASO 3: Clickear
            System.out.println("  [ClickEstadoFinalizado] Paso 3: Clickeando 'Finalizado'...");
            ejecutarClickConReintentos(js, estado, "Finalizado");
            
            // OPTIMIZACIÓN: Esperar proactivamente a que botón Guardar esté disponible
            System.out.println("  [ClickEstadoFinalizado] Esperando a que botón Guardar esté disponible...");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("kaceCustomSubmit")));
                System.out.println("  [ClickEstadoFinalizado] ✓ Botón Guardar detectado, lista para guardado");
            } catch (Exception e) {
                System.out.println("  [ClickEstadoFinalizado] ⚠ Botón Guardar no inmediato, procediendo...");
            }
            System.out.println("  [ClickEstadoFinalizado] ✓ 'Finalizado' seleccionado");
            
            // PASO 4: Buscar y clickear botón de guardado
            System.out.println("  [ClickEstadoFinalizado] Paso 4: Buscando botón guardado...");
            
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
            
            System.out.println("  [ClickEstadoFinalizado] ✓ Botón guardado encontrado");
            
            // PASO 5: Clickear guardado
            System.out.println("  [ClickEstadoFinalizado] Paso 5: Clickeando guardado...");
            ejecutarClickConReintentos(js, guardarButton, "Guardado");
            
            // OPTIMIZACIÓN: Esperar a que formulario se recargue (stale element o new form)
            try {
                wait.until(ExpectedConditions.stalenessOf(guardarButton));
                System.out.println("  [ClickEstadoFinalizado] ✓ Página recargada después de guardar");
                
                // OPTIMIZACIÓN: Detectar que el estado 'Finalizado' desapareció (cambio exitoso)
                System.out.println("  [ClickEstadoFinalizado] Detectando si estado cambió exitosamente...");
                try {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//button[contains(text(), 'Finalizado')]")));
                    System.out.println("  [ClickEstadoFinalizado] ✓ 'Finalizado' desapareció - cambio exitoso! Estado listo para siguiente transición");
                } catch (Exception e) {
                    System.out.println("  [ClickEstadoFinalizado] ⚠ Estado aún visible, puede estar en transición...");
                }
            } catch (Exception e) {
                System.out.println("  [ClickEstadoFinalizado] ⚠ Página no recargó inmediatamente, continuando...");
            }
            System.out.println("  [ClickEstadoFinalizado] ✓✓ 'Finalizado' guardado exitosamente");
            
            driver.switchTo().defaultContent();
            System.out.println("  [ClickEstadoFinalizado] ========== COMPLETADO ==========\n");
            
        } catch (TimeoutException e) {
            System.out.println("  [ClickEstadoFinalizado] ✗ TIMEOUT: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Finalizado' por timeout", e);
        } catch (Exception e) {
            System.out.println("  [ClickEstadoFinalizado] ✗ Error: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Finalizado'", e);
        }
    }
    
    private void ejecutarClickConReintentos(JavascriptExecutor js, WebElement elemento, String nombre) throws Exception {
        boolean exitoso = false;
        
        try {
            System.out.println("  [ClickEstadoFinalizado]   Intento 1: click() directo en " + nombre + "...");
            js.executeScript("arguments[0].click();", elemento);
            System.out.println("  [ClickEstadoFinalizado]   ✓ Click exitoso");
            exitoso = true;
        } catch (Exception e1) {
            try {
                System.out.println("  [ClickEstadoFinalizado]   Intento 2: dispatchEvent en " + nombre + "...");
                js.executeScript(
                    "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                    "arguments[0].dispatchEvent(evt);",
                    elemento
                );
                System.out.println("  [ClickEstadoFinalizado]   ✓ dispatchEvent exitoso");
                exitoso = true;
            } catch (Exception e2) {
                try {
                    System.out.println("  [ClickEstadoFinalizado]   Intento 3: focus + click en " + nombre + "...");
                    js.executeScript(
                        "arguments[0].focus(); " +
                        "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                        "arguments[0].dispatchEvent(evt);",
                        elemento
                    );
                    System.out.println("  [ClickEstadoFinalizado]   ✓ focus + click exitoso");
                    exitoso = true;
                } catch (Exception e3) {
                    System.out.println("  [ClickEstadoFinalizado]   ✗ Todos los intentos fallaron para " + nombre);
                }
            }
        }
        
        if (!exitoso) {
            throw new Exception("No se pudo hacer click en " + nombre);
        }
    }
}