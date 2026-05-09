# Sistema Bancario de Microservicios

Este proyecto implementa un sistema bancario distribuido utilizando arquitectura de microservicios con Spring Boot.

## Descripción del Sistema

El sistema consta de dos microservicios principales:
- **customer-service**: Gestiona la identidad de los clientes del banco.
- **account-service**: Maneja cuentas bancarias y movimientos financieros con validación de saldo.

## Arquitectura y Tecnologías

### Microservicios
- `customer-service` (Puerto 8081): Gestión de clientes usando herencia JPA.
- `account-service` (Puerto 8082): Gestión de cuentas y movimientos usando composición JPA.

### Stack Tecnológico
- Java 17
- Spring Boot 3.3.0
- Spring Data JPA con Hibernate
- H2 Database (desarrollo)
- Spring WebFlux para comunicación reactiva
- Maven para gestión de dependencias
- Resilience4j para circuit-breaker

### Comunicación
- Comunicación síncrona REST entre servicios.
- `account-service` utiliza WebClient reactivo para validar clientes y obtener datos.

## Requisitos Previos
- Java 17 o superior
- Maven 3.6+

## Configuración y Variables de Entorno
Los servicios utilizan configuración por defecto en `application.yml`. Para producción, configurar:
- `spring.datasource.url`: URL de base de datos PostgreSQL/MySQL
- `spring.datasource.username` y `password`: Credenciales de BD

## Guía de Ejecución
1. Construir ambos servicios:
   ```bash
   cd customer-service && mvn clean package
   cd ../account-service && mvn clean package
   ```
2. Ejecutar customer-service:
   ```bash
   cd customer-service && mvn spring-boot:run
   ```
3. En otra terminal, ejecutar account-service:
   ```bash
   cd account-service && mvn spring-boot:run
   ```

## Documentación del API
- Customer Service: http://localhost:8081/swagger-ui.html
- Account Service: http://localhost:8082/swagger-ui.html

## Endpoints Principales

### Customer Service
- `POST /customers`: Crear cliente
- `GET /customers`: Listar clientes

### Account Service
- `POST /accounts`: Crear cuenta (valida cliente existente)
- `POST /movements`: Registrar movimiento con validación de saldo
- `GET /reports?clienteId=...&fechaInicio=...&fechaFin=...`: Generar reporte de estado de cuenta

### Ejemplos de Uso
1. Crear cliente:
   ```json
   POST http://localhost:8081/customers
   {
     "name": "Juan Perez",
     "gender": "M",
     "age": 30,
     "identification": "123456789",
     "address": "Calle 123",
     "phone": "555-1234",
     "clienteId": "CLI001",
     "password": "secret"
   }
   ```

2. Crear cuenta:
   ```json
   POST http://localhost:8082/accounts
   {
     "numeroCuenta": "ACC001",
     "tipoCuenta": "AHORROS",
     "saldoInicial": 1000.00,
     "estado": true,
     "clienteid": "CLI001"
   }
   ```

3. Registrar depósito:
   ```json
   POST http://localhost:8082/movements
   {
     "numeroCuenta": "ACC001",
     "tipoMovimiento": "DEPOSITO",
     "valor": 500.00
   }
   ```

4. Generar reporte:
   ```
   GET http://localhost:8082/reports?clienteId=1&startDate=2026-05-01&endDate=2026-05-31
   ```

## Estructura de Carpetas
```
solution/
├── customer-service/
│   ├── src/main/java/com/sofka/customers/
│   ├── pom.xml
│   └── README.md
├── account-service/
│   ├── src/main/java/com/sofka/accounts/
│   ├── pom.xml
│   └── README.md
└── .github/
    ├── specs/
    ├── requeriments/
    ├── architect/
    └── builder/
```
