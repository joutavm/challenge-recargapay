package com.wallet.service.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreatedEvent implements DomainEvent {
    private UUID aggregateId;
    private UUID userId;
    private BigDecimal initialBalance;
    private Instant occurredAt;
    private int version;

    @Override
    public String getEventType() {
        return "WALLET_CREATED";
    }
} 