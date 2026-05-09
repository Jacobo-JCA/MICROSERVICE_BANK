HU-03 — Registro de Movimientos y Validación de Saldo

Como cliente del banco  
Quiero realizar depósitos y retiros en mis cuentas  
Para gestionar mis finanzas personales.

Criterios de aceptación

Registro de Movimientos
    Given una cuenta con un saldo actual
    When se realiza un movimiento (valor positivo para depósito, negativo para retiro)
    Then el sistema registra la transacción con fecha y tipo
    And actualiza el saldo disponible de la cuenta

Validación de Saldo Insuficiente
    Given una cuenta con saldo X
    When se intenta realizar un retiro por un valor mayor a X
    Then el sistema debe rechazar la operación
    And mostrar el mensaje "Saldo no disponible"

Persistencia del Saldo Resultante
    Given se procesa un movimiento exitoso
    When se guarda el registro del movimiento
    Then el campo 'saldo' del movimiento debe reflejar el saldo total de la cuenta tras la operación
