package com.natura.automation.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Maneja el modal "Ya tienes una sesión activa" que la app muestra cuando una cuenta ya tenía
 * sesión abierta (de una corrida anterior que solo cerró el navegador, sin hacer logout —
 * el backend rastrea la sesión por CUENTA DE USUARIO, no por navegador/cookies/caché local).
 *
 * Sin cerrarlo, el flujo queda bloqueado indefinidamente. Puede aparecer en más de un punto
 * (tras el login, o al forzar navegación a /agent), así que este helper se llama desde varios
 * lugares del flujo en vez de una sola vez.
 */
public final class ModalSesionActiva {

    private static final By BOTON_CONTINUAR = By.xpath(
            "//button[contains(normalize-space(.), 'continuar aqu')]");

    private ModalSesionActiva() {}

    /** Revisa y cierra el modal si está presente, esperando hasta timeoutSeg a que aparezca. */
    public static void manejar(WebDriver driver, int timeoutSeg) {
        try {
            WebElement boton;
            if (timeoutSeg > 0) {
                boton = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeg))
                        .until(d -> {
                            WebElement el = d.findElement(BOTON_CONTINUAR);
                            return el.isDisplayed() ? el : null;
                        });
            } else {
                boton = driver.findElement(BOTON_CONTINUAR);
                if (!boton.isDisplayed()) return;
            }
            boton.click();
            System.out.println("  [ModalSesionActiva] Modal 'sesión activa' detectado — se cerró la sesión anterior.");
            dormir(500);
        } catch (Exception ignored) {
            // No apareció el modal: flujo normal.
        }
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
