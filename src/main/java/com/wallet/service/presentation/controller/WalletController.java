package com.wallet.service.presentation.controller;

import com.wallet.service.application.command.*;
import com.wallet.service.application.handler.WalletCommandHandler;
import com.wallet.service.application.handler.WalletQueryHandler;
import com.wallet.service.application.query.WalletDto;
import com.wallet.service.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletCommandHandler commandHandler;
    private final WalletQueryHandler queryHandler;

    @PostMapping
    public ResponseEntity<CreateWalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        UUID walletId = commandHandler.handle(new CreateWalletCommand(request.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateWalletResponse(walletId));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletDto> getWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(queryHandler.getWallet(walletId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletDto> getWalletByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(queryHandler.getWalletByUserId(userId));
    }

    @GetMapping("/{walletId}/history")
    public ResponseEntity<WalletDto> getWalletAtTime(
            @PathVariable UUID walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp) {
        return ResponseEntity.ok(queryHandler.getWalletAtTime(walletId, timestamp));
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody TransactionRequest request) {
        commandHandler.handle(new DepositCommand(walletId, request.getAmount()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody TransactionRequest request) {
        commandHandler.handle(new WithdrawCommand(walletId, request.getAmount()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        commandHandler.handle(new TransferCommand(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount()
        ));
        return ResponseEntity.noContent().build();
    }
} 