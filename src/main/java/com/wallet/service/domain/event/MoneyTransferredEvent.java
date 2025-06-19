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
public class MoneyTransferredEvent implements DomainEvent {
    private UUID aggregateId;
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String transactionId;
    private TransferType transferType; // SENT or RECEIVED
    private Instant occurredAt;
    private int version;

    public enum TransferType {
        SENT, RECEIVED
    }

    @Override
    public String getEventType() {
        return "MONEY_TRANSFERRED_" + transferType;
    }
} 