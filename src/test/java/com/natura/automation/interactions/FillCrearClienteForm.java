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
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class FillCrearClienteForm implements Interaction {

    // Llena el formulario "Crear Cliente" de Natura dentro del iframe OneScript.
    // Genera datos aleatorios si no se provee mapa, o usa los datos del feature si se proveen.

    private static final Random RANDOM = new Random();

    private static final String[] TIPOS_DOC  = {"CC"};
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

        // Al pasar a agente el formulario carga, luego aparece un mensaje "Espere..." y re-renderiza.
        // Esperar a que ese mensaje desaparezca y a que Tipo Documento esté disponible y usable
        // (sus opciones cargadas) antes de empezar a llenar.
        long tLoad = System.currentTimeMillis();
        esperarFormularioListo(driver);
        System.out.println("[FillCrearClienteForm] esperarFormularioListo tardó " +
                ((System.currentTimeMillis() - tLoad) / 1000.0) + "s.");

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

        // ── Tipo Documento (Choices.js) — PRIMERO ────────────────────────────
        // Se llena ANTES que Código Consultor a propósito: escribir el código de consultor
        // dispara el lookup "Obteniendo datos del Cliente" que re-renderiza/bloquea el dropdown
        // de Tipo Documento. Seleccionándolo primero (con la lista recién cargada) se evita eso.
        long tTipo = System.currentTimeMillis();
        seleccionarChoices(driver, "tipo_documento", tipoDoc);
        System.out.println("[FillCrearClienteForm] selección Tipo Documento tardó " +
                ((System.currentTimeMillis() - tTipo) / 1000.0) + "s.");

        // ── Campos de texto ───────────────────────────────────────────────────
        escribir(driver, "input[name='data[documento]']",              codConsultor); // Código consultor
        escribir(driver, "input[name='data[telefono]']",               celular);      // Celular
        escribir(driver, "input[name='data[campos_extra1]']",          numIdent);     // Número Identificación
        escribir(driver, "input[name='data[nombres]']",                nombres);
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
                registrarValor(nombreCampo(css), valor);
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

        By choicesBy = By.cssSelector(".formio-component-" + campo + " .choices");
        By opcionesBy = By.cssSelector(
                ".formio-component-" + campo + " .choices__list--dropdown .choices__item--choice");

        // Reintentar: si el formulario aún re-renderiza, la selección puede perderse.
        for (int intento = 1; intento <= 3; intento++) {
            ensureIframe(driver);
            try {
                WebElement comp = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector(".formio-component-" + campo)));
                scrollToCenter(driver, comp);

                // 1) ABRIR el dropdown garantizando que quede abierto (Choices.js necesita
                //    eventos de mouse reales; un .click() por JS no siempre lo dispara).
                abrirChoices(driver, choicesBy);

                // 2) Escribir el valor en el buscador (filtra y resalta la opción) y esperar
                //    a que la opción esté cargada/visible.
                WebElement search = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                                ".formio-component-" + campo + " input.choices__input--cloned")));
                search.clear();
                search.sendKeys(valor);

                List<WebElement> opciones = esperarOpcionesChoices(driver, opcionesBy, 12);
                if (opciones.isEmpty()) {
                    System.err.println("  [Choices] " + campo + ": las opciones no cargaron (intento " + intento + "), reintentando...");
                    continue;
                }
                dormir(400); // asentamiento tras cargar

                // 3) Seleccionar. Choices.js selecciona de forma confiable con ENTER sobre el
                //    buscador (la opción resaltada); un .click() por JS no dispara su handler.
                search.sendKeys(Keys.ENTER);
                dormir(300);

                // Fallback: si Enter no registró, clic NATIVO sobre la opción coincidente.
                if (!choicesSeleccionNoVacia(driver, campo)) {
                    WebElement objetivo = opciones.stream()
                            .filter(o -> {
                                String t = o.getText().trim();
                                return t.equalsIgnoreCase(valor) || t.toLowerCase().contains(valor.toLowerCase());
                            })
                            .findFirst().orElse(opciones.get(0));
                    scrollToCenter(driver, objetivo);
                    try { objetivo.click(); } catch (Exception e) { dispatchMouseClick(driver, objetivo); }
                    dormir(300);
                }

                // 4) Verificar que quedó algo seleccionado (no placeholder).
                if (choicesSeleccionNoVacia(driver, campo)) {
                    String elegido = driver.findElement(By.cssSelector(
                            ".formio-component-" + campo + " .choices__list--single .choices__item")).getText().trim();
                    System.out.println("  [Choices] " + campo + " = " + elegido + " (OK, intento " + intento + ")");
                    registrarValor(campo, elegido);
                    return;
                }
                System.err.println("  [Choices] " + campo + " no quedó seleccionado (intento " + intento + "), reintentando...");
            } catch (Exception e) {
                System.err.println("  [Choices] ERROR " + campo + " = " + valor + " (intento " + intento + "): " + e.getMessage());
            }
        }
        System.err.println("  [Choices] No se pudo seleccionar " + campo + " = " + valor + " tras 3 intentos.");
    }

    /**
     * Abre el dropdown de un Choices.js asegurando que quede en estado abierto (clase is-open).
     * UN solo clic por iteración (dos clics seguidos lo abren y lo vuelven a cerrar). Alterna
     * el método de clic entre intentos por si uno no dispara el evento que Choices.js escucha.
     */
    private void abrirChoices(WebDriver driver, By choicesBy) {
        for (int i = 0; i < 5; i++) {
            if (choicesAbierto(driver, choicesBy)) return; // ya abierto

            WebElement choices = driver.findElement(choicesBy);
            WebElement opener;
            try {
                opener = choices.findElement(By.cssSelector(".choices__inner"));
            } catch (Exception nf) {
                opener = choices;
            }
            scrollToCenter(driver, opener);
            // Alternar: clic nativo en intentos pares, secuencia de eventos de mouse en impares.
            if (i % 2 == 0) {
                try { opener.click(); } catch (Exception e) { jsClick(driver, opener); }
            } else {
                dispatchMouseClick(driver, opener);
            }
            dormir(350);
        }
    }

    private boolean choicesAbierto(WebDriver driver, By choicesBy) {
        try {
            String clase = driver.findElement(choicesBy).getAttribute("class");
            return clase != null && clase.contains("is-open");
        } catch (Exception e) {
            return false;
        }
    }

    /** Dispara la secuencia de eventos de mouse para componentes que solo responden a eventos reales. */
    private void dispatchMouseClick(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var el=arguments[0];" +
                    "['pointerdown','mousedown','mouseup','click'].forEach(function(t){" +
                    "  el.dispatchEvent(new MouseEvent(t,{bubbles:true,cancelable:true,view:window}));" +
                    "});", el);
        } catch (Exception ignored) {}
    }

    /**
     * Espera a que el dropdown del Choices tenga opciones REALES cargadas (no vacío, no "cargando")
     * y las devuelve. Tope timeoutSeg. Si no cargan, devuelve lista vacía.
     */
    private List<WebElement> esperarOpcionesChoices(WebDriver driver, By opcionesBy, int timeoutSeg) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeg)).until(d ->
                    d.findElements(opcionesBy).stream()
                            .anyMatch(o -> opcionReal(o.getText())));
        } catch (Exception ignored) {}
        return driver.findElements(opcionesBy).stream()
                .filter(o -> opcionReal(o.getText()))
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean opcionReal(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        return !t.isEmpty() && !t.contains("cargando") && !t.contains("loading");
    }

    /** True si el Choices tiene una selección real (item single con texto no placeholder). */
    private boolean choicesSeleccionNoVacia(WebDriver driver, String campo) {
        try {
            String sel = driver.findElement(By.cssSelector(
                    ".formio-component-" + campo + " .choices__list--single .choices__item")).getText().trim();
            String s = sel.toLowerCase();
            return !s.isEmpty() && !s.contains("elige") && !s.contains("seleccion");
        } catch (Exception e) {
            return false;
        }
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

    /**
     * Espera a que el campo Tipo Documento esté DISPONIBLE para usarse, sin esperar a que
     * cierre el modal "Obteniendo datos del Cliente" (que puede tardar ~40s del backend).
     * Como los clics se hacen por JavaScript (atraviesan el overlay swal2), no hace falta
     * que el modal desaparezca: basta con que el componente Choices esté renderizado.
     * Tope de seguridad alto por si el formulario tarda en renderizar.
     */
    private void esperarFormularioListo(WebDriver driver) {
        ensureIframe(driver);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(60)).until(d -> {
                ensureIframe(d);
                return d.findElements(By.cssSelector(".formio-component-tipo_documento .choices"))
                        .stream().anyMatch(WebElement::isDisplayed);
            });
            System.out.println("[FillCrearClienteForm] Campo Tipo Documento renderizado — continuando.");
        } catch (Exception e) {
            System.err.println("[FillCrearClienteForm] Tipo Documento no se renderizó en 60s — continuando igual.");
        }
        ensureIframe(driver);
    }

    /** Sondea (rápido) hasta timeoutSeg a que NO haya modal de carga; retorna apenas desaparece. */
    private void esperarFinDeCargando(WebDriver driver, int timeoutSeg) {
        long inicio = System.currentTimeMillis();
        long fin = inicio + timeoutSeg * 1000L;
        boolean huboModal = false;
        while (System.currentTimeMillis() < fin) {
            if (!cargandoVisible(driver)) {
                if (huboModal) {
                    System.out.println("[FillCrearClienteForm] Modal de carga cerrado en " +
                            ((System.currentTimeMillis() - inicio) / 1000.0) + "s — continuando.");
                }
                return;
            }
            huboModal = true;
            dormir(250);
        }
        System.err.println("[FillCrearClienteForm] El modal de carga no cerró tras " + timeoutSeg + "s — continuando igual.");
    }

    private boolean cargandoVisible(WebDriver driver) {
        // Documento principal.
        driver.switchTo().defaultContent();
        if (textoDeCargaVisible(driver)) return true;
        // Iframe del formulario.
        try {
            driver.switchTo().frame(driver.findElement(By.id("form_onescript_iframe")));
            if (textoDeCargaVisible(driver)) return true;
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * True solo si hay un modal de carga ACTIVO: spinner swal2 visible, o un popup swal2 visible
     * cuyo texto es de carga. Evita falsos positivos por un .swal2-container residual en el DOM.
     */
    private boolean textoDeCargaVisible(WebDriver driver) {
        boolean spinner = driver.findElements(By.cssSelector(".swal2-loader"))
                .stream().anyMatch(WebElement::isDisplayed);
        if (spinner) return true;
        return driver.findElements(By.cssSelector(".swal2-popup")).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(p -> {
                    String t = p.getText().toLowerCase();
                    return t.contains("cargando") || t.contains("obteniendo")
                            || t.contains("espere") || t.contains("procesando");
                });
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ── Registro de pasos en el reporte (para que los valores queden en step_details) ──

    /**
     * Inyecta un paso en el reporte de Serenity con el formato "enters '<valor>' into <campo>"
     * para que el generador de step_details (Excel/CSV) capture el valor en la columna Valor.
     */
    private void registrarValor(String campo, String valor) {
        com.natura.automation.util.ReportePaso.valor(campo, valor);
    }

    /** Extrae un nombre legible del selector: input[name='data[nombres]'] -> nombres. */
    private String nombreCampo(String css) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("data\\[([^\\]]+)\\]").matcher(css);
        return m.find() ? m.group(1) : css;
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
            registrarValor(campo, valor);
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
