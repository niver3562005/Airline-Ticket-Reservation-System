package com.aeronest.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

@Entity
public class ActivityLog extends BaseEntity {
    private String eventType;
    private String reference;
    @Lob private String description;
    public ActivityLog() {}
    public ActivityLog(String eventType, String reference, String description) {
        this.eventType = eventType; this.reference = reference; this.description = description;
    }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
