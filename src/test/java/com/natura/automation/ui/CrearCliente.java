package com.natura.automation.ui;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class CrearCliente {

    private CrearCliente() {}

    // ── Tipo de documento (Choices.js dropdown) ─────────────────────────────
    public static final Target Tipo_Documento = Target.the("Tipo Documento (dropdown)")
            .located(By.cssSelector(".formio-component-tipo_documento .choices__list--single"));

    public static final Target Tipo_Documento_Opcion = Target.the("Tipo Documento opción {0}")
            .locatedBy(".formio-component-tipo_documento .choices__item--choice[data-value='{0}']");

    // ── Documento / Código Consultor ─────────────────────────────────────────
    public static final Target Documento = Target.the("Documento / Código Consultor")
            .located(By.cssSelector("input[name='data[documento]']"));

    // ── Número Identificación ─────────────────────────────────────────────────
    public static final Target Numero_Identificacion = Target.the("Número Identificación")
            .located(By.cssSelector("input[name='data[campos_extra1]']"));

    // ── Nombres ──────────────────────────────────────────────────────────────
    public static final Target Nombres = Target.the("Nombres")
            .located(By.cssSelector("input[name='data[nombres]']"));

    // ── Celular ───────────────────────────────────────────────────────────────
    public static final Target Celular = Target.the("Celular")
            .located(By.cssSelector("input[name='data[telefono]']"));

    // ── Email ─────────────────────────────────────────────────────────────────
    public static final Target Email = Target.the("Email")
            .located(By.cssSelector("input[name='data[email]']"));

    // ── Fecha de nacimiento (Flatpickr — campo visible, no el hidden) ─────────
    public static final Target Fecha_Nacimiento = Target.the("Fecha Nacimiento")
            .located(By.cssSelector(".formio-component-fecha_nacimiento input.form-control:not([type='hidden'])"));

    // ── Perfil (Choices.js dropdown) ─────────────────────────────────────────
    public static final Target Perfil = Target.the("Perfil (dropdown)")
            .located(By.cssSelector(".formio-component-perfil .choices__list--single"));

    public static final Target Perfil_Opcion = Target.the("Perfil opción {0}")
            .locatedBy(".formio-component-perfil .choices__item--choice[data-value='{0}']");

    // ── Gerencia ──────────────────────────────────────────────────────────────
    public static final Target Gerencia = Target.the("Gerencia")
            .located(By.cssSelector("input[name='data[gerencia]']"));

    // ── Correo Gerente ────────────────────────────────────────────────────────
    public static final Target Correo_Gerente = Target.the("Correo Gerente")
            .located(By.cssSelector("input[name='data[email_alterno]']"));

    // ── Límite de crédito total ───────────────────────────────────────────────
    public static final Target Limite_Credito_Total = Target.the("Límite Crédito Total")
            .located(By.cssSelector("input[name='data[limite_credito_total]']"));

    // ── Puntos actuales ───────────────────────────────────────────────────────
    public static final Target Puntos_Actuales = Target.the("Puntos Actuales")
            .located(By.cssSelector("input[name='data[puntos_actuales]']"));

    // ── Ciclo último pedido ───────────────────────────────────────────────────
    public static final Target Ciclo_Ultimo_Pedido = Target.the("Ciclo Último Pedido")
            .located(By.cssSelector("input[name='data[ciclo_ultimo_pedido]']"));

    // ── Nombre líder ──────────────────────────────────────────────────────────
    public static final Target Nombre_Lider = Target.the("Nombre Líder")
            .located(By.cssSelector("input[name='data[nombre_lider]']"));

    // ── Email alterno ─────────────────────────────────────────────────────────
    public static final Target Email_Alterno = Target.the("Email Alterno")
            .located(By.cssSelector("input[name='data[email_alterno]']"));

    // ── Límite de crédito utilizado ───────────────────────────────────────────
    public static final Target Limite_Credito_Utilizado = Target.the("Límite Crédito Utilizado")
            .located(By.cssSelector("input[name='data[limite_credito_utilizado]']"));

    // ── Camino de crecimiento ─────────────────────────────────────────────────
    public static final Target Camino_Crecimiento = Target.the("Camino Crecimiento")
            .located(By.cssSelector("input[name='data[camino_crecimiento]']"));

    // ── Puntos para subir de nivel ────────────────────────────────────────────
    public static final Target Puntos_Subir_Nivel = Target.the("Puntos Subir Nivel")
            .located(By.cssSelector("input[name='data[puntos_subir_nivel]']"));

    // ── Número último pedido ──────────────────────────────────────────────────
    public static final Target Numero_Ultimo_Pedido = Target.the("Número Último Pedido")
            .located(By.cssSelector("input[name='data[numero_ultimo_pedido]']"));

    // ── Teléfono líder ────────────────────────────────────────────────────────
    public static final Target Telefono_Lider = Target.the("Teléfono Líder")
            .located(By.cssSelector("input[name='data[telefono_lider]']"));

    // ── Dirección de entrega ──────────────────────────────────────────────────
    public static final Target Direccion_Entrega = Target.the("Dirección Entrega")
            .located(By.cssSelector("input[name='data[direccion_entrega]']"));

    // ── Sector ────────────────────────────────────────────────────────────────
    public static final Target Sector = Target.the("Sector")
            .located(By.cssSelector("input[name='data[sector]']"));

    // ── Crédito disponible ────────────────────────────────────────────────────
    public static final Target Credito_Disponible = Target.the("Crédito Disponible")
            .located(By.cssSelector("input[name='data[credito_disponible]']"));

    // ── Crédito pendiente de usar ─────────────────────────────────────────────
    public static final Target Credito_Pendiente_Usar = Target.the("Crédito Pendiente Usar")
            .located(By.cssSelector("input[name='data[credito_pendiente_usar]']"));

    // ── Siguiente nivel ───────────────────────────────────────────────────────
    public static final Target Siguiente_Nivel = Target.the("Siguiente Nivel")
            .located(By.cssSelector("input[name='data[siguiente_nivel]']"));

    // ── Estado último pedido ──────────────────────────────────────────────────
    public static final Target Estado_Ultimo_Pedido = Target.the("Estado Último Pedido")
            .located(By.cssSelector("input[name='data[estado_ultimo_pedido]']"));

    // ── Código de grupo ───────────────────────────────────────────────────────
    public static final Target Codigo_Grupo = Target.the("Código Grupo")
            .located(By.cssSelector("input[name='data[codigo_grupo]']"));

    // ── Botón Crear / Actualizar Cliente ──────────────────────────────────────
    public static final Target Boton_Crear_Actualizar_Cliente = Target.the("Botón Crear / Actualizar Cliente")
            .located(By.xpath("//button[@name='data[crearActualizarCliente]' or " +
                              "contains(normalize-space(.), 'Crear Cliente') or " +
                              "contains(normalize-space(.), 'Actualizar Cliente')]"));
}
