package com.sara.automation.tasks;

import com.sara.automation.ui.CasoCreatePage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;
import net.serenitybdd.screenplay.actions.Scroll;
import net.serenitybdd.screenplay.targets.Target;
import net.serenitybdd.screenplay.waits.WaitUntil;
import net.thucydides.core.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;
import static net.serenitybdd.screenplay.matchers.WebElementStateMatchers.isVisible;

public class SelectManualLists implements Task {

    private final String departamento;
    private final String municipio;
    private final String serviciosEspeciales;
    private final String gestor;
    private final String linea;
    private final String servicio;

    public SelectManualLists(String departamento, String municipio, String serviciosEspeciales, String gestor, String linea, String servicio) {
        this.departamento = departamento;
        this.municipio = municipio;
        this.serviciosEspeciales = serviciosEspeciales;
        this.gestor = gestor;
        this.linea = linea;
        this.servicio = servicio;
    }

    public static Performable withValues(String departamento, String municipio, String serviciosEspeciales, String gestor, String linea, String servicio) {
        return instrumented(SelectManualLists.class, departamento, municipio, serviciosEspeciales, gestor, linea, servicio);
    }

    @Override
    @Step("Selecciona listas manuales respetando la secuencia de la pantalla")
    public <T extends Actor> void performAs(T actor) {
        // Esta tarea asume que ya estamos dentro del iframe OneScript.

        // 1) Seccion General
        seleccionar(actor, CasoCreatePage.Departamento_Solicita_Combo, departamento);
        seleccionar(actor, CasoCreatePage.Municipio_Solicita_Combo, municipio);

        if (serviciosEspeciales != null && !serviciosEspeciales.isEmpty()) {
            seleccionar(actor, CasoCreatePage.Servicios_Especiales_Combo, serviciosEspeciales);
        }

        // 2) Seccion Asignacion
        actor.attemptsTo(Scroll.to(CasoCreatePage.Seccion_Asignacion));
        actor.attemptsTo(WaitUntil.the(CasoCreatePage.Gestor_Coordinacion_Combo, isVisible()).forNoMoreThan(15).seconds());

        seleccionar(actor, CasoCreatePage.Gestor_Coordinacion_Combo, gestor);
        seleccionar(actor, CasoCreatePage.Linea_Combo, linea);
        seleccionar(actor, CasoCreatePage.Servicio_Combo, servicio);
    }

    private <T extends Actor> void seleccionar(T actor, Target combo, String valor) {
        actor.attemptsTo(WaitUntil.the(combo, isVisible()).forNoMoreThan(20).seconds());
        actor.attemptsTo(Click.on(combo));

        try {
            actor.attemptsTo(WaitUntil.the(CasoCreatePage.CustomDropdownSearch, isVisible()).forNoMoreThan(3).seconds());
            actor.attemptsTo(Enter.theValue(valor).into(CasoCreatePage.CustomDropdownSearch));
            actor.attemptsTo(WaitUntil.the(CasoCreatePage.CustomDropdownListItem.of(valor), isVisible()).forNoMoreThan(10).seconds());
            actor.attemptsTo(Click.on(CasoCreatePage.CustomDropdownListItem.of(valor)));
            return;
        } catch (Exception ignore) {
            // Continua a estrategia estandar.
        }

        try {
            actor.attemptsTo(WaitUntil.the(CasoCreatePage.Opcion_Lista.of(valor), isVisible()).forNoMoreThan(10).seconds());
            actor.attemptsTo(Click.on(CasoCreatePage.Opcion_Lista.of(valor)));
        } catch (Exception e) {
            actor.attemptsTo(WaitUntil.the(CasoCreatePage.Opcion_Lista_Contiene.of(valor), isVisible()).forNoMoreThan(10).seconds());
            actor.attemptsTo(Click.on(CasoCreatePage.Opcion_Lista_Contiene.of(valor)));
        }
    }
}
