package com.aeronest.service;

import com.aeronest.domain.DomainEnums.PaymentMethod;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentProcessorFactoryTest {
    private final PaymentProcessorFactory factory = new PaymentProcessorFactory(List.of(
            new CardPaymentProcessor(), new BankTransferPaymentProcessor(), new CounterPaymentProcessor()));

    @Test void cardPaymentsAreImmediatelySettled() {
        var result = factory.forMethod(PaymentMethod.CARD).process(BigDecimal.TEN);
        assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.reference()).startsWith("CARD-");
    }
    @Test void counterPaymentsRemainPending() {
        var result = factory.forMethod(PaymentMethod.PAY_AT_COUNTER).process(BigDecimal.TEN);
        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.reference()).startsWith("HOLD-");
    }
}
