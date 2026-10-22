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

    RoundedFill([NSColor colorWithCalibratedRed:0.10 green:0.42 blue:0.86 alpha:0.95 * alpha], NSMakeRect(112, 772, 66, 66), 18);
    DrawText(@"✈", NSMakeRect(112, 783, 66, 48), 31, NSFontWeightMedium, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentCenter);
    DrawText(scene[@"title"], NSMakeRect(205, 748, 1260, 80), 50, NSFontWeightBold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"subtitle"], NSMakeRect(208, 826, 1430, 58), 25, NSFontWeightRegular, [NSColor colorWithCalibratedRed:0.84 green:0.90 blue:1.0 alpha:alpha], NSTextAlignmentLeft);
    DrawText([NSString stringWithFormat:@"%02ld / %02ld", (long)index, (long)featureCount], NSMakeRect(1635, 792, 170, 36), 18, NSFontWeightSemibold, [[NSColor whiteColor] colorWithAlphaComponent:0.82 * alpha], NSTextAlignmentRight);
    Fill([[NSColor whiteColor] colorWithAlphaComponent:0.22 * alpha], NSMakeRect(112, 956, 1696, 4));
    Fill([NSColor colorWithCalibratedRed:0.92 green:0.72 blue:0.30 alpha:alpha], NSMakeRect(112, 956, 1696.0 * index / featureCount, 4));
}

static void DrawIntro(NSDictionary *scene, double progress) {
    Fill([NSColor colorWithCalibratedRed:0.01 green:0.04 blue:0.11 alpha:0.48], NSMakeRect(0, 0, CanvasWidth, CanvasHeight));
    CGFloat alpha = MIN(1.0, progress / 0.20);
    RoundedFill([[NSColor whiteColor] colorWithAlphaComponent:0.14 * alpha], NSMakeRect(150, 190, 82, 82), 22);
    DrawText(@"✈", NSMakeRect(150, 205, 82, 54), 40, NSFontWeightMedium, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentCenter);
    DrawText(scene[@"title"], NSMakeRect(260, 168, 1200, 130), 104, NSFontWeightBold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"subtitle"], NSMakeRect(158, 330, 1400, 92), 35, NSFontWeightMedium, [NSColor colorWithCalibratedRed:0.90 green:0.94 blue:1.0 alpha:alpha], NSTextAlignmentLeft);
    RoundedFill([NSColor colorWithCalibratedRed:0.92 green:0.72 blue:0.30 alpha:alpha], NSMakeRect(158, 466, 162, 8), 4);
    DrawText(@"FLIGHTS  ·  STAYS  ·  TRANSFERS  ·  LIVE OPERATIONS", NSMakeRect(158, 510, 1340, 48), 21, NSFontWeightSemibold, [[NSColor whiteColor] colorWithAlphaComponent:0.9 * alpha], NSTextAlignmentLeft);
}

static void DrawOutro(NSDictionary *scene, double progress) {
    Fill([NSColor colorWithCalibratedRed:0.01 green:0.04 blue:0.12 alpha:0.84], NSMakeRect(0, 0, CanvasWidth, CanvasHeight));
    NSGradient *glow = [[NSGradient alloc] initWithColors:@[
        [NSColor colorWithCalibratedRed:0.07 green:0.55 blue:0.78 alpha:0.35],
        [NSColor clearColor]
    ]];
    [glow drawInRect:NSMakeRect(900, -200, 1200, 1200) relativeCenterPosition:NSMakePoint(0.2, -0.2)];
    CGFloat alpha = MIN(1.0, progress / 0.18);
    DrawText(@"AEROVISTA", NSMakeRect(170, 174, 600, 42), 22, NSFontWeightBold, [NSColor colorWithCalibratedRed:0.92 green:0.72 blue:0.30 alpha:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"title"], NSMakeRect(165, 240, 1420, 110), 76, NSFontWeightBold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentLeft);
    DrawText(scene[@"subtitle"], NSMakeRect(170, 374, 1480, 80), 28, NSFontWeightMedium, [NSColor colorWithCalibratedRed:0.82 green:0.89 blue:1.0 alpha:alpha], NSTextAlignmentLeft);

    NSArray *chips = @[@"50 destinations", @"10 scheduled flights", @"Refund workflows", @"Travel services", @"MySQL persistence", @"48 automated tests"];
    CGFloat x = 170, y = 500;
    for (NSString *chip in chips) {
        CGFloat chipWidth = 70 + chip.length * 13;
        if (x + chipWidth > 1720) { x = 170; y += 88; }
        RoundedFill([[NSColor whiteColor] colorWithAlphaComponent:0.12 * alpha], NSMakeRect(x, y, chipWidth, 58), 29);
        DrawText(chip, NSMakeRect(x + 24, y + 15, chipWidth - 48, 30), 20, NSFontWeightSemibold, [[NSColor whiteColor] colorWithAlphaComponent:alpha], NSTextAlignmentCenter);
        x += chipWidth + 18;
    }
    DrawText(@"github.com/niver3562005/Airline-Ticket-Reservation-System", NSMakeRect(170, 830, 1350, 50), 24, NSFontWeightMedium, [NSColor colorWithCalibratedRed:0.86 green:0.91 blue:1.0 alpha:alpha], NSTextAlignmentLeft);
    DrawText(@"YOUR JOURNEY, ELEVATED", NSMakeRect(170, 912, 900, 42), 20, NSFontWeightBold, [NSColor colorWithCalibratedRed:0.92 green:0.72 blue:0.30 alpha:alpha], NSTextAlignmentLeft);
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
        NSString *capturePath = [root stringByAppendingPathComponent:@"linkedin-showcase/captures"];
        NSString *outputPath = [root stringByAppendingPathComponent:@"AeroVista-LinkedIn-Showcase.mp4"];
        NSArray<NSDictionary *> *scenes = @[
            @{@"file": @"01-home.png", @"title": @"AeroVista", @"subtitle": @"A premium airline reservation and journey-management platform", @"duration": @4.2, @"style": @"intro"},
            @{@"file": @"02-search.png", @"title": @"Smart flight search", @"subtitle": @"Cabin choices, baggage allowances, stopovers and connecting routes", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"03-schedule.png", @"title": @"Live operations", @"subtitle": @"Availability, delays, cancellations and passenger recovery", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"04-countries.png", @"title": @"50-country travel guide", @"subtitle": @"Visa guidance, destination details and unique local imagery", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"05-packages.png", @"title": @"Curated journeys", @"subtitle": @"Packages, promo codes and connected travel experiences", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"06-hotels.png", @"title": @"Hotel marketplace", @"subtitle": @"Distinct stays across 50 international destinations", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"07-cabs.png", @"title": @"Airport transfers", @"subtitle": @"Cabs, shuttles and journey-linked passenger services", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"08-deals.png", @"title": @"Deals that convert", @"subtitle": @"Clear promotions, benefits and validated offer codes", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"09-payment.png", @"title": @"Secure payment flow", @"subtitle": @"Notifications, refund eligibility and cash-return tracking", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"10-manage.png", @"title": @"Manage every journey", @"subtitle": @"Check-in, boarding, cancellations, refunds and 24/7 care", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"11-login.png", @"title": @"Protected passenger accounts", @"subtitle": @"Profiles, preferences and complete booking history", @"duration": @3.2, @"style": @"feature"},
            @{@"file": @"01-home.png", @"title": @"Built for the complete journey", @"subtitle": @"Spring Boot · Thymeleaf · MVC · MySQL · Responsive UI · Automated tests", @"duration": @5.0, @"style": @"outro"}
        ];

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
                AVVideoAverageBitRateKey: @8000000,
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
            [posterData writeToFile:[root stringByAppendingPathComponent:@"AeroVista-LinkedIn-Cover.png"] atomically:YES];
            CGImageRelease(posterImage);
        } else {
            NSLog(@"Poster export warning: %@", posterError);
        }
        NSLog(@"Created %@", outputPath);
    }
    return 0;
}
