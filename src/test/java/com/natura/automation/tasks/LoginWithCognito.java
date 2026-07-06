package com.natura.automation.tasks;

import com.natura.automation.ui.AgentPage;
import com.natura.automation.ui.LoginPage;
import com.natura.automation.util.ModalSesionActiva;
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
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();

        // Cognito redirige a /auth (no a /agent), así que esperar 8s a "/agent" siempre se agotaba.
        // En su lugar: sondear hasta que la URL llegue sola a /agent O salga del dominio de Cognito
        // (señal de que el intercambio de código en /auth ya empezó); apenas ocurre, se continúa.
        long fin = System.currentTimeMillis() + 12_000L;
        boolean deVueltaEnApp = false;
        while (System.currentTimeMillis() < fin) {
            String url = driver.getCurrentUrl();
            if (url.contains("/agent")) {
                return; // ya llegó solo
            }
            if (!url.contains("amazoncognito.com")) {
                deVueltaEnApp = true; // de vuelta en la app (/auth) — proceder a forzar /agent
                break;
            }
            dormir(200);
        }
        if (!deVueltaEnApp) {
            System.out.println("  No se detectó el retorno desde Cognito en 12s, forzando navegación igual...");
        }

        // La cuenta puede quedar con sesión activa de una corrida anterior (el test nunca hace
        // logout, solo cierra el navegador). La app entonces muestra el modal "Ya tienes una
        // sesión activa" y bloquea todo hasta que se responda. Si no se maneja, el test se queda
        // congelado indefinidamente en "Cargando autenticación..." esperando un iframe que nunca
        // llega. Se cierra aquí eligiendo "Sí, continuar aquí" (cierra la sesión anterior).
        ModalSesionActiva.manejar(driver, 5);

        // Buffer breve para que /auth complete el intercambio de código y establezca la sesión
        // antes de forzar la navegación a /agent.
        dormir(1500);
        actor.attemptsTo(Open.url(AgentPage.URL));

        // Puede reaparecer el modal justo después de navegar a /agent.
        ModalSesionActiva.manejar(driver, 5);

        // Esperar a que la URL contenga "/agent".
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
                ModalSesionActiva.manejar(d, 0); // reintenta por si aparece durante la espera
                return d.getCurrentUrl().contains("/agent");
            });
        } catch (TimeoutException e) {
            throw new AssertionError("Timeout esperando a llegar a AgentPage. URL actual: " + driver.getCurrentUrl());
        }
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
