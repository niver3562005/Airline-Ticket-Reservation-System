package com.aeronest.controller;

import com.aeronest.service.BusinessRuleException;
import com.aeronest.service.FlightService;
import com.aeronest.service.ResourceNotFoundException;
import com.aeronest.service.TravelExperienceService;
import com.aeronest.service.FlightAvailabilityService;
import com.aeronest.service.FlightTelemetryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

@Controller
public class TravelHubController {
    private final TravelExperienceService experience;
    private final FlightService flights;
    private final FlightAvailabilityService availability;
    private final FlightTelemetryService telemetry;

    public TravelHubController(TravelExperienceService experience, FlightService flights,
                               FlightAvailabilityService availability, FlightTelemetryService telemetry) {
        this.experience = experience; this.flights = flights; this.availability = availability; this.telemetry = telemetry;
    }

    @GetMapping({"/travel-hub", "/support"})
    public String hub(@RequestParam(required = false) String visaCode,
                      @RequestParam(required = false) String flightNumber,
                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate,
                      Model model) {
        loadHub(model);
        if (visaCode != null && !visaCode.isBlank()) {
            try { model.addAttribute("visaResult", experience.country(visaCode)); }
            catch (ResourceNotFoundException ex) { model.addAttribute("visaError", ex.getMessage()); }
        }
        if (flightNumber != null && !flightNumber.isBlank()) {
            try {
                var flight = flights.status(flightNumber, flightDate == null ? LocalDate.now() : flightDate);
                model.addAttribute("statusFlight", flight); model.addAttribute("availability", availability.forFlight(flight));
                model.addAttribute("telemetry", telemetry.snapshot(flight));
            }
            catch (BusinessRuleException | ResourceNotFoundException ex) { model.addAttribute("statusError", ex.getMessage()); }
        }
        model.addAttribute("selectedFlightDate", flightDate == null ? LocalDate.now().plusDays(1) : flightDate);
        model.addAttribute("selectedFlightNumber", flightNumber == null ? "" : flightNumber);
        return "public/travel-hub";
    }

    @GetMapping("/flight-status")
    public String status(@RequestParam(required = false) String flightNumber,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate,
                         Model model) { return hub(null, flightNumber, flightDate, model); }

    @GetMapping("/airport-guide/{iata}")
    public String airport(@PathVariable String iata, Model model) {
        loadHub(model); model.addAttribute("selectedAirport", iata.toUpperCase());
        model.addAttribute("selectedFacilities", experience.facilities(iata)); return "public/travel-hub";
    }

    @GetMapping("/packages")
    public String packages(Model model) { loadHub(model); model.addAttribute("showAllPackages", true); return "public/travel-hub"; }

    private void loadHub(Model model) {
        model.addAttribute("countries", experience.countries()); model.addAttribute("packages", experience.packages());
        model.addAttribute("facilities", experience.facilities());
    }
}
