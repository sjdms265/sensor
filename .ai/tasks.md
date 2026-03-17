# Project Tasks

## In Progress
<!-- Tasks currently being worked on -->

## Backlog

### MQTT Bridge (sensormanager)
- [ ] **#1** Configure `SpringCloudMqtt2Kafka` bindings in `application.yml` — add `sensorValueProcessor-in-0` (MQTT), `sensorValueProcessor-out-0` (Kafka), update `spring.cloud.function.definition`; without this the bean is inactive while the old Camel route continues to run
- [ ] **#2** Remove legacy Camel `Mqtt2Kafka` route — delete `Mqtt2Kafka.java`, `SensorEndpointKeyProcessor.java`, Camel/paho-mqtt5 deps from `pom.xml` and `camel.*` from `application.yml` (depends on #1 being validated)
- [ ] **#3** Implement dead-letter fallback in `SpringCloudMqtt2Kafka` — replace stub `handleFallback()` with a real DLQ Kafka topic via Spring Cloud Stream error channels or DB table for replay
- [ ] **#4** Add unit tests for `SpringCloudMqtt2Kafka` — cover valid message, unknown userId, malformed JSON, and WebSocket call using Mockito

### MCP Server (sensor-mcp-server)
- [ ] **#5** Replace `RestTemplate` with `WebClient` in `SensorService` — migrate blocking HTTP calls to non-blocking WebClient or RestClient; extract duplicated `ObjectMapper` instantiation into a shared bean

### Kubernetes (k8s)
- [ ] **#6** Add K8s deployment manifests for all services — create Deployment + Service YAMLs for `sensoreureka`, `sensorgateway`, `temperature-sensor`, `sensorai`, `sensor-mcp-server`, `sensor-gui`, and `ollama` using `kube` profile and K8s DNS names

### Frontend (sensor-gui)
- [ ] **#7** Add Angular chat UI for `sensorai` — add `/chat` route component that calls `/sensorai/stats` and `/sensorai/rain` via gateway and renders structured JSON responses

## Done
<!-- Completed tasks -->
