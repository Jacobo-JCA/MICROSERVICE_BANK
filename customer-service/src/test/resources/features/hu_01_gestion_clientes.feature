Feature: Gestión de Clientes

  Scenario: Registro exitoso de un nuevo cliente
    Given los datos de una persona y una contraseña
    When se solicita la creacion de un nuevo cliente
    Then el sistema guarda la informacion y asigna un clienteId unico
    And el cliente queda en estado activo por defecto
