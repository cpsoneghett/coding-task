

# 🚀 Coding Task - Device API

**Author:** Christiano Soneghett


This coding task is to develop a REST API capable of persisting and managing device resources.

## 📦 Stack

- [Java 21](https://docs.oracle.com/en/java/javase/21/)
- [Spring Boot 3.5.3](https://spring.io/projects/spring-boot) 
- [MySQL 8.x](https://dev.mysql.com/downloads/mysql/8.0.html) 
- [Spring Doc (Swagger)](https://springdoc.org/) 
- [Docker](https://www.docker.com/)  (Image and Containerization)


## How to Run The Application:

### 1. With Docker:

At the root directory, run:

```bash
docker-compose up --build
```

Then, the server might start at port 8081 as exposed in the docker-compose.yml. If not, after MySQL starts, you can run
the application directly from the project root with tests:

```bash
mvn spring-boot:run
```

##

## API Documentation:

The information regarding the API and all the operations available, including for testing are documented and available in the Swagger page below:

http://localhost:8081/swagger-ui/index.html

## Possible improvements:

1) More unit and integrated tests. Some cases might not be well covered;
2) Caching search;
3) To implement security for the endpoints;
4) Review code to performance improvement.
