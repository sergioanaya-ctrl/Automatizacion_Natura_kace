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

/** Escribe la descripción del caso en el editor enriquecido (contenteditable). */
public class EjecutarDescripcionCaso implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final By EDITOR = By.cssSelector(
            ".formio-component-kaceDescription .editor-content[contenteditable='true']");

    public static Performable diligenciar() {
        return instrumented(EjecutarDescripcionCaso.class);
    }

    @Override
    @Step("Diligenciar la descripción del caso")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));

        String texto = "Caso de prueba automatizado generado el " + System.currentTimeMillis() +
                ". Descripción de validación del flujo de creación de casos.";

        WebElement editor = new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.presenceOfElementLocated(EDITOR));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", editor);
        try {
            editor.click();
            editor.sendKeys(texto);
        } catch (Exception e) {
            // Fallback: fijar contenido por JS y disparar input para que el editor lo registre.
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].innerHTML = arguments[1];" +
                    "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));",
                    editor, texto);
        }
        System.out.println("[EjecutarDescripcionCaso] Descripción diligenciada.");
        com.natura.automation.util.ReportePaso.valor("descripcion_caso", texto);
        driver.switchTo().defaultContent();
    }
}
