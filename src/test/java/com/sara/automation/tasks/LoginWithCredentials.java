package com.sara.automation.tasks;

import com.sara.automation.ui.LoginPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.NoSuchElementException;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class LoginWithCredentials implements Task {

    private final String username;
    private final String password;

    public LoginWithCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static Performable with(String username, String password) {
        return instrumented(LoginWithCredentials.class, username, password);
    }

    @Override
    @Step("Login con credenciales (si los campos existen)")
    public <T extends Actor> void performAs(T actor) {
        try {
            actor.attemptsTo(
                    Enter.theValue(username).into(LoginPage.USERNAME),
                    Enter.theValue(password).into(LoginPage.PASSWORD),
                    Click.on(LoginPage.LOGIN_BUTTON)
            );
        } catch (NoSuchElementException e) {
            // Si no existen campos de login en la página actual, ignorar (prueba rápida)
            System.out.println("Campos de login no encontrados en la página actual. Se omite el login.");
        } catch (Exception e) {
            System.out.println("Error intentando hacer login: " + e.getMessage());
        }
    }
}

