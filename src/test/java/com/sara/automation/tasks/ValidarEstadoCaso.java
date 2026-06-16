package com.sara.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Task para validar que el caso quede en el estado esperado.
 * 
 * Busca el botón del estado dentro del iframe OneScript (la misma estrategia que funciona
 * para todas las transiciones de estado). Si el botón es visible, significa que el estado
 * anterior fue completado exitosamente.
 */
public class ValidarEstadoCaso implements Task {

    private final String estadoEsperado;

    public ValidarEstadoCaso(String estadoEsperado) {
        this.estadoEsperado = estadoEsperado;
    }

    public static Performable conEstado(String estado) {
        return instrumented(ValidarEstadoCaso.class, estado);
    }

    @Override
    @Step("Validar que el caso quede en estado '{0}'")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        
        System.out.println("\n  [ValidarEstadoCaso] ==================== VALIDACIÓN FINAL DEL ESTADO ====================");
        System.out.println("  [ValidarEstadoCaso] Verificando que estado '" + estadoEsperado + "' está disponible en iframe...");
        
        try {
            driver.switchTo().defaultContent();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            
            // Entrar al iframe (mismo patrón que usan los demás estados)
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("form_onescript_iframe")));
            
            WebElement botón = null;
            String botónEncontrado = null;
            
            // ESTRATEGIA 1: Buscar literal el botón con el nombre del estado (todos tienen la misma estructura)
            String xpathLiteral = String.format("//button[contains(text(), '%s')]", estadoEsperado);
            
            try {
                WebDriverWait waitRapida = new WebDriverWait(driver, Duration.ofSeconds(3));
                botón = waitRapida.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathLiteral)));
                botónEncontrado = estadoEsperado;
                System.out.println("  [ValidarEstadoCaso] ✓ Encontrado botón literal: '" + estadoEsperado + "'");
            } catch (Exception e1) {
                System.out.println("  [ValidarEstadoCaso] - Botón literal no encontrado, intentando con mapeo...");
                
                // ESTRATEGIA 2: Usar mapeo (Por Programar para Abierto, etc.)
                String botónMapeado = mapearEstadoABotón(estadoEsperado);
                String xpathMapeado = String.format("//button[contains(text(), '%s')]", botónMapeado);
                
                try {
                    WebDriverWait waitRapida = new WebDriverWait(driver, Duration.ofSeconds(2));
                    botón = waitRapida.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathMapeado)));
                    botónEncontrado = botónMapeado;
                    System.out.println("  [ValidarEstadoCaso] ✓ Encontrado botón mapeado: '" + botónMapeado + "'");
                } catch (Exception e2) {
                    System.out.println("  [ValidarEstadoCaso] - Botón mapeado tampoco encontrado");
                    throw e2;
                }
            }
            
            // Si el botón es visible, el estado anterior fue completado correctamente
            System.out.println("  [ValidarEstadoCaso] ✓ Botón '" + botónEncontrado + "' encontrado y visible");
            System.out.println("  [ValidarEstadoCaso] ✓✓✓ VALIDACIÓN EXITOSA: Caso alcanzó el estado esperado");
            System.out.println("  [ValidarEstadoCaso] ==================== VALIDACIÓN COMPLETADA ====================\n");
            
            driver.switchTo().defaultContent();
            
        } catch (Exception e) {
            try {
                driver.switchTo().defaultContent();
            } catch (Exception ignored) {
            }
            
            System.out.println("  [ValidarEstadoCaso] ✗ VALIDACIÓN FALLIDA: " + e.getMessage());
            throw new AssertionError("Error al validar estado '" + estadoEsperado + "': " + e.getMessage(), e);
        }
    }
    
    /**
     * Mapea el estado del caso al nombre del botón en el iframe si el literal no existe.
     * Por defecto intenta usar el estado literal primero.
     */
    private String mapearEstadoABotón(String estado) {
        String estadoNormalizado = estado.toLowerCase();
        switch (estadoNormalizado) {
            case "abierto":
                return "Abierto";          // Estado final después de Finalizado
            case "programado":
                return "Programado";
            case "aceptado":
                return "Aceptado y en desplazamiento";
            case "concluido":
                return "Concluido";
            case "finalizado":
                return "Finalizado";
            default:
                return estado; // Si no mapea, usa el estado tal cual
        }
    }
}
