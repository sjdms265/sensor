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

###SAME CONSOLE TAB###
https://minikube.sigs.k8s.io/docs/handbook/pushing/#1-pushing-directly-to-the-in-cluster-docker-daemon-docker-env
https://www.baeldung.com/spring-cloud-kubernetes
https://www.youtube.com/watch?v=IRNdW8tvh_E

minikube start --vm-driver=virtualbox
eval $(minikube -p minikube docker-env)
minikube dashboard --url (IN A NEW CONSOLE  TAB)

mvn clean install
java -jar -Dspring.profiles.active=local target/sensormanager-1.0-SNAPSHOT.jar
docker build -t sensormanager .
docker run -it -p 8081:8081 sensormanager  sensormanager
kubectl delete -f deployment.yaml
kubectl apply -f deployment.yaml

---

POSTGRES k8s
https://github.com/bitnami/charts/tree/master/bitnami/postgresql/#installing-the-chart
- helm repo add bitnami https://charts.bitnami.com/bitnami

helm install sensormanager --set auth.postgresPassword=postgres,auth.database=sensormanager oci://registry-1.docker.io/bitnamicharts/postgresql

Pulled: registry-1.docker.io/bitnamicharts/postgresql:12.4.3
Digest: sha256:300464cbd54a77ee8ff2de3b2f71d41e2516354449f514a5a148df98b616ae09
NAME: sensormanager
LAST DEPLOYED: Wed May 10 10:49:02 2023
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: postgresql
CHART VERSION: 12.4.3
APP VERSION: 15.2.0

** Please be patient while the chart is being deployed **

PostgreSQL can be accessed via port 5432 on the following DNS names from within your cluster:

    sensormanager-postgresql.default.svc.cluster.local - Read/Write connection

To get the password for "postgres" run:

    export POSTGRES_PASSWORD=$(kubectl get secret --namespace default sensormanager-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)

To connect to your database run the following command:

    kubectl run sensormanager-postgresql-client --rm --tty -i --restart='Never' --namespace default --image docker.io/bitnami/postgresql:15.2.0-debian-11-r30 --env="PGPASSWORD=$POSTGRES_PASSWORD" \
      --command -- psql --host sensormanager-postgresql -U postgres -d sensormanager -p 5432

    > NOTE: If you access the container using bash, make sure that you execute "/opt/bitnami/scripts/postgresql/entrypoint.sh /bin/bash" in order to avoid the error "psql: local user with ID 1001} does not exist"

To connect to your database from outside the cluster execute the following commands:

    kubectl port-forward --namespace default svc/sensormanager-postgresql 5432:5432 &
    PGPASSWORD="$POSTGRES_PASSWORD" psql --host 127.0.0.1 -U postgres -d sensormanager -p 5432

WARNING: The configured password will be ignored on new installation in case when previous Posgresql release was deleted through the helm command. In that case, old PVC will have an old password, and setting it through helm won't take effect. Deleting persistent volumes (PVs) will solve the issue.


---
KAFKA k8s

#helm install broker --set provisioning.topics[0]=sensor-value,provisioning.topics[1]=sensor-value-change oci://registry-1.docker.io/bitnamicharts/kafka
helm install broker --set autoCreateTopicsEnable=true oci://registry-1.docker.io/bitnamicharts/kafka

Pulled: registry-1.docker.io/bitnamicharts/kafka:22.1.1
Digest: sha256:d19302e5809d5de83dae7fb9920164961f6299930b8682cd882d585e8c12d2fc
NAME: broker
LAST DEPLOYED: Wed May 10 11:39:28 2023
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: kafka
CHART VERSION: 22.1.1
APP VERSION: 3.4.0

** Please be patient while the chart is being deployed **

Kafka can be accessed by consumers via port 9092 on the following DNS name from within your cluster:

    broker-kafka.default.svc.cluster.local

Each Kafka broker can be accessed by producers via port 9092 on the following DNS name(s) from within your cluster:

    broker-kafka-0.broker-kafka-headless.default.svc.cluster.local:9092

To create a pod that you can use as a Kafka client run the following commands:

    kubectl run broker-kafka-client --restart='Never' --image docker.io/bitnami/kafka:3.4.0-debian-11-r28 --namespace default --command -- sleep infinity
    kubectl exec --tty -i broker-kafka-client --namespace default -- bash

    PRODUCER:
        kafka-console-producer.sh \
            --broker-list broker-kafka-0.broker-kafka-headless.default.svc.cluster.local:9092 \
            --topic test

    CONSUMER:
        kafka-console-consumer.sh \
            --bootstrap-server broker-kafka.default.svc.cluster.local:9092 \
            --topic test \
            --from-beginning

--- zipkin k8s

https://github.com/openzipkin/zipkin/tree/master/charts/zipkin

helm repo add openzipkin https://openzipkin.github.io/zipkin

helm install zipkin openzipkin/zipkin

NAME: zipkin
LAST DEPLOYED: Wed May 10 12:08:05 2023
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
1. Get the application URL by running these commands:
   export POD_NAME=$(kubectl get pods --namespace default -l "app.kubernetes.io/name=zipkin,app.kubernetes.io/instance=zipkin" -o jsonpath="{.items[0].metadata.name}")
   export CONTAINER_PORT=$(kubectl get pod --namespace default $POD_NAME -o jsonpath="{.spec.containers[0].ports[0].containerPort}")
   echo "Visit http://127.0.0.1:8080 to use your application"
   kubectl --namespace default port-forward $POD_NAME 8080:$CONTAINER_PORT


---------------- POSTGRES DOCKER

docker run --name=sensordb -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres 

----------------   REQUEST

###
POST http://localhost:8081/login
Content-Type: application/x-www-form-urlencoded

username=sjdms265&password=1234

###
GET http://localhost:8081/api/users
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzamRtczI2NSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODEvbG9naW4iLCJleHAiOjE2NzExMzAwMDV9.O3RbGwZtYe2hqHsmb2NHhbcG66MyzmJ9OkeA0oTBt7w

---

---

ZIPKIN

http://localhost:9411/zipkin
