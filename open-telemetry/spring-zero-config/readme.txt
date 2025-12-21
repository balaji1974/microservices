Maven command to run:
clean compile package
skip Test


Docker commands to run:

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