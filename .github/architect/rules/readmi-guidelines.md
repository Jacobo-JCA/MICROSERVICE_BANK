# Estructura del README — Sistema Bancario (Microservicios)

Todo proyecto debe contar con un único `README.md` en la raíz que detalle la operación de ambos microservicios.

## Secciones Obligatorias

### 1. Descripción del Sistema
Explicar el propósito del sistema bancario y la separación de responsabilidades entre `customer-service` e `account-service`.

### 2. Arquitectura y Tecnologías
- Lista de microservicios.
- Stack tecnológico (Java 17, Spring Boot 3.4.2, Maven, etc.).
- Diagrama simplificado de comunicación.

### 3. Requisitos Previos
Versiones exactas de Java, Maven y base de datos necesaria.

### 4. Configuración y Variables de Entorno
Listar las variables necesarias para ambos servicios (DB_URL, DB_USER, etc.).

### 5. Guía de Ejecución (Paso a Paso)
- Orden recomendado para levantar los servicios.
- Comandos de Maven para compilar y ejecutar (`mvn spring-boot:run`).
- Puertos por defecto (8081 para customer, 8082 para account).

### 6. Documentación del API
- Enlace al Swagger UI de cada microservicio:
  - `http://localhost:8081/swagger-ui.html` (Customer Service)
  - `http://localhost:8082/swagger-ui.html` (Account Service)

### 7. Endpoints Principales
Muestra ejemplos rápidos de:
- Creación de cliente.
- Creación de cuenta.
- Registro de un movimiento exitoso.
- Registro de un movimiento fallido por saldo insuficiente.
- Generación de reporte.

### 8. Estructura de Carpetas
Detallar la ubicación de cada microservicio en el repositorio.

---

**Reglas de Mantenimiento:**
- Prohibido dejar secciones con `TODO` o `pendiente`.
- El README debe reflejar el estado actual del código en la rama principal.
- Si se agrega un nuevo microservicio, debe integrarse en este mismo README central.