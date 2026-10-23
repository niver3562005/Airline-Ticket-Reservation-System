package com.aeronest.controller;

import com.aeronest.domain.Flight;
import com.aeronest.service.AirportService;
import com.aeronest.service.BusinessRuleException;
import com.aeronest.service.FlightService;
import com.aeronest.service.SeatMapService;
import com.aeronest.web.FlightSearchForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PublicController {
    private final AirportService airports; private final FlightService flights; private final SeatMapService seats;
    public PublicController(AirportService airports, FlightService flights, SeatMapService seats) { this.airports = airports; this.flights = flights; this.seats = seats; }
    @GetMapping("/") public String home(Model model) {
        model.addAttribute("search", new FlightSearchForm()); model.addAttribute("airports", airports.active());
        model.addAttribute("featuredFlights", flights.featured()); return "public/home";
    }
    @GetMapping("/flight-schedule")
    public String schedule(@RequestParam(required = false) String route, Model model) {
        model.addAttribute("schedule", flights.publicSchedule(route)); model.addAttribute("route", route == null ? "" : route);
        return "public/flight-schedule";
    }
    @GetMapping("/flights")
    public String search(@Valid @ModelAttribute("search") FlightSearchForm form, BindingResult result, Model model) {
        model.addAttribute("airports", airports.active());
        if (result.hasErrors()) return "public/home";
        try {
            model.addAttribute("routeOrigin", airports.get(form.getOriginId()));
            model.addAttribute("routeDestination", airports.get(form.getDestinationId()));
            List<Flight> matches = flights.search(form.getOriginId(), form.getDestinationId(), form.getDepartureDate());
            Map<Long, Integer> availableSeats = matches.stream().collect(Collectors.toMap(Flight::getId,
                    flight -> seats.availableSeats(flight, form.getCabinClass()).size()));
            model.addAttribute("flights", matches); model.addAttribute("availableSeats", availableSeats);
            model.addAttribute("connections", flights.searchConnections(form.getOriginId(), form.getDestinationId(), form.getDepartureDate()));
        }
        catch (BusinessRuleException ex) { result.reject("search", ex.getMessage()); return "public/home"; }
        return "public/search-results";
    }
}
