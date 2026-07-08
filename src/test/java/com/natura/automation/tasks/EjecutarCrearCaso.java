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
import org.openqa.selenium.interactions.Actions;
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
// Darle 2 segundos al frontend lento para que procese el guardado del cliente
            dormir(2000);
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
            System.out.println("[EjecutarCrearCaso] Clic en 'Nuevo Caso' OK (Método Humano Nativo)");
        } catch (Exception e) {
           System.out.println("[EjecutarCrearCaso] Falló el clic normal, intentando con clase Actions...");
                    // Usamos Actions para simular el mouse físico en lugar de JS inyectado
                        Actions actions = new Actions(driver);
                        actions.moveToElement(btn).click().perform();
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

            // Esperar a que el control de Nivel 3 esté HABILITADO (los datos de clasificación
            // llegan por AJAX después de que el contenedor ya está renderizado). Sin esta espera,
            // SeleccionarNiveles arranca antes de que las opciones existan y falla intermitentemente.
            By nivel3Habilitado = By.xpath(
                    "//div[contains(@class,'classifications-dropdown-wrap')]" +
                    "[.//label[normalize-space()='Nivel 3']]" +
                    "//div[contains(@class,'classifications-dropdown-control')]" +
                    "[not(contains(@class,'classifications-dropdown-control--disabled'))]");
            try {
                new WebDriverWait(driver, Duration.ofSeconds(30))
                        .until(ExpectedConditions.presenceOfElementLocated(nivel3Habilitado));
                System.out.println("[EjecutarCrearCaso] Nivel 3 habilitado y listo.");
            } catch (Exception e) {
                System.err.println("[EjecutarCrearCaso] Nivel 3 no se habilitó en 30s — se continúa de todas formas.");
            }
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
