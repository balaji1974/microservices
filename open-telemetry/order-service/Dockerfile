FROM eclipse-temurin:17-jre

ADD target/order-service-0.0.1-SNAPSHOT.jar order-service-0.0.1-SNAPSHOT.jar
ADD target/agent/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

ENTRYPOINT java -javaagent:/opentelemetry-javaagent.jar -jar /order-service-0.0.1-SNAPSHOT.jar