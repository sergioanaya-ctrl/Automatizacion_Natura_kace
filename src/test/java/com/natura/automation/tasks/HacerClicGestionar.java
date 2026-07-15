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
 * Task para hacer clic en el botón "Gestionar" 
 */
public class HacerClicGestionar implements Task {

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            // Esperar a que el botón esté presente
            WaitUntil.the(InteraccionesPage.BTN_GESTIONAR, isPresent())
                .forNoMoreThan(10).seconds(),
            // Esperar a que el botón esté habilitado (no disabled)
            WaitUntil.the(InteraccionesPage.BTN_GESTIONAR, isEnabled())
                .forNoMoreThan(10).seconds(),
            Click.on(InteraccionesPage.BTN_GESTIONAR)
        );
        
        // Esperar a que se procese la acción
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Performable nuevo() {
        return instrumented(HacerClicGestionar.class);
    }
}
