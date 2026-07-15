package com.natura.automation.tasks;

import com.natura.automation.ui.InteraccionesPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Alterna entre "Gestionar" y "Liberar" hasta que aparezca "Crear Caso".
 * Cuando "Crear Caso" ya está visible, pulsa "Crear Caso" y luego "Usar cliente seleccionado".
 */
public class HacerClicGestionarOCrearCaso implements Task {

    private static final By BTN_GESTIONAR = By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Gestionar']");
    private static final By BTN_LIBERAR = By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Liberar']");
    private static final By BTN_CREAR_CASO = By.xpath("//button[@id='btn_interaction_create_case' and normalize-space()='Crear Caso']");
    private static final By BTN_USAR_CLIENTE_SELECCIONADO = By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Usar cliente seleccionado']");

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        for (int intento = 0; intento < 20; intento++) {
            System.out.println("[HacerClicGestionarOCrearCaso] intento=" + (intento + 1)
                    + " crearCaso=" + estaVisible(driver, BTN_CREAR_CASO)
                    + " gestionar=" + estaVisible(driver, BTN_GESTIONAR)
                    + " liberar=" + estaVisible(driver, BTN_LIBERAR));

            if (estaVisible(driver, BTN_CREAR_CASO)) {
                System.out.println("[HacerClicGestionarOCrearCaso] Crear Caso visible, clic en Crear Caso y luego en Usar cliente seleccionado.");
                clicarClickable(driver, BTN_CREAR_CASO);
                clicarClickable(driver, BTN_USAR_CLIENTE_SELECCIONADO);
                return;
            }

            Optional<By> accion = accionVisible(driver);
            if (accion.isPresent()) {
                System.out.println("[HacerClicGestionarOCrearCaso] clic en " + nombreAccion(accion.get()));
                clickYCambiaEstado(driver, accion.get());
                if (estaVisible(driver, BTN_CREAR_CASO)) {
                    System.out.println("[HacerClicGestionarOCrearCaso] Crear Caso apareció justo después del clic.");
                    clicarClickable(driver, BTN_CREAR_CASO);
                    clicarClickable(driver, BTN_USAR_CLIENTE_SELECCIONADO);
                    return;
                }
                continue;
            }

            System.out.println("[HacerClicGestionarOCrearCaso] sin Gestionar/Liberar visible, esperando.");
            esperarVisible(wait, BTN_GESTIONAR, BTN_LIBERAR, BTN_CREAR_CASO);
        }

        if (estaVisible(driver, BTN_CREAR_CASO)) {
            System.out.println("[HacerClicGestionarOCrearCaso] Crear Caso visible al final, clic en Crear Caso y luego en Usar cliente seleccionado.");
            clicarClickable(driver, BTN_CREAR_CASO);
            clicarClickable(driver, BTN_USAR_CLIENTE_SELECCIONADO);
            return;
        }

        throw new AssertionError("No apareció el botón Crear Caso tras alternar entre Gestionar y Liberar.");
    }

    private Optional<By> accionVisible(WebDriver driver) {
        if (elementoVisible(driver, BTN_GESTIONAR)) {
            return Optional.of(BTN_GESTIONAR);
        }
        if (elementoVisible(driver, BTN_LIBERAR)) {
            return Optional.of(BTN_LIBERAR);
        }
        return Optional.empty();
    }

    private String nombreAccion(By locator) {
        if (BTN_GESTIONAR.equals(locator)) {
            return "Gestionar";
        }
        if (BTN_LIBERAR.equals(locator)) {
            return "Liberar";
        }
        return locator.toString();
    }

    private void clickYCambiaEstado(WebDriver driver, By locator) {
        clicar(driver, locator);
        esperarAQueElBotonCambie(driver, locator);
    }

    private void esperarAQueElBotonCambie(WebDriver driver, By accionActual) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        try {
            if (accionActual.equals(BTN_GESTIONAR)) {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(BTN_LIBERAR),
                        ExpectedConditions.visibilityOfElementLocated(BTN_CREAR_CASO)
                ));
            } else {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(BTN_GESTIONAR),
                        ExpectedConditions.visibilityOfElementLocated(BTN_CREAR_CASO)
                ));
            }
            if (estaVisible(driver, BTN_CREAR_CASO)) {
                System.out.println("[HacerClicGestionarOCrearCaso] Crear Caso ya visible tras " + nombreAccion(accionActual));
            } else {
                System.out.println("[HacerClicGestionarOCrearCaso] cambió de " + nombreAccion(accionActual));
            }
        } catch (Exception e) {
            System.out.println("[HacerClicGestionarOCrearCaso] no se detectó cambio tras " + nombreAccion(accionActual));
        }
    }

    private void clicar(WebDriver driver, By locator) {
        long fin = System.currentTimeMillis() + 8000L;
        while (System.currentTimeMillis() < fin) {
            try {
                WebElement elemento = driver.findElement(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", elemento);
                try {
                    elemento.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
                }
                return;
            } catch (Exception ignored) {
                dormir(250);
            }
        }
        throw new AssertionError("No se pudo hacer clic en el elemento: " + locator);
    }

    private void clicarClickable(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement elemento = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", elemento);
        try {
            elemento.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
        }
    }

    private boolean elementoVisible(WebDriver driver, By locator) {
        List<WebElement> elementos = driver.findElements(locator);
        return elementos.stream().anyMatch(WebElement::isDisplayed);
    }

    private boolean estaVisible(WebDriver driver, By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void esperarProcesamiento() {
        // Sin pausa fija: el flujo depende de waits explícitos.
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean esperarVisible(WebDriverWait wait, By... locators) {
        try {
            return wait.until(driver -> {
                for (By locator : locators) {
                    if (estaVisible(driver, locator)) {
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    public static Performable nuevo() {
        return instrumented(HacerClicGestionarOCrearCaso.class);
    }
}
