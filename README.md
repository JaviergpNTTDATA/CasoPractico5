# NovaBank Digital Services — Reactive Microservices Platform (CP5)

Sistema bancario distribuido implementado como **plataforma de microservicios** sobre **Spring Boot 3 + Spring Cloud**, evolucionado desde un stack clásico (MVC/JPA) a un stack **reactivo end‑to‑end (WebFlux + R2DBC)** para mejorar escalabilidad en operaciones I/O, permitir **streaming en tiempo real (SSE)** y modelar correctamente resiliencia/fail-fast en flujos transaccionales.

> **Dominio**: clientes, cuentas, movimientos, operaciones (depósitos, retiradas, transferencias) y autenticación con JWT, expuesto vía API Gateway.  
> **Arquitectura**: Config Server + Eureka + Gateway + microservicios de negocio + servicios auxiliares (mock de tipo de cambio).

---

## 1. Módulos del repositorio (multi‑módulo Maven)

- **config-server**: Spring Cloud Config Server (config centralizada)
- **config-repo/**: repositorio de YAML consumido por Config Server
- **eureka-server**: Service Discovery (Netflix Eureka)
- **api-gateway**: Spring Cloud Gateway + filtro de autenticación JWT
- **auth-server**: autenticación (login) y emisión de JWT (**WebFlux + R2DBC**)
- **client-service**: gestión de clientes (**WebFlux + R2DBC**)
- **account-service**: cuentas y movimientos (**WebFlux + R2DBC**) + **SSE** para stream de movimientos
- **operation-service**: operaciones financieras (**WebFlux + R2DBC**) + integración con servicios externos + resiliencia reactiva
- **exchange-rate-mock-service**: servicio mock de tipo de cambio (para pruebas y demos)

---

## 2. Visión de arquitectura (esquema)

### 2.1 Topología general

```text
                 +----------------------+
                 |     Config Server    |
                 |  (config-server)     |
                 +----------+-----------+
                            |
                            v
+------------------+   +----+-------------------+
|   Eureka Server  |<--+  Microservices (Eureka)|
|  (eureka-server) |   |  client/account/oper.. |
+------------------+   +------------------------+

Clients (Postman / Frontend / etc.)
           |
           v
+------------------+         +------------------+
|   API Gateway    |-------->|    auth-server   |
| (api-gateway)    |         |  (JWT login)     |
+--------+---------+         +------------------+
         |
         +------------------------------+
         |                              |
         v                              v
 +---------------+              +---------------+
 | client-service|              |account-service|
 +---------------+              +-------+-------+
                                         |
                                         |  SSE: /accounts/{iban}/movements/stream
                                         v
                                   (stream events)

operation-service --(HTTP/WebClient)--> account-service
operation-service --(HTTP/WebClient)--> exchange-rate-mock-service
```

### 2.2 Stack tecnológico (reactivo)

- Capa web: **Spring WebFlux** (`Mono`/`Flux`)
- Persistencia: **Spring Data R2DBC** + **r2dbc-postgresql**
- Cliente HTTP: **WebClient** (con load balancing cuando aplica)
- Docs: **springdoc-openapi** para WebFlux
- Tests: Reactor Test / WebTestClient / MockWebServer / @DataR2dbcTest

---

## 3. Principios de diseño aplicados

### 3.1 Reactivo end‑to‑end
Se evita mezclar WebFlux con accesos bloqueantes (JPA/JDBC). La plataforma está pensada para:
- mucha concurrencia,
- latencias de red entre microservicios,
- operaciones I/O dominantes.

### 3.2 Resiliencia y fallbacks según criticidad del dato
NovaBank diferencia explícitamente entre:
- **Datos transaccionales** (dinero/saldos/movimientos): ante fallo de un dato crítico (p.ej. tipo de cambio) se aplica **fail-fast** y se aborta la operación.
- **Datos de visualización**: se permite degradación controlada (DTO parcial / “no disponible”) si no afecta integridad.

### 3.3 Streaming en tiempo real (SSE)
El sistema publica movimientos en tiempo real para una cuenta mediante **Server‑Sent Events**.

Implementación concreta:
- **Endpoint SSE**:
  - `account-service/src/main/java/com/novabank/account/controller/AccountController.java`
  - `GET /accounts/{iban}/movements/stream` con `produces = MediaType.TEXT_EVENT_STREAM_VALUE`
- **Bus de eventos**:
  - `account-service/src/main/java/com/novabank/account/service/MovementEventService.java`
  - `Sinks.Many<MovementDTO>` multicast con backpressure buffer y filtro por IBAN

---

## 4. Seguridad / Autenticación

### 4.1 Flujo JWT
1. El consumidor llama a `POST /auth/login` (vía gateway o directo al auth-server según despliegue).
2. Si las credenciales son válidas, `auth-server` devuelve un **JWT**.
3. Las llamadas al resto de endpoints se realizan con:
   ```text
   Authorization: Bearer <TOKEN>
   ```

### 4.2 Gateway como punto de control
El **API Gateway** aplica el filtro de autenticación/validación de token antes de enrutar.

> Nota: la ruta final dependerá de la configuración del gateway y de los puertos definidos en `config-repo/*.yml` / `application.yml`.

---

## 5. Documentación API (Swagger / OpenAPI)

En servicios WebFlux, Swagger UI suele estar disponible en:

- `http://localhost:<puerto>/swagger-ui.html`
- `http://localhost:<puerto>/swagger-ui/index.html`

---

## 6. Base de datos y scripts de inicialización

Cada microservicio gestiona su propia base de datos (PostgreSQL). En el stack reactivo:
- La conexión se configura con `spring.r2dbc.*`
- La inicialización de SQL se habilita con:
  ```yaml
  spring:
    sql:
      init:
        mode: always
  ```

Cuando existe, `schema.sql` se ejecuta automáticamente sobre el **ConnectionFactory R2DBC**.

---

## 7. Resiliencia reactiva (Circuit Breaker + fallbacks)

En WebFlux los fallos se gestionan dentro del flujo (no con `try/catch` imperativo). Se aplican:
- Circuit breaker y timeouts en llamadas HTTP (WebClient)
- Operadores como `onErrorResume`, `switchIfEmpty`, `timeout`, etc.

Punto clave (caso de uso de transferencias):
- **Si el tipo de cambio falla o no está disponible**, la operación **se aborta** (error controlado) y **no se continúa** con acciones transaccionales (no modificación de saldos).

---

## 8. Tests: tipos, herramientas y qué cubren

### 8.1 Unit tests / service tests (Reactor)
- **Herramienta**: `reactor-test` + **StepVerifier**
- **Objetivo**: validar `Mono/Flux` (emisiones, completado, error) sin bloquear.

### 8.2 Web layer tests (WebFlux)
- **Herramienta**: **WebTestClient**
- **Objetivo**: probar endpoints REST reactivos verificando status code y JSON.

### 8.3 Persistencia (R2DBC)
- **Herramienta**: **@DataR2dbcTest**
- **Objetivo**: validar repositorios R2DBC y queries reactivos con contexto reducido.

### 8.4 Integraciones HTTP simuladas
- **Herramienta**: **MockWebServer**
- **Objetivo**: simular dependencias (tipo de cambio, etc.) controlando respuestas y fallos.

### 8.5 Test obligatorio: fallo controlado del tipo de cambio
Se incluye explícitamente una prueba donde el servicio de tipo de cambio falla (p.ej. 500/timeout) y se valida:
- que la operación devuelve un error controlado,
- y que no se ejecutan efectos secundarios transaccionales posteriores.

---

## 9. Cómo compilar

Desde la raíz:

```bash
mvn clean compile
```

---

## 10. Cómo ejecutar en local (orden recomendado)

> Arrancar cada servicio en una terminal distinta.

1) Config Server  
```bash
mvn -pl config-server spring-boot:run
```

2) Eureka  
```bash
mvn -pl eureka-server spring-boot:run
```

3) Auth + Gateway + Servicios negocio  
```bash
mvn -pl auth-server spring-boot:run
mvn -pl api-gateway spring-boot:run
mvn -pl client-service spring-boot:run
mvn -pl account-service spring-boot:run
mvn -pl operation-service spring-boot:run
mvn -pl exchange-rate-mock-service spring-boot:run
```

> Nota importante (multi‑módulo): si ejecutas `spring-boot:run` desde la raíz con `-pl`, asegúrate de que la configuración de Maven no intente ejecutar el goal sobre el agregador. La forma más simple es ejecutar el comando desde el directorio del módulo (`cd auth-server && mvn spring-boot:run`), o mantener el `-pl` únicamente sobre el módulo.

---

## 11. Cómo ejecutar tests

Todos:
```bash
mvn test
```

Por módulo:
```bash
mvn -pl operation-service test
mvn -pl account-service test
mvn -pl client-service test
```

---

## 12. Tecnologías

- **Java 17**
- **Spring Boot 3.x**
- **Spring Cloud**: Config Server, Eureka, Gateway
- **Spring WebFlux**
- **Spring Data R2DBC**
- **PostgreSQL** + **r2dbc-postgresql**
- **Spring Security** + JWT (jjwt)
- **springdoc-openapi** (WebFlux)
- **JUnit 5**
- **Reactor Test (StepVerifier)**
- **WebTestClient**
- **MockWebServer**
- **Lombok**
- **Maven**

---

## 13. Repositorio

https://github.com/JaviergpNTTDATA/CasoPractico5
