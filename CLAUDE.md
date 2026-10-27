# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

A small Spring Boot "movie service" (REST + PostgreSQL + Kafka) used as a demo app for **container/JAR optimization**: layered extraction via `-Djarmode=tools`, the multi-stage layered `src/main/docker/Dockerfile`, and dependency-usage analysis with the [loosejar](https://github.com/kyrill007/loosejar) java agent. The business code is intentionally simple; the interesting work tends to be in packaging, startup, and image-size experiments.

`presentation/slides.html` is the companion talk ("Lean Spring Boot Containers", reveal.js): SBOM/CVE scanning with Syft and Grype, loosejar-driven dependency pruning, a custom `jlink` JRE, and a minimal OS base layer. Use it to see where the experiments are headed.

## Toolchain

- Java 25 (`java.version` in `pom.xml`), Spring Boot 3.5.x, Maven via the wrapper (`./mvnw`).
- `devbox.json` provides the JDK (Zulu 25) and httpie; use `devbox shell` or `devbox run <script>`.

## Commands

```bash
./mvnw package                     # build target/movie-service-0.0.1-SNAPSHOT.jar
./mvnw spring-boot:run             # run; spring-boot-docker-compose auto-starts compose.yml
./mvnw test                        # no tests exist yet (no src/test)
./mvnw test -Dtest=ClassName#method   # single test, once tests are added

docker compose up -d               # Postgres + Kafka manually (needed for `java -jar`)
docker build -f src/main/docker/Dockerfile -t movie-service .   # after ./mvnw package

just loosejar                      # extract jar to ext/, run with loosejar agent -> test.csv
devbox run analyze                 # extract, run with agent, POST a movie, stop -> test.csv (only 0% jars)
```

`spring-boot-docker-compose` is an optional dependency and is excluded from the repackaged jar, so it only auto-manages containers under `spring-boot:run`/IDE runs. When running the jar directly, start `compose.yml` yourself. This includes `just loosejar`, which unlike `devbox run analyze` does not run `docker compose up -d`; Liquibase migrates at startup, so without Postgres the app fails before loosejar records anything useful.

`devbox run build` runs `./mvnw clean verify` and then builds the `movie-service:latest` image from `src/main/docker/Dockerfile`. `devbox run build-optimized` does the same with `-Poptimized` (the dependency exclusions) and tags `movie-service:optimized`. `devbox run build-ownjre` also uses `-Poptimized` but builds with `src/main/docker/Dockerfile.ownjre` (custom `jlink` JRE from `jdeps`, on `ubuntu:24.04`) and tags `movie-service:ownjre`. `devbox run build-chisel` is the same but uses `src/main/docker/Dockerfile.chisel`, which puts that JRE on a `scratch` image with a minimal Ubuntu rootfs cut by Canonical's `chisel`, and tags `movie-service:chisel`. `devbox run build-apko` builds `src/main/docker/Dockerfile.apko`: same custom JRE, but the root filesystem (`wolfi-baselayout` + `glibc` from Wolfi) is built with `apko build-minirootfs` in a build stage (config inline as a heredoc) and copied onto `scratch`; tags `movie-service:apko`.

## Architecture

Single module, package `com.example.movieservice`, classic layering: `controller` (`/api/movies`) → `service` → `repository` (Spring Data JPA) + `kafka`.

- `MovieService` is where writes happen: each `@Transactional` create/update/delete saves via JPA and then publishes a `MovieEvent` (`CREATED`/`UPDATED`/`DELETED`, built through static factories on `MovieEvent`) through `MovieEventProducer`.
- `MovieEventProducer` sends to the topic from property `kafka.topic.movie-events` (`movie-events`), keyed by movie ID, JSON-serialized via Spring Kafka's `JsonSerializer`.
- Schema is owned by **Liquibase** (`src/main/resources/db/changelog/`, master file includes numbered change files); `spring.jpa.hibernate.ddl-auto=none`, so schema changes must be a new changeset added to `db.changelog-master.yaml`, not entity edits alone.

## Gotchas

- `README.md` is partly stale: it mentions Spring Boot 3.2 / Java 17, `docker-compose.yml`, Zookeeper, `moviedb`/`movieuser` credentials and `movie-postgres`/`movie-kafka` container names. Actual values: `compose.yml` with `postgres:15.3` and `apache/kafka` (KRaft, no Zookeeper), DB `mydb`, user `myuser` / `secret` (see `application.properties`); compose services have no fixed container names.
- loosejar artifacts (`loosejar*`, `ext/`, CSV output) are local experiment output. Only `loosejar*` and `*.jar` are gitignored; `ext/`, `loose.csv` and `test.csv` are not, so don't commit them by accident.
- Lombok is used as an annotation processor and is excluded from the repackaged jar via `spring-boot-maven-plugin` `<excludes>`. Keep that in mind when comparing jar or layer contents.
- `compose.yml` uses the unpinned `apache/kafka:latest`, so the Kafka version can drift between runs.
