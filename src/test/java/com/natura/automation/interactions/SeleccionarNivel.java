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

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Selecciona un valor en un nivel del componente "Clasificaciones" (dropdown personalizado).
 * Estructura: cada nivel es un .classifications-dropdown-control (role=button, aria-haspopup=listbox)
 * dentro de un .classifications-dropdown-wrap rotulado por su label ("Nivel 3", "Nivel 4", ...).
 * Al abrirlo aparece un buscador ("Buscar clasificación…") y una lista de opciones.
 */
public class SeleccionarNivel implements Interaction {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");

    private final int nivel;
    private final String valor;

    public SeleccionarNivel(int nivel, String valor) {
        this.nivel = nivel;
        this.valor = valor;
    }

    public static Performable nivel(int nivel, String valor) {
        return instrumented(SeleccionarNivel.class, nivel, valor);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        ensureIframe(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // 1) Localizar el control del nivel por su label (que no esté deshabilitado).
        By controlNivel = By.xpath(
                "//div[contains(@class,'classifications-dropdown-wrap')]" +
                "[.//label[normalize-space()='Nivel " + nivel + "']]" +
                "//div[contains(@class,'classifications-dropdown-control')]" +
                "[not(contains(@class,'classifications-dropdown-control--disabled'))]");

        WebElement control = wait.until(ExpectedConditions.presenceOfElementLocated(controlNivel));
        scrollToCenter(driver, control);
        jsClick(driver, control);

        // 2) Buscador (si está presente): filtrar por el valor.
        try {
            WebElement buscador = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                            ".classifications-dropdown-search, input[placeholder*='Buscar']")));
            buscador.clear();
            buscador.sendKeys(valor);
        } catch (Exception sinBuscador) {
            // algunos niveles pueden no tener buscador; continuar a la lista
        }

        // 3) Seleccionar la opción por texto (case-insensitive) dentro de la lista de clasificaciones.
        String mayus = "'" + valor.toUpperCase() + "'";
        By opcion = By.xpath(
                "//ul[contains(@class,'classifications-dropdown-list')]" +
                "/li[contains(@class,'classifications-dropdown-item')]" +
                "[not(contains(@class,'--placeholder'))]" +
                "[translate(normalize-space(.), 'abcdefghijklmnopqrstuvwxyzáéíóúñ'," +
                " 'ABCDEFGHIJKLMNOPQRSTUVWXYZÁÉÍÓÚÑ') = " + mayus + "]");
        WebElement op = wait.until(ExpectedConditions.elementToBeClickable(opcion));
        scrollToCenter(driver, op);
        jsClick(driver, op);

        // 4) Verificar que el control del nivel ahora muestra el valor.
        if (nivelMuestraValor(driver, nivel, valor)) {
            System.out.println("  [Niveles] Nivel " + nivel + " = " + valor + " (OK)");
        } else {
            System.err.println("  [Niveles] Nivel " + nivel + " = " + valor +
                    " — no se pudo confirmar la selección (revisar selectores de opción).");
        }
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
