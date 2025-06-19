package com.wallet.service.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletProjectionRepository extends JpaRepository<WalletProjection, UUID> {
    Optional<WalletProjection> findByUserId(UUID userId);
} 