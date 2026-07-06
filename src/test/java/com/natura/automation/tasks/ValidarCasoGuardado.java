package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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

        // Bajo carga (muchos runners en paralelo) la recarga tras Guardar puede tardar más que
        // un timeout fijo de 30s (mismo patrón visto en el iframe de Crear Cliente). Se sondea
        // en bucle con heartbeat cada 15s para distinguir "sigue esperando" de "colgado real",
        // y se sube el tope a 60s para dar margen al backend bajo concurrencia.
        long inicio = System.currentTimeMillis();
        long fin = inicio + 60_000L;
        long proximoLog = inicio + 15_000L;

        while (System.currentTimeMillis() < fin) {
            try {
                WebElement titulo = driver.findElement(TITULO_DETALLE);
                if (titulo.isDisplayed()) {
                    System.out.println("[ValidarCasoGuardado] Caso guardado correctamente: " +
                            titulo.getText().replace("\n", " ").trim() +
                            " (confirmado en " + ((System.currentTimeMillis() - inicio) / 1000.0) + "s)");
                    return;
                }
            } catch (Exception ignored) {
                // el título aún no existe en el DOM: reintentar.
            }
            long ahora = System.currentTimeMillis();
            if (ahora >= proximoLog) {
                System.out.println("[ValidarCasoGuardado] Sigue esperando la confirmación del caso... " +
                        ((ahora - inicio) / 1000) + "s transcurridos.");
                proximoLog = ahora + 15_000L;
            }
            dormir(300);
        }

        throw new AssertionError("[ValidarCasoGuardado] No apareció la cabecera 'Detalles del caso numero N' " +
                "tras guardar (60s). La pantalla no confirmó la creación del caso.");
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
