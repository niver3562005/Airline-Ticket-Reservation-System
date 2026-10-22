package com.aeronest.controller;

import com.aeronest.service.AccountService;
import com.aeronest.service.BusinessRuleException;
import com.aeronest.web.PasswordResetForm;
import com.aeronest.web.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final AccountService accounts;
    public AuthController(AccountService accounts) { this.accounts = accounts; }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            boolean customer = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
            return "redirect:" + (customer ? "/account" : "/admin");
        }
        return "public/login";
    }

    @GetMapping("/register") public String register(Model model) {
        if (!model.containsAttribute("registrationForm")) model.addAttribute("registrationForm", new RegistrationForm());
        return "public/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationForm registrationForm, BindingResult result,
                           Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return "public/register";
        try {
            String token = accounts.register(registrationForm);
            redirect.addFlashAttribute("verificationUrl", "/verify-email?token=" + token);
            redirect.addFlashAttribute("registeredEmail", registrationForm.getEmail());
            return "redirect:/verification-sent";
        } catch (BusinessRuleException ex) {
            result.rejectValue("email", "account.exists", ex.getMessage()); return "public/register";
        }
    }

    @GetMapping("/verification-sent") public String verificationSent() { return "public/verification-sent"; }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirect) {
        try { accounts.verifyEmail(token); redirect.addFlashAttribute("success", "Email verified. You can now sign in."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/login";
    }

    @GetMapping("/forgot-password") public String forgotPassword() { return "public/forgot-password"; }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {
        model.addAttribute("submitted", true);
        accounts.requestPasswordReset(email).ifPresent(token -> model.addAttribute("resetUrl", "/reset-password?token=" + token));
        return "public/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        if (!accounts.isResetTokenValid(token)) { model.addAttribute("invalid", true); return "public/reset-password"; }
        PasswordResetForm form = new PasswordResetForm(); form.setToken(token); model.addAttribute("passwordResetForm", form);
        return "public/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute PasswordResetForm passwordResetForm, BindingResult result,
                                Model model, RedirectAttributes redirect) {
        if (!passwordResetForm.getPassword().equals(passwordResetForm.getConfirmPassword()))
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords must match");
        if (result.hasErrors()) return "public/reset-password";
        try { accounts.resetPassword(passwordResetForm); redirect.addFlashAttribute("success", "Password updated. Sign in with your new password."); return "redirect:/login"; }
        catch (BusinessRuleException ex) { model.addAttribute("error", ex.getMessage()); return "public/reset-password"; }
    }
}
