---
applyTo: "**/src/main/java/**/*.java"
---

> **Scope**: Sistema Bancario de Microservicios. Java 17 / Spring Boot 3.4.2 / Maven.

# Instrucciones Detalladas para Backend — Sistema Bancario

## Stack Tecnológico y Estándares

- **Java 17**: Records, Switch Expressions, Sealed Classes.
- **Spring Boot 3.4.2**: Web, Data JPA, Validation.
- **Persistencia**: **Spring Data JPA (Hibernate)**. Uso obligatorio de repositorios que hereden de `JpaRepository`.
- **Base de Datos**: PostgreSQL o MySQL.
- **Comunicación**: **Spring Cloud OpenFeign (Feign Client)** obligatorio para la comunicación entre microservicios.
- **Clarificación**: Si el implementador tiene dudas sobre cualquier aspecto técnico o de negocio durante la construcción, **debe detenerse y preguntar al usuario** antes de proceder con suposiciones.

---

## 1. Patrón Repository y Capa de Datos (Buenas Prácticas)

- **Entidades JPA**:
  - Usar `@Entity` y `@Table(name = "snake_case")`.
  - Atributos siempre en `camelCase`.
  - Relaciones `@OneToMany`, `@ManyToOne` con `FetchType.LAZY` por defecto para evitar problemas de rendimiento.
  - Implementar `equals()` y `hashCode()` usando solo el ID para evitar problemas con proxies de Hibernate.
- **Repositorios**:
  - No escribir lógica de negocio en queries SQL complejas si se puede hacer en el Service.
  - Usar `@Query` solo cuando sea necesario por rendimiento.
  - Las consultas de búsqueda deben retornar `Optional<T>` o `List<T>`.

---

## 1.A. Modelo OOP por Microservicio (Mandatorio)

### `customer-service` — Herencia (Inheritance)

La jerarquía `Persona → Cliente` **debe implementarse con `@MappedSuperclass`**, de modo que `Persona` no sea una tabla en la base de datos, sino una base para la entidad `Cliente`:

```java
@MappedSuperclass
public abstract class Persona {
    private String nombre;
    private String genero;
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;
}

@Entity
@Table(name = "clientes")
public class Cliente extends Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK de la entidad Cliente

    @Column(unique = true, nullable = false)
    private String clienteid; // Clave única de negocio
    
    private String contraseña;
    private Boolean estado;
}
```

- **Persona**: Clase base (No persistida como tabla independiente).
- **Cliente**: Única entidad persistida en la base de datos para este módulo.
- **Regla**: El `clienteid` es el identificador único de negocio, mientras que `id` es la PK técnica.

---

### `account-service` — Composición (Composition)

Las entidades de este microservicio deben seguir esta estructura exacta:

```java
@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    private String numeroCuenta; // Clave única (PK)
    private String tipoCuenta;   // Ahorro / Corriente
    private BigDecimal saldoInicial;
    private Boolean estado;
    
    // Campo para la relación lógica con customer-service
    private String clienteid;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Movimiento> movimientos = new ArrayList<>();
}

@Entity
@Table(name = "movimientos")
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Clave única (PK)
    
    private LocalDateTime fecha;
    private String tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal saldo; // Saldo disponible después del movimiento

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "numero_cuenta", nullable = false)
    private Cuenta cuenta;
}
```

- **Cuenta**: Maneja su clave única (`numeroCuenta`), tipo, saldo inicial y estado. El `numeroCuenta` es la `@Id`, por lo que la base de datos rechazará duplicados automáticamente. No añadir lógica extra en el Service para esto.
- **Movimientos**: Entidad con clave única, maneja fecha, tipo, valor y el saldo resultante.
- **Composición**: Los movimientos no existen sin la cuenta (`orphanRemoval = true`).

- `orphanRemoval = true` garantiza que al eliminar una `Cuenta` se eliminan todos sus `Movimientos` (composición estricta).
- Un `Movimiento` **no puede existir sin su `Cuenta`** (`nullable = false` obligatorio en el `@JoinColumn`).

---

## 2. Capa de Servicio (SOLID y Lógica)

- **Interfaces**: Cada servicio debe tener una interfaz en `service/` y su implementación en `service/impl/`.
- **Inyección**: Solo por constructor.
- **Mapeo DTO-Entity**: 
  - No usar las Entidades JPA en los Controladores.
  - Realizar la conversión en el Service o usar una capa de `Mapper`.
  - Usar Java Records para los DTOs para garantizar inmutabilidad.
- **Validación**: Usar Bean Validation (`@NotNull`, `@Positive`, etc.) en los Records de entrada.
- **Modularidad de Métodos**: Si un método de servicio excede las 15-20 líneas, debe ser refactorizado en métodos privados más pequeños y cohesivos. Cada método debe hacer una sola cosa.
- **Programación Funcional**: Se debe priorizar el uso de **expresiones lambda** y la API de **Streams** de Java para el procesamiento de colecciones, mapeos y filtrados, siempre que mejore la legibilidad y mantenibilidad.

---

## 3. Manejo Global de Excepciones (Mandatorio)

- **Excepciones de Negocio**: Crear excepciones que extiendan de `RuntimeException` (ej: `SaldoInsuficienteException`, `RecursoNoEncontradoException`).
- **Mensajes**: Las excepciones deben contener mensajes claros y descriptivos que se mostrarán al usuario final.
- **GlobalExceptionHandler**:
  - Implementar una clase con `@RestControllerAdvice`.
  - Capturar `MethodArgumentNotValidException` para errores de validación de campos.
  - Capturar las excepciones de negocio y retornar el código HTTP correcto (400 para saldo, 404 para no encontrado).
  - **PROHIBIDO**: Bloques `try-catch` manuales para control de flujo.

---

## 4. Capa de Controlador (REST API)

- **Orientación a Recursos (Sustantivos en plural)**: REST es orientado a recursos, es decir, en el diseño de las rutas (paths) se deben usar sustantivos en plural y **nunca verbos**.
  - **Correcto:** `/v1/users`
  - **Incorrecto:** `/createUsers`
- **Profundidad Máxima en URLs**: Las rutas no deben tener más de tres niveles de anidamiento de recursos. 
  - **Límite aceptable:** `/clients/{id}/accounts/{id}/movements/`
  - Superar los 3 niveles es considerado una mala práctica.
- **Profundidad Máxima en JSON**: Las estructuras de datos o cargas útiles (payloads JSON) no deben estar excesivamente anidadas; se permite como **máximo 3 niveles de profundidad**.
- **Verbos HTTP**: Implementar correctamente el uso semántico de los verbos:
  - `GET`: Para consultas y obtención de recursos (no modifica estado).
  - `POST`: Para creación de nuevos recursos.
  - `PUT`: Para actualización completa de un recurso existente.
  - `PATCH`: Para actualizaciones parciales de un recurso.
  - `DELETE`: Para eliminación de recursos.
- **Endpoints Requeridos**:
  - `/clientes`: CRUD completo de clientes (customer-service).
  - `/cuentas`: CRUD completo de cuentas (account-service).
  - `/movimientos`: Registro y consulta de movimientos (account-service).
  - `/reportes?fecha=rango fechas&clienteId=...`: Generación de estado de cuenta en formato JSON (account-service).
- **Códigos de Estado**:
  - `200 OK` para éxitos generales.
  - `201 Created` para POST exitosos.
  - `204 No Content` para DELETE exitosos sin cuerpo de respuesta.
  - `400 Bad Request` para errores de negocio (ej: Saldo insuficiente).
  - `404 Not Found` cuando el recurso no existe.
- **Respuestas Envolventes**: Todos los métodos del controlador deben retornar un objeto **`ResponseEntity<T>`** para tener control total sobre el código de estado y los headers. Nunca retornar el DTO directamente.
- **Inyección**: Inyectar las interfaces de los servicios por constructor.
- **Prohibido**: Bloques `try-catch` y lógica de negocio.

---

## 5. Arquitectura y Cohesión

- **Bajo Acoplamiento**: Los servicios no se conocen entre sí por base de datos. La comunicación entre microservicios desde `account-service` hacia `customer-service` se realiza con **WebClient** de forma reactiva.
- **Alta Cohesión**: Cada microservicio tiene su propio modelo de datos y lógica. No hay "tablas compartidas".
- **Transactional**: Usar `@Transactional` en métodos de Service que realicen más de una operación de escritura para garantizar la integridad (Atomicidad).
- **Eliminación Lógica**: El borrado es siempre lógico (`estado = false`). Nunca se elimina físicamente un registro ni se propaga la eliminación entre microservicios.

---

## 5.A. Comunicación Reactiva con WebClient

El `account-service` usa **WebClient** únicamente en los siguientes casos. El `customer-service` **nunca** llama al `account-service`.

| Caso | Endpoint | Operación con WebClient |
|------|----------|-------------------------|
| Crear cuenta | `POST /cuentas` | Validar que el `clienteid` existe antes de persistir. |
| Generar reporte | `GET /reportes` | Obtener el nombre del cliente para incluirlo en el JSON. |

```java
// Ejemplo: Validar cliente con WebClient (NO usar .block())
public Mono<ClienteResponse> validarCliente(String clienteid) {
    return webClient.get()
        .uri("/clientes/{id}", clienteid)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError,
            response -> Mono.error(new RecursoNoEncontradoException("Cliente no encontrado: " + clienteid)))
        .bodyToMono(ClienteResponse.class);
}
```

- `WebClient` se configura como `@Bean` en una clase `WebClientConfig`.
- **Prohibido usar `.block()`** — el flujo reactivo debe mantenerse hasta el controlador.
- El resto de endpoints usan HTTP síncrono normal (JPA, repositorios, lógica de servicio estándar).

---

## 6. Reglas de Negocio Críticas (Implementación)

### Lógica de Cálculos y Validación (account-service)

Para procesar cualquier movimiento, se deben seguir estas reglas exactas:

1. **Obtener `saldoActual`**: 
   - Se debe consultar el último movimiento de la cuenta (ordenado por fecha descendente).
   - Si no existen movimientos, el `saldoActual` es igual al `saldoInicial` de la cuenta.

2. **Cálculo del `saldoNuevo`**:
   - `saldoNuevo = saldoActual + valor` (donde `valor` es positivo para depósitos y negativo para retiros).

3. **Validación**:
   - **Depósitos**: Siempre se permiten.
   - **Retiros**: Si `saldoNuevo < 0`, se debe lanzar una excepción de negocio con el mensaje **"Saldo no disponible"**.

4. **Persistencia del Movimiento**:
   - `movimiento.valor`: El valor exacto recibido en la petición (+600, -575, etc).
   - `movimiento.saldo`: El `saldoNuevo` calculado.

**Ejemplo de implementación en el Service:**
```java
@Transactional
public MovimientoResponse registrarMovimiento(MovimientoRequest request) {
    Cuenta cuenta = cuentaRepository.findByNumeroCuenta(request.numeroCuenta())
        .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada"));

    // 1. Obtener saldoActual (último movimiento o saldoInicial)
    BigDecimal saldoActual = movimientoRepository.findTopByCuentaOrderByFechaDesc(cuenta)
        .map(Movimiento::getSaldo)
        .orElse(cuenta.getSaldoInicial());

    // 2. Calcular saldoNuevo
    BigDecimal valor = request.valor();
    BigDecimal saldoNuevo = saldoActual.add(valor);

    // 3. Validar retiro
    if (valor.compareTo(BigDecimal.ZERO) < 0 && saldoNuevo.compareTo(BigDecimal.ZERO) < 0) {
        throw new SaldoInsuficienteException("Saldo no disponible");
    }

    // 4. Guardar movimiento con los valores calculados
    Movimiento movimiento = new Movimiento();
    movimiento.setFecha(LocalDateTime.now());
    movimiento.setValor(valor);
    movimiento.setSaldo(saldoNuevo);
    movimiento.setCuenta(cuenta);
    
    return mapper.toResponse(movimientoRepository.save(movimiento));
}
```

---

## NUNCA HACER (Checklist de Calificación)

1. **NO** usar `@Autowired` en campos (Inyección por constructor obligatoria).
2. **NO** usar `double` o `float` para montos (BigDecimal obligatorio).
3. **NO** usar `try-catch` para ocultar errores o manejar lógica de negocio.
4. **NO** exponer entidades JPA directamente en los controladores.
5. **NO** ignorar los mensajes de error; deben ser claros y estar centralizados.
6. **NO** omitir la interfaz en la capa de servicio.
7. **NO** realizar suposiciones técnicas sin preguntar al usuario.