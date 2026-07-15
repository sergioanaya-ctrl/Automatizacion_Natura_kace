package com.natura.automation.stepdefinitions;

import com.natura.automation.tasks.EjecutarCrearCaso;
import com.natura.automation.tasks.EjecutarCrearCliente;
import com.natura.automation.tasks.EjecutarDescripcionCaso;
import com.natura.automation.tasks.EjecutarNiveles;
import com.natura.automation.tasks.DiligenciarDatosCorreo;
import com.natura.automation.tasks.LlenarFormularioNC;
import com.natura.automation.tasks.LlenarCamposRequeridosDinamicos;
import com.natura.automation.tasks.LlenarFormularioPlaneamientoComercial;
import com.natura.automation.tasks.LlenarFormularioTransportadora;
import com.natura.automation.tasks.RecorrerTransicionesEstado;
import com.natura.automation.tasks.ValidarCasoGuardado;
import com.natura.automation.tasks.ValidarClienteCreado;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import net.serenitybdd.screenplay.actors.OnStage;

import java.util.List;
import java.util.Map;

// Steps exclusivos de Crear Cliente.
// Login, inicio de navegador y agente los provee LoginDefinitions — no se duplican aquí.
public class CrearClienteStepDefinitions {

    @And("diligencia el formulario crear cliente con datos aleatorios")
    public void diligenciaElFormularioCrearClienteConDatosAleatorios() {
        OnStage.theActorInTheSpotlight().attemptsTo(
                EjecutarCrearCliente.conDatosAleatorios());
    }

    @And("diligencia el formulario crear cliente")
    public void diligenciaElFormularioCrearCliente(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        OnStage.theActorInTheSpotlight().attemptsTo(
                EjecutarCrearCliente.conDatos(rows.get(0)));
    }

    @And("se valida que el cliente fue creado correctamente")
    public void seValidaQueElClienteFueCreadoCorrectamente() {
        OnStage.theActorInTheSpotlight().attemptsTo(ValidarClienteCreado.ahora());
    }

    @And("crea un nuevo caso")
    public void creaUnNuevoCaso() {
        OnStage.theActorInTheSpotlight().attemptsTo(EjecutarCrearCaso.nuevo());
    }

    @And("selecciona los niveles de clasificacion")
    public void seleccionaLosNivelesDeClasificacion() {
        OnStage.theActorInTheSpotlight().attemptsTo(EjecutarNiveles.diligenciar());
    }

    @And("diligencia los datos de correo")
    public void diligenciaLosDatosDeCorreo() {
        OnStage.theActorInTheSpotlight().attemptsTo(DiligenciarDatosCorreo.diligenciar());
    }

    @And("diligencia la descripcion del caso")
    public void diligenciaLaDescripcionDelCaso() {
        OnStage.theActorInTheSpotlight().attemptsTo(EjecutarDescripcionCaso.diligenciar());
    }

    @And("diligencia el formulario NC si aplica")
    public void diligenciaElFormularioNcSiAplica() {
        OnStage.theActorInTheSpotlight().attemptsTo(LlenarFormularioNC.siAplica());
    }

    @And("diligencia el formulario Planeamiento Comercial si aplica")
    public void diligenciaElFormularioPlaneamientoComercialSiAplica() {
        OnStage.theActorInTheSpotlight().attemptsTo(LlenarFormularioPlaneamientoComercial.siAplica());
    }

    @And("diligencia el formulario Datos Transportadora si aplica")
    public void diligenciaElFormularioDatosTransportadoraSiAplica() {
        OnStage.theActorInTheSpotlight().attemptsTo(LlenarFormularioTransportadora.siAplica());
    }

    @And("diligencia los campos requeridos dinamicos si aplican")
    public void diligenciaLosCamposRequeridosDinamicosSiAplican() {
        OnStage.theActorInTheSpotlight().attemptsTo(LlenarCamposRequeridosDinamicos.siAplica());
    }

    @And("recorre las transiciones de estado del caso")
    public void recorreLasTransicionesDeEstadoDelCaso() {
        OnStage.theActorInTheSpotlight().attemptsTo(RecorrerTransicionesEstado.hastaFinalizar());
    }

    @Then("se valida que el caso fue guardado correctamente")
    public void seValidaQueElCasoFueGuardadoCorrectamente() {
        OnStage.theActorInTheSpotlight().attemptsTo(ValidarCasoGuardado.ahora());
    }
}
