package com.natura.automation.tasks;

import com.natura.automation.ui.AgentPage;
import com.natura.automation.ui.LoginPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;
import static net.serenitybdd.screenplay.Tasks.instrumented;

public class LoginWithCognito implements Task {

    private final String username;
    private final String password;

    public LoginWithCognito(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static Performable with(String username, String password) {
        return instrumented(LoginWithCognito.class, username, password);
    }

    @Override
    @Step("Login con Cognito usando credenciales")
    public <T extends Actor> void performAs(T actor) {
        // 1. Abrir URL de Cognito
        actor.attemptsTo(
                Open.url(LoginPage.COGNITO_LOGIN_URL)
        );

        // 2. Llenar username y click Next
        actor.attemptsTo(
                WaitUntil.the(LoginPage.COGNITO_USERNAME, isVisible()).forNoMoreThan(10).seconds(),
                Enter.theValue(username).into(LoginPage.COGNITO_USERNAME),
                Click.on(LoginPage.COGNITO_NEXT_BUTTON)
        );

        // 3. Llenar password y click Continue
        actor.attemptsTo(
                WaitUntil.the(LoginPage.COGNITO_PASSWORD, isVisible()).forNoMoreThan(10).seconds(),
                Enter.theValue(password).into(LoginPage.COGNITO_PASSWORD),
                Click.on(LoginPage.COGNITO_CONTINUE_BUTTON)
        );

        // 4. Esperar a estar en Agent page (verificar URL contiene "/agent")
        esperarEnAgentPage(actor);
    }

    private void esperarEnAgentPage(Actor actor) {
        // Esperar a que la URL contenga "/agent" usando WebDriverWait dinámico
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(
                d -> d.getCurrentUrl().contains("/agent")
            );
            return;
        } catch (TimeoutException e) {
            System.out.println("  No se llegó automáticamente a /agent en 8 segundos, forzando navegación...");
        }

        // Si no llegó automáticamente a /agent, fuerza la navegación a la URL de Agent.
        actor.attemptsTo(Open.url(AgentPage.URL));

        // Esperar nuevamente a que la URL contenga "/agent"
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(
                d -> d.getCurrentUrl().contains("/agent")
            );
            return;
        } catch (TimeoutException e) {
            throw new AssertionError("Timeout esperando a llegar a AgentPage. URL actual: " + driver.getCurrentUrl());
        }
    }
}
