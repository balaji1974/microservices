# Open Telemetry   

## Service to Monitor 
```xml
Instrumentation - Metrics, Logs, Traces 

Example: A metric shows high latency (something's wrong), 
traces show the slow service (where), 
and logs provide the specific error message or user ID (why). 

``` 

## Create an order service 
```xml
1. Spring Initilizer
Go to spring initilizer page https://start.spring.io/ 
and add the following dependencies: 
Spring Web
DevTools


2. Create a project 'order-service' and download

3. The pom.xml will have the following dependencies: 
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
	<scope>runtime</scope>
	<optional>true</optional>
</dependency>


4. Create an Order model in the model directory
Create a package called model and inside it create
a Order.java file as follows:

import java.math.BigDecimal;
import java.time.ZonedDateTime;
public record Order(Long id, Long customerId, ZonedDateTime orderTime, BigDecimal totalAmount) {
}

5. Add a controller file inside the controller directory  
Create a package called controller and inside it create
OrderController.java as follows:

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.java.bala.springboot.order_service.model.Order;

@RestController
@RequestMapping ("/orders")
public class OrderController {
	
	@GetMapping ("/{id}")
	public Order findbyId(@PathVariable Long id) {
		return new Order(id, 1L, ZonedDateTime.now(), BigDecimal.TEN);
	}
	

}

Just for illustation purpose, its a simple controller 
that does nothing but takes an order id and creates a java record 
and returns it back with an order time and order amount


6. Check different ways of running the application below
``` 

## Auto Instrumentation - Agent Based

### Run the service as a jar file
```xml
Download open telemetry java agent:
curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

Run the service with open telemetry java agent: 
java -javaagent:opentelemetry-javaagent.jar -jar target/order-service-0.0.1-SNAPSHOT.jar 
(Since we are not running open telemetry collector service sperately on port 4318 
we will have an error message Failed to connect to localhost/[0:0:0:0:0:0:0:1]:4318)

Log all metrics on the console:
java -javaagent:opentelemetry-javaagent.jar -Dotel.traces.exporter=logging -Dotel.metrics.exporter=logging -Dotel.logs.exporter=logging  -jar target/order-service-0.0.1-SNAPSHOT.jar

Excute the curl command to see the logs:
curl --location 'http://localhost:8080/orders/1'

```

### Run the service directly from eclipse
```xml
Right click on the project -> Run Configuration -> Arguments (tab) -> VM Arguments ->
-javaagent:opentelemetry-javaagent.jar -Dotel.traces.exporter=logging -Dotel.metrics.exporter=logging -Dotel.logs.exporter=logging

Now right click and run the project 

Excute the curl command to see the logs:
curl --location 'http://localhost:8080/orders/1'

```

### Run the service as a docker build
```xml
Clean and Build the application

Create a Dockerfile:
FROM eclipse-temurin:17-jre

ADD target/order-service-0.0.1-SNAPSHOT.jar order-service-0.0.1-SNAPSHOT.jar
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

ENTRYPOINT java -javaagent:/opentelemetry-javaagent.jar \ 
	-Dotel.traces.exporter=logging \ 
	-Dotel.metrics.exporter=logging \ 
	-Dotel.logs.exporter=logging  \
	-jar /order-service-0.0.1-SNAPSHOT.jar

Create a docker-compose.yml file:
version: '3'
services:
    order-service:
        build: ./
        ports: 
            - "8080:8080"


Make sure docker is running (docker desktop in my case)
Run the following command: 
docker compose up -d

Copy the container id by running the following command:
docker ps

Follow the logs
docker logs -f <container-id>

Excute the curl command to see the logs:
curl --location 'http://localhost:8080/orders/1'

Finally stop the application:
docker compose down

```

### Optimizing the docker build
```xml
Prevent the docker image from downloading otel everytime from the internet
Add the following mavel resource plugin 
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-my-file</id>
            <!-- Bind to a phase like validate, generate-resources, or compile -->
            <phase>validate</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <!-- The output directory to copy the resources to -->
                <outputDirectory>${project.build.directory}/agent</outputDirectory>
                <resources>
                    <resource>
                        <!-- The directory where your source file is located -->
                        <directory>${project.basedir}</directory>
                        <includes>
                            <!-- The specific file(s) you want to copy -->
                            <include>opentelemetry-javaagent.jar</include>
                        </includes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>

Delete the docker image, Clean and Build the application 
and check if opentelemetry-javaagent.jar is copied from the project root folder 
to the /target/agent folder 

Modify the Dockerfile to remove the image from downloading from the github
and adding the environment variables during building of image.

FROM eclipse-temurin:17-jre

ADD target/order-service-0.0.1-SNAPSHOT.jar order-service-0.0.1-SNAPSHOT.jar
ADD target/agent/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

ENTRYPOINT java -javaagent:/opentelemetry-javaagent.jar -jar /order-service-0.0.1-SNAPSHOT.jar

Modify thedocker-compose.yml file to add OTel environment varibales:
version: '3'
services:
    order-service:
        build: ./
        environment:
            - OTEL_TRACES_EXPORTER=console
            - OTEL_METRICS_EXPORTER=console
            - OTEL_LOGS_EXPORTER=console
        ports: 
            - "8080:8080"

Make sure docker is running (docker desktop in my case)
Run the following command: 
docker compose up -d

Copy the container id by running the following command:
docker ps

Follow the logs
docker logs -f <container-id>

Excute the curl command to see the logs:
curl --location 'http://localhost:8080/orders/1'

Finally stop the application:
docker compose down

```

## Create an order service that connects to database
```xml
1. Spring Initilizer
Go to spring initilizer page https://start.spring.io/ 
and add the following dependencies: 
Spring Web
DevTools
Spring JPA
Postgres SQL Driver


2. Create a project 'order-service-jpa' and download

3. The pom.xml will have the following dependencies: 
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
	<scope>runtime</scope>
	<optional>true</optional>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
	<groupId>org.postgresql</groupId>
	<artifactId>postgresql</artifactId>
	<scope>runtime</scope>
</dependency>

4. Add the following mavel resource plugin 
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-my-file</id>
            <!-- Bind to a phase like validate, generate-resources, or compile -->
            <phase>validate</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <!-- The output directory to copy the resources to -->
                <outputDirectory>${project.build.directory}/agent</outputDirectory>
                <resources>
                    <resource>
                        <!-- The directory where your source file is located -->
                        <directory>${project.basedir}</directory>
                        <includes>
                            <!-- The specific file(s) you want to copy -->
                            <include>opentelemetry-javaagent.jar</include>
                        </includes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>

5. Create a docker compose file (docker-compose.yml):
version: '3'
services:
    order-service:
        build: ./
        environment:
            - OTEL_TRACES_EXPORTER=console
            - OTEL_METRICS_EXPORTER=console
            - OTEL_LOGS_EXPORTER=console
        ports: 
            - "8080:8080"
        depends_on:
            - postgres

    postgres:
        container_name: postgres
        image: postgres:latest
        restart: always
        environment:
          - POSTGRES_DB=opentelementry
          - POSTGRES_PASSWORD=secret
          - POSTGRES_USER=myuser
        ports:
          - "5432:5432"

Note: The postgres service added. 

Test if the service creates a database correctly or not by running:
docker compose up postgres -d

6. In the application.properties files add the following database 
connection parameters
spring.application.name=order-service-jpa

spring.sql.init.mode=always

# Running this as application
#spring.datasource.url=jdbc:postgresql://localhost:5432/opentelementry

# Running this inside docker
spring.datasource.url=jdbc:postgresql://postgres:5432/opentelementry
spring.datasource.username=myuser
spring.datasource.password=secret

spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.default_schema=orders

7. Create a docker file and add the following
FROM eclipse-temurin:17-jre

ADD target/order-service-jpa-0.0.1-SNAPSHOT.jar order-service-jpa-0.0.1-SNAPSHOT.jar
ADD target/agent/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

ENTRYPOINT java -javaagent:/opentelemetry-javaagent.jar -jar /order-service-jpa-0.0.1-SNAPSHOT.jar

(nothing new here, and it is as per the previous example)

8. Create an Order Entity model in the model directory
Create a package called model and inside it create
a Order.java file as follows:

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="orders")
public class Order {
	
	@Id
	private Long id; 
	
	@Column(name="customer_id")
	private Long customerId;
	
	@Column(name="order_date")
	private ZonedDateTime orderTime;
	
	@Column(name="total_amount")
	private BigDecimal totalAmount;
	
	public Long getId() {
		return id;
	}
	public Long getCustomerId() {
		return customerId;
	}
	public ZonedDateTime getOrderTime() {
		return orderTime;
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}	
}

9. Create an Order Repository in the repository directory
Create a package called repository and inside it create
a OrderRepository.java file as follows:

import org.springframework.data.jpa.repository.JpaRepository;
import com.java.bala.springboot.order_service_jpa.model.Order;
public interface OrderRepository extends JpaRepository<Order, Long> {

}

10. Add a controller file inside the controller directory  
Create a package called controller and inside it create
OrderController.java as follows:

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.java.bala.springboot.order_service_jpa.model.Order;
import com.java.bala.springboot.order_service_jpa.repository.OrderRepository;

@RestController
@RequestMapping ("/orders")
public class OrderController {
	
	private final OrderRepository orderRepository;
	
	public OrderController(OrderRepository orderRepository) {
		super();
		this.orderRepository = orderRepository;
	}

	@GetMapping ("/{id}")
	public Order findbyId(@PathVariable Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid id :"+ id));
	}
}

Just for illustation purpose, its a simple controller 
that does nothing but takes an order id and fetches the 
Order details from the Postgres database and returns it

12. Create the database schema and data scripts
In the application root folder create the following files:
schema.sql 
CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE orders.orders (
	id int8 NOT NULL,
	customer_id int8 NULL,
	order_date timestamptz(6) NULL,
	total_amount numeric(38,2) NULL,
	CONSTRAINT order_pkey PRIMARY KEY(id)	
);

data.sql
INSERT INTO orders.orders(id, customer_id, order_date, total_amount) VALUES (1, 1, now(), 10);
INSERT INTO orders.orders(id, customer_id, order_date, total_amount) VALUES (2, 2, now(), 20);
INSERT INTO orders.orders(id, customer_id, order_date, total_amount) VALUES (3, 3, now(), 30);

13. Build and Run the application 
Clean and Build the application and check if 
opentelemetry-javaagent.jar is copied from the project root folder 
to the /target/agent folder 

Make sure the application runs using: 
java -javaagent:opentelemetry-javaagent.jar -Dotel.traces.exporter=logging -Dotel.metrics.exporter=logging -Dotel.logs.exporter=logging

Check if database is created and records inserted into the postgres database
database name: opentelemetry
schema name: orders
table name: orders


14. Next make sure docker is running (docker desktop in my case)
Run the following command: 
docker compose up -d

Copy the container id by running the following command:
docker ps

Follow the logs
docker logs -f <container-id>

Excute the curl command to see the logs:
curl --location 'http://localhost:8080/orders/1'
curl --location 'http://localhost:8080/orders/2'
curl --location 'http://localhost:8080/orders/3'

Finally stop the application:
docker compose down

``` 

## Push OpenTelemetry trace data to Jeager 
```xml
1. In the pom.xml add the following 2 dependencies. 
<!-- Micrometer Tracing bridge for OTel -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>

<!-- OTel Exporter for Jaeger/OTLP compatibility -->
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>

2. Add the following 2 configurations in the application.properties files:
# Enable OTLP tracing endpoint
management.otlp.tracing.endpoint=http://jaeger:4318/v1/traces

# Set the tracing sampling rate to 100% (for development/testing)
management.tracing.sampling.probability=1.0

3. Modify the Dockerfile to remove the javaagent opentel dependency:

ADD target/order-service-jpa-0.0.1-SNAPSHOT.jar .
#ADD target/agent/opentelemetry-javaagent.jar .

#ENTRYPOINT java -javaagent:opentelemetry-javaagent.jar -jar order-service-jpa-0.0.1-SNAPSHOT.jar
ENTRYPOINT java -jar order-service-jpa-0.0.1-SNAPSHOT.jar

4. In the docker-compose.yml file make the following changes:
- OTEL_SERVICE_NAME=order-service-jpa
# - OTEL_TRACES_EXPORTER=console
- OTEL_TRACES_EXPORTER=jaeger

5. Clean and build the application

6. Next make sure docker is running (docker desktop in my case)
Run the following command: 
docker compose up -d

Copy the container id by running the following command:
docker ps

Follow the logs
docker logs -f <container-id>

Excute the curl command to see the logs:
curl --location 'http://localhost:8080/orders/1'
curl --location 'http://localhost:8080/orders/2'
curl --location 'http://localhost:8080/orders/3'\

7. Open the Jagger UI from the below URL:
http://localhost:16686/search

From the services dropdown search for 
order-service-jpa

select and click find traces
You will see the complete list of all http traces. 
Click on the trace and explore

8. Finally stop the application:
docker compose down

``` 

## Push OpenTelemetry trace data to Zipkin 
```xml
1. Run zipkin on docker
Create a docker-compose.yml file
services:
    zipkin:
        image: openzipkin/zipkin:latest
        container_name: zipkin
        ports:
            - "9411:9411"


2. Run the docker zipkin container
docker compose up -d

3. Make sure Zipkin is running:
http://localhost:9411/

4. Create a spring boot app with the following dependencies:
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
	<scope>runtime</scope>
	<optional>true</optional>
</dependency>

<!-- Spring Boot Actuator for tracing and health endpoints -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Bridge Spring/Micrometer Observations to OpenTelemetry -->
<dependency>
	<groupId>io.micrometer</groupId>
	<artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>

<!-- Export spans to Zipkin -->
<dependency>
	<groupId>io.opentelemetry</groupId>
	<artifactId>opentelemetry-exporter-zipkin</artifactId>
</dependency>

5. Export the traces to zipkin by configuring in the application.properties
# Tracing
management.tracing.sampling.probability=1.0
# Export traces to Zipkin
management.tracing.export.zipkin.endpoint=http://localhost:9411/api/v2/spans

6. Create a simple controller:
@RestController
@RequestMapping ("/orders")
public class OrderController {
	
	private final ObservationRegistry observationRegistry;
	
	public OrderController(ObservationRegistry observationRegistry) {
	    this.observationRegistry = observationRegistry;
	}

	@GetMapping ("/{id}")
	public Order findbyId(@PathVariable Long id) {
		return new Order(id, 1L, ZonedDateTime.now(), BigDecimal.TEN);
	}
	
}

7. Run the application
8. Excute the curl command:
curl --location 'http://localhost:8080/orders/1'
curl --location 'http://localhost:8080/orders/2'
curl --location 'http://localhost:8080/orders/3'

9. View the traces on Zipkin
http://localhost:9411/
Click Run Query 

10. Stop the application and bring the docker container down.
docker compose down


``` 

## Reference
```xml
https://www.youtube.com/playlist?list=PLLMxXO6kMiNg6EcNCx6C6pydmgUlDDcZY

```



