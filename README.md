# Membership Service

Microservice part of Gym Management System, this service contains create memberships, upgrade or downgrade them.

## Running Locally

### **Prerequisites**
To run the application locally, ensure you have the following:

- **MySQL** – Installed and running.
- **Java 17+** – Ensure you have Java Development Kit (JDK) installed.
- **Maven** – To build and run the project.
- **Docker** – If you prefer running MySQL in a container. But it is necessary for running Kafka.
- **Discovery Service** – This microservice relies on a service discovery component. Ensure it is running before starting this service.
- **Kafka** – This service runs on a docker container, so before calling this service, and instance of Kafka must be up 
and running. The logic for creating and managing memberships is handled by the **orchestrator-service**, so you must not call directly
  this service or any other. 

## Setup Instructions

### 1. Start the Discovery Service
Before running this service, you need to start the Discovery Service to enable service registration and discovery.

```sh
cd path/to/discovery-service
mvn spring-boot:run
```

### 2. Configure MySQL Database
If MySQL is installed locally, create a database:

```sql
CREATE DATABASE gymcustomerdb;
```
You can also use Docker to run MySQL in a container for a more isolated setup:

```bash
docker run --name gym-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=gym_management -p 3306:3306 -d mysql:8
```
This command will run a MySQL container with the database **gymcustomerdb** created and available on port 3306.

### 3. Configure Application Properties
Make sure the application’s database connection is correctly configured. Open src/main/resources/application.properties (or application.yml) and ensure the following configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gymcustomerdb
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=true
```

If you're using Docker for MySQL, ensure the host is localhost or the IP address of your Docker container.

#### Important ####

The **DB_USERNAME** and **DB_PASSWORD** environment variables must be configured before running the application.
These values can be securely stored in:

- GitHub Secrets for secure CI/CD integration.
- Environment Variables in your development environment, such as IntelliJ IDEA or your operating system.

### 4 . Configure Kafka Properties
Make sure kafka is correctly configured, change ports if necessary, also, since thi is the producer service that sends
messages to kafka topic, we must include serializer and deserializer in this configuration.

```properties
# Kafka configuration
# Connect to Kafka inside Docker
spring.cloud.stream.kafka.binder.brokers=localhost:9092
# Topic name
spring.cloud.stream.bindings.membership-out.destination=membership-topic
spring.cloud.stream.bindings.membership-out.content-type=application/json

# Kafka Producer Properties
spring.cloud.stream.kafka.bindings.membership-topic.producer.configuration.key.serializer=org.apache.kafka.common.serialization.StringSerializer
spring.cloud.stream.kafka.bindings.membership-topic.producer.configuration.value.serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### 5. Start the Membership Service
Once the Discovery Service is up and running, navigate to the directory of the membership microservice:

```bash
cd path/to/membership-service
```
Run the following command to build and start the application:

```bash
mvn spring-boot:run
```
This will start the Membership Service, which should now be available on http://localhost:8084 (or any configured port).

### **Membership Service API Endpoints**

- **GET** `/api/memberships`  
  Retrieve a list of all memberships available.
- **GET** `/api/memberships/{id}`  
  Get detailed information of a specific membership by its unique ID.
- **POST** `/api/memberships/`  
  Create a new membership.
- **PUT** `/api/memberships/{uuid}/upgrade`  
  Upgrades membership to GOLD level.
- **PUT** `/api/memberships/{uuid}/downgrade`  
  Downgrades membership to BASIC level.

### **Troubleshooting**
1. Ensure that the Discovery Service is running before starting the Membership service.
2. Check that MySQL is correctly configured and the **gymcustomerdb** database exists.
3. If running MySQL via Docker, verify that the container is up and accessible.
4. Check that kafka container is running correctly.