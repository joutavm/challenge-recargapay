# Wallet Service

A microservice for managing user wallets with support for deposits, withdrawals, and transfers. Built using Spring Boot 3.5, CQRS pattern, and Event Sourcing architecture.

## Architecture Overview

This service implements:
- **CQRS (Command Query Responsibility Segregation)**: Separates write and read operations for better scalability and performance
- **Event Sourcing**: All state changes are stored as events, providing complete audit trail and historical balance queries
- **Domain-Driven Design**: Clean separation of concerns with aggregate roots and domain events

## Technical Stack

- Java 17
- Spring Boot 3.3.0
- Gradle
- H2 Database (in-memory for demo, easily replaceable with PostgreSQL/MySQL)
- JPA/Hibernate
- Lombok
- Jackson for JSON serialization

## Features

- Create wallets for users
- Deposit funds
- Withdraw funds
- Transfer funds between wallets
- Query current balance
- Query historical balance at any point in time
- Complete audit trail of all operations

## Getting Started

### Prerequisites

- Java 17 or higher
- Gradle (optional, project includes Gradle wrapper)

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd wallet-service
```

2. Build the project:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew bootRun
```

The service will start on `http://localhost:8080`

### H2 Console

The H2 database console is available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:walletdb`
- Username: `sa`
- Password: (leave empty)

## API Documentation

### Create Wallet

Creates a new wallet for a user.

```http
POST /api/wallets
Content-Type: application/json

{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

Response:
```json
{
  "walletId": "456e7890-e89b-12d3-a456-426614174000"
}
```

### Get Wallet Balance

Retrieves the current balance of a wallet.

```http
GET /api/wallets/{walletId}
```

Response:
```json
{
  "id": "456e7890-e89b-12d3-a456-426614174000",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "balance": 100.00,
  "version": 3,
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

### Get Wallet by User ID

```http
GET /api/wallets/user/{userId}
```

### Get Historical Balance

Retrieves the balance at a specific point in time.

```http
GET /api/wallets/{walletId}/history?timestamp=2024-01-15T10:30:00Z
```

### Deposit Funds

```http
POST /api/wallets/{walletId}/deposit
Content-Type: application/json

{
  "amount": 50.00
}
```

### Withdraw Funds

```http
POST /api/wallets/{walletId}/withdraw
Content-Type: application/json

{
  "amount": 25.00
}
```

### Transfer Funds

```http
POST /api/wallets/transfer
Content-Type: application/json

{
  "fromWalletId": "456e7890-e89b-12d3-a456-426614174000",
  "toWalletId": "789e0123-e89b-12d3-a456-426614174000",
  "amount": 30.00
}
```

## Design Decisions

### CQRS Implementation

- **Commands**: All write operations (create, deposit, withdraw, transfer) are handled through command handlers
- **Queries**: Read operations use optimized projections for better performance
- **Separation**: Clear boundary between write and read models

### Event Sourcing

- All state changes are stored as immutable events
- Events are the source of truth
- Wallet state is reconstructed by replaying events
- Provides complete audit trail and ability to query historical states

### Database Design

Two main tables:
1. **events**: Stores all domain events with full details
2. **wallet_projections**: Optimized read model for current wallet state

### Error Handling

- Comprehensive exception handling with meaningful error messages
- Validation at API level using Bean Validation
- Domain validation in aggregate roots
- Global exception handler for consistent error responses

### Transaction Management

- All commands are wrapped in transactions
- Transfer operations ensure atomicity across both wallets
- Event store and projections are updated within the same transaction

## Trade-offs and Compromises

1. **In-Memory Database**: Used H2 for simplicity. In production, would use PostgreSQL or similar
2. **Simple Event Store**: Basic implementation suitable for demo. Production would use dedicated event store
3. **No Authentication**: Authentication/authorization would be added in production
4. **Synchronous Processing**: All operations are synchronous. Could add async event processing for scalability
5. **Limited Testing**: Due to time constraints, only basic structure is provided

## Future Enhancements

1. Add comprehensive test coverage
2. Implement event replay and snapshots for performance
3. Add authentication and authorization
4. Implement rate limiting
5. Add monitoring and metrics
6. Implement distributed transaction handling
7. Add API versioning
8. Implement event-driven notifications

## Running Tests

```bash
./gradlew test
```

## Health Check

The service exposes health endpoints:

```http
GET /actuator/health
```

## Time Investment

This project was completed in approximately 7 hours, focusing on:
- Architecture design and setup: 2 hours
- Core domain implementation: 2 hours
- CQRS and Event Sourcing: 2 hours
- REST API and error handling: 1 hour 