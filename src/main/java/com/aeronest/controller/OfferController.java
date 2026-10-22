package com.aeronest.controller;

import com.aeronest.service.PromotionCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OfferController {
    private final PromotionCatalogService promotions;

    public OfferController(PromotionCatalogService promotions) { this.promotions = promotions; }

    @GetMapping("/deals")
    public String deals(Model model) {
        model.addAttribute("promotions", promotions.activeOffers());
        return "public/deals";
    }

    @GetMapping("/payment-options")
    public String paymentOptions() { return "public/payment-options"; }
}
