package com.natura.automation.tasks;

import com.natura.automation.ui.AgentPage;
import com.natura.automation.util.ModalSesionActiva;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class GoToAgentPage implements Task {

    public static Performable now() {
        return instrumented(GoToAgentPage.class);
    }

    @Override
    @Step("Navega a la pagina del agente de Natura")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Open.url(AgentPage.URL));

        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        // El modal "Ya tienes una sesión activa" puede reaparecer aquí también (no solo en el
        // login). Si no se cierra, la página nunca avanza al formulario y el iframe jamás carga.
        ModalSesionActiva.manejar(driver, 3);

        // 1) Esperar document.readyState === 'complete'
        wait.until(d -> "complete".equals(js.executeScript("return document.readyState")));
        System.out.println("[GoToAgentPage] Página cargada OK");

        // 2) Esperar a que haya contenido en el body
        wait.until(d -> {
            Object len = js.executeScript("return document.body.innerHTML.length");
            return len != null && !len.toString().equals("0");
        });

        // 3) Esperar a que el iframe del formulario esté presente (lo que se usa a continuación).
        //    Antes se esperaba 60s por un menú que no existe en esta pantalla.
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("form_onescript_iframe")));
            System.out.println("[GoToAgentPage] Iframe del formulario presente OK");
        } catch (Exception e) {
            System.out.println("[GoToAgentPage] Iframe no detectado aún — continuando (lo reintenta EjecutarCrearCliente)");
        }
    }
}
