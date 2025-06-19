package com.wallet.service.domain.aggregate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.wallet.service.domain.event.DomainEvent;
import com.wallet.service.domain.event.MoneyDepositedEvent;
import com.wallet.service.domain.event.MoneyTransferredEvent;
import com.wallet.service.domain.event.MoneyWithdrawnEvent;
import com.wallet.service.domain.event.WalletCreatedEvent;
import com.wallet.service.domain.exception.InsufficientFundsException;

import lombok.Getter;

@Getter
public final class Wallet {
    private UUID id;
    private UUID userId;
    private BigDecimal balance;
    private int version;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    // For creating a new wallet
    public Wallet(UUID userId) {
        WalletCreatedEvent event = WalletCreatedEvent.builder()
                .aggregateId(UUID.randomUUID())
                .userId(userId)
                .initialBalance(BigDecimal.ZERO)
                .occurredAt(Instant.now())
                .version(1)
                .build();
        
        apply(event);
        uncommittedEvents.add(event);
    }

    // For event sourcing reconstruction
    public Wallet() {
        this.balance = BigDecimal.ZERO;
        this.version = 0;
    }

    public void deposit(BigDecimal amount, String transactionId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        BigDecimal newBalance = balance.add(amount);
        
        MoneyDepositedEvent event = MoneyDepositedEvent.builder()
                .aggregateId(id)
                .amount(amount)
                .balanceAfter(newBalance)
                .transactionId(transactionId)
                .occurredAt(Instant.now())
                .version(version + 1)
                .build();

        apply(event);
        uncommittedEvents.add(event);
    }

    public void withdraw(BigDecimal amount, String transactionId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        BigDecimal newBalance = balance.subtract(amount);
        
        MoneyWithdrawnEvent event = MoneyWithdrawnEvent.builder()
                .aggregateId(id)
                .amount(amount)
                .balanceAfter(newBalance)
                .transactionId(transactionId)
                .occurredAt(Instant.now())
                .version(version + 1)
                .build();

        apply(event);
        uncommittedEvents.add(event);
    }

    public void transferOut(UUID toWalletId, BigDecimal amount, String transactionId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        BigDecimal newBalance = balance.subtract(amount);
        
        MoneyTransferredEvent event = MoneyTransferredEvent.builder()
                .aggregateId(id)
                .fromWalletId(id)
                .toWalletId(toWalletId)
                .amount(amount)
                .balanceAfter(newBalance)
                .transactionId(transactionId)
                .transferType(MoneyTransferredEvent.TransferType.SENT)
                .occurredAt(Instant.now())
                .version(version + 1)
                .build();

        apply(event);
        uncommittedEvents.add(event);
    }

    public void transferIn(UUID fromWalletId, BigDecimal amount, String transactionId) {
        BigDecimal newBalance = balance.add(amount);
        
        MoneyTransferredEvent event = MoneyTransferredEvent.builder()
                .aggregateId(id)
                .fromWalletId(fromWalletId)
                .toWalletId(id)
                .amount(amount)
                .balanceAfter(newBalance)
                .transactionId(transactionId)
                .transferType(MoneyTransferredEvent.TransferType.RECEIVED)
                .occurredAt(Instant.now())
                .version(version + 1)
                .build();

        apply(event);
        uncommittedEvents.add(event);
    }

    public void apply(DomainEvent event) {
        switch (event) {
            case WalletCreatedEvent e -> handle(e);
            case MoneyDepositedEvent e -> handle(e);
            case MoneyWithdrawnEvent e -> handle(e);
            case MoneyTransferredEvent e -> handle(e);
            default -> {
            }
        }
    }

    private void handle(WalletCreatedEvent event) {
        this.id = event.getAggregateId();
        this.userId = event.getUserId();
        this.balance = event.getInitialBalance();
        this.version = event.getVersion();
    }

    private void handle(MoneyDepositedEvent event) {
        this.balance = event.getBalanceAfter();
        this.version = event.getVersion();
    }

    private void handle(MoneyWithdrawnEvent event) {
        this.balance = event.getBalanceAfter();
        this.version = event.getVersion();
    }

    private void handle(MoneyTransferredEvent event) {
        this.balance = event.getBalanceAfter();
        this.version = event.getVersion();
    }

    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
} 