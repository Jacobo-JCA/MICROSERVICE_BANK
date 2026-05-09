## Arquitectura (OBLIGATORIO)

> **Regla**: Este proyecto es una arquitectura de microservicios. Cada microservicio debe seguir un patrón modular por capas internamente.

| Microservicio | Responsabilidad | Arquitectura Interna | Comunicación |
|---------------|-----------------|----------------------|--------------|
| `customer-service` | Gestión de Personas y Clientes. "Guardián de identidades". | Modular por Capas | Síncrona (REST) |
| `account-service` | Gestión de Cuentas y Movimientos. "Guardián del dinero". | Modular por Capas | Asíncrona (Feign/WebClient/Messaging) |

---

## Modelo de Dominio por Microservicio

### Microservicio 1 — `customer-service` (Personas)

> **Patrón OOP: Herencia**  
> La entidad `Cliente` hereda de `Persona`. Esto debe representarse en JPA usando `@Inheritance`.

| Clase | Tipo | Atributos |
|-------|------|-----------|
| `Persona` | Clase Base (Abstracta) | `nombre`, `genero`, `edad`, `identificacion`, `direccion`, `telefono` |
| `Cliente` | Entidad Persistida | `id` (PK), `clienteid` (Clave Única), `contraseña`, `estado` + atributos de Persona |

- La relación entre `Customer-Service` y `Account-Service` es: **1 cliente → N cuentas**.
- El `clienteId` es la referencia que viaja al `account-service`.

---

### Microservicio 2 — `account-service` (Finanzas)

> **Patrón OOP: Composición**  
> `Movimiento` existe **solo dentro del ciclo de vida de una `Cuenta`**. Si la `Cuenta` se elimina, sus `Movimientos` desaparecen. Esto debe representarse con `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` en la entidad `Cuenta`.

| Clase | Atributos |
|-------|-----------|
| `Cuenta` | `numeroCuenta` (PK), `tipoCuenta`, `saldoInicial`, `estado`, `clienteid` (referencia lógica) |
| `Movimiento` | `id` (PK), `fecha`, `tipoMovimiento`, `valor`, `saldo` (saldo después), `numeroCuenta` (FK) |

- La relación es: **1 cuenta → N movimientos**.
- Un `Movimiento` **no puede existir sin su `Cuenta`** (composición estricta).

---

## Reglas de Negocio Clave (Mandatorias)

1. **Validación de Saldo**: Un retiro no puede dejar saldo negativo. Se debe obtener el `saldoActual` del último movimiento (o `saldoInicial` si es el primero) y validar que `saldoActual + valor >= 0`.
2. **Cálculo de Movimientos**: Cada movimiento debe registrar el `valor` de la transacción (+/-) y el `saldo` resultante calculado a partir del último estado de la cuenta.
3. **Identificadores Únicos**: El `clienteid` y el `numeroCuenta` deben ser únicos. Esta restricción debe ser delegada a la base de datos mediante el uso de constraints `UNIQUE` o `PRIMARY KEY`. No se requiere lógica de validación manual en el Service para duplicados; se debe permitir que la excepción de persistencia fluya al manejador global.
4. **Una cuenta pertenece a exactamente 1 cliente**; el `clienteid` en la `Cuenta` es inmutable tras su creación.

---

## Requerimientos Funcionales y Endpoints

### F1: Operaciones CRUD
Se deben implementar las operaciones de Crear, Leer, Actualizar y Eliminar para las entidades principales en sus respectivos microservicios:
- **`customer-service`**:
  - `/clientes`: Gestión de clientes.
- **`account-service`**:
  - `/cuentas`: Gestión de cuentas.
  - `/movimientos`: Gestión y registro de movimientos.

### F2 y F3: Registro de Movimientos y Validación de Saldo
- Soporte para valores positivos (depósitos) y negativos (retiros).
- Cada movimiento debe actualizar el saldo disponible y quedar registrado en el historial.
- **Validación**: Si un retiro excede el saldo disponible, se debe retornar el error **"Saldo no disponible"** con el código de estado correspondiente (400 Bad Request).

### F4: Reportes de Estado de Cuenta
- **Endpoint**: `/reportes?fecha=rango fechas&clienteId=...` (en `account-service`).
- **Funcionalidad**: Retorna un JSON con las cuentas del cliente, sus saldos actuales y el detalle de movimientos en el rango de fechas especificado.

---

## Arquitectura Modular por Capas (Interna de cada Microservicio)

| Capa | Responsabilidad | Prohibido |
|------|-----------------|-----------|
| **DTO / Entity** | Records para transferencia y Clases JPA para persistencia. | Lógica de negocio |
| **Repository** | Interfaces Spring Data JPA para acceso a datos. | Lógica de negocio |
| **Service (Interface + Impl)** | Lógica de negocio y orquestación. **Uso de Interfaces obligatorio.** | Acceso directo a HTTP |
| **Controller** | Endpoints REST, delegación al service. **Inyecta Interfaces.** | Lógica de negocio, `try-catch` |
| **Config / Client** | Configuración de Beans y clientes. | Lógica de negocio |

---

## Reglas de Comunicación entre Microservicios

- El `account-service` es el **único que hace llamadas externas**. El `customer-service` **nunca llama** a `account-service`.
- El `account-service` solo conoce el `clienteid`; nunca accede a datos personales directamente.

### Cuándo usar WebClient (Reactivo — `Mono`)
`account-service` debe utilizar **WebClient** exclusivamente en estos dos casos:

| Caso | Endpoint | Acción |
|------|----------|--------|
| Crear cuenta | `POST /cuentas` | Validar que el `clienteid` existe en `customer-service` antes de persistir la cuenta. |
| Generar reporte | `GET /reportes` | Obtener el nombre del cliente desde `customer-service` para incluirlo en la respuesta JSON. |

- WebClient debe retornar `Mono<T>` y manejarse de forma **reactiva**. **Prohibido el uso de `.block()`**.
- El resto de endpoints son operaciones internas y usan HTTP síncrono normal, sin WebClient.

### Eliminación Lógica (sin cascada física)
- El **borrado es lógico**: `estado = false` en cada entidad de forma independiente.
- `customer-service` y `account-service` manejan su propio estado de eliminación sin propagación entre servicios.

---

## Patrón de DI y Manejo de Errores
- **Inyección por constructor exclusivamente.**
- **Prohibido el uso de `try-catch`** en lógica de negocio o controladores.
- **GlobalExceptionHandler obligatorio** en cada microservicio usando `@RestControllerAdvice`.

---

## Restricciones
- NO generar tests (responsabilidad del agente `craftsman`).
- Los DTOs son siempre Java Records.
- Las entidades JPA deben ser clases tradicionales con getters/setters.
- El saldo de una cuenta NO se calcula sumando movimientos; se mantiene un saldo actual actualizado atómicamente.
- El saldo en el `Movimiento` es el saldo RESULTANTE después de la operación.