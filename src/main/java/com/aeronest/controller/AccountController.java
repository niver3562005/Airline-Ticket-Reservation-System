package com.aeronest.controller;

import com.aeronest.domain.CustomerAccount;
import com.aeronest.service.AccountService;
import com.aeronest.service.BusinessRuleException;
import com.aeronest.service.FeedbackService;
import com.aeronest.service.NotificationService;
import com.aeronest.service.TravelPartnerService;
import com.aeronest.web.PasswordChangeForm;
import com.aeronest.web.ProfileForm;
import com.aeronest.web.FeedbackForm;
import com.aeronest.domain.DomainEnums.FeedbackCategory;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {
    private final AccountService accounts;
    private final NotificationService notifications;
    private final FeedbackService feedback;
    private final TravelPartnerService travelPartners;
    public AccountController(AccountService accounts, NotificationService notifications, FeedbackService feedback, TravelPartnerService travelPartners) {
        this.accounts = accounts; this.notifications = notifications; this.feedback = feedback; this.travelPartners = travelPartners;
    }

    @GetMapping("/account")
    public String account(Authentication authentication, Model model) {
        CustomerAccount account = accounts.accountFor(authentication.getName());
        model.addAttribute("account", account); model.addAttribute("bookings", accounts.bookingHistory(account.getEmail()));
        model.addAttribute("notifications", notifications.forAccount(account).stream().limit(4).toList());
        model.addAttribute("unreadNotifications", notifications.unreadCount(account));
        model.addAttribute("partnerReservations", travelPartners.forAccount(account));
        return "account/dashboard";
    }

    @GetMapping("/account/profile")
    public String profile(Authentication authentication, Model model) {
        CustomerAccount account = accounts.accountFor(authentication.getName()); model.addAttribute("account", account);
        if (!model.containsAttribute("profileForm")) model.addAttribute("profileForm", accounts.profileForm(account));
        if (!model.containsAttribute("passwordChangeForm")) model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        return "account/profile";
    }

    @PostMapping("/account/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileForm profileForm, BindingResult result,
                                Authentication authentication, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("account", accounts.accountFor(authentication.getName()));
            model.addAttribute("passwordChangeForm", new PasswordChangeForm()); return "account/profile";
        }
        accounts.updateProfile(authentication.getName(), profileForm); redirect.addFlashAttribute("success", "Profile preferences saved.");
        notifications.profileUpdated(accounts.accountFor(authentication.getName()));
        return "redirect:/account/profile";
    }

    @PostMapping("/account/password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeForm passwordChangeForm, BindingResult result,
                                 Authentication authentication, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            CustomerAccount account = accounts.accountFor(authentication.getName()); model.addAttribute("account", account);
            model.addAttribute("profileForm", accounts.profileForm(account)); return "account/profile";
        }
        try { accounts.changePassword(authentication.getName(), passwordChangeForm); redirect.addFlashAttribute("success", "Password changed securely."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/account/profile";
    }

    @GetMapping("/account/notifications")
    public String notifications(Authentication authentication, Model model) {
        CustomerAccount account = accounts.accountFor(authentication.getName()); model.addAttribute("account", account);
        model.addAttribute("notifications", notifications.forAccount(account)); return "account/notifications";
    }

    @PostMapping("/account/notifications/{id}/read")
    public String readNotification(@PathVariable Long id, Authentication authentication) {
        notifications.markRead(id, accounts.accountFor(authentication.getName())); return "redirect:/account/notifications";
    }

    @GetMapping("/account/feedback")
    public String feedback(Authentication authentication, Model model) {
        CustomerAccount account = accounts.accountFor(authentication.getName()); model.addAttribute("account", account);
        if (!model.containsAttribute("feedbackForm")) model.addAttribute("feedbackForm", new FeedbackForm());
        model.addAttribute("categories", FeedbackCategory.values()); model.addAttribute("feedbackHistory", feedback.forAccount(account));
        return "account/feedback";
    }

    @PostMapping("/account/feedback")
    public String submitFeedback(@Valid @ModelAttribute FeedbackForm feedbackForm, BindingResult result,
                                 Authentication authentication, Model model, RedirectAttributes redirect) {
        CustomerAccount account = accounts.accountFor(authentication.getName());
        if (result.hasErrors()) {
            model.addAttribute("account", account); model.addAttribute("categories", FeedbackCategory.values());
            model.addAttribute("feedbackHistory", feedback.forAccount(account)); return "account/feedback";
        }
        try { feedback.submit(account, feedbackForm); redirect.addFlashAttribute("success", "Thank you. Your feedback is now with our care team."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/account/feedback";
    }
}
