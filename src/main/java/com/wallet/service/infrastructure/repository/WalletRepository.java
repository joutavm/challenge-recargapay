package com.wallet.service.infrastructure.repository;

import com.wallet.service.domain.aggregate.Wallet;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    void save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByIdAtTime(UUID id, Instant timestamp);
    Optional<Wallet> findByUserId(UUID userId);
} 