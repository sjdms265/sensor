FROM openjdk:17-jdk
VOLUME /tmp
COPY target/temperature-sensor-1.0-SNAPSHOT.jar temperature-sensor.jar
CMD ["java","-jar","-Dspring.profiles.active=kube", "temperature-sensor.jar"]
