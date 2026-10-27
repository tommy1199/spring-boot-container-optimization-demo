# Movie Management Service

A Spring Boot REST API for managing movies with PostgreSQL persistence and Kafka event streaming.

## Features

- **CRUD Operations**: Create, Read, Update, and Delete movies
- **PostgreSQL Database**: Spring Data JDBC-based persistence
- **Kafka Event Streaming**: Publishes events for all movie operations
- **Search Functionality**: Search movies by name or category
- **Docker Compose**: Easy setup for Postgres and Kafka

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose

## Project Structure

```
movie-service/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/movieservice/
│       │       ├── controller/
│       │       │   └── MovieController.java
│       │       ├── event/
│       │       │   └── MovieEvent.java
│       │       ├── kafka/
│       │       │   └── MovieEventProducer.java
│       │       ├── model/
│       │       │   └── Movie.java
│       │       ├── repository/
│       │       │   └── MovieRepository.java
│       │       ├── service/
│       │       │   └── MovieService.java
│       │       └── MovieServiceApplication.java
│       └── resources/
│           ├── application.properties
│           └── db/
│               └── changelog/
│                   ├── db.changelog-master.yaml
│                   └── changes/
│                       └── 001-create-movies-table.yaml
├── docker-compose.yml
└── pom.xml
```

## Getting Started

### 1. Start Infrastructure Services

Start PostgreSQL and Kafka using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Kafka on port 9092
- Zookeeper on port 2181

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

Liquibase will automatically:
- Create the `movies` table on first startup
- Add indexes for category and name columns
- Track all schema changes in the `databasechangelog` table

## Database Schema

The `movies` table structure:

| Column       | Type         | Constraints    |
|--------------|--------------|----------------|
| id           | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name         | VARCHAR(255) | NOT NULL       |
| release_date | DATE         | NOT NULL       |
| category     | VARCHAR(100) | NOT NULL       |

Indexes:
- `idx_movies_category` on `category` column
- `idx_movies_name` on `name` column

## API Endpoints

### Get All Movies
```http
GET /api/movies
```

### Get Movie by ID
```http
GET /api/movies/{id}
```

### Get Movies by Category
```http
GET /api/movies/category/{category}
```

### Search Movies by Name
```http
GET /api/movies/search?name={searchTerm}
```

### Create Movie
```http
POST /api/movies
Content-Type: application/json

{
  "name": "The Matrix",
  "releaseDate": "1999-03-31",
  "category": "Science Fiction"
}
```

### Update Movie
```http
PUT /api/movies/{id}
Content-Type: application/json

{
  "name": "The Matrix Reloaded",
  "releaseDate": "2003-05-15",
  "category": "Science Fiction"
}
```

### Delete Movie
```http
DELETE /api/movies/{id}
```

## Example Usage with curl

### Create a Movie
```bash
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Inception",
    "releaseDate": "2010-07-16",
    "category": "Thriller"
  }'
```

### Get All Movies
```bash
curl http://localhost:8080/api/movies
```

### Update a Movie
```bash
curl -X PUT http://localhost:8080/api/movies/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Inception Updated",
    "releaseDate": "2010-07-16",
    "category": "Sci-Fi Thriller"
  }'
```

### Delete a Movie
```bash
curl -X DELETE http://localhost:8080/api/movies/1
```

## Kafka Events

The service publishes events to the `movie-events` Kafka topic for the following operations:

- **CREATED**: When a new movie is created
- **UPDATED**: When a movie is updated
- **DELETED**: When a movie is deleted

### Event Structure
```json
{
  "eventType": "CREATED",
  "movieId": 1,
  "movieName": "Inception",
  "category": "Thriller",
  "timestamp": "2025-10-22T10:30:00"
}
```

### Monitoring Kafka Events

You can monitor Kafka events using the Kafka console consumer:

```bash
docker exec -it movie-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic movie-events \
  --from-beginning
```

## Database Access

PostgreSQL connection details:
- **Host**: localhost:5432
- **Database**: moviedb
- **Username**: movieuser
- **Password**: moviepass

You can connect using any PostgreSQL client or psql:

```bash
docker exec -it movie-postgres psql -U movieuser -d moviedb
```

## Liquibase Management

### View Applied Changesets
```bash
docker exec -it movie-postgres psql -U movieuser -d moviedb -c "SELECT * FROM databasechangelog;"
```

### Add New Database Changes

Create a new changeset file in `src/main/resources/db/changelog/changes/`:

```yaml
# 002-add-director-column.yaml
databaseChangeLog:
  - changeSet:
      id: 002-add-director-column
      author: movie-service
      changes:
        - addColumn:
            tableName: movies
            columns:
              - column:
                  name: director
                  type: VARCHAR(255)
      rollback:
        - dropColumn:
            tableName: movies
            columnName: director
```

Then add it to `db.changelog-master.yaml`:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/001-create-movies-table.yaml
  - include:
      file: db/changelog/changes/002-add-director-column.yaml
```

### Rollback Changes

Liquibase supports rollback operations. To rollback the last change:

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

## Stopping the Application

### Stop the Spring Boot Application
Press `Ctrl+C` in the terminal where the application is running

### Stop Infrastructure Services
```bash
docker-compose down
```

To also remove volumes (database data):
```bash
docker-compose down -v
```

## Configuration

Configuration can be modified in `src/main/resources/application.properties`:

- Server port
- Database connection settings
- Kafka bootstrap servers
- Spring Data JDBC SQL logging
- Kafka topic names

## Technologies Used

- **Spring Boot 3.2.0**
- **Spring Data JDBC**
- **Spring Kafka**
- **PostgreSQL 15**
- **Apache Kafka 7.5.0**
- **Liquibase** (Database Migration)
- **Maven**
- **Lombok**
- **Docker & Docker Compose**