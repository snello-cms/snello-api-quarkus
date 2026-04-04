# Snello API (Quarkus)

Snello is a metadata-driven headless CMS API.

It lets you model entities, fields, and query behavior through metadata, then expose REST endpoints without writing one controller per business table.

## Why Snello

- Metadata-first API design
- Dynamic table and field management
- Generic REST layer for CRUD and query operations
- Pluggable document storage (filesystem, S3-compatible)
- Built-in support for derived media generation (formats and webp)

## Documentation for Final Users

Start here:

- [Documentation Index](docs/index.md)

Feature pages:

- [Metadata](docs/metadata.md)
- [Field Definitions](docs/field-definitions.md)
- [SelectQuery](docs/select-query.md)
- [Document Management](docs/document-management.md)

## Quick Start

Run the API in development mode:

```bash
./mvnw quarkus:dev
```

Stop local dev dependencies:

```bash
docker compose -f compose-devservices.yml down
```

## Local Development Stack

In development mode, Quarkus Compose Dev Services starts local dependencies automatically.

Services used in DEV:

- PostgreSQL
- MinIO
- Keycloak

Main compose file:

- compose-devservices.yml

Keycloak realm imports:

- dev/keycloak/realm-snello.json
- dev/keycloak/realm-lovers.json

OIDC realms configured in dev:

- snello realm -> client snello-api
- lovers realm -> client accounting

## Testing

Run all tests:

```bash
./mvnw test
```

Run one test class:

```bash
./mvnw -Dtest=io.snello.test.metadata.ProgramsMetadataCreationTest test
```

## Notes

- TODO and bug backlog is tracked in TODO.md.