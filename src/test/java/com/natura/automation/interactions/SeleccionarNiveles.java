package com.natura.automation.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Diligencia el componente "Clasificaciones" en cascada (Nivel 3 a 6).
 *
 * Por ahora solo se recorren las ramas CONFIRMADAS de extremo a extremo (hasta Nivel 6):
 *   - Nivel 3: ACOMPANAMIENTO, DESARROLLO Y RECONOCIMIENTO  /  ATRACCION, CADASTRO E INICIO
 *   - Nivel 4: CONSULTA
 *   - Niveles 5 y 6: dinámico (lo que la app muestre para esa rama).
 *
 * Cuando se tenga la matriz completa de todos los niveles, habilitar el resto de ramas
 * (ver NIVEL3_PENDIENTES) y/o ampliar las preferencias por nivel.
 * Niveles 1 y 2 vienen fijos y deshabilitados (COLOMBIA, CEN).
 */
public class SeleccionarNiveles implements Interaction {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final Random RANDOM = new Random();
    private static final int NIVEL_INICIAL = 3;
    private static final int NIVEL_FINAL = 6;

    // Ramas de Nivel 3 confirmadas (cascadean completas hasta Nivel 6).
    private static final List<String> NIVEL3_CONFIRMADOS = Arrays.asList(
            "ACOMPANAMIENTO, DESARROLLO Y RECONOCIMIENTO",
            "ATRACCION, CADASTRO E INICIO");

    // Nivel 4 confirmado para esas ramas.
    private static final List<String> NIVEL4_PREFERIDO = Arrays.asList("CONSULTA");

    // PENDIENTE (habilitar con la matriz completa): el resto de ramas de Nivel 3:
    //   "CAPTACION DE PEDIDOS", "DESPACHO Y ENTREGA DE PEDIDO", "ENTRENAMIENTO Y HERRAMIENTAS",
    //   "IMPRODUCTIVO", "PAGO DE PEDIDO", "RITUALES Y EVENTOS", "SERVICIO POST-VENTA"

    // Opciones (en cualquier nivel) que NO se deben seleccionar porque abren formularios extra
    // obligatorios aún no soportados (Datos NC, Datos Transportadora, etc.). Se comparan
    // case-insensitive contra el texto de la opción. Agregar aquí las que el reporte muestre como fallidas.
    // Ya no se excluyen ramas: el formulario "Datos NC" se llena condicionalmente
    // (ver LlenarFormularioNC). Si algún otro formulario extra bloqueara el guardado,
    // se puede volver a agregar aquí su opción de nivel.
    private static final List<String> OPCIONES_EXCLUIDAS = java.util.Collections.emptyList();

    public static Performable aleatorios() {
        return instrumented(SeleccionarNiveles.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        ensureIframe(driver);

        for (int nivel = NIVEL_INICIAL; nivel <= NIVEL_FINAL; nivel++) {
            if (!esperarNivelSeleccionable(driver, nivel)) {
                System.out.println("[Niveles] Nivel " + nivel + " no aplica para esta rama — fin de la cascada.");
                break;
            }
            List<String> preferidos = preferidosDe(nivel);
            if (!seleccionarEnNivel(driver, nivel, preferidos)) {
                System.err.println("[Niveles] Nivel " + nivel + " sin opciones seleccionables — fin.");
                break;
            }
        }

        System.out.println("[Niveles] Niveles completados.");
    }

    /**
     * Valores preferidos por nivel (vacío = cualquier opción al azar).
     * Actualmente TODOS los niveles eligen al azar (N3 a N6) para variar las ramas y probar
     * la máquina de estados con distintas clasificaciones.
     * Para acotar de nuevo a ramas confirmadas, devolver NIVEL3_CONFIRMADOS / NIVEL4_PREFERIDO.
     */
    private List<String> preferidosDe(int nivel) {
        return Collections.emptyList();
    }

    /** Espera (corto) a que el control del nivel exista y esté habilitado. */
    private boolean esperarNivelSeleccionable(WebDriver driver, int nivel) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.presenceOfElementLocated(controlHabilitado(nivel)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Abre el nivel y elige una opción. Si hay valores preferidos presentes, elige entre ellos;
     * si no, elige una opción cualquiera al azar (sin placeholder). Verifica la selección.
     */
    private boolean seleccionarEnNivel(WebDriver driver, int nivel, List<String> preferidos) {
        try {
            WebElement control = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(controlHabilitado(nivel)));
            scrollToCenter(driver, control);
            jsClick(driver, control);

            By opciones = By.cssSelector(
                    ".classifications-dropdown-list .classifications-dropdown-item:not(.classifications-dropdown-item--placeholder)");
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(opciones));

            List<WebElement> items = driver.findElements(opciones).stream()
                    .filter(WebElement::isDisplayed)
                    .collect(Collectors.toList());
            if (items.isEmpty()) return false;

            // Excluir opciones que abren formularios extra no soportados (lista negra).
            List<WebElement> base = items.stream()
                    .filter(it -> OPCIONES_EXCLUIDAS.stream()
                            .noneMatch(ex -> it.getText().trim().equalsIgnoreCase(ex)))
                    .collect(Collectors.toList());
            if (base.isEmpty()) {
                System.err.println("  [Niveles] Nivel " + nivel + ": todas las opciones están excluidas — se elige igualmente.");
                base = items;
            }

            // Filtrar a los preferidos si hay alguno presente (sobre las opciones no excluidas).
            List<WebElement> candidatos = base;
            if (!preferidos.isEmpty()) {
                List<WebElement> filtrados = base.stream()
                        .filter(it -> preferidos.stream().anyMatch(p -> it.getText().trim().equalsIgnoreCase(p)))
                        .collect(Collectors.toList());
                if (!filtrados.isEmpty()) {
                    candidatos = filtrados;
                } else {
                    System.err.println("  [Niveles] Nivel " + nivel + ": ninguno de los valores preferidos " +
                            preferidos + " está disponible — se elige al azar.");
                }
            }

            WebElement elegido = candidatos.get(RANDOM.nextInt(candidatos.size()));
            String valor = elegido.getText().trim();
            scrollToCenter(driver, elegido);
            jsClick(driver, elegido);

            if (nivelMuestraValor(driver, nivel, valor)) {
                System.out.println("  [Niveles] Nivel " + nivel + " = " + valor + " (OK)");
            } else {
                System.out.println("  [Niveles] Nivel " + nivel + " = " + valor + " (seleccionado; no se pudo confirmar el label)");
            }
            return true;
        } catch (Exception e) {
            System.err.println("  [Niveles] ERROR en Nivel " + nivel + ": " + e.getMessage());
            return false;
        }
    }

    private By controlHabilitado(int nivel) {
        return By.xpath(
                "//div[contains(@class,'classifications-dropdown-wrap')]" +
                "[.//label[normalize-space()='Nivel " + nivel + "']]" +
                "//div[contains(@class,'classifications-dropdown-control')]" +
                "[not(contains(@class,'classifications-dropdown-control--disabled'))]");
    }

    private boolean nivelMuestraValor(WebDriver driver, int nivel, String valor) {
        try {
            String texto = driver.findElement(By.xpath(
                    "//div[contains(@class,'classifications-dropdown-wrap')]" +
                    "[.//label[normalize-space()='Nivel " + nivel + "']]" +
                    "//*[contains(@class,'classifications-dropdown-control-label')]")).getText().trim();
            return texto.equalsIgnoreCase(valor) || texto.toLowerCase().contains(valor.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));
    }

    private void scrollToCenter(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
        } catch (Exception ignored) {}
    }

    private void jsClick(WebDriver driver, WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }
}
