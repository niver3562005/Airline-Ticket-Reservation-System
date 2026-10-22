package com.aeronest.domain;

public final class DomainEnums {
    private DomainEnums() {}
    public enum FlightStatus { SCHEDULED, BOARDING, DEPARTED, IN_AIR, ARRIVED, DELAYED, CANCELLED }
    public enum CabinClass { ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST }
    public enum BookingStatus { PENDING, CONFIRMED, CANCELLED, COMPLETED }
    public enum PaymentStatus { PENDING, PAID, REFUND_PENDING, REFUNDED, FAILED }
    public enum PaymentMethod { CARD, BANK_TRANSFER, PAY_AT_COUNTER }
    public enum LoyaltyTier { EXPLORER, SILVER, GOLD }
    public enum AccountTokenType { EMAIL_VERIFICATION, PASSWORD_RESET }
    public enum VisaRequirement { VISA_FREE, VISA_ON_ARRIVAL, ETA_REQUIRED, EVISA_REQUIRED, VISA_REQUIRED, CHECK_REQUIRED }
    public enum FacilityType { LOUNGE, WIFI, DINING, SHOPPING, PRAYER_ROOM, MEDICAL, ACCESSIBILITY, TRANSPORT, SLEEP, FAMILY }
    public enum PackageType { CITY_BREAK, BEACH, FAMILY, BUSINESS, ADVENTURE, WELLNESS }
    public enum NotificationType { BOOKING, PAYMENT, SECURITY, CHECK_IN, BOARDING, DEPARTURE, DELAY, ARRIVAL, REFUND, PROFILE, DISRUPTION, PARTNER_SERVICE, JOURNEY_SERVICE, SYSTEM }
    public enum NotificationChannel { IN_APP, EMAIL, SMS }
    public enum DeliveryStatus { QUEUED, SENT, SIMULATED, FAILED }
    public enum RefundStatus { REQUESTED, APPROVED, PROCESSING, COMPLETED, DECLINED }
    public enum FeedbackCategory { BOOKING, AIRPORT, ONBOARD, DIGITAL, BAGGAGE, SUPPORT }
    public enum FeedbackStatus { NEW, REVIEWED, RESOLVED }
    public enum SecurityStatus { PENDING, DOCUMENTS_VERIFIED, CLEARED, MANUAL_REVIEW }
    public enum BoardingStatus { NOT_READY, READY, BOARDED, NO_SHOW }
    public enum PartnerType { TRAVEL_AGENCY, HOTEL, CAB, CAFE, SIM }
    public enum PartnerReservationStatus { REQUESTED, CONFIRMED, COMPLETED, CANCELLED }
    public enum DisruptionChoice { REBOOK, TRAVEL_CREDIT, FULL_REFUND }
    public enum DisruptionStatus { ACTION_REQUIRED, SELECTED, COMPLETED }
    public enum JourneyServiceType { PRIORITY_CHECK_IN, FAST_TRACK_SECURITY, PRIORITY_BOARDING, BAGGAGE_ASSISTANCE, MEET_AND_ASSIST }
    public enum JourneyServiceStatus { REQUESTED, CONFIRMED, COMPLETED, CANCELLED }
}
