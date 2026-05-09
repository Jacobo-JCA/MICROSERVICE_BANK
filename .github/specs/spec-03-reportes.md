---
id: SPEC-03
status: DRAFT
feature: financial-reports
created: 2026-05-08
updated: 2026-05-08
version: "1.1"
related-specs: [SPEC-01, SPEC-02]
---

# Spec: Reportes de Estado de Cuenta (account-service)

---

## 1. REQUERIMIENTOS

### Descripción
Generación de reportes detallados por cliente y rango de fechas.

### Requerimiento de Negocio
> Fuente: `.github/requeriments/hu-04-generacion-reportes.md`

### API Endpoint
- `GET /reportes?fechaInicio=YYYY-MM-DD&fechaFin=YYYY-MM-DD&clienteId=ID`
  - `fechaInicio` y `fechaFin`: Rango de fechas en formato ISO-8601.
  - `clienteId`: Identificador de negocio del cliente.

### Formato de Salida (JSON)
> El JSON debe respetar la regla de máximo 3 niveles de profundidad.

```json
{
  "cliente": "Nombre Cliente",
  "fechaInicio": "2024-01-01",
  "fechaFin": "2024-01-31",
  "cuentas": [
    {
      "numeroCuenta": "478758",
      "tipo": "Ahorros",
      "saldoInicial": 2000,
      "estado": true,
      "movimientos": [
        {
          "fecha": "2024-01-05T10:00:00",
          "tipoMovimiento": "RETIRO",
          "valor": -575,
          "saldo": 1425
        }
      ]
    }
  ]
}
```

---

## 2. DISEÑO

### Capas de Implementación

#### DTO (Records)
- `ReporteResponse`: Estructura del JSON de salida (3 niveles: cliente → cuentas → movimientos).
- `CuentaReporteDTO`: Datos de la cuenta con su lista de movimientos.
- `MovimientoReporteDTO`: Datos del movimiento filtrado por rango de fechas.

#### Service
- `ReporteService` / `ReporteServiceImpl`: Consulta `CuentaRepository` filtrando por `clienteid`. Consulta `MovimientoRepository` filtrando por `cuenta` y rango de fechas. Obtiene el nombre del cliente desde `customer-service` mediante **WebClient (`Mono<ClienteResponse>`)** de forma reactiva (sin `.block()`).

#### Controller
- `ReporteController`: Expone el endpoint `GET /reportes` con los query params.

---

## 3. LISTA DE TAREAS

### DTO
- [ ] Crear `ReporteResponse` (cliente, fechaInicio, fechaFin, lista de cuentas).
- [ ] Crear `CuentaReporteDTO` (numeroCuenta, tipo, saldoInicial, estado, lista de movimientos).
- [ ] Crear `MovimientoReporteDTO` (fecha, tipoMovimiento, valor, saldo).

### Repository
- [ ] Añadir query en `MovimientoRepository` para filtrar por cuenta y rango de fechas.
- [ ] Añadir query en `CuentaRepository` para obtener cuentas por `clienteid`.

### Service
- [ ] Implementar `ReporteService` (interfaz) y `ReporteServiceImpl`.

### Controller
- [ ] Crear `ReporteController` con el endpoint `GET /reportes`.

