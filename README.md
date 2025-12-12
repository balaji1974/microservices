# Microservices - Step by Step      
This has details to building microservices from scratch

# Port Details  
##(will be updated as and when new services are added)  
### Spring Cloud Config Server
server.port=9100


### Spring Cloud Eureka Server
server.port=9200 (prod)  
server.port=9210 (test)  
server.port=9220 (dev)  

### Spring Cloud API Gateway  
server.port=9000 (prod)  
server.port=9010 (test)  
server.port=9020 (dev)  


### Zipkin (distrubuted tracing)    
QUERY_PORT=9300   

### Zoo Keeper and Apache Kakfa (middleware)   
Default ports were used   
ZooKeeper=2181   
Kafka=9092  

### Elastic Search (To store tracelog)    
http.port: 9400    
transport.port: 9500    
(or else default will be 9300 and wwill conflict with my zipkin port)


# Step 1: Build the cloud config server (for centralized configuration management)

a. Include the following dependency in the pom.xml 
```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

b. Add the following in the application.properties file  
spring.application.name=cloud-config-server  
server.port=9100  

c. Create a local config folder called git-config-repo and save this file as <application_name>.properties  
eg. login-service.properties  

d. Now go to this folder in command prompt and create a local git reposistory. This is where all the configurations will reside. The following is commands that need to be run for this.
cd git-config-repo  
git init  
git add .  
git commit -m "Initial commit"  

e. The final configuration is to tie this git config folder in the config server property file. This can be done by add the following line in the application.properties file.  
spring.cloud.config.server.git.uri=file:///Users/balaji/eclipse-workspace/Microservices/git-config-repo  

f. Please note that in my case I pushed all my config files into the centralized github and pulled them up during the application startup rather than maintaing it locally.  
For this instead of the above line I did the below configuration after pushing my config files to github  
spring.cloud.config.server.git.uri=https://github.com/balaji1974/microservices  
spring.cloud.config.server.git.searchPaths=git-config-repo  
spring.cloud.config.server.git.default-label=main  
(the above line depends on your branch name in github - avoiding master :))   

Add the user name and password as below in case of private repository  
spring.cloud.config.server.git.username=   
spring.cloud.config.server.git.password=   


Note: For private repositories please generate a personal access token and use this in the place of password 

f. Add the following annotation in the main class to make spring aware that this is a configuration server 
@EnableConfigServer

g. Start the server and use the browser to go to the following url:  
http://localhost:9100/login-service/default  
If a json respone with the config file content is displayed then everything is working fine.  

With this the config server is ready to pick the configuration files from the central git repo.  

# Step 2: Connecting the client services to the config server.

a. Include the following dependency in the pom.xml 

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

b. In the application.properties file enter the application name. This name should macth the name given in the cloud config property file.  
spring.application.name=login-service

c. Finally connect the client to the config server with the following line.  
spring.config.import=optional:configserver:http://localhost:9100  

With this the client will be able to fetch all the configurations from the central config server.  


# Step 3: Adding multiple enviroment properties to the config server   
Make the copy of the properties file that as created earlier and create files for dev and test enviroments as  
login-service-dev.properties  
login-service-test.properties  
Make changes to this file as per your local settings  

Excute the following command in git for these files.  
git add .  
git commit -m "Added new enviroment files"  

If the repository is located in github push these files.  

Now restart the config server and check if the files load properly with the following url:  
http://localhost:9100/login-service/dev  
http://localhost:9100/login-service/test  

Now to configure the client for the different enviroments we need to add the below line of code in the application.properties file of the client service.   
spring.profiles.active=test  

If the above does not work [in some versions of spring because of bug] we need to add the below line also. For me it works and the below line is not needed.  
spring.cloud.config.profile=test  

Thats it and all profiles are set and reading now from the central config server after client restart  


# Step 4: Adding basic security to connect to Spring Config Server (This will be later changed to OAuth2.0 when we integrate an OAuth Server)  

### a. Spring config server by default comes with basic security and all we need to add is the following 2 lines in the application.properties file.   
spring.security.user.name=balaji  
spring.security.user.password=balaji  

b. Now the clients can connect to the server using the same user id and password. It can be enabled on the client side by adding the following 2 lines:  
spring.cloud.config.username=balaji  
spring.cloud.config.password=balaji  

But in 99% of the use cases this might not be so useful as the password is open and anyone can peek into it.  

c. So we need to have some basic encryption for this password.  

For this to happen we need have "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" in our JVM   
I did not have this with me so, I went to oracle and downloaded it. Next I installed the 2 jar files that came with it, in my JDK_HOME/lib/security/ folder  

d. Next step is to encrypt my password. For this from command line I ran the following script after starting my config server.  
curl localhost:9100/encrypt -d balajibalaji -> Where 'balajibalaji' is my secrect key.  

I will now get an encrypted password which I can copy and paste it in the config server properties file like below:
spring.cloud.config.password={cipher}936fbc53d891780736d0470380c49beb43e73aee0fed5a639f1a220e3e2ba999  

Please note that {cipher} is ammended before the password which tells Springs that my password is encrypted.  

Also the secrect I used must be specified to Spring for it to use in encrypt/decrypt of passwords. This is done by added it in the following way:  
encrypt.key=balajibalaji  

e. So my final application.properties file for the config server would be:  

encrypt.key=balajibalaji  

spring.security.user.name=balaji  
spring.security.user.password={cipher}ccae6044dc0fd0a13e3e459298b7ff5bdf4ed56ee9a204779fb0e108b022af33  


### f. On the client side the configuration is fairly straigh forward:  


encrypt.key=balajibalaji  
spring.cloud.config.username=balaji
spring.cloud.config.password={cipher}ccae6044dc0fd0a13e3e459298b7ff5bdf4ed56ee9a204779fb0e108b022af33  

This security is not tamper proof but atleast it will not expose my passwords to the outside would when I push my config server or the microservice to some other third party location.  


# Step 5: Adding Discovery and Naming Server (Eureka) into my microservice ecosystem

a. Include the following two dependencies for config client and eureka server in the pom.xml 

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

b. Add the following annotation in the main class to make spring aware that this is a eureka server  
@EnableEurekaServer  

c. Connect Eureka to the config client with the following properties:  
spring.application.name=eureka-naming-server  
spring.config.import=optional:configserver:http://localhost:9100  

encrypt.key=pA5hGk9SUN87  
spring.cloud.config.username=balaji  
spring.cloud.config.password={cipher}1a0de898f66611ff768031fb288db89146a249989996ae638f6431c437d62d5f  

spring.profiles.active=test  

d. Now as usual create 3 config files as shown below in the local github repository of the config client, add the below configurations to it and show below and commit it as before.  
eureka-naming-server.properties  
eureka-naming-server-test.properties  
eureka-naming-server-dev.properties  

The contents of the file are sampled below:  
spring.application.name=eureka-naming-server  
server.port=9200  

eureka.client.register-with-eureka=false  
eureka.client.fetch-registry=false  
eureka.server.maxThreadsForPeerReplication=0


e. Now start up Eureka and see if it is picking the configuration from the config files. In our case it must pick from the test configuration file as we had given spring.profiles.active=test  

Thats it. Eureka server is up and running.  


# Step 6: Connecting the client services to my discovery and naming server (Eureka)  

a. Include the following dependency for eureka client in the pom.xml  

```xml  
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

b. Add the following annotation in the main class to make spring aware that this is a eureka client  
@EnableEurekaClient  

c. Connect the client services with the Eureka server by adding the following properties:  
eureka.client.serviceUrl.defaultZone=http://localhost:9200/eureka  

Of course this file will be in the git repo and hence it has to be committed.  

d. Restart the config server first, then the Eureka and then the client.  
Now check if the client is connected with Eureka by going to the following URL:  (since I have enabled test profile)
http://localhost:9210/  


# Step 7: Adding basic security on the Spring Cloud Eureka Server (This will be later changed)  
a. Include the following dependency for security in the pom.xml  

```xml  
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
``` 

b. Add the following basic in-memory (to be changed later) configuration class. 

@Configuration  
@EnableWebSecurity  
@Order(1)   
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {  

&nbsp;&nbsp;&nbsp;@Autowired  
&nbsp;&nbsp;&nbsp;public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String password = passwordEncoder().encode("randompwd");  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;auth.inMemoryAuthentication().withUser("balaji")  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.password(password).roles("SYSTEM");  
&nbsp;&nbsp;&nbsp;}  

&nbsp;&nbsp;&nbsp;@Override  
&nbsp;&nbsp;&nbsp;protected void configure(HttpSecurity http) throws Exception {  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;http.sessionManagement()  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.and().requestMatchers().antMatchers("/eureka/**")   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.and().authorizeRequests().antMatchers("/eureka/**")  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.hasRole("SYSTEM").anyRequest().denyAll().and()  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.httpBasic().and().csrf().disable();  
&nbsp;&nbsp;&nbsp;}   
   
&nbsp;&nbsp;&nbsp;@Bean  
&nbsp;&nbsp;&nbsp;public BCryptPasswordEncoder passwordEncoder() {   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return new BCryptPasswordEncoder();  
&nbsp;&nbsp;&nbsp;}  
}  

With this the server is secure.  


# Step 8: Changing the client configuration to connect to Eureka   

a. On the client property file modify the following line:  

eureka.client.serviceUrl.defaultZone=http://localhost:9210/eureka  

to   

eureka.client.serviceUrl.defaultZone=http://balaji:randompwd@localhost:9210/eureka   

Thats all and the client is now ready to connect to Eureka after restart.   

# Step 9: Creating the API Gateway  
a. The dependencies that are needed for the api-gateway are - Cloud config client for fetching the centralized configuration, Eureka client for registering with discovery services and Spring cloud Gatway server.  
Add then as follows  

```xml 
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
``` 

b. Check if the dependences are downloaded and if the server is starting successfully after this  

c. Add the following configuration properties in the API-Gateway propeties file that is stored in the github and commit it  
(As usual create 3 properties file for each enivornment and add this into all these 3 properties file)  

spring.cloud.gateway.discovery.locator.enabled=true  
spring.cloud.gateway.discovery.locator.lower-case-service-id=true   

This will allow the api gateway to discover other services that are registered in Eureka and this will also allow other microservices to be called using the API gateway URL   

As a next step our login microservice was called using the below URL  
http://localhost:8110/login  

Now it can be accessed using the API gateway URL as follows:   
http://localhost:9010/login-service/login  

d. Please note that API gateway is not only for adding your cross cutting concerns but it can be used for various routing options that are needed for your microservice.  
This can be achieved by the following configuration class that can be added into the gateway:  
@Configuration  
public class ApiGatewayConfiguration<PredicateSpec> {  

&nbsp;&nbsp;&nbsp;@Bean  
&nbsp;&nbsp;&nbsp;public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return builder.routes()  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.route(p -> p  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.path("/mytest-api")  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.filters(f -> f  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.addRequestHeader("CustomerHeader", "Anything Here")  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.addRequestParameter("Custom Parameters", "Custom Values To add"))  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.uri("http://httpbin.org:80")  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;).route(p -> p.path("/login/\*\*")   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.uri("lb://login")  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;).build();  
&nbsp;&nbsp;&nbsp;}  
}  
The above is just a sample of what can be achieved with the router inside API gateway like injecting headers, performing alternate routes, going for a loadbalancer url from Eureka etc   


# Step 10 How do we handle if something goes wrong between microservices calls. Implementing Circuit Breakers in our microservices chain using resillence4j is what we will see next   
a. Lets add resilience4j dependency in the login microservice to test our application. The two dependencies needed are:   
```xml   
<dependency>  
	<groupId>org.springframework.boot</groupId>  
	<artifactId>spring-boot-starter-aop</artifactId>  
</dependency>  
<dependency>  
	<groupId>io.github.resilience4j</groupId>  
	<artifactId>resilience4j-spring-boot2</artifactId>  
</dependency>  
```   
b. Lets create a new method in our Login Controller to test the retry and circuit breaker logic  
&nbsp;&nbsp;&nbsp;@GetMapping("/hello")   
&nbsp;&nbsp;&nbsp;public String helloWorld() throws RuntimeException{  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.info("Method is being called");  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if(true) throw new RuntimeException();  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return "Hello World";  
&nbsp;&nbsp;&nbsp;}  

c. Check if application starts and if this method is accesssable but throws exception.   
Now add the following annotation to this method.   
@Retry(name="default")   

This will retry this method for 3 times before an error is thrown. This is the defaulf behaviour of the retry method.  

d. Now add a named retry annotation as follows instead of the default so that we can configure the retry behaviour in the application.properties file.  
@Retry(name="hello-api")   

In the application properties file add the following lines:  
resilience4j.retry.instances.hello-api.maxAttempts=5  
resilience4j.retry.instances.hello-api.waitDuration=2s  
resilience4j.retry.instances.hello-api.enable-exponential-backoff=true   

This will have a maximum of 5 retries with wait duration of 2 seconds between retries and also an exponential backoff beween subsequent retries.  

e. A fallback method can be configured in the @Retry annotation as follows:  
@Retry(name="hello-api" , fallbackMethod="helloFallback")  

This method should have an Exception class as its parameter:  
&nbsp;&nbsp;&nbsp;public String helloFallback(Exception e) {  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return "This is fallback response";  
&nbsp;&nbsp;&nbsp;}  

f. @CircuitBreaker(name="default", fallbackMethod="helloFallback")  
This annotation is used for circuit breaker configuration. It will break the circuit and will not call the method if it fails continously and will directly call the fallback method after n number of retries.  

Many other configuration settings like bulkhead, ratelimiter and timelimiter are possible with resilience4j.   
You can explore the configuration document at https://resilience4j.readme.io/docs/getting-started-3    


# Step 11: Distributed tracing with Zipkin 

a. Download openzipkin server from the following URL:   
https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=LATEST&c=exec    
Once it is done copy the jar file into a zipkin folder and start it with the following command: (min Java 8 needed)    
java -jar zipkin-server-2.23.2-exec.jar    
Check if zipkin started correctly by going to the following url from the browser:   
http://127.0.0.1:9411/zipkin/   

Start zipkin in our own custom port:   
Zipkin can be started in our own custom port using the command    
java -jar zipkin-server-2.23.2-exec.jar --QUERY_PORT=9300     
A host of other parameters can be configured during startup which is given in the below document url.    
https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md    

Alternatively if you want to start zipkin as a Spring boot project I have included a project named zipkin-tracing-server that can be used, but this supports Java 8 and hence it is not advisable to use this.    

b. Next add the following dependencies into our pom.xml file, in our case the two needed projects into which this must be added are login-service and api-gateway.   
```xml   
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```   

c. Next add the following configuration into the application.properties files of these 2 projects, in our case into the github repo and commit it.   
spring.sleuth.sampler.probability=1    
spring.zipkin.baseUrl=http://127.0.0.1:9300   

Note that 1=100% rate of sampling and 0.5 means 50% etc.    
Also, the zipkin base url must be specified in these 2 projects only if the default zipkin port is changed from 9411    

d. Now restart the api-gateway and the login-service and launch the url for the login-service through the api-gateway and see it getting traced in the zipkin console at http://127.0.0.1:9300/zipkin/    

e. By this we have configured our two microservices to be traced by unique id using cloud slueth and zipkin across multiple service calls that happen internally.   

# Step 12: Connect Zipkin and microservices to an intermediate middleware like Kafka/Rabit MQ etc.   
a. The method of connecting our microservices directly to Zipkin for tracing to prone to errors and scalability issues.    
So we will introduce a middleware that will collect all the tracing data from our microservices and zipkin will connect to this middleware and pick up this data.   

b. To do this our two microservices that use cloud sluth must have one more dependency.   
In our case I am using kafka as the middleware so so the dependency I added for this in API gateway and Login Service is as follows:  
```xml   
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```  

c. Next I start Apache Kafka (check my kafka repository for details) and the default topic that would be created by cloud sluth for publishing is "zipkin"   

d. Next we need to add the following properties in our API gateway and Login service properties files:   
spring.zipkin.baseUrl=http://127.0.0.1:9300  -> This has to be commented as we are no more directly publishing tracelogs into zipkin    
spring.zipkin.sender.type: kafka -> This has to be added to make cloud sluth know that the default collector for log tracing is going to be kafka    

d. Commit this file into github.   

f. Next start zipkin server with the following parameters to let it know that it has to collect trace logs from Kakfa.   
java -jar zipkin-server-2.23.2-exec.jar --QUERY_PORT=9300 --KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092    

Other kafka parameters can also be configured. Please check the below url for details:   
https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md#kafka-collector  

g. Now start the API gateway, Login Service, Apache Kafka and Zipkin.   

h. Thats it, and now you can see all the tracelogs of zipkin being collected from our middleware (Kakfa).   
You can also use other messaging services like RabbitMQ, ActiveMQ etc.   

# Step 13: Store all Zipkin tracelogs into Elastic search so that we do not loose data when the zipkin server restarts.   

a. Zipkin was using in-memory data store to store all its tracelogs.    
This is not ideal in production as all data would be lost during server restarts.   
So we will now configure Elastic search to store all the logs into a persistance state.   
Note, that instead of Elasticsearch we can also configure Cassendra, MySQL or any other NoSQL or JDBC datastores.    

b. For starting Elastic search and configuring its startup port, please refer to my Kafka repositiory where I have a seperate section for this.   

c. To make zipkin use elasticsearch as a storage medium start zipkin with the following parameters:   
--STORAGE_TYPE=elasticsearch --ES_HOSTS=http://localhost:9400 --ES_HTTP_LOGGING=BASIC    

d. Re-start zipkin and make sure that Elasticsearch is also started.   

f. Thats all needs to be done as the configuration is out-of-the-box. All tracelogs are now stored into the persistant storage.      
  

# Misc services
## A. Scheduler-service 
```xml
(Service creation can be done from a REST API endpoint and maintained)
1. It is quite common that microservices need background services for this I have added a scheduler-service
2. It has a database store and hence can be scaled to run mutiple instances 
3. First time while running uncomment property 
spring.quartz.jdbc.initialize-schema=always
from the properties file for quartz to create a database store for its persistance 
4. All quartz properties are configured in quartz.properties file 
5. A sample json script has been provided for creating the scheduler-service and maintaing this service (modify/delete etc)
6. Creating a new job is quite simple as creating a new java class in com.bala.scheduler.schedulerservice.job package and then calling the 
REST end point POST http://localhost:8080/api/saveOrUpdate for creating the job. A sample body of this endpoint is given below: 
{
    "jobName": "Simple Cron Job",
    "jobGroup": "CronJob",
    "jobStatus": "SCHEDULED",
    "jobClass": "com.bala.scheduler.schedulerservice.job.SimpleCronJob",
    "cronExpression": "0 0/1 * 1/1 * ? *",
    "description": "i am job number 2",
    "interfaceName": "interface_2",
    "repeatTime": null,
    "cronJob": true
}
```

---------------------------------------------------

# A full spring boot app example for docker 
## MySQL - run in docker 

```xml
docker pull mysql
docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=<pwd> -d mysql

Connect to the database using any tool like MySQLWorkbench

Next create database: scheduler
Create user and password to connect to this database: sch-user & <pwd>

Try to connect using this userid and check if everything is fine
```

## Spring boot application - Dockerization 
```xml
Go to your application in eclipse 
NOTE: The application must be able to connect using the IP address of the MySQL port (running in docker or outside of docker) before building the application into a jar. 

Eg. spring.datasource.url=jdbc:mysql://192.168.100.16:3306/scheduler 
where 192.168.100.16:3306 is the IP address of mysql and not the localhost. 

This can be achived by editing the my.cnf file and setting the bind address to 
bind-address = 0.0.0.0 

Next Add the following plugin setting in maven project and then clean and install
<plugin>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-maven-plugin</artifactId>
	<configuration>
		<image>
			<name>balaji1974/${project.artifactId}.${project.version}</name>
		</image>
		<pullPolicy>
			IF_NOT_PRESENT
		</pullPolicy>
	</configuration>
</plugin>

If the project artifactId is scheduler-service and version is 0.0.1 then a jar file called scheduler-service-0.0.1.jar  will be created in target folder
```
```xml
Create a docker file in the root directory of the project and name it Dockerfile with the below settings: 
FROM openjdk:11
EXPOSE 8080
ADD target/scheduler-service-0.0.1.jar scheduler-service-0.0.1.jar
CMD ["java", "-jar", "scheduler-service-0.0.1.jar"]

Next build the docker file by running the below command: (where balaji1974 is my docker repo)
docker build -t balaji1974/scheduler-service:latest -t balaji1974/scheduler-service:v0.0.1 .
(I have added multiple tags-latest and v0.0.1 to my imaage)

Run the scheduler service with the below command:
docker container run -p 8080:8080 --name scheduler-service balaji1974/scheduler-service:latest

(This will run the image if it is already present or else it will fetch from the docker hub)
```

## Push / pull to docker hub: 
(Note for free account, push/pull works only for public repo or the first 5 private repositories 
Also push works with docker desktop for private repo but pull does not work) 
Free account allows only one private repo to have multiple tags. 
```xml
docker push balaji1974/scheduler-service:latest
docker push balaji1974/scheduler-service:v0.0.1
docker pull balaji1974/scheduler-service:latest

```

## Docker compose - Microservices Installation and Practical approach 
```xml
Docker compose is already part of Docker desktop and so install docker desktop on mac/windows to get it

Check the docker-compose.yml file

```

## Spring boot application - Kubernetes
```xml
All the below done inside GCP: 
------------------------------
1. First Create a K8S cluster
2. Connect to the cluster 
3. Copy the connect cluster command and paste it in the command line interface inside the cluster and run it (something similar like below)
gcloud container clusters get-credentials <cluster-name> --zone <zone-name> --project <project-name>
4. Check K8S version - kubectl version
5. Check docker version - docker version
6. Next login to docker - docker login 

Creating a kubenetes deployment:
--------------------------------
kubectl create deployment scheduler-service --image=balaji1974/scheduler-service:latest 
kubectl create deployment test-subsystem-01 --image=balaji1974/test-subsystem-01:latest
-> This creates a pod, replicaset & deployment for us 


Expose deployment to the outside world:
---------------------------------------
kubectl expose deployment scheduler-service --type=LoadBalancer --port=8080 
kubectl expose deployment test-subsystem-01 --type=LoadBalancer --port=8080 
-> This creates a service for us 

Get Details
-----------
kubectl get <pods/replicaset/deployment/service> -> Use any one of these parameters to get the details about them from a running cluster

Delete Details 
--------------
kubectl delete <pods/replicaset/deployment/service> <name>
kubectl delete all -l app=test-subsystem-01 

Kubernetes basic architectural units
------------------------------------
Service contains -> ReplicaSets contains -> Pods contains -> Containers

Scale deployments
-----------------
kubectl scale deployment scheduler-service --replicas=3 
kubectl scale deployment test-subsystem-01 --replicas=3 

Autoscale deployements
----------------------
kubectl autoscale deployment test-subsystem-01 --min=1 --max=3 --cpu-percent=5 

Move to next version - attach new image to deployment
-----------------------------------------------------
kubectl set image deployment test-subsystem-01 test-subsystem-01=balaji1974/test-subsystem-01:v0.0.2
(first test-subsystem-01 is the name of the deployment and 
second test-subsystem-01 is the name of the container and 
third test-subsystem-01 is the name of the image)

To tail logs of a service
-------------------------
kubectl logs --follow <pod_name>


Important build and deploy commands of CI/CD pipeline 
-----------------------------------------------------
docker build -t balaji1974/test-subsystem-02:v0.0.1 .
docker push balaji1974/test-subsystem-02:v0.0.1
kubectl create deployment test-subsystem-02 --image=balaji1974/test-subsystem-02:v0.0.1
kubectl expose deployment test-subsystem-02 --type=LoadBalancer --port=8080 
kubectl delete all -l app=test-subsystem-02
kubectl get pods
kubectl logs --follow <pod_name>

Notes: 
-----
Pods are throw away units which have dynamic IP address. 
A service is tied with Pods and exposes a permenant IP to the outside world. 


If our service name is hello-world then kubernetes exposes it in an environment variable called 
HELLO_WORLD_SERVICE_HOST to other services when a new pod is launched

HOSTNAME is the environment variable for the pod name 

kubectl create secret generic regcred --from-file=.dockerconfigjson=./.docker/config.json --type=kubernetes.io/dockerconfigjson


```

## Spring boot application - Observability
```xml
Please check the projects under OpenTelemetry folder

```

## Github actions
```xml
Select the project in Google developer console -> IAM & ADMIN -> Service Accounts 
Create Service Account -> Enter service account name -> eg. github-actions -> Save (skip all other portions)
Next click the account created -> Keys (tab) -> Add Key -> Create New Key -> JSON -> Create (this will download a new key file)
Copy this service account name and go to IAM 
IAM -> Add -> Enter the copied service account name -> Role -> Storage Admin -> Save 

```

## Spring boot - Web Sockets
```xml
simple-socket
-------------
Please refer the application simple-socket

Explaination of this app given in the below link:
https://spring.io/guides/gs/messaging-stomp-websocket/

After starting the application it can be viewed at the following link: 
http://localhost:8080 

```


## Spring boot and application.properties 
### https://pushbuildtestdeploy.com/spring-boot-application.properties-in-kubernetes/


## With this we have come to the end of major components of the microservices architecture using Spring. 
---  
## Next up is adding security to the microservices, containerization with Docker and deployig it on Kubernetes clusters. I have seperate repo for each of them. Please refer them.  

## Watch out for the spring security section which has lots of details on securing microservices. 

## Watch out for devops section whch has lots of detaits on containeratization, kubernetes and builidng and deploying CI/CD pipelines 
---   




