package com.natura.automation.ui;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class LoginPage {

    public static final String COGNITO_LOGIN_URL = NaturaPage.COGNITO_LOGIN_URL;

    public static final Target COGNITO_USERNAME = Target.the("Campo usuario Cognito")
            .located(By.cssSelector("input[name='username']"));

    public static final Target COGNITO_NEXT_BUTTON = Target.the("Botón siguiente Cognito")
            .located(By.cssSelector("button[type='submit']"));

    public static final Target COGNITO_PASSWORD = Target.the("Campo contraseña Cognito")
            .located(By.cssSelector("input[name='password']"));

    public static final Target COGNITO_CONTINUE_BUTTON = Target.the("Botón continuar Cognito")
            .located(By.cssSelector("button[type='submit']"));

    private LoginPage() {}
}
