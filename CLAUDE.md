# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Memory

Auto memory is always enabled for this project. At the start of every conversation:
- Read `/home/barnowl/.claude/projects/-home-barnowl-Documents-java-sensor/memory/MEMORY.md` to load saved context.
- Save new memories throughout the conversation (user preferences, feedback, project decisions).
- Project tasks are tracked in `.ai/tasks.md`. This file is the single source of truth for all project tasks — always read it for current work status and update it when tasks are created, started, or completed.

## Build & Run

**Build all modules:**
```bash
mvn clean install
```

**Build a single module (with its dependencies):**
```bash
mvn clean install -pl sensormanager -am
```

**Run a service:**
```bash
mvn -pl sensormanager spring-boot:run
mvn -pl temperature-sensor spring-boot:run
```

**Run tests:**
```bash
mvn clean test                                      # all modules
mvn test -pl sensormanager                          # single module
mvn test -Dtest=UserServiceImplTest                 # single class
mvn test -Dtest=UserServiceImplTest#methodName      # single method
```

**Angular GUI** (Node 20.13.1 required):
```bash
cd sensor-gui && npm install && ng serve --open
```

## Infrastructure (local dev)

Start these Docker Compose stacks before running services:
```bash
docker-compose -f k8s/minikube/bootstrap/postgres/docker-compose.yml -p postgres up -d
docker-compose -f k8s/minikube/bootstrap/kafka/docker-compose.yml -p kafka up -d
docker-compose -f k8s/minikube/bootstrap/zipkin/docker-compose.yml -p zipkin up -d
```

Start services in order: sensoreureka → sensorgateway → sensormanager + temperature-sensor (+ optionally sensorai, sensor-mcp-server).

## Architecture

Multi-module Maven project (Java 21, Spring Boot 3.5.10, Spring Cloud 2025.0.0).

| Service | Port | Role |
|---|---|---|
| sensoreureka | 8761 | Eureka service registry |
| sensorgateway | 8080 | Spring Cloud Gateway (routes to downstream services) |
| sensormanager | 8081 | Auth/user management, MQTT→Kafka bridge, WebSocket |
| temperature-sensor | 8082 | Kafka Streams processor, sensor data persistence |
| sensorai | — | AI queries via Ollama / Anthropic (Spring AI) |
| sensor-mcp-server | — | MCP server exposing sensor tools to AI clients |
| sensor-gui | 4200 | Angular 17 frontend |

All backend services register with Eureka. The gateway uses load-balanced discovery routing.

**Shared library:** `sensor-common` holds DTOs (`SensorEndpointDTO`, `SensorSpecDTO`, `SensorUserDTO`, `GraphSensorEndpoint`) and the `SensorType` enum. It is a dependency of every other service.

## Data Flow

```
MQTT broker (Mosquitto)
    └─ Apache Camel (paho-mqtt5) in sensormanager
          └─ Kafka topic: sensor-value
                ├─ NewSensorValueProcessor (Kafka Streams, temperature-sensor)
                │     └─ sliding-window reduce → sensor-value-change topic
                └─ SensorValueChangeProcessor (Kafka Streams, temperature-sensor)
                      └─ persists changed values to PostgreSQL

POST /api/echoSensorEndpoint (BaseController, sensormanager)
    └─ StreamBridge → sensor-value topic  (same downstream path)
    └─ SensorValueWebSocketHandler → WebSocket clients
```

[//]: # (Kafka Streams Serdes in `NewSensorValueProcessor` and `SensorValueChangeProcessor` are constructed as `new JsonDeserializer<>&#40;SensorEndpointDTO.class, false&#41;` — the `false` disables header-based type detection so both Camel messages &#40;no headers&#41; and StreamBridge messages work uniformly.)

StreamBridge bindings that send `SensorEndpointDTO` directly must have `useNativeEncoding: true` in `application.yml` so Spring Cloud Stream skips its own byte[] conversion and lets `JsonSerializer` stamp the correct `__TypeId__` header.

## Key Config Patterns

- **Profiles:** `local` and `kube`. Kubernetes Dockerfiles activate `kube` profile (`-Dspring.profiles.active=kube`), which rewrites datasource URLs and Kafka brokers to K8s DNS names.
- **JWT secret:** `${JWT_SECRET:secret}` — override in production.
- **MQTT broker:** `camel.component.paho-mqtt5.broker-url` defaults to `tcp://192.168.50.11:1883`.
- **Both sensormanager and temperature-sensor share the same PostgreSQL database** (`sensormanager` on port 5432).

## GraphQL

Both sensormanager and temperature-sensor expose GraphQL endpoints. GraphiQL UI is enabled at `/graphiql` in local profiles. Schema files live in `src/main/resources/graphql/`.

## Observability

All services use Micrometer + OpenTelemetry tracing with Zipkin export (`management.tracing.sampling.probability: 1.0`). Zipkin runs via Docker Compose at the default port.

## Coding Conventions

### Java

- **Prefer `record` over class for DTOs** — use Java records for all data-transfer objects and value types (e.g. `SensorStatsResults`, `Rain`, `HumidexResultDTO`). Only use a `@Data`/`@Builder` class when mutability or JPA mapping is required.
- **Prefer `record` over class for simple result types** — MCP tool result types (e.g. `TemperatureResults`) should also be records.
- **Lombok on entities/mutable classes only** — use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` only where a plain record cannot be used (JPA entities, Spring-managed beans that need mutation).
- **Utility classes are `final` with a private constructor** — stateless helpers (e.g. `HumidexCalculator`) must not be instantiable.
- **Static helper methods shared across controllers** — make them `public static` so sibling classes in the same package can reuse them without duplication (see `ChatController.escapeStBraces`).
- **Use `@Slf4j` + `log.info/warn/error`** — no `System.out.println` anywhere.
- **Enums carry behaviour** — add factory/lookup methods directly on the enum (e.g. `HumidexLevel.fromIndex(double)`) rather than scattering switch statements across callers.

### Testing

- **Use AssertJ** (`assertThat`) for all assertions — no JUnit `assertEquals`.
- **Use `@ExtendWith(MockitoExtension.class)` + `@InjectMocks`/`@Mock`** for unit tests; avoid Spring context loading unless integration testing.
- **Parameterized tests with `@CsvSource`** for boundary/range checks.
- **Test method naming:** `methodName_shouldDoX_whenConditionY`.