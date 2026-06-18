package com.natura.automation.util;

import net.serenitybdd.core.Serenity;

/**
 * Registro de valores diligenciados para el reporte step_details.
 *
 * Usa Serenity.recordReportData() (adjunta datos al reporte) en vez de StepEventBus.stepStarted/
 * stepFinished. Esto NO dispara capturas de pantalla ni eventos de paso, por lo que NO interfiere
 * con el contexto del iframe (la implementación con StepEventBus rompía las selecciones de niveles
 * → "0 transiciones").
 */
public final class ReportePaso {

    private ReportePaso() {}

    /** Adjunta un valor diligenciado al reporte del test actual (seguro, sin tocar el navegador). */
    public static void valor(String campo, String valor) {
        if (valor == null) return;
        // Limpiar: quedarse con la primera línea y quitar el texto del botón "Remove item"
        // que Choices.js incluye en el texto del item seleccionado.
        String limpio = valor.split("\\R")[0].replace("Remove item", "").trim();
        if (limpio.isEmpty()) return;
        try {
            Serenity.recordReportData()
                    .withTitle("Valor: " + campo)
                    .andContents("enters '" + limpio + "' into " + campo);
        } catch (Exception ignored) {}
    }
}
