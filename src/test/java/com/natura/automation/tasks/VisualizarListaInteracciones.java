package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.waits.WaitUntil;
import com.natura.automation.ui.InteraccionesPage;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.*;

/**
 * Task para verificar que la lista de interacciones del workspace está visible y cargada
 */
public class VisualizarListaInteracciones implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        // Esperar a que la tabla de interacciones esté presente
        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.LISTA_INTERACCIONES_CARGADA, isPresent())
                .forNoMoreThan(10).seconds()
        );
    }

    public static Performable nueva() {
        return instrumented(VisualizarListaInteracciones.class);
    }
}
