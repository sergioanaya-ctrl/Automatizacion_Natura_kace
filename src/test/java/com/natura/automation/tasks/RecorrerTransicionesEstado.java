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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Recorre la máquina de estados del caso si es interactuable.
 * Si el caso no pertenece al asesor, detecta que los controles o el botón guardar
 * están deshabilitados y finaliza pacíficamente (Todo OK) para validar solo la creación.
 */
public class RecorrerTransicionesEstado implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final By ESTADOS_DISPONIBLES = By.cssSelector(
            ".formio-component-kaceStates .states__content button.btn-outline-secondary");
            
    // Localizador del botón guardar (Asegúrate de que coincida con el que usas en GuardarCaso.java)
    private static final By BOTON_GUARDAR = By.xpath("//button[contains(@class, 'kace-floating-submit')]");

    private static final List<String> PRIORITARIOS = Arrays.asList("EN GESTION", "SOLUCIONADO");
    private static final List<String> TERMINALES = Arrays.asList("SOLUCIONADO");
    private static final int MAX_TRANSICIONES = 10; 

    public static Performable hastaFinalizar() {
        return instrumented(RecorrerTransicionesEstado.class);
    }

    @Override
    @Step("Recorrer las transiciones de estado del caso hasta finalizar")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        java.util.Set<String> visitados = new java.util.HashSet<>();

        for (int i = 1; i <= MAX_TRANSICIONES; i++) {
            entrarIframe(driver);

            // 1) CONTROL DE SEGURIDAD: Verificar si el botón Guardar está bloqueado/deshabilitado
            List<WebElement> btnGuardarList = driver.findElements(BOTON_GUARDAR);
            if (!btnGuardarList.isEmpty()) {
                WebElement btnGuardar = btnGuardarList.get(0);
                if (!esInteractuable(btnGuardar)) {
                    System.out.println("[Estados] El botón 'Guardar' está deshabilitado. " +
                            "El caso no pertenece a este asesor. Finalizando ciclo de estados con ÉXITO.");
                    driver.switchTo().defaultContent();
                    return; // Sale limpio ("Todo OK") y continúa a la validación visual
                }
            }

            // 2) Obtener estados disponibles y filtrar solo los que REALMENTE son interactuables
            List<WebElement> disponibles = driver.findElements(ESTADOS_DISPONIBLES).stream()
                    .filter(this::esInteractuable) // <-- Filtro inteligente para ignorar botones bloqueados
                    .collect(Collectors.toList());

            if (disponibles.isEmpty()) {
                System.out.println("[Estados] Sin estados interactuables disponibles (caso no asignado a este asesor o ya finalizado). " +
                        "Flujo de estados terminado con ÉXITO (" + (i - 1) + " transiciones).");
                driver.switchTo().defaultContent();
                return; // Sale limpio ("Todo OK")
            }

            System.out.println("[Estados] Disponibles para interactuar: " + disponibles.stream()
                    .map(b -> b.getText().trim()).collect(Collectors.toList()));

            WebElement elegido = elegirEstado(disponibles, visitados);
            if (elegido == null) {
                driver.switchTo().defaultContent();
                throw new RuntimeException("[Estados] El caso quedó oscilando entre estados ya visitados " +
                        visitados + " sin alcanzar un estado final — se detiene la automatización.");
            }

            String estado = elegido.getText().trim();
            visitados.add(estado.toLowerCase());
            scrollToCenter(driver, elegido);
            clickRobusto(driver, elegido);
            System.out.println("[Estados] Transición " + i + " -> " + estado);

            driver.switchTo().defaultContent();
            actor.attemptsTo(GuardarCaso.ahora()); // Ejecuta el clic en Guardar del caso interactuable

            if (esTerminal(estado)) {
                System.out.println("[Estados] Estado final '" + estado + "' alcanzado — flujo de estados finalizado.");
                return;
            }
        }
        driver.switchTo().defaultContent();
        throw new RuntimeException("[Estados] Se alcanzó el tope de " + MAX_TRANSICIONES +
                " transiciones sin llegar a un estado final.");
    }

    /**
     * Evalúa minuciosamente si un elemento web está realmente activo para el usuario.
     * Detecta deshabilitados nativos, por atributo HTML o por clases de frameworks de UI.
     */
    private boolean esInteractuable(WebElement el) {
        try {
            if (!el.isDisplayed() || !el.isEnabled()) {
                return false;
            }
            // Validar atributo HTML 'disabled'
            String disabledAttr = el.getAttribute("disabled");
            if (disabledAttr != null && !disabledAttr.equals("false")) {
                return false;
            }
            // Validar clases CSS comunes de bloqueo (ej: 'disabled', 'btn-disabled')
            String cssClass = el.getAttribute("class");
            if (cssClass != null && (cssClass.contains("disabled") || cssClass.contains("--disabled"))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false; // Si se pierde la referencia o falla, no se arriesga a interactuar
        }
    }

    private WebElement elegirEstado(List<WebElement> disponibles, java.util.Set<String> visitados) {
        for (String prioritario : PRIORITARIOS) {
            for (WebElement b : disponibles) {
                if (b.getText().trim().equalsIgnoreCase(prioritario)) {
                    return b;
                }
            }
        }
        WebElement noVisitadoNoTerminal = disponibles.stream()
                .filter(b -> !visitados.contains(b.getText().trim().toLowerCase()))
                .filter(b -> !esTerminal(b.getText().trim()))
                .findFirst().orElse(null);
        if (noVisitadoNoTerminal != null) return noVisitadoNoTerminal;

        return disponibles.stream()
                .filter(b -> !visitados.contains(b.getText().trim().toLowerCase()))
                .findFirst().orElse(null);
    }

    private boolean esTerminal(String estado) {
        return TERMINALES.stream().anyMatch(t -> t.equalsIgnoreCase(estado));
    }

    private void entrarIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));
    }

    private void scrollToCenter(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }

    private void clickRobusto(WebDriver driver, WebElement el) {
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}