package com.natura.automation.tasks;

import com.natura.automation.interactions.FillCrearClienteForm;
import com.natura.automation.util.ModalSesionActiva;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class EjecutarCrearCliente implements Task {

    // Flujo desde el agente (el formulario OneScript ya está cargado):
    // 1. Salir de cualquier iframe activo
    // 2. Entrar al iframe OneScript (form_onescript_iframe)
    // 3. Llenar el formulario y enviarlo (FillCrearClienteForm -> botón 'Crear Cliente')
    // 4. Devolver contexto al documento principal

    private final Map<String, String> datos;

    public EjecutarCrearCliente(Map<String, String> datos) {
        this.datos = datos;
    }

    public static Performable conDatosAleatorios() {
        return instrumented(EjecutarCrearCliente.class, (Map<String, String>) null);
    }

    public static Performable conDatos(Map<String, String> datos) {
        return instrumented(EjecutarCrearCliente.class, datos);
    }

    @Override
    @Step("Ejecutar creación de cliente en Natura")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        // 1) Salir de cualquier iframe activo
        driver.switchTo().defaultContent();

        // 2) El formulario OneScript ya está cargado en la página del agente:
        //    entrar directamente al iframe. No hay menú 'Crear Cliente' que abrir;
        //    'Crear Cliente' es el botón de envío al final (lo maneja FillCrearClienteForm).
        //    Bajo carga (muchos runners en paralelo) el backend puede tardar más en inyectar
        //    el iframe que los 30s fijos que se usaban antes, y un solo ExpectedConditions
        //    fallaba en bloque sin reintentar. Aquí se sondea en bucle: cada intento vuelve a
        //    defaultContent y reintenta el switchTo, hasta que el iframe esté disponible.
        System.out.println("[EjecutarCrearCliente] Esperando iframe OneScript...");
        esperarIframeDisponible(driver, 90);
        System.out.println("[EjecutarCrearCliente] Iframe OK, iniciando formulario...");

        // 3) Llenar el formulario (aleatorio si datos == null) y enviar
        actor.attemptsTo(datos != null
                ? FillCrearClienteForm.conDatos(datos)
                : FillCrearClienteForm.conDatosAleatorios());

        // 4) Volver al contexto principal
        driver.switchTo().defaultContent();
        System.out.println("[EjecutarCrearCliente] Formulario enviado.");
    }

    /**
     * Espera a que el iframe del formulario esté disponible, reintentando el switchTo en bucle
     * en vez de un único ExpectedConditions con timeout fijo. Bajo carga (varios runners en
     * paralelo) el backend puede tardar en inyectar el iframe más de lo que tolera un solo
     * intento; aquí cada 500ms se reintenta desde defaultContent hasta lograrlo o agotar tope.
     */
    private void esperarIframeDisponible(WebDriver driver, int timeoutSeg) {
        By iframeBy = By.id("form_onescript_iframe");
        long inicio = System.currentTimeMillis();
        long fin = inicio + timeoutSeg * 1000L;
        long proximoLog = inicio + 10_000L; // heartbeat cada 10s para no verse "colgado"
        Exception ultimoError = null;

        while (System.currentTimeMillis() < fin) {
            try {
                driver.switchTo().defaultContent();
                // Red de seguridad: si el modal "sesión activa" reapareció aquí y nadie lo
                // cerró antes, esto lo detecta y lo cierra en vez de agotar el tope en silencio.
                ModalSesionActiva.manejar(driver, 0);
                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeBy));
                System.out.println("[EjecutarCrearCliente] Iframe detectado en " +
                        ((System.currentTimeMillis() - inicio) / 1000.0) + "s.");
                return; // éxito: ya está dentro del iframe
            } catch (Exception e) {
                ultimoError = e;
                long ahora = System.currentTimeMillis();
                if (ahora >= proximoLog) {
                    System.out.println("[EjecutarCrearCliente] Sigue esperando el iframe... " +
                            ((ahora - inicio) / 1000) + "s transcurridos.");
                    proximoLog = ahora + 10_000L;
                }
                dormir(500);
            }
        }
        throw new RuntimeException("[EjecutarCrearCliente] El iframe OneScript no estuvo disponible tras " +
                timeoutSeg + "s.", ultimoError);
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
