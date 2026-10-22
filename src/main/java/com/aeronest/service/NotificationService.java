package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.CustomerAccount;
import com.aeronest.domain.Flight;
import com.aeronest.domain.TravelNotification;
import com.aeronest.domain.PartnerReservation;
import com.aeronest.domain.JourneyServiceRequest;
import com.aeronest.domain.DomainEnums.BookingStatus;
import com.aeronest.domain.DomainEnums.DeliveryStatus;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.domain.DomainEnums.NotificationChannel;
import com.aeronest.domain.DomainEnums.NotificationType;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.CustomerAccountRepository;
import com.aeronest.repository.TravelNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {
    private final TravelNotificationRepository notifications;
    private final CustomerAccountRepository accounts;
    private final BookingRepository bookings;
    private final NotificationDeliveryService delivery;

    public NotificationService(TravelNotificationRepository notifications, CustomerAccountRepository accounts,
                               BookingRepository bookings, NotificationDeliveryService delivery) {
        this.notifications = notifications; this.accounts = accounts; this.bookings = bookings; this.delivery = delivery;
    }

    public List<TravelNotification> forAccount(CustomerAccount account) {
        return notifications.findTop50ByAccountIdAndChannelOrderByCreatedAtDesc(account.getId(), NotificationChannel.IN_APP);
    }

    public long unreadCount(CustomerAccount account) {
        return notifications.countByAccountIdAndReadAtIsNullAndChannel(account.getId(), NotificationChannel.IN_APP);
    }

    public List<TravelNotification> recentDeliveries() { return notifications.findTop50ByOrderByCreatedAtDesc(); }

    @Transactional
    public void bookingCreated(Booking booking) {
        String payment = booking.getStatus() == BookingStatus.CONFIRMED ? "Payment approved" : "Payment is pending";
        sendBooking(booking, NotificationType.BOOKING, "Booking " + booking.getPnr() + " created",
                payment + ". Your " + booking.getFlight().getOrigin().getIataCode() + " to " + booking.getFlight().getDestination().getIataCode() +
                        " journey is saved. Total: " + booking.getCurrency() + " " + booking.getTotalAmount().toPlainString() + ".");
    }

    @Transactional
    public void checkInCompleted(Booking booking, String passengerName, String boardingGroup) {
        sendBooking(booking, NotificationType.CHECK_IN, "Check-in complete",
                passengerName + " is checked in for " + booking.getFlight().getFlightNumber() + ". Boarding: " + boardingGroup + ".");
    }

    @Transactional
    public void passengerOperationalUpdate(Booking booking, String passengerName, String security, String boarding) {
        NotificationType type = "BOARDED".equals(boarding) ? NotificationType.BOARDING : NotificationType.SECURITY;
        sendBooking(booking, type, "Journey readiness updated",
                passengerName + " — security: " + security.replace('_', ' ') + ", boarding: " + boarding.replace('_', ' ') + ".");
    }

    @Transactional
    public void refundRequested(Booking booking, String amount) {
        sendBooking(booking, NotificationType.REFUND, "Refund request received",
                "We received the refund request for booking " + booking.getPnr() + ". Estimated refundable amount: LKR " + amount + ".");
    }

    @Transactional
    public void refundCompleted(Booking booking, String amount, String reference) {
        sendBooking(booking, NotificationType.REFUND, "Refund completed",
                "LKR " + amount + " was returned for booking " + booking.getPnr() + ". Reference: " + reference + ".");
    }

    @Transactional
    public void profileUpdated(CustomerAccount account) {
        send(account, null, NotificationType.PROFILE, "Profile updated", "Your AeroVista contact details and preferences were updated.");
    }

    @Transactional
    public void partnerReservationCreated(PartnerReservation reservation) {
        String title = reservation.getPartner().getType().name().replace('_', ' ') + " request " + reservation.getReference();
        String message = reservation.getPartner().getName() + " received your request for " + reservation.getServiceAt() +
                ". Estimated total: " + reservation.getCurrency() + " " + reservation.getEstimatedTotal().toPlainString() + ".";
        if (reservation.getAccount() != null)
            save(reservation.getAccount(), reservation.getAirlineBooking(), reservation.getEmail(), NotificationChannel.IN_APP,
                    NotificationType.PARTNER_SERVICE, title, message);
        save(reservation.getAccount(), reservation.getAirlineBooking(), reservation.getEmail(), NotificationChannel.EMAIL,
                NotificationType.PARTNER_SERVICE, title, message);
        save(reservation.getAccount(), reservation.getAirlineBooking(), reservation.getPhone(), NotificationChannel.SMS,
                NotificationType.PARTNER_SERVICE, title, message);
    }

    @Transactional
    public void journeyServiceRequested(JourneyServiceRequest request) {
        String service = request.getType().name().replace('_', ' ');
        sendBooking(request.getBooking(), NotificationType.JOURNEY_SERVICE,
                "Airport service " + request.getReference() + " requested",
                service + " is requested for " + request.getPassenger().getFullName() + " at " + request.getAirportCode() +
                        ". Airport confirmation is required. Price: " + request.getCurrency() + " " + request.getPrice().toPlainString() + ".");
    }

    @Transactional
    public void flightCancelled(Booking booking, String arrangement) {
        sendBooking(booking, NotificationType.DISRUPTION, booking.getFlight().getFlightNumber() + " cancelled",
                "We are sorry—your flight has been cancelled. " + arrangement + " Open Manage booking to choose rebooking, travel credit or a full refund.");
    }

    @Transactional
    public void disruptionChoiceSaved(Booking booking, String choice, String detail) {
        sendBooking(booking, NotificationType.DISRUPTION, "Disruption choice received",
                choice.replace('_', ' ') + " was saved for " + booking.getPnr() + ". " + detail);
    }

    @Transactional
    public void flightStatusChanged(Flight flight, FlightStatus status) {
        NotificationType type = switch (status) {
            case BOARDING -> NotificationType.BOARDING;
            case DEPARTED, IN_AIR -> NotificationType.DEPARTURE;
            case ARRIVED -> NotificationType.ARRIVAL;
            case DELAYED, CANCELLED -> NotificationType.DELAY;
            default -> NotificationType.SYSTEM;
        };
        String title = flight.getFlightNumber() + " is " + status.name().replace('_', ' ').toLowerCase();
        String delayDetail = status == FlightStatus.DELAYED
                ? " Revised departure " + flight.getEstimatedDepartureAt() + " (" + flight.getDelayMinutes() + " minutes). " +
                (flight.getDelayReason() == null ? "Please monitor your inbox before travelling to the airport." : flight.getDelayReason() + ".")
                : " Scheduled departure " + flight.getDepartureAt() + ".";
        String message = "Latest status for " + flight.getOrigin().getIataCode() + " to " + flight.getDestination().getIataCode() +
                ": " + status.name().replace('_', ' ') + "." + delayDetail;
        bookings.findByFlightIdAndStatusNot(flight.getId(), BookingStatus.CANCELLED)
                .forEach(booking -> sendBooking(booking, type, title, message));
    }

    @Transactional
    public void markRead(Long notificationId, CustomerAccount account) {
        TravelNotification notification = notifications.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (notification.getAccount() == null || !notification.getAccount().getId().equals(account.getId()))
            throw new BusinessRuleException("This notification does not belong to your account");
        notification.setReadAt(LocalDateTime.now());
    }

    private void sendBooking(Booking booking, NotificationType type, String title, String message) {
        CustomerAccount account = accounts.findByEmailIgnoreCase(booking.getContactEmail()).orElse(null);
        send(account, booking, type, title, message);
        save(account, booking, booking.getContactEmail(), NotificationChannel.EMAIL, type, title, message);
        if (booking.getContactPhone() != null && !booking.getContactPhone().isBlank())
            save(account, booking, booking.getContactPhone(), NotificationChannel.SMS, type, title, message);
    }

    private void send(CustomerAccount account, Booking booking, NotificationType type, String title, String message) {
        if (account != null) save(account, booking, account.getEmail(), NotificationChannel.IN_APP, type, title, message);
    }

    private void save(CustomerAccount account, Booking booking, String recipient, NotificationChannel channel,
                      NotificationType type, String title, String message) {
        TravelNotification notification = new TravelNotification(); notification.setAccount(account); notification.setBooking(booking);
        notification.setRecipient(recipient); notification.setChannel(channel); notification.setType(type); notification.setTitle(title);
        notification.setMessage(message); DeliveryStatus status = delivery.deliver(channel, recipient, title, message);
        notification.setDeliveryStatus(status); if (status == DeliveryStatus.SENT || status == DeliveryStatus.SIMULATED) notification.setSentAt(LocalDateTime.now());
        notifications.save(notification);
    }
}
