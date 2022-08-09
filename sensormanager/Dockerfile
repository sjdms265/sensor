FROM openjdk:17-jdk
VOLUME /tmp
COPY target/sensormanager-1.0-SNAPSHOT.jar sensormanager.jar
CMD ["java","-jar","-Dspring.profiles.active=kube", "sensormanager.jar"]