package com.wallet.service.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferCommand {
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
} 