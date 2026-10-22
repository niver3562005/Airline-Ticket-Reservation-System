package com.aeronest.service;

import com.aeronest.repository.CustomerAccountRepository;
import com.aeronest.web.PasswordChangeForm;
import com.aeronest.web.PasswordResetForm;
import com.aeronest.web.ProfileForm;
import com.aeronest.web.RegistrationForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AccountServiceIntegrationTest {
    @Autowired AccountService service;
    @Autowired CustomerAccountRepository accounts;
    @Autowired PasswordEncoder encoder;

    @Test void registrationHashesPasswordAndRequiresEmailVerification() {
        RegistrationForm form = registration("new.passenger@example.com");
        String token = service.register(form);
        var account = accounts.findByEmailIgnoreCase(form.getEmail()).orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo(form.getPassword());
        assertThat(encoder.matches(form.getPassword(), account.getPasswordHash())).isTrue();
        assertThat(account.isEmailVerified()).isFalse();
        service.verifyEmail(token);
        assertThat(account.isEmailVerified()).isTrue();
    }

    @Test void duplicateEmailIsRejectedCaseInsensitively() {
        service.register(registration("unique@example.com"));
        assertThatThrownBy(() -> service.register(registration("UNIQUE@example.com")))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("already exists");
    }

    @Test void resetTokenChangesPasswordAndCannotBeReused() {
        String email = "reset@example.com"; service.register(registration(email));
        String token = service.requestPasswordReset(email).orElseThrow();
        PasswordResetForm reset = new PasswordResetForm(); reset.setToken(token); reset.setPassword("NewTrip@456"); reset.setConfirmPassword("NewTrip@456");
        service.resetPassword(reset);
        assertThat(encoder.matches("NewTrip@456", accounts.findByEmailIgnoreCase(email).orElseThrow().getPasswordHash())).isTrue();
        assertThatThrownBy(() -> service.resetPassword(reset)).isInstanceOf(BusinessRuleException.class).hasMessageContaining("expired");
    }

    @Test void profileAndPasswordCanBeUpdatedSecurely() {
        String email = "profile@example.com"; String verify = service.register(registration(email)); service.verifyEmail(verify);
        ProfileForm profile = new ProfileForm(); profile.setFirstName("Maya"); profile.setLastName("Fernando"); profile.setPhone("+94 77 100 2000"); profile.setMarketingOptIn(true);
        service.updateProfile(email, profile);
        PasswordChangeForm password = new PasswordChangeForm(); password.setCurrentPassword("Travel@123"); password.setNewPassword("Secure@789"); password.setConfirmPassword("Secure@789");
        service.changePassword(email, password);
        var account = accounts.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(account.getFullName()).isEqualTo("Maya Fernando"); assertThat(account.isMarketingOptIn()).isTrue();
        assertThat(encoder.matches("Secure@789", account.getPasswordHash())).isTrue();
    }

    private RegistrationForm registration(String email) {
        RegistrationForm form = new RegistrationForm(); form.setFirstName("Nivetha"); form.setLastName("Silva");
        form.setEmail(email); form.setPhone("+94 77 123 4567"); form.setPassword("Travel@123");
        form.setConfirmPassword("Travel@123"); form.setTermsAccepted(true); return form;
    }
}
