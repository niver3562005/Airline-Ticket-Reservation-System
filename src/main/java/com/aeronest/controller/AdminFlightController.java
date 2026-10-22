package com.aeronest.controller;

import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.service.AircraftService;
import com.aeronest.service.AirportService;
import com.aeronest.service.BusinessRuleException;
import com.aeronest.service.FlightService;
import com.aeronest.service.SeatMapService;
import com.aeronest.service.FlightAvailabilityService;
import com.aeronest.service.FlightTelemetryService;
import com.aeronest.web.TelemetryForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/admin/flights")
public class AdminFlightController {
    private final FlightService flights; private final AirportService airports; private final AircraftService aircraft; private final SeatMapService seats;
    private final FlightAvailabilityService availability; private final FlightTelemetryService telemetry;
    public AdminFlightController(FlightService flights, AirportService airports, AircraftService aircraft, SeatMapService seats,
                                 FlightAvailabilityService availability, FlightTelemetryService telemetry) {
        this.flights = flights; this.airports = airports; this.aircraft = aircraft; this.seats = seats;
        this.availability = availability; this.telemetry = telemetry;
    }
    @GetMapping public String list(Model model) { model.addAttribute("flights", flights.all()); model.addAttribute("statuses", FlightStatus.values()); return "flights/list"; }
    @GetMapping("/new") public String create(Model model) { model.addAttribute("flight", new Flight()); loadOptions(model); return "flights/form"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id, Model model) { model.addAttribute("flight", flights.get(id)); loadOptions(model); return "flights/form"; }
    @GetMapping("/{id}") public String detail(@PathVariable Long id, Model model) {
        Flight f = flights.get(id); model.addAttribute("flight", f); model.addAttribute("seatMap", seats.seatMap(f));
        model.addAttribute("availability", availability.forFlight(f)); model.addAttribute("telemetry", telemetry.snapshot(f));
        if (!model.containsAttribute("telemetryForm")) model.addAttribute("telemetryForm", telemetry.form(f)); return "flights/detail";
    }
    @PostMapping
    public String save(@Valid @ModelAttribute Flight flight, BindingResult result,
                       @RequestParam Long originId, @RequestParam Long destinationId, @RequestParam Long aircraftId,
                       Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { loadOptions(model); return "flights/form"; }
        try { flights.save(flight, originId, destinationId, aircraftId); redirect.addFlashAttribute("success", "Flight saved successfully."); return "redirect:/admin/flights"; }
        catch (BusinessRuleException ex) { result.reject("flight", ex.getMessage()); loadOptions(model); return "flights/form"; }
    }
    @PostMapping("/{id}/status") public String status(@PathVariable Long id, @RequestParam FlightStatus status, RedirectAttributes redirect) {
        flights.updateStatus(id, status); redirect.addFlashAttribute("success", "Flight status updated."); return "redirect:/admin/flights";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try { flights.delete(id); redirect.addFlashAttribute("success", "Flight deleted."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/flights";
    }
    @PostMapping("/{id}/telemetry")
    public String telemetry(@PathVariable Long id, @Valid @ModelAttribute TelemetryForm telemetryForm, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { detail(id, model); model.addAttribute("telemetryForm", telemetryForm); return "flights/detail"; }
        telemetry.update(flights.get(id), telemetryForm); redirect.addFlashAttribute("success", "Live operations telemetry updated.");
        return "redirect:/admin/flights/" + id;
    }
    private void loadOptions(Model model) { model.addAttribute("airports", airports.active()); model.addAttribute("aircraftList", aircraft.active()); model.addAttribute("statuses", FlightStatus.values()); }
}
