package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.DeliveryStatus;
import com.aeronest.domain.DomainEnums.NotificationChannel;
import com.aeronest.domain.DomainEnums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class TravelNotification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) private CustomerAccount account;
    @ManyToOne(fetch = FetchType.LAZY) private Booking booking;
    @Column(nullable = false, length = 180) private String recipient;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private NotificationChannel channel;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private NotificationType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DeliveryStatus deliveryStatus;
    @Column(nullable = false, length = 140) private String title;
    @Column(nullable = false, length = 700) private String message;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    public boolean isRead() { return readAt != null; }
    public CustomerAccount getAccount() { return account; }
    public void setAccount(CustomerAccount account) { this.account = account; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
