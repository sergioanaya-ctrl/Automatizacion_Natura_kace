package com.natura.automation.tasks;

import com.natura.automation.interactions.SeleccionarNiveles;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.thucydides.core.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;

/**
 * Diligencia el componente "Clasificaciones" (niveles 3 a 6) de forma dinámica:
 * en cada nivel se elige una opción válida al azar entre las que la app muestra.
 */
public class EjecutarNiveles implements Task {

    public static Performable diligenciar() {
        return instrumented(EjecutarNiveles.class);
    }

    @Override
    @Step("Seleccionar los niveles de clasificación")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(SeleccionarNiveles.aleatorios());
    }
}
