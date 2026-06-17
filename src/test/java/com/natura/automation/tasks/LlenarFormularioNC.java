package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.thucydides.core.annotations.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Llena el formulario "Datos NC" SOLO si está visible (algunas clasificaciones lo habilitan).
 * Si no aparece, no hace nada y el flujo continúa. Así no se necesitan exclusiones de niveles.
 *
 * Campos (os-select personalizado):
 *   - Tipo NC            (.formio-component-tipo_nc)            -> opción aleatoria
 *   - # NC               (input[name='data[numero_nc]'])        -> número aleatorio
 *   - Gestión Cierre NCS (.formio-component-gestion_cierre_ncs) -> opción aleatoria
 */
public class LlenarFormularioNC implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final Random RANDOM = new Random();

    public static Performable siAplica() {
        return instrumented(LlenarFormularioNC.class);
    }

    @Override
    @Step("Diligenciar el formulario Datos NC si está visible")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));

        if (!estaVisible(driver, ".formio-component-tipo_nc")) {
            System.out.println("[FormularioNC] No visible — se omite y continúa el flujo.");
            driver.switchTo().defaultContent();
            return;
        }

        System.out.println("[FormularioNC] Visible — diligenciando campos requeridos.");
        seleccionarOsSelectAleatorio(driver, "tipo_nc");
        escribirTexto(driver, "input[name='data[numero_nc]']", randomDigitos(6));
        seleccionarOsSelectAleatorio(driver, "gestion_cierre_ncs");

        driver.switchTo().defaultContent();
    }

    private boolean estaVisible(WebDriver driver, String css) {
        return driver.findElements(By.cssSelector(css)).stream().anyMatch(WebElement::isDisplayed);
    }

    /** Abre un os-select por su componente y elige una opción al azar de la lista. */
    private void seleccionarOsSelectAleatorio(WebDriver driver, String campo) {
        try {
            By controlBy = By.cssSelector(".formio-component-" + campo + " .os-dropdown-control");
            By opcionesBy = By.cssSelector(".formio-component-" + campo + " .os-dropdown-list li");

            By searchBy = By.cssSelector(".formio-component-" + campo + " .os-dropdown-search");

            // Abrir el dropdown (clic nativo, fallback JS).
            WebElement control = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(controlBy));
            scrollToCenter(driver, control);
            clickReal(driver, control);

            // Esperar a que las opciones REALMENTE carguen (Tipo NC viene del backend y demora).
            List<WebElement> items = esperarOpcionesCargadas(driver, opcionesBy, 20);
            if (items.isEmpty()) {
                System.err.println("  [FormularioNC] " + campo + ": las opciones no cargaron.");
                return;
            }

            String valor = items.get(RANDOM.nextInt(items.size())).getText().trim();

            // Filtrar con el buscador para traer la opción al frente (y dar tiempo a renderizar).
            try {
                WebElement search = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(searchBy));
                search.clear();
                search.sendKeys(valor);
                dormir(500);
            } catch (Exception ignored) {}

            // Clic NATIVO (evento confiable) sobre la opción; fallbacks: eventos de mouse / Enter.
            By liBy = By.xpath("//*[contains(@class,'formio-component-" + campo + "')]" +
                    "//ul[contains(@class,'os-dropdown-list')]/li[normalize-space()=" + xpathLiteral(valor) + "]");
            try {
                WebElement li = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.elementToBeClickable(liBy));
                scrollToCenter(driver, li);
                try {
                    li.click();
                } catch (Exception e) {
                    dispatchMouseClick(driver, li);
                }
            } catch (Exception e) {
                try { driver.findElement(searchBy).sendKeys(Keys.ENTER); } catch (Exception ignored) {}
            }

            if (osSelectTieneValor(driver, campo)) {
                System.out.println("  [FormularioNC] " + campo + " = " + valor);
            } else {
                System.err.println("  [FormularioNC] " + campo + ": no se pudo confirmar la selección de '" + valor + "'.");
            }
        } catch (Exception e) {
            System.err.println("  [FormularioNC] ERROR en " + campo + ": " + e.getMessage());
        }
    }

    /**
     * Espera a que las opciones del dropdown REALMENTE carguen: que ya no haya un indicador
     * de "Cargando..." y que exista al menos una opción real visible con texto.
     */
    private List<WebElement> esperarOpcionesCargadas(WebDriver driver, By opcionesBy, int timeoutSeg) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeg)).until(d -> {
                List<WebElement> visibles = d.findElements(opcionesBy).stream()
                        .filter(WebElement::isDisplayed)
                        .collect(Collectors.toList());
                boolean hayCargando = visibles.stream().anyMatch(e -> esCargando(e.getText()));
                boolean hayReal = visibles.stream().anyMatch(e -> !esCargando(e.getText()));
                return hayReal && !hayCargando;
            });
        } catch (Exception ignored) {}
        return driver.findElements(opcionesBy).stream()
                .filter(WebElement::isDisplayed)
                .filter(e -> !esCargando(e.getText()))
                .collect(Collectors.toList());
    }

    /** Detecta opciones placeholder/indicadores de carga que no deben seleccionarse. */
    private boolean esCargando(String texto) {
        String s = texto == null ? "" : texto.trim().toLowerCase();
        return s.isEmpty() || s.contains("cargando") || s.contains("Cargando...")|| s.contains("loading") || s.contains("cargar");
    }

    private void clickReal(WebDriver driver, WebElement el) {
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** True si el control del os-select ya muestra un valor (no "Elige una opción"). */
    private boolean osSelectTieneValor(WebDriver driver, String campo) {
        try {
            String txt = driver.findElement(By.cssSelector(
                    ".formio-component-" + campo + " .os-dropdown-control")).getText().trim().toLowerCase();
            return !txt.isEmpty() && !txt.contains("elige una opción") && !txt.contains("elige una opcion");
        } catch (Exception e) {
            return false;
        }
    }

    /** Dispara la secuencia de eventos de mouse para componentes React/custom. */
    private void dispatchMouseClick(WebDriver driver, WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "var el=arguments[0];" +
                "['pointerdown','mousedown','mouseup','click'].forEach(function(t){" +
                "  el.dispatchEvent(new MouseEvent(t,{bubbles:true,cancelable:true,view:window}));" +
                "});", el);
    }

    /** Construye un literal XPath seguro para textos con comillas. */
    private String xpathLiteral(String s) {
        if (!s.contains("'")) return "'" + s + "'";
        if (!s.contains("\"")) return "\"" + s + "\"";
        return "concat('" + s.replace("'", "',\"'\",'") + "')";
    }

    private void escribirTexto(WebDriver driver, String css, String valor) {
        try {
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
            scrollToCenter(driver, el);
            try {
                el.click();
                el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                el.sendKeys(valor);
            } catch (Exception clickEx) {
                ((JavascriptExecutor) driver).executeScript(
                        "var el=arguments[0],v=arguments[1];" +
                        "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                        "s.call(el,v);" +
                        "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                        "el.dispatchEvent(new Event('change',{bubbles:true}));", el, valor);
            }
            System.out.println("  [FormularioNC] # NC = " + valor);
        } catch (Exception e) {
            System.err.println("  [FormularioNC] ERROR en " + css + ": " + e.getMessage());
        }
    }

    private String randomDigitos(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    private void scrollToCenter(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }
}
