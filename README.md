# snello-api-quarkus3

## Project Overview

Snello is a headless CMS designed to expose data structures and REST APIs without writing application code.

The project was originally created in Java using Micronaut in 2019 and has been rewritten in Quarkus in recent years.

Its core idea is metadata-driven development: you define metadata and Snello can create database tables and serve REST queries automatically.

Project TODO and BUG notes are tracked in `TODO.md`.

## Development Setup (Docker Compose Dev Services)

In development mode, the application is configured to use Quarkus Compose Dev Services.
When you run the app in dev mode, Docker Compose is used to start local dependencies automatically.

### Services started in DEV

- PostgreSQL
- MinIO
- Keycloak

Compose definition:

- `compose-devservices.yml`

Keycloak realm imports:

- `dev/keycloak/realm-snello.json`
- `dev/keycloak/realm-lovers.json`

### OIDC realms configured for DEV

- `snello` realm
	- client: `snello-api`
- `lovers` realm
	- client: `accounting`

### Quick start

```bash
./mvnw quarkus:dev
```

To stop all local DEV services:

```bash
docker compose -f compose-devservices.yml down
```