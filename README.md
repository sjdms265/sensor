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

sudo lsof -i :8090
kill PID

