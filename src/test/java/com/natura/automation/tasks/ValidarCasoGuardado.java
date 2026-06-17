package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Valida que el caso quedó guardado: tras guardar y recargar, la cabecera muestra
 * "Detalles del caso numero N" (la pantalla pasa de "Crear caso" a "Detalles del caso").
 */
public class ValidarCasoGuardado implements Task {

    private static final By TITULO_DETALLE = By.xpath(
            "//*[contains(translate(normalize-space(.),'DETALLES','detalles'),'detalles del caso')]");

    public static Performable ahora() {
        return instrumented(ValidarCasoGuardado.class);
    }

    @Override
    @Step("Validar que el caso fue guardado correctamente")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.switchTo().defaultContent();

        try {
            WebElement titulo = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.visibilityOfElementLocated(TITULO_DETALLE));
            System.out.println("[ValidarCasoGuardado] Caso guardado correctamente: " +
                    titulo.getText().replace("\n", " ").trim());
        } catch (Exception e) {
            throw new AssertionError("[ValidarCasoGuardado] No apareció la cabecera 'Detalles del caso numero N' " +
                    "tras guardar. La pantalla no confirmó la creación del caso.");
        }
    }
}
