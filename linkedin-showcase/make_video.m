#import <AppKit/AppKit.h>
#import <AVFoundation/AVFoundation.h>
#import <CoreVideo/CoreVideo.h>

static const NSInteger CanvasWidth = 1920;
static const NSInteger CanvasHeight = 1080;
static const int32_t FramesPerSecond = 30;

static void Fill(NSColor *color, NSRect rect) {
    [color setFill];
    [[NSBezierPath bezierPathWithRect:rect] fill];
}

static void RoundedFill(NSColor *color, NSRect rect, CGFloat radius) {
    [color setFill];
    [[NSBezierPath bezierPathWithRoundedRect:rect xRadius:radius yRadius:radius] fill];
}

static void DrawText(NSString *text, NSRect rect, CGFloat size, NSFontWeight weight, NSColor *color, NSTextAlignment alignment) {
    NSMutableParagraphStyle *paragraph = [[NSMutableParagraphStyle alloc] init];
    paragraph.alignment = alignment;
    paragraph.lineBreakMode = NSLineBreakByWordWrapping;
    NSDictionary *attributes = @{
        NSFontAttributeName: [NSFont systemFontOfSize:size weight:weight],
        NSForegroundColorAttributeName: color,
        NSParagraphStyleAttributeName: paragraph
    };
    [text drawInRect:rect withAttributes:attributes];
}

static NSDictionary *Feature(NSString *file, NSString *section, NSString *title, NSString *subtitle, double duration) {
    return @{
        @"file": file,
        @"section": section,
        @"title": title,
        @"subtitle": subtitle,
        @"duration": @(duration),
        @"style": @"feature"
    };
}

static void DrawScreenshot(NSImage *image, double progress, NSInteger index) {
    CGFloat zoom = 1.0 + 0.035 * progress;
    CGFloat drawWidth = CanvasWidth * zoom;
    CGFloat drawHeight = CanvasHeight * zoom;
    CGFloat direction = index % 2 == 0 ? 1.0 : -1.0;
    CGFloat originX = (CanvasWidth - drawWidth) / 2.0 + direction * 30.0 * progress;
    CGFloat originY = (CanvasHeight - drawHeight) / 2.0;
    [image drawInRect:NSMakeRect(originX, originY, drawWidth, drawHeight)
             fromRect:NSZeroRect
            operation:NSCompositingOperationSourceOver
             fraction:1.0
       respectFlipped:YES
                hints:@{NSImageHintInterpolation: @(NSImageInterpolationHigh)}];
}

static void DrawFeatureCaption(NSDictionary *scene, NSInteger index, NSInteger featureCount, double progress) {
    double fadeIn = MIN(1.0, progress / 0.16);
    double fadeOut = MIN(1.0, (1.0 - progress) / 0.12);
    CGFloat alpha = MAX(0.0, MIN(fadeIn, fadeOut));
    NSGradient *gradient = [[NSGradient alloc] initWithColorsAndLocations:
        [NSColor clearColor], 0.0,
        [NSColor colorWithCalibratedRed:0.02 green:0.07 blue:0.16 alpha:0.90 * alpha], 0.58,
        [NSColor colorWithCalibratedRed:0.01 green:0.04 blue:0.11 alpha:0.98 * alpha], 1.0,
        nil];
    [gradient drawInRect:NSMakeRect(0, 650, CanvasWidth, 430) angle:-90];

    DrawText([scene[@"section"] uppercaseString], NSMakeRect(205, 706, 1260, 34), 18, NSFontWeightBold, [NSColor colorWithCalibratedRed:0.85 green:0.73 blue:0.55 alpha:alpha], NSTextAlignmentLeft);
    RoundedFill([NSColor colorWithCalibratedRed:0.08 green:0.17 blue:0.35 alpha:0.96 * alpha], NSMakeRect(112, 772, 66, 66), 18);
    DrawText(@"✈", NSMakeRect(112, 783, 66, 48), 31, NSFontWeightMedium, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentCenter);
    DrawText(scene[@"title"], NSMakeRect(205, 748, 1320, 80), 46, NSFontWeightBold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"subtitle"], NSMakeRect(208, 826, 1430, 58), 25, NSFontWeightRegular, [NSColor colorWithCalibratedRed:0.84 green:0.90 blue:1.0 alpha:alpha], NSTextAlignmentLeft);
    DrawText([NSString stringWithFormat:@"%02ld / %02ld", (long)index, (long)featureCount], NSMakeRect(1635, 792, 170, 36), 18, NSFontWeightSemibold, [[NSColor whiteColor] colorWithAlphaComponent:0.82 * alpha], NSTextAlignmentRight);
    Fill([[NSColor whiteColor] colorWithAlphaComponent:0.22 * alpha], NSMakeRect(112, 956, 1696, 4));
    Fill([NSColor colorWithCalibratedRed:0.85 green:0.73 blue:0.55 alpha:alpha], NSMakeRect(112, 956, 1696.0 * index / featureCount, 4));
}

static void DrawIntro(NSDictionary *scene, double progress) {
    Fill([NSColor colorWithCalibratedRed:0.01 green:0.04 blue:0.11 alpha:0.48], NSMakeRect(0, 0, CanvasWidth, CanvasHeight));
    CGFloat alpha = MIN(1.0, progress / 0.20);
    RoundedFill([[NSColor whiteColor] colorWithAlphaComponent:0.14 * alpha], NSMakeRect(150, 190, 82, 82), 22);
    DrawText(@"✈", NSMakeRect(150, 205, 82, 54), 40, NSFontWeightMedium, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentCenter);
    DrawText(scene[@"title"], NSMakeRect(260, 168, 1200, 130), 104, NSFontWeightBold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"subtitle"], NSMakeRect(158, 330, 1400, 92), 35, NSFontWeightMedium, [NSColor colorWithCalibratedRed:0.90 green:0.94 blue:1.0 alpha:alpha], NSTextAlignmentLeft);
    RoundedFill([NSColor colorWithCalibratedRed:0.85 green:0.73 blue:0.55 alpha:alpha], NSMakeRect(158, 466, 162, 8), 4);
    DrawText(@"FLIGHTS  ·  STAYS  ·  TRANSFERS  ·  LIVE OPERATIONS", NSMakeRect(158, 510, 1340, 48), 21, NSFontWeightSemibold, [[NSColor whiteColor] colorWithAlphaComponent:0.9 * alpha], NSTextAlignmentLeft);
}

static void DrawOutro(NSDictionary *scene, double progress) {
    Fill([NSColor colorWithCalibratedRed:0.01 green:0.04 blue:0.12 alpha:0.84], NSMakeRect(0, 0, CanvasWidth, CanvasHeight));
    NSGradient *glow = [[NSGradient alloc] initWithColors:@[
        [NSColor colorWithCalibratedRed:0.91 green:0.71 blue:0.56 alpha:0.28],
        [NSColor clearColor]
    ]];
    [glow drawInRect:NSMakeRect(900, -200, 1200, 1200) relativeCenterPosition:NSMakePoint(0.2, -0.2)];
    CGFloat alpha = MIN(1.0, progress / 0.18);
    DrawText(@"AEROVISTA", NSMakeRect(170, 174, 600, 42), 22, NSFontWeightBold, [NSColor colorWithCalibratedRed:0.85 green:0.73 blue:0.55 alpha:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"title"], NSMakeRect(165, 240, 1420, 110), 76, NSFontWeightBold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"subtitle"], NSMakeRect(170, 374, 1480, 80), 28, NSFontWeightMedium, [NSColor colorWithCalibratedRed:0.82 green:0.89 blue:1.0 alpha:alpha], NSTextAlignmentLeft);

    NSArray *chips = @[@"Role-based portals", @"Year-round schedules", @"Refund workflows", @"Travel services", @"MySQL persistence", @"Automated testing"];
    CGFloat x = 170, y = 500;
    for (NSString *chip in chips) {
        CGFloat chipWidth = 70 + chip.length * 13;
        if (x + chipWidth > 1720) { x = 170; y += 88; }
        RoundedFill([[NSColor whiteColor] colorWithAlphaComponent:0.12 * alpha], NSMakeRect(x, y, chipWidth, 58), 29);
        DrawText(chip, NSMakeRect(x + 24, y + 15, chipWidth - 48, 30), 20, NSFontWeightSemibold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentCenter);
        x += chipWidth + 18;
    }
    DrawText(@"github.com/niver3562005/Airline-Ticket-Reservation-System", NSMakeRect(170, 830, 1350, 50), 24, NSFontWeightMedium, [NSColor colorWithCalibratedRed:0.86 green:0.91 blue:1.0 alpha:alpha], NSTextAlignmentLeft);
    DrawText(@"YOUR JOURNEY, ELEVATED", NSMakeRect(170, 912, 900, 42), 20, NSFontWeightBold, [NSColor colorWithCalibratedRed:0.85 green:0.73 blue:0.55 alpha:alpha], NSTextAlignmentLeft);
}

static void RenderFrame(NSDictionary *scene, NSImage *image, NSInteger index, NSInteger featureCount, double progress, CVPixelBufferRef buffer) {
    CVPixelBufferLockBaseAddress(buffer, 0);
    void *base = CVPixelBufferGetBaseAddress(buffer);
    size_t bytesPerRow = CVPixelBufferGetBytesPerRow(buffer);
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef context = CGBitmapContextCreate(base, CanvasWidth, CanvasHeight, 8, bytesPerRow, colorSpace, kCGBitmapByteOrder32Little | kCGImageAlphaPremultipliedFirst);
    CGColorSpaceRelease(colorSpace);
    if (!context) { CVPixelBufferUnlockBaseAddress(buffer, 0); return; }

    CGContextTranslateCTM(context, 0, CanvasHeight);
    CGContextScaleCTM(context, 1, -1);
    NSGraphicsContext *graphics = [NSGraphicsContext graphicsContextWithCGContext:context flipped:YES];
    [NSGraphicsContext saveGraphicsState];
    [NSGraphicsContext setCurrentContext:graphics];
    Fill([NSColor colorWithCalibratedRed:0.01 green:0.04 blue:0.10 alpha:1], NSMakeRect(0, 0, CanvasWidth, CanvasHeight));
    DrawScreenshot(image, progress, index);
    NSString *style = scene[@"style"];
    if ([style isEqualToString:@"intro"]) DrawIntro(scene, progress);
    else if ([style isEqualToString:@"outro"]) DrawOutro(scene, progress);
    else DrawFeatureCaption(scene, index, featureCount, progress);
    [NSGraphicsContext restoreGraphicsState];
    CGContextRelease(context);
    CVPixelBufferUnlockBaseAddress(buffer, 0);
}

int main(int argc, const char * argv[]) {
    @autoreleasepool {
        NSString *root = argc > 1 ? [NSString stringWithUTF8String:argv[1]] : [[NSFileManager defaultManager] currentDirectoryPath];
        BOOL shortMode = argc > 2 && [[[NSString alloc] initWithUTF8String:argv[2]] isEqualToString:@"short"];
        NSString *capturePath = [root stringByAppendingPathComponent:@"linkedin-showcase/extended-captures"];
        NSString *outputPath = [root stringByAppendingPathComponent:@"AeroVista-Premium-Platform-Walkthrough.mp4"];
        NSString *coverPath = [root stringByAppendingPathComponent:@"AeroVista-Premium-Platform-Cover.png"];
        NSArray<NSDictionary *> *scenes = @[
            @{@"file": @"01-home.png", @"title": @"AeroVista", @"subtitle": @"Premium airline reservation and operations experience", @"duration": @4.5, @"style": @"intro"},

            Feature(@"02-create-account.png", @"Account & Security", @"Create a passenger account", @"Validated registration, secure passwords and travel preferences", 2.1),
            Feature(@"03-verify-email.png", @"Account & Security", @"Secure email verification", @"Single-use activation protects every passenger identity", 1.9),
            Feature(@"04-sign-in.png", @"Account & Security", @"Passenger sign-in", @"Secure access to bookings, preferences and journey alerts", 1.9),
            Feature(@"05-account-welcome.png", @"Account & Security", @"Personal journey dashboard", @"Loyalty identity, live tools, bookings and recent updates", 2.2),
            Feature(@"06-manage-profile.png", @"Account & Security", @"Manage the passenger profile", @"Contact details, preferences and account security", 1.9),
            Feature(@"07-profile-updated.png", @"Account & Security", @"Profile update confirmed", @"Saved preferences create a passenger notification", 1.8),

            Feature(@"08-flight-schedule.png", @"Flight Discovery", @"Published flight schedule", @"Flight numbers, aircraft, gates, times and live status", 2.1),
            Feature(@"09-flight-search.png", @"Flight Discovery", @"Live flight availability", @"Prices, remaining seats, delay alerts and baggage details", 2.2),
            Feature(@"10-cabin-allowances.png", @"Flight Discovery", @"Four cabin experiences", @"Economy through First with 23–50 kg baggage allowances", 2.1),

            Feature(@"11-booking-contact.png", @"Booking & Payment", @"Flight checkout", @"Account-aware contact details and a complete flight summary", 2.1),
            Feature(@"12-passenger-seat.png", @"Booking & Payment", @"Passenger, meal and seat selection", @"Passport validation, Business seat, meal and assistance", 2.5),
            Feature(@"13-payment-promo.png", @"Booking & Payment", @"Protected payment and promotions", @"Validated offers with no stored card details", 2.2),
            Feature(@"14-booking-confirmation.png", @"Booking & Payment", @"Booking confirmed", @"Ticket, itinerary, payment status and a unique PNR", 2.8),
            Feature(@"15-account-booking.png", @"Booking & Payment", @"Booking added to the account", @"Journey history and real-time booking notifications", 2.0),

            Feature(@"16-manage-booking.png", @"Journey Management", @"Manage the complete journey", @"Flight times, PNR, readiness and passenger controls", 2.2),
            Feature(@"17-journey-readiness.png", @"Journey Management", @"Check-in and boarding readiness", @"Document, security and boarding stages for every passenger", 2.1),
            Feature(@"18-check-in-complete.png", @"Journey Management", @"Online check-in complete", @"The passenger is ready for security and boarding", 2.0),
            Feature(@"19-priority-airport-care.png", @"Journey Management", @"Priority airport care", @"Fast-track, boarding, baggage and meet-and-assist", 2.2),
            Feature(@"20-priority-confirmed.png", @"Journey Management", @"Airport service confirmed", @"A priced request is sent to the operations team", 2.0),

            Feature(@"21-accommodations.png", @"Connected Travel", @"Global accommodation marketplace", @"53 distinctive hotels across 50 destinations", 2.2),
            Feature(@"22-hotel-reservation.png", @"Connected Travel", @"Flight-connected hotel reservation", @"Dates, transfers, preferences and airline PNR", 2.2),
            Feature(@"23-service-confirmation.png", @"Connected Travel", @"Accommodation confirmed", @"The service appears in passenger care and the dashboard", 2.0),
            Feature(@"24-airport-transfers.png", @"Connected Travel", @"Cabs and airport shuttles", @"Private transfers, shared shuttles and chauffeurs", 2.0),
            Feature(@"25-travel-agencies.png", @"Connected Travel", @"Verified travel agencies", @"Multi-city planning and destination assistance", 1.9),
            Feature(@"26-airport-dining.png", @"Connected Travel", @"Airport dining", @"Terminal cafés and dietary requests before departure", 1.9),
            Feature(@"27-travel-sim.png", @"Connected Travel", @"Travel SIM and eSIM", @"Connectivity ready from the moment of arrival", 1.9),
            Feature(@"28-packages.png", @"Connected Travel", @"Curated flight-and-stay packages", @"City, family, business, wellness and island journeys", 2.1),

            Feature(@"29-visa-guide.png", @"Travel Intelligence", @"Visa and destination guidance", @"Entry details with links to official authorities", 2.0),
            Feature(@"30-live-tracking.png", @"Travel Intelligence", @"Real-time flight telemetry", @"Position, speed, altitude, ETA and availability", 2.5),
            Feature(@"31-airport-facilities.png", @"Travel Intelligence", @"Airport facilities", @"Lounges, dining, accessibility, transport and family services", 2.0),
            Feature(@"32-deals.png", @"Travel Intelligence", @"Deals and promotions", @"Transparent offers and validated discount codes", 1.9),
            Feature(@"33-payment-options.png", @"Travel Intelligence", @"Flexible payment options", @"Card, bank transfer and pay-at-counter", 1.9),

            Feature(@"34-feedback.png", @"Passenger Care", @"Passenger feedback", @"Rated, categorized and linked to the booking", 2.0),
            Feature(@"35-feedback-recorded.png", @"Passenger Care", @"Feedback case created", @"An auditable service case reaches the care team", 1.9),
            Feature(@"36-notifications.png", @"Passenger Care", @"Multi-channel notifications", @"Booking, payment, profile, check-in and partner events", 2.1),
            Feature(@"37-refund-quote.png", @"Passenger Care", @"Transparent cancellation quote", @"Paid amount, fee and cash return before confirmation", 2.5),
            Feature(@"38-booking-cancelled.png", @"Passenger Care", @"Cancellation completed safely", @"A tracked cash-refund request opens automatically", 2.2),
            Feature(@"39-cancelled-in-account.png", @"Passenger Care", @"Account history after cancellation", @"Cancelled journeys and connected services remain visible", 2.3),
            Feature(@"40-refund-notifications.png", @"Passenger Care", @"Cancellation and refund alerts", @"Communications are recorded across the journey timeline", 2.2),

            Feature(@"41-admin-sign-in.png", @"Airline Operations", @"Secure staff access", @"Role-based passenger and operations separation", 1.9),
            Feature(@"42-operations-dashboard.png", @"Airline Operations", @"Airline operations dashboard", @"Flights, bookings, check-in, revenue and activity KPIs", 2.6),
            Feature(@"43-flight-operations.png", @"Airline Operations", @"Flight schedule management", @"Aircraft, gates, fares and operational statuses", 2.0),
            Feature(@"44-flight-control.png", @"Airline Operations", @"Live flight control", @"Manifest, seat map, delay and operational controls", 2.0),
            Feature(@"45-telemetry-control.png", @"Airline Operations", @"Telemetry control", @"Latitude, longitude, altitude, speed and ETA", 2.0),
            Feature(@"46-booking-operations.png", @"Airline Operations", @"Operational booking detail", @"Payments, travellers, seats and readiness", 2.1),
            Feature(@"47-manifest-readiness.png", @"Airline Operations", @"Passenger manifest", @"Check-in, security and boarding controls", 2.0),
            Feature(@"48-service-center.png", @"Airline Operations", @"Passenger service centre", @"Refunds, feedback, disruptions and delivery monitoring", 2.1),
            Feature(@"49-connected-services-ops.png", @"Airline Operations", @"Connected-service operations", @"Hotel, cab, agency, café and SIM queues", 2.0),
            Feature(@"50-priority-ops.png", @"Airline Operations", @"Priority airport operations", @"Fast-track and baggage requests tracked for fulfilment", 2.0),
            Feature(@"51-refund-operations.png", @"Airline Operations", @"Refund operations", @"Cash returns, fees and processor status", 2.4),
            Feature(@"52-feedback-operations.png", @"Airline Operations", @"Feedback and service recovery", @"Ratings and auditable staff responses", 2.0),
            Feature(@"53-airport-management.png", @"Airline Operations", @"Airport management", @"Validated IATA data and global operational locations", 2.0),
            Feature(@"54-fleet-management.png", @"Airline Operations", @"Fleet management", @"Aircraft capacity and cabin-layout controls", 2.0),

            @{@"file": @"01-home.png", @"title": @"One platform. Every journey.", @"subtitle": @"Spring Boot · Thymeleaf · MVC · MySQL · Responsive UI · Automated tests", @"duration": @5.5, @"style": @"outro"}
        ];

        if (shortMode) {
            capturePath = [root stringByAppendingPathComponent:@"linkedin-showcase/short-captures"];
            outputPath = [root stringByAppendingPathComponent:@"AeroVista-LinkedIn-Short-Walkthrough.mp4"];
            coverPath = [root stringByAppendingPathComponent:@"AeroVista-LinkedIn-Short-Cover.png"];
            scenes = @[
                @{@"file": @"01-home.png", @"title": @"AeroVista", @"subtitle": @"A premium journey from search to arrival", @"duration": @5.0, @"style": @"intro"},
                Feature(@"02-workspace-selector.png", @"Secure Access", @"One platform. Three workspaces.", @"Purpose-built access for customers, airline staff and administrators", 4.7),
                Feature(@"03-customer-login.png", @"Passenger Access", @"A polished, protected welcome", @"Secure account access begins the connected passenger journey", 4.7),
                Feature(@"04-customer-dashboard.png", @"Passenger Experience", @"The passenger journey hub", @"Bookings, check-in, alerts, payments, services and feedback", 4.7),
                Feature(@"05-long-term-schedule.png", @"Flight Discovery", @"Year-round schedules", @"Future dates, rotating times, fares and baggage packages", 4.7),
                Feature(@"06-flight-checkout.png", @"Booking", @"Complete flight checkout", @"Cabin, passenger, seat, promotion and payment choices", 4.7),
                Feature(@"07-booking-confirmation.png", @"Booking", @"Booking confirmed", @"A complete itinerary, payment status and passenger PNR", 4.7),
                Feature(@"08-manage-booking.png", @"Journey Management", @"Manage the complete journey", @"Check-in, readiness, services and cancellation controls", 4.7),
                Feature(@"09-refund-quote.png", @"Passenger Care", @"Transparent cancellations", @"Fees and expected cash returns are shown before confirmation", 4.7),
                Feature(@"10-travel-services.png", @"Connected Travel", @"Travel beyond the flight", @"Hotels, transfers, agencies, cafés and eSIM services", 4.7),
                Feature(@"11-live-flight.png", @"Live Operations", @"Live flight intelligence", @"Status, position, altitude, speed and estimated arrival", 4.7),
                Feature(@"12-staff-login.png", @"Airline Staff", @"Secure operations access", @"Staff enter a dedicated workspace for passenger and flight readiness", 4.7),
                Feature(@"13-staff-dashboard.png", @"Airline Staff", @"Daily operations workspace", @"Flights, manifests, boarding, disruptions and service recovery", 4.7),
                Feature(@"14-flight-operations.png", @"Airline Staff", @"Long-term schedule control", @"Aircraft, gates, fares, packages and flight status", 4.7),
                Feature(@"15-service-center.png", @"Airline Staff", @"Passenger service centre", @"Refunds, feedback, priority care and partner requests", 4.7),
                Feature(@"16-admin-login.png", @"Administration", @"Protected administrator access", @"Governance tools stay separated from passenger and staff workflows", 4.7),
                Feature(@"17-admin-dashboard.png", @"Administration", @"Administrator control centre", @"Fleet, airports, schedules, revenue and service oversight", 4.7),
                Feature(@"18-fleet-management.png", @"Administration", @"Fleet and capacity management", @"Aircraft availability and cabin layouts ready for operations", 4.7),
                @{@"file": @"01-home.png", @"title": @"AeroVista", @"subtitle": @"A complete passenger and airline operations experience", @"duration": @5.0, @"style": @"outro"}
            ];
        }

        NSMutableDictionary<NSString *, NSImage *> *images = [NSMutableDictionary dictionary];
        for (NSDictionary *scene in scenes) {
            NSString *file = scene[@"file"];
            if (!images[file]) {
                NSImage *image = [[NSImage alloc] initWithContentsOfFile:[capturePath stringByAppendingPathComponent:file]];
                if (!image) { NSLog(@"Cannot load %@", file); return 1; }
                images[file] = image;
            }
        }

        [[NSFileManager defaultManager] removeItemAtPath:outputPath error:nil];
        NSError *error = nil;
        AVAssetWriter *writer = [[AVAssetWriter alloc] initWithURL:[NSURL fileURLWithPath:outputPath] fileType:AVFileTypeMPEG4 error:&error];
        if (!writer) { NSLog(@"Writer error: %@", error); return 1; }
        NSDictionary *settings = @{
            AVVideoCodecKey: AVVideoCodecTypeH264,
            AVVideoWidthKey: @(CanvasWidth),
            AVVideoHeightKey: @(CanvasHeight),
            AVVideoCompressionPropertiesKey: @{
                AVVideoAverageBitRateKey: @6000000,
                AVVideoProfileLevelKey: AVVideoProfileLevelH264HighAutoLevel,
                AVVideoMaxKeyFrameIntervalKey: @60
            }
        };
        AVAssetWriterInput *input = [AVAssetWriterInput assetWriterInputWithMediaType:AVMediaTypeVideo outputSettings:settings];
        input.expectsMediaDataInRealTime = NO;
        NSDictionary *pixelAttributes = @{
            (NSString *)kCVPixelBufferPixelFormatTypeKey: @(kCVPixelFormatType_32BGRA),
            (NSString *)kCVPixelBufferWidthKey: @(CanvasWidth),
            (NSString *)kCVPixelBufferHeightKey: @(CanvasHeight)
        };
        AVAssetWriterInputPixelBufferAdaptor *adaptor = [AVAssetWriterInputPixelBufferAdaptor assetWriterInputPixelBufferAdaptorWithAssetWriterInput:input sourcePixelBufferAttributes:pixelAttributes];
        if (![writer canAddInput:input]) { NSLog(@"Cannot add writer input"); return 1; }
        [writer addInput:input];
        if (![writer startWriting]) { NSLog(@"Start error: %@", writer.error); return 1; }
        [writer startSessionAtSourceTime:kCMTimeZero];

        int64_t frameNumber = 0;
        NSInteger featureCount = scenes.count - 2;
        for (NSInteger index = 0; index < scenes.count; index++) {
            NSDictionary *scene = scenes[index];
            NSInteger sceneFrames = MAX(1, llround([scene[@"duration"] doubleValue] * FramesPerSecond));
            for (NSInteger localFrame = 0; localFrame < sceneFrames; localFrame++) {
                while (!input.readyForMoreMediaData) usleep(2000);
                @autoreleasepool {
                    CVPixelBufferRef buffer = NULL;
                    if (CVPixelBufferPoolCreatePixelBuffer(NULL, adaptor.pixelBufferPool, &buffer) != kCVReturnSuccess || !buffer) {
                        NSLog(@"Cannot allocate frame %lld", frameNumber); return 1;
                    }
                    double progress = sceneFrames == 1 ? 1.0 : (double)localFrame / (sceneFrames - 1);
                    RenderFrame(scene, images[scene[@"file"]], index, featureCount, progress, buffer);
                    BOOL ok = [adaptor appendPixelBuffer:buffer withPresentationTime:CMTimeMake(frameNumber, FramesPerSecond)];
                    CVPixelBufferRelease(buffer);
                    if (!ok) { NSLog(@"Append error: %@", writer.error); return 1; }
                    frameNumber++;
                }
            }
            NSLog(@"Rendered %ld/%ld: %@", (long)index + 1, (long)scenes.count, scene[@"title"]);
        }

        [input markAsFinished];
        dispatch_semaphore_t finished = dispatch_semaphore_create(0);
        [writer finishWritingWithCompletionHandler:^{ dispatch_semaphore_signal(finished); }];
        dispatch_semaphore_wait(finished, DISPATCH_TIME_FOREVER);
        if (writer.status != AVAssetWriterStatusCompleted) { NSLog(@"Finish error: %@", writer.error); return 1; }
        AVURLAsset *videoAsset = [AVURLAsset URLAssetWithURL:[NSURL fileURLWithPath:outputPath] options:nil];
        AVAssetImageGenerator *generator = [[AVAssetImageGenerator alloc] initWithAsset:videoAsset];
        generator.appliesPreferredTrackTransform = YES;
        generator.maximumSize = NSMakeSize(CanvasWidth, CanvasHeight);
        NSError *posterError = nil;
        CGImageRef posterImage = [generator copyCGImageAtTime:CMTimeMakeWithSeconds(2.4, 600) actualTime:NULL error:&posterError];
        if (posterImage) {
            NSBitmapImageRep *poster = [[NSBitmapImageRep alloc] initWithCGImage:posterImage];
            NSData *posterData = [poster representationUsingType:NSBitmapImageFileTypePNG properties:@{}];
            [posterData writeToFile:coverPath atomically:YES];
            CGImageRelease(posterImage);
        } else {
            NSLog(@"Poster export warning: %@", posterError);
        }
        NSLog(@"Created %@", outputPath);
    }
    return 0;
}
