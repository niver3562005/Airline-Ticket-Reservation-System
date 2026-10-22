# AeroVista

**Your Journey, Elevated.** AeroVista is a full-stack airline ticket reservation and operations platform built for the SE2030 topic **Web-based Airline Ticket Reservation System**.

The application combines a premium responsive passenger experience with a secure airline operations console. It uses Spring Boot, Thymeleaf and a layered MVC architecture, and it is structured into six substantial functional areas to meet the breadth expected from a six-member project.

## Main capabilities

1. **Passenger identity and security** — sign-up, email-verification demo flow, sign-in, remember-me, password reset, profile management, BCrypt hashing, CSRF protection and login throttling.
2. **Flight discovery and reservation** — one-way/round-trip search controls, direct and validated one-stop itineraries, live seat availability, cabin selection, group bookings for up to six travellers, dynamic fares and PNR generation.
3. **Booking servicing** — confirmation ticket, booking retrieval, cancellation-fee calculation, auditable refund workflow, online check-in, seat assignment, meal and assistance requests, frequent-flyer details and boarding groups.
4. **Airport, fleet and schedule management** — protected CRUD, IATA validation, generated cabin layouts, schedule validation, fare/gate assignment and flight-status updates.
5. **Payments and pricing** — card, bank-transfer and counter-payment strategies, demand-aware cabin pricing and promotion rules; no real card details are collected.
6. **Operations intelligence** — role-aware dashboard, booking and revenue KPIs, passenger manifests, live seat maps, security/boarding readiness, recent activity and event-driven audit records.
7. **Travel intelligence** — live flight-status lookup, Sri Lankan passport visa guidance with official authority links, destination entry details, airport facilities and curated flight-and-stay packages.
8. **Passenger communications** — persisted in-app, email and SMS notifications for payments, bookings, check-in, security, boarding, departure, delay, arrival, refund and profile events.
9. **Passenger care** — a 24/7 support experience, trackable ratings and feedback cases, refund processing, delivery monitoring and a dedicated administration service centre.
10. **Connected journey marketplace** — verified travel agencies, hotels, airport cabs, cafés and SIM/eSIM services with flight-linked requests and passenger notifications.
11. **Modern airline operations** — four cabin classes, automatic flight telemetry, altitude/speed/position/ETA displays, cabin-level availability and structured cancellation recovery through rebooking, credit or full refund.
12. **Priority airport journey** — passenger-linked priority check-in, fast-track security, priority boarding, baggage assistance and meet-and-assist requests with transparent pricing and staff fulfilment controls.
13. **Privacy centre** — premium multi-column service footer, clear privacy/cookie/legal pages, and persisted Accept all, Necessary only or category-level browser consent.
14. **Published flight schedule** — more than ten seeded journeys with airline identity, flight numbers, gates, aircraft, departure/arrival times, live status, fare brands, onboard features and cabin-specific baggage allowances.
15. **Proactive disruption and cash returns** — revised delay times with in-app/email/SMS delivery, transparent refund quotes before cancellation, a qualifying 24-hour full-refund window and tracked return-to-original-payment processing.
16. **Global destination catalogue** — 50 database-backed countries with featured cities, local currencies, emergency contacts, advisory entry guidance and an image-led destination browser.
17. **Hotel and airport-transfer network** — 53 image-backed hotels across the 50 featured destinations, plus cabs, shared shuttles, executive chauffeurs and resort-transfer coordination.
18. **Deals and secure checkout** — a dedicated offers page, four server-validated promo codes, illustrated payment guidance and a modern payment-method selector covering card, bank transfer and pay-at-counter workflows.

## Technology and architecture

- Java 17 and Spring Boot 3.3
- Spring MVC, Thymeleaf and Spring Security
- Spring Data JPA/Hibernate
- H2 persistent database for zero-setup local use and automated tests
- MySQL production profile and Docker Compose
- Jakarta Bean Validation
- JUnit 5, Spring Boot Test and MockMvc
- Responsive custom CSS, inline SVG icon system and progressive-enhancement JavaScript

### Premium interface system

The passenger and operations experiences share one reference-led AeroVista design language: Sora display typography, Manrope body copy, Inter for dense travel data, a deep-navy/champagne palette, restrained shadows, generous rounded surfaces and responsive layouts. The home experience uses an airport-at-sunset hero and floating booking panel; the same tokens continue through authentication, search, checkout, confirmation, account, travel-service and administration pages. Motion respects the browser's reduced-motion preference.

The LinkedIn showcase tooling lives in `linkedin-showcase/`. `capture_walkthrough.js` exercises the live MySQL-backed journey and records 54 verified screens, while `make_video.m` assembles them into a 55-scene `AeroVista-Premium-Platform-Walkthrough.mp4` with a matching cover image.

```text
Browser → MVC Controllers → Business Services → JPA Repositories → Database
                              ↓
                  Strategy / Factory / Domain Events
```

The controllers handle HTTP concerns, transactional services enforce business rules, repositories isolate persistence, and the domain layer models accounts, airports, facilities, country travel information, aircraft, flights, packages, bookings, passengers, payments, refunds, feedback, notifications and audit activity. Database and service constraints prevent two active bookings from claiming the same seat; schedule rules prevent aircraft overlaps and require a 60-minute turnaround.

### Design patterns

- **Strategy:** `PricingStrategy` / `DemandAwarePricingStrategy` separates fare calculation from booking orchestration.
- **Factory:** `PaymentProcessorFactory` selects card, bank-transfer or counter processors without conditional payment logic in `BookingService`.
- **Observer/Event:** booking events are handled by `BookingAuditListener`, keeping the audit trail separate from reservation transactions.
- **Repository:** Spring Data repositories provide a clean persistence boundary.

## Run locally

Requirements: Java 17+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). The application creates a persistent H2 database in `./data/` and seeds realistic airports, aircraft, future flights and a demonstration member account on first launch.

### Demonstration accounts

| Role | Username | Password |
|---|---|---|
| Passenger | `traveller@aerovista.test` | `Travel@123` |
| Administrator | `admin` | `aeronest123` |
| Operations agent | `agent` | `agent123` |

Administrators manage airports and aircraft. Administrators and agents can use the operations dashboard, flight controls, manifests and booking views. Passengers have a separate account dashboard, profile and booking history.

### MySQL profile

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

The MySQL service listens on `127.0.0.1:3307` and creates the `aerovista` schema. The profile accepts `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables.

### Email and SMS provider connection

The notification engine works immediately in simulation mode and records every delivery for demonstration. To connect real provider adapters, expose HTTPS webhook endpoints that accept `recipient`, `subject` and `message`, then set:

```bash
export EMAIL_NOTIFICATION_WEBHOOK=https://your-provider.example/email
export SMS_NOTIFICATION_WEBHOOK=https://your-provider.example/sms
mvn spring-boot:run
```

Provider failures are safely recorded as failed delivery attempts and never roll back the booking itself.

## Quality verification

```bash
mvn test
mvn clean package
```

The 48 automated tests cover pricing boundaries, four-cabin inventory, valid and invalid promotion handling, payment selection, atomic booking creation, duplicate-seat rejection, cash-refund quotes, disruption choices, delay-email content, partner and priority-airport reservations, the 50-country/image-backed hotel catalogue, telemetry persistence, notification creation, privacy pages, account security, deals and payment pages, public travel tools, the public schedule, direct and connecting-flight search, aircraft schedule conflict prevention, checkout rendering, authentication and role authorization.

## IntelliJ IDEA + MySQL setup

For a complete beginner-friendly setup, follow [SETUP_INTELLIJ_MYSQL.md](SETUP_INTELLIJ_MYSQL.md). It covers opening the Maven project, choosing Java 17+, starting MySQL with Docker or a native installation, creating the schema/user, configuring an IntelliJ run profile, testing the database connection, and opening the site from another device on the same Wi-Fi network.

## Recommended demonstration flow

1. Search from Colombo to Dubai for tomorrow.
2. Filter the result and open the responsive checkout.
3. Sign in as the demo passenger to show account-aware contact prefill.
4. Enter traveller data, choose a matching seat and apply `FLY10`.
5. Show the ticket, PNR and **Manage booking** flow.
6. Track demo flight **AN900** using today’s date to show automatic location, altitude, speed and time-to-land updates.
7. Open Hotels & Services to reserve a cab, hotel, agency, café or travel SIM and connect it to a PNR.
8. Open the Travel Hub to show visa guidance, packages, airport facilities and class-level availability.
9. Browse the 50-country destination directory and open Hotels to show the 53-city image-backed stay catalogue.
10. Open Deals, copy `WELCOME20`, and show the three secure demonstration payment choices during checkout.
11. From Manage booking, request fast-track security or baggage assistance and show the persisted request in the operations service centre.
12. Sign in as an administrator to demonstrate KPIs, manifests, live seat maps, telemetry controls, disruption arrangements, airport services, refunds, feedback cases and flight-status notifications.

## Security and data quality

- BCrypt password hashing and role-based route authorization
- CSRF tokens on state-changing requests
- Login attempt throttling and session hardening
- Server-side validation for important user inputs
- Transactional reservation flow with pessimistic/unique seat protection
- Parameterized JPA queries and optimistic locking on mutable entities
- Password reset and email verification use expiring single-use tokens

Payment processing is explicitly simulated for this academic platform; no real card details or external payment gateway are used. Email/SMS notifications default to simulated delivery and become provider-ready through the configured webhooks. Visa information is advisory and each result links to its official immigration source for final verification. The Java package and default database identifiers retain the original `aeronest` project namespace for migration compatibility, while the complete product-facing brand is AeroVista.
