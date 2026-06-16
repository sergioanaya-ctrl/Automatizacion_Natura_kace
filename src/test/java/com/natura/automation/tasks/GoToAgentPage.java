package com.natura.automation.tasks;

import com.natura.automation.ui.AgentPage;
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

        // 1) Esperar document.readyState === 'complete'
        wait.until(d -> "complete".equals(js.executeScript("return document.readyState")));
        System.out.println("[GoToAgentPage] Página cargada OK");

        // 2) Esperar a que haya contenido en el body
        wait.until(d -> {
            Object len = js.executeScript("return document.body.innerHTML.length");
            return len != null && !len.toString().equals("0");
        });

        // 3) Esperar a que aparezca algún elemento de menú o navegación
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[role='menuitem'], [role='navigation'], .menu-item, nav, .sidebar")));
            System.out.println("[GoToAgentPage] Menú visible OK");
        } catch (Exception e) {
            System.out.println("[GoToAgentPage] No se encontró menú con selectores estándar — continuando");
        }
    }
}
