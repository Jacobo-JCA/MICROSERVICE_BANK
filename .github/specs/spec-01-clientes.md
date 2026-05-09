---
id: SPEC-01
status: DRAFT
feature: customer-management
created: 2026-05-08
updated: 2026-05-08
version: "1.1"
related-specs: []
---

# Spec: Gestión de Clientes (customer-service)

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.

---

## 1. REQUERIMIENTOS

### Descripción
Gestión integral de la identidad de los clientes del banco. Permite el CRUD de clientes, donde cada cliente hereda los datos base de la clase `Persona`.

### Requerimiento de Negocio
> Fuente: `.github/requeriments/hu-01-gestion-clientes.md`

### Criterios de Aceptación
- Registro de clientes con datos personales (nombre, género, edad, identificación, dirección, teléfono) y contraseña.
- Actualización de información de contacto y seguridad.
- Eliminación de registros.
- Asignación de un `clienteid` único como identificador de negocio.

### Reglas de Negocio
1. `Persona` es una clase base abstracta (`@MappedSuperclass`). **No es una entidad ni tiene tabla propia en la BD.**
2. `Cliente` es la única entidad persistida. Hereda todos los campos de `Persona` y añade `clienteid`, `contraseña` y `estado`.
3. El `clienteid` debe ser único en el sistema (`@Column(unique = true)`). La BD garantiza la unicidad; no se requiere lógica adicional en el Service.
4. El `estado` inicial del cliente es `true` (Activo).

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades (JPA)
- `Persona` (`@MappedSuperclass`): nombre, genero, edad, identificacion, direccion, telefono. (**Sin tabla en BD**)
- `Cliente` (`@Entity`, tabla `clientes`): id (PK), clienteid (Único), contraseña, estado + hereda campos de Persona.

### API Endpoints

#### Clientes (/clientes)
- `GET /clientes`: Lista todos los clientes.
- `GET /clientes/{id}`: Obtiene un cliente por su PK.
- `POST /clientes`: Crea un nuevo cliente.
- `PUT /clientes/{id}`: Actualiza datos completos del cliente.
- `PATCH /clientes/{id}`: Actualización parcial.
- `DELETE /clientes/{id}`: Elimina un cliente.

### Capas de Implementación

#### DTO (Records)
- `ClienteRequest`: nombre, genero, edad, identificacion, direccion, telefono, clienteid, contraseña.
- `ClienteResponse`: id, clienteid, nombre, estado, (demás campos de Persona).

#### Service
- `ClienteService` / `ClienteServiceImpl`: CRUD completo. La unicidad de `clienteid` la garantiza la BD.

#### Controller
- `ClienteController`: Mapeo de rutas `/clientes`.

---

## 3. LISTA DE TAREAS

### DTO
- [ ] Crear `ClienteRequest` (todos los campos de Persona + clienteid + contraseña).
- [ ] Crear `ClienteResponse` (id, clienteid, nombre, estado, demás campos).

### Entity / Repository
- [ ] Crear clase base `Persona` con `@MappedSuperclass`.
- [ ] Crear entidad `Cliente extends Persona` con `@Entity`.
- [ ] Crear `ClienteRepository` (solo para `Cliente`).

### Service
- [ ] Implementar `ClienteService` (interfaz) y `ClienteServiceImpl` (implementación) con CRUD.

### Controller
- [ ] Crear `ClienteController` con los endpoints definidos.

