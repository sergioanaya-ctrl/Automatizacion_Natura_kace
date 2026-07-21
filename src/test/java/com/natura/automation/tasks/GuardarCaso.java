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

/** Hace clic en "Guardar" el caso y espera a que la pagina recargue. */
public class GuardarCaso implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final By BOTON_GUARDAR = By.xpath("//button[contains(@class, 'kace-floating-submit') and normalize-space()='Guardar']");
    private static final By SWAL_POPUP = By.cssSelector(".swal2-popup");
    private static final By SWAL_CONFIRM = By.cssSelector(".swal2-confirm");
    private static final By SWAL_LOADER = By.cssSelector(".swal2-loader");

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
        System.out.println("[GuardarCaso] Clic en 'Guardar' OK - esperando recarga...");

        driver.switchTo().defaultContent();

        // Espera fija antes de verificar: justo tras el clic la pagina anterior puede seguir
        // reportando readyState=complete y el spinner todavia puede no aparecer.
        dormir(5000);

        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
                    "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        } catch (Exception ignored) {
        }

        esperarSinSpinnerOAceptarConfirmacion(driver, 15);
        dormir(1200);
    }

    /** Espera a que termine swal2; si hay popup de confirmacion, lo acepta. */
    private void esperarSinSpinnerOAceptarConfirmacion(WebDriver driver, int timeoutSeg) {
        long fin = System.currentTimeMillis() + timeoutSeg * 1000L;
        long sinSwalDesde = 0L;

        while (System.currentTimeMillis() < fin) {
            if (aceptarConfirmacionSweetAlert(driver)) {
                sinSwalDesde = 0L;
                dormir(700);
                continue;
            }

            boolean haySwal = haySweetAlertVisible(driver);
            if (!haySwal) {
                if (sinSwalDesde == 0L) {
                    sinSwalDesde = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - sinSwalDesde >= 1200L) {
                    driver.switchTo().defaultContent();
                    return;
                }
            } else {
                sinSwalDesde = 0L;
            }

            dormir(200);
        }
        driver.switchTo().defaultContent();
    }

    private boolean aceptarConfirmacionSweetAlert(WebDriver driver) {
        driver.switchTo().defaultContent();
        if (aceptarConfirmacionEnContextoActual(driver)) return true;

        int totalFrames = driver.findElements(By.tagName("iframe")).size();
        for (int i = 0; i < totalFrames; i++) {
            try {
                driver.switchTo().defaultContent();
                WebElement frame = driver.findElements(By.tagName("iframe")).get(i);
                driver.switchTo().frame(frame);
                if (aceptarConfirmacionEnContextoActual(driver)) return true;
            } catch (Exception ignored) {
                // El iframe pudo desaparecer durante la recarga; continuar con el siguiente.
            }
        }

        driver.switchTo().defaultContent();
        return false;
    }

    private boolean aceptarConfirmacionEnContextoActual(WebDriver driver) {
        WebElement popup = driver.findElements(SWAL_POPUP)
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
        if (popup == null) return false;

        WebElement confirmacion = driver.findElements(SWAL_CONFIRM)
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
        if (confirmacion == null) return false;

        String textoPopup = popup.getText().replace("\n", " ").trim();
        try {
            confirmacion.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmacion);
        }
        System.out.println("[GuardarCaso] SweetAlert confirmado tras guardar: " + textoPopup);
        return true;
    }

    private boolean haySweetAlertVisible(WebDriver driver) {
        driver.switchTo().defaultContent();
        if (haySweetAlertVisibleEnContextoActual(driver)) return true;

        int totalFrames = driver.findElements(By.tagName("iframe")).size();
        for (int i = 0; i < totalFrames; i++) {
            try {
                driver.switchTo().defaultContent();
                WebElement frame = driver.findElements(By.tagName("iframe")).get(i);
                driver.switchTo().frame(frame);
                if (haySweetAlertVisibleEnContextoActual(driver)) return true;
            } catch (Exception ignored) {
                // El iframe pudo desaparecer durante la recarga; continuar con el siguiente.
            }
        }

        driver.switchTo().defaultContent();
        return false;
    }

    private boolean haySweetAlertVisibleEnContextoActual(WebDriver driver) {
        boolean popup = driver.findElements(SWAL_POPUP)
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .isPresent();
        boolean loader = driver.findElements(SWAL_LOADER)
                .stream()
                .anyMatch(WebElement::isDisplayed);
        return popup || loader;
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
