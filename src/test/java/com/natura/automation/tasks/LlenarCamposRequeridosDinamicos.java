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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Red de seguridad genérica: detecta y llena CUALQUIER campo obligatorio vacío que aparezca en
 * el formulario del caso, sin necesitar conocer su nombre de antemano. Las combinaciones de
 * niveles habilitan distintos formularios extra (Datos NC, Planeamiento Comercial, Transportadora,
 * y potencialmente otros aún no vistos) — en vez de agregar una clase nueva por cada uno, este
 * task cubre los que ya tienen manejador dedicado (los deja intactos, ya estarán llenos) y
 * cualquier campo requerido NUEVO que aparezca sin manejador propio.
 *
 * Se ejecuta al FINAL, después de los formularios específicos, justo antes de intentar guardar.
 */
public class LlenarCamposRequeridosDinamicos implements Task {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final Random RANDOM = new Random();

    public static Performable siAplica() {
        return instrumented(LlenarCamposRequeridosDinamicos.class);
    }

    @Override
    @Step("Diligenciar campos requeridos vacíos detectados dinámicamente")
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));

        int textosLlenados = llenarTextosRequeridosVacios(driver);
        int selectsNativosLlenados = llenarSelectsNativosRequeridosVacios(driver);
        int osSelectsLlenados = llenarOsSelectsRequeridosVacios(driver);

        int total = textosLlenados + selectsNativosLlenados + osSelectsLlenados;
        if (total > 0) {
            System.out.println("[CamposDinamicos] Se llenaron " + textosLlenados + " campo(s) de texto/número, " +
                    selectsNativosLlenados + " lista(s) <select> y " + osSelectsLlenados +
                    " os-select adicional(es) que no tenían manejador propio.");
        } else {
            System.out.println("[CamposDinamicos] No se detectaron campos requeridos vacíos adicionales.");
        }

        driver.switchTo().defaultContent();
    }

    /**
     * Busca inputs requeridos, visibles y vacíos (texto, número, email, teléfono, textarea —
     * no hidden, checkbox ni radio) y les pone un valor genérico no vacío. Cubre campos de
     * cualquier tipo, no solo texto plano.
     */
    private int llenarTextosRequeridosVacios(WebDriver driver) {
        By camposRequeridos = By.cssSelector(
                "input[aria-required='true']:not([type='hidden']):not([type='checkbox']):not([type='radio']):not([type='file']), " +
                "textarea[aria-required='true']");
        List<WebElement> campos = driver.findElements(camposRequeridos).stream()
                .filter(WebElement::isDisplayed)
                .filter(el -> el.getAttribute("value") == null || el.getAttribute("value").trim().isEmpty())
                .collect(Collectors.toList());

        int llenados = 0;
        for (WebElement el : campos) {
            String nombreCampo = extraerNombreCampo(el.getAttribute("name"));
            String tipo = el.getAttribute("type");
            String valor = "number".equalsIgnoreCase(tipo) ? randomDigitos(6) : "AUTO" + randomDigitos(6);
            try {
                scrollToCenter(driver, el);
                try {
                    el.click();
                    el.sendKeys(valor);
                } catch (Exception clickEx) {
                    ((JavascriptExecutor) driver).executeScript(
                            "var el=arguments[0],v=arguments[1];" +
                            "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
                            "s.call(el,v);" +
                            "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                            "el.dispatchEvent(new Event('change',{bubbles:true}));", el, valor);
                }
                System.out.println("  [CamposDinamicos] (nuevo) " + nombreCampo + " = " + valor);
                com.natura.automation.util.ReportePaso.valor(nombreCampo, valor);
                llenados++;
            } catch (Exception e) {
                System.err.println("  [CamposDinamicos] ERROR llenando " + nombreCampo + ": " + e.getMessage());
            }
        }
        return llenados;
    }

    /**
     * Busca listas <select> nativas requeridas (no las envueltas por Choices.js, que quedan
     * ocultas con hidden="" y ref="selectContainer" — esas ya las maneja seleccionarChoices en
     * el formulario de Cliente; aquí son selects "planos" que puede usar el formulario del caso)
     * sin opción real elegida, y selecciona una opción válida al azar (distinta del placeholder).
     */
    private int llenarSelectsNativosRequeridosVacios(WebDriver driver) {
        By selectsRequeridos = By.cssSelector("select[aria-required='true']:not([hidden])");
        List<WebElement> selects = driver.findElements(selectsRequeridos).stream()
                .filter(WebElement::isDisplayed)
                .collect(Collectors.toList());

        int llenados = 0;
        for (WebElement select : selects) {
            String nombreCampo = extraerNombreCampo(select.getAttribute("name"));
            try {
                List<WebElement> opciones = select.findElements(By.tagName("option")).stream()
                        .filter(o -> o.getAttribute("value") != null && !o.getAttribute("value").trim().isEmpty())
                        .collect(Collectors.toList());
                if (opciones.isEmpty()) continue;

                String seleccionActual = select.getAttribute("value");
                if (seleccionActual != null && !seleccionActual.trim().isEmpty()) continue; // ya tiene valor

                WebElement opcion = opciones.get(RANDOM.nextInt(opciones.size()));
                String valor = opcion.getText().trim();
                new org.openqa.selenium.support.ui.Select(select).selectByVisibleText(opcion.getText());

                System.out.println("  [CamposDinamicos] (nueva lista) " + nombreCampo + " = " + valor);
                com.natura.automation.util.ReportePaso.valor(nombreCampo, valor);
                llenados++;
            } catch (Exception e) {
                System.err.println("  [CamposDinamicos] ERROR en lista " + nombreCampo + ": " + e.getMessage());
            }
        }
        return llenados;
    }

    /**
     * Busca os-select requeridos que sigan mostrando el placeholder "Elige una opción" y
     * selecciona una opción real al azar.
     */
    private int llenarOsSelectsRequeridosVacios(WebDriver driver) {
        By controlesRequeridos = By.cssSelector(
                ".formio-component.required .os-dropdown-control, " +
                ".formio-component-label-hidden.required .os-dropdown-control");
        List<WebElement> controles = driver.findElements(controlesRequeridos).stream()
                .filter(WebElement::isDisplayed)
                .filter(el -> el.getText().toLowerCase().contains("elige una opción") ||
                              el.getText().toLowerCase().contains("elige una opcion"))
                .collect(Collectors.toList());

        int llenados = 0;
        for (WebElement control : controles) {
            String etiqueta = etiquetaDe(control);
            try {
                scrollToCenter(driver, control);
                clickReal(driver, control);

                By opcionesBy = By.xpath(
                        "ancestor::div[contains(@class,'os-dropdown')][1]//ul[contains(@class,'os-dropdown-list')]/li");
                List<WebElement> opciones = esperarOpcionesCargadas(driver, control, opcionesBy, 15);
                if (opciones.isEmpty()) {
                    System.err.println("  [CamposDinamicos] " + etiqueta + ": opciones no cargaron, se omite.");
                    continue;
                }

                WebElement elegido = opciones.get(RANDOM.nextInt(opciones.size()));
                String valor = elegido.getText().trim();
                scrollToCenter(driver, elegido);
                try {
                    elegido.click();
                } catch (Exception e) {
                    dispatchMouseClick(driver, elegido);
                }
                dormir(300);

                System.out.println("  [CamposDinamicos] (nuevo os-select) " + etiqueta + " = " + valor);
                com.natura.automation.util.ReportePaso.valor(etiqueta, valor);
                llenados++;
            } catch (Exception e) {
                System.err.println("  [CamposDinamicos] ERROR en os-select " + etiqueta + ": " + e.getMessage());
            }
        }
        return llenados;
    }

    private List<WebElement> esperarOpcionesCargadas(WebDriver driver, WebElement control, By opcionesRelativo, int timeoutSeg) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeg)).until(d ->
                    control.findElements(opcionesRelativo).stream()
                            .anyMatch(o -> opcionReal(o.getText())));
        } catch (Exception ignored) {}
        try {
            return control.findElements(opcionesRelativo).stream()
                    .filter(o -> opcionReal(o.getText()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    private boolean opcionReal(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        return !t.isEmpty() && !t.contains("cargando") && !t.contains("loading");
    }

    private String etiquetaDe(WebElement control) {
        try {
            WebElement label = control.findElement(By.xpath("preceding-sibling::label[1]"));
            return label.getText().trim();
        } catch (Exception e) {
            return "os-select";
        }
    }

    private String extraerNombreCampo(String nameAttr) {
        if (nameAttr == null) return "campo";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("data\\[([^\\]]+)\\]").matcher(nameAttr);
        return m.find() ? m.group(1) : nameAttr;
    }

    private void clickReal(WebDriver driver, WebElement el) {
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void dispatchMouseClick(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var el=arguments[0];" +
                    "['pointerdown','mousedown','mouseup','click'].forEach(function(t){" +
                    "  el.dispatchEvent(new MouseEvent(t,{bubbles:true,cancelable:true,view:window}));" +
                    "});", el);
        } catch (Exception ignored) {}
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

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
