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

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class EjecutarCrearCaso implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    // Botón "Nuevo Caso" (visible tras crear el cliente).
    private static final By BOTON_NUEVO_CASO = By.cssSelector("button[name='data[nuevoCliente1]']");

    public static Performable nuevo() {
        return instrumented(EjecutarCrearCaso.class);
    }

    @Override
    @Step("Crear un nuevo caso")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        // El botón vive dentro del iframe OneScript.
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(BOTON_NUEVO_CASO));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(BOTON_NUEVO_CASO));
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        System.out.println("[EjecutarCrearCaso] Clic en 'Nuevo Caso' OK");

        // Esperar a que el formulario del caso REALMENTE cargue (niveles/estados/descripción),
        // en vez de una pausa fija de 10s. Al dar clic en "Nuevo Caso" el iframe se RECARGA,
        // por eso hay que RE-ENGANCHARSE al iframe en cada intento (la referencia anterior queda obsoleta).
        long inicio = System.currentTimeMillis();
        By componentesCaso = By.cssSelector(
                ".classifications-dropdown-wrap, .formio-component-kaceStates, .formio-component-kaceDescription");
        long fin = inicio + 15_000L;
        boolean cargado = false;
        while (System.currentTimeMillis() < fin) {
            try {
                driver.switchTo().defaultContent();
                driver.switchTo().frame(driver.findElement(FORM_IFRAME));
                if (!driver.findElements(componentesCaso).isEmpty()) {
                    cargado = true;
                    break;
                }
            } catch (Exception ignored) {
                // iframe aún recargando: reintentar.
            }
            dormir(250);
        }
        if (cargado) {
            System.out.println("[EjecutarCrearCaso] Caso cargado en " +
                    ((System.currentTimeMillis() - inicio) / 1000.0) + "s.");
        } else {
            System.err.println("[EjecutarCrearCaso] El formulario del caso no apareció en 15s — continuando igual.");
        }

        driver.switchTo().defaultContent();
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
