# Migration Plan: Spring Boot 4.1.0 + Spring AI 2.0.0

> **IMPORTANT:** Never execute tests (`mvn test`) during any phase of this migration. Only run `mvn clean compile` to verify compilation. The user will run tests manually.

## Context

The project currently runs Spring Boot 3.5.16, Spring Cloud 2025.0.0, and Spring AI 1.1.2. The `sensorai` module is an outlier using `spring-boot-starter-parent:3.5.10` as a direct parent. This migration upgrades to Spring Boot 4.1.0 (Spring Framework 7.0), Spring Cloud 2025.1.2 (the Boot 4.x-compatible train), and Spring AI 2.0.0. Spring AI 2.0 includes significant MCP transport and annotation namespace changes that affect both the `sensorai` client and `sensor-mcp-server`.

---

## Phase 1: POM Version Bumps

**Root `pom.xml`:**
- `spring.boot.dependencies.version`: `3.5.16` → `4.1.0`
- `spring.boot.maven.plugin.version`: `3.5.16` → `4.1.0`
- `spring-cloud.version`: `2025.0.0` → `2025.1.2`
- `spring-ai.version`: `1.1.2` → `2.0.0`
- `camel.version`: `4.21.0` → verify compatible version with Boot 4.x (likely 4.25+)
- Remove `org.apache.httpcomponents:httpclient:4.5.14` (HC4 is EOL; Boot 4.x uses HC5 natively)
- Review/remove CVE pinned versions that Boot 4.1.0 may already resolve (xstream, snakeyaml, snappy-java, okio-jvm, commons-compress, lz4-java)

**`sensorai/pom.xml`:**
- Change `<parent>` from `spring-boot-starter-parent:3.5.10` → `spring-boot-starter-parent:4.1.0`
- `spring-ai.version`: `1.1.2` → `2.0.0`
- Remove hardcoded `spring-cloud-starter-netflix-eureka-client:4.3.0` — let BOM manage it via the root `spring-cloud-dependencies:2025.1.2`
- Add Spring Cloud BOM import to `<dependencyManagement>` (since this module doesn't inherit root)

---

## Phase 2: Spring Boot 4.0 Breaking Changes

**Property renames** in `application.yml`/`application.yaml`:
- `management.tracing.enabled` → `management.tracing.export.enabled` (if present)
- `spring.dao.exceptiontranslation.enabled` → `spring.persistence.exceptiontranslation.enabled` (not used — no action)
- Verify `management.tracing.sampling.probability` remains valid under Boot 4.x

**Cleanup dead config:**
- Remove `spring.cloud.loadbalancer.ribbon.enabled: false` from `sensormanager/application.yml` and `temperature-sensor/application.yml`

---

## Phase 3: Spring AI 2.0.0 — MCP Client Migration (sensorai)

**File: `sensorai/.../config/SensorAiConfig.java`**

The MCP client transport moves from `io.modelcontextprotocol` to `org.springframework.ai.mcp` and SSE is replaced by StreamableHttp:

| Before | After |
|--------|-------|
| `io.modelcontextprotocol.client.McpClient` | `org.springframework.ai.mcp.McpClient` |
| `io.modelcontextprotocol.client.McpSyncClient` | `org.springframework.ai.mcp.McpSyncClient` |
| `io.modelcontextprotocol.client.transport.HttpClientSseClientTransport` | Replace with StreamableHttp transport |
| `io.modelcontextprotocol.spec.McpSchema` | `org.springframework.ai.mcp.McpSchema` |
| `org.springframework.ai.mcp.SyncMcpToolCallbackProvider` | Verify new package path |

The `McpSyncClient` bean configuration changes from SSE to StreamableHttp. The `SyncMcpToolCallbackProvider` wrapping the client for `defaultToolCallbacks(...)` should be verified — Spring AI 2.0 auto-registers `ToolCallingAdvisor`, so confirm this doesn't conflict with manual MCP tool registration.

**Key change:** The client should connect via the new StreamableHttp endpoint instead of SSE (`/mcp/sensor/sse`). Update `mcp.sensor-mcp-server.base-url` and transport initialization to use the new endpoint path.

---

## Phase 4: Spring AI 2.0.0 — MCP Server Migration (sensor-mcp-server)

**Annotation namespace migration:**

| File | Before | After |
|------|--------|-------|
| `SensorPrompts.java` | `org.springaicommunity.mcp.annotation.McpPrompt` | `org.springframework.ai.mcp.annotation.McpPrompt` |
| `SensorPrompts.java` | `org.springaicommunity.mcp.annotation.McpArg` | `org.springframework.ai.mcp.annotation.McpArg` |
| `UserProfileResource.java` | `org.springaicommunity.mcp.annotation.McpResource` | `org.springframework.ai.mcp.annotation.McpResource` |
| `SensorPrompts.java` | `io.modelcontextprotocol.spec.McpSchema` | `org.springframework.ai.mcp.McpSchema` |
| `UserProfileResource.java` | `io.modelcontextprotocol.spec.McpSchema` | `org.springframework.ai.mcp.McpSchema` |

**MCP Server properties (`application.properties`):**

Replace SSE transport config with StreamableHttp:
```properties
# Remove:
spring.ai.mcp.server.sse-message-endpoint=/mcp/sensor/messages
spring.ai.mcp.server.sse-endpoint=/mcp/sensor/sse

# Add StreamableHttp endpoint:
spring.ai.mcp.server.streamable-http.endpoint=/mcp/sensor
```

**`McpConfig.java`** — `MethodToolCallbackProvider` API should remain stable but now throws `IllegalArgumentException` on errors instead of `IllegalStateException`.

---

## Phase 5: Spring AI 2.0.0 — ChatClient & Converter Changes (sensorai)

**Files: `ChatController.java`, `HumidexController.java`**

- `ChatClient.builder(model).defaultSystem(...)` — verify builder API still accepts same fluent pattern
- `.defaultToolCallbacks(new SyncMcpToolCallbackProvider(...))` — verify not conflicting with auto-registered `ToolCallingAdvisor`
- `BeanOutputConverter<T>` — API largely stable; `getFormat()` still returns JSON schema string
- `.user(userSpec -> userSpec.text(...).param(...))` — verify `.param()` API unchanged

These controllers should work with minimal changes — mostly validating compilation after the dependency bump.

---

## Phase 6: Apache Camel Compatibility Check

The `sensormanager` module uses `camel-spring-boot-starter` and `camel-paho-mqtt5` at version `4.21.0`. Camel 4.x officially supports Spring Boot 4.x starting from a certain version. Verify the latest compatible Camel 4.x release for Spring Boot 4.1.0 and bump `<camel.version>` accordingly.

---

## Execution Order

1. Bump all POM versions (Phase 1)
2. Apply property changes and cleanup (Phase 2)
3. Migrate MCP server annotations and transport (Phase 4)
4. Migrate MCP client transport (Phase 3)
5. Verify ChatClient/Converter API (Phase 5)
6. Verify Camel compatibility (Phase 6)
7. Compile and run tests

---

## Verification

1. `mvn clean compile` — all modules must compile without errors
2. `mvn clean test` — all tests pass
3. Start services in order (sensoreureka → sensorgateway → sensormanager + temperature-sensor + sensor-mcp-server + sensorai) and confirm:
   - Eureka dashboard at :8761 shows all services registered
   - MCP server responds on the new StreamableHttp endpoint
   - `GET /sensorai/stats/hello` returns a valid AI response via the MCP tool chain
