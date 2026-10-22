package com.aeronest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController {
    @GetMapping("/privacy-policy") public String privacy() { return "public/privacy-policy"; }
    @GetMapping("/cookie-policy") public String cookies() { return "public/cookie-policy"; }
    @GetMapping("/legal-notice") public String legal() { return "public/legal-notice"; }
}
