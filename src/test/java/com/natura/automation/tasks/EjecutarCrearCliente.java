package com.natura.automation.tasks;

import com.natura.automation.interactions.FillCrearClienteForm;
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
        System.out.println("[EjecutarCrearCliente] Esperando iframe OneScript...");
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("form_onescript_iframe")));
        System.out.println("[EjecutarCrearCliente] Iframe OK, iniciando formulario...");

        // 3) Llenar el formulario (aleatorio si datos == null) y enviar
        actor.attemptsTo(datos != null
                ? FillCrearClienteForm.conDatos(datos)
                : FillCrearClienteForm.conDatosAleatorios());

        // 4) Volver al contexto principal
        driver.switchTo().defaultContent();
        System.out.println("[EjecutarCrearCliente] Formulario enviado.");
    }
}
