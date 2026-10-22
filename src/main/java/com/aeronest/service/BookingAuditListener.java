package com.aeronest.service;

import com.aeronest.domain.ActivityLog;
import com.aeronest.repository.ActivityLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BookingAuditListener {
    private final ActivityLogRepository logs;
    public BookingAuditListener(ActivityLogRepository logs) { this.logs = logs; }
    @EventListener
    public void record(BookingEvent event) { logs.save(new ActivityLog(event.type(), event.pnr(), event.description())); }
}
