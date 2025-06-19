package com.wallet.service.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getAggregateId();
    Instant getOccurredAt();
    String getEventType();
    int getVersion();
} 