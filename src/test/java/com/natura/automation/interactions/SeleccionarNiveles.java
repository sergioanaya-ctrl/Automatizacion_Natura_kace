package com.natura.automation.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Diligencia el componente "Clasificaciones" en cascada (Nivel 3 a 6).
 *
 * Por ahora solo se recorren las ramas CONFIRMADAS de extremo a extremo (hasta Nivel 6):
 *   - Nivel 3: ACOMPANAMIENTO, DESARROLLO Y RECONOCIMIENTO  /  ATRACCION, CADASTRO E INICIO
 *   - Nivel 4: CONSULTA
 *   - Niveles 5 y 6: dinámico (lo que la app muestre para esa rama).
 *
 * Cuando se tenga la matriz completa de todos los niveles, habilitar el resto de ramas
 * (ver NIVEL3_PENDIENTES) y/o ampliar las preferencias por nivel.
 * Niveles 1 y 2 vienen fijos y deshabilitados (COLOMBIA, CEN).
 */
public class SeleccionarNiveles implements Interaction {

    private static final By FORM_IFRAME = By.id("form_onescript_iframe");
    private static final Random RANDOM = new Random();
    private static final int NIVEL_INICIAL = 3;
    private static final int NIVEL_FINAL = 6;

    // Ramas de Nivel 3 confirmadas (cascadean completas hasta Nivel 6).
    private static final List<String> NIVEL3_CONFIRMADOS = Arrays.asList(
            "ACOMPANAMIENTO, DESARROLLO Y RECONOCIMIENTO",
            "ATRACCION, CADASTRO E INICIO");

    // Nivel 4 confirmado para esas ramas.
    private static final List<String> NIVEL4_PREFERIDO = Arrays.asList("CONSULTA");

    // PENDIENTE (habilitar con la matriz completa): el resto de ramas de Nivel 3:
    //   "CAPTACION DE PEDIDOS", "DESPACHO Y ENTREGA DE PEDIDO", "ENTRENAMIENTO Y HERRAMIENTAS",
    //   "IMPRODUCTIVO", "PAGO DE PEDIDO", "RITUALES Y EVENTOS", "SERVICIO POST-VENTA"

    // Opciones (en cualquier nivel) que NO se deben seleccionar porque abren formularios extra
    // obligatorios aún no soportados (Datos NC, Datos Transportadora, etc.). Se comparan
    // case-insensitive contra el texto de la opción. Agregar aquí las que el reporte muestre como fallidas.
    // Ya no se excluyen ramas: el formulario "Datos NC" se llena condicionalmente
    // (ver LlenarFormularioNC). Si algún otro formulario extra bloqueara el guardado,
    // se puede volver a agregar aquí su opción de nivel.
    private static final List<String> OPCIONES_EXCLUIDAS = java.util.Collections.emptyList();

    public static Performable aleatorios() {
        return instrumented(SeleccionarNiveles.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        ensureIframe(driver);

        // Gate obligatorio: Nivel 3 DEBE estar habilitado antes de iniciar la cascada.
        // EjecutarCrearCaso ya esperó, pero el re-enganche al iframe en ensureIframe puede
        // coincidir con un re-render del formulario; esta espera lo cubre.
        System.out.println("[Niveles] Esperando que Nivel 3 esté habilitado...");
        if (!esperarNivelSeleccionable(driver, NIVEL_INICIAL)) {
            throw new RuntimeException("[Niveles] Nivel 3 nunca se habilitó tras cambiar al iframe — " +
                    "no hay clasificaciones disponibles para este caso.");
        }
        System.out.println("[Niveles] Nivel 3 habilitado. Iniciando cascada.");
        dormir(500);

        for (int nivel = NIVEL_INICIAL; nivel <= NIVEL_FINAL; nivel++) {
            // Nivel 3 ya fue confirmado arriba; para N4-N6 esperar habilitación normal
            // (fin de cascada si el nivel no aplica para la rama elegida).
            if (nivel > NIVEL_INICIAL && !esperarNivelSeleccionable(driver, nivel)) {
                System.out.println("[Niveles] Nivel " + nivel + " no aplica para esta rama — fin de la cascada.");
                break;
            }
            // El control SÍ existe y está habilitado (el nivel aplica), pero si sus opciones
            // nunca cargan (lento o roto) NO se debe seguir adelante en silencio: sin clasificación
            // real, no habrá estados disponibles más adelante y el test fallaría minutos después
            // con un error que no apunta a la causa. Se falla aquí mismo, de inmediato.
            List<String> preferidos = preferidosDe(nivel);
            if (!seleccionarEnNivel(driver, nivel, preferidos)) {
                throw new RuntimeException("[Niveles] Nivel " + nivel + " está habilitado pero no se pudo " +
                        "seleccionar ninguna opción (no cargaron a tiempo o el clic no registró). " +
                        "Se detiene la automatización aquí en vez de continuar sin clasificación.");
            }

            // Pausa breve tras confirmar la selección: el control del SIGUIENTE nivel puede tardar
            // en renderizarse/habilitarse justo después de elegir en este. Sin esta pausa, se pasa
            // demasiado rápido a comprobar el siguiente nivel y a veces "no aparece" solo porque
            // aún no había terminado de renderizar (no porque la cascada realmente termine ahí),
            // dejando el caso sub-clasificado y sin estados disponibles más adelante.
            dormir(1000);
        }

        System.out.println("[Niveles] Niveles completados.");
    }

    /**
     * Valores preferidos por nivel (vacío = cualquier opción al azar).
     * Actualmente TODOS los niveles eligen al azar (N3 a N6) para variar las ramas y probar
     * la máquina de estados con distintas clasificaciones.
     * Para acotar de nuevo a ramas confirmadas, devolver NIVEL3_CONFIRMADOS / NIVEL4_PREFERIDO.
     */
    private List<String> preferidosDe(int nivel) {
        return Collections.emptyList();
    }

    /**
     * Espera a que el control del nivel exista y esté habilitado. Antes esperaba solo 8s, y bajo
     * carga eso alcanzaba a confundirse con "el nivel no aplica para esta rama" cuando en realidad
     * el control simplemente tardaba más en renderizarse — dejando el caso sub-clasificado (menos
     * niveles de los que realmente correspondían) y sin estados disponibles más adelante. Ahora
     * espera hasta 20s, dando tiempo real antes de concluir que la cascada terminó ahí.
     */
    private boolean esperarNivelSeleccionable(WebDriver driver, int nivel) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.presenceOfElementLocated(controlHabilitado(nivel)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Abre el nivel y elige una opción. Si hay valores preferidos presentes, elige entre ellos;
     * si no, elige una opción cualquiera al azar (sin placeholder). Verifica la selección.
     * Reintenta una vez (reabriendo el control) por si el primer intento fue un parpadeo
     * transitorio; si tampoco carga en el segundo intento, se da por fallido de verdad.
     */
    private boolean seleccionarEnNivel(WebDriver driver, int nivel, List<String> preferidos) {
        for (int intento = 1; intento <= 3; intento++) {
            List<WebElement> items = abrirYObtenerOpciones(driver, nivel, intento);
            if (items == null) continue; // fallo en este intento, probar de nuevo
            if (items.isEmpty()) continue;
            // Si el clic no llega a confirmarse (el "repaso" en elegirYConfirmar falla), NO se
            // asume éxito: se reintenta el ciclo completo (reabrir el nivel y volver a elegir)
            // en vez de avanzar al siguiente nivel con una selección que nunca quedó registrada.
            if (elegirYConfirmar(driver, nivel, preferidos, items)) {
                return true;
            }
            System.err.println("  [Niveles] Nivel " + nivel + ": selección no confirmada (intento " + intento + "), reintentando...");
        }
        return false;
    }

    /** Abre el control del nivel y devuelve sus opciones cargadas; null si algo falló al abrir. */
    private List<WebElement> abrirYObtenerOpciones(WebDriver driver, int nivel, int intento) {
        try {
            WebElement control = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(controlHabilitado(nivel)));
            scrollToCenter(driver, control);
            jsClick(driver, control);

            By opciones = By.cssSelector(
                    ".classifications-dropdown-list .classifications-dropdown-item:not(.classifications-dropdown-item--placeholder)");
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(opciones));

            // Pausa de asentamiento: apenas aparece la primera opción la lista puede seguir
            // renderizándose (más ítems, o un re-render que reemplaza los WebElement ya
            // encontrados). Sin esta pausa, a veces se leía/clicaba sobre una lista incompleta
            // o a punto de re-renderizarse, y el clic no quedaba registrado.
            dormir(500);

            List<WebElement> items = driver.findElements(opciones).stream()
                    .filter(WebElement::isDisplayed)
                    .collect(Collectors.toList());
            if (items.isEmpty()) {
                System.err.println("  [Niveles] Nivel " + nivel + ": opciones vacías (intento " + intento + ").");
            }
            return items;
        } catch (Exception e) {
            System.err.println("  [Niveles] Nivel " + nivel + ": no cargaron opciones en 20s (intento " + intento + ").");
            return null;
        }
    }

    private boolean elegirYConfirmar(WebDriver driver, int nivel, List<String> preferidos, List<WebElement> items) {
        try {

            // Excluir opciones que abren formularios extra no soportados (lista negra).
            List<WebElement> base = items.stream()
                    .filter(it -> OPCIONES_EXCLUIDAS.stream()
                            .noneMatch(ex -> it.getText().trim().equalsIgnoreCase(ex)))
                    .collect(Collectors.toList());
            if (base.isEmpty()) {
                System.err.println("  [Niveles] Nivel " + nivel + ": todas las opciones están excluidas — se elige igualmente.");
                base = items;
            }

            // Filtrar a los preferidos si hay alguno presente (sobre las opciones no excluidas).
            List<WebElement> candidatos = base;
            if (!preferidos.isEmpty()) {
                List<WebElement> filtrados = base.stream()
                        .filter(it -> preferidos.stream().anyMatch(p -> it.getText().trim().equalsIgnoreCase(p)))
                        .collect(Collectors.toList());
                if (!filtrados.isEmpty()) {
                    candidatos = filtrados;
                } else {
                    System.err.println("  [Niveles] Nivel " + nivel + ": ninguno de los valores preferidos " +
                            preferidos + " está disponible — se elige al azar.");
                }
            }

            WebElement elegido = candidatos.get(RANDOM.nextInt(candidatos.size()));
            String valor = elegido.getText().trim();
            scrollToCenter(driver, elegido);

            // Re-buscar el elemento por texto justo antes de clicar: la lista puede haberse
            // re-renderizado entre el momento en que obtuvimos las referencias y ahora,
            // dejando los WebElement guardados stale. jsClick sobre un elemento stale no lanza
            // excepción pero tampoco registra el clic. Re-buscar garantiza una ref fresca.
            By itemPorTexto = By.xpath(
                    ".//div[contains(@class,'classifications-dropdown-list')]" +
                    "//div[contains(@class,'classifications-dropdown-item')" +
                    " and not(contains(@class,'classifications-dropdown-item--placeholder'))" +
                    " and normalize-space()='" + valor.replace("'", "\\'") + "']");
            try {
                WebElement fresh = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(itemPorTexto));
                scrollToCenter(driver, fresh);
                jsClick(driver, fresh);
            } catch (Exception ex) {
                // Si no encontramos por XPath exacto, usar la referencia original
                jsClick(driver, elegido);
            }

            // Sondear hasta 8s a que el control muestre el valor elegido.
            if (esperarConfirmacion(driver, nivel, valor, 8)) {
                System.out.println("  [Niveles] Nivel " + nivel + " = " + valor + " (OK)");
                com.natura.automation.util.ReportePaso.valor("Nivel " + nivel, valor);
                return true;
            }
            System.err.println("  [Niveles] Nivel " + nivel + ": clic en '" + valor + "' no se reflejó en el control tras 8s.");
            return false;
        } catch (Exception e) {
            System.err.println("  [Niveles] ERROR en Nivel " + nivel + ": " + e.getMessage());
            return false;
        }
    }

    /** Sondea hasta timeoutSeg a que el control del nivel muestre el valor elegido. */
    private boolean esperarConfirmacion(WebDriver driver, int nivel, String valor, int timeoutSeg) {
        long fin = System.currentTimeMillis() + timeoutSeg * 1000L;
        while (System.currentTimeMillis() < fin) {
            if (nivelMuestraValor(driver, nivel, valor)) return true;
            dormir(300);
        }
        return false;
    }

    private By controlHabilitado(int nivel) {
        return By.xpath(
                "//div[contains(@class,'classifications-dropdown-wrap')]" +
                "[.//label[normalize-space()='Nivel " + nivel + "']]" +
                "//div[contains(@class,'classifications-dropdown-control')]" +
                "[not(contains(@class,'classifications-dropdown-control--disabled'))]");
    }

    private boolean nivelMuestraValor(WebDriver driver, int nivel, String valor) {
        try {
            String texto = driver.findElement(By.xpath(
                    "//div[contains(@class,'classifications-dropdown-wrap')]" +
                    "[.//label[normalize-space()='Nivel " + nivel + "']]" +
                    "//*[contains(@class,'classifications-dropdown-control-label')]")).getText().trim();
            return texto.equalsIgnoreCase(valor) || texto.toLowerCase().contains(valor.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(FORM_IFRAME));
    }

    private void scrollToCenter(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
        } catch (Exception ignored) {}
    }

    private void jsClick(WebDriver driver, WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
