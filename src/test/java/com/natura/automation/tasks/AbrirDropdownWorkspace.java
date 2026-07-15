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
 * Task para abrir el dropdown de selección de workspace.
 */
public class AbrirDropdownWorkspace implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.BTN_SELECT_WORKSPACE_DROPDOWN, isPresent()).forNoMoreThan(10).seconds(),
            WaitUntil.the(InteraccionesPage.BTN_SELECT_WORKSPACE_DROPDOWN, isEnabled()).forNoMoreThan(10).seconds(),
            Scroll.to(InteraccionesPage.BTN_SELECT_WORKSPACE_DROPDOWN),
            Click.on(InteraccionesPage.BTN_SELECT_WORKSPACE_DROPDOWN)
        );
    }

    public static Performable nuevo() {
        return instrumented(AbrirDropdownWorkspace.class);
    }
}
