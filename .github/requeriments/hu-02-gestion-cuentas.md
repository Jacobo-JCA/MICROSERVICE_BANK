HU-02 — Gestión de Cuentas

Como administrador del banco  
Quiero gestionar las cuentas bancarias asociadas a los clientes  
Para permitir que los clientes puedan transaccionar con su dinero.

Criterios de aceptación

Creación de Cuentas
    Given un cliente existente (clienteId)
    When se crea una cuenta (Ahorros o Corriente) con un saldo inicial
    Then el sistema genera un número de cuenta único y la asocia al cliente

CRUD de Cuentas
    Given una cuenta existente
    When se requiere editar o eliminar el registro
    Then el sistema permite la operación manteniendo la consistencia de los datos
