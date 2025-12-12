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

## Reference
```xml
https://www.youtube.com/playlist?list=PLLMxXO6kMiNg6EcNCx6C6pydmgUlDDcZY

```



