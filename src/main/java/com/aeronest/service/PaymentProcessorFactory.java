package com.aeronest.service;

import com.aeronest.domain.DomainEnums.PaymentMethod;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProcessorFactory {
    private final Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);
    public PaymentProcessorFactory(List<PaymentProcessor> available) { available.forEach(p -> processors.put(p.supports(), p)); }
    public PaymentProcessor forMethod(PaymentMethod method) {
        PaymentProcessor processor = processors.get(method);
        if (processor == null) throw new BusinessRuleException("Unsupported payment method");
        return processor;
    }
}
