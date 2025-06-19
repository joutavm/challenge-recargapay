package com.wallet.service.infrastructure.repository;

import com.wallet.service.domain.aggregate.Wallet;
import com.wallet.service.domain.event.DomainEvent;
import com.wallet.service.domain.event.WalletCreatedEvent;
import com.wallet.service.infrastructure.eventstore.EventStore;
import com.wallet.service.infrastructure.persistence.WalletProjection;
import com.wallet.service.infrastructure.persistence.WalletProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WalletRepositoryImpl implements WalletRepository {
    private final EventStore eventStore;
    private final WalletProjectionRepository walletProjectionRepository;

    @Override
    @Transactional
    public void save(Wallet wallet) {
        if (!wallet.getUncommittedEvents().isEmpty()) {
            eventStore.save(wallet.getUncommittedEvents(), "Wallet");
            
            // Update projection
            WalletProjection projection = walletProjectionRepository
                    .findById(wallet.getId())
                    .orElse(WalletProjection.builder()
                            .id(wallet.getId())
                            .userId(wallet.getUserId())
                            .build());
            
            projection.setBalance(wallet.getBalance());
            projection.setVersion(wallet.getVersion());
            projection.setLastUpdated(Instant.now());
            
            walletProjectionRepository.save(projection);
            
            wallet.markEventsAsCommitted();
            log.info("Saved wallet {} with balance {}", wallet.getId(), wallet.getBalance());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Wallet> findById(UUID id) {
        List<DomainEvent> events = eventStore.getEvents(id);
        if (events.isEmpty()) {
            return Optional.empty();
        }
        
        Wallet wallet = new Wallet();
        events.forEach(wallet::apply);
        return Optional.of(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Wallet> findByIdAtTime(UUID id, Instant timestamp) {
        List<DomainEvent> events = eventStore.getEventsUntil(id, timestamp);
        if (events.isEmpty()) {
            return Optional.empty();
        }
        
        Wallet wallet = new Wallet();
        events.forEach(wallet::apply);
        return Optional.of(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Wallet> findByUserId(UUID userId) {
        return walletProjectionRepository.findByUserId(userId)
                .flatMap(projection -> findById(projection.getId()));
    }
} 