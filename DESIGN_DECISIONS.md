# Design Decisions and Architecture

## Overview

This document explains the key design decisions made in implementing the Wallet Service, focusing on how CQRS and Event Sourcing patterns address the functional and non-functional requirements.

## Architecture Pattern: CQRS with Event Sourcing

### Why CQRS?

1. **Separation of Concerns**: Commands (writes) and Queries (reads) have different performance characteristics and scalability requirements
2. **Optimized Read Models**: Wallet balance queries can be served from optimized projections
3. **Future Scalability**: Read and write sides can be scaled independently

### Why Event Sourcing?

1. **Audit Trail**: Every state change is recorded as an immutable event (critical for financial systems)
2. **Historical Queries**: Can reconstruct wallet state at any point in time
3. **Debugging**: Complete history of what happened and when
4. **Event-Driven Architecture**: Foundation for future event-driven integrations

## Key Design Decisions

### 1. Domain Model

**Decision**: Use Domain-Driven Design with Aggregate Roots

**Rationale**:
- `Wallet` is the aggregate root that ensures consistency
- All business rules are enforced within the aggregate
- Events are the only way to change state

**Implementation**:
```java
public class Wallet {
    // State is private and can only be changed through domain events
    private UUID id;
    private BigDecimal balance;
    
    // Business operations generate events
    public void deposit(BigDecimal amount) {
        // Validation
        // Create event
        // Apply event
    }
}
```

### 2. Event Store Design

**Decision**: Simple JPA-based event store with JSON serialization

**Rationale**:
- Sufficient for the assignment scope
- Easy to implement and test
- Can be replaced with dedicated event store (EventStore, Kafka) in production

**Trade-offs**:
- Not optimized for high-volume scenarios
- Limited querying capabilities compared to specialized event stores

### 3. Projection Strategy

**Decision**: Synchronous projection updates

**Rationale**:
- Ensures read-after-write consistency
- Simpler implementation for the assignment
- Sufficient for moderate load

**Production Alternative**:
- Asynchronous projections with eventual consistency
- Separate read database
- Event streaming for real-time updates

### 4. Transaction Handling

**Decision**: Single database transaction for both event store and projections

**Rationale**:
- Ensures consistency between write and read models
- Prevents partial updates
- Simpler error handling

**Implementation**:
```java
@Transactional
public void save(Wallet wallet) {
    // Save events
    eventStore.save(wallet.getUncommittedEvents());
    // Update projection
    updateProjection(wallet);
    // Mark events as committed
    wallet.markEventsAsCommitted();
}
```

### 5. Transfer Implementation

**Decision**: Two-phase transfer with separate events for each wallet

**Rationale**:
- Each wallet maintains its own event stream
- Ensures atomic updates within each aggregate
- Clear audit trail for both sender and receiver

**Alternative Considered**: Saga pattern for distributed transactions
- Would be necessary in a microservices architecture
- Overkill for single service implementation

### 6. API Design

**Decision**: RESTful API with separate endpoints for commands and queries

**Rationale**:
- Clear separation following CQRS principles
- Intuitive for API consumers
- Easy to implement different security policies

**Examples**:
- Commands: `POST /api/wallets/{id}/deposit`
- Queries: `GET /api/wallets/{id}`

### 7. Error Handling Strategy

**Decision**: Domain exceptions with global exception handler

**Rationale**:
- Consistent error responses
- Clear separation between domain and technical errors
- Easy to extend with new exception types

### 8. Historical Balance Implementation

**Decision**: Event replay up to specified timestamp

**Rationale**:
- True event sourcing approach
- Accurate historical state reconstruction
- No need to store snapshots for this scale

**Performance Consideration**:
- For large event streams, would implement snapshots
- Could cache historical queries

## Non-Functional Requirements Implementation

### 1. High Availability

**Addressed by**:
- Stateless service design
- Can run multiple instances
- Database clustering for persistence layer

### 2. Audit Trail

**Addressed by**:
- Every state change stored as immutable event
- Events include timestamp, version, and full details
- Can reconstruct complete history

### 3. Performance

**Current Implementation**:
- Projections for fast current balance queries
- Indexed event store for efficient retrieval

**Future Optimizations**:
- Event snapshots for historical queries
- Caching layer for frequently accessed wallets
- Read replicas for query scaling

## Compromises and Limitations

### 1. Synchronous Processing

**Compromise**: All operations are synchronous
**Reason**: Simplicity for the assignment
**Production Solution**: Async command processing with response queues

### 2. In-Memory Database

**Compromise**: Using H2 instead of production database
**Reason**: Easy setup and testing
**Production Solution**: PostgreSQL with proper indexes and partitioning

### 3. Single Service

**Compromise**: Monolithic implementation
**Reason**: Assignment scope
**Production Solution**: Separate services for different bounded contexts

### 4. No Authentication

**Compromise**: No security layer
**Reason**: Focus on core functionality
**Production Solution**: OAuth2/JWT with proper authorization

### 5. Limited Validation

**Compromise**: Basic validation only
**Reason**: Time constraints
**Production Solution**: Comprehensive validation including:
- Currency support
- Transaction limits
- Fraud detection
- Idempotency checks

## Future Enhancements

1. **Event Versioning**: Handle event schema evolution
2. **Snapshots**: Periodic snapshots for performance
3. **Event Replay**: Admin tools for event replay and correction
4. **Monitoring**: Metrics for event processing and projections
5. **Multi-tenancy**: Support for multiple organizations
6. **Eventual Consistency**: Move to async projections with proper handling

## Conclusion

The chosen architecture provides a solid foundation for a financial service that requires:
- Complete audit trail
- Historical queries
- High reliability
- Future scalability

While some compromises were made for the assignment scope, the design allows for gradual evolution toward a production-ready system without major architectural changes. 