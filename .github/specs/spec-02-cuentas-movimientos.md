---
id: SPEC-02
status: DRAFT
feature: account-movements
created: 2026-05-08
updated: 2026-05-08
version: "1.1"
related-specs: [SPEC-01]
---

# Spec: Gestión de Cuentas y Movimientos (account-service)

> **Estado:** `DRAFT`

---

## 1. REQUERIMIENTOS

### Descripción
Gestión de cuentas bancarias (CRUD) y registro de transacciones (depósitos/retiros) con validación de saldo. Las cuentas pertenecen a clientes gestionados en `customer-service`.

### Requerimiento de Negocio
> Fuente: `.github/requeriments/hu-02-gestion-cuentas.md`, `.github/requeriments/hu-03-registro-movimientos.md`

### Reglas de Negocio
1. **Obtención del Saldo Actual**: Para cualquier movimiento, el `saldoActual` se obtiene del campo `saldo` del **último movimiento** de esa cuenta (ordenado por fecha descendente). Si no existen movimientos previos, se usa el `saldoInicial` de la cuenta.
2. **Fórmula de Cálculo**: `saldoNuevo = saldoActual + valor` (valor positivo = depósito, negativo = retiro).
3. **Validación de Saldo**: Si `valor < 0` y `saldoNuevo < 0`, lanzar excepción con el mensaje literal **"Saldo no disponible"** (400 Bad Request).
4. **Persistencia del Movimiento**: Guardar `movimiento.valor` (el valor original recibido) y `movimiento.saldo` (el `saldoNuevo` calculado).
5. **Composición**: Un `Movimiento` no puede existir sin su `Cuenta`.
6. **Duplicados**: El `numeroCuenta` es la `@Id` de la entidad `Cuenta`. La BD rechaza duplicados automáticamente; no se necesita lógica extra en el Service.
7. **Eliminación Lógica**: El borrado es lógico (`estado = false`). No se elimina físicamente ningún registro ni se propaga la eliminación hacia otros microservicios.
8. **Comunicación**: Este servicio solo conoce el `clienteid`. Utiliza **WebClient (reactivo, `Mono`)** para llamadas hacia `customer-service` en estos casos:
   - `POST /cuentas`: Validar que el `clienteid` existe antes de crear la cuenta.
   - `GET /reportes`: Obtener el nombre del cliente para incluirlo en la respuesta.
   - **Prohibido `.block()`**. El `customer-service` nunca llama al `account-service`.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades (JPA)
- `Cuenta` (`@Entity`, tabla `cuentas`): numeroCuenta (PK), tipoCuenta (AHORROS/CORRIENTE), saldoInicial, estado, clienteid.
- `Movimiento` (`@Entity`, tabla `movimientos`): id (PK, auto), fecha, tipoMovimiento (DEPOSITO/RETIRO), valor, saldo (saldo resultante), numeroCuenta (FK).

#### Relación (Composición)
- `Cuenta` tiene `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` sobre `Movimiento`.
- `Movimiento` tiene `@ManyToOne(optional = false)` con `@JoinColumn(nullable = false)` hacia `Cuenta`.

### API Endpoints

#### Cuentas (/cuentas)
- `GET /cuentas`: Lista todas las cuentas.
- `GET /cuentas/{numeroCuenta}`: Obtiene una cuenta por su número.
- `POST /cuentas`: Crea una nueva cuenta.
- `PUT /cuentas/{numeroCuenta}`: Actualiza datos de la cuenta.
- `PATCH /cuentas/{numeroCuenta}`: Actualización parcial.
- `DELETE /cuentas/{numeroCuenta}`: Elimina la cuenta y sus movimientos (composición).

#### Movimientos (/movimientos)
- `GET /movimientos`: Lista todos los movimientos.
- `GET /movimientos/{id}`: Obtiene un movimiento por su ID.
- `POST /movimientos`: Registra un nuevo movimiento (aplica lógica de saldo).
- `PUT /movimientos/{id}`: Actualiza datos de un movimiento.
- `PATCH /movimientos/{id}`: Actualización parcial.
- `DELETE /movimientos/{id}`: Elimina un movimiento.

### Manejo de Errores
- `@RestControllerAdvice` (`GlobalExceptionHandler`) captura `SaldoInsuficienteException` y retorna 400 con el mensaje "Saldo no disponible".
- Captura `RecursoNoEncontradoException` y retorna 404.

---

## 3. LISTA DE TAREAS

### Entity / Repository
- [ ] Crear entidad `Cuenta` con composición hacia `Movimiento`.
- [ ] Crear entidad `Movimiento` con FK a `Cuenta`.
- [ ] Crear `CuentaRepository` y `MovimientoRepository`.
- [ ] Añadir `MovimientoRepository.findTopByCuentaOrderByFechaDesc(Cuenta)` para obtener el último saldo.

### Service
- [ ] Implementar `CuentaService` (interfaz) y `CuentaServiceImpl` con CRUD.
- [ ] Implementar `MovimientoService` (interfaz) y `MovimientoServiceImpl` con la lógica de validación y cálculo de saldo.
- [ ] Configurar `WebClientConfig` (bean de `WebClient` apuntando a `customer-service`).
- [ ] Implementar `CustomerWebClient` con método para validar existencia del cliente (retorna `Mono<ClienteResponse>`).

### Controller
- [ ] Crear `CuentaController` con los endpoints definidos.
- [ ] Crear `MovimientoController` con los endpoints definidos.
- [ ] Crear `GlobalExceptionHandler` con `@RestControllerAdvice`.

