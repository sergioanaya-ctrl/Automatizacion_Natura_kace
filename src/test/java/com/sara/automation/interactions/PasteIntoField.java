package com.sara.automation.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.abilities.BrowseTheWeb.as;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class PasteIntoField implements Interaction {

    private final Target target;
    private final String value;

    public PasteIntoField(Target target, String value) {
        this.target = target;
        this.value = value;
    }

    public static Performable value(String value, Target target) {
        return instrumented(PasteIntoField.class, target, value);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        // Garantiza que el driver este dentro del iframe.
        WebDriver driver = as(actor).getDriver();
        driver.switchTo().defaultContent();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("form_onescript_iframe")));
        } catch (Exception e) {
            System.out.println("[PasteIntoField] Warning: No se pudo cambiar al iframe, continuando de todas formas...");
        }

        actor.attemptsTo(
                WaitUntil.the(target, isVisible()).forNoMoreThan(20).seconds(),
                Click.on(target)
        );

        WebElement element = target.resolveFor(actor);

        try {
            copiarAlPortapapeles(value);

            new Actions(driver)
                    .moveToElement(element)
                    .click()
                    .keyDown(Keys.CONTROL)
                    .sendKeys("a")
                    .keyUp(Keys.CONTROL)
                    .sendKeys(Keys.DELETE)
                    .keyDown(Keys.CONTROL)
                    .sendKeys("v")
                    .keyUp(Keys.CONTROL)
                    .sendKeys(Keys.TAB)
                    .perform();
        } catch (Exception e) {
            pegarConJavaScript(driver, element, value);
        }
    }

    private void copiarAlPortapapeles(String value) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(value), null);
    }

    private void pegarConJavaScript(WebDriver driver, WebElement element, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "const el = arguments[0];" +
                        "const value = arguments[1];" +
                        "el.focus();" +
                        "el.value = '';" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.value = value;" +
                        "el.dispatchEvent(new Event('paste', { bubbles: true }));" +
                        "el.dispatchEvent(new InputEvent('input', { bubbles: true, data: value, inputType: 'insertFromPaste' }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "el.blur();",
                element,
                value
        );
    }
}
