# Product Requirements Document — Sensor Platform

**Version:** 1.0
**Date:** 2026-03-10
**Status:** Draft

---

## 1. Overview

The Sensor Platform is a multi-service IoT backend that collects, processes, persists, and analyzes sensor readings (temperature, humidity, etc.) from physical devices. It exposes a real-time dashboard via WebSocket, a REST/GraphQL API for historical data, and an AI layer that answers natural-language questions about sensor data.

---

## 2. Goals

- Reliably ingest sensor data from MQTT-connected devices into a durable Kafka pipeline.
- Process and persist only meaningful value changes to minimize storage growth.
- Provide a real-time WebSocket feed for live dashboard updates.
- Expose a REST + GraphQL API for querying historical sensor readings and user/device management.
- Enable AI-driven insights (sensor stats, rain probability) via a natural-language chat interface.
- Support local development with Docker Compose and production deployment on Kubernetes (Minikube).

---

## 3. Non-Goals

- The platform does not control physical devices (write-back to actuators).
- The platform does not process video or binary sensor streams — only numeric float values.
- Multi-tenant data isolation beyond per-user JWT scoping is out of scope for v1.

---

## 4. Users & Personas

| Persona | Description |
|---|---|
| **Device owner** | Registers sensors, views live readings and historical charts via the Angular GUI. |
| **Admin** | Manages users and roles via the admin API. |
| **AI user** | Asks natural-language questions about their sensor history (e.g. "What was the average temperature last week?"). |
| **Developer** | Integrates with the REST/GraphQL API or extends the MCP toolset. |

---

## 5. Architecture Overview

```
Physical Sensor (ESP8266/Home Assistant)
    │  MQTT publish
    ▼
Mosquitto MQTT Broker (tcp://192.168.50.11:1883)
    │  Spring Cloud Stream (sensormanager)
    ▼
Kafka topic: sensor-value
    ├── NewSensorValueProcessor  (Kafka Streams, temperature-sensor)
    │       sliding-window reduce → sensor-value-change topic
    └── SensorValueChangeProcessor (Kafka Streams, temperature-sensor)
            persists changed values → PostgreSQL

POST /api/echoSensorEndpoint  (manual injection, sensormanager)
    ├── StreamBridge → sensor-value topic  (same downstream path)
    └── SensorValueWebSocketHandler → WebSocket clients

sensor-gui (Angular 17, port 4200)
    └── calls gateway (port 8080) → sensormanager / temperature-sensor / sensorai

sensorai
    └── Spring AI + ChatClient
          └── sensor-mcp-server (MCP tools: sensor stats, rain probability)
```

### Services

| Service | Port | Role |
|---|---|---|
| sensoreureka | 8761 | Eureka service registry |
| sensorgateway | 8080 | Spring Cloud Gateway — single entry point |
| sensormanager | 8081 | Auth, user management, MQTT→Kafka bridge, WebSocket |
| temperature-sensor | 8082 | Kafka Streams processing, data persistence, GraphQL/REST query API |
| sensorai | — | AI chat endpoint (Spring AI + Ollama / Anthropic) |
| sensor-mcp-server | — | MCP server exposing sensor tools to AI clients |
| sensor-gui | 4200 | Angular 17 frontend |

---

## 6. Functional Requirements

### 6.1 Authentication & User Management (sensormanager)

| ID | Requirement |
|---|---|
| AUTH-1 | Users authenticate via JWT. Tokens are issued on login and refreshed via `/api/auth/refreshToken`. |
| AUTH-2 | JWT secret is configurable via environment variable `JWT_SECRET` (default: `secret`). Access token TTL: 30 min; refresh token TTL: ~16 h. |
| AUTH-3 | Admins can list, create, and delete users via `GET/POST/DELETE /api/admin/users`. |
| AUTH-4 | Admins can create roles and assign them to users via `/api/roles` and `/api/roles/addtouser`. |
| AUTH-5 | All downstream sensor endpoints require a valid JWT in the `Authorization` header. |

### 6.2 MQTT → Kafka Ingestion (sensormanager)

| ID | Requirement |
|---|---|
| MQTT-1 | The platform subscribes to the Mosquitto broker and consumes messages from the `sensorValue` MQTT topic. |
| MQTT-2 | Each MQTT message payload is a JSON-serialized `SensorEndpointDTO` containing `userId`, `sensorId`, `value` (float), and `date`. |
| MQTT-3 | Incoming messages are validated: the `userId` must correspond to a known `SensorUser`. Invalid messages are discarded and logged. |
| MQTT-4 | Valid messages are forwarded to the Kafka topic `sensor-value` with a composite key `{userId}-{sensorId}`. |
| MQTT-5 | Valid messages are simultaneously broadcast to all connected WebSocket clients. |
| MQTT-6 | Failed message processing must route to a dead-letter mechanism (DLQ topic or DB table) rather than silently dropping data. |
| MQTT-7 | The MQTT bridge must tolerate broker disconnects with retry logic and a fallback route. |

### 6.3 Manual Sensor Data Injection (sensormanager)

| ID | Requirement |
|---|---|
| ECHO-1 | `POST /api/echoSensorEndpoint` accepts a `SensorEndpointDTO` body and injects it directly into the `sensor-value` Kafka topic (same downstream path as MQTT). |
| ECHO-2 | The same endpoint simultaneously pushes the reading to WebSocket clients. |

### 6.4 Stream Processing (temperature-sensor)

| ID | Requirement |
|---|---|
| STREAM-1 | `NewSensorValueProcessor` consumes `sensor-value`, applies a configurable sliding time window (default 60 s), and reduces readings per key — emitting only records where the value has changed. Output goes to `sensor-value-change`. |
| STREAM-2 | `SensorValueChangeProcessor` consumes `sensor-value-change` and persists changed readings to PostgreSQL. |
| STREAM-3 | Kafka Serdes must not rely on type-id headers so that messages from both Camel/MQTT and StreamBridge are processed uniformly (`JsonDeserializer` with header type detection disabled). |

### 6.5 Sensor Specification Management (temperature-sensor)

| ID | Requirement |
|---|---|
| SPEC-1 | Each physical sensor has a `SensorSpec` record that includes a unique sensor ID, human-readable name, and `SensorType` (e.g. `TEMPERATURE`, `HUMIDITY`). |
| SPEC-2 | `SensorSpec` records are created/updated via REST and GraphQL APIs exposed by temperature-sensor. |

### 6.6 Historical Query API (temperature-sensor)

| ID | Requirement |
|---|---|
| QUERY-1 | REST endpoints allow querying `SensorEndpoint` records by userId, sensorId, and page size. |
| QUERY-2 | GraphQL queries expose the same data with configurable depth limits (max depth: 5). |
| QUERY-3 | GraphiQL IDE is enabled at `/graphiql` in local profile for development exploration. |
| QUERY-4 | Data returned excludes statistical outliers beyond 2× the average to reduce noise in charts. |

### 6.7 Real-Time WebSocket Feed (sensormanager)

| ID | Requirement |
|---|---|
| WS-1 | `SensorValueWebSocketHandler` pushes `SensorEndpointDTO` JSON to all active WebSocket sessions whenever a new reading arrives (via MQTT or REST injection). |
| WS-2 | WebSocket failures must not block or abort the Kafka forwarding path. |

### 6.8 AI Sensor Analysis (sensorai + sensor-mcp-server)

| ID | Requirement |
|---|---|
| AI-1 | `GET /sensorai/stats/{userId}/{sensorId}` returns AI-computed statistics (average, min, max) for the given sensor, formatted as structured JSON (`SensorStatsResults`). |
| AI-2 | `GET /sensorai/rain/{userId}` returns an AI-computed rain probability based on the most recent temperature and humidity readings for the user. |
| AI-3 | The AI layer uses Spring AI `ChatClient` backed by either Ollama (local) or Anthropic (cloud), configurable per profile. |
| AI-4 | `sensor-mcp-server` exposes MCP tools (`get-sensor-info-by-userId-and-pattern`, `get-stats-by-userId-sensorId`) that the AI client calls to retrieve real sensor data. |
| AI-5 | AI prompts pass the caller's JWT token through to MCP tools so that downstream sensor API calls are authenticated. |
| AI-6 | AI responses are structured JSON (via `BeanOutputConverter`) to allow reliable parsing by the frontend. |

### 6.9 Angular Frontend (sensor-gui)

| ID | Requirement |
|---|---|
| GUI-1 | The Angular app authenticates users via the gateway and stores JWT tokens for subsequent API calls. |
| GUI-2 | A live dashboard displays real-time sensor readings received over WebSocket. |
| GUI-3 | Historical charts render `GraphSensorEndpoint` time-series data fetched via GraphQL. |
| GUI-4 | An AI chat view allows users to ask natural-language questions about their sensor data, calling `sensorai` via the gateway. |

---

## 7. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-1 | **Observability:** All services export distributed traces to Zipkin (100% sampling). Micrometer + OpenTelemetry are used for metrics and tracing. |
| NFR-2 | **Service discovery:** All backend services register with Eureka. The gateway uses load-balanced discovery routing — no hard-coded URLs between services. |
| NFR-3 | **Profiles:** `local` profile uses `localhost` URLs; `kube` profile rewrites datasource and broker URLs to Kubernetes DNS names. K8s Dockerfiles set `-Dspring.profiles.active=kube`. |
| NFR-4 | **Security:** JWT secret must be injected via `JWT_SECRET` environment variable in production. Default value `secret` is for local development only. |
| NFR-5 | **Resilience:** MQTT bridge must retry on socket timeouts (up to 2 retries, 1.5 s delay) and route unrecoverable failures to a dead-letter destination. |
| NFR-6 | **Data consistency:** Both services (sensormanager and temperature-sensor) share the same PostgreSQL database (`sensormanager`, port 5432). Schema migrations are managed via JPA `ddl-auto: update`. |
| NFR-7 | **Java version:** Java 21. Spring Boot 3.5.x, Spring Cloud 2025.0.x. |

---

## 8. Data Model

### SensorEndpointDTO (shared DTO)
| Field | Type | Description |
|---|---|---|
| `userId` | String | Owning user identifier (matches `SensorUser.username`) |
| `sensorId` | String | Sensor identifier (e.g. `sensor.10000db11e_t`) |
| `value` | Float | Numeric sensor reading |
| `date` | Date | Raw timestamp from device (UTC, `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`) |
| `parsedDateTime` | OffsetDateTime | Normalized UTC timestamp |

### SensorSpec
| Field | Type | Description |
|---|---|---|
| `id` | String | Unique sensor ID |
| `name` | String | Human-readable label |
| `sensorCategory` | SensorType | Enum: `TEMPERATURE`, `HUMIDITY`, … |

### SensorUser
| Field | Type | Description |
|---|---|---|
| `username` | String | Login name (also used as `userId` in sensor data) |
| `password` | String | BCrypt-hashed |
| `roles` | List\<Role\> | Granted authorities (e.g. `ROLE_USER`, `ROLE_ADMIN`) |

---

## 9. Kafka Topics

| Topic | Producer | Consumer | Description |
|---|---|---|---|
| `sensor-value` | sensormanager (MQTT bridge, StreamBridge) | temperature-sensor (Kafka Streams) | Raw sensor readings |
| `sensor-value-change` | temperature-sensor (NewSensorValueProcessor) | temperature-sensor (SensorValueChangeProcessor) | De-duplicated, windowed readings |
| `sensor-echo-topic` | sensormanager (sensorEcho function) | — | Echo/test topic |

---

## 10. Infrastructure

### Local Development (Docker Compose)
Start in order:
1. PostgreSQL (`k8s/minikube/bootstrap/postgres/docker-compose.yml`)
2. Kafka + Zookeeper (`k8s/minikube/bootstrap/kafka/docker-compose.yml`)
3. Zipkin (`k8s/minikube/bootstrap/zipkin/docker-compose.yml`)

Then start services: `sensoreureka` → `sensorgateway` → `sensormanager` + `temperature-sensor` (→ optionally `sensorai`, `sensor-mcp-server`).

### Kubernetes (Minikube)
- Deployment YAMLs live under `k8s/minikube/services/`.
- Each service uses the `kube` Spring profile for DNS-based service discovery.
- Ollama runs as a Kubernetes Deployment with a persistent volume for model storage.

---

## 11. Open Items / Known Gaps

| # | Area | Description |
|---|---|---|
| 1 | MQTT bridge | `SpringCloudMqtt2Kafka` bindings not yet configured in `application.yml`; both Camel and Spring Cloud Stream bridges are active simultaneously. |
| 2 | MQTT bridge | `handleFallback()` is a stub — no DLQ or persistence implemented yet. |
| 3 | K8s | Deployment manifests missing for: `sensoreureka`, `sensorgateway`, `temperature-sensor`, `sensorai`, `sensor-mcp-server`, `sensor-gui`. |
| 4 | K8s | No Ollama Deployment/Service manifest despite bootstrap artifacts being present. |
| 5 | Testing | No tests for `SpringCloudMqtt2Kafka`. |
| 6 | Frontend | No Angular UI for the `sensorai` chat endpoints. |

---

## 12. Glossary

| Term | Definition |
|---|---|
| **SensorEndpointDTO** | Shared DTO representing a single timestamped sensor reading |
| **SensorSpec** | Metadata record for a physical sensor (type, name) |
| **MCP** | Model Context Protocol — standard for exposing tools to AI models |
| **Kafka Streams** | Stream processing library used for windowed aggregation of sensor readings |
| **StreamBridge** | Spring Cloud Stream utility for imperative (non-binding) Kafka sends |
| **MQTT** | Lightweight pub/sub protocol used by IoT devices |