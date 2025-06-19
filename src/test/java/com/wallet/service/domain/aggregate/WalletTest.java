package com.wallet.service.domain.aggregate;

import com.wallet.service.domain.event.DomainEvent;
import com.wallet.service.domain.event.MoneyDepositedEvent;
import com.wallet.service.domain.event.WalletCreatedEvent;
import com.wallet.service.domain.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void testCreateWallet() {
        // Given
        UUID userId = UUID.randomUUID();

        // When
        Wallet wallet = new Wallet(userId);

        // Then
        assertNotNull(wallet.getId());
        assertEquals(userId, wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
        assertEquals(1, wallet.getVersion());
        assertEquals(1, wallet.getUncommittedEvents().size());
        
        DomainEvent event = wallet.getUncommittedEvents().get(0);
        assertTrue(event instanceof WalletCreatedEvent);
        assertEquals("WALLET_CREATED", event.getEventType());
    }

    @Test
    void testDeposit() {
        // Given
        Wallet wallet = new Wallet(UUID.randomUUID());
        wallet.markEventsAsCommitted();
        BigDecimal depositAmount = new BigDecimal("100.00");

        // When
        wallet.deposit(depositAmount, "TX123");

        // Then
        assertEquals(depositAmount, wallet.getBalance());
        assertEquals(2, wallet.getVersion());
        assertEquals(1, wallet.getUncommittedEvents().size());
        
        DomainEvent event = wallet.getUncommittedEvents().get(0);
        assertTrue(event instanceof MoneyDepositedEvent);
        assertEquals("MONEY_DEPOSITED", event.getEventType());
    }

    @Test
    void testWithdrawWithSufficientFunds() {
        // Given
        Wallet wallet = new Wallet(UUID.randomUUID());
        wallet.markEventsAsCommitted();
        wallet.deposit(new BigDecimal("100.00"), "TX123");
        wallet.markEventsAsCommitted();

        // When
        wallet.withdraw(new BigDecimal("50.00"), "TX124");

        // Then
        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
        assertEquals(3, wallet.getVersion());
    }

    @Test
    void testWithdrawWithInsufficientFunds() {
        // Given
        Wallet wallet = new Wallet(UUID.randomUUID());
        wallet.markEventsAsCommitted();

        // When/Then
        assertThrows(InsufficientFundsException.class, () -> {
            wallet.withdraw(new BigDecimal("50.00"), "TX124");
        });
    }

    @Test
    void testInvalidDepositAmount() {
        // Given
        Wallet wallet = new Wallet(UUID.randomUUID());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.deposit(new BigDecimal("-10.00"), "TX123");
        });
    }
} 