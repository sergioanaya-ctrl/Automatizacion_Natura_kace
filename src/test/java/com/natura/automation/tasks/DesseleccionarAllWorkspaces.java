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
 * Task para deseleccionar el checkbox "Todos los espacios de trabajo"
 */
public class DesseleccionarAllWorkspaces implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.CHECKBOX_ALL_WORKSPACES, isPresent()).forNoMoreThan(10).seconds(),
            Click.on(InteraccionesPage.CHECKBOX_ALL_WORKSPACES)
        );
    }

    public static Performable nuevo() {
        return instrumented(DesseleccionarAllWorkspaces.class);
    }
}
