package com.natura.automation.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;
import java.util.Random;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class FillCrearClienteForm implements Interaction {

    // Llena el formulario "Crear Cliente" de Natura dentro del iframe OneScript.
    // Genera datos aleatorios si no se provee mapa, o usa los datos del feature si se proveen.

    private static final Random RANDOM = new Random();

    private static final String[] TIPOS_DOC  = {"CC", "CE", "Cod Consul", "Cod Consul Col", "Cod Consul UR", "NIT", "PPT"};
    private static final String[] PERFILES    = {"CB", "LN", "GV", "GN", "CF", "CBD"};
    private static final String[] NIVELES     = {"BRONCE", "PLATA", "ORO", "DIAMANTE", "ESMERALDA"};
    private static final String[] SECTORES    = {"SUR", "NORTE", "CENTRO", "ORIENTE", "OCCIDENTE"};
    private static final String[] CAMINOS     = {"NIVEL 1", "NIVEL 2", "NIVEL 3", "NIVEL 4"};
    private static final String[] ESTADOS_PED = {"ENTREGADO", "PENDIENTE", "CANCELADO", "EN PROCESO"};
    private static final String[] NOMBRES     = {
        "Maria", "Ana", "Laura", "Valentina", "Camila", "Sofia", "Daniela",
        "Carlos", "Juan", "Andres", "Sebastian", "Miguel", "David", "Felipe"
    };
    private static final String[] APELLIDOS   = {
        "Garcia", "Lopez", "Martinez", "Rodriguez", "Gonzalez", "Perez",
        "Sanchez", "Ramirez", "Torres", "Vargas", "Castillo", "Morales"
    };

    private final Map<String, String> datos;

    public FillCrearClienteForm(Map<String, String> datos) {
        this.datos = datos;
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    public static Performable conDatosAleatorios() {
        return instrumented(FillCrearClienteForm.class, (Map<String, String>) null);
    }

    public static Performable conDatos(Map<String, String> datos) {
        return instrumented(FillCrearClienteForm.class, datos);
    }

    // ── Punto de entrada ─────────────────────────────────────────────────────

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        ensureIframe(driver);

        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("input[name='data[documento]'], input[name='data[nombres]']")));

        // Esperar a que el formulario termine de cargar (overlay "Cargando formulario") y que
        // el dropdown Tipo Documento tenga sus opciones, para que la selección no se "vuele".
        esperarFormularioListo(driver);

        // Resolver valores: feature primero, aleatorio si no se proveyó
        String tipoDoc      = get("tipo_documento",           randomDe(TIPOS_DOC));
        String codConsultor = get("codigo_consultor",         randomDigitos(7));
        String numIdent     = get("numero_identificacion",    randomDigitos(10));
        String nombres      = get("nombres",                  generarNombre());
        String celular      = get("celular",                  "3" + randomDigitos(9));
        String correo       = get("correo",                   generarCorreo(nombres));
        String fechaNac     = get("fecha_nacimiento",         generarFecha());
        String dirEntrega   = get("direccion_entrega",        generarDireccion());
        String gerencia     = get("gerencia",                 "GER" + randomDigitos(3));
        String correoGer    = get("correo_gerente",           "gerente" + randomDigitos(3) + "@natura.com");
        String sector       = get("sector",                   randomDe(SECTORES));
        long   limTotal     = parseLong(get("limite_credito_total",      String.valueOf((2 + RANDOM.nextInt(9)) * 1_000_000L)));
        long   limUsado     = parseLong(get("limite_credito_utilizado",  String.valueOf(RANDOM.nextInt((int)(limTotal / 1_000_000L)) * 1_000_000L)));
        long   credDisp     = parseLong(get("credito_disponible",        String.valueOf(limTotal - limUsado)));
        String perfil       = get("perfil",                   randomDe(PERFILES));
        String camino       = get("camino_crecimiento",       randomDe(CAMINOS));
        long   credPend     = parseLong(get("credito_pendiente_usar",    String.valueOf(RANDOM.nextInt(500) * 1000L)));
        int    puntos       = parseInt(get("puntos_actuales",            String.valueOf(RANDOM.nextInt(5000))));
        int    puntosSubir  = parseInt(get("puntos_subir_nivel",         String.valueOf(RANDOM.nextInt(1000))));
        String sigNivel     = get("siguiente_nivel",          randomDe(NIVELES));
        int    ciclo        = parseInt(get("ciclo_ultimo_pedido",        String.valueOf(1 + RANDOM.nextInt(18))));
        String numPedido    = get("numero_ultimo_pedido",     randomDigitos(8));
        String estadoPed    = get("estado_ultimo_pedido",     randomDe(ESTADOS_PED));
        String nomLider     = get("nombre_lider",             generarNombre());
        String telLider     = get("telefono_lider",           "3" + randomDigitos(9));
        String codGrupo     = get("codigo_grupo",             "GR" + randomDigitos(4));

        System.out.println("[FillCrearClienteForm] tipo=" + tipoDoc + " | doc=" + codConsultor +
                           " | nombre=" + nombres + " | perfil=" + perfil);

        // ── Tipo Documento (Choices.js) ──────────────────────────────────────
        seleccionarChoices(driver, "tipo_documento", tipoDoc);

        // ── Campos de texto en orden visual del formulario ───────────────────
        escribir(driver, "input[name='data[documento]']",              codConsultor);
        escribir(driver, "input[name='data[campos_extra1]']",          numIdent);   // Número Identificación
        escribir(driver, "input[name='data[nombres]']",                nombres);
        escribir(driver, "input[name='data[telefono]']",               celular);    // Celular
        escribir(driver, "input[name='data[email]']",                  correo);

        // ── Fecha Nacimiento (Flatpickr) ─────────────────────────────────────
        escribirFecha(driver, "fecha_nacimiento", fechaNac);

        // ── Resto de campos ──────────────────────────────────────────────────
        escribir(driver, "input[name='data[direccion_entrega]']",          dirEntrega);
        escribir(driver, "input[name='data[gerencia]']",                   gerencia);
        escribir(driver, "input[name='data[email_alterno]']",              correoGer);  // Correo Gerente
        escribir(driver, "input[name='data[sector]']",                     sector);
        escribir(driver, "input[name='data[limite_credito_total]']",       String.valueOf(limTotal));
        escribir(driver, "input[name='data[limite_credito_utilizado]']",   String.valueOf(limUsado));
        escribir(driver, "input[name='data[credito_disponible]']",         String.valueOf(credDisp));

        // ── Perfil (Choices.js) ──────────────────────────────────────────────
        seleccionarChoices(driver, "perfil", perfil);

        // ── Campos inferiores ────────────────────────────────────────────────
        escribir(driver, "input[name='data[camino_crecimiento]']",         camino);
        escribir(driver, "input[name='data[credito_pendiente_usar]']",     String.valueOf(credPend));
        escribir(driver, "input[name='data[puntos_actuales]']",            String.valueOf(puntos));
        escribir(driver, "input[name='data[puntos_subir_nivel]']",         String.valueOf(puntosSubir));
        escribir(driver, "input[name='data[siguiente_nivel]']",            sigNivel);
        escribir(driver, "input[name='data[ciclo_ultimo_pedido]']",        String.valueOf(ciclo));
        escribir(driver, "input[name='data[numero_ultimo_pedido]']",       numPedido);
        escribir(driver, "input[name='data[estado_ultimo_pedido]']",       estadoPed);
        escribir(driver, "input[name='data[nombre_lider]']",               nomLider);
        escribir(driver, "input[name='data[telefono_lider]']",             telLider);
        escribir(driver, "input[name='data[codigo_grupo]']",               codGrupo);

        // ── Botón Crear Cliente ──────────────────────────────────────────────
        clickCrearCliente(driver);
    }

    // ── Interacciones con WebDriver ──────────────────────────────────────────

    private void escribir(WebDriver driver, String css, String valor) {
        if (valor == null || valor.trim().isEmpty()) return;
        ensureIframe(driver);
        for (int intento = 1; intento <= 3; intento++) {
            try {
                // presence (no clickable): el campo puede estar fuera del viewport o tapado.
                WebElement el = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(css)));
                scrollToCenter(driver, el);
                try {
                    el.click();
                    el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                    el.sendKeys(valor);
                } catch (Exception clickEx) {
                    // Fallback: fijar value por JS y disparar eventos para que FormIO lo registre.
                    setValueViaJs(driver, el, valor);
                }
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                if (intento == 3) throw e;
            } catch (Exception e) {
                System.err.println("  [escribir] ERROR (" + intento + ") " + css + ": " + e.getMessage());
                if (intento == 3) return;
            }
        }
    }

    private void seleccionarChoices(WebDriver driver, String campo, String valor) {
        if (valor == null || valor.trim().isEmpty()) return;

        // Reintentar: si el formulario aún re-renderiza, la selección puede perderse.
        for (int intento = 1; intento <= 3; intento++) {
            ensureIframe(driver);
            try {
                WebElement comp = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector(".formio-component-" + campo)));
                scrollToCenter(driver, comp);

                // 1) Abrir el dropdown (Choices.js): la caja clickable es .choices .selection.dropdown.
                WebElement opener;
                try {
                    opener = comp.findElement(By.cssSelector(".choices .selection.dropdown"));
                } catch (Exception nf) {
                    opener = comp.findElement(By.cssSelector(".choices__inner, .choices"));
                }
                jsClick(driver, opener);

                // 2) Escribir en el input de búsqueda clonado (visible solo al abrir).
                WebElement search = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                                ".formio-component-" + campo + " input.choices__input--cloned")));
                search.sendKeys(valor);

                // 3) Esperar a que aparezca la opción filtrada y confirmar con Enter.
                By opcionFiltrada = By.cssSelector(
                        ".formio-component-" + campo + " .choices__list--dropdown .choices__item--choice");
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.visibilityOfElementLocated(opcionFiltrada));
                search.sendKeys(Keys.ENTER);

                // 4) Verificar que la selección quedó registrada (texto en el item seleccionado).
                if (choicesTieneValor(driver, campo, valor)) {
                    System.out.println("  [Choices] " + campo + " = " + valor + " (OK, intento " + intento + ")");
                    return;
                }
                System.err.println("  [Choices] " + campo + " no quedó seleccionado (intento " + intento + "), reintentando...");
            } catch (Exception e) {
                System.err.println("  [Choices] ERROR " + campo + " = " + valor + " (intento " + intento + "): " + e.getMessage());
            }
        }
        System.err.println("  [Choices] No se pudo seleccionar " + campo + " = " + valor + " tras 3 intentos.");
    }

    /** True si el item seleccionado (single) del Choices contiene el valor esperado. */
    private boolean choicesTieneValor(WebDriver driver, String campo, String valor) {
        try {
            String seleccion = driver.findElement(By.cssSelector(
                    ".formio-component-" + campo + " .choices__list--single .choices__item")).getText().trim();
            return seleccion.equalsIgnoreCase(valor) || seleccion.toLowerCase().contains(valor.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    /** Espera a que desaparezca el overlay/mensaje "Cargando formulario" y que el dropdown esté listo. */
    private void esperarFormularioListo(WebDriver driver) {
        // 1) Que ningún loader común ni texto "Cargando" siga visible.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
                boolean loaderVisible = d.findElements(By.cssSelector(
                        ".loading, .loader, .spinner, .formio-loading, .swal2-loader, " +
                        "[class*='cargando'], [class*='loading']"))
                        .stream().anyMatch(WebElement::isDisplayed);
                boolean textoCargando = d.findElements(By.xpath(
                        "//*[contains(translate(., 'CARGAND', 'cargand'), 'cargand')]"))
                        .stream().anyMatch(WebElement::isDisplayed);
                return !loaderVisible && !textoCargando;
            });
        } catch (Exception ignored) {
            // Si el selector genérico no aplica, continuar (lo cubre la verificación del Choices).
        }
        // 2) Que el dropdown Tipo Documento tenga sus opciones cargadas.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(
                            ".formio-component-tipo_documento .choices__item--choice")));
        } catch (Exception ignored) {}
    }

    // ── Helpers de interacción robusta ───────────────────────────────────────

    private void scrollToCenter(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
        } catch (Exception ignored) {}
    }

    private void jsClick(WebDriver driver, WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    private void setValueViaJs(WebDriver driver, WebElement el, String valor) {
        ((JavascriptExecutor) driver).executeScript(
                "var el = arguments[0], v = arguments[1];" +
                "var proto = el.tagName === 'TEXTAREA' ? window.HTMLTextAreaElement.prototype" +
                "                                       : window.HTMLInputElement.prototype;" +
                "var setter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
                "setter.call(el, v);" +
                "el.dispatchEvent(new Event('input',  { bubbles: true }));" +
                "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                "el.dispatchEvent(new Event('blur',   { bubbles: true }));",
                el, valor);
    }

    private void escribirFecha(WebDriver driver, String campo, String valor) {
        if (valor == null || valor.trim().isEmpty()) return;
        ensureIframe(driver);
        try {
            By inputVisible = By.cssSelector(
                    ".formio-component-" + campo + " input.form-control:not([type='hidden'])");
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(inputVisible));
            el.click();
            el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            el.sendKeys(valor);
            el.sendKeys(Keys.ESCAPE);
            System.out.println("  [Fecha] " + campo + " = " + valor);
        } catch (Exception e) {
            System.err.println("  [Fecha] ERROR " + campo + ": " + e.getMessage());
        }
    }

    private void clickCrearCliente(WebDriver driver) {
        ensureIframe(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[name='data[crearActualizarCliente]']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            btn.click();
            System.out.println("[FillCrearClienteForm] Clic en 'Crear Cliente' OK (CSS)");
            return;
        } catch (Exception ignored) {}

        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//button[contains(normalize-space(.), 'Crear Cliente') or contains(normalize-space(.), 'Actualizar Cliente')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            btn.click();
            System.out.println("[FillCrearClienteForm] Clic en 'Crear Cliente' OK (XPath)");
            return;
        } catch (Exception ignored) {}

        Object result = ((JavascriptExecutor) driver).executeScript(
                "var btn = document.querySelector(\"button[name*='crearActualizarCliente']\");" +
                "if (!btn) btn = Array.from(document.querySelectorAll('button'))" +
                "  .find(b => b.textContent.includes('Crear') || b.textContent.includes('Actualizar'));" +
                "if (btn) { btn.scrollIntoView(true); btn.click(); return 'clicked'; } return 'not-found';"
        );
        if (!"clicked".equals(result)) {
            throw new RuntimeException("[FillCrearClienteForm] No se encontró el botón Crear Cliente. JS: " + result);
        }
        System.out.println("[FillCrearClienteForm] Clic en 'Crear Cliente' OK (JS)");
    }

    private void ensureIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("form_onescript_iframe")));
    }

    // ── Generadores de datos aleatorios ──────────────────────────────────────

    private String generarNombre() {
        return NOMBRES[RANDOM.nextInt(NOMBRES.length)] + " " +
               APELLIDOS[RANDOM.nextInt(APELLIDOS.length)] + " " +
               APELLIDOS[RANDOM.nextInt(APELLIDOS.length)];
    }

    private String generarCorreo(String nombre) {
        String base = nombre.toLowerCase()
                .replaceAll("[áàä]", "a").replaceAll("[éèë]", "e")
                .replaceAll("[íìï]", "i").replaceAll("[óòö]", "o")
                .replaceAll("[úùü]", "u").replaceAll("[^a-z]", ".");
        return base + randomDigitos(3) + "@natura.com";
    }

    private String generarFecha() {
        int dia  = 1 + RANDOM.nextInt(28);
        int mes  = 1 + RANDOM.nextInt(12);
        int anio = 1970 + RANDOM.nextInt(35);
        return String.format("%02d/%02d/%04d", dia, mes, anio);
    }

    private String generarDireccion() {
        String[] tipos = {"Calle", "Carrera", "Diagonal", "Transversal"};
        return tipos[RANDOM.nextInt(tipos.length)] + " " +
               (10 + RANDOM.nextInt(90)) + " # " +
               (1 + RANDOM.nextInt(99)) + "-" +
               (1 + RANDOM.nextInt(99));
    }

    private String randomDigitos(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    private String randomDe(String[] arr) {
        return arr[RANDOM.nextInt(arr.length)];
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return 0L; }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private String get(String key, String valorPorDefecto) {
        if (datos == null) return valorPorDefecto;
        String v = datos.get(key);
        return (v == null || v.trim().isEmpty()) ? valorPorDefecto : v.trim();
    }
}
