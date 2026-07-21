package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Diligencia los datos básicos del correo de respuesta dentro del formulario del caso.
 */
public class DiligenciarDatosCorreo implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final By BTN_NUEVA_RESPUESTA = By.xpath("//button[@type='button' and contains(normalize-space(), 'Nueva respuesta')]");
    private static final By INPUT_DE = By.xpath("//div[contains(@class,'email-form-field')][label[normalize-space()='De']]//input");
    private static final By INPUT_PARA = By.xpath("//div[contains(@class,'email-form-field')][label[normalize-space()='Para']]//input");
    private static final By INPUT_ASUNTO = By.xpath("//div[contains(@class,'email-form-field')][label[normalize-space()='Asunto']]//input");
    private static final By SELECT_PLANTILLA = By.xpath("//div[contains(@class,'email-form-field')][label[normalize-space()='Plantilla']]//select");

    public static Performable diligenciar() {
        return instrumented(DiligenciarDatosCorreo.class);
    }

    @Override
    @Step("Diligenciar datos de correo")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));

        WebElement nuevaRespuesta = wait.until(ExpectedConditions.elementToBeClickable(BTN_NUEVA_RESPUESTA));
        scroll(driver, nuevaRespuesta);
        nuevaRespuesta.click();

        //escribir(driver, wait, INPUT_DE, "sergio.anaya@konecta.com");
        escribir(driver, wait, INPUT_PARA, "sergio.anaya@konecta.com");
        escribir(driver, wait, INPUT_ASUNTO, "Respuesta caso " + randomNumero());
        seleccionarPlantillaAleatoria(driver, wait);

        System.out.println("[DiligenciarDatosCorreo] Datos de correo diligenciados.");
        driver.switchTo().defaultContent();
    }

    private void escribir(WebDriver driver, WebDriverWait wait, By locator, String texto) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scroll(driver, input);
        input.clear();
        input.sendKeys(texto);
    }

    private void seleccionarPlantillaAleatoria(WebDriver driver, WebDriverWait wait) {
        WebElement selectElement = wait.until(ExpectedConditions.elementToBeClickable(SELECT_PLANTILLA));
        scroll(driver, selectElement);

        Select select = new Select(selectElement);
        List<WebElement> opciones = new ArrayList<>();
        for (WebElement option : select.getOptions()) {
            String value = option.getAttribute("value");
            if (value != null && !value.trim().isEmpty()) {
                opciones.add(option);
            }
        }

        if (opciones.isEmpty()) {
            throw new AssertionError("No hay plantillas disponibles para seleccionar.");
        }

        WebElement opcion = opciones.get(new Random().nextInt(opciones.size()));
        select.selectByValue(opcion.getAttribute("value"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                selectElement);
    }

    private int randomNumero() {
        return 1000 + new Random().nextInt(9000);
    }

    private void scroll(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }
}
