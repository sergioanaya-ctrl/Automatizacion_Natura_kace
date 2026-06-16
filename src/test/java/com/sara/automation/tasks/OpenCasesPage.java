package com.sara.automation.tasks;

import com.sara.automation.ui.CasesPage;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Open;
import net.thucydides.core.annotations.Step;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class OpenCasesPage implements Task {

    public static Performable now() {
        return instrumented(OpenCasesPage.class);
    }

    @Override
    @Step("Abre la pagina de casos")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Open.url(CasesPage.URL));
    }
}

