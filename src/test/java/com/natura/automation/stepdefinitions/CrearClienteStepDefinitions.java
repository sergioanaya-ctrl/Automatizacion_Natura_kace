package com.natura.automation.stepdefinitions;

import com.natura.automation.tasks.EjecutarCrearCaso;
import com.natura.automation.tasks.EjecutarCrearCliente;
import com.natura.automation.tasks.EjecutarNiveles;
import com.natura.automation.tasks.ValidarClienteCreado;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import net.serenitybdd.screenplay.actors.OnStage;

import java.util.List;
import java.util.Map;

// Steps exclusivos de Crear Cliente.
// Login, inicio de navegador y agente los provee LoginDefinitions — no se duplican aquí.
public class CrearClienteStepDefinitions {

    @Cuando("diligencia el formulario crear cliente con datos aleatorios")
    public void diligenciaElFormularioCrearClienteConDatosAleatorios() {
        OnStage.theActorInTheSpotlight().attemptsTo(
                EjecutarCrearCliente.conDatosAleatorios());
    }

    @Cuando("diligencia el formulario crear cliente")
    public void diligenciaElFormularioCrearCliente(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        OnStage.theActorInTheSpotlight().attemptsTo(
                EjecutarCrearCliente.conDatos(rows.get(0)));
    }

    @Entonces("se valida que el cliente fue creado correctamente")
    public void seValidaQueElClienteFueCreadoCorrectamente() {
        OnStage.theActorInTheSpotlight().attemptsTo(ValidarClienteCreado.ahora());
    }

    @Cuando("crea un nuevo caso")
    public void creaUnNuevoCaso() {
        OnStage.theActorInTheSpotlight().attemptsTo(EjecutarCrearCaso.nuevo());
    }

    @Cuando("selecciona los niveles de clasificacion")
    public void seleccionaLosNivelesDeClasificacion() {
        OnStage.theActorInTheSpotlight().attemptsTo(EjecutarNiveles.diligenciar());
    }
}
