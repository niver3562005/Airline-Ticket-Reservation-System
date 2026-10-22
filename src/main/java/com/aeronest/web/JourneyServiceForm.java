package com.aeronest.web;

import com.aeronest.domain.DomainEnums.JourneyServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class JourneyServiceForm {
    @NotNull private JourneyServiceType type;
    @NotNull private Long passengerId;
    @Size(max = 500) private String notes;

    public JourneyServiceType getType() { return type; }
    public void setType(JourneyServiceType type) { this.type = type; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
