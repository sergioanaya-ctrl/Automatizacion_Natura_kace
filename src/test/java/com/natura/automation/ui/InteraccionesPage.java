package com.natura.automation.ui;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

/**
 * Page Object para la página de Interacciones y Workspaces en Natura Agent
 */
public class InteraccionesPage {

    // ========== CHECKBOX Y DROPDOWN DE WORKSPACE ==========
    /**
     * Checkbox "Todos los espacios de trabajo" (allWorkspaces)
     */
    public static final Target CHECKBOX_ALL_WORKSPACES = Target.the("Checkbox Todos los espacios de trabajo")
            .located(By.id("allWorkspaces"));

    /**
     * Botón dropdown para seleccionar workspace
     */
    public static final Target BTN_SELECT_WORKSPACE_DROPDOWN = Target.the("Botón Dropdown Seleccionar Workspace")
            .located(By.id("btn_select_combo_box"));

    /**
     * Opción de workspace en el dropdown (parametrizado)
     * Uso: InteraccionesPage.WORKSPACE_OPTION.of("Automatizacion")
     */
    public static final Target WORKSPACE_OPTION = Target.the("Opción de Workspace {0}")
            .locatedBy("//*[contains(text(), '{0}') and contains(@class, 'flex') and contains(@class, 'cursor-default')]");

    /**
     * Opción de workspace alternativa con data-testid
     */
    public static final Target WORKSPACE_OPTION_BY_DATA_TESTID = Target.the("Opción Workspace {0}")
            .locatedBy("//div[@data-testid='multiselect_item__' and contains(text(), '{0}')]");

    /**
     * Opción de workspace con SVG de check (alternativa más robusta)
     */
    public static final Target WORKSPACE_OPTION_CHECKED = Target.the("Workspace {0} con check")
            .locatedBy("//div[contains(@class, 'relative flex cursor-default select-none') and contains(., '{0}')]");

    // ========== LISTA DE INTERACCIONES ==========
    /**
     * Contenedor de la tabla de interacciones
     */
    public static final Target TABLA_INTERACCIONES = Target.the("Tabla de Interacciones")
            .located(By.xpath("//table[@class='w-full']"));

    /**
     * Fila de interacción por asunto (parametrizado)
     * Uso: InteraccionesPage.FILA_INTERACCION.of("pruebas java")
     */
    public static final Target FILA_INTERACCION = Target.the("Fila interacción {0}")
            .locatedBy("//tr[.//span[contains(text(), '{0}')]]");

    /**
     * Celda específica de una interacción (con subject y status)
     */
    public static final Target INTERACCION_ROW = Target.the("Interacción {0}")
            .locatedBy("//tr[@class='transition-colors cursor-pointer hover:bg-purple-50']//" +
                    "span[contains(@class, 'font-medium') and text()='{0}']");

    /**
     * Cualquier fila de interacción en la tabla (para obtener la primera)
     */
    public static final Target PRIMERA_INTERACCION = Target.the("Primera interacción en la tabla")
            .locatedBy("//tbody[@class='bg-white divide-y divide-gray-200']/tr[1]");

    /**
     * Contador de interacciones (badge con número)
     */
    public static final Target CONTADOR_INTERACCIONES = Target.the("Contador de interacciones")
            .located(By.xpath("//span[@class='px-2 py-1 bg-purple-100 text-purple-700 text-xs font-semibold rounded-full']"));

    // ========== BOTONES DE ACCIÓN ==========
    /**
     * Botón "Gestionar" para actualizar y filtrar interacciones
     */
    public static final Target BTN_GESTIONAR = Target.the("Botón Gestionar")
            .located(By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Gestionar']"));

    /**
     * Botón "Liberar" en la interacción
     */
    public static final Target BTN_LIBERAR = Target.the("Botón Liberar")
            .located(By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Liberar']"));

    /**
     * Botón "Crear Caso" desde la interacción
     */
    public static final Target BTN_CREAR_CASO = Target.the("Botón Crear Caso")
            .located(By.xpath("//button[@id='btn_interaction_create_case' and normalize-space()='Crear Caso']"));

    /**
     * Botón "Usar cliente seleccionado"
     */
    public static final Target BTN_USAR_CLIENTE_SELECCIONADO = Target.the("Botón Usar cliente seleccionado")
            .located(By.xpath("//button[@id='btn_update_and_filter' and normalize-space()='Usar cliente seleccionado']"));


    // ========== PAGINACIÓN ==========
    /**
     * Botón siguiente página de interacciones
     */
    public static final Target BTN_SIGUIENTE_PAGINA = Target.the("Botón Siguiente página")
            .located(By.id("btn_interactions_next_page"));

    /**
     * Botón página anterior
     */
    public static final Target BTN_ANTERIOR_PAGINA = Target.the("Botón Página anterior")
            .located(By.id("btn_interactions_previous_page"));

    /**
     * Información de paginación (ej: "Página 1 de 2")
     */
    public static final Target INFO_PAGINACION = Target.the("Información de paginación")
            .locatedBy("//div[@class='flex flex-col text-xs text-gray-500']/span[@class='font-medium text-gray-700']");

    // ========== ESTADOS Y ELEMENTOS DINÁMICOS ==========
    /**
     * Elemento de espera - lista de interacciones cargada
     */
    public static final Target LISTA_INTERACCIONES_CARGADA = Target.the("Lista de interacciones cargada")
            .locatedBy("//tbody[@class='bg-white divide-y divide-gray-200']");

    /**
     * Badge de estado de la interacción (En revisión, Creada, etc)
     */
    public static final Target ESTADO_INTERACCION = Target.the("Estado de interacción {0}")
            .locatedBy("//span[@class='text-xs mt-1 text-yellow-600'][contains(text(), '{0}')]");

    /**
     * Overlay/Modal que aparece cuando se abre el dropdown
     */
    public static final Target DROPDOWN_OVERLAY = Target.the("Overlay del dropdown")
            .locatedBy("//div[contains(@class, 'fixed')]");

}
