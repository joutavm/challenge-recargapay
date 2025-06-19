package com.wallet.service.application.handler;

import com.wallet.service.application.query.WalletDto;
import com.wallet.service.domain.aggregate.Wallet;
import com.wallet.service.infrastructure.persistence.WalletProjection;
import com.wallet.service.infrastructure.persistence.WalletProjectionRepository;
import com.wallet.service.infrastructure.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletQueryHandler {
    private final WalletRepository walletRepository;
    private final WalletProjectionRepository walletProjectionRepository;

    @Transactional(readOnly = true)
    public WalletDto getWallet(UUID walletId) {
        WalletProjection projection = walletProjectionRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        return WalletDto.builder()
                .id(projection.getId())
                .userId(projection.getUserId())
                .balance(projection.getBalance())
                .version(projection.getVersion())
                .lastUpdated(projection.getLastUpdated())
                .build();
    }

    @Transactional(readOnly = true)
    public WalletDto getWalletByUserId(UUID userId) {
        WalletProjection projection = walletProjectionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        return WalletDto.builder()
                .id(projection.getId())
                .userId(projection.getUserId())
                .balance(projection.getBalance())
                .version(projection.getVersion())
                .lastUpdated(projection.getLastUpdated())
                .build();
    }

    @Transactional(readOnly = true)
    public WalletDto getWalletAtTime(UUID walletId, Instant timestamp) {
        Wallet wallet = walletRepository.findByIdAtTime(walletId, timestamp)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        return WalletDto.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .version(wallet.getVersion())
                .lastUpdated(timestamp)
                .build();
    }
} 