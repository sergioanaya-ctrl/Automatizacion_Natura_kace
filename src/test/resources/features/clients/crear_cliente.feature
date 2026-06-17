Feature: Creacion de Clientes en Natura

  @cliente1
  Scenario: Crear Cliente 01 - Datos Aleatorios
    Given el actor tiene un navegador disponible
    When abre la pagina de casos
    And realiza login con credenciales
    And navega a agent
    And diligencia el formulario crear cliente con datos aleatorios
    Then se valida que el cliente fue creado correctamente
    And crea un nuevo caso
    And selecciona los niveles de clasificacion
    And diligencia la descripcion del caso
    And recorre las transiciones de estado del caso
    Then se valida que el caso fue guardado correctamente

  @cliente2
  Scenario: Crear Cliente 02 - Datos Aleatorios
    Given el actor tiene un navegador disponible
    When abre la pagina de casos
    And realiza login con credenciales
    And navega a agent
    And diligencia el formulario crear cliente con datos aleatorios
    Then se valida que el cliente fue creado correctamente
    And crea un nuevo caso

  @cliente3
  Scenario: Crear Cliente 03 - Datos Aleatorios
    Given el actor tiene un navegador disponible
    When abre la pagina de casos
    And realiza login con credenciales
    And navega a agent
    And diligencia el formulario crear cliente con datos aleatorios
    Then se valida que el cliente fue creado correctamente
    And crea un nuevo caso
