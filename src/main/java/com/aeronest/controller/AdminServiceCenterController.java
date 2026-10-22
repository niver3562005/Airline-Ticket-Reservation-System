package com.aeronest.controller;

import com.aeronest.service.BusinessRuleException;
import com.aeronest.service.FeedbackService;
import com.aeronest.service.NotificationService;
import com.aeronest.service.RefundService;
import com.aeronest.service.TravelPartnerService;
import com.aeronest.service.DisruptionService;
import com.aeronest.service.JourneyServiceRequestService;
import com.aeronest.domain.DomainEnums.PartnerReservationStatus;
import com.aeronest.domain.DomainEnums.JourneyServiceStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminServiceCenterController {
    private final RefundService refunds;
    private final FeedbackService feedback;
    private final NotificationService notifications;
    private final TravelPartnerService travelPartners;
    private final DisruptionService disruptions;
    private final JourneyServiceRequestService journeyServices;

    public AdminServiceCenterController(RefundService refunds, FeedbackService feedback, NotificationService notifications,
                                        TravelPartnerService travelPartners, DisruptionService disruptions,
                                        JourneyServiceRequestService journeyServices) {
        this.refunds = refunds; this.feedback = feedback; this.notifications = notifications;
        this.travelPartners = travelPartners; this.disruptions = disruptions; this.journeyServices = journeyServices;
    }

    @GetMapping("/admin/service-center")
    public String serviceCenter(Model model) {
        model.addAttribute("refunds", refunds.all()); model.addAttribute("feedbackItems", feedback.all());
        model.addAttribute("deliveries", notifications.recentDeliveries().stream().limit(20).toList());
        model.addAttribute("pendingRefunds", refunds.pendingCount()); model.addAttribute("newFeedback", feedback.newCount());
        model.addAttribute("partnerReservations", travelPartners.allReservations()); model.addAttribute("partnerStatuses", PartnerReservationStatus.values());
        model.addAttribute("disruptions", disruptions.all()); model.addAttribute("disruptionsRequiringAction", disruptions.requiringAction());
        model.addAttribute("journeyServiceRequests", journeyServices.all()); model.addAttribute("journeyServiceStatuses", JourneyServiceStatus.values());
        return "admin/service-center";
    }

    @PostMapping("/admin/service-center/refunds/{id}/complete")
    public String completeRefund(@PathVariable Long id, RedirectAttributes redirect) {
        try { refunds.complete(id); redirect.addFlashAttribute("success", "Refund completed and passenger notifications sent."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/service-center";
    }

    @PostMapping("/admin/service-center/feedback/{id}/resolve")
    public String resolveFeedback(@PathVariable Long id, @RequestParam String response, RedirectAttributes redirect) {
        try { feedback.resolve(id, response); redirect.addFlashAttribute("success", "Feedback response saved and case resolved."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/service-center";
    }

    @PostMapping("/admin/service-center/partners/{id}/status")
    public String partnerStatus(@PathVariable Long id, @RequestParam PartnerReservationStatus status, RedirectAttributes redirect) {
        travelPartners.updateStatus(id, status); redirect.addFlashAttribute("success", "Partner reservation status updated.");
        return "redirect:/admin/service-center";
    }

    @PostMapping("/admin/service-center/journey-services/{id}/status")
    public String journeyServiceStatus(@PathVariable Long id, @RequestParam JourneyServiceStatus status, RedirectAttributes redirect) {
        journeyServices.updateStatus(id, status); redirect.addFlashAttribute("success", "Airport journey service status updated.");
        return "redirect:/admin/service-center";
    }

    @PostMapping("/admin/service-center/disruptions/{id}/complete")
    public String completeDisruption(@PathVariable Long id, RedirectAttributes redirect) {
        try { disruptions.complete(id); redirect.addFlashAttribute("success", "Passenger cancellation arrangement completed and notifications sent."); }
        catch (BusinessRuleException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/service-center";
    }
}
