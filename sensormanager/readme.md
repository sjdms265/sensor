KAFKA LOCAL

https://kafka.apache.org/quickstart

bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

bin/kafka-topics.sh --create --topic temperature --bootstrap-server localhost:9092
bin/kafka-topics.sh --describe --topic temperature --bootstrap-server localhost:9092

bin/kafka-topics.sh --create --topic temperaturechange --bootstrap-server localhost:9092
bin/kafka-topics.sh --describe --topic temperaturechange --bootstrap-server localhost:9092

---

KAFKA DOCKER

https://developer.confluent.io/quickstart/kafka-docker/
docker-compose up -d


---

SENSOR MANAGER


https://www.section.io/engineering-education/spring-boot-kubernetes/
https://medium.com/swlh/deploy-a-spring-boot-application-into-kubernetes-661cb07c2c88
https://medium.com/swlh/how-to-run-locally-built-docker-images-in-kubernetes-b28fbc32cc1d

eval $(minikube -p minikube docker-env)
minikube start & minikube dashboard --url

mvn clean install
- java -jar -Dspring.profiles.active=local target/sensormanager-1.0-SNAPSHOT.jar
- docker run -it -p 8081:8081 sensormanager  sensormanager
docker build -t sensormanager .
kubectl delete -f deployment.yaml
kubectl apply -f deployment.yaml

---

POSTGRES k8s
https://github.com/bitnami/charts/tree/master/bitnami/postgresql/#installing-the-chart
- helm repo add bitnami https://charts.bitnami.com/bitnami

helm install sensordb --set auth.postgresPassword=123456,auth.database=sensormanager bitnami/postgresql

** Please be patient while the chart is being deployed **

PostgreSQL can be accessed via port 5432 on the following DNS names from within your cluster:

    sensordb-postgresql.default.svc.cluster.local - Read/Write connection

To get the password for "postgres" run:

    export POSTGRES_PASSWORD=$(kubectl get secret --namespace default sensordb-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)

To connect to your database run the following command:

    kubectl run sensordb-postgresql-client --rm --tty -i --restart='Never' --namespace default --image docker.io/bitnami/postgresql:14.5.0-debian-11-r2 --env="PGPASSWORD=$POSTGRES_PASSWORD" \
      --command -- psql --host sensordb-postgresql -U postgres -d sensormanager -p 5432

    > NOTE: If you access the container using bash, make sure that you execute "/opt/bitnami/scripts/postgresql/entrypoint.sh /bin/bash" in order to avoid the error "psql: local user with ID 1001} does not exist"

To connect to your database from outside the cluster execute the following commands:

    kubectl port-forward --namespace default svc/sensordb-postgresql 5432:5432 &
    PGPASSWORD="$POSTGRES_PASSWORD" psql --host 127.0.0.1 -U postgres -d sensormanager -p 5432

---
KAFKA k8s

helm install sensorkafka --set provisioning.topics[0]=temperature,provisioning.topics[1]=temperaturechange bitnami/kafka

NAME: sensorkafka
LAST DEPLOYED: Wed Aug 17 15:08:37 2022
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: kafka
CHART VERSION: 18.0.8
APP VERSION: 3.2.1

** Please be patient while the chart is being deployed **

Kafka can be accessed by consumers via port 9092 on the following DNS name from within your cluster:

    sensorkafka.default.svc.cluster.local

Each Kafka broker can be accessed by producers via port 9092 on the following DNS name(s) from within your cluster:

    sensorkafka-0.sensorkafka-headless.default.svc.cluster.local:9092

To create a pod that you can use as a Kafka client run the following commands:

    kubectl run sensorkafka-client --restart='Never' --image docker.io/bitnami/kafka:3.2.1-debian-11-r4 --namespace default --command -- sleep infinity
    kubectl exec --tty -i sensorkafka-client --namespace default -- bash

    PRODUCER:
        kafka-console-producer.sh \
            
            --broker-list sensorkafka-0.sensorkafka-headless.default.svc.cluster.local:9092 \
            --topic test

    CONSUMER:
        kafka-console-consumer.sh \
            
            --bootstrap-server sensorkafka.default.svc.cluster.local:9092 \
            --topic test \
            --from-beginning

---------------- POSTGRES DOCKER

docker run --name=sensordb -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres 

--------------- TEST endpoints

###
POST http://localhost:8081/login
Content-Type: application/x-www-form-urlencoded

username=sjdms265&password=1234


###
GET http://localhost:8081/api/users
Authorization: Bearer yJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzamRtczI2NSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODEvbG9naW4iLCJleHAiOjE2NzExMzE1MjF9.fVjCfsaX_f5DfUfeRM8GxI9eltcxJg5z6dkvYBQRblE
