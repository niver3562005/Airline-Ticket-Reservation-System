package com.aeronest.service;

import com.aeronest.domain.DomainEnums.DeliveryStatus;
import com.aeronest.domain.DomainEnums.NotificationChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class NotificationDeliveryService {
    private final String emailWebhook;
    private final String smsWebhook;
    private final RestClient client = RestClient.create();

    public NotificationDeliveryService(@Value("${aeronest.notifications.email-webhook:}") String emailWebhook,
                                       @Value("${aeronest.notifications.sms-webhook:}") String smsWebhook) {
        this.emailWebhook = emailWebhook; this.smsWebhook = smsWebhook;
    }

    public DeliveryStatus deliver(NotificationChannel channel, String recipient, String title, String message) {
        if (channel == NotificationChannel.IN_APP) return DeliveryStatus.SENT;
        String endpoint = channel == NotificationChannel.EMAIL ? emailWebhook : smsWebhook;
        if (endpoint == null || endpoint.isBlank()) return DeliveryStatus.SIMULATED;
        try {
            client.post().uri(endpoint).body(Map.of("recipient", recipient, "title", title, "message", message,
                    "channel", channel.name())).retrieve().toBodilessEntity();
            return DeliveryStatus.SENT;
        } catch (RuntimeException ex) {
            return DeliveryStatus.FAILED;
        }
    }
}
