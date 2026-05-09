HU-04 — Reporte de Estado de Cuenta

Como cliente del banco  
Quiero generar un reporte de mi estado de cuenta en un rango de fechas  
Para visualizar el detalle de mis movimientos y saldos.

Criterios de aceptación

Generación de Reporte JSON
    Given un clienteId y un rango de fechas
    When se consulta el endpoint /reportes
    Then el sistema retorna un JSON con la información de las cuentas y el detalle de movimientos en ese periodo

Contenido del Reporte
    Given la consulta de reporte es exitosa
    Then el JSON debe incluir: Cuentas asociadas, saldos actuales y el detalle cronológico de movimientos
