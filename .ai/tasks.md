# Project Tasks

## In Progress
<!-- Tasks currently being worked on -->
- [ ] **#8** Implement Humidex index calculation endpoint

## Backlog

### MQTT Bridge (sensormanager)
- [ ] **#1** Configure `SpringCloudMqtt2Kafka` bindings in `application.yml` — add `sensorValueProcessor-in-0` (MQTT), `sensorValueProcessor-out-0` (Kafka), update `spring.cloud.function.definition`; without this the bean is inactive while the old Camel route continues to run
- [ ] **#2** Remove legacy Camel `Mqtt2Kafka` route — delete `Mqtt2Kafka.java`, `SensorEndpointKeyProcessor.java`, Camel/paho-mqtt5 deps from `pom.xml` and `camel.*` from `application.yml` (depends on #1 being validated)
- [ ] **#3** Implement dead-letter fallback in `SpringCloudMqtt2Kafka` — replace stub `handleFallback()` with a real DLQ Kafka topic via Spring Cloud Stream error channels or DB table for replay
- [ ] **#4** Add unit tests for `SpringCloudMqtt2Kafka` — cover valid message, unknown userId, malformed JSON, and WebSocket call using Mockito

### Kubernetes (k8s)
- [ ] **#6** Add K8s deployment manifests for all services — create Deployment + Service YAMLs for `sensoreureka`, `sensorgateway`, `temperature-sensor`, `sensorai`, `sensor-mcp-server`, `sensor-gui`, and `ollama` using `kube` profile and K8s DNS names

### Frontend (sensor-gui)
- [ ] **#7** Add Angular chat UI for `sensorai` — add `/chat` route component that calls `/sensorai/stats` and `/sensorai/rain` via gateway and renders structured JSON responses

### AI / Sensor Analysis (sensorai + sensor-mcp-server)
- [ ] **#8** Implement Humidex index calculation endpoint — add `GET /sensorai/humidex/{userId}/{sensorId}` that accepts temperature (°C) and humidity (%) parameters, computes the Humidex index, and returns a structured JSON response with the numeric value and a `HumidexLevel` enum:
  - `NO_DISCOMFORT` — index < 29
  - `SOME_DISCOMFORT` — index 30–39
  - `GREAT_DISCOMFORT` — index 40–45
  - `DANGEROUS` — index 45–54
  - `HEAT_STROKE` — index > 54
  
  **Scope:**
  - Add `HumidexLevel` enum to `sensor-common`
  - Add `HumidexResultDTO` (value, level) to `sensor-common`
  - Add MCP tool `get-humidex-by-userId-sensorId` in `sensor-mcp-server` that fetches the latest temperature + humidity readings for the user/sensor and computes the index using the formula: `H = T + 0.5555 × (6.11 × e^(5417.7530 × (1/273.16 − 1/(273.15 + Td))) − 10)` where `Td` is the dew point derived from relative humidity
  - Add `HumidexController` in `sensorai` that calls the MCP tool via `ChatClient` and returns `HumidexResultDTO`
  - Add unit tests for the Humidex calculation logic

## Done
- [x] **#5** Replace `RestTemplate` with `WebClient` in `SensorService` — migrated blocking HTTP calls to WebClient; extracted shared `ObjectMapper` bean; updated `UserProfileResource`; updated tests
