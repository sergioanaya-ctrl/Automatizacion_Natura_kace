package com.natura.automation.stepdefinitions;

import io.cucumber.java.en.And;
import net.serenitybdd.screenplay.actors.OnStage;
import com.natura.automation.tasks.*;

/**
 * Step Definitions para interacciones con Workspaces en Natura Agent
 * Estos steps se ejecutan cuando el Cucumber encuentra coincidencias en los archivos .feature
 */
public class InteraccionesStepDefinitions {

    /**
     * Deselecciona el checkbox "Todos los espacios de trabajo"
     * 
     * Step: And deselecciona el checkbox allWorkspaces
     */
    @And("deselecciona el checkbox allWorkspaces")
    public void deseleccionaCheckboxAllWorkspaces() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            DesseleccionarAllWorkspaces.nuevo()
        );
    }

    /**
     * Abre el dropdown de selección de workspace
     * 
     * Step: And abre el dropdown de selección de workspace
     */
    @And("abre el dropdown de selección de workspace")
    public void abreDropdownWorkspace() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            AbrirDropdownWorkspace.nuevo()
        );
    }

    /**
     * Selecciona un workspace específico del dropdown
     * 
     * Step: And selecciona el workspace "Automatizacion"
     */
    @And("selecciona el workspace {string}")
    public void seleccionaWorkspace(String nombreWorkspace) {
        OnStage.theActorInTheSpotlight().attemptsTo(
            SeleccionarWorkspace.conNombre(nombreWorkspace)
        );
    }

    /**
     * Visualiza la lista de interacciones del workspace
     * 
     * Step: And visualiza la lista de interacciones del workspace
     */
    @And("visualiza la lista de interacciones del workspace")
    public void visualizaListaInteracciones() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            VisualizarListaInteracciones.nueva()
        );
    }

    /**
     * Selecciona una interacción específica de la lista
     * 
     * Step: And selecciona la interacción "pruebas java" de la lista
     */
    @And("selecciona la interacción {string} de la lista")
    public void seleccionaInteraccion(String nombreInteraccion) {
        OnStage.theActorInTheSpotlight().attemptsTo(
            SeleccionarInteraccion.conNombre(nombreInteraccion)
        );
    }

    /**
     * Hace clic en "Gestionar" y, si no existe, en "Crear Caso"
     *
     * Step: And da clic en Gestionar o Crear Caso
     */
    @And("da clic en Gestionar o Crear Caso")
    public void hacerClicGestionarOCrearCaso() {
        OnStage.theActorInTheSpotlight().attemptsTo(
            HacerClicGestionarOCrearCaso.nuevo()
        );
    }
}
