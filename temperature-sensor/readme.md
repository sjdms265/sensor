POSTGRESS K8s

helm install sensordb bitnami/postgresql

NAME: sensordb
LAST DEPLOYED: Tue Dec  6 16:56:54 2022
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: postgresql
CHART VERSION: 12.1.3
APP VERSION: 15.1.0

** Please be patient while the chart is being deployed **

PostgreSQL can be accessed via port 5432 on the following DNS names from within your cluster:

    sensordb-postgresql.default.svc.cluster.local - Read/Write connection

To get the password for "postgres" run:

    export POSTGRES_PASSWORD=$(kubectl get secret --namespace default sensordb-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)

To connect to your database run the following command:

    kubectl run sensordb-postgresql-client --rm --tty -i --restart='Never' --namespace default --image docker.io/bitnami/postgresql:15.1.0-debian-11-r7 --env="PGPASSWORD=$POSTGRES_PASSWORD" \
      --command -- psql --host sensordb-postgresql -U postgres -d postgres -p 5432

    > NOTE: If you access the container using bash, make sure that you execute "/opt/bitnami/scripts/postgresql/entrypoint.sh /bin/bash" in order to avoid the error "psql: local user with ID 1001} does not exist"

To connect to your database from outside the cluster execute the following commands:

    kubectl port-forward --namespace default svc/sensordb-postgresql 5432:5432 &
    PGPASSWORD="$POSTGRES_PASSWORD" psql --host 127.0.0.1 -U postgres -d postgres -p 5432

---
KAFKA k8s

helm install sensorkafka bitnami/kafka

NAME: sensorkafka
LAST DEPLOYED: Tue Dec  6 17:06:25 2022
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
CHART NAME: kafka
CHART VERSION: 20.0.0
APP VERSION: 3.3.1

** Please be patient while the chart is being deployed **

Kafka can be accessed by consumers via port 9092 on the following DNS name from within your cluster:

    sensorkafka.default.svc.cluster.local

Each Kafka broker can be accessed by producers via port 9092 on the following DNS name(s) from within your cluster:

    sensorkafka-0.sensorkafka-headless.default.svc.cluster.local:9092

To create a pod that you can use as a Kafka client run the following commands:

    kubectl run sensorkafka-client --restart='Never' --image docker.io/bitnami/kafka:3.3.1-debian-11-r19 --namespace default --command -- sleep infinity
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



---

TEMPERATURE-SENSOR

https://www.section.io/engineering-education/spring-boot-kubernetes/
https://medium.com/swlh/deploy-a-spring-boot-application-into-kubernetes-661cb07c2c88
https://medium.com/swlh/how-to-run-locally-built-docker-images-in-kubernetes-b28fbc32cc1d

###SAME CONSOLE TAB###
https://minikube.sigs.k8s.io/docs/handbook/pushing/#1-pushing-directly-to-the-in-cluster-docker-daemon-docker-env

minikube start
eval $(minikube -p minikube docker-env)
minikube dashboard --url (IN A NEW CONSOLE  TAB)

- mvn clean install
- java -jar  -Dspring.profiles.active=local target//temperature-sensor-1.0-SNAPSHOT.jar
- docker build -t temperature-sensor .
- docker run -it -p 8081:8081 temperature-sensor  temperature-sensor

- kubectl delete -f deployment.yaml
- kubectl apply -f deployment.yaml

---

POSTGRES DOCKER

docker run --name=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres 


---

KAFKA DOCKER

https://developer.confluent.io/quickstart/kafka-docker/

/sensor/k8s/minikube/bootstrap/kafka
docker-compose up -d

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
--create \
--topic temperature

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
--create \
--topic temperaturechange
