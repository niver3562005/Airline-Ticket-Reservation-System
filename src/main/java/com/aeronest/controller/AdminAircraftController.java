package com.aeronest.controller;

import com.aeronest.domain.Aircraft;
import com.aeronest.service.AircraftService;
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

@Controller @RequestMapping("/admin/aircraft")
public class AdminAircraftController {
    private final AircraftService aircraft;
    public AdminAircraftController(AircraftService aircraft) { this.aircraft = aircraft; }
    @GetMapping public String list(Model model) { model.addAttribute("aircraftList", aircraft.all()); return "aircraft/list"; }
    @GetMapping("/new") public String create(Model model) { model.addAttribute("aircraft", new Aircraft()); return "aircraft/form"; }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id, Model model) { model.addAttribute("aircraft", aircraft.get(id)); return "aircraft/form"; }
    @PostMapping
    public String save(@Valid @ModelAttribute Aircraft value, BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) return "aircraft/form";
        try { aircraft.save(value); redirect.addFlashAttribute("success", "Aircraft saved successfully."); return "redirect:/admin/aircraft"; }
        catch (BusinessRuleException ex) { result.reject("aircraft", ex.getMessage()); return "aircraft/form"; }
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try { aircraft.delete(id); redirect.addFlashAttribute("success", "Aircraft deleted."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/aircraft";
    }
}
