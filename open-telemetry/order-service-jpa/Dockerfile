FROM eclipse-temurin:17-jre

ADD target/order-service-jpa-0.0.1-SNAPSHOT.jar .
#ADD target/agent/opentelemetry-javaagent.jar .

#ENTRYPOINT java -javaagent:opentelemetry-javaagent.jar -jar order-service-jpa-0.0.1-SNAPSHOT.jar

ENTRYPOINT java -jar order-service-jpa-0.0.1-SNAPSHOT.jar
