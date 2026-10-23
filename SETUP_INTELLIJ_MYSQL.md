# Run AeroVista with IntelliJ IDEA and MySQL

## Requirements

- IntelliJ IDEA Community or Ultimate
- Java Development Kit 17 or newer
- Maven 3.9+ (IntelliJ's bundled Maven is fine)
- MySQL 8.x, either through Docker Desktop or a normal MySQL installation

## 1. Open the project

1. Extract the supplied AeroVista ZIP.
2. Open IntelliJ IDEA and choose **Open**.
3. Select the extracted folder containing `pom.xml`.
4. Choose **Trust Project** if IntelliJ asks.
5. Open **File → Project Structure → Project** and select Java 17 or newer as the Project SDK.
6. Open the Maven panel and press **Reload All Maven Projects**.

## 2A. Recommended MySQL setup with Docker

Open IntelliJ's Terminal tab in the project folder and run:

```bash
docker compose up -d
```

This starts MySQL with:

| Setting | Value |
|---|---|
| Host | `127.0.0.1` |
| Port | `3307` |
| Database | `aerovista` |
| Username | `aeronest` |
| Password | `aeronest` |

Check that it is ready with:

```bash
docker compose ps
```

## 2B. Native MySQL alternative

If MySQL is installed directly on the computer, run the following in MySQL Workbench or the MySQL command line as an administrator:

```sql
CREATE DATABASE IF NOT EXISTS aerovista
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'aeronest'@'localhost' IDENTIFIED BY 'aeronest';
GRANT ALL PRIVILEGES ON aerovista.* TO 'aeronest'@'localhost';
FLUSH PRIVILEGES;
```

For native MySQL on its normal port, use this database URL in IntelliJ:

```text
jdbc:mysql://127.0.0.1:3306/aerovista?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo
```

## 3. Create the IntelliJ run configuration

1. Open **Run → Edit Configurations**.
2. Press **+** and choose **Spring Boot**.
3. Select main class `com.aeronest.AeroNestApplication`.
4. Set **Active profiles** to `mysql`.
5. For the Docker database, no environment variables are required. The application already defaults to port `3307` and the credentials above.
6. For native MySQL, add these environment variables:

```text
DB_URL=jdbc:mysql://127.0.0.1:3306/aerovista?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo
DB_USERNAME=aeronest
DB_PASSWORD=aeronest
```

7. Press **Run**. Hibernate creates or upgrades the tables and the seeders add airports, aircraft, 50 long-term services for every international destination, connecting segments, 50 destination countries, 53 city hotels, cabs/shuttles, packages and demonstration accounts.
8. Open [http://localhost:8080](http://localhost:8080).

You can also run the project from IntelliJ's Terminal:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 4. Confirm that MySQL is updating

Connect from MySQL Workbench using the values above and run:

```sql
USE aerovista;
SHOW TABLES;
SELECT flight_number, departure_at, arrival_at, fare_brand,
       baggage_allowance_kg, status, delay_minutes, delay_reason
FROM flights
ORDER BY departure_at
LIMIT 15;

SELECT COUNT(*) AS country_count,
       SUM(image_path IS NOT NULL) AS countries_with_images
FROM country_travel_info;

SELECT type, COUNT(*) AS partner_count,
       SUM(image_path IS NOT NULL) AS partners_with_images
FROM travel_partners
GROUP BY type;
```

Create a booking in the website and verify it with:

```sql
SELECT pnr, contact_email, status, total_amount, created_at
FROM booking
ORDER BY created_at DESC;
```

The default Hibernate naming strategy uses tables such as `booking`, `passenger`, `payment`, `refund_request`, `country_travel_info`, `travel_partners` and `travel_notification`.

## 5. Open AeroVista on a phone or another computer

Both devices must be connected to the same trusted Wi-Fi network.

1. In the IntelliJ run configuration, add this program argument:

```text
--server.address=0.0.0.0
```

2. Find the development computer's local IP address. On macOS, open **System Settings → Wi-Fi → Details**. It normally looks like `192.168.1.25`.
3. On the second device, open `http://YOUR_LOCAL_IP:8080`, for example `http://192.168.1.25:8080`.
4. If it does not load, allow Java/IntelliJ through the computer firewall. Only use this on a trusted private network.

Do not expose the MySQL port to the internet. The second device connects to the Spring Boot website, while the application talks to MySQL locally.

## 6. Demonstration accounts

| Role | Username | Password |
|---|---|---|
| Passenger | `traveller@aerovista.test` | `Travel@123` |
| Administrator | `admin` | `aeronest123` |
| Operations agent | `agent` | `agent123` |

## 7. Optional real email/SMS provider

Without provider URLs, notification deliveries are safely stored as `SIMULATED` in MySQL. To connect provider adapters, configure HTTPS endpoints:

```text
EMAIL_NOTIFICATION_WEBHOOK=https://your-provider.example/email
SMS_NOTIFICATION_WEBHOOK=https://your-provider.example/sms
```

Every provider call receives `recipient`, `title`, `message` and `channel`. Payment processing remains a safe academic simulation and does not collect real card numbers.

## 8. Build and test

```bash
mvn clean test
mvn clean package
java -jar target/aeronest-1.0.0.jar --spring.profiles.active=mysql
```

The packaged application is then available at `http://localhost:8080`.
