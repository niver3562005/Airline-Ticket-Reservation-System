package com.aeronest.service;

import com.aeronest.domain.AccountToken;
import com.aeronest.domain.Booking;
import com.aeronest.domain.CustomerAccount;
import com.aeronest.domain.DomainEnums.AccountTokenType;
import com.aeronest.repository.AccountTokenRepository;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.CustomerAccountRepository;
import com.aeronest.web.PasswordChangeForm;
import com.aeronest.web.PasswordResetForm;
import com.aeronest.web.ProfileForm;
import com.aeronest.web.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountService {
    private final CustomerAccountRepository accounts;
    private final AccountTokenRepository tokens;
    private final BookingRepository bookings;
    private final PasswordEncoder encoder;

    public AccountService(CustomerAccountRepository accounts, AccountTokenRepository tokens,
                          BookingRepository bookings, PasswordEncoder encoder) {
        this.accounts = accounts; this.tokens = tokens; this.bookings = bookings; this.encoder = encoder;
    }

    @Transactional
    public String register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (accounts.existsByEmailIgnoreCase(email)) throw new BusinessRuleException("An account already exists for this email address");
        CustomerAccount account = new CustomerAccount();
        account.setFirstName(form.getFirstName().trim()); account.setLastName(form.getLastName().trim());
        account.setEmail(email); account.setPhone(form.getPhone().trim()); account.setPasswordHash(encoder.encode(form.getPassword()));
        account.setLoyaltyNumber(nextLoyaltyNumber()); account.setMarketingOptIn(form.isMarketingOptIn()); account.setEmailVerified(false);
        accounts.save(account);
        return createToken(account, AccountTokenType.EMAIL_VERIFICATION, 24).getToken();
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        AccountToken token = usableToken(rawToken, AccountTokenType.EMAIL_VERIFICATION);
        token.getAccount().setEmailVerified(true); token.setUsedAt(LocalDateTime.now());
    }

    @Transactional
    public Optional<String> requestPasswordReset(String email) {
        return accounts.findByEmailIgnoreCase(email.trim()).map(account -> createToken(account, AccountTokenType.PASSWORD_RESET, 1).getToken());
    }

    public boolean isResetTokenValid(String rawToken) {
        return tokens.findByTokenAndType(rawToken, AccountTokenType.PASSWORD_RESET).filter(AccountToken::isUsable).isPresent();
    }

    @Transactional
    public void resetPassword(PasswordResetForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) throw new BusinessRuleException("Passwords must match");
        AccountToken token = usableToken(form.getToken(), AccountTokenType.PASSWORD_RESET);
        token.getAccount().setPasswordHash(encoder.encode(form.getPassword())); token.setUsedAt(LocalDateTime.now());
    }

    public CustomerAccount accountFor(String email) {
        return accounts.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("Passenger account not found"));
    }

    public List<Booking> bookingHistory(String email) { return bookings.findByContactEmailIgnoreCaseOrderByCreatedAtDesc(email); }

    public ProfileForm profileForm(CustomerAccount account) {
        ProfileForm form = new ProfileForm(); form.setFirstName(account.getFirstName()); form.setLastName(account.getLastName());
        form.setPhone(account.getPhone()); form.setMarketingOptIn(account.isMarketingOptIn()); return form;
    }

    @Transactional
    public void updateProfile(String email, ProfileForm form) {
        CustomerAccount account = accountFor(email); account.setFirstName(form.getFirstName().trim());
        account.setLastName(form.getLastName().trim()); account.setPhone(form.getPhone().trim()); account.setMarketingOptIn(form.isMarketingOptIn());
    }

    @Transactional
    public void changePassword(String email, PasswordChangeForm form) {
        CustomerAccount account = accountFor(email);
        if (!encoder.matches(form.getCurrentPassword(), account.getPasswordHash())) throw new BusinessRuleException("Current password is incorrect");
        if (!form.getNewPassword().equals(form.getConfirmPassword())) throw new BusinessRuleException("New passwords must match");
        account.setPasswordHash(encoder.encode(form.getNewPassword()));
    }

    private AccountToken createToken(CustomerAccount account, AccountTokenType type, int hours) {
        AccountToken token = new AccountToken(); token.setAccount(account); token.setType(type);
        token.setToken(UUID.randomUUID().toString().replace("-", "")); token.setExpiresAt(LocalDateTime.now().plusHours(hours));
        return tokens.save(token);
    }

    private AccountToken usableToken(String rawToken, AccountTokenType type) {
        return tokens.findByTokenAndType(rawToken, type).filter(AccountToken::isUsable)
                .orElseThrow(() -> new BusinessRuleException("This secure link is invalid or has expired"));
    }

    private String nextLoyaltyNumber() {
        String value;
        do { value = "AV" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT); }
        while (accounts.existsByLoyaltyNumber(value));
        return value;
    }
}
