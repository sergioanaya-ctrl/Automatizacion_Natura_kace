package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Random;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Llena el formulario "Datos Transportadora" SOLO si está visible (algunas combinaciones de
 * niveles lo habilitan, ej. CAPTACION DE PEDIDOS / SOLICITUD / PEDIDO / PEDIDO MANUAL).
 * Si no aparece, no hace nada y el flujo continúa.
 *
 * Campos de texto simple y obligatorios: Número de Pedido (data[n_pedido]),
 * Estado del Envío (data[estado_envio]), Transportadora (data[transportadora]).
 * El campo n_reclamacion viene oculto (formio-hidden) y no se toca.
 *
 * Nota: el campo n_pedido tiene el mismo nombre en "Datos Planeamiento Comercial", por eso
 * la visibilidad se verifica sobre "transportadora" (único de esta sección) y no sobre n_pedido.
 */
public class LlenarFormularioTransportadora implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final Random RANDOM = new Random();

    public static Performable siAplica() {
        return instrumented(LlenarFormularioTransportadora.class);
    }

    @Override
    @Step("Diligenciar el formulario Datos Transportadora si está visible")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));

        if (!estaVisible(driver, ".formio-component-transportadora")) {
            System.out.println("[Transportadora] No visible — se omite y continúa el flujo.");
            driver.switchTo().defaultContent();
            return;
        }

        System.out.println("[Transportadora] Visible — diligenciando campos requeridos.");
        escribir(driver, "input[name='data[n_pedido]']",      randomDigitos(8));
        escribir(driver, "input[name='data[estado_envio]']",  randomDe("En tránsito", "Entregado", "Devuelto", "En bodega"));
        escribir(driver, "input[name='data[transportadora]']", randomDe("Servientrega", "Coordinadora", "TCC", "Envia"));

        driver.switchTo().defaultContent();
    }

    private boolean estaVisible(WebDriver driver, String css) {
        return driver.findElements(By.cssSelector(css)).stream().anyMatch(WebElement::isDisplayed);
    }

    private void escribir(WebDriver driver, String css, String valor) {
        try {
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
            scrollToCenter(driver, el);
            try {
                el.click();
                el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                el.sendKeys(valor);
            } catch (Exception clickEx) {
                ((JavascriptExecutor) driver).executeScript(
                        "var el=arguments[0],v=arguments[1];" +
                        "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                        "s.call(el,v);" +
                        "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                        "el.dispatchEvent(new Event('change',{bubbles:true}));", el, valor);
            }
            System.out.println("  [Transportadora] " + css + " = " + valor);
            com.natura.automation.util.ReportePaso.valor(css, valor);
        } catch (Exception e) {
            System.err.println("  [Transportadora] ERROR en " + css + ": " + e.getMessage());
        }
    }

    private String randomDigitos(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    private String randomDe(String... opciones) {
        return opciones[RANDOM.nextInt(opciones.length)];
    }

    private void scrollToCenter(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }
}
