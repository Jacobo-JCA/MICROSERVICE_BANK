Feature: Registro de Movimientos y Validación de Saldo

  Scenario: Validación de Saldo Insuficiente
    Given una cuenta con saldo 1000
    When se intenta realizar un retiro por un valor mayor a 1000
    Then el sistema debe rechazar la operacion
    And mostrar el mensaje "Saldo no disponible"
