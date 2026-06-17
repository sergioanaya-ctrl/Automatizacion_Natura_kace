package com.natura.automation.stepdefinitions;

import com.natura.automation.tasks.EjecutarCrearCaso;
import com.natura.automation.tasks.EjecutarCrearCliente;
import com.natura.automation.tasks.EjecutarDescripcionCaso;
import com.natura.automation.tasks.EjecutarNiveles;
import com.natura.automation.tasks.RecorrerTransicionesEstado;
import com.natura.automation.tasks.ValidarCasoGuardado;
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

    @Cuando("diligencia la descripcion del caso")
    public void diligenciaLaDescripcionDelCaso() {
        OnStage.theActorInTheSpotlight().attemptsTo(EjecutarDescripcionCaso.diligenciar());
    }

    @Cuando("recorre las transiciones de estado del caso")
    public void recorreLasTransicionesDeEstadoDelCaso() {
        OnStage.theActorInTheSpotlight().attemptsTo(RecorrerTransicionesEstado.hastaFinalizar());
    }

    @Entonces("se valida que el caso fue guardado correctamente")
    public void seValidaQueElCasoFueGuardadoCorrectamente() {
        OnStage.theActorInTheSpotlight().attemptsTo(ValidarCasoGuardado.ahora());
    }
}
