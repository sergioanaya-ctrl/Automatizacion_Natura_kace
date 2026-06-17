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
import java.util.List;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class ValidarClienteCreado implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final Duration TIMEOUT_TOTAL = Duration.ofSeconds(40);

    public static Performable ahora() {
        return instrumented(ValidarClienteCreado.class);
    }

    @Override
    @Step("Validar que el cliente fue creado correctamente")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        // El swal2 de éxito aparece tras la respuesta del backend. Sondear (doc principal + todos
        // los iframes) hasta TIMEOUT_TOTAL; al encontrarlo se confirma con OK (o se falla si es error).
        long fin = System.currentTimeMillis() + TIMEOUT_TOTAL.toMillis();
        while (System.currentTimeMillis() < fin) {
            if (buscarSwalEnTodosLosFrames(driver)) {
                return;
            }
            dormir(500);
        }

        // Sin diálogo: revisar errores de validación de FormIO dentro del iframe.
        entrarAlIframe(driver);
        List<WebElement> errores = driver.findElements(By.cssSelector(
                ".formio-error-wrapper, .has-error, .formio-errors .error, .alert-danger"))
                .stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        driver.switchTo().defaultContent();

        if (!errores.isEmpty()) {
            String detalle = errores.stream()
                    .map(e -> e.getText().trim())
                    .filter(t -> !t.isEmpty())
                    .reduce((a, b) -> a + " | " + b)
                    .orElse("(sin texto)");
            throw new AssertionError(
                    "[ValidarClienteCreado] El formulario muestra errores de validación, " +
                    "el cliente NO fue creado: " + detalle);
        }

        throw new AssertionError(
                "[ValidarClienteCreado] No apareció el diálogo de éxito (SweetAlert2) ni errores visibles " +
                "tras " + TIMEOUT_TOTAL.getSeconds() + "s. Revisar el flujo de creación del cliente.");
    }

    /** Recorre el documento principal y cada iframe buscando un popup SweetAlert2. */
    private boolean buscarSwalEnTodosLosFrames(WebDriver driver) {
        // 1) Documento principal (lugar más común del swal2).
        driver.switchTo().defaultContent();
        if (procesarPopupSiExiste(driver)) return true;

        // 2) Iframe del formulario directamente (atajo al caso más probable).
        try {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(driver.findElement(FORM_IFRAME));
            if (procesarPopupSiExiste(driver)) return true;
        } catch (Exception ignored) {
        }

        // 3) El resto de iframes de primer nivel.
        driver.switchTo().defaultContent();
        int totalFrames = driver.findElements(By.tagName("iframe")).size();
        for (int i = 0; i < totalFrames; i++) {
            try {
                driver.switchTo().defaultContent();
                List<WebElement> frames = driver.findElements(By.tagName("iframe"));
                if (i >= frames.size()) break;
                driver.switchTo().frame(frames.get(i));
                if (procesarPopupSiExiste(driver)) return true;
            } catch (Exception ignored) {
                // frame no accesible / desapareció: continuar
            }
        }
        driver.switchTo().defaultContent();
        return false;
    }

    /**
     * Si hay un popup swal2 visible en el contexto actual: confirma con OK.
     * Devuelve true en éxito; lanza AssertionError si es de error; false si no hay popup.
     */
    private boolean procesarPopupSiExiste(WebDriver driver) {
        WebElement popup = driver.findElements(By.cssSelector(".swal2-popup")).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null);
        if (popup == null) return false;

        boolean esError = driver.findElements(By.cssSelector(".swal2-icon.swal2-error, .swal2-icon-error"))
                .stream().anyMatch(WebElement::isDisplayed);
        String texto = popup.getText().replace("\n", " ").trim();

        clickOk(driver);

        if (esError) {
            throw new AssertionError(
                    "[ValidarClienteCreado] El sistema reportó un error al crear el cliente: " + texto);
        }
        System.out.println("[ValidarClienteCreado] Cliente creado — diálogo de éxito confirmado: " + texto);
        return true;
    }

    private void clickOk(WebDriver driver) {
        WebElement ok = new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".swal2-confirm")));
        try {
            ok.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ok);
        }
    }

    private void entrarAlIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
