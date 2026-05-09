HU-01 — Gestión de Clientes

Como administrador del banco  
Quiero poder crear, editar, actualizar y eliminar registros de clientes  
Para mantener la base de datos de identidades actualizada.

Criterios de aceptación

Registro de Clientes
    Given los datos de una persona (nombre, identificación, dirección, etc.) y una contraseña
    When se solicita la creación de un nuevo cliente
    Then el sistema guarda la información y asigna un clienteId único
    And el cliente queda en estado activo por defecto

Actualización de Datos
    Given un cliente existente
    When se modifican sus datos personales o contraseña
    Then el sistema actualiza el registro sin perder el histórico de movimientos (si los tuviera)

Eliminación Lógica
    Given un cliente existente
    When se solicita su eliminación
    Then el sistema realiza un borrado asegurando la integridad referencial
