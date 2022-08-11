KAFKA LOCAL

bin/kafka-topics.sh --create --topic temperature --bootstrap-server localhost:9092
bin/kafka-topics.sh --describe --topic temperature --bootstrap-server localhost:9092

bin/kafka-topics.sh --create --topic temperaturechange --bootstrap-server localhost:9092
bin/kafka-topics.sh --describe --topic temperaturechange --bootstrap-server localhost:9092

---

KAFKA DOCKER

https://developer.confluent.io/quickstart/kafka-docker/
docker-compose up -d
