package com.aeronest.controller;

import com.aeronest.service.BookingService;
import com.aeronest.service.BusinessRuleException;
import com.aeronest.domain.DomainEnums.BoardingStatus;
import com.aeronest.domain.DomainEnums.SecurityStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/operations/bookings")
public class AdminBookingController {
    private final BookingService bookings;
    public AdminBookingController(BookingService bookings) { this.bookings = bookings; }
    @GetMapping public String list(Model model) { model.addAttribute("bookings", bookings.all()); return "bookings/list"; }
    @GetMapping("/{pnr}") public String detail(@PathVariable String pnr, Model model) {
        model.addAttribute("booking", bookings.findByPnr(pnr)); model.addAttribute("securityStatuses", SecurityStatus.values());
        model.addAttribute("boardingStatuses", BoardingStatus.values()); return "bookings/detail";
    }
    @PostMapping("/{pnr}/passengers/{passengerId}/status")
    public String passengerStatus(@PathVariable String pnr, @PathVariable Long passengerId,
                                  @RequestParam SecurityStatus securityStatus, @RequestParam BoardingStatus boardingStatus,
                                  RedirectAttributes redirect) {
        try { bookings.updatePassengerOperations(passengerId, securityStatus, boardingStatus); redirect.addFlashAttribute("success", "Passenger readiness updated and notifications queued."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/operations/bookings/" + pnr;
    }
}
