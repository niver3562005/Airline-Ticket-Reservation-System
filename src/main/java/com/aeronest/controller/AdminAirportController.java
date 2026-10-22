package com.aeronest.controller;

import com.aeronest.domain.Airport;
import com.aeronest.service.AirportService;
import com.aeronest.service.BusinessRuleException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/admin/airports")
public class AdminAirportController {
    private final AirportService airports;
    public AdminAirportController(AirportService airports) { this.airports = airports; }
    @GetMapping public String list(Model model) { model.addAttribute("airports", airports.all()); return "airports/list"; }
    @GetMapping("/new") public String create(Model model) { model.addAttribute("airport", new Airport()); return "airports/form"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id, Model model) { model.addAttribute("airport", airports.get(id)); return "airports/form"; }
    @PostMapping
    public String save(@Valid @ModelAttribute Airport airport, BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) return "airports/form";
        try { airports.save(airport); redirect.addFlashAttribute("success", "Airport saved successfully."); return "redirect:/admin/airports"; }
        catch (BusinessRuleException ex) { result.reject("airport", ex.getMessage()); return "airports/form"; }
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try { airports.delete(id); redirect.addFlashAttribute("success", "Airport deleted."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/airports";
    }
}
