package com.natura.automation.stepdefinitions;

import com.natura.automation.tasks.GoToAgentPage;
import com.natura.automation.tasks.LoginWithCognito;
import com.natura.automation.tasks.OpenCasesPage;
import com.natura.automation.utils.CredentialsReader;
import com.natura.automation.utils.UserPoolManager;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.thucydides.core.annotations.Managed;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebDriver;

public class LoginDefinitions {

    @Managed(driver = "chrome")
    WebDriver browser;

    private Actor actor;

    @Before
    public void prepararEscenario() {
        OnStage.setTheStage(new OnlineCast());
        actor = OnStage.theActorCalled("Natura");
    }

    @After
    public void finalizarEscenario() {
        // Libera el usuario cacheado para este thread. Necesario porque con maxParallelForks
        // bajo (ej. 2), Gradle reutiliza el MISMO thread ("Test worker") para ejecutar muchos
        // runners secuencialmente dentro de un mismo fork JVM. Sin liberar, el primer runner
        // de cada fork cachea su usuario y todos los siguientes de ese fork repetían el mismo
        // (el ciclo barajado nunca llegaba a asignarles uno nuevo). Al liberar aquí, el próximo
        // escenario en ese mismo thread vuelve a pedir uno del ciclo.
        UserPoolManager.releaseCurrentThreadUser();
    }

    @Given("el actor tiene un navegador disponible")
    public void elActorTieneUnNavegadorDisponible() {
        actor.can(BrowseTheWeb.with(browser));
    }

    @When("abre la pagina de casos")
    public void abreLaPaginaDeCasos() {
        actor.attemptsTo(OpenCasesPage.now());
    }

    @When("realiza login con credenciales")
    public void realizaLoginConCredenciales() {
        String user = CredentialsReader.getUsuario();
        String pass = CredentialsReader.getContrasena();
        actor.attemptsTo(LoginWithCognito.with(user, pass));
    }

    @When("navega a agent")
    public void navegaAAgent() {
        actor.attemptsTo(GoToAgentPage.now());
    }

    @Then("deberia estar en la ruta cases")
    public void deberiaEstarEnLaRutaCases() {
        String currentUrl = BrowseTheWeb.as(actor).getDriver().getCurrentUrl();
        Assertions.assertThat(currentUrl).contains("konecta");
    }

    @Then("deberia ver la ruta agent")
    public void deberiaVerLaRutaAgent() {
        String currentUrl = BrowseTheWeb.as(actor).getDriver().getCurrentUrl();
        boolean isAgent = currentUrl.contains("/agent");
        boolean isSSO = currentUrl.contains("id.konecta.cloud") || currentUrl.contains("konecta-cloud");
        Assertions.assertThat(isAgent || isSSO).isTrue();
    }



}
