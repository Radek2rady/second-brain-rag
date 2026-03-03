# Architecture

This project follows **Hexagonal Architecture** (also known as Ports and Adapters) principles.

## Package Structure

- **domain**: Contains the core business logic, entities, and repository interfaces (Ports). This layer has no dependencies on any external frameworks or technologies (not even Spring).
- **application**: Contains use cases/services that orchestrate domain objects to fulfill business requirements. It acts as the API for the application core.
- **infrastructure**: Contains the implementation of the adapters. This includes database repositories (Spring Data JPA implementations mapping to domain ports), REST controllers, external client integrations, and Spring configuration.
