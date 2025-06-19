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
public class MoneyWithdrawnEvent implements DomainEvent {
    private UUID aggregateId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String transactionId;
    private Instant occurredAt;
    private int version;

    @Override
    public String getEventType() {
        return "MONEY_WITHDRAWN";
    }
} 