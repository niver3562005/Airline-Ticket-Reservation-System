package com.aeronest.service;

import com.aeronest.domain.CustomerAccount;
import com.aeronest.repository.CustomerAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AeroVistaUserDetailsService implements UserDetailsService {
    private final CustomerAccountRepository accounts;
    private final String adminPassword;
    private final String agentPassword;

    public AeroVistaUserDetailsService(CustomerAccountRepository accounts, PasswordEncoder encoder) {
        this.accounts = accounts; this.adminPassword = encoder.encode("aeronest123"); this.agentPassword = encoder.encode("agent123");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("admin".equalsIgnoreCase(username)) return User.withUsername("admin").password(adminPassword).roles("ADMIN", "AGENT").build();
        if ("agent".equalsIgnoreCase(username)) return User.withUsername("agent").password(agentPassword).roles("AGENT").build();
        CustomerAccount account = accounts.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return User.withUsername(account.getEmail()).password(account.getPasswordHash()).roles("CUSTOMER")
                .disabled(!account.isEmailVerified()).build();
    }
}
