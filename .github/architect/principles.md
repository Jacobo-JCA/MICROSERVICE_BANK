# Principios de Diseño — Sistema Bancario

Centraliza los principios de diseño para garantizar que el sistema financiero sea robusto, seguro y escalable.

---

### Código Limpio / Clean Code
**Objetivo:** Producir código legible, mantenible y autoexplicativo.

- **Claridad:** El código debe transmitir su intención con nombres descriptivos alineados al dominio bancario (`withdraw`, `deposit`, `checkBalance`).
- **Responsabilidad Única (SRP):** Las clases y funciones deben hacer una sola cosa. Un `AccountService` gestiona cuentas, no valida direcciones físicas del cliente.
- **Eliminación de Deuda:** No debe existir código duplicado. Las validaciones de saldo deben estar centralizadas.

---

### Principios SOLID
**Objetivo:** Asegurar bajo acoplamiento, alta cohesión y testabilidad.

- **DIP — Dependency Inversion:** Las clases de alto nivel no deben depender de clases concretas. **Toda comunicación entre capas o servicios debe realizarse a través de interfaces.** El `AccountService` depende de la interfaz `CustomerClient`, no de su implementación.
- **OCP — Open/Closed:** El sistema debe permitir nuevos tipos de movimientos sin modificar el código existente, extendiendo interfaces.
- **LSP — Liskov Substitution:** Las subclases o implementaciones deben ser sustituibles por sus interfaces sin alterar el comportamiento.
- **ISP — Interface Segregation:** Definir interfaces granulares para que los clientes no dependan de métodos que no utilizan.

---

### Manejo de Errores y Excepciones
**Objetivo:** Centralizar el control de errores para mantener el código limpio y profesional.

- **Prohibido el uso de `try-catch`**: No se permite capturar excepciones localmente en Controllers o Services a menos que sea estrictamente necesario por requerimientos del compilador (Checked Exceptions).
- **Global Exception Handler**: Todas las excepciones de negocio (ej: `SaldoInsuficienteException`) y de sistema deben ser gestionadas por un `@RestControllerAdvice`.
- **Cohesión en Errores**: Cada excepción personalizada debe mapearse a un código de estado HTTP adecuado y un mensaje claro para el usuario final.

---

### Alta Cohesión y Bajo Acoplamiento
**Objetivo:** Cada microservicio es autónomo pero colabora eficientemente.

- **Alta cohesión:** Todo lo relacionado con transacciones va en `account-service`. Todo lo relacionado con datos del cliente en `customer-service`.
- **Bajo acoplamiento:** Los servicios se comunican por IDs y contratos claros. El `account-service` no conoce la tabla `Persona`.

---

### Reglas de Negocio Críticas
- **Atomicidad:** El registro de un movimiento y la actualización del saldo de la cuenta deben ocurrir en la misma transacción.
- **Validación de Saldo:** Siempre verificar `saldoActual - retiro >= 0`.
- **Integridad:** El saldo guardado en un `Movimiento` debe reflejar el estado final de la cuenta tras esa operación exacta.