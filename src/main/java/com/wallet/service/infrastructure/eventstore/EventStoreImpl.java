package com.wallet.service.infrastructure.eventstore;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.service.domain.event.DomainEvent;
import com.wallet.service.domain.event.MoneyDepositedEvent;
import com.wallet.service.domain.event.MoneyTransferredEvent;
import com.wallet.service.domain.event.MoneyWithdrawnEvent;
import com.wallet.service.domain.event.WalletCreatedEvent;
import com.wallet.service.infrastructure.persistence.EventEntity;
import com.wallet.service.infrastructure.persistence.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreImpl implements EventStore {
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void save(List<DomainEvent> events, String aggregateType) {
        List<EventEntity> entities = events.stream()
                .map(event -> EventEntity.builder()
                        .aggregateId(event.getAggregateId())
                        .aggregateType(aggregateType)
                        .eventType(event.getEventType())
                        .version(event.getVersion())
                        .eventData(serialize(event))
                        .occurredAt(event.getOccurredAt())
                        .createdAt(Instant.now())
                        .build())
                .collect(Collectors.toList());

        eventRepository.saveAll(entities);
        log.info("Saved {} events for aggregate type {}", events.size(), aggregateType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> getEvents(UUID aggregateId) {
        return eventRepository.findByAggregateIdOrderByVersionAsc(aggregateId)
                .stream()
                .map(this::deserialize)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> getEventsUntil(UUID aggregateId, Instant timestamp) {
        return eventRepository.findByAggregateIdAndOccurredAtLessThanEqualOrderByVersionAsc(aggregateId, timestamp)
                .stream()
                .map(this::deserialize)
                .collect(Collectors.toList());
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private DomainEvent deserialize(EventEntity entity) {
        try {
            Class<?> eventClass = getEventClass(entity.getEventType());
            return (DomainEvent) objectMapper.readValue(entity.getEventData(), eventClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }

    private Class<?> getEventClass(String eventType) {
        switch (eventType) {
            case "WALLET_CREATED" -> {
                return WalletCreatedEvent.class;
            }
            case "MONEY_DEPOSITED" -> {
                return MoneyDepositedEvent.class;
            }
            case "MONEY_WITHDRAWN" -> {
                return MoneyWithdrawnEvent.class;
            }
            case "MONEY_TRANSFERRED_SENT", "MONEY_TRANSFERRED_RECEIVED" -> {
                return MoneyTransferredEvent.class;
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
} 