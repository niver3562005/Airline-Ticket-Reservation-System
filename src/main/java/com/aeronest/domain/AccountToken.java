package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.AccountTokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_tokens")
public class AccountToken extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String token;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(nullable = false)
    private CustomerAccount account;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private AccountTokenType type;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public boolean isUsable() { return usedAt == null && expiresAt.isAfter(LocalDateTime.now()); }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public CustomerAccount getAccount() { return account; }
    public void setAccount(CustomerAccount account) { this.account = account; }
    public AccountTokenType getType() { return type; }
    public void setType(AccountTokenType type) { this.type = type; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
