# sensor

Spring cloud + homeassistant + sonoff integration Test project

# sdk man

https://towardsdatascience.com/install-and-run-multiple-java-versions-on-linux-using-sdkman-858571bce6cf

# docker-compose

docker-compose -f /home/barnowl/Documents/java/sensor/k8s/minikube/bootstrap/kafka/docker-compose.yml -p kafka start zookeeper broker
docker-compose -f /home/barnowl/Documents/spring-boot/k8s/minikube/bootstrap/kafka/docker-compose.yml -p kafka up -d

docker-compose -f /home/barnowl/Documents/java/sensor/k8s/minikube/bootstrap/postgres/docker-compose.yml -p postgres start postgres

docker-compose -f /home/barnowl/Documents/java/sensor/k8s/minikube/bootstrap/zipkin/docker-componse.yml -p zipkin start zipkin

# kill port

## linux
sudo lsof -i :8090
kill PID

## windows
netstat -ano | findstr :PORT
taskkill /PID PID_FROM_STEP1 /F

## mcp servers .ai\mcp\mcp.json

Before using, you'll need to:

- Replace <your-github-token> with a real GitHub PAT
- Install kafka-mcp-server (Go binary via brew or build from source)
- Install uv for the git server (pip install uv or via installer)
- Adjust the PostgreSQL DSN if your credentials differ

| Server |	What it does |	Install |
|-------|-----|------------------|
| pbytebase/dbhub	|	Query your PostgreSQL sensormanager DB, explore schemas, inspect sensor data |		npx dbhub |
| tuannvm/kafka-mcp-server | Inspect sensor-value topics, monitor consumer groups, produce test messages | brew install kafka-mcp-server or build from Go source |
| github/github-mcp-server | Manage issues/PRs, monitor Actions, check security alerts | Docker or one-click VS Code install |
| manusa/kubernetes-mcp-server | Manage minikube pods, view logs, debug your kube profile services | npx kubernetes-mcp-server@latest |
| arvindand/maven-tools-mcp | Look up latest Spring Boot/Cloud/AI versions, dependency resolution | Java-based |
| pab1it0/prometheus-mcp-server | Query Micrometer metrics (JVM stats, Kafka lag, HTTP rates) via PromQL | Docker |
| Docker MCP Gateway | Manage your Docker Compose stacks (Postgres, Kafka, Zipkin) | Built into Docker Desktop 4.59+ |
| @modelcontextprotocol/server-git | Local git operations, diff analysis, branch management | uvx mcp-server-git |

