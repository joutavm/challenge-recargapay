package com.wallet.service.infrastructure.eventstore;

import com.wallet.service.domain.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventStore {
    void save(List<DomainEvent> events, String aggregateType);
    List<DomainEvent> getEvents(UUID aggregateId);
    List<DomainEvent> getEventsUntil(UUID aggregateId, Instant timestamp);
} 