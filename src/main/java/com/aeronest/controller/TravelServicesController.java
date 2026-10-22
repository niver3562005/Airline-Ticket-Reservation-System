package com.aeronest.controller;

import com.aeronest.domain.CustomerAccount;
import com.aeronest.domain.DomainEnums.PartnerType;
import com.aeronest.service.AccountService;
import com.aeronest.service.BusinessRuleException;
import com.aeronest.service.TravelPartnerService;
import com.aeronest.web.PartnerReservationForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
public class TravelServicesController {
    private final TravelPartnerService services;
    private final AccountService accounts;

    public TravelServicesController(TravelPartnerService services, AccountService accounts) {
        this.services = services; this.accounts = accounts;
    }

    @GetMapping("/travel-services")
    public String services(@RequestParam(required = false) PartnerType type,
                           @RequestParam(required = false) Long partnerId,
                           @RequestParam(required = false) String confirmed,
                           Authentication authentication, Model model) {
        PartnerReservationForm form = new PartnerReservationForm(); form.setServiceAt(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0));
        form.setServiceEndsAt(LocalDateTime.now().plusDays(2).withSecond(0).withNano(0)); form.setPartnerId(partnerId);
        CustomerAccount account = customer(authentication);
        if (account != null) { form.setTravellerName(account.getFullName()); form.setEmail(account.getEmail()); form.setPhone(account.getPhone()); }
        model.addAttribute("reservationForm", form); model.addAttribute("confirmed", confirmed); load(model, type); return "public/travel-services";
    }

    @PostMapping("/travel-services/reservations")
    public String reserve(@Valid @ModelAttribute("reservationForm") PartnerReservationForm form, BindingResult result,
                          Authentication authentication, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { load(model, null); return "public/travel-services"; }
        try {
            var reservation = services.reserve(form, customer(authentication));
            redirect.addFlashAttribute("success", "Service request " + reservation.getReference() + " is confirmed in the travel desk queue.");
            return "redirect:/travel-services?confirmed=" + reservation.getReference();
        } catch (BusinessRuleException ex) {
            result.reject("reservation", ex.getMessage()); load(model, null); return "public/travel-services";
        }
    }

    @GetMapping("/cabin-guide") public String cabinGuide() { return "public/cabin-guide"; }

    private void load(Model model, PartnerType type) {
        model.addAttribute("partners", services.partners(type)); model.addAttribute("partnerTypes", PartnerType.values()); model.addAttribute("selectedType", type);
    }

    private CustomerAccount customer(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(value -> value.getAuthority().equals("ROLE_CUSTOMER"))) return null;
        return accounts.accountFor(authentication.getName());
    }
}
