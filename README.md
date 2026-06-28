# java-ddd-demo

> [中文文档](README_CN.md)

Spring Boot 3.2 + DDD six-module architecture demo, with two repository implementation styles compared side by side.

## Architecture

```
                      ┌──────────────────────────────────────────────────────┐
                      │                  demo-interface                       │
                      │   REST Controller / GlobalExceptionHandler / AOP     │
                      └──────────┬───────────────────────────────┬───────────┘
                                 │                               │
                          ┌──────▼──────┐                  ┌─────▼──────┐
                          │  demo-start  │                  │    DTO     │
                          │  Boot App    │                  │  Command   │
                          │  Config      │                  │  Query     │
                          └──────┬───────┘                  │  CO / Resp │
                                 │                          └─────┬──────┘
                                 │                                │
                      ┌──────────▼────────────────────────────────▼──────────┐
                      │                  demo-application                     │
                      │          AppService  (orchestration only)             │
                      │          Converter (DTO ↔ Domain)                    │
                      └──────────┬───────────────────────────────┬───────────┘
                                 │                               │
                    ┌────────────▼────────────┐      ┌───────────▼───────────┐
                    │     demo-domain          │      │   demo-infrastructure │
                    │                          │      │                       │
                    │  Aggregate Root          │      │  JPA Impl / PO        │
                    │  Value Object            │      │  MQ Producer           │
                    │  Domain Service          │◄─────│  External API Client   │
                    │  Repository Interface    │      │                       │
                    │  Domain Event            │      └───────────────────────┘
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     demo-common          │
                    │  BizException / ErrorCode│
                    │  @BizLog / DomainEvent   │
                    └──────────────────────────┘
```

### Dependency Direction

```
interface → application → domain ← infrastructure
                              ↑
                          common (shared by all)
```

## Quick Start

```bash
# 1. Start PostgreSQL & create database
createdb ddd_demo

# 2. Edit connection if needed
#    demo-start/src/main/resources/application.yml

# 3. Run
./mvnw spring-boot:run -pl demo-start

# 4. Test
./mvnw test
```

## Module Overview

| Module | Layer | Responsibility |
|--------|-------|----------------|
| `demo-common` | Shared | Exception / Event base / Log annotation |
| `demo-domain` | Domain | Aggregate / Value Object / Repository interface / Domain Service |
| `demo-application` | Application | AppService (orchestration) / DTO / Converter |
| `demo-infrastructure` | Infrastructure | JPA impl / MQ / External services |
| `demo-interface` | Interface | REST Controller / Global exception handler |
| `demo-start` | Bootstrap | Startup class / Config / DB schema |

## API Reference

### Skill (Style 1: PO/Domain separation)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/skills` | Create skill |
| GET | `/api/skills/{id}` | Get by ID |
| GET | `/api/skills` | Paged list |
| PUT | `/api/skills/{id}` | Update fields |
| PUT | `/api/skills/{id}/name` | Rename |
| PUT | `/api/skills/{id}/status` | Change status |
| POST | `/api/skills/{id}/activate` | Activate |
| POST | `/api/skills/{id}/archive` | Archive |
| POST | `/api/skills/batch-archive` | Batch archive |
| DELETE | `/api/skills/{id}` | Delete |

### Project

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/projects` | Create project |
| GET | `/api/projects/{id}` | Get by ID |
| GET | `/api/projects` | List all |
| PUT | `/api/projects/{id}` | Update |
| POST | `/api/projects/{id}/activate` | Activate |
| POST | `/api/projects/{id}/complete` | Complete |
| DELETE | `/api/projects/{id}` | Delete |

### Order

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/orders` | Create order |
| GET | `/api/orders/{id}` | Get by ID |
| POST | `/api/orders/{id}/pay` | Pay |
| DELETE | `/api/orders/{id}` | Delete |

## Repository Style Comparison

See details: [docs/ddd-repository-styles.md](docs/ddd-repository-styles.md)

| | Style 1 (Skill) | Style 2 (Skill2) |
|---|---|---|
| Domain Model | Pure POJO | @Entity on domain |
| PO Class | Yes | None |
| Converter | Yes (toDomain / toPO) | None |
| Repository Impl | Handwritten class | JpaRepository auto-gen |
| Precise Update | Native SQL | JPQL @Modifying |
| Extra Files | 6 | 0 |

## DDD Practices

| Practice | Description |
|----------|-------------|
| Rich Domain | Behavior methods inside aggregate root (`activate()` / `pay()`) |
| Factory Method | `register()` / `place()` / `initiate()` instead of new + setter |
| reconstitute() | Rebuild from persistence, bypassing business constructor |
| Domain Event | Emitted on state change, published after transaction commit |
| CQRS | Read repo returns lightweight projections; write repo operates aggregates |
| State Machine | Enum + switch guards valid transition paths |
