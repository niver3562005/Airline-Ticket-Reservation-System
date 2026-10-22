package com.aeronest.repository;

import com.aeronest.domain.AccountToken;
import com.aeronest.domain.DomainEnums.AccountTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    Optional<AccountToken> findByTokenAndType(String token, AccountTokenType type);
}
