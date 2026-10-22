package com.aeronest.controller;

import com.aeronest.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {
    private final DashboardService dashboard;
    public AdminDashboardController(DashboardService dashboard) { this.dashboard = dashboard; }
    @GetMapping("/admin") public String dashboard(Model model) { model.addAttribute("dashboard", dashboard.snapshot()); return "admin/dashboard"; }
}
