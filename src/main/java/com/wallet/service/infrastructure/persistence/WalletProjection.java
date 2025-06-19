package com.wallet.service.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_projections", indexes = {
    @Index(name = "idx_user_id", columnList = "userId", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletProjection {
    @Id
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private UUID userId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private int version;
    
    @Column(nullable = false)
    private Instant lastUpdated;
} 