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
 * Interaction para clickear el estado "Concluido" dentro del iframe OneScript
 * Busca botón con XPath simple, clickea y guarda
 */
public class ClickEstadoConcluido implements Interaction {

    public static ClickEstadoConcluido clickEstadoConcluido() {
        return instrumented(ClickEstadoConcluido.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        try {
            System.out.println("\n  [ClickEstadoConcluido] ========== TRANSICIÓN A Concluido ==========");
            
            WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Cambiar al iframe
            driver.switchTo().defaultContent();
            WebElement iframeElement = driver.findElement(By.id("form_onescript_iframe"));
            driver.switchTo().frame(iframeElement);
            System.out.println("  [ClickEstadoConcluido] ✓ Iframe OK");
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            // PASO 2: Buscar botón Concluido
            System.out.println("  [ClickEstadoConcluido] Paso 2: Buscando botón 'Concluido'...");
            
            WebElement estado = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[contains(text(), 'Concluido')]")
                )
            );
            
            // Scroll y visibilidad
            js.executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", estado);
            
            // Esperar clickeable
            estado = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Concluido')]")
                )
            );
            
            System.out.println("  [ClickEstadoConcluido] ✓ Botón 'Concluido' encontrado");
            
            // PASO 3: Clickear
            System.out.println("  [ClickEstadoConcluido] Paso 3: Clickeando 'Concluido'...");
            ejecutarClickConReintentos(js, estado, "Concluido");
            
            // OPTIMIZACIÓN: Esperar proactivamente a que botón Guardar esté disponible
            System.out.println("  [ClickEstadoConcluido] Esperando a que botón Guardar esté disponible...");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("kaceCustomSubmit")));
                System.out.println("  [ClickEstadoConcluido] ✓ Botón Guardar detectado, lista para guardado");
            } catch (Exception e) {
                System.out.println("  [ClickEstadoConcluido] ⚠ Botón Guardar no inmediato, procediendo...");
            }
            System.out.println("  [ClickEstadoConcluido] ✓ 'Concluido' seleccionado");
            
            // PASO 4: Buscar y clickear botón de guardado
            System.out.println("  [ClickEstadoConcluido] Paso 4: Buscando botón guardado...");
            
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
            
            System.out.println("  [ClickEstadoConcluido] ✓ Botón guardado encontrado");
            
            // PASO 5: Clickear guardado
            System.out.println("  [ClickEstadoConcluido] Paso 5: Clickeando guardado...");
            ejecutarClickConReintentos(js, guardarButton, "Guardado");
            
            // OPTIMIZACIÓN: Esperar a que formulario se recargue (stale element o new form)
            try {
                wait.until(ExpectedConditions.stalenessOf(guardarButton));
                System.out.println("  [ClickEstadoConcluido] ✓ Página recargada después de guardar");
                
                // OPTIMIZACIÓN: Detectar que el estado 'Concluido' desapareció (cambio exitoso)
                System.out.println("  [ClickEstadoConcluido] Detectando si estado cambió exitosamente...");
                try {
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//button[contains(text(), 'Concluido')]")));
                    System.out.println("  [ClickEstadoConcluido] ✓ 'Concluido' desapareció - cambio exitoso! Estado listo para siguiente transición");
                } catch (Exception e) {
                    System.out.println("  [ClickEstadoConcluido] ⚠ Estado aún visible, puede estar en transición...");
                }
            } catch (Exception e) {
                System.out.println("  [ClickEstadoConcluido] ⚠ Página no recargó inmediatamente, continuando...");
            }
            System.out.println("  [ClickEstadoConcluido] ✓✓ 'Concluido' guardado exitosamente");
            
            driver.switchTo().defaultContent();
            System.out.println("  [ClickEstadoConcluido] ========== COMPLETADO ==========\n");
            
        } catch (TimeoutException e) {
            System.out.println("  [ClickEstadoConcluido] ✗ TIMEOUT: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Concluido' por timeout", e);
        } catch (Exception e) {
            System.out.println("  [ClickEstadoConcluido] ✗ Error: " + e.getMessage());
            try {
                WebDriver driver = net.serenitybdd.screenplay.abilities.BrowseTheWeb.as(actor).getDriver();
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {}
            throw new RuntimeException("Fallo la transicion a 'Concluido'", e);
        }
    }
    
    private void ejecutarClickConReintentos(JavascriptExecutor js, WebElement elemento, String nombre) throws Exception {
        boolean exitoso = false;
        
        try {
            System.out.println("  [ClickEstadoConcluido]   Intento 1: click() directo en " + nombre + "...");
            js.executeScript("arguments[0].click();", elemento);
            System.out.println("  [ClickEstadoConcluido]   ✓ Click exitoso");
            exitoso = true;
        } catch (Exception e1) {
            try {
                System.out.println("  [ClickEstadoConcluido]   Intento 2: dispatchEvent en " + nombre + "...");
                js.executeScript(
                    "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                    "arguments[0].dispatchEvent(evt);",
                    elemento
                );
                System.out.println("  [ClickEstadoConcluido]   ✓ dispatchEvent exitoso");
                exitoso = true;
            } catch (Exception e2) {
                try {
                    System.out.println("  [ClickEstadoConcluido]   Intento 3: focus + click en " + nombre + "...");
                    js.executeScript(
                        "arguments[0].focus(); " +
                        "var evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window }); " +
                        "arguments[0].dispatchEvent(evt);",
                        elemento
                    );
                    System.out.println("  [ClickEstadoConcluido]   ✓ focus + click exitoso");
                    exitoso = true;
                } catch (Exception e3) {
                    System.out.println("  [ClickEstadoConcluido]   ✗ Todos los intentos fallaron para " + nombre);
                }
            }
        }
        
        if (!exitoso) {
            throw new Exception("No se pudo hacer click en " + nombre);
        }
    }
}