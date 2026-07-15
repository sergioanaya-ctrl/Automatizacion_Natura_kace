package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.waits.WaitUntil;
import com.natura.automation.ui.InteraccionesPage;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.*;

/**
 * Task para seleccionar una interacción específica de la lista
 */
public class SeleccionarInteraccion implements Task {
    
    private String nombreInteraccion;

    public SeleccionarInteraccion(String nombreInteraccion) {
        this.nombreInteraccion = nombreInteraccion;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.FILA_INTERACCION.of(nombreInteraccion), isPresent())
                .forNoMoreThan(10).seconds(),
            Click.on(InteraccionesPage.FILA_INTERACCION.of(nombreInteraccion))
        );
    }

    public static Performable conNombre(String nombreInteraccion) {
        return instrumented(SeleccionarInteraccion.class, nombreInteraccion);
    }
}
