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
 * Task para seleccionar un workspace específico del dropdown
 */
public class SeleccionarWorkspace implements Task {
    
    private String nombreWorkspace;

    public SeleccionarWorkspace(String nombreWorkspace) {
        this.nombreWorkspace = nombreWorkspace;
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            WaitUntil.the(InteraccionesPage.WORKSPACE_OPTION.of(nombreWorkspace), isPresent())
                .forNoMoreThan(10).seconds(),
            Click.on(InteraccionesPage.WORKSPACE_OPTION.of(nombreWorkspace))
        );
        
        // Esperar a que se cierre el dropdown y se carguen las interacciones
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Performable conNombre(String nombreWorkspace) {
        return instrumented(SeleccionarWorkspace.class, nombreWorkspace);
    }
}
