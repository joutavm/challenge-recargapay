package com.wallet.service.application.handler;

import com.wallet.service.application.command.*;
import com.wallet.service.domain.aggregate.Wallet;
import com.wallet.service.infrastructure.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletCommandHandler {
    private final WalletRepository walletRepository;

    @Transactional
    public UUID handle(CreateWalletCommand command) {
        // Check if wallet already exists for user
        if (walletRepository.findByUserId(command.getUserId()).isPresent()) {
            throw new IllegalStateException("Wallet already exists for user: " + command.getUserId());
        }

        Wallet wallet = new Wallet(command.getUserId());
        walletRepository.save(wallet);
        
        log.info("Created wallet {} for user {}", wallet.getId(), command.getUserId());
        return wallet.getId();
    }

    @Transactional
    public void handle(DepositCommand command) {
        Wallet wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + command.getWalletId()));

        String transactionId = UUID.randomUUID().toString();
        wallet.deposit(command.getAmount(), transactionId);
        walletRepository.save(wallet);
        
        log.info("Deposited {} to wallet {}", command.getAmount(), command.getWalletId());
    }

    @Transactional
    public void handle(WithdrawCommand command) {
        Wallet wallet = walletRepository.findById(command.getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + command.getWalletId()));

        String transactionId = UUID.randomUUID().toString();
        wallet.withdraw(command.getAmount(), transactionId);
        walletRepository.save(wallet);
        
        log.info("Withdrew {} from wallet {}", command.getAmount(), command.getWalletId());
    }

    @Transactional
    public void handle(TransferCommand command) {
        if (command.getFromWalletId().equals(command.getToWalletId())) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        Wallet fromWallet = walletRepository.findById(command.getFromWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found: " + command.getFromWalletId()));
        
        Wallet toWallet = walletRepository.findById(command.getToWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found: " + command.getToWalletId()));

        String transactionId = UUID.randomUUID().toString();
        
        // Process transfer
        fromWallet.transferOut(command.getToWalletId(), command.getAmount(), transactionId);
        toWallet.transferIn(command.getFromWalletId(), command.getAmount(), transactionId);
        
        // Save both wallets
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        
        log.info("Transferred {} from wallet {} to wallet {}", 
                command.getAmount(), command.getFromWalletId(), command.getToWalletId());
    }
} 