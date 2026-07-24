package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Alterna entre "Gestionar" y "Liberar" hasta que aparezca "Crear Caso".
 * Cuando "Crear Caso" ya está visible, pulsa "Crear Caso" y luego "Usar cliente seleccionado".
 */
public class HacerClicGestionarOCrearCaso implements Task {

    private static final By BTN_GESTIONAR = By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Gestionar']");
    private static final By BTN_LIBERAR = By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Liberar']");
    private static final By BTN_CREAR_CASO = By.xpath("//button[@id='btn_interaction_create_case' and normalize-space()='Crear Caso']");
    private static final By BTN_USAR_CLIENTE_SELECCIONADO = By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Usar cliente seleccionado']");

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait waitLargo = new WebDriverWait(driver, Duration.ofSeconds(10));

        for (int intento = 1; intento <= 25; intento++) {
            boolean visibleCrearCaso = estaVisible(driver, BTN_CREAR_CASO);
            boolean visibleGestionar = estaVisible(driver, BTN_GESTIONAR);
            boolean visibleLiberar = estaVisible(driver, BTN_LIBERAR);

            System.out.println(String.format("[HacerClicGestionarOCrearCaso] Intento=%d | CrearCaso=%b | Gestionar=%b | Liberar=%b",
                    intento, visibleCrearCaso, visibleGestionar, visibleLiberar));

            // 1. Condición de salida principal
            if (visibleCrearCaso) {
                completarFlujoCrearCaso(actor, driver);
                return;
            }

            // 2. Identificar qué acción ejecutar (Gestionar o Liberar)
            Optional<By> accion = accionVisible(driver);
            if (accion.isPresent()) {
                By botonActual = accion.get();
                By botonEsperado = BTN_GESTIONAR.equals(botonActual) ? BTN_LIBERAR : BTN_GESTIONAR;

                System.out.println("[HacerClicGestionarOCrearCaso] Ejecutando clic en: " + nombreAccion(botonActual));
                
                // Hace el clic y asegura que la UI reaccione
                boolean cambioExitoso = hacerClicYAsegurarCambio(driver, botonActual, botonEsperado);

                if (estaVisible(driver, BTN_CREAR_CASO)) {
                    completarFlujoCrearCaso(actor, driver);
                    return;
                }

                if (!cambioExitoso) {
                    System.out.println("[HacerClicGestionarOCrearCaso] El estado no cambió tras el clic. Reintentando en la siguiente vuelta...");
                }
                
                dormir(500); // Breve pausa de estabilización de la interfaz
                continue;
            }

            // 3. Si por alguna razón no se ve ningún botón, esperamos a que alguno aparezca
            System.out.println("[HacerClicGestionarOCrearCaso] Ningún botón visible, esperando actualización del DOM...");
            esperarVisible(waitLargo, BTN_GESTIONAR, BTN_LIBERAR, BTN_CREAR_CASO);
        }

        // Validación final tras agotar intentos
        if (estaVisible(driver, BTN_CREAR_CASO)) {
            completarFlujoCrearCaso(actor, driver);
            return;
        }

        throw new AssertionError("No apareció el botón 'Crear Caso' tras alternar entre Gestionar y Liberar.");
    }

    private <T extends Actor> void completarFlujoCrearCaso(T actor, WebDriver driver) {
        System.out.println("[HacerClicGestionarOCrearCaso] 'Crear Caso' visible. Finalizando interacción...");
        clicarClickable(driver, BTN_CREAR_CASO);

        // Si la interacción ya tiene un cliente asociado, aparece el modal "Usar cliente
        // seleccionado". Si NO hay cliente creado para esta interacción, el modal nunca
        // aparece y hay que crear el cliente por el flujo normal antes de continuar.
        WebDriverWait waitModalCliente = new WebDriverWait(driver, Duration.ofSeconds(6));
        boolean modalClienteVisible;
        try {
            modalClienteVisible = waitModalCliente.until(d -> estaVisible(d, BTN_USAR_CLIENTE_SELECCIONADO));
        } catch (Exception e) {
            modalClienteVisible = false;
        }

        if (modalClienteVisible) {
            System.out.println("[HacerClicGestionarOCrearCaso] Cliente ya existente detectado. Usando 'Usar cliente seleccionado'.");
            clicarClickable(driver, BTN_USAR_CLIENTE_SELECCIONADO);
        } else {
            System.out.println("[HacerClicGestionarOCrearCaso] No hay cliente creado para esta interacción. Creando cliente con datos aleatorios...");
            actor.attemptsTo(EjecutarCrearCliente.conDatosAleatorios());
            // El swal2 de éxito de creación queda abierto y tapa el botón "Nuevo Caso" que
            // busca el siguiente step (EjecutarCrearCaso); hay que confirmarlo aquí igual
            // que en el flujo normal (escenario "Datos Aleatorios").
            actor.attemptsTo(ValidarClienteCreado.ahora());
        }
    }

    private boolean hacerClicYAsegurarCambio(WebDriver driver, By botonAClicar, By botonEsperado) {
        // Esperar a que el botón esté REALMENTE clickeable antes de disparar el clic.
        // Sin esta espera, un clic sobre un botón todavía en animación/detrás de overlay
        // no surte efecto, y el waitCorto de abajo agota su timeout completo sin motivo
        // (efecto observado: demoras largas entre alternar Gestionar/Liberar).
        try {
            WebElement boton = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(botonAClicar));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", boton);
            try {
                boton.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", boton);
            }
        } catch (Exception e) {
            // El botón dejó de estar presente justo antes del clic (ya cambió de estado): seguir.
        }

        // Espera corta para verificar que el DOM cambió a Liberar/Gestionar O apareció Crear Caso
        WebDriverWait waitCorto = new WebDriverWait(driver, Duration.ofSeconds(2));
        try {
            return waitCorto.until(d ->
                estaVisible(d, BTN_CREAR_CASO) || estaVisible(d, botonEsperado)
            );
        } catch (Exception e) {
            return false;
        }
    }

    private Optional<By> accionVisible(WebDriver driver) {
        if (estaVisible(driver, BTN_GESTIONAR)) {
            return Optional.of(BTN_GESTIONAR);
        }
        if (estaVisible(driver, BTN_LIBERAR)) {
            return Optional.of(BTN_LIBERAR);
        }
        return Optional.empty();
    }

    private String nombreAccion(By locator) {
        if (BTN_GESTIONAR.equals(locator)) return "Gestionar";
        if (BTN_LIBERAR.equals(locator)) return "Liberar";
        return locator.toString();
    }

    private void clicarClickable(WebDriver driver, By locator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement elemento = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", elemento);
        try {
            elemento.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
        }
    }

    private boolean estaVisible(WebDriver driver, By locator) {
        List<WebElement> elementos = driver.findElements(locator);
        return !elementos.isEmpty() && elementos.get(0).isDisplayed();
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean esperarVisible(WebDriverWait wait, By... locators) {
        try {
            return wait.until(driver -> {
                for (By locator : locators) {
                    if (estaVisible(driver, locator)) {
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    public static Performable nuevo() {
        return instrumented(HacerClicGestionarOCrearCaso.class);
    }
}