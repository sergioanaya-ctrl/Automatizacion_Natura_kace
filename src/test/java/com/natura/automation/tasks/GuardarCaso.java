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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/** Hace clic en "Guardar" el caso y espera a que la página recargue. */
public class GuardarCaso implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final By BOTON_GUARDAR = By.xpath("//button[contains(@class, 'kace-floating-submit') and normalize-space()='Guardar']");
    public static Performable ahora() {
        return instrumented(GuardarCaso.class);
    }

    @Override
    @Step("Guardar el caso")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));

        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.presenceOfElementLocated(BOTON_GUARDAR));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(BOTON_GUARDAR));
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        System.out.println("[GuardarCaso] Clic en 'Guardar' OK — esperando recarga...");

        driver.switchTo().defaultContent();

        // Espera fija de 5s ANTES de verificar nada: justo tras el clic, document.readyState
        // todavía dice "complete" (de la página VIEJA, la recarga aún no arrancó) y el spinner
        // todavía no aparece, así que las comprobaciones de abajo pasaban en falso positivo de
        // inmediato, dejando solo ~1.2s de margen real. Bajo carga eso no alcanzaba y se
        // validaba antes de que el caso realmente se hubiera guardado.
        dormir(5000);

        // Esperar a que la página recargue tras guardar.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        } catch (Exception ignored) {}

        // Esperar a que cierre cualquier spinner de guardado (swal2), y un buffer corto para
        // que el SPA re-renderice los estados.
        esperarSinSpinner(driver, 15);
        dormir(1200);
    }

    /** Espera (rápido) a que NO haya spinner/popup de carga swal2 visible; retorna apenas desaparece. */
    private void esperarSinSpinner(WebDriver driver, int timeoutSeg) {
        long fin = System.currentTimeMillis() + timeoutSeg * 1000L;
        while (System.currentTimeMillis() < fin) {
            boolean spinner = driver.findElements(By.cssSelector(".swal2-loader, .swal2-popup"))
                    .stream().anyMatch(WebElement::isDisplayed);
            if (!spinner) return;
            dormir(200);
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
