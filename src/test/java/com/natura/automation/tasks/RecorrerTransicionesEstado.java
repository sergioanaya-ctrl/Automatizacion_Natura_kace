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

    // Estados que finalizan el flujo: se eligen solo cuando no hay otra opción.
    private static final List<String> TERMINALES = Arrays.asList("SOLUCIONADO");
    private static final int MAX_TRANSICIONES = 8; // tope de seguridad

    public static Performable hastaFinalizar() {
        return instrumented(RecorrerTransicionesEstado.class);
    }

    @Override
    @Step("Recorrer las transiciones de estado del caso hasta finalizar")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

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

            // Preferir un estado NO terminal; usar el terminal solo si es el único.
            WebElement elegido = disponibles.stream()
                    .filter(b -> TERMINALES.stream().noneMatch(t -> b.getText().trim().equalsIgnoreCase(t)))
                    .findFirst()
                    .orElse(disponibles.get(0));

            String estado = elegido.getText().trim();
            scrollToCenter(driver, elegido);
            clickRobusto(driver, elegido);
            System.out.println("[Estados] Transición " + i + " -> " + estado);

            driver.switchTo().defaultContent();
            actor.attemptsTo(GuardarCaso.ahora()); // clic en Guardar + espera de recarga
        }
        System.err.println("[Estados] Se alcanzó el tope de " + MAX_TRANSICIONES + " transiciones — revisar el flujo.");
        driver.switchTo().defaultContent();
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
