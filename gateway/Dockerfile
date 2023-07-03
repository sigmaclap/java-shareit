FROM amazoncorretto:11-alpine-jdk
COPY target/shareit-gateway-0.0.1-SNAPSHOT.jar gateway.jar
ENTRYPOINT ["java","-jar","/gateway.jar"]