package com.natura.automation.tasks;

import com.natura.automation.interactions.SeleccionarNivel;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.thucydides.core.annotations.Step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Diligencia el componente "Clasificaciones" seleccionando los niveles en cascada.
 * Niveles 1 y 2 vienen fijos (COLOMBIA, CEN). Se llenan del 3 en adelante.
 * Las opciones de Nivel 4 dependen de lo elegido en Nivel 3 (ver mapa).
 */
public class EjecutarNiveles implements Task {

    private static final Random RANDOM = new Random();

    // Mapa: cada Nivel 3 -> opciones válidas de Nivel 4. Evita buscar valores inexistentes.
    private static final Map<String, String[]> NIVEL3_A_NIVEL4 = new LinkedHashMap<>();
    static {
        NIVEL3_A_NIVEL4.put("ACOMPANAMIENTO, DESARROLLO Y RECONOCIMIENTO", new String[]{"CONSULTA"});
        NIVEL3_A_NIVEL4.put("ATRACCION, CADASTRO E INICIO",               new String[]{"CONSULTA", "SOLICITUD"});
        NIVEL3_A_NIVEL4.put("CAPTACION DE PEDIDOS",                       new String[]{"CONSULTA", "SOLICITUD"});
        NIVEL3_A_NIVEL4.put("DESPACHO Y ENTREGA DE PEDIDO",               new String[]{"RECLAMO"});
        NIVEL3_A_NIVEL4.put("ENTRENAMIENTO Y HERRAMIENTAS",               new String[]{"CONSULTA", "SOLICITUD"});
        NIVEL3_A_NIVEL4.put("IMPRODUCTIVO",                               new String[]{"IMPRODUCTIVO"});
        NIVEL3_A_NIVEL4.put("PAGO DE PEDIDO",                             new String[]{"CONSULTA", "SOLICITUD"});
        NIVEL3_A_NIVEL4.put("RITUALES Y EVENTOS",                         new String[]{"CONSULTA", "SOLICITUD"});
        NIVEL3_A_NIVEL4.put("SERVICIO POST-VENTA",                        new String[]{"RECLAMO", "SOLICITUD"});
    }

    public static Performable diligenciar() {
        return instrumented(EjecutarNiveles.class);
    }

    @Override
    @Step("Seleccionar los niveles de clasificación")
    public <T extends Actor> void performAs(T actor) {
        // Nivel 3 al azar; Nivel 4 = una opción válida para ese Nivel 3.
        List<String> nivel3Opciones = new ArrayList<>(NIVEL3_A_NIVEL4.keySet());
        String nivel3 = nivel3Opciones.get(RANDOM.nextInt(nivel3Opciones.size()));
        String[] nivel4Opciones = NIVEL3_A_NIVEL4.get(nivel3);
        String nivel4 = nivel4Opciones[RANDOM.nextInt(nivel4Opciones.length)];

        System.out.println("[Niveles] Nivel 3 = " + nivel3 + "  ->  Nivel 4 = " + nivel4);

        actor.attemptsTo(
                SeleccionarNivel.nivel(3, nivel3),
                SeleccionarNivel.nivel(4, nivel4)
                // TODO: agregar Nivel 5 y 6 cuando se confirmen sus valores dependientes.
        );
    }
}
