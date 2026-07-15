package com.natura.automation.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.waits.WaitUntil;
import com.natura.automation.ui.InteraccionesPage;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.*;

/**
 * Task para hacer clic en el botón "Crear Caso"
 */
public class HacerClicCrearCaso implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.BTN_CREAR_CASO, isPresent())
                .forNoMoreThan(15).seconds(),
            WaitUntil.the(InteraccionesPage.BTN_CREAR_CASO, isVisible())
                .forNoMoreThan(15).seconds(),
            WaitUntil.the(InteraccionesPage.BTN_CREAR_CASO, isEnabled())
                .forNoMoreThan(15).seconds(),
            Scroll.to(InteraccionesPage.BTN_CREAR_CASO),
            Click.on(InteraccionesPage.BTN_CREAR_CASO)
        );

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Performable nuevo() {
        return instrumented(HacerClicCrearCaso.class);
    }
}
