package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Recorre la máquina de estados del caso:
 *   mientras haya "Estados disponibles", elige uno, guarda y espera la recarga.
 * Prefiere estados NO terminales (deja SOLUCIONADO para el final), de modo que avance
 * por los intermedios (PRIMER CONTACTO -> EN GESTION -> ...) antes de finalizar.
 *
 * Los estados disponibles son botones .btn-outline-secondary; el estado actual es verde
 * (.btn-success) y no se toca.
 */
public class RecorrerTransicionesEstado implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final By ESTADOS_DISPONIBLES = By.cssSelector(
            ".formio-component-kaceStates .states__content button.btn-outline-secondary");

    // Orden de prioridad: si aparecen disponibles, se eligen en este orden.
    // EN GESTION lleva a SOLUCIONADO; SOLUCIONADO es el estado final.
    private static final List<String> PRIORITARIOS = Arrays.asList("EN GESTION", "SOLUCIONADO");
    // Estados que finalizan el flujo.
    private static final List<String> TERMINALES = Arrays.asList("SOLUCIONADO");
    private static final int MAX_TRANSICIONES = 10; // tope de seguridad

    public static Performable hastaFinalizar() {
        return instrumented(RecorrerTransicionesEstado.class);
    }

    @Override
    @Step("Recorrer las transiciones de estado del caso hasta finalizar")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        java.util.Set<String> visitados = new java.util.HashSet<>();

        for (int i = 1; i <= MAX_TRANSICIONES; i++) {
            entrarIframe(driver);

            List<WebElement> disponibles = driver.findElements(ESTADOS_DISPONIBLES).stream()
                    .filter(WebElement::isDisplayed)
                    .collect(Collectors.toList());

            if (disponibles.isEmpty()) {
                System.out.println("[Estados] Sin estados disponibles — flujo de estados finalizado (" + (i - 1) + " transiciones).");
                driver.switchTo().defaultContent();
                return;
            }

            System.out.println("[Estados] Disponibles: " + disponibles.stream()
                    .map(b -> b.getText().trim()).collect(Collectors.toList()));

            WebElement elegido = elegirEstado(disponibles, visitados);
            if (elegido == null) {
                // El caso quedó oscilando entre estados ya visitados (ej. ESCALADO BO <-> PRIMER
                // CONTACTO) sin llegar nunca a un estado que la app confirme como final. Si se
                // continúa, ValidarCasoGuardado esperaría minutos por una pantalla "Detalles del
                // caso" que jamás va a aparecer. Se falla aquí mismo, de inmediato, en vez de
                // arrastrar el problema al siguiente paso con un error que no apunta a la causa.
                driver.switchTo().defaultContent();
                throw new RuntimeException("[Estados] El caso quedó oscilando entre estados ya visitados " +
                        visitados + " sin alcanzar un estado final — se detiene la automatización aquí " +
                        "en vez de esperar una confirmación que no va a llegar.");
            }

            String estado = elegido.getText().trim();
            visitados.add(estado.toLowerCase());
            scrollToCenter(driver, elegido);
            clickRobusto(driver, elegido);
            System.out.println("[Estados] Transición " + i + " -> " + estado);

            driver.switchTo().defaultContent();
            actor.attemptsTo(GuardarCaso.ahora()); // clic en Guardar + espera de recarga

            if (esTerminal(estado)) {
                System.out.println("[Estados] Estado final '" + estado + "' alcanzado — flujo de estados finalizado.");
                return;
            }
        }
        driver.switchTo().defaultContent();
        throw new RuntimeException("[Estados] Se alcanzó el tope de " + MAX_TRANSICIONES +
                " transiciones sin llegar a un estado final — se detiene la automatización aquí.");
    }

    /**
     * Elige el siguiente estado:
     *   1) el prioritario de mayor orden disponible (EN GESTION, luego SOLUCIONADO);
     *   2) si no hay prioritarios, el primer disponible NO visitado y NO terminal (avanza sin volver atrás);
     *   3) si no, el primer disponible NO visitado;
     *   4) si todos están visitados, devuelve null (evita oscilar entre ESCALADO BO/NATURA).
     */
    private WebElement elegirEstado(List<WebElement> disponibles, java.util.Set<String> visitados) {
        for (String prioritario : PRIORITARIOS) {
            for (WebElement b : disponibles) {
                if (b.getText().trim().equalsIgnoreCase(prioritario)) {
                    return b;
                }
            }
        }
        WebElement noVisitadoNoTerminal = disponibles.stream()
                .filter(b -> !visitados.contains(b.getText().trim().toLowerCase()))
                .filter(b -> !esTerminal(b.getText().trim()))
                .findFirst().orElse(null);
        if (noVisitadoNoTerminal != null) return noVisitadoNoTerminal;

        return disponibles.stream()
                .filter(b -> !visitados.contains(b.getText().trim().toLowerCase()))
                .findFirst().orElse(null);
    }

    private boolean esTerminal(String estado) {
        return TERMINALES.stream().anyMatch(t -> t.equalsIgnoreCase(estado));
    }

    private void entrarIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));
    }

    private void scrollToCenter(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }

    private void clickRobusto(WebDriver driver, WebElement el) {
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
