package com.natura.automation.tasks;

import com.natura.automation.ui.InteraccionesPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.waits.WaitUntil;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.*;

/**
 * Task para seleccionar un workspace específico del dropdown.
 */
public class SeleccionarWorkspace implements Task {

    private final String nombreWorkspace;

    public SeleccionarWorkspace(String nombreWorkspace) {
        this.nombreWorkspace = nombreWorkspace;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.WORKSPACE_OPTION.of(nombreWorkspace), isPresent())
                .forNoMoreThan(10).seconds(),
            WaitUntil.the(InteraccionesPage.WORKSPACE_OPTION.of(nombreWorkspace), isVisible())
                .forNoMoreThan(10).seconds(),
            Scroll.to(InteraccionesPage.WORKSPACE_OPTION.of(nombreWorkspace)),
            Click.on(InteraccionesPage.WORKSPACE_OPTION.of(nombreWorkspace))
        );

        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.LISTA_INTERACCIONES_CARGADA, isPresent())
                .forNoMoreThan(10).seconds()
        );
    }

    public static Performable conNombre(String nombreWorkspace) {
        return instrumented(SeleccionarWorkspace.class, nombreWorkspace);
    }
}
