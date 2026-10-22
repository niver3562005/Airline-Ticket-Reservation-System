package com.aeronest.controller;

import com.aeronest.domain.Booking;
import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.CabinClass;
import com.aeronest.domain.DomainEnums.PaymentMethod;
import com.aeronest.service.BookingService;
import com.aeronest.service.AccountService;
import com.aeronest.service.BusinessRuleException;
import com.aeronest.service.FlightService;
import com.aeronest.service.SeatMapService;
import com.aeronest.service.DisruptionService;
import com.aeronest.service.JourneyServiceRequestService;
import com.aeronest.service.RefundService;
import com.aeronest.service.PromotionCatalogService;
import com.aeronest.domain.DomainEnums.DisruptionChoice;
import com.aeronest.domain.DomainEnums.JourneyServiceType;
import com.aeronest.web.BookingForm;
import com.aeronest.web.PassengerForm;
import com.aeronest.web.JourneyServiceForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
public class BookingController {
    private final BookingService bookings; private final FlightService flights; private final SeatMapService seats; private final AccountService accounts;
    private final DisruptionService disruptions;
    private final JourneyServiceRequestService journeyServices;
    private final RefundService refunds;
    private final PromotionCatalogService promotions;
    public BookingController(BookingService bookings, FlightService flights, SeatMapService seats, AccountService accounts,
                             DisruptionService disruptions, JourneyServiceRequestService journeyServices, RefundService refunds,
                             PromotionCatalogService promotions) {
        this.bookings = bookings; this.flights = flights; this.seats = seats; this.accounts = accounts;
        this.disruptions = disruptions; this.journeyServices = journeyServices;
        this.refunds = refunds;
        this.promotions = promotions;
    }

    @GetMapping("/book/{flightId}")
    public String form(@PathVariable Long flightId, @RequestParam(defaultValue = "1") int travellers,
                       @RequestParam(defaultValue = "ECONOMY") CabinClass cabin, Authentication authentication, Model model) {
        BookingForm form = new BookingForm(); form.setFlightId(flightId);
        for (int i = 0; i < Math.max(1, Math.min(6, travellers)); i++) { PassengerForm p = new PassengerForm(); p.setCabinClass(cabin); form.getPassengers().add(p); }
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            var account = accounts.accountFor(authentication.getName()); form.setContactName(account.getFullName());
            form.setContactEmail(account.getEmail()); form.setContactPhone(account.getPhone());
            form.getPassengers().get(0).setFirstName(account.getFirstName()); form.getPassengers().get(0).setLastName(account.getLastName());
        }
        model.addAttribute("bookingForm", form); loadBookingPage(model, flights.get(flightId)); return "public/book";
    }

    @PostMapping("/book")
    public String create(@Valid @ModelAttribute("bookingForm") BookingForm form, BindingResult result, Model model, RedirectAttributes redirect) {
        Flight flight = flights.get(form.getFlightId());
        if (result.hasErrors()) { loadBookingPage(model, flight); return "public/book"; }
        try { Booking booking = bookings.create(form); return "redirect:/booking/confirmed/" + booking.getPnr(); }
        catch (BusinessRuleException ex) { result.reject("booking", ex.getMessage()); loadBookingPage(model, flight); return "public/book"; }
    }

    @GetMapping("/booking/confirmed/{pnr}") public String confirmed(@PathVariable String pnr, Model model) {
        model.addAttribute("booking", bookings.findByPnr(pnr)); return "public/confirmation";
    }
    @GetMapping("/manage") public String manageForm() { return "public/manage"; }
    @PostMapping("/manage") public String manage(@RequestParam String pnr, @RequestParam String email, Model model) {
        try { loadManaged(model, bookings.find(pnr, email), email); }
        catch (RuntimeException ex) { model.addAttribute("error", ex.getMessage()); }
        return "public/manage";
    }
    @PostMapping("/manage/cancel") public String cancel(@RequestParam String pnr, @RequestParam String email,
                                                         @RequestParam(defaultValue = "Travel plans changed") String reason,
                                                         RedirectAttributes redirect) {
        try { bookings.cancel(pnr, email, reason); redirect.addFlashAttribute("success", "Booking " + pnr + " was cancelled. Your refund request is now being tracked."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/manage";
    }
    @PostMapping("/manage/disruption")
    public String disruption(@RequestParam String pnr, @RequestParam String email, @RequestParam DisruptionChoice choice,
                             @RequestParam(required = false) Long alternateFlightId, Model model) {
        try { disruptions.select(pnr, email, choice, alternateFlightId); model.addAttribute("success", "Your preferred cancellation arrangement has been saved and passenger care has been notified."); }
        catch (RuntimeException ex) { model.addAttribute("error", ex.getMessage()); }
        loadManaged(model, bookings.find(pnr, email), email); return "public/manage";
    }
    @PostMapping("/manage/priority-service")
    public String priorityService(@RequestParam String pnr, @RequestParam String email,
                                  @RequestParam JourneyServiceType type, @RequestParam Long passengerId,
                                  @RequestParam(required = false) String notes, Model model) {
        JourneyServiceForm form = new JourneyServiceForm(); form.setType(type); form.setPassengerId(passengerId); form.setNotes(notes);
        try {
            var saved = journeyServices.request(pnr, email, form);
            model.addAttribute("success", saved.getReference() + " has been sent to our airport team for confirmation.");
        } catch (RuntimeException ex) { model.addAttribute("error", ex.getMessage()); }
        loadManaged(model, bookings.find(pnr, email), email); return "public/manage";
    }
    @PostMapping("/check-in") public String checkIn(@RequestParam String pnr, @RequestParam String email, @RequestParam Long passengerId, Model model) {
        try { bookings.checkIn(pnr, email, passengerId); model.addAttribute("success", "Check-in complete. Your boarding pass is ready."); }
        catch (RuntimeException ex) { model.addAttribute("error", ex.getMessage()); }
        loadManaged(model, bookings.find(pnr, email), email); return "public/manage";
    }
    private void loadBookingPage(Model model, Flight flight) {
        model.addAttribute("flight", flight); model.addAttribute("seatMap", seats.seatMap(flight));
        model.addAttribute("cabins", CabinClass.values()); model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("promotions", promotions.activeOffers());
    }
    private void loadManaged(Model model, Booking booking, String email) {
        model.addAttribute("booking", booking); model.addAttribute("email", email);
        var disruption = disruptions.forBooking(booking); model.addAttribute("disruption", disruption);
        model.addAttribute("alternatives", disruption == null ? java.util.List.of() : disruptions.alternatives(disruption.getAffectedFlight()));
        model.addAttribute("disruptionChoices", DisruptionChoice.values());
        model.addAttribute("journeyServiceRequests", journeyServices.forBooking(booking));
        model.addAttribute("journeyServiceTypes", JourneyServiceType.values());
        if (booking.getStatus() != com.aeronest.domain.DomainEnums.BookingStatus.CANCELLED)
            model.addAttribute("refundQuote", refunds.quote(booking));
    }
}
